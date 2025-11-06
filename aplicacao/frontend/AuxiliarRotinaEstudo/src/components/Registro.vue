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
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <!-- Cabeçalho do Modal -->
      <div class="modal-header">
        <h2>Criar Conta</h2>
        <button @click="$emit('close')" class="close-btn">×</button>
      </div>

      <!-- Formulário de Registro -->
      <form @submit.prevent="registrar" class="register-form">
        <div class="form-group">
          <label for="nome">Nome completo</label>
            <input
                id="nome"
                v-model="usuario.nome"
                type="text"
                placeholder="Seu nome completo"
                :class="{ 'error': errors.nome }"
                @blur="validarCampo('nome')"
            />
          <span v-if="errors.nome" class="error-text">{{ errors.nome }}</span>
        </div>

        <div class="form-group">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="usuario.email"
            type="email"
            placeholder="seu@email.com"
            :class="{ 'error': errors.email }"
            @blur="validarCampo('email')"
          />
          <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
        </div>

        <div class="form-group">
          <label for="senha">Senha</label>
          <input
            id="senha"
            v-model="usuario.senha"
            type="password"
            placeholder="Mínimo 6 caracteres"
            :class="{ 'error': errors.senha }"
            @blur="validarCampo('senha')"
          />
          <span v-if="errors.senha" class="error-text">{{ errors.senha }}</span>
        </div>

        <div class="form-group">
          <label for="confirmar-senha">Confirmar Senha</label>
          <input
            id="confirmar-senha"
            v-model="confirmarSenha"
            type="password"
            placeholder="Digite a senha novamente"
            :class="{ 'error': errors.confirmarSenha }"
            @blur="validarConfirmacaoSenha"
          />
          <span v-if="errors.confirmarSenha" class="error-text">{{ errors.confirmarSenha }}</span>
        </div>

        <div v-if="errorGeral" class="error-message">
          {{ errorGeral }}
        </div>

        <div class="form-actions">
          <button type="button" @click="$emit('close')" class="cancel-btn">
            Cancelar
          </button>
          <button type="submit" class="register-btn" :disabled="loading">
            {{ loading ? 'Criando conta...' : 'Criar Conta' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 1rem;
}

.modal-content {
  background: white;
  border-radius: 12px;
  width: 100%;
  max-width: 450px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem;
  border-bottom: 1px solid #e1e5e9;
}

.modal-header h2 {
  margin: 0;
  color: #333;
}

.close-btn {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  color: #666;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  color: #333;
}

.register-form {
  padding: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
  position: relative;
}

.form-group label {
  display: block;
  margin-bottom: 0.5rem;
  color: #333;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 0.75rem;
  border: 2px solid #e1e5e9;
  border-radius: 6px;
  font-size: 1rem;
  transition: border-color 0.3s;
  box-sizing: border-box;
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.form-group input.error {
  border-color: #dc3545;
}

.error-text {
  display: block;
  margin-top: 0.25rem;
  color: #dc3545;
  font-size: 0.875rem;
  min-height: 1.25rem;
}

.error-message {
  margin: 1rem 0;
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  text-align: center;
}

.form-actions {
  display: flex;
  gap: 1rem;
  margin-top: 1.5rem;
}

.cancel-btn, .register-btn {
  flex: 1;
  padding: 0.75rem;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s;
}

.cancel-btn {
  background: #f8f9fa;
  color: #666;
  border: 2px solid #e1e5e9;
}

.cancel-btn:hover {
  background: #e9ecef;
}

.register-btn {
  background: #28a745;
  color: white;
}

.register-btn:hover:not(:disabled) {
  background: #218838;
}

.register-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}
</style>