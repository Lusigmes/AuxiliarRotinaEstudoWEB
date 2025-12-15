<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { carregarRevisoesPendentesPaginado, concluirRevisao, reagendarDataRevisao } from '@/api/RevisaoService';
import { usePagination } from '@/composables/usePagination';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData, formatarDataParaPTBR, validarFormatoData } from '@/utils/dateUtils';
import { watch } from 'vue';
import { onMounted } from 'vue';
import { computed, ref } from 'vue';

const {
  items:revisoes,
  page,
  totalPages,
  atualizarPagina,
} = usePagination<RevisaoResponseInterface>(carregarRevisoesPendentesPaginado, 6);


const emit = defineEmits<{
  (e: 'atualizar', payload: RevisaoResponseInterface): void;
  (e: 'fechar'): void;
}>();

const mudarPagina= async (novaPag: number) => {
  await atualizarPagina(novaPag - 1);
};

async function carregarRevisoes(pagina: number = page.value){
  await atualizarPagina(pagina);
};

const revisoesPendentes = computed(() => {
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  
  return revisoes.value
    .filter(revisao => !revisao.concluida)
    .sort((a, b) => {
      const dataA = converterStringParaData(a.dataRevisao);
      const dataB = converterStringParaData(b.dataRevisao);
      return dataA.getTime() - dataB.getTime();
    });
});

const revisaoParaReagendar = ref<RevisaoResponseInterface | null>(null);
const showReagendarDialog = ref(false);
const novaData = ref('');
const loading = ref(false);
const loadingConclusao = ref<number | null>(null);
 

async function concluirRevisaoItem(revisao: RevisaoResponseInterface) {
  loadingConclusao.value = revisao.id;
  try {
    const revisaoConcluida = await concluirRevisao(revisao.id);
    
    const index = revisoes.value.findIndex(r => r.id === revisao.id);
    if (index !== -1) {
      revisoes.value.splice(index, 1);
      
      if (revisoes.value.length === 0 && page.value > 0) {
        await carregarRevisoes(page.value - 1);
      } else {
        await carregarRevisoes(page.value);
      }
    }
    
    emit('atualizar', revisaoConcluida);
    
  } catch (error) {
    console.error('Erro ao concluir revisão:', error);
    alert('Erro ao concluir revisão');
    await carregarRevisoes(page.value);
  } finally {
    loadingConclusao.value = null;
  }
};

function abrirModalReagendar(revisao: RevisaoResponseInterface){
  revisaoParaReagendar.value = revisao;
  novaData.value = revisao.dataRevisao;
  showReagendarDialog.value = true;
};

function fecharModalReagendar(){
  revisaoParaReagendar.value = null;
  novaData.value = '';
  showReagendarDialog.value = false;
};

async function confirmarReagendamento(){
  if(!revisaoParaReagendar.value || !novaData.value) return;

  if (!validarFormatoData(novaData.value)) {
    alert('Data inválida. Use o formato DD/MM/AAAA');
    return;
  }
  loading.value = true;

  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const dataInformada = converterStringParaData(novaData.value);
  const cincoDiasAtras = new Date();
  cincoDiasAtras.setDate(cincoDiasAtras.getDate() - 5);
  cincoDiasAtras.setHours(0, 0, 0, 0);

  if (dataInformada < cincoDiasAtras) {
    alert('Limite para reagendamento: até 5 dias atrás');
    return;
  }
  try {
    const revisaoReagendada = await reagendarDataRevisao(revisaoParaReagendar.value.id, novaData.value);
    
    const index = revisoes.value.findIndex(r => r.id === revisaoParaReagendar.value!.id);
    if (index !== -1) {
      revisoes.value[index] = { ...revisaoReagendada };
      
      const textoParaData = converterStringParaData(novaData.value);
      const hoje = new Date();
      hoje.setHours(0, 0, 0, 0);
      
      if (textoParaData < hoje) {
        revisoes.value.splice(index, 1);
      }
    }
    
    emit('atualizar', revisaoReagendada);

    await carregarRevisoes(page.value);
    
    fecharModalReagendar();
    
    // criar naotification store paraa tratar
    const textoParaData = converterStringParaData(novaData.value);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (textoParaData < hoje) {
      setTimeout(() => {
        alert('Revisão reagendada para data passada. Verifique a aba "Atrasadas".');
      }, 300);
    }
    
  } catch (error) {
    console.error('Erro ao reagendar revisão:', error);
    alert('Erro ao reagendar revisão: ' + error);
  } finally {
    loading.value = false;
  }
}

const nomeDisciplinas = ref<Record<number, string>>({});

async function fetchNomeDisciplina(idEstudo: number) {
  if (!idEstudo) return;
  if (nomeDisciplinas.value[idEstudo]) return;
  try {
    const nome = await nomeDisciplinaDoEstudo(idEstudo);
    nomeDisciplinas.value[idEstudo] = nome;
  } catch (err) {
    console.error('Erro ao buscar nome da disciplina:', err);
    nomeDisciplinas.value[idEstudo] = '—';
  }
}

onMounted(async () => {
  await carregarRevisoes(page.value);
  revisoes.value.forEach(r => fetchNomeDisciplina(r.idEstudo));
});

watch(() => revisoes.value, (newVal) => {
  newVal.forEach(r => fetchNomeDisciplina(r.idEstudo));
}, { deep: true });

</script>

<template>
  <v-card variant="flat">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Revisões Atrasadas</span>
      <v-chip color="warning">
        {{ revisoesPendentes.length }}
      </v-chip>
    </v-card-title>
    
    
    <v-card-text>
      <v-table v-if="revisoesPendentes.length > 0">
        <thead>
          <tr>
            <th>Data</th>
            <th>Disciplina</th>
            <th>Status</th>
            <th class="d-flex justify-end">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="revisao in revisoesPendentes" :key="revisao.id">
            <td>
              <v-chip :color="revisao.dataRevisao < new Date().toLocaleDateString('pt-BR') ? 'red' : 'blue'">
                {{ revisao.dataRevisao }}
              </v-chip>
            </td>
            <td>
             <v-chip color="primary" variant="flat">
                  {{ nomeDisciplinas[revisao.idEstudo] || "Carregando..." }}
              </v-chip>
            </td> 
            <td>
              <v-chip :color="revisao.concluida ? 'success' : 'warning'">
                {{ revisao.concluida ? 'Concluída' : 'Pendente' }}
              </v-chip>
            </td>
            <td>
              <div class="d-flex gap-1 justify-end">
                <v-btn
                  size="small"
                  color="success"
                  @click="concluirRevisaoItem(revisao)"
                  :loading="loadingConclusao === revisao.id"
                >
                  <v-icon icon="mdi-check" class="mr-2" />
                  Concluir
                </v-btn>
                <v-btn
                  size="small"
                  color="warning"
                  @click="abrirModalReagendar(revisao)"
                >
                  Reagendar
                </v-btn>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>

      <v-card v-else variant="flat" class="text-center py-8">
        <v-card-text>
          <v-icon size="64" color="success" class="mb-4">mdi-check-circle</v-icon>
          <div class="text-h6 text-grey">Nenhuma revisão pendente!</div>
          <div class="text-body-1 text-grey mt-2">Todas as revisões estão em dia</div>
        </v-card-text>
      </v-card>
      
      <v-row 
        justify="center" 
        class="mt-4" 
        v-if="totalPages > 1 && Number.isInteger(totalPages)"
      >
        <v-pagination
          :length="totalPages"
          :model-value="page + 1"
          @update:model-value="mudarPagina"
          color="primary"
          size="small"
          rounded
          :show-first-last-page="true"   
          :total-visible="0"   
        />
      </v-row>
    </v-card-text>

    <v-dialog v-model="showReagendarDialog" max-width="500px">
      <v-card v-if="revisaoParaReagendar">
        <v-card-title>Reagendar Revisão</v-card-title>
        <v-card-text>
          <v-text-field
            v-model="novaData"
            label="Nova Data"
            placeholder="DD/MM/AAAA"
            v-mask="'##/##/####'"
            variant="outlined"
            density="compact"
            :rules="[v => !!v || 'Data é obrigatória']"
          />
          <div class="text-caption text-grey mt-2">
            Data atual: {{ revisaoParaReagendar.dataRevisao }}
          </div>
          <div class="text-caption text-blue mt-1">
            ⓘ Limite para datas passadas: 5 dias 
          </div>
        </v-card-text>
        <v-card-actions>


        <v-spacer />

        <div class="d-flex gap-2 mt-4 justify-end">
          <v-btn color="grey" @click="fecharModalReagendar">  
            <v-icon icon="mdi-close" class="mr-2" /> 
            Cancelar
          </v-btn>
          <v-btn color="primary" @click="confirmarReagendamento" :loading="loading">
            <v-icon icon="mdi-check" class="mr-2" />
            Reagendar
          </v-btn>
        </div>



        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-card>
</template>

<style scoped>
.gap-1 {
  gap: 4px;
}
</style>
