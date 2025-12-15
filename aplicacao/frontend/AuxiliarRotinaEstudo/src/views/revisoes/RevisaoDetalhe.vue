<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { concluirRevisao, reagendarDataRevisao } from '@/api/RevisaoService';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData, validarFormatoData } from '@/utils/dateUtils';
import { ref, watch } from 'vue';

const props = defineProps<{
  revisao: RevisaoResponseInterface | null;
}>();

const emit = defineEmits<{
  (e: 'atualizar', payload: Partial<RevisaoResponseInterface>): void;
  (e: 'fechar'): void;
  (e: 'concluir'): void;

}>();

const modoReagendar = ref(false);
const novaData = ref('');
const loading = ref(false);
const nomeDisciplina = ref<string>('');

watch(() => props.revisao, async (newVal) => {
  if (newVal?.idEstudo) {
    try {
      const nome = await nomeDisciplinaDoEstudo(newVal.idEstudo);
      nomeDisciplina.value = nome;
    } catch (err) {
      console.error('Erro ao buscar nome da disciplina:', err);
      nomeDisciplina.value = '—';
    }
  }
}, { immediate: true });

function abrirReagendar(){
  modoReagendar.value = true;
  if(props.revisao){
    novaData.value = props.revisao.dataRevisao;
  } 
};

async function concluir() {
  if(!props.revisao) return;
  loading.value = true;
  try{
    const revisaoConcluida = await concluirRevisao(props.revisao.id);
    emit('atualizar', revisaoConcluida);
    emit('concluir');
    emit('fechar');
  }catch (error) {
    console.error('Erro ao concluir revisão:', error);
    alert('Erro ao concluir revisão');
  } finally {
    loading.value = false;
  }
};

async function reagendar(){
  if(!props.revisao || !novaData.value) return;
  
  if (!validarFormatoData(novaData.value)) {
    alert('Data inválida. Use o formato DD/MM/AAAA');
    return;
  }
  
  loading.value = true;
  try{
    const revisaoReagendada = await reagendarDataRevisao(props.revisao.id, novaData.value);
    
    emit('atualizar', revisaoReagendada);
    
    // ver se precisa mostrar mensagem
    const novaDataObj = converterStringParaData(novaData.value);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (novaDataObj < hoje) {
      setTimeout(() => {
        alert('Revisão reagendada para data passada. Verifique a aba "Atrasadas".');
      }, 300);
    }
    
    modoReagendar.value = false;
    emit("fechar");
    
  }catch (error) {
    console.error('Erro ao reagendar revisão:', error);
    alert('Erro ao reagendar revisão: ' + error);
  } finally {
    loading.value = false;
  }
}


</script>

<template>
  <v-dialog :model-value="!!revisao" max-width="600px" @update:model-value="!$event && $emit('fechar')">
    <v-card v-if="revisao">
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Detalhes da Revisão</span>
        <v-btn icon @click="$emit('fechar')" elevation="0">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>

      <v-card-text>
        <v-list>
          <v-list-item>
            <template #prepend>
              <v-icon>mdi-calendar</v-icon>
            </template>
            <v-list-item-title>Data da Revisão</v-list-item-title>
            <v-list-item-subtitle>{{ revisao.dataRevisao }}</v-list-item-subtitle>
          </v-list-item>

          <v-list-item>
            <template #prepend>
              <v-icon>mdi-check-circle</v-icon>
            </template>
            <v-list-item-title>Status</v-list-item-title>
            <v-list-item-subtitle>
              <v-chip :color="revisao.concluida ? 'success' : 'warning'" size="small">
                {{ revisao.concluida ? 'Concluída' : 'Pendente' }}
              </v-chip>
            </v-list-item-subtitle>
          </v-list-item>

          <v-list-item>
            <template #prepend>
              <v-icon>mdi-book</v-icon>
            </template>
            <v-list-item-title>Disciplina Relacionada {{ revisao.id }}</v-list-item-title>
            <v-list-item-subtitle>{{ nomeDisciplina || 'Carregando...' }}</v-list-item-subtitle>
          </v-list-item>
        </v-list>

        <div v-if="modoReagendar" class="mt-4">
          <v-text-field
            v-model="novaData"
            label="Nova Data"
            placeholder="DD/MM/AAAA"
            v-mask="'##/##/####'"
            variant="outlined"
            density="compact"
            :rules="[v => !!v || 'Data é obrigatória']"
          />
          <div class="d-flex gap-2 mt-4 justify-end">
            <v-btn  variant="outlined" color="grey" @click="modoReagendar = false">         
               <v-icon icon="mdi-close" class="mr-2" /> 
               Cancelar
            </v-btn>
            <v-btn color="primary" @click="reagendar" :loading="loading">
              <v-icon icon="mdi-check" class="mr-2" />
              Reagendar
            </v-btn>
          </div>
        </div>
      </v-card-text>

      <v-card-actions v-if="!revisao.concluida && !modoReagendar">
        <v-spacer />
        <v-btn color="warning" @click="abrirReagendar">
          Reagendar
        </v-btn>
        <v-btn color="success" @click="concluir" :loading="loading">
          <v-icon icon="mdi-check" class="mr-2" />
          Concluir
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.gap-2 {
  gap: 8px;
}
</style>  