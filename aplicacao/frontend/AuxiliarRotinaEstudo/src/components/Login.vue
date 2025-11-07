<script setup lang="ts">
import { ref, reactive, watch } from 'vue';
import {useRouter} from 'vue-router';
import Registro from './Registro.vue';
import { useAuth } from '@/composables/useAuth';
import type { LoginUsuarioInterface } from '@/types';

const router = useRouter();
const {login, error, loading, token} = useAuth();

const mostrarModalRegistro = ref(false);
const mostrarSenha = ref(false);

const loginData = reactive<LoginUsuarioInterface>({
  email:'', senha:''
});

const rules = {
  required: (value: string) => !!value || 'Campo obrigatório',
  email: (value: string) => {
    const regex_email = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return regex_email.test(value) || 'E-mail inválido';
  }
};

async function fazerLogin(){
  const sucesso = await login(loginData);
  if(sucesso){
    router.push("/tela-principal");
  }else{
    alert("Falha no login"); // tratar com notificação
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
  <v-app>
    <v-main>
      <v-container fluid class="fill-height login-background">
        <v-row justify="center" align="center">
          <v-col cols="12" sm="8" md="6" lg="4" xl="3">
            <v-card class="login-card" elevation="12" rounded="xl">
              <v-card-item class="login-header">
                <template #prepend>
                  <v-avatar color="primary" size="56">
                    <v-icon icon="mdi-book-education" size="32" />
                  </v-avatar>
                </template>
                
                <v-card-title class="text-h4 font-weight-bold text-primary">
                  Sistema de Estudos
                </v-card-title>
                
                <v-card-subtitle class="text-h6 text-medium-emphasis">
                  Organize seus estudos e revisões
                </v-card-subtitle>
              </v-card-item>

              <v-card-text>
                <v-form @submit.prevent="fazerLogin" class="login-form">
                  <v-text-field
                    v-model="loginData.email"
                    label="E-mail"
                    type="email"
                    placeholder="seu@email.com"
                    :rules="[rules.required, rules.email]"
                    prepend-inner-icon="mdi-email"
                    variant="outlined"
                    color="primary"
                    required
                    class="mb-4"
                  />

                  <v-text-field
                    v-model="loginData.senha"
                    label="Senha"
                    :type="mostrarSenha ? 'text' : 'password'"
                    placeholder="Sua senha"
                    :rules="[rules.required]"
                    prepend-inner-icon="mdi-lock"
                    variant="outlined"
                    color="primary"
                    required
                    class="mb-2"
                    :append-inner-icon="mostrarSenha ? 'mdi-eye-off' : 'mdi-eye'"
                    @click:append-inner="mostrarSenha = !mostrarSenha"
                  />

                  <v-btn
                    type="submit"
                    block
                    size="x-large"
                    color="primary"
                    :loading="loading"
                    :disabled="loading"
                    class="login-btn mt-4"
                  >
                    <template v-if="loading">
                      <v-progress-circular
                        indeterminate
                        size="20"
                        width="2"
                        class="mr-2"
                      />
                      Entrando...
                    </template>
                    <template v-else>
                      <v-icon icon="mdi-login" class="mr-2" />
                      Entrar
                    </template>
                  </v-btn>

                  <v-alert
                    v-if="error"
                    type="error"
                    variant="tonal"
                    class="mt-4"
                    closable
                  >
                    {{ error }}
                  </v-alert>
                </v-form>
              </v-card-text>

              <v-divider class="my-2" />

              <v-card-actions class="register-section">
                <v-container>
                  <v-row align="center" justify="center">
                    <v-col cols="auto">
                      <span class="text-body-1 text-medium-emphasis mr-2">
                        Não tem uma conta?
                      </span>
                    </v-col>
                    <v-col cols="auto">
                      <v-btn
                        @click="mostrarModalRegistro = true"
                        variant="text"
                        color="primary"
                        size="large"
                      >
                        <v-icon icon="mdi-account-plus" class="mr-2" />
                        Cadastre-se
                      </v-btn>
                    </v-col>
                  </v-row>
                </v-container>
              </v-card-actions>
            </v-card>
          </v-col>
        </v-row>
      </v-container>

      <v-dialog
        v-model="mostrarModalRegistro"
        max-width="500"
        persistent
      >
        <Registro 
          @close="mostrarModalRegistro = false"
          @success="registroSucesso"
        />
      </v-dialog>
    </v-main>
  </v-app>
</template>

<style scoped>
.login-background {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  min-height: 100vh;
}

.login-card {
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.login-header {
  text-align: center;
  padding: 2rem 1rem 1rem;
}

:deep(.v-card-item__prepend) {
  align-items: center;
  justify-content: center;
  width: 100%;
  padding-bottom: 1rem;
}

:deep(.v-card-title) {
  width: 100;
  text-align: center;
  line-height: 1.2;
}

:deep(.v-card-subtitle) {
  width: 100;
  text-align: center;
}

.login-form {
  padding: 0 0.5rem;
}

.register-section {
  padding: 1rem;
}

/* Animação suave para o card */
.login-card {
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-2px);
}
</style>