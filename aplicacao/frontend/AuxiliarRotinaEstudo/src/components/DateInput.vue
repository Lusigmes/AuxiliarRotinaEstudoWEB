<template>
  <v-text-field
    v-model="dataFormatada"
    :label="label"
    variant="outlined"
    :error-messages="errorMsg"
    readonly
    @click="modal = true"
    v-bind="$attrs"
  >
    <template #append>
      <v-icon color="primary">mdi-calendar</v-icon>
    </template>
  </v-text-field>

  <v-dialog v-model="modal" width="auto">
    <v-date-picker
      v-model="dataSelecionada"
      :locale="ptBR"
      @update:model-value="onDataSelecionada"
    />
  </v-dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { ptBR } from 'date-fns/locale/pt-BR';
import { 
  converterStringParaData, 
  formatarDataParaPTBR 
} from '@/utils/dateUtils';

interface Props {
    valorData: string;
    label?: string;
    errorMsg?: string;
};

const props = defineProps<Props>();
const emit = defineEmits<{
    'update:valorData': [value: string];
}>();

const modal = ref(false);
const dataSelecionada = ref<Date | null>(null);

watch(() => props.valorData, (novaData) => {
    if (novaData) {
        dataSelecionada.value = converterStringParaData(novaData);
    } else {
        dataSelecionada.value = null;
    }
}, { immediate: true });

const dataFormatada = computed(() => props.valorData);

function onDataSelecionada(data: Date | null){
    modal.value = false;
    if(data){
        const dataFormatada = formatarDataParaPTBR(data);
        emit('update:valorData', dataFormatada);
    }
};
</script>