<template>
  <v-container fluid class="pa-6">
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template #divider>
        <v-icon>mdi-chevron-right</v-icon>
      </template>
    </v-breadcrumbs>

    <v-card variant="flat" class="mb-6">
      <v-card-item>
        <template #prepend>
          <v-avatar color="purple" variant="tonal" size="48">
            <v-icon icon="mdi-chart-bar" />
          </v-avatar>
        </template>

        <div class="d-flex align-center justify-space-between flex-grow-1">
          <div>
            <v-card-title class="text-h4">Relatórios de Estudos</v-card-title>
            <v-card-subtitle class="text-h6">
              Análise detalhada do seu desempenho e progresso
            </v-card-subtitle>
          </div>

          <!-- status  -->
          <div class="status-header cursor-none" v-if="statusRevisoes && statusRevisoes.length > 0">
            <div class="status-badges d-flex align-center">
              <div class="status-badge-group">
                <div class="status-badge status-concluidas" v-if="getStatusCount('concluídas') > 0">
                  <div class="status-circle">
                    <div class="status-count">{{ getStatusCount('concluídas') }}</div>
                  </div>
                  <div class="status-label">Concluídas</div>
                </div>
                
                <div class="status-badge status-pendentes" v-if="getStatusCount('pendentes') > 0">
                  <div class="status-circle">
                    <div class="status-count">{{ getStatusCount('pendentes') }}</div>
                  </div>
                  <div class="status-label">Pendentes</div>
                </div>
                
                <div class="status-badge status-atrasadas" v-if="getStatusCount('atrasadas') > 0">
                  <div class="status-circle">
                    <div class="status-count">{{ getStatusCount('atrasadas') }}</div>
                  </div>
                  <div class="status-label">Atrasadas</div>
                </div>
              </div>
              
              <div class="status-total ml-3">
                <div class="total-count">{{ totalRevisoes }}</div>
                <div class="total-label">Revisões</div>
              </div>
            </div>
          </div>
        </div>
      </v-card-item>
    </v-card>

    <!-- filtragem  -->
    <v-card class="mb-6" elevation="4">
      <v-card-text class="pa-4">
        <v-row class="align-center" dense>
          <v-col cols="12" md="6">
            <div class="d-flex align-center gap-2">
              <v-text-field
                v-model="filtroInicio"
                label="Data Início"
                placeholder="DD/MM/AAAA"
                v-mask="'##/##/####'"
                variant="outlined"
                density="comfortable"
                hide-details
                class="compact-field"
                :rules="[validarData]"
              >
                <template #prepend-inner>
                  <v-icon size="small" color="primary">mdi-calendar</v-icon>
                </template>
              </v-text-field>
              
              <span class="text-body-2 text-medium-emphasis mx-2">até</span>
              
              <v-text-field
                v-model="filtroFim"
                label="Data Fim"
                placeholder="DD/MM/AAAA"
                v-mask="'##/##/####'"
                variant="outlined"
                density="comfortable"
                hide-details
                class="compact-field"
                :rules="[validarData]"
              >
                <template #prepend-inner>
                  <v-icon size="small" color="primary">mdi-calendar</v-icon>
                </template>
              </v-text-field>
            </div>
          </v-col>

          <v-col cols="12" md="6">
            <div class="d-flex align-center gap-2 justify-end">
              <div class="d-flex gap-2">
                <v-btn
                  color="primary"
                  variant="flat"
                  size="small"
                  prepend-icon="mdi-filter-variant"
                  @click="aplicarFiltros"
                  :loading="loading"
                  elevation="2"
                >
                  Aplicar Filtro
                </v-btn>
                <v-btn
                  variant="tonal"
                  size="small"
                  @click="limparFiltros"
                  :disabled="loading"
                >
                  Limpar
                </v-btn>
              </div>
              <v-spacer />

              <v-btn
                color="primary"
                variant="tonal"
                prepend-icon="mdi-file-pdf-box"
                @click="exportarPDF"
                :loading="exportando || loading"
                :disabled="loading || !resumo"
                class="ml-4"
              >
                Exportar PDF
              </v-btn>
            </div>
          </v-col>
        </v-row>
        
      <div v-if="resumo && resumo.periodoInicio" class="mt-3 d-flex align-center">
        <v-chip color="primary" variant="tonal" size="small" class="mr-2">
          <v-icon size="small" start>mdi-calendar</v-icon>
          Período Ativo
        </v-chip>
        <span class="text-body-2">
          {{ formatarDataParaDisplay(resumo.periodoInicio) }} - {{ formatarDataParaDisplay(resumo.periodoFim) }}
        </span>
      </div>
      </v-card-text>
    </v-card>

    <v-overlay :model-value="loading" class="align-center justify-center" persistent>
      <v-card class="pa-6 text-center rounded-xl" elevation="8">
        <v-progress-circular 
          indeterminate 
          color="primary" 
          size="64" 
          width="4"
        />
        <div class="text-h6 mt-4 text-primary">Gerando relatórios...</div>
        <div class="text-caption text-medium-emphasis mt-1">
          Isso pode levar alguns instantes
        </div>
      </v-card>
    </v-overlay>

    <div v-if="!loading" class="dashboard-grid">
      <!-- estudos realizados -->
      <v-card class="dashboard-card" elevation="2">
        <div class="card-icon-wrapper bg-primary-lighten-5">
          <v-icon color="primary" size="28">mdi-book-multiple</v-icon>
        </div>
        <div class="card-content">
          <div class="text-h3 font-weight-bold text-primary mb-1">
            {{ resumo?.totalEstudos || 0 }}
          </div>
          <div class="text-body-2 text-medium-emphasis mb-2">
            Estudos Realizados
          </div>
        </div>
      </v-card>

      <!-- revisões concluídas -->
      <v-card class="dashboard-card" elevation="2">
        <div class="card-icon-wrapper bg-success-lighten-5">
          <v-icon color="success" size="28">mdi-check-circle</v-icon>
        </div>
        <div class="card-content">
          <div class="text-h3 font-weight-bold text-success mb-1">
            {{ resumo?.totalRevisoesConcluidas || 0 }}
          </div>
          <div class="text-body-2 text-medium-emphasis mb-2">
            Revisões Concluídas
          </div>
        </div>
      </v-card>

      <!-- tempo de estudo -->
      <v-card class="dashboard-card" elevation="2">
        <div class="card-icon-wrapper bg-orange-lighten-5">
          <v-icon color="orange-darken-2" size="28">mdi-clock-time-four</v-icon>
        </div>
        <div class="card-content">
          <div class="text-h3 font-weight-bold text-orange-darken-2 mb-1">
            {{ resumo?.tempoTotal || 0 }}h
          </div>
          <div class="text-body-2 text-medium-emphasis mb-2">
            Tempo Total
          </div>
          <div class="text-caption">
            Média diária: {{ resumo?.mediaTempoDiario?.toFixed(1) || '0.0' }}h
          </div>
        </div>
      </v-card>

      <!-- top disciplina -->
      <v-card class="dashboard-card" elevation="2">
        <div class="card-icon-wrapper bg-purple-lighten-5">
          <v-icon color="purple" size="28">mdi-trophy</v-icon>
        </div>
        <div class="card-content">
          <div class="text-h5 font-weight-bold text-purple mb-2 truncate">
            {{ resumo?.disciplinaMaisEstudada || 'Nenhuma' }}
          </div>
          <div class="text-body-2 text-medium-emphasis mb-2">
            Disciplina Mais Estudada
          </div>
        </div>
      </v-card>
    </div>

    <div v-if="!loading" class="charts-section">
    
      <v-row class="mb-6 align-stretch">
        <!-- gráfico -->
        <v-col cols="12" md="8">
          <v-card class="chart-card" elevation="2">
            <v-card-title class="pa-4">
              <div class="d-flex align-center">
                <v-icon color="purple" class="mr-2">mdi-chart-donut</v-icon>
                <span class="text-h6">Distribuição por Disciplina</span>
              </div>
              <v-spacer />
              <v-chip size="small" variant="tonal" color="purple">
                Total: {{ calcularTotalTempo() }}h
              </v-chip>
            </v-card-title>
            <v-divider />
            <v-card-text class="pa-4 chart-container-wrapper">
              <div v-if="tempoPorDisciplina && Object.keys(tempoPorDisciplina).length > 0" 
                   class="chart-container-full">
                <canvas ref="pieChart"></canvas>
              </div>
              <div v-else class="no-data-container">
                <v-icon size="48" class="mb-3">mdi-chart-areaspline-variant</v-icon>
                <div class="text-h6 mb-1">Sem dados suficientes</div>
                <div class="text-body-2 text-medium-emphasis">
                  Adicione mais estudos para visualizar o gráfico
                </div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <!-- top disciplinas -->
        <v-col cols="12" md="4">
          <v-card class="ranking-card" elevation="2">
            <v-card-title class="pa-4">
              <div class="d-flex align-center">
                <v-icon color="blue" class="mr-2">mdi-podium</v-icon>
                <span class="text-h6">Top Disciplinas</span>
              </div>
            </v-card-title>
            <v-divider />
            <v-card-text class="pa-4">
              <div v-if="topDisciplinas && topDisciplinas.length > 0">
                <div v-for="(disciplina, index) in topDisciplinas" 
                     :key="disciplina.disciplina" 
                     class="ranking-item">
                  <div class="d-flex align-center py-2">
                    <div class="ranking-number">{{ index + 1 }}</div>
                    <div class="ml-3 flex-grow-1">
                      <div class="text-body-1 font-weight-medium truncate">
                        {{ disciplina.disciplina }}
                      </div>
                      <div class="d-flex align-center mt-1">
                        <div class="flex-grow-1">
                          <v-progress-linear
                            :model-value="calcularPercentualRanking(disciplina.totalTempo || 0)"
                            color="blue"
                            height="6"
                            rounded
                            class="mr-2"
                          />
                        </div>
                        <div class="text-caption font-weight-bold text-blue-darken-2">
                          {{ disciplina.totalTempo || 0 }}h
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-6">
                <v-icon size="48" color="grey-lighten-1" class="mb-3">mdi-podium-bronze</v-icon>
                <div class="text-body-1">Nenhuma disciplina encontrada</div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- timeline -->
      <v-row class="mb-6">
        <v-col cols="12">
          <v-card class="timeline-card" elevation="2">
            <v-card-title class="pa-4">
              <div class="d-flex align-center">
                <v-icon color="orange" class="mr-2">mdi-timeline-clock</v-icon>
                <span class="text-h6">Estudos por Dia</span>
              </div>
              <v-spacer />
              <span class="text-caption text-medium-emphasis">
                Dias Estudados: {{ estudosDiario?.length || 0 }}
              </span>
            </v-card-title>
            <v-divider />
            <v-card-text class="pa-4">
              <div v-if="estudosDiario && estudosDiario.length > 0">
                <div class="estudos-grid-wrapper">
                  <div class="estudos-grid">
                    <div v-for="dia in estudosDiario.slice(0, 1000)" :key="dia.data" class="estudo-dia-item">
                      <div class="estudo-dia-content">
                        <div class="estudo-dia-header">
                          <div class="estudo-dia-data">{{ formatarDataParaDisplay(dia.data || '') }}</div>
                          <v-chip color="orange" variant="tonal" size="x-small" density="compact">
                            {{ dia.quantidadeEstudos || 0 }} estudos
                          </v-chip>
                        </div>
                        <div class="estudo-dia-tempo">
                          <v-icon size="x-small" color="orange" class="mr-1">mdi-clock</v-icon>
                          {{ dia.tempoTotal || 0 }} horas
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              <div v-else class="text-center py-6">
                <v-icon size="48" color="grey-lighten-1" class="mb-3">mdi-calendar-blank</v-icon>
                <div class="text-body-1">Nenhum estudo registrado</div>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <!-- tabela  -->
      <v-card v-if="estatisticasDisciplinas && estatisticasDisciplinas.length > 0" class="mb-6" elevation="2">
        <v-card-title class="pa-4 d-flex align-center justify-space-between">
          <div class="d-flex align-center">
            <v-icon color="deep-purple" class="mr-2">mdi-table</v-icon>
            <span class="text-h6">Visão Geral das Disciplinas</span>
          </div>

          <div class="d-flex align-center">
            <v-btn
              color="secondary"
              variant="tonal"
              size="small"
              prepend-icon="mdi-table-arrow-down"
              @click="exportarTabela"
              :loading="exportandoTabela"
              :disabled="loading || estatisticasDisciplinas.length === 0"
              class="mr-2"
            >
              Exportar Tabela
            </v-btn>
            
            <v-btn
              variant="tonal"
              size="small"
              @click="expandirTabela = !expandirTabela"
              :prepend-icon="expandirTabela ? 'mdi-chevron-up' : 'mdi-chevron-down'"
              color="deep-purple"
            >
              {{ expandirTabela ? 'Recolher' : 'Expandir' }}
            </v-btn>
          </div>
        </v-card-title>
        
        <v-expand-transition>
          <div v-show="expandirTabela">
            <v-divider />
            <div class="table-responsive">
              <v-table density="comfortable" hover>
                <thead class="table-header">
                  <tr>
                    <th class="text-left">
                      <v-icon size="small" class="mr-1">mdi-book</v-icon>
                      Disciplina
                    </th>
                    <th class="text-center">
                      <v-icon size="small" class="mr-1">mdi-counter</v-icon>
                      Estudos
                    </th>
                    <th class="text-center">
                      <v-icon size="small" class="mr-1">mdi-clock</v-icon>
                      Tempo Total
                    </th>
                    <th class="text-center">
                      <v-icon size="small" class="mr-1">mdi-chart-bar</v-icon>
                      Média/Estudo
                    </th>
                    <th class="text-center">
                      <v-icon size="small" class="mr-1">mdi-check</v-icon>
                      Revisões
                    </th>
                    <th class="text-center">
                      <v-icon size="small" class="mr-1">mdi-percent</v-icon>
                      Tempo
                    </th>
                  </tr>
                </thead>
                <tbody>
                  <tr v-for="disciplina in estatisticasDisciplinas" :key="disciplina.disciplina">
                    <td class="font-weight-bold">
                     <strong> {{ disciplina.disciplina }}</strong>
                    </td>
                    <td class="text-center">
                      <v-chip size="small" variant="tonal" color="primary">
                        {{ disciplina.totalEstudos || 0 }}
                      </v-chip>
                    </td>
                    <td class="text-center font-weight-bold">
                      {{ disciplina.totalTempo || 0 }}h
                    </td>
                    <td class="text-center">
                      {{ (disciplina.mediaTempoEstudo || 0).toFixed(1) }}h
                    </td>
                    <td class="text-center">
                      <v-chip :color="(disciplina.totalRevisoesConcluidas || 0) > 0 ? 'success' : 'grey'" 
                              variant="flat" 
                              size="small">
                        {{ disciplina.totalRevisoesConcluidas || 0 }}
                      </v-chip>
                    </td>
                    <td class="text-center">
                      <div class="percent-cell">
                        <v-progress-linear
                          :model-value="calcularPercentualTempo(disciplina.totalTempo || 0)"
                          :color="calcularCorPercentual(disciplina.totalTempo || 0)"
                          height="8"
                          rounded
                          class="mb-1"
                        />
                        <div class="text-caption font-weight-medium">
                          {{ formatarPercentual(calcularPercentualTempo(disciplina.totalTempo || 0)) }}
                        </div>
                      </div>
                    </td>
                  </tr>
                </tbody>
              </v-table>
            </div>
          </div>
        </v-expand-transition>
      </v-card>
    </div>
<!-- apos aqui edição  sss -->
    <v-card v-if="!loading" class="mt-6" variant="flat" color="primary-lighten-5">
      <v-card-text class="pa-4 text-center">
        <div class="d-flex align-center justify-center gap-4">
          <v-icon color="primary">mdi-information</v-icon>
          <span class="text-body-2">
            Dados atualizados em tempo real • Última atualização: {{ getDataHoraAtualFormatada() }}
          </span>
          <v-btn
            variant="text"
            size="small"
            color="primary"
            @click="carregarRelatorios"
            :loading="loading"
          >
            <v-icon start>mdi-refresh</v-icon>
            Atualizar
          </v-btn>
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>
<script setup lang="ts">
import { ref, onMounted, watch, nextTick, onBeforeUnmount, computed } from 'vue';
import Chart from 'chart.js/auto';
import { useRouter } from 'vue-router';
import { 
  obterResumo,
  obterEstatisticasDisciplinas,
  obterEstudosDiario,
  obterStatusRevisoes,
  obterTempoPorDisciplina,
  obterDashboard
} from '@/api/RelatorioService';
import type { 
  RelatorioResumoInterface, 
  EstatisticaDisciplinaInterface,
  EstudoDiarioInterface,
  RevisaoStatusInterface,
  DashboardInterface 
} from '@/types';
import { 
  validarFormatoData,
  formatarDataParaDisplay,
  getDataHoraAtualFormatada,
} from '@/utils/dateUtils';
import { exportarPdfRelatorio } from '@/utils/pdfRelatorioExportService';

const router = useRouter();

const breadcrumbs = [
  { 
    title: 'Dashboard', 
    disabled: false, 
    to: '/tela-principal',
    exact: true 
  },
  { 
    title: 'Relatórios', 
    disabled: true 
  }
];

const loading = ref(false);
const filtroInicio = ref('');
const filtroFim = ref('');
const expandirTabela = ref(false);
const exportando = ref(false);
const exportandoTabela = ref(false);

const resumo = ref<RelatorioResumoInterface | null>(null);
const estatisticasDisciplinas = ref<EstatisticaDisciplinaInterface[]>([]);
const estudosDiario = ref<EstudoDiarioInterface[]>([]);
const statusRevisoes = ref<RevisaoStatusInterface[]>([]);
const tempoPorDisciplina = ref<Record<string, number> | null>(null);
const dashboardData = ref<DashboardInterface | null>(null);
const topDisciplinas = ref<EstatisticaDisciplinaInterface[]>([]);

const pieChart = ref<HTMLCanvasElement | null>(null);
let chartInstance: Chart | null = null;

const totalRevisoes = computed(() => {
  if (!statusRevisoes.value) return 0;
  return statusRevisoes.value.reduce((total, status) => total + (status.quantidade || 0), 0);
});

const getStatusCount = (statusKey: string) => {
  if (!statusRevisoes.value) return 0;
  const status = statusRevisoes.value.find(s => 
    s.status.toLowerCase() === statusKey.toLowerCase()
  );
  return status?.quantidade || 0;
};

const validarData = (value: string) => {
  if (!value) return true;
  return validarFormatoData(value) || 'Data inválida. Use DD/MM/AAAA';
};

const calcularPercentualTempo = (tempoDisciplina: number): number => {
  const total = resumo.value?.tempoTotal ?? calcularTotalTempo();
  if (!total || total === 0) return 0;
  const pct = (tempoDisciplina / total) * 100;
  if (!isFinite(pct)) return 0;
  return Math.max(0, Math.min(100, Number(pct.toFixed(1))));
};

const formatarPercentual = (valor: number): string => {
  if (valor === null || valor === undefined) return '0%';
  return `${Number(valor).toFixed(1)}%`;
};

const calcularCorPercentual = (tempoDisciplina: number): string => {
  const percentual = calcularPercentualTempo(tempoDisciplina);
  if (percentual > 50) return 'primary';
  if (percentual > 25) return 'blue';
  if (percentual > 10) return 'cyan';
  return 'grey';
};

const calcularPercentualRanking = (tempoDisciplina: number): number => {
  if (!topDisciplinas.value.length || !topDisciplinas.value[0]?.totalTempo) return 0;
  const maxTempo = topDisciplinas.value[0].totalTempo || 1;
  return (tempoDisciplina / maxTempo) * 100;
};

const calcularTotalTempo = (): number => {
  if (!tempoPorDisciplina.value) return 0;
  return Object.values(tempoPorDisciplina.value).reduce((acc, val) => acc + val, 0);
};

async function exportarPDF() {
  if (!resumo.value) {
    alert('Carregue os dados do relatório primeiro');
    return;
  }

  exportando.value = true;
  
  try {
    await exportarPdfRelatorio.generatePDF({
      resumo: resumo.value,
      estatisticasDisciplinas: estatisticasDisciplinas.value,
      estudosDiario: estudosDiario.value,
      statusRevisoes: statusRevisoes.value,
      tempoPorDisciplina: tempoPorDisciplina.value,
      topDisciplinas: topDisciplinas.value,
      periodoInicio: filtroInicio.value || (resumo.value?.periodoInicio || ''),
      periodoFim: filtroFim.value || (resumo.value?.periodoFim || '')
    });
    
  } catch (error) {
    console.error('Erro detalhado ao exportar PDF:', error);
    alert(`Erro ao gerar PDF: ${error instanceof Error ? error.message : 'Tente novamente.'}`);
  } finally {
    exportando.value = false;
  }
}

async function exportarTabela() {
  if (estatisticasDisciplinas.value.length === 0) {
    alert('Não há dados para exportar');
    return;
  }

  exportandoTabela.value = true;
  
  try {
    exportarPdfRelatorio.exportTabelaCSV(estatisticasDisciplinas.value, resumo.value);
  } catch (error) {
    console.error('Erro ao exportar tabela:', error);
    alert('Erro ao exportar tabela. Tente novamente.');
  } finally {
    exportandoTabela.value = false;
  }
}

function destruirGrafico() {
  if (chartInstance) {
    chartInstance.destroy();
    chartInstance = null;
  }
}

function criarGraficoPizza() {
  destruirGrafico();
  
  if (!pieChart.value || !tempoPorDisciplina.value) {
    return;
  }
  
  const labels = Object.keys(tempoPorDisciplina.value);
  const data = Object.values(tempoPorDisciplina.value);
  
  if (labels.length === 0 || data.length === 0) {
    return;
  }
  
  const ctx = pieChart.value.getContext('2d');
  if (ctx) {
    ctx.clearRect(0, 0, pieChart.value.width, pieChart.value.height);
  }

  const backgroundColors = [
    '#6366F1', '#EC4899', '#10B981', '#F59E0B', 
    '#3B82F6', '#EF4444', '#8B5CF6', '#06B6D4',
    '#84CC16', '#F97316', '#8B5CF6', '#64748B'
  ];

  try {
    chartInstance = new Chart(pieChart.value, {
      type: 'doughnut',
      data: {
        labels: labels,
        datasets: [{
          data: data,
          backgroundColor: backgroundColors.slice(0, labels.length),
          borderWidth: 2,
          borderColor: '#ffffff',
          hoverOffset: 15,
          borderRadius: 8
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'right',
            labels: {
              padding: 15,
              usePointStyle: true,
              font: {
                size: 11
              },
              color: '#424242',
              boxWidth: 8,
              boxHeight: 8
            }
          },
          tooltip: {
            backgroundColor: 'rgba(0, 0, 0, 0.9)',
            titleColor: '#fff',
            bodyColor: '#fff',
            padding: 12,
            cornerRadius: 6,
            boxPadding: 6,
            callbacks: {
              label: (context) => {
                const label = context.label || '';
                const value = context.raw as number;
                const total = context.dataset.data.reduce((a: number, b: number) => a + b, 0);
                const percentage = Math.round((value / total) * 100);
                return `${label}: ${value}h (${percentage}%)`;
              }
            }
          }
        },
        cutout: '65%',
        animation: {
          animateScale: true,
          animateRotate: true,
          duration: 1000,
          easing: 'easeOutQuart'
        }
      }
    });
    
  } catch (error) {
    console.error('Erro ao criar gráfico:', error);
  }
}

async function carregarRelatorios() {
  loading.value = true;
  try {
    const [
      resumoData,
      disciplinasData,
      estudosData,
      statusData,
      tempoData,
      dashboardDataResponse
    ] = await Promise.all([
      obterResumo(filtroInicio.value, filtroFim.value).catch(() => null),
      obterEstatisticasDisciplinas(filtroInicio.value, filtroFim.value).catch(() => []),
      obterEstudosDiario(filtroInicio.value, filtroFim.value).catch(() => []),
      obterStatusRevisoes().catch(() => []),
      obterTempoPorDisciplina(filtroInicio.value, filtroFim.value).catch(() => ({})),
      obterDashboard().catch(() => null)
    ]);

    resumo.value = resumoData;
    estatisticasDisciplinas.value = disciplinasData;
    estudosDiario.value = estudosData;
    statusRevisoes.value = statusData;
    tempoPorDisciplina.value = tempoData;
    dashboardData.value = dashboardDataResponse;
    
    topDisciplinas.value = disciplinasData.slice(0, 5);

    await nextTick();
    
    setTimeout(() => {
      if (tempoPorDisciplina.value && Object.keys(tempoPorDisciplina.value).length > 0) {
        criarGraficoPizza();
      } else {
        destruirGrafico();
      }
    }, 100);

  } catch (error) {
    console.error('Erro ao carregar relatórios:', error);
  } finally {
    loading.value = false;
  }
}

function aplicarFiltros() {
  if (filtroInicio.value && !validarFormatoData(filtroInicio.value)) {
    alert('Data de início inválida');
    return;
  }
  if (filtroFim.value && !validarFormatoData(filtroFim.value)) {
    alert('Data de fim inválida');
    return;
  }
  
  carregarRelatorios();
}

function limparFiltros() {
  filtroInicio.value = '';
  filtroFim.value = '';
  carregarRelatorios();
}

watch(tempoPorDisciplina, (newVal) => {
  nextTick(() => {
    setTimeout(() => {
      if (newVal && Object.keys(newVal).length > 0) {
        criarGraficoPizza();
      } else {
        destruirGrafico();
      }
    }, 50);
  });
}, { deep: true });

onMounted(async () => {
  await carregarRelatorios();
  
  setTimeout(() => {
    if (tempoPorDisciplina.value && Object.keys(tempoPorDisciplina.value).length > 0) {
      criarGraficoPizza();
    }
  }, 300);
});

onBeforeUnmount(() => {
  destruirGrafico();
});
</script>
<style scoped>
.status-header {
  background: white;
  border-radius: 12px;
  padding: 8px 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid #e0e0e0;
  display: inline-block;
}

.status-badges {
  display: flex;
  align-items: center;
  gap: 8px;
}

.status-badge-group {
  display: flex;
  align-items: center;
  gap: 12px;
  padding-right: 12px;
  border-right: 1px solid #e0e0e0;
}

.status-badge {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 60px;
}

.status-circle {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  margin-bottom: 4px;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.status-circle:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.2);
}

.status-count {
  font-size: 0.9rem;
  font-weight: bold;
  color: white;
}

.status-label {
  font-size: 0.7rem;
  font-weight: 500;
  color: #424242;
  white-space: nowrap;
}

.status-concluidas .status-circle {
  background: linear-gradient(135deg, #10B981, #059669);
}

.status-pendentes .status-circle {
  background: linear-gradient(135deg, #F59E0B, #D97706);
}

.status-atrasadas .status-circle {
  background: linear-gradient(135deg, #EF4444, #DC2626);
}

.status-total {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding-left: 8px;
}

.total-count {
  font-size: 1.2rem;
  font-weight: bold;
  color: #424242;
}

.total-label {
  font-size: 0.7rem;
  color: #757575;
  font-weight: 500;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
  gap: 1rem;
  margin-bottom: 2rem;
}

.dashboard-card {
  border-radius: 12px;
  padding: 1.25rem;
  position: relative;
  overflow: hidden;
  border: 1px solid #e0e0e0;
  transition: all 0.3s ease;
}

.dashboard-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  border-color: var(--v-primary-base);
}

.card-icon-wrapper {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 1rem;
}

.card-content {
  margin-bottom: 0.5rem;
}

  .chart-card,
  .ranking-card,
  .timeline-card {
  border-radius: 12px;
  overflow: hidden;
  transition: transform 0.3s ease;
  border: 1px solid #e0e0e0;
  display: flex;
  flex-direction: column;
  height: 100%;
}

.chart-card:hover,
.ranking-card:hover,
.timeline-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
}

.chart-container-wrapper {
  flex: 1;
  min-height: 350px;
  position: relative;
  display: flex;
  flex-direction: column;
}

.align-stretch > .v-col {
  display: flex;
}

.ranking-card .v-card-text {
  flex: 1;
  overflow: auto;
}

.chart-container-full {
  position: absolute;
  top: 10px;
  left: 10px;
  right: 10px;
  bottom: 10px;
  height: calc(100% - 20px);
  width: calc(100% - 20px);
}

.no-data-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #9e9e9e;
}

.ranking-number {
  width: 24px;
  height: 24px;
  background: linear-gradient(135deg, #3B82F6, #1D4ED8);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: bold;
  color: white;
  flex-shrink: 0;
}

.ranking-item {
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
}

.ranking-item:last-child {
  border-bottom: none;
}

.estudos-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(140px, 1fr));
  gap: 12px;
  grid-auto-rows: 110px;
}

.estudos-grid-wrapper {
  max-height: calc(110px * 3 + 12px * 2);
  overflow-y: auto;
  padding-right: 8px;
}

.estudos-grid-wrapper::-webkit-scrollbar {
  width: 8px;
}
.estudos-grid-wrapper::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}
.estudos-grid-wrapper::-webkit-scrollbar-thumb {
  background: #c5c5c5;
  border-radius: 4px;
}
.estudos-grid-wrapper::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}

.estudo-dia-item {
  background-color: #f8f9fa;
  border-radius: 8px;
  padding: 12px;
  border: 1px solid #e0e0e0;
  transition: all 0.2s ease;
}

.estudo-dia-item:hover {
  background-color: #f1f8e9;
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

.estudo-dia-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.estudo-dia-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.estudo-dia-data {
  font-weight: bold;
  color: #424242;
  font-size: 0.85rem;
}

.estudo-dia-tempo {
  display: flex;
  align-items: center;
  color: #FF9800;
  font-weight: 500;
  font-size: 0.9rem;
}

.table-header {
  background: #f8fafc;
}

.table-header th {
  font-weight: 600;
  color: #475569;
}

  .table-responsive {
  overflow-x: auto;
  overflow-y: auto;
  max-height: calc(56px * 5 + 56px);
}

.table-responsive table {
  width: 100%;
  border-collapse: collapse;
}

.table-responsive thead th {
  position: sticky;
  top: 0;
  background: #f8fafc;
  z-index: 3;
}

.percent-cell {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  width: 90px;
  margin: 0 auto;
}
.percent-cell .text-caption {
  font-size: 0.85rem;
}

.truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.gap-2 {
  gap: 8px;
}

.ml-3 {
  margin-left: 12px;
}

.ml-4 {
  margin-left: 16px;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.dashboard-card,
.chart-card,
.ranking-card,
.timeline-card {
  animation: fadeIn 0.6s ease-out forwards;
}

@media (max-width: 960px) {
  .status-header {
    padding: 6px 12px;
    margin-top: 12px;
  }
  
  .status-badge-group {
    gap: 8px;
    padding-right: 8px;
  }
  
  .status-badge {
    min-width: 50px;
  }
  
  .status-circle {
    width: 36px;
    height: 36px;
  }
  
  .dashboard-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .chart-container-wrapper {
    height: 300px;
  }
  
  .estudos-grid {
    grid-template-columns: repeat(3, minmax(140px, 1fr));
    grid-auto-rows: 110px;
  }
}

@media (max-width: 768px) {
  .status-badges {
    flex-wrap: wrap;
    gap: 12px;
  }
  
  .status-badge-group {
    border-right: none;
    padding-right: 0;
    border-bottom: 1px solid #e0e0e0;
    padding-bottom: 8px;
    margin-bottom: 8px;
    width: 100%;
    justify-content: space-around;
  }
  
  .compact-field {
    min-width: 120px !important;
  }
}

@media (max-width: 600px) {
  .status-circle {
    width: 32px;
    height: 32px;
  }
  
  .status-count {
    font-size: 0.8rem;
  }
  
  .status-label {
    font-size: 0.6rem;
  }
  
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
  
  .chart-container-wrapper {
    height: 250px;
  }
  
  .estudos-grid {
    grid-template-columns: repeat(2, minmax(120px, 1fr));
    grid-auto-rows: 100px;
  }
  
  .d-flex.gap-2 {
    flex-wrap: wrap;
  }
}

:deep(.v-btn) {
  transition: all 0.2s ease;
}

:deep(.v-btn:hover) {
  transform: translateY(-1px);
}

:deep(.v-chip) {
  transition: all 0.2s ease;
}

:deep(.v-chip:hover) {
  transform: scale(1.05);
}

.table-responsive::-webkit-scrollbar {
  height: 6px;
}

.table-responsive::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.table-responsive::-webkit-scrollbar-thumb {
  background: #c5c5c5;
  border-radius: 3px;
}

.table-responsive::-webkit-scrollbar-thumb:hover {
  background: #a8a8a8;
}
</style>