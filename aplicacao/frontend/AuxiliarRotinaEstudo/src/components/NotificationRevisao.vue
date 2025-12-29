<template>
  <div class="notificacao-wrapper">
    <v-badge
      v-if="hasNotificacoes && !menuAberto"
      :content="totalNotificacoes"
      color="warning"
      dot
      location="top end"
      offset-x="8"
      offset-y="8"
      :model-value="true"
    >
      <v-btn
        icon
        :loading="loading"
        @click.stop="toggleMenu"
        class="notification-btn"
        variant="text"
        :class="{ 
          'has-notifications': hasNotificacoes,
          'menu-open': menuAberto 
        }"
      >
        <v-icon 
          :color="menuAberto ? 'white' : 'white'"
          size="24"
        >
          {{ menuAberto ? 'mdi-bell-ring' : 'mdi-bell' }}
        </v-icon>
      </v-btn>
    </v-badge>
    
    <v-btn
      v-else
      icon
      :loading="loading"
      @click.stop="toggleMenu"
      class="notification-btn"
      variant="text"
      :class="{ 
        'has-notifications': hasNotificacoes,
        'menu-open': menuAberto 
      }"
    >
      <v-icon 
        :color="menuAberto ? 'white' : 'white'"
        size="24"
      >
        {{ menuAberto ? 'mdi-bell-ring' : (hasNotificacoes ? 'mdi-bell' : 'mdi-bell') }}
      </v-icon>
    </v-btn>
    
    <v-menu
      v-model="menuAberto"
      :close-on-content-click="true"
      location="bottom end"
      :offset="[0, 8]"
      max-width="400"
      min-width="350"
      class="notification-menu"
      transition="slide-y-transition"
    >
      <template #default="{ isActive }">
        <v-card
          :class="{ 'notification-card': true, 'active': isActive }"
          elevation="4"
        >
          <v-card-title class="d-flex justify-space-between align-center pa-4">
            <div class="d-flex align-center">
              <v-icon color="primary" class="mr-2">mdi-bell</v-icon>
              <span class="text-h6">Notificações</span>
              <v-chip 
                v-if="totalNotificacoes > 0" 
                color="primary" 
                size="small" 
                class="ml-2"
              >
                {{ totalNotificacoes }}
              </v-chip>
            </div>
            <div class="d-flex align-center gap-2">
              <v-btn
                icon
                size="small"
                @click="atualizarNotificacoes"
                :loading="loading"
                variant="text"
                title="Atualizar"
              >
                <v-icon>mdi-refresh</v-icon>
              </v-btn>
            </div>
          </v-card-title>
          
          <v-divider />
          
          <v-card-text class="pa-0">
            <template v-if="hasNotificacoes">
              <div v-if="revisoesPendentesCount > 0" class="notificacao-item pendente">
                <div class="d-flex align-center pa-3">
                  <v-icon color="warning" class="mr-3">mdi-calendar-clock</v-icon>
                  <div>
                    <div class="text-body-1 font-weight-bold">
                      {{ revisoesPendentesCount }} revisão(ões) para hoje
                    </div>
                    <div class="text-caption">
                      Revisões programadas para o dia atual
                    </div>
                  </div>
                </div>
              </div>          
            </template>
            
            <template v-else>
              <div class="text-center py-8">
                <v-icon size="48" color="success" class="mb-3">mdi-check-circle</v-icon>
                <div class="text-body-1">Tudo em dia!</div>
                <div class="text-caption text-grey mt-1">
                  Nenhuma revisão pendente
                </div>
              </div>
            </template>
          </v-card-text>
          
          <v-card-actions class="pa-3">
            <v-btn
              block
              color="primary"
              variant="flat"
              @click="irParaRevisoes"
              :disabled="!hasNotificacoes"
            >
              Ir para Seção de Revisões
            </v-btn>
          </v-card-actions>
        </v-card>
      </template>
    </v-menu>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useNotificationRevisaoStore } from '@/stores/notificationRevisaoStore';
import { storeToRefs } from 'pinia';

const router = useRouter();
const notificationRevisaoStore = useNotificationRevisaoStore();

const menuAberto = ref(false);

const {
  revisoesPendentesCount,
  totalNotificacoes,
  hasNotificacoes,
  loading
} = storeToRefs(notificationRevisaoStore);


function toggleMenu() {
  menuAberto.value = !menuAberto.value;
  if (menuAberto.value) {
    atualizarNotificacoes();
  }
}

async function atualizarNotificacoes() {
  await notificationRevisaoStore.atualizarContadores();
}

function irParaRevisoes() {
  menuAberto.value = false;
  router.push('/tela-principal/revisao');
}

onMounted(async () => {
  await atualizarNotificacoes();
});

</script>

<style scoped>
.notificacao-wrapper {
  position: relative;
  display: inline-block;
}

.notification-btn {
  position: relative;
  transition: all 0.3s ease;
}

.notification-btn.has-notifications:not(.menu-open) {
  animation: pulse 2s infinite;
}

.notification-btn.menu-open {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
  border-radius: 50%;
}

.notification-btn:active {
  transform: scale(0.95);
}

@keyframes pulse {
  0% { 
    transform: scale(1); 
    box-shadow: 0 0 0 0 rgba(255, 193, 7, 0.7);
  }
  70% { 
    transform: scale(1.05); 
    box-shadow: 0 0 0 10px rgba(255, 193, 7, 0);
  }
  100% { 
    transform: scale(1); 
    box-shadow: 0 0 0 0 rgba(255, 193, 7, 0);
  }
}

.notificacao-item {
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  transition: background-color 0.2s;
}

.notificacao-item:hover {
  background-color: rgba(0, 0, 0, 0.04);
}

.notificacao-item.pendente {
  background-color: rgba(255, 193, 7, 0.05);
  border-left: 4px solid #ffc107;
}

.gap-2 {
  gap: 8px;
}

.notification-menu {
  position: absolute !important;
}

.notification-menu :deep(.v-overlay__content) {
  position: fixed !important;
  top: 64px !important; 
  right: 80px !important; 
  margin: 0 !important;
  transform: none !important;
  animation: slideInRight 0.2s ease-out;
}

.notification-menu :deep(.v-overlay__content)::before {
  content: '';
  position: absolute;
  top: -8px;
  right: 20px;
  width: 0;
  height: 0;
  border-left: 8px solid transparent;
  border-right: 8px solid transparent;
  border-bottom: 8px solid white;
  filter: drop-shadow(0 -2px 2px rgba(0, 0, 0, 0.1));
  z-index: 1;
}

.notification-card {
  border-radius: 12px !important;
  overflow: hidden;
  position: relative;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.notification-card.active {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15) !important;
}

@keyframes slideInRight {
  from {
    opacity: 0;
    transform: translateX(20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.notification-menu :deep(.v-overlay__scrim) {
  background-color: transparent !important;
  pointer-events: none !important;
}

.notification-menu :deep(.v-overlay__content > *) {
  pointer-events: auto !important;
}

:deep(.v-badge__badge) {
  font-size: 10px !important;
  font-weight: bold !important;
  min-width: 10px !important;
  height: 10px !important;
  padding: 0 4px !important;
  border-radius: 10px;
}
</style>