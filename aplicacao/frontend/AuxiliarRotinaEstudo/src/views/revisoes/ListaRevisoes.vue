<script setup lang="ts">
import type { RevisaoResponseInterface } from '@/types';
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { ref, watch } from 'vue';
import { formatarDataParaExibicao } from '@/utils/dateUtils';
import { useNotification } from '@/composables/useNotification';

interface Props {
  visivel: boolean;
  data: Date | null;
  revisoes: RevisaoResponseInterface[];
}

const props = defineProps<Props>();

const emit = defineEmits<{
  (e: 'fechar'): void;
  (e: 'selecionar-revisao', revisao: RevisaoResponseInterface): void;
  (e: 'ver-detalhes', payload: { revisao: RevisaoResponseInterface, origemLista: boolean }): void;
}>();

const { showNotification } = useNotification();
const revisoesComNomes = ref<Array<RevisaoResponseInterface & { nomeDisciplina: string }>>([]);
const loadingNomes = ref(false);

async function carregarNomesDisciplinas() {
  if (props.revisoes.length === 0) {
    revisoesComNomes.value = [];
    return;
  }

  loadingNomes.value = true;
  try {
    const revisoesComNomesPromises = props.revisoes.map(async (revisao) => {
      try {
        const nomeDisciplina = await nomeDisciplinaDoEstudo(revisao.idEstudo);
        return {
          ...revisao,
          nomeDisciplina
        };
      } catch (error) {
        console.error('Erro ao buscar nome da disciplina:', error);
        return {
          ...revisao,
          nomeDisciplina: '—'
        };
      }
    });

    revisoesComNomes.value = await Promise.all(revisoesComNomesPromises);
  } catch (error) {
    console.error('Erro ao carregar nomes das disciplinas:', error);
    showNotification('Erro ao carregar nomes das disciplinas', 'warning');
  } finally {
    loadingNomes.value = false;
  }
}

watch(
  () => props.revisoes,
  async () => {
    if (props.visivel && props.revisoes.length > 0) {
      await carregarNomesDisciplinas();
    }
  },
  { deep: true }
);

watch(
  () => props.visivel,
  async (visivel) => {
    if (visivel && props.revisoes.length > 0) {
      await carregarNomesDisciplinas();
    } else {
      revisoesComNomes.value = [];
    }
  }
);

function fecharModal() {
  emit('fechar');
}
function abrirDetalhesDaLista(revisao: RevisaoResponseInterface) {
  emit('ver-detalhes', { revisao, origemLista: true });
}
</script>

<template>
  <v-dialog :model-value="visivel" max-width="900px" scrollable @update:model-value="!$event && fecharModal()">
    <v-card v-if="data">
      <v-card-title class="d-flex justify-space-between align-center">
        <div>
          <span class="text-h5">Revisões do dia</span>
          <div class="text-subtitle-1 text-grey">
            {{ formatarDataParaExibicao(data) }}
          </div>
        </div>
        <v-btn icon @click="fecharModal" elevation="0">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>
      
      <v-divider />
      
      <v-card-text class="pt-4">
        <div v-if="loadingNomes" class="text-center py-8">
          <v-progress-circular indeterminate />
          <div class="mt-4">Carregando revisões...</div>
        </div>
        
        <div v-else-if="revisoesComNomes.length > 0" class="grid-revisoes-dia">
          <div class="grid-container">
            <div 
              v-for="(revisao, index) in revisoesComNomes" 
              :key="revisao.id"
              class="grid-item"
            >
              <v-card variant="outlined" class="h-100 revisao-card">
                <v-card-item class="pa-3">
                  <div class="d-flex justify-space-between align-start mb-2">
                    <v-chip 
                      :color="revisao.concluida ? 'success' : 'warning'" 
                      size="small"
                      class="font-weight-medium"
                    >
                      {{ revisao.concluida ? 'CONCLUÍDA' : 'PENDENTE' }}
                    </v-chip>
                    <span class="text-caption text-grey">#{{ index + 1 }}</span>
                  </div>
                  
                  <div class="mb-3">
                    <div class="text-subtitle-2 font-weight-bold mb-1">
                      {{ revisao.nomeDisciplina }}
                    </div>
                  </div>
                  
                  <v-divider class="my-2" />
                  
                  <div class="d-flex justify-space-between align-center">
                    <div class="d-flex align-center">
                      <v-icon size="16" class="mr-1" color="primary">mdi-calendar</v-icon>
                      <span class="text-caption">{{ revisao.dataRevisao }}</span>
                    </div>
                                        
                    <v-btn
                      icon
                      size="x-small"
                      variant="text"
                      color="primary"
                      @click.stop="abrirDetalhesDaLista(revisao)"
                    >
                      <v-icon>mdi-open-in-new</v-icon>
                    </v-btn>
                  </div>
                </v-card-item>
              </v-card>
            </div>
          </div>
        </div>
        
        <v-card v-else variant="flat" class="text-center py-8">
          <v-card-text>
            <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-calendar-blank</v-icon>
            <h3 class="text-h6 text-grey">Nenhuma revisão neste dia</h3>
            <p class="text-body-2 text-grey mt-2">
              Não há revisões agendadas para {{ formatarDataParaExibicao(data) }}
            </p>
          </v-card-text>
        </v-card>
      </v-card-text>
      
      <v-card-actions class="pa-4">
        <v-spacer />
        <v-btn color="primary" @click="fecharModal">
          Fechar
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.grid-revisoes-dia .grid-container {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.grid-revisoes-dia .grid-item {
  min-height: 140px;
}

.grid-revisoes-dia .revisao-card {
  cursor: pointer;
  transition: all 0.2s ease;
}

.grid-revisoes-dia .revisao-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.1);
  border-color: rgb(var(--v-theme-primary));
}

.h-100 {
  height: 100%;
}

@media (max-width: 960px) {
  .grid-revisoes-dia .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
  }
}

@media (max-width: 600px) {
  .grid-revisoes-dia .grid-container {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 8px;
  }
}
</style>