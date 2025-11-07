<template>
  <v-app>
    <!-- toolbar -->
    <v-app-bar color="primary" elevation="4" rounded="0">
      <template #prepend>
        <v-avatar color="white" size="40">
          <v-icon icon="mdi-book-education" color="primary" />
        </v-avatar>
      </template>

      <v-app-bar-title class="text-h5 font-weight-bold">
        Sistema de Estudos
      </v-app-bar-title>

      <template #append>
        <v-btn icon @click="toggleTheme" class="mr-2">
          <v-icon>mdi-theme-light-dark</v-icon>
        </v-btn>
        
        <v-menu location="bottom end">
          <template v-slot:activator="{ props }">
            <v-btn icon v-bind="props">
              <v-avatar color="secondary" size="36">
                <span class="text-caption font-weight-bold">
                  {{ usuarioIniciais }}
                </span>
              </v-avatar>
            </v-btn>
          </template>
          
          <v-list>
            <v-list-item>
              <template #prepend>
                <v-avatar color="secondary" size="36">
                  <span class="text-caption font-weight-bold">
                    {{ usuarioIniciais }}
                  </span>
                </v-avatar>
              </template>
              <v-list-item-title>{{ usuarioNome }}</v-list-item-title>
              <v-list-item-subtitle>{{ usuarioEmail }}</v-list-item-subtitle>
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

    <!-- exibe componentes -->
    <v-main>
      <router-view></router-view>
    </v-main>

    <!-- add estudo btn flutuante -->
    <v-fab-transition>
      <v-btn
        v-if="$route.name === 'Dashboard'"
        color="primary"
        size="large"
        icon
        fab
        fixed
        bottom
        right
        @click="navigateTo('registro-estudo')"
      >
        <v-icon size="28">mdi-plus</v-icon>
      </v-btn>
    </v-fab-transition>
  </v-app>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { useTheme } from 'vuetify';

const router = useRouter();
const { logout: authLogout, usuario } = useAuth();
const theme = useTheme();

const usuarioNome = computed(() => usuario.value?.nome || 'Usuário');
const usuarioEmail = computed(() => usuario.value?.email || '');
const usuarioIniciais = computed(() => {
  const nome = usuario.value?.nome || 'U';
  return nome.split(' ').map(n => n[0]).join('').toUpperCase().substring(0, 2);
});

function navigateTo(destino: string) {
  router.push(`/tela-principal/${destino}`);
}

function logout() {
  authLogout();
  router.push('/');
}

function toggleTheme() {
  theme.global.name.value = theme.global.current.value.dark ? 'light' : 'dark';
}
</script>