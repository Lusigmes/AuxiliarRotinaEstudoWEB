<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import {useRouter} from 'vue-router';
import Registro from './Registro.vue';
import { useAuth } from '@/composables/useAuth';
import type { LoginUsuarioInterface } from '@/types';

const router = useRouter();
const {login, error, loading, token} = useAuth();

const mostrarModalRegistro = ref(false);

const loginData = reactive<LoginUsuarioInterface>({
  email:'', senha:''
});

async function fazerLogin(){
  const sucesso = await login(loginData);
  if(sucesso){
    router.push("/tela-principal");
  }else{
    alert("Falha no login")
  }
}

const registroSucesso = () => {
  mostrarModalRegistro.value = false
  loginData.email = ''
  loginData.email = ''
};

watch(token, (newToken) => {
  console.log('Token mudou:', newToken);
});
</script>

<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1>📚 Sistema de Estudos</h1>
        <p>Organize seus estudos e revisões</p>
      </div>

      <form @submit.prevent="fazerLogin" class="login-form">
        <div class="form-group">
          <label for="email">E-mail</label>
          <input
            id="email"
            v-model="loginData.email"
            type="email"
            placeholder="seu@email.com"
            required
          />
        </div>

        <div class="form-group">
          <label for="senha">Senha</label>
          <input
            id="senha"
            v-model="loginData.senha"
            type="password"
            placeholder="Sua senha"
            required
          />
        </div>

        <button type="submit" class="login-btn" :disabled="loading">
          {{ loading ? 'Entrando...' : 'Entrar' }}
        </button>

        <div v-if="error" class="error-message">
          {{ error }}
        </div>
      </form>

      <div class="register-section">
        <p>Não tem uma conta?</p>
        <button @click="mostrarModalRegistro = true" class="register-link">
          Cadastre-se
        </button>
      </div>
    </div>

    <Registro 
      v-if="mostrarModalRegistro"
      @close="mostrarModalRegistro = false"
      @success="registroSucesso"
    />
  </div>
</template>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 1rem;
}

.login-card {
  background: white;
  padding: 2rem;
  border-radius: 12px;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.login-header {
  text-align: center;
  margin-bottom: 2rem;
}

.login-header h1 {
  color: #333;
  margin-bottom: 0.5rem;
  font-size: 1.8rem;
}

.login-header p {
  color: #666;
  margin: 0;
}

.login-form {
  margin-bottom: 1.5rem;
}

.form-group {
  margin-bottom: 1rem;
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
}

.form-group input:focus {
  outline: none;
  border-color: #667eea;
}

.login-btn {
  width: 100%;
  padding: 0.75rem;
  background: #667eea;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 1rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s;
}

.login-btn:hover:not(:disabled) {
  background: #5a6fd8;
}

.login-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

.error-message {
  margin-top: 1rem;
  padding: 0.75rem;
  background: #fee;
  border: 1px solid #fcc;
  border-radius: 6px;
  color: #c33;
  text-align: center;
}

.register-section {
  text-align: center;
  padding-top: 1rem;
  border-top: 1px solid #e1e5e9;
}

.register-section p {
  color: #666;
  margin-bottom: 0.5rem;
}

.register-link {
  background: none;
  border: none;
  color: #667eea;
  text-decoration: underline;
  cursor: pointer;
  font-size: 1rem;
}

.register-link:hover {
  color: #5a6fd8;
}
</style>