<script setup lang="ts">
import type { RevisaoResponseInterface } from '@/types';
import { computed, onMounted, ref, watch } from 'vue';
import RevisaoDetalhe from './RevisaoDetalhe.vue';
import RevisaoCalendario from './RevisaoCalendario.vue';
import RevisoesPendentes from './RevisoesPendentes.vue';
import RevisoesAtrasadas from './RevisoesAtrasadas.vue';
import { converterStringParaData } from '@/utils/dateUtils';
import { useRevisaoStore } from '@/stores/revisaoStore';
import RevisoesConcluidas from './RevisoesConcluidas.vue';
import { useNotification } from '@/composables/useNotification';

const breadcrumbs = [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Revisões', disabled: true }
];

const abaAtiva = ref('calendario');
const revisaoSelecionada = ref<RevisaoResponseInterface | null>(null);
const origemDetalhesLista = ref(false); 

const recarregarPendentes = ref(0)
const recarregarAtrasadas = ref(0)
const recarregarConcluidas = ref(0)

const revisaoStore = useRevisaoStore();
const { showNotification } = useNotification();
  
const revisoes = computed(() => revisaoStore.todasRevisoes);
const contadorPendentes = computed(() => revisaoStore.contadorPendentes);
const contadorAtrasadas = computed(() => revisaoStore.contadorAtrasadas);
const contadorConcluidas = computed(() => revisaoStore.contadorConcluidas);
const loading = computed(() => revisaoStore.loading);

function abrirDetalhesRevisao(payload: { revisao: RevisaoResponseInterface, origemLista?: boolean }) {
  revisaoSelecionada.value = payload.revisao;
  origemDetalhesLista.value = payload.origemLista || false;
};

function fecharDetalhes(){
  revisaoSelecionada.value = null;
  origemDetalhesLista.value = false;
};

function abrirDetalhesRevisaoSimples(revisao: RevisaoResponseInterface) {
  revisaoSelecionada.value = revisao;
  origemDetalhesLista.value = false;
};

async function atualizarListas(){
  await revisaoStore.carregarTodasRevisoes();
  showNotification('Listas de revisões atualizadas', 'success');
};

function onRevisaoAtualizada(revisao: RevisaoResponseInterface) {
  if (!revisao || !revisao.dataRevisao) {
    console.error('Revisão ou dataRevisao está indefinido:', revisao)
    atualizarListas()
    return
  }
  
  const dataRevisao = converterStringParaData(revisao.dataRevisao)
  const hoje = new Date()
  hoje.setHours(0, 0, 0, 0)
  
  setTimeout(async () => {
    await revisaoStore.carregarTodasRevisoes()
    
    if (revisao.concluida) {
      recarregarConcluidas.value++
      showNotification('Revisão concluída com sucesso!', 'success');
    } else if (dataRevisao < hoje) {
      recarregarAtrasadas.value++
      showNotification('Revisão atualizada para data passada.', 'warning');
      setTimeout(() => {
        abaAtiva.value = 'atrasadas'
      }, 500)
    } else if (dataRevisao >= hoje) {
      recarregarPendentes.value++
      showNotification('Revisão reagendada com sucesso!', 'success');
    }
  }, 100)
};

watch(() => abaAtiva.value, async (novaAba) => {
  await revisaoStore.atualizarContadores();
  
  if (novaAba === 'pendentes') {
    recarregarPendentes.value++;
    showNotification('Carregando revisões pendentes', 'info');
  } else if (novaAba === 'atrasadas') {
    recarregarAtrasadas.value++;
    showNotification('Carregando revisões atrasadas', 'info');
  } else if (novaAba === 'concluidas') {
    recarregarConcluidas.value++;
    showNotification('Carregando revisões concluídas', 'info');
  }
});

onMounted(async () => {
  await revisaoStore.inicializar();
  showNotification('Revisões carregadas com sucesso!', 'success');
});
</script>

<template>
  <v-container fluid class="pa-6">
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template #divider><v-icon>mdi-chevron-right</v-icon></template>
    </v-breadcrumbs>

    <v-card variant="flat" class="mb-6">
      <v-card-item>
        <template #prepend>
          <v-avatar color="orange" variant="tonal" size="48">
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
      <v-tabs v-model="abaAtiva" color="primary" slider-color="primary" class="tabs-no-bg">
        <v-tab value="calendario" :class="{'active-tab': abaAtiva === 'calendario'}">
          <v-icon start>mdi-calendar-month</v-icon>
          Visão Geral
        </v-tab>
        <v-tab color="warning" value="pendentes" :class="{'active-tab': abaAtiva === 'pendentes'}">
          <v-icon start>mdi-clock-outline</v-icon>
          Pendentes
          <v-badge v-if="contadorPendentes > 0" color="warning" :content="contadorPendentes" inline class="ml-2" />
        </v-tab>
        <v-tab color="error" value="atrasadas" :class="{'active-tab': abaAtiva === 'atrasadas'}">
          <v-icon start>mdi-alert-circle-outline</v-icon>
          Atrasadas
          <v-badge v-if="contadorAtrasadas > 0" color="error" :content="contadorAtrasadas" inline class="ml-2" />
        </v-tab>
        <v-tab color="success" value="concluidas" :class="{'active-tab': abaAtiva === 'concluidas'}">
          <v-icon start>mdi-check-circle-outline</v-icon>
          Concluídas
          <v-badge v-if="contadorConcluidas > 0" color="success" :content="contadorConcluidas" inline class="ml-2" />
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
            :key="recarregarPendentes"
            @ver-detalhes="abrirDetalhesRevisaoSimples"
            @atualizar="onRevisaoAtualizada"
          />
        </v-window-item>

        <v-window-item value="atrasadas">
          <RevisoesAtrasadas 
            :key="recarregarAtrasadas"
            @ver-detalhes="abrirDetalhesRevisaoSimples"
            @atualizar="onRevisaoAtualizada"
          />
        </v-window-item>
        
        <v-window-item value="concluidas">
          <RevisoesConcluidas 
            :key="recarregarConcluidas"
            @ver-detalhes="abrirDetalhesRevisaoSimples"
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
      :origem-lista="origemDetalhesLista"
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