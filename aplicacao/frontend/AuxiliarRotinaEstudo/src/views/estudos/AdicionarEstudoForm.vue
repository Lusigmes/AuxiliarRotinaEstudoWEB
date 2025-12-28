<script setup lang="ts">
import type { EstudoInterface } from '@/types';
import { getDataHoje, validarFormatoData } from '@/utils/dateUtils';
import { reactive } from 'vue';
import * as yup from 'yup';

interface Emits{
    (e:'adicionar-estudo', estudo: EstudoInterface): void;
    (e:'cancelar'): void;
    (e:'fechar'): void;
};
const emit = defineEmits<Emits>();

const props = defineProps<{
    loading?: boolean;
}>();

const form = reactive({
    nomeDisciplina:'',
    tema: '',
    tempoDeEstudo: 0,
    diaDoEstudo: getDataHoje(),
});

const errors = reactive({
    nomeDisciplina:'',
    tema: '',
    tempoDeEstudo: '',
    diaDoEstudo: '',
});

const schema = yup.object({
  nomeDisciplina: yup
    .string()
    .required('Disciplina é obrigatória')
    .min(2, 'Disciplina deve ter pelo menos 2 caracteres')
    .max(50, 'Disciplina deve ter no máximo 50 caracteres'),
  
  tema: yup
    .string()
    .required('Tema é obrigatório')
    .min(2, 'Tema deve ter pelo menos 2 caracteres')
    .max(100, 'Tema deve ter no máximo 100 caracteres'),
  
  tempoDeEstudo: yup
    .number()
    .typeError('Tempo deve ser um número')
    .required('Tempo é obrigatório')
    .min(1, 'Tempo mínimo é 1 hora')
    .max(24, 'Tempo máximo é 24 horas)'),
  
  diaDoEstudo: yup
    .string()
    .required('Data é obrigatória')
    .test('data-valida', 'Data inválida', validarFormatoData), 
});

const validarForm = async (): Promise<boolean> => {
try {
    await schema.validate(form, {abortEarly: false});
    Object.keys(errors).forEach(key => {
        errors[key as keyof typeof errors] = '';
    });

    return true;

} catch (erro: any) {
 Object.keys(errors).forEach(key => {
      errors[key as keyof typeof errors] = '';
    });
    
    if (erro.inner) {
      erro.inner.forEach((e: yup.ValidationError) => {
        const field = e.path as keyof typeof errors;
        if (field in errors) {
          errors[field] = e.message;
        }
      });
    }

    return false;
    }
};


const salvarEstudo = async () => {
    const valido = await validarForm();
    if(!valido) return;

    emit('adicionar-estudo', {...form});
    emit('fechar');

    Object.assign(form, {
        nomeDisciplina:'',
        tema: '',
        tempoDeEstudo: 0,
        diaDoEstudo: getDataHoje(),
    });
};
</script>

<template>
  <v-card>
    <v-card-item>
      <template #prepend>
        <v-avatar color="primary" variant="tonal" size="40">
          <v-icon icon="mdi-book-plus" />
        </v-avatar>
      </template>
      
      <v-card-title class="text-h5">Adicionar Novo Estudo</v-card-title>
      <v-card-subtitle>Preencha os dados do estudo</v-card-subtitle>

      <template #append>
        <v-btn icon @click="$emit('cancelar')" elevation="0">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </template>
    </v-card-item>

    <v-card-text>
      <v-form @submit.prevent="salvarEstudo">
        <v-row>
          <v-col cols="12" md="6">
            <v-text-field
              v-model="form.nomeDisciplina"
              label="Disciplina"
              placeholder="Ex: Matemática, Programação..."
              variant="outlined"
              :error-messages="errors.nomeDisciplina"
              required
            />
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field
              v-model="form.tema"
              label="Tema/Conteúdo"
              placeholder="Ex: Cálculo Integral, POO..."
              variant="outlined"
              :error-messages="errors.tema"
              required
            />
          </v-col>

          <v-col cols="12" md="6">
            <v-text-field
              v-model.number="form.tempoDeEstudo"
              label="Tempo de Estudo (horas)"
              type="number"
              variant="outlined"
              :error-messages="errors.tempoDeEstudo"
              required
            />
          </v-col>

          <v-col cols="12" md="6">
            <DataInput
              v-model="form.diaDoEstudo"
              label="Data do Estudo"
              :error-msg="errors.diaDoEstudo"
            />
          </v-col>
        </v-row>

        <div class="d-flex justify-end gap-2 mt-4">
          <v-btn
            variant="outlined"
            color="grey"
            @click="$emit('cancelar')"
            :disabled="props.loading"
          >
          <v-icon icon="mdi-close" class="mr-2" /> Cancelar

          </v-btn>
          <v-btn
            color="primary"
            variant="tonal"
            type="submit"
            :loading="props.loading"
          >
            <v-icon icon="mdi-check" class="mr-2" />
            Adicionar Estudo
          </v-btn>
        </div>
      </v-form>
    </v-card-text>
  </v-card>
</template>


<style scoped>
.gap-2 {
  gap: 8px;
}
</style>