import { ref, computed } from 'vue';
import { defineStore } from 'pinia';
import { contarRevisoesPendentes } from '@/api/RevisaoService';

export const useNotificationRevisaoStore = defineStore('notificationRevisao', () => {
  const revisoesPendentesCount = ref(0);
  const loading = ref(false);
  const ultimaAtualizacao = ref<Date | null>(null);
  
  const totalNotificacoes = computed(() => revisoesPendentesCount.value);
  
  
  const hasNotificacoes = computed(() => 
    revisoesPendentesCount.value > 0  
  );
  
  async function atualizarContadores() {
    try {
      loading.value = true;
      
      const pendentes = await contarRevisoesPendentes();
      
      revisoesPendentesCount.value = pendentes;
      ultimaAtualizacao.value = new Date();
      
    } catch (error) {
      console.error('Erro ao atualizar contadores de notificação:', error);
    } finally {
      loading.value = false;
    }
  }

  function resetarNotificacoes() {
    revisoesPendentesCount.value = 0;
    ultimaAtualizacao.value = null;
  }
  
  function iniciarAtualizacaoAutomatica() {
    atualizarContadores();
    
    const intervalo = setInterval(() => {
      atualizarContadores();
    }, 5 * 60 * 1000);
    
    return () => clearInterval(intervalo);
  }
  
  return {
    revisoesPendentesCount,
    totalNotificacoes,
    hasNotificacoes,
    loading,
    ultimaAtualizacao,
    atualizarContadores,
    resetarNotificacoes,
    iniciarAtualizacaoAutomatica
  };
});