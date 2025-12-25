<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { useRevisaoStore } from '@/stores/revisaoStore';
import type { RevisaoResponseInterface } from '@/types';
import { computed, ref, watch } from 'vue';

const props = defineProps<{
  revisao: RevisaoResponseInterface | null;
  origemLista?: boolean;  
}>();

const emit = defineEmits<{
  (e: 'atualizar', payload: RevisaoResponseInterface): void;
  (e: 'fechar'): void;
}>();

const revisaoStore = useRevisaoStore();
const loading = ref(false);
const nomeDisciplina = ref<string>(''); 


const mostrarBotaoConcluir = computed(() => {
  return props.revisao && !props.revisao?.concluida && !props.origemLista;
});


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

async function concluir() {
  if(!props.revisao) return;
  loading.value = true;
  try{
    const revisaoConcluida = await revisaoStore.concluirRevisao(props.revisao.id);
    emit('atualizar', revisaoConcluida);
    emit('fechar');
  }catch (error) {
    console.error('Erro ao concluir revisão:', error);
    alert('Erro ao concluir revisão');
  } finally {
    loading.value = false;
  }
};


function fecharModal() {
  emit('fechar');
}
</script>
<template>
  <v-dialog :model-value="!!revisao" max-width="600px" @update:model-value="!$event && fecharModal()">
    <v-card v-if="revisao">
      <v-card-title class="d-flex justify-space-between align-center">
        <span>Detalhes da Revisão</span>
        <v-btn icon @click="fecharModal" elevation="0">
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
                {{ revisao.concluida ? 'Concluída' : 'Aguardando conclusão' }}
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
      </v-card-text>

      <v-card-actions v-if="mostrarBotaoConcluir">
        <v-spacer />
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