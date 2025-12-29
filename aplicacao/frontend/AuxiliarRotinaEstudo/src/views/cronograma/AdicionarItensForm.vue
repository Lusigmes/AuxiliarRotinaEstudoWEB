<script setup lang="ts">
import { ref } from 'vue';
import { DiasSemana } from '@/types/enums';
import type { ItemCronogramaInterface, ModoAdicao } from '@/types';
import { useNotification } from '@/composables/useNotification';

const props = defineProps<{
  modoAdicao: ModoAdicao;
  itensTemporarios: ItemCronogramaInterface[];
}>();

const emits = defineEmits(['adicionar-item', 'remover-item', 'cancelar', 'finalizar']);
const { showNotification } = useNotification();

const novoItem = ref<ItemCronogramaInterface>({ nomeDisciplina: '', diaSemana: DiasSemana.SEGUNDA });

const diasOptions = [
  { title: 'Segunda-feira', value: DiasSemana.SEGUNDA },
  { title: 'Terça-feira', value: DiasSemana.TERCA },
  { title: 'Quarta-feira', value: DiasSemana.QUARTA },
  { title: 'Quinta-feira', value: DiasSemana.QUINTA },
  { title: 'Sexta-feira', value: DiasSemana.SEXTA },
  { title: 'Sábado', value: DiasSemana.SABADO },
  { title: 'Domingo', value: DiasSemana.DOMINGO }
];

function formatarDiaSemana(dia: DiasSemana) {
  const encontrado = diasOptions.find(d => d.value === dia);
  return encontrado?.title || dia;
}

function emitirAdicionarItem() {
  if (!novoItem.value.nomeDisciplina) {
    showNotification('Digite o nome da disciplina', 'warning');
    return;
  }
  
  const itensNoMesmoDia = props.itensTemporarios.filter(
    item => item.diaSemana === novoItem.value.diaSemana
  );
  
  const itemComOrdem = {
    ...novoItem.value,
    ordem: itensNoMesmoDia.length  
  };
  
  emits('adicionar-item', itemComOrdem);
  novoItem.value = { nomeDisciplina: '', diaSemana: DiasSemana.SEGUNDA };
  
  showNotification('Disciplina adicionada a lista', 'success');
}
</script>

<template>
  <v-card variant="flat" elevation="2">
    <v-card-item>
      <template #prepend>
        <v-avatar color="green" variant="tonal" size="48">
          <v-icon icon="mdi-plus-circle" />
        </v-avatar>
      </template>

      <v-card-title class="text-h4">
        {{ props.modoAdicao === 'criar' ? 'Criar Cronograma' : 'Adicionar Itens' }}
      </v-card-title>
      <v-card-subtitle class="text-h6">
        {{ props.modoAdicao === 'criar' ? 'Adicione as disciplinas do seu cronograma' : 'Adicione novas disciplinas ao seu cronograma' }}
      </v-card-subtitle>
    </v-card-item>

    <v-card-text>
      <v-form @submit.prevent="emitirAdicionarItem" class="mb-6">
        <v-row>
          <v-col cols="12" md="5">
            <v-text-field v-model="novoItem.nomeDisciplina" label="Nome da Disciplina" placeholder="Ex: Matemática, Programação..." variant="outlined" required />
          </v-col>

          <v-col cols="12" md="5">
            <v-select v-model="novoItem.diaSemana" :items="diasOptions" label="Dia da Semana" variant="outlined" required />
          </v-col>

          <v-col cols="12" md="2">
            <v-btn color="primary" type="submit" block>
              <v-icon icon="mdi-plus" class="mr-2" /> Adicionar
            </v-btn>
          </v-col>
        </v-row>
      </v-form>

      <v-card variant="outlined" v-if="props.itensTemporarios.length > 0">
        <v-card-title class="text-h6">Itens Adicionados ({{ props.itensTemporarios.length }})</v-card-title>
        <v-card-text>
          <v-chip
            v-for="(item, index) in props.itensTemporarios"
            :key="index"
            color="blue"
            variant="flat"
            class="ma-1"
            closable
            @click:close="$emit('remover-item', index)"
          >
            {{ item.nomeDisciplina }} — {{ formatarDiaSemana(item.diaSemana) }}
          </v-chip>
        </v-card-text>
      </v-card>

      <div class="d-flex justify-center gap-4 mt-6">
        <v-btn variant="outlined" color="grey" @click="$emit('cancelar')">
          <v-icon icon="mdi-close" class="mr-2" /> Cancelar
        </v-btn>
        <v-btn color="primary" variant="tonal" :disabled="props.itensTemporarios.length === 0" @click="$emit('finalizar')">
          <v-icon icon="mdi-check" class="mr-2" /> {{ props.modoAdicao === 'criar' ? 'Criar Cronograma' : 'Adicionar Itens' }}
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>


<style scoped>
.gap-4 { gap: 16px; }
</style>
