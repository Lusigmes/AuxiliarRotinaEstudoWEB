<script setup lang="ts">
import type { RegistroUsuarioInterface } from '@/types';
import {ref, reactive} from 'vue';
import * as yup from 'yup';
import { useRouter } from 'vue-router';
import { watch } from 'vue';
import { useAuth } from '@/composables/useAuth';

const router = useRouter();
const { registro, error: authError, loading } = useAuth();

interface Emits{
    (e:'close'): void // emitir algo
    (e:'success'): void // emitir algo
}

const emit = defineEmits<Emits>()

const usuario = reactive<RegistroUsuarioInterface>({
      nome: "",
      email: "",
      senha: "",
  });

const errors = reactive({
    nome: "",
    email: "",
    senha: "",
    confirmarSenha: ""
});

const confirmarSenha = ref("");
const errorGeral = ref("");
const mostrarSenha = ref(false);
const mostrarConfirmarSenha = ref(false);

  
const schema = yup.object({
  nome: yup.string()
      .required('Nome é obrigatório')
      .min(2, 'Nome deve ter pelo menos 2 caracteres')
      .max(100, 'Nome deve ter no máximo 100 caracteres'),
  email: yup.string()
      .required('E-mail é obrigatório')
      .email('E-mail deve ser válido'),
  senha: yup.string()
  .required('Senha é obrigatória')
  .min(6, 'Senha deve ter pelo menos 6 caracteres'),
});


const validarCampo = async (campo: keyof typeof errors) => {
  try {
    await schema.validateAt(campo, usuario);
    errors[campo] = '';
  } catch (erro: any) {
    errors[campo] = erro.message;
  }
}

const validarConfirmacaoSenha = () => {
  if (usuario.senha !== confirmarSenha.value) {
    errors.confirmarSenha = 'As senhas não coincidem';
  } else {
    errors.confirmarSenha = '';
  }
}

const validarForm = async (): Promise<boolean> => {
  try {
    await schema.validate(usuario, { abortEarly: false });
    
    if (usuario.senha !== confirmarSenha.value) {
      errors.confirmarSenha = 'As senhas não coincidem';
      return false;
    }
    
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
}

async function registrar(){
    try{
      const valido = await validarForm();
      if(!valido) return;
          
      const usuarioEnvio = {
        ...usuario,
      };
      
      let sucesso = await registro(usuarioEnvio);
      if(sucesso){
        emit('success');
        router.push("/tela-principal");   

      }
    }catch (error: any) {
      errorGeral.value = error.message || 'Erro ao criar conta. Tente novamente.';
    } finally {
    }

}

watch([() => usuario.senha, confirmarSenha], () => {
  if (usuario.senha && confirmarSenha.value) {
    validarConfirmacaoSenha();
  }
});
</script>


<template>
  <v-dialog
    :model-value="true"
    max-width="500"
    persistent
    @update:model-value="$emit('close')"
  >
    <v-card class="register-card" elevation="16" rounded="xl">
      <v-card-item class="modal-header">
        <template #prepend>
          <v-avatar color="success" variant="tonal" size="40">
            <v-icon icon="mdi-account-plus" />
          </v-avatar>
        </template>
        
        <v-card-title class="text-h5 font-weight-bold">
          Criar Conta
        </v-card-title>
        
        <template #append>
          <v-btn
            icon
            variant="text"
            size="small"
            @click="$emit('close')"
          >
            <v-icon icon="mdi-close" />
          </v-btn>
        </template>
      </v-card-item>

      <v-divider />

      <v-card-text class="pa-6">
        <v-form @submit.prevent="registrar" class="register-form">
          <v-text-field
            v-model="usuario.nome"
            label="Nome completo"
            placeholder="Seu nome completo"
            :error="!!errors.nome"
            :error-messages="errors.nome"
            @blur="validarCampo('nome')"
            prepend-inner-icon="mdi-account"
            variant="outlined"
            color="primary"
            class="mb-4"
          />

          <v-text-field
            v-model="usuario.email"
            label="E-mail"
            type="email"
            placeholder="seu@email.com"
            :error="!!errors.email"
            :error-messages="errors.email"
            @blur="validarCampo('email')"
            prepend-inner-icon="mdi-email"
            variant="outlined"
            color="primary"
            class="mb-4"
          />

          <v-text-field
            v-model="usuario.senha"
            label="Senha"
            :type="mostrarSenha ? 'text' : 'password'"
            placeholder="Mínimo 6 caracteres"
            :error="!!errors.senha"
            :error-messages="errors.senha"
            @blur="validarCampo('senha')"
            prepend-inner-icon="mdi-lock"
            variant="outlined"
            color="primary"
            class="mb-4"
            :append-inner-icon="mostrarSenha ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="mostrarSenha = !mostrarSenha"
          />

          <v-text-field
            v-model="confirmarSenha"
            label="Confirmar Senha"
            :type="mostrarConfirmarSenha ? 'text' : 'password'"
            placeholder="Digite a senha novamente"
            :error="!!errors.confirmarSenha"
            :error-messages="errors.confirmarSenha"
            @blur="validarConfirmacaoSenha"
            prepend-inner-icon="mdi-lock-check"
            variant="outlined"
            color="primary"
            class="mb-2"
            :append-inner-icon="mostrarConfirmarSenha ? 'mdi-eye-off' : 'mdi-eye'"
            @click:append-inner="mostrarConfirmarSenha = !mostrarConfirmarSenha"
          />

          <v-alert
            v-if="errorGeral"
            type="error"
            variant="tonal"
            class="mt-4"
            closable
          >
            {{ errorGeral }}
          </v-alert>

          <v-alert
            v-if="authError"
            type="error"
            variant="tonal"
            class="mt-4"
            closable
          >
            {{ authError }}
          </v-alert>

          <v-card-actions class="form-actions px-0">
            <v-btn
              @click="$emit('close')"
              variant="outlined"
              color="grey"
              size="large"
              class="flex-grow-1"
            >
              <v-icon icon="mdi-close" class="mr-2" />
              Cancelar
            </v-btn>
            
            <v-btn
              type="submit"
              color="success"
              variant="flat"
              size="large"
              :loading="loading"
              :disabled="loading"
              class="flex-grow-1"
            >
              <template v-if="loading">
                <v-progress-circular
                  indeterminate
                  size="20"
                  width="2"
                  class="mr-2"
                />
                Criando conta...
              </template>
              <template v-else>
                <v-icon icon="mdi-check" class="mr-2" />
                Criar Conta
              </template>
            </v-btn>
          </v-card-actions>
        </v-form>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<style scoped>
.register-card {
  backdrop-filter: blur(10px);
}

.modal-header {
  padding: 1.5rem;
}

.register-form {
  padding: 0.5rem 0;
}

.form-actions {
  gap: 1rem;
  margin-top: 1.5rem;
}

.register-card {
  transition: all 0.3s ease;
}

:deep(.v-field--prepended) {
  padding-left: 12px;
}

:deep(.v-field--appended) {
  padding-right: 12px;
}
</style>