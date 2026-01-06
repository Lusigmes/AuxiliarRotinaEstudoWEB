<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { carregarRevisoesPendentesPaginado } from '@/api/RevisaoService';
import { useRevisaoPaginada } from '@/composables/useRevisaoPaginada';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData } from '@/utils/dateUtils';
import { onMounted } from 'vue';
import { ref, watch } from 'vue';
import ReagendarRevisaoModal from './ReagendarRevisaoModal.vue';
import { useNotification } from '@/composables/useNotification';

const props = defineProps<{
  key?: number
}>();

const {
  items: revisoesDaPagina,
  page,
  totalPages,
  atualizarPagina,
  revisaoStore,
  verificarEAjustarPagina
} = useRevisaoPaginada(carregarRevisoesPendentesPaginado, 'pendentes', 6);

const emit = defineEmits<{
  (e: 'atualizar', payload: RevisaoResponseInterface): void;
  (e: 'ver-detalhes', payload: RevisaoResponseInterface): void;
}>();

const { showNotification } = useNotification();
const revisaoParaReagendar = ref<RevisaoResponseInterface | null>(null);
const showReagendarDialog = ref(false);
const loadingConclusao = ref<number | null>(null);
const nomeDisciplinas = ref<Record<number, string>>({});

async function mudarPagina(novaPag: number) {
  await atualizarPagina(novaPag - 1);
};

async function carregarRevisoes(pagina: number = page.value) {
  await atualizarPagina(pagina);
};

function abrirDetalhes(revisao: RevisaoResponseInterface) {
  emit('ver-detalhes', revisao);
}

async function concluirRevisaoItem(revisao: RevisaoResponseInterface) {
  loadingConclusao.value = revisao.id;
  try {
    const revisaoConcluida = await revisaoStore.concluirRevisao(revisao.id);

    await atualizarPagina(page.value);
    
    await new Promise(resolve => setTimeout(resolve, 50));
    
    await verificarEAjustarPagina();
        
    emit('atualizar', revisaoConcluida);
    showNotification('Revisão concluída com sucesso!', 'success');
    
  } catch (error) {
    console.error('Erro ao concluir revisão:', error);
    showNotification('Erro ao concluir revisão. Tente novamente.', 'error');
  } finally {
    loadingConclusao.value = null;
  }
};

function abrirModalReagendar(revisao: RevisaoResponseInterface){
  revisaoParaReagendar.value = revisao;
  showReagendarDialog.value = true;
  showNotification('Reagendando revisão...', 'info');
};

function fecharModalReagendar(){
  revisaoParaReagendar.value = null;
  showReagendarDialog.value = false;
};

async function confirmarReagendamento(novaData: string){
  if(!revisaoParaReagendar.value) return;

  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  const dataInformada = converterStringParaData(novaData);
  const cincoDiasAtras = new Date();
  cincoDiasAtras.setDate(cincoDiasAtras.getDate() - 5);
  cincoDiasAtras.setHours(0, 0, 0, 0);

  if (dataInformada < cincoDiasAtras) {
    showNotification('Limite para reagendamento: até 5 dias atrás', 'warning');
    return;
  }
  
  try {
    const revisaoReagendada = await revisaoStore.reagendarDataRevisao(
      revisaoParaReagendar.value.id, 
      novaData
    );
    
    await atualizarPagina(page.value);
    
    await new Promise(resolve => setTimeout(resolve, 50));

    await verificarEAjustarPagina();

    emit('atualizar', revisaoReagendada);
    fecharModalReagendar();
    
    const textoParaData = converterStringParaData(novaData);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (textoParaData.getTime() < hoje.getTime()) {
      setTimeout(() => {
        showNotification('Revisão reagendada.', 'success');
      }, 1000);
    }
    
  } catch (error) {
    console.error('Erro ao reagendar revisão:', error);
    showNotification('Erro ao reagendar revisão. Tente novamente.', 'error');
  } 
};

async function associarNomeDisciplina(idEstudo: number) {
  if (!idEstudo) return;
  try {
    const nome = await nomeDisciplinaDoEstudo(idEstudo);
    nomeDisciplinas.value[idEstudo] = nome;
  } catch (err) {
    console.error('Erro ao buscar nome da disciplina:', err);
    nomeDisciplinas.value[idEstudo] = '—';
  }
}

const carregarNomesDisciplinas = async () => {
  const promessas = revisoesDaPagina.value.map(r => associarNomeDisciplina(r.idEstudo));
  await Promise.all(promessas);
};

onMounted(async () => {
  await carregarRevisoes(0);
  await carregarNomesDisciplinas();
  showNotification('Revisões pendentes carregadas', 'success');
});

watch(
  () => revisoesDaPagina.value,
  async () => {
    await carregarNomesDisciplinas();
  },
  { deep: true }
);

watch(
  () => props.key,
  async () => {
    await carregarRevisoes(0)
    await carregarNomesDisciplinas()
  }
)

watch(
  () => page.value,
  async () => {
    await carregarNomesDisciplinas();
  }
);
</script>

<template>
  <v-card variant="flat">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Revisões Pendentes</span>
    </v-card-title>
    
    <v-card-text>
      <v-table v-if="revisoesDaPagina.length > 0">
        <thead>
          <tr>
            <th>Data</th>
            <th>Disciplina</th>
            <th>Status</th>
            <th class="d-flex justify-end">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="revisao in revisoesDaPagina" 
          :key="revisao.id" 
          class="revisao-pendente-row"
          @click="abrirDetalhes(revisao)"
          style="cursor: pointer;"
          >
            <td>
              <v-chip :color="converterStringParaData(revisao.dataRevisao).getTime() < new Date().getTime() ? 'warning' : 'warning'">
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
            <td @click.stop>
              <div class="d-flex gap-1 justify-end">
                <v-btn
                  size="small"
                  color="success"
                  @click.stop="concluirRevisaoItem(revisao)"
                  :loading="loadingConclusao === revisao.id"
                >
                  <v-icon icon="mdi-check" class="mr-2" />
                  Concluir
                </v-btn>
                <v-btn
                  size="small"
                  color="warning"
                  @click.stop="abrirModalReagendar(revisao)"
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
          <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-checkbox-marked-circle-outline</v-icon>
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

    <ReagendarRevisaoModal
        v-model="showReagendarDialog"
        :revisao="revisaoParaReagendar"
        @reagendar="confirmarReagendamento"
      />
  </v-card>
</template>

<style scoped>
.revisao-pendente-row:hover {
  background-color: rgba(33, 150, 243, 0.04);
}

.gap-1 {
  gap: 4px;
}

.gap-2 {
  gap: 8px;
}
</style>