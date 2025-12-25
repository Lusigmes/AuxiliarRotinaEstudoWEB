<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { carregarRevisoesPendentesPaginado } from '@/api/RevisaoService';
import { useRevisaoPaginada } from '@/composables/useRevisaoPaginada';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData, validarFormatoData } from '@/utils/dateUtils';
import { onMounted } from 'vue';
import { ref, watch } from 'vue';

const props = defineProps<{
  key?: number
}>();

const {
  items: revisoesDaPagina,
  page,
  totalPages,
  atualizarPagina,
  revisaoStore,
  recarregarPaginaAtual
} = useRevisaoPaginada(carregarRevisoesPendentesPaginado, 'pendentes', 6);

const emit = defineEmits<{
  (e: 'atualizar', payload: RevisaoResponseInterface): void;
  (e: 'ver-detalhes', payload: RevisaoResponseInterface): void;
}>();

const revisaoParaReagendar = ref<RevisaoResponseInterface | null>(null);
const showReagendarDialog = ref(false);
const novaData = ref('');
const loadingReagendar = ref(false);
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
    
    await recarregarPaginaAtual();
    
    emit('atualizar', revisaoConcluida);
    
  } catch (error) {
    console.error('Erro ao concluir revisão:', error);
    alert('Erro ao concluir revisão');
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
  
  loadingReagendar.value = true;

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
    const revisaoReagendada = await revisaoStore.reagendarDataRevisao(
      revisaoParaReagendar.value.id, 
      novaData.value
    );
    
    // Recarregue a página atual
    await recarregarPaginaAtual();
    
    emit('atualizar', revisaoReagendada);
    fecharModalReagendar();
    
    const textoParaData = converterStringParaData(novaData.value);
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    if (textoParaData.getTime() < hoje.getTime()) {
      setTimeout(() => {
        alert('Revisão reagendada para data passada. Verifique a aba "Atrasadas".');
      }, 300);
    }
    
  } catch (error) {
    console.error('Erro ao reagendar revisão:', error);
    alert('Erro ao reagendar revisão: ' + error);
  } finally {
    loadingReagendar.value = false;
  }
}

async function fetchNomeDisciplina(idEstudo: number) {
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
  const promessas = revisoesDaPagina.value.map(r => fetchNomeDisciplina(r.idEstudo));
  await Promise.all(promessas);
};

onMounted(async () => {
  await carregarRevisoes(0);
  await carregarNomesDisciplinas();
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
            <v-btn color="primary" @click="confirmarReagendamento" :loading="loadingReagendar">
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