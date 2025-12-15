<script setup lang="ts">
import { computed, watchEffect } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { useTheme } from 'vuetify';

const router = useRouter();
const { logout: authLogout, usuario } = useAuth();
const theme = useTheme();

const usuarioAutenticado = computed(() => {
  return usuario.value !== null;
});

const usuarioNome = computed(() => usuario.value?.nome || 'Usuário');
const usuarioEmail = computed(() => usuario.value?.email || '');

const usuarioIniciais = computed(() => {
  const nome = usuario.value?.nome || 'U';
  return nome.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
});

function logout() {
  authLogout();
  router.push('/');
}

function toggleTheme() {
  theme.global.name.value = theme.global.current.value.dark ? 'light' : 'dark';
}
watchEffect(() => {
  if (!usuario.value) {
    router.push('/');
  }
});
</script>
<template>
  <v-app-bar color="primary" elevation="4" rounded="0">
 
    <template #prepend>

      <v-app-bar-title class="text-h5 font-weight-bold ml-8">
        CronoStudy - Sistema de Gerência de Estudos
      </v-app-bar-title>
    </template>


    <template #append>
    
      <v-btn icon @click="toggleTheme" class="mr-2">
        <v-icon>mdi-theme-light-dark</v-icon>
      </v-btn>
      
      <!-- Menu do Usuário -->
      <v-menu location="bottom end">
        <template v-slot:activator="{ props }">
          <v-btn icon v-bind="props">
            <v-avatar color="secondary" size="36">
              <span class="text-caption font-weight-bold text-white">
                {{ usuarioIniciais }}
              </span>
            </v-avatar>
          </v-btn>
        </template>
        
        <v-list>
          <v-list-item>
            <template #prepend>
              <v-avatar color="secondary" size="36">
                <span class="text-caption font-weight-bold text-white">
                  {{ usuarioIniciais }}
                </span>
              </v-avatar>
            </template>
            <v-list-item-title class="font-weight-bold">
              {{ usuarioNome }}
            </v-list-item-title>
            <v-list-item-subtitle>
              {{ usuarioEmail }}
            </v-list-item-subtitle>
          </v-list-item>
          
          <v-divider class="my-2" />

          <v-list-item @click="logout" color="error">
            <template #prepend>
              <v-icon icon="mdi-logout" />
            </template>
            <v-list-item-title>Sair</v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>
    </template>
  </v-app-bar>
</template>

<style scoped>
:deep(.v-tab) {
  font-weight: 600;
}

:deep(.v-app-bar__prepend) {
  margin-inline-end: 16px;
}
</style>