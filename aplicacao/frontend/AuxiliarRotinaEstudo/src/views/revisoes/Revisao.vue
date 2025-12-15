<script setup lang="ts">
import { contarRevisoesAtrasadas, contarRevisoesPendentes, listarRevisoes, listarRevisoesAtrasadas, listarRevisoesPendentes } from '@/api/RevisaoService';
import type { RevisaoResponseInterface } from '@/types';
import { onMounted, ref } from 'vue';
import RevisaoDetalhe from './RevisaoDetalhe.vue';
import RevisaoCalendario from './RevisaoCalendario.vue';
import RevisoesPendentes from './RevisoesPendentes.vue';
import RevisoesAtrasadas from './RevisoesAtrasadas.vue';
import { converterStringParaData } from '@/utils/dateUtils';

const breadcrumbs = [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Revisões', disabled: true }
];

const abaAtiva = ref('calendario');
const loading = ref(false);

const revisoes = ref<RevisaoResponseInterface[]>([]);
const revisaoSelecionada = ref<RevisaoResponseInterface | null>(null);

const contadorPendentes = ref(0);
const contadorAtrasadas = ref(0);

async function carregarContadores(){
  try{
    contadorPendentes.value = await contarRevisoesPendentes();
    contadorAtrasadas.value = await contarRevisoesAtrasadas();

  }catch(error){
    console.error('Erro ao carregar contadores:', error);
    contadorPendentes.value =0;
    contadorAtrasadas.value =0;

  }
}
async function carregarRevisoes() {
  loading.value = true;
  try{
    revisoes.value = await listarRevisoes();
    await carregarContadores();
  } catch (error) {
    console.error('Erro ao carregar revisões:', error);
    alert('Erro ao carregar revisões. Tente novamente.');
  } finally {
    loading.value = false;
  }
};

function abrirDetalhesRevisao(revisao: RevisaoResponseInterface){
  revisaoSelecionada.value = revisao;
};

function fecharDetalhes(){
  revisaoSelecionada.value = null;
};

async function atualizarListas(){
  await carregarRevisoes();
  await carregarContadores();
};


const forceUpdateKey = ref(0);

function forcarAtualizacao() {
  forceUpdateKey.value += 1;
  atualizarListas();
}

function onRevisaoAtualizada(revisao: RevisaoResponseInterface) {
  if (!revisao || !revisao.dataRevisao) {
    console.error('Revisão ou dataRevisao está indefinido:', revisao);
    atualizarListas();
    return;
  }
  
  const dataRevisao = converterStringParaData(revisao.dataRevisao);
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  
  
  atualizarListas();
  forcarAtualizacao();
  

  if (dataRevisao < hoje && !revisao.concluida) {
    setTimeout(() => {
      alert('Revisão atualizada para data passada. Verifique a aba "Atrasadas".');
    }, 500);
  }
}

onMounted( async() =>{
  await carregarRevisoes();
})

</script>

<template>
  <v-container fluid class="pa-6">
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template #divider><v-icon>mdi-chevron-right</v-icon></template>
    </v-breadcrumbs>

    <v-card variant="flat" class="mb-6">
      <v-card-item>
        <template #prepend>
          <v-avatar color="purple" variant="tonal" size="48">
            <v-icon icon="mdi-calendar-check" />
          </v-avatar>
        </template>

        <v-card-title class="text-h4">Revisões</v-card-title>
        <v-card-subtitle class="text-h6">
          Controle suas revisões programadas
        </v-card-subtitle>
      </v-card-item>
    </v-card>

  <v-card variant="flat" class="mb-4">
      <v-tabs 
        v-model="abaAtiva" 
       color="primary" 
        slider-color="primary"
        class="tabs-no-bg"
      >
        <v-tab 
          value="calendario"
          :class="{'active-tab': abaAtiva === 'calendario'}"
        >
          <v-icon start>mdi-calendar-month</v-icon>
          Visão Geral
        </v-tab>
        <v-tab
        color="warning"
        value="pendentes"
        :class="{'active-tab': abaAtiva === 'pendentes'}"
        >
        <v-icon start>mdi-clock-outline</v-icon>
        Pendentes
          <v-badge 
            v-if="contadorPendentes > 0" 
            color="warning" 
            :content="contadorPendentes" 
            inline 
            class="ml-2" 
          />
        </v-tab>
        <v-tab 
        color="error"
          value="atrasadas"
          :class="{'active-tab': abaAtiva === 'atrasadas'}"
        >
          <v-icon start>mdi-alert-circle-outline</v-icon>
          Atrasadas
          <v-badge 
            v-if="contadorAtrasadas > 0" 
            color="error" 
            :content="contadorAtrasadas" 
            inline 
            class="ml-2" 
            />
        </v-tab>
      </v-tabs>
      <v-window v-model="abaAtiva">
        <v-window-item value="calendario">
          <RevisaoCalendario 
            :revisoes="revisoes"
            @ver-detalhes="abrirDetalhesRevisao"
          />
        </v-window-item>

        <v-window-item value="pendentes">
          <RevisoesPendentes 
           :key="'pendentes-' + forceUpdateKey"
            @atualizar="onRevisaoAtualizada"
          />
        </v-window-item>

        <v-window-item value="atrasadas">
          <RevisoesAtrasadas 
           :key="'atrasadas-' + forceUpdateKey"
            @ver-detalhes="abrirDetalhesRevisao"
            @atualizar="forcarAtualizacao"
          />
        </v-window-item>
      </v-window>
    </v-card>

    <v-card v-if="loading" variant="flat" class="text-center py-12 mt-4">
      <v-card-text>
        <v-progress-circular indeterminate color="primary" size="64" />
        <div class="text-h6 mt-4">Carregando revisões...</div>
      </v-card-text>
    </v-card>

    <RevisaoDetalhe 
      :revisao="revisaoSelecionada"
      @fechar="fecharDetalhes"
      @atualizar="atualizarListas"
    />
  </v-container>
</template>

<style scoped>
.v-badge {
    margin-left: 8px;
}
.active-tab {
  background-color: rgba(var(--v-theme-primary), 0.1);
  font-weight: bold;
}

.v-tab {
  transition: all 0.3s ease;
}

.v-tab:hover {
  background-color: rgba(var(--v-theme-primary), 0.05);
}

.tabs-no-bg {
  background-color: transparent !important;
}
</style>