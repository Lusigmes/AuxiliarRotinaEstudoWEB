import type { App } from 'vue';
import { useNotificationRevisaoStore } from '@/stores/notificationRevisaoStore';

export default {
  install(app: App) {
    app.config.globalProperties.$notificacoes = {
      async atualizar() {
        const store = useNotificationRevisaoStore();
        await store.atualizarContadores();
      },
      
      resetar() {
        const store = useNotificationRevisaoStore();
        store.resetarNotificacoes();
      }
    };
  }
};