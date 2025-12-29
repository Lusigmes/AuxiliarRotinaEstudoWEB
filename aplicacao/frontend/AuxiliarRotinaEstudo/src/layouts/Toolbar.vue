<script setup lang="ts">
import { computed, watchEffect, onMounted, onUnmounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useAuth } from '@/composables/useAuth';
import { useNotification } from '@/composables/useNotification';
import { useTheme } from 'vuetify';
import { useNotificationRevisaoStore } from '@/stores/notificationRevisaoStore';
import NotificationRevisao from '@/components/NotificationRevisao.vue';
import TempoExpirarToken from '@/components/TempoExpirarToken.vue'; 

const router = useRouter();
const route = useRoute();
const { logout: authLogout, usuario } = useAuth();
const { showNotification } = useNotification();
const theme = useTheme();
const notificationRevisaoStore = useNotificationRevisaoStore();

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
  showNotification('Logout realizado com sucesso!', 'info');
  setTimeout(() => {
    router.push('/');
  }, 500);
}

function toggleTheme() {
  const isDark = theme.global.current.value.dark;
  theme.global.name.value = isDark ? 'light' : 'dark';
  showNotification(
    `Tema alterado para ${isDark ? 'claro' : 'escuro'}`,
    'info'
  );
}

onMounted(() => {
  if (usuarioAutenticado.value) {
    try {
      const cleanup = notificationRevisaoStore.iniciarAtualizacaoAutomatica();
      
      const stopWatch = watchEffect(() => {
        if (usuarioAutenticado.value && route.path !== '/login') {
          notificationRevisaoStore.atualizarContadores();
        }
      });
      
      onUnmounted(() => {
        cleanup();
        stopWatch();
      });
    } catch (error) {
      console.error('Erro ao iniciar notificações:', error);
      showNotification('Erro ao carregar notificações de revisão', 'warning');
    }
  }
});

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
      <div class="d-flex align-center toolbar-icons">
        <TempoExpirarToken v-if="usuarioAutenticado" />
        
        <div class="notification-container mr-2">
          <NotificationRevisao v-if="usuarioAutenticado" />
        </div>
        
        <!-- tema -->
        <v-btn 
          icon 
          @click="toggleTheme" 
          class="mr-2 theme-btn"
        >
          <v-icon>mdi-theme-light-dark</v-icon>
        </v-btn>
        
        <!-- menu -->
        <div class="user-menu-container">
          <v-menu location="bottom end" :offset="[0, 8]">
            <template v-slot:activator="{ props }">
              <v-btn 
                icon 
                v-bind="props"
                class="user-btn"
              >
                <v-avatar color="secondary" size="36">
                  <span class="text-caption font-weight-bold text-white">
                    {{ usuarioIniciais }}
                  </span>
                </v-avatar>
              </v-btn>
            </template>
            
            <v-list min-width="200">
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
        </div>
      </div>
    </template>
  </v-app-bar>
</template>

<style scoped>
.toolbar-icons {
  height: 100%;
  display: flex;
  align-items: center;
  position: relative;
}

.notification-container {
  height: 100%;
  display: flex;
  align-items: center;
  position: relative;
  margin-right: 12px;
}

.notification-container :deep(.notificacao-wrapper) {
  position: relative;
  height: 100%;
  display: flex;
  align-items: center;
}

.user-menu-container {
  height: 100%;
  display: flex;
  align-items: center;
}

.theme-btn,
.user-btn {
  height: 48px !important;
  width: 48px !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

:deep(.v-toolbar__content) {
  min-height: 64px !important;
  height: 64px !important;
  position: relative;
}

:deep(.v-btn) {
  min-width: unset !important;
}

:deep(.v-overlay__content) {
  z-index: 9999 !important;
}
</style>