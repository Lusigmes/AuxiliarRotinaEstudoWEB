<script setup lang="ts">
import { computed, ref } from 'vue';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData, formatarDataParaPTBR } from '@/utils/dateUtils';
import ListaRevisoes from './ListaRevisoes.vue';

interface Props {
  revisoes: RevisaoResponseInterface[];
  loading?: boolean;
}

const props = defineProps<Props>();
const emit = defineEmits<{
  (e: 'selecionar-revisao', revisao: RevisaoResponseInterface): void;  
  (e: 'ver-detalhes', payload: { revisao: RevisaoResponseInterface, origemLista?: boolean }): void;  
}>();

const mesAtual = ref(new Date().getMonth());
const anoAtual = ref(new Date().getFullYear());

const modalVisivel = ref(false);
const dataSelecionada = ref<Date | null>(null);
const revisoesDoDia = ref<RevisaoResponseInterface[]>([]);

const revisoesPorData = computed(() => {
  const agrupadas: Record<string, RevisaoResponseInterface[]> = {};
  
  props.revisoes.forEach(revisao => {
    const dataStr = formatarDataParaPTBR(converterStringParaData(revisao.dataRevisao));
    if (!agrupadas[dataStr]) {
      agrupadas[dataStr] = [];
    }
    agrupadas[dataStr].push(revisao);
  });
  
  return agrupadas;
});

const diasDoMes = computed(() => {
  const primeiroDia = new Date(anoAtual.value, mesAtual.value, 1);
  const ultimoDia = new Date(anoAtual.value, mesAtual.value + 1, 0);
  const dias = [];
  
  for (let i = 0; i < primeiroDia.getDay(); i++) {
    dias.push(null);
  }
  
  for (let dia = 1; dia <= ultimoDia.getDate(); dia++) {
    dias.push(new Date(anoAtual.value, mesAtual.value, dia));
  }
  
  return dias;
});

const nomeMes = computed(() => {
  return new Date(anoAtual.value, mesAtual.value).toLocaleDateString('pt-BR', {
    month: 'long',
    year: 'numeric'
  }).replace(/^\w/, c => c.toUpperCase());
});

function mesAnterior() {
  if (mesAtual.value === 0) {
    mesAtual.value = 11;
    anoAtual.value--;
  } else {
    mesAtual.value--;
  }
}

function proximoMes() {
  if (mesAtual.value === 11) {
    mesAtual.value = 0;
    anoAtual.value++;
  } else {
    mesAtual.value++;
  }
}

function revisoesNoDia(data: Date | null): RevisaoResponseInterface[] {
  if (!data) return [];
  const dataStr = formatarDataParaPTBR(data);
  return revisoesPorData.value[dataStr] || [];
}

function abrirModalDoDia(data: Date) {
  if (!data) return;
  
  dataSelecionada.value = data;
  revisoesDoDia.value = revisoesNoDia(data);
  
  if (revisoesDoDia.value.length > 0) {
    modalVisivel.value = true;
  }
}

function formatarContagem(revisoes: RevisaoResponseInterface[]) {
  const pendentes = revisoes.filter(r => !r.concluida).length;
  const concluidas = revisoes.filter(r => r.concluida).length;
  
  if (pendentes === 0 && concluidas === 0) return '';
  if (concluidas === 0) return `${pendentes} ⏱️`;
  if (pendentes === 0) return `${concluidas} ✅`;
  return `${pendentes}⏱️ ${concluidas}✅`;
}

const diasSemana = ['Dom', 'Seg', 'Ter', 'Qua', 'Qui', 'Sex', 'Sáb'];

function fecharModal() {
  modalVisivel.value = false;
  dataSelecionada.value = null;
  revisoesDoDia.value = [];
}

function selecionarRevisaoDoModal(revisao: RevisaoResponseInterface) {
  fecharModal();
  emit('selecionar-revisao', revisao);
};

function verDetalhesDoModal(payload: { revisao: RevisaoResponseInterface, origemLista: boolean }) {
  emit('ver-detalhes', payload);  
};
</script>

<template>
  <div class="calendario-revisoes">
    <div class="d-flex justify-space-between align-center mb-4">
      <div>
        <h3 class="text-h5 font-weight-bold">{{ nomeMes }}</h3>
        <div class="text-caption text-grey">
          Total: {{ revisoes.length }} revisões
        </div>
      </div>
      
      <div class="d-flex gap-1">
        <v-btn icon @click="mesAnterior" elevation="0" size="small">
          <v-icon>mdi-chevron-left</v-icon>
        </v-btn>
        <v-btn icon @click="proximoMes" elevation="0" size="small">
          <v-icon>mdi-chevron-right</v-icon>
        </v-btn>
      </div>
    </div>

    <div class="calendario-header">
      <div v-for="dia in diasSemana" :key="dia" class="dia-header text-center">
        <span class="text-caption font-weight-medium">{{ dia }}</span>
      </div>
    </div>

    <div class="calendario-grid compacto">
      <div
        v-for="(dia, index) in diasDoMes"
        :key="index"
        class="dia-cell compacto"
        :class="{
          'dia-vazio': !dia,
          'dia-hoje': dia && formatarDataParaPTBR(dia) === formatarDataParaPTBR(new Date()),
          'dia-com-revisoes': dia && revisoesNoDia(dia).length > 0
        }"
        @click="dia && abrirModalDoDia(dia)"
      >
        <template v-if="dia">
          <div class="dia-numero compacto">
            {{ dia.getDate() }}
          </div>
          
          <div class="dia-revisoes compacto">
            <div class="contador-revisoes compacto">
              {{ formatarContagem(revisoesNoDia(dia)) }}
            </div>
          </div>
        </template>
      </div>
    </div>  
  
    <div class="legenda mt-3">
      <div class="d-flex align-center gap-3 justify-center">
        <div class="d-flex align-center gap-1">
          <div class="indicador-indisponivel"></div>
          <span class="text-caption">Hoje</span>
        </div>

        <div class="d-flex align-center gap-1">
          <div class="indicador-revisao"></div>
          <span class="text-caption">Com revisões</span>
        </div>

        <div class="d-flex align-center gap-1">
          <div class="indicador-vazio"></div>
          <span class="text-caption">Sem revisões</span>
        </div>
        
        <div class="d-flex align-center gap-1">
          <span class="text-caption">⏱️ Revisões pendentes</span>
        </div>
        <div class="d-flex align-center gap-1">
          <span class="text-caption">✅ Revisões concluidas</span>
        </div>
      </div>
    </div>
    
    <v-card v-if="revisoes.length === 0 && !loading" variant="flat" class="text-center py-6 mt-3">
      <v-card-text>
        <v-icon size="48" color="grey-lighten-1" class="mb-3">mdi-calendar-blank</v-icon>
        <h3 class="text-subtitle-1 text-medium-emphasis mb-1">Nenhuma revisão agendada</h3>
        <p class="text-caption text-medium-emphasis">
          Registre seus estudos para ver as revisões aqui
        </p>
      </v-card-text>
    </v-card>

      <lista-revisoes
        :visivel="modalVisivel"
        :data="dataSelecionada"
        :revisoes="revisoesDoDia"
        @fechar="fecharModal"
        @selecionar-revisao="selecionarRevisaoDoModal"
        @ver-detalhes="verDetalhesDoModal"  
      />
  </div>
</template>

<style scoped>
.calendario-header {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
  margin-bottom: 4px;
}

.dia-header {
  padding: 4px 0;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 4px;
}

.calendario-grid.compacto {
  display: grid;
  grid-template-columns: repeat(7, 1fr);
  gap: 2px;
}

.dia-cell.compacto {
  height: 70px;
  min-height: 70px;
  border: 1px solid rgba(0, 0, 0, 0.12);
  border-radius: 6px;
  padding: 4px;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  display: flex;
  flex-direction: column;
  font-size: 0.8rem;
}

.dia-cell.compacto:hover {
  background-color: rgba(0, 0, 0, 0.04);
  transform: translateY(-1px);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.dia-vazio {
  background-color: rgba(0, 0, 0, 0.02);
  border: 1px dashed rgba(0, 0, 0, 0.1);
  cursor: default;
}

.dia-hoje {
  background-color: #e3f2fd !important;
  border-color: #2196f3 !important;
  font-weight: bold;
}

.dia-com-revisoes {
  background-color: #f3e5f5;
  border-color: #9c27b0;
  font-weight: bold;
}

.dia-numero.compacto {
  font-size: 0.75rem; 
  font-weight: 500;
  margin-bottom: 2px; 
  text-align: center;
}

.dia-revisoes.compacto {
  flex-grow: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.contador-revisoes.compacto {
  font-size: 0.65rem; 
  line-height: 1.1;
  color: #666;
  width: 100%;
  text-align: center;
  padding: 0 2px;
  word-break: break-word;
  overflow: hidden;
  text-overflow: ellipsis;
  max-height: 28px;
}

.legenda {
  padding: 8px;
  background-color: rgba(0, 0, 0, 0.02);
  border-radius: 6px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.indicador-indisponivel {
  width: 10px;
  height: 10px;
  background-color: #e3f2fd;
  border: 1px solid #2196f3;
  border-radius: 2px;
}

.indicador-revisao {
  width: 10px;
  height: 10px;
  background-color: #f3e5f5;
  border: 1px solid #9c27b0;
  border-radius: 2px;
}

.indicador-vazio {
  width: 10px;
  height: 10px;
  background-color: rgba(0, 0, 0, 0.02);
  border: 1px dashed rgba(0, 0, 0, 0.1);
  border-radius: 2px;
}

.gap-1 {
  gap: 4px;
}

.gap-3 {
  gap: 12px;
}
</style>