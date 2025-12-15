<script setup lang="ts">
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData, formatarDataParaPTBR } from '@/utils/dateUtils';
import { computed } from 'vue';
import { ref } from 'vue';


interface Props{
    revisoes: RevisaoResponseInterface[];
    loading?: boolean;
    titulo: string;
    tipo: 'pendentes' | 'atrasadas'  | 'todas' ;
}

const props = defineProps<Props>();
const emit = defineEmits<{
   (e: 'selecionar-revisao', revisao: RevisaoResponseInterface): void;  

}>()

const revisoesOrdenadas = computed(() => {
  return [...props.revisoes].sort((a, b) => {
    const dataA = converterStringParaData(a.dataRevisao);
    const dataB = converterStringParaData(b.dataRevisao);
    return dataA.getTime() - dataB.getTime();
  });
});

// Cor do status
function corStatus(revisao: RevisaoResponseInterface) {
  if (revisao.concluida) return 'green';
  if (props.tipo === 'atrasadas') return 'red';
  return 'orange';
}

// Ícone do status
function iconeStatus(revisao: RevisaoResponseInterface) {
  if (revisao.concluida) return 'mdi-check-circle';
  if (props.tipo === 'atrasadas') return 'mdi-alert-circle';
  return 'mdi-clock-alert';
}

// Texto do status
function textoStatus(revisao: RevisaoResponseInterface) {
  if (revisao.concluida) return 'Concluída';
  if (props.tipo === 'atrasadas') return 'Atrasada';
  return 'Pendente';
}

// Verificar se a data é hoje
function ehHoje(dataStr: string) {
  const hoje = formatarDataParaPTBR(new Date());
  return dataStr === hoje;
}
</script>

<template>
  <div class="lista-revisoes">
    <!-- Cabeçalho -->
    <div class="d-flex justify-space-between align-center mb-4">
      <h3 class="text-h5 font-weight-bold">{{ titulo }}</h3>
      <v-chip :color="tipo === 'atrasadas' ? 'red' : 'orange'" variant="flat">
        {{ revisoes.length }} {{ revisoes.length === 1 ? 'revisão' : 'revisões' }}
      </v-chip>
    </div>

    <!-- Lista -->
    <div v-if="revisoesOrdenadas.length > 0" class="lista-container">
      <v-card
        v-for="revisao in revisoesOrdenadas"
        :key="revisao.id"
        class="mb-2 revisao-item"
        variant="outlined"
        @click="$emit('selecionar-revisao', revisao)"
      >
        <v-card-item class="py-2">
          <template #prepend>
            <v-avatar :color="corStatus(revisao)" variant="tonal" size="40">
              <v-icon :icon="iconeStatus(revisao)" color="white" />
            </v-avatar>
          </template>

          <v-card-title class="text-body-1 font-weight-medium">
            Revisão #{{ revisao.id }}
          </v-card-title>
          
          <v-card-subtitle class="d-flex align-center gap-2">
            <v-chip
              size="x-small"
              :color="ehHoje(revisao.dataRevisao) ? 'primary' : 'grey'"
              variant="flat"
            >
              <v-icon size="12" class="mr-1">mdi-calendar</v-icon>
              {{ revisao.dataRevisao }}
              <v-icon v-if="ehHoje(revisao.dataRevisao)" size="12" class="ml-1">mdi-star</v-icon>
            </v-chip>
            
            <v-chip size="x-small" :color="corStatus(revisao)" variant="outlined">
              {{ textoStatus(revisao) }}
            </v-chip>
          </v-card-subtitle>

          <template #append>
            <v-btn icon size="small" variant="text">
              <v-icon>mdi-chevron-right</v-icon>
            </v-btn>
          </template>
        </v-card-item>
      </v-card>
    </div>

    <!-- Sem revisões -->
    <v-card v-else variant="flat" class="text-center py-8">
      <v-card-text>
        <v-icon
          size="64"
          :color="tipo === 'atrasadas' ? 'grey-lighten-1' : 'orange-lighten-3'"
          class="mb-4"
        >
          {{ tipo === 'atrasadas' ? 'mdi-check-all' : 'mdi-check-circle-outline' }}
        </v-icon>
        <h3 class="text-h6 text-medium-emphasis mb-2">
          {{ tipo === 'pendentes' ? 'Nenhuma revisão pendente' : 
             tipo === 'atrasadas' ? 'Nenhuma revisão atrasada' : 
             'Nenhuma revisão encontrada' }}
        </h3>
        <p class="text-body-2 text-medium-emphasis">
          {{ tipo === 'pendentes' ? 'Todas as revisões estão em dia!' :
             tipo === 'atrasadas' ? 'Ótimo! Você está em dia com suas revisões' :
             'Registre estudos para ver revisões aqui' }}
        </p>
      </v-card-text>
    </v-card>
  </div>
</template>

<style scoped>
.lista-container {
  max-height: 500px;
  overflow-y: auto;
}

.revisao-item {
  cursor: pointer;
  transition: all 0.2s ease;
}

.revisao-item:hover {
  background-color: rgba(0, 0, 0, 0.02);
  border-color: rgb(var(--v-theme-primary));
  transform: translateX(2px);
}

:deep(.revisao-item .v-card-title) {
  padding-top: 4px;
  padding-bottom: 4px;
}

:deep(.revisao-item .v-card-subtitle) {
  padding-top: 0;
  padding-bottom: 0;
}
</style>