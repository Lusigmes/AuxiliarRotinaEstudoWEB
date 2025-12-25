import { defineStore } from 'pinia'
import { ref, computed, reactive } from 'vue'
import { 
  listarRevisoes,
  contarRevisoesPendentes,
  contarRevisoesAtrasadas,
  contarRevisoesConcluidas,
  concluirRevisao as apiConcluirRevisao,
  reagendarDataRevisao as apiReagendarDataRevisao
} from '@/api/RevisaoService'
import type { RevisaoResponseInterface } from '@/types'
import { converterStringParaData } from '@/utils/dateUtils'

export const useRevisaoStore = defineStore('revisao', () => {
  const revisoesMap = reactive(new Map<number, RevisaoResponseInterface>());
  
  const contadorPendentes = ref(0);
  const contadorAtrasadas = ref(0);
  const contadorConcluidas = ref(0);
  const loading = ref(false);

  const todasRevisoes = computed(() => Array.from(revisoesMap.values()));


  const revisoesPendentes = computed(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    return todasRevisoes.value
      .filter(revisao => !revisao.concluida)
      .filter(revisao => {
        const dataRevisao = converterStringParaData(revisao.dataRevisao);
        return dataRevisao.getTime() <= hoje.getTime();
      })
      .sort((a, b) => {
        const dataA = converterStringParaData(a.dataRevisao);
        const dataB = converterStringParaData(b.dataRevisao);
        return dataA.getTime() - dataB.getTime();
      })
  });

  const revisoesAtrasadas = computed(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    return todasRevisoes.value
      .filter(revisao => !revisao.concluida)
      .filter(revisao => {
        const dataRevisao = converterStringParaData(revisao.dataRevisao);
        return dataRevisao.getTime() < hoje.getTime();
      })
      .sort((a, b) => {
        const dataA = converterStringParaData(a.dataRevisao);
        const dataB = converterStringParaData(b.dataRevisao);
        return dataA.getTime() - dataB.getTime();
      });
  });

  const revisoesConcluidas = computed(() => {
    return todasRevisoes.value
      .filter(revisao => revisao.concluida)
      .sort((a, b) => {
        const dataA = converterStringParaData(a.dataRevisao);
        const dataB = converterStringParaData(b.dataRevisao);
        return dataB.getTime() - dataA.getTime();
      });
  });


  const atualizarRevisaoNoMap = (revisao: RevisaoResponseInterface) => {
    revisoesMap.set(revisao.id, { ...revisao });
  }

  async function carregarTodasRevisoes() {
    loading.value = true;
    try {
      const revisoes = await listarRevisoes();
      revisoesMap.clear();
      revisoes.forEach(revisao => {
        atualizarRevisaoNoMap(revisao);
      })
      await atualizarContadores();
    } catch (error) {
      console.error('Erro ao carregar revisões:', error);
      throw error;
    } finally {
      loading.value = false;
    }
  }

  async function atualizarContadores() {
    try {
      const [pendentes, atrasadas, concluidas] = await Promise.all([
        contarRevisoesPendentes(),
        contarRevisoesAtrasadas(),
        contarRevisoesConcluidas()
      ]);
      contadorPendentes.value = pendentes;
      contadorAtrasadas.value = atrasadas;
      contadorConcluidas.value = concluidas ;
    } catch (error) {
      console.error('Erro ao atualizar contadores:', error);
    }
  };

  async function concluirRevisao(idRevisao: number) {
    try {
      const revisaoAtualizada = await apiConcluirRevisao(idRevisao);
      
      atualizarRevisaoNoMap(revisaoAtualizada);
      
      await atualizarContadores();
      
      return revisaoAtualizada;
    } catch (error) {
      console.error('Erro ao concluir revisão:', error);
      throw error;
    }
  };

  async function reagendarDataRevisao(idRevisao: number, novaData: string) {
    try {
      const revisaoAtualizada = await apiReagendarDataRevisao(idRevisao, novaData);
      
      atualizarRevisaoNoMap(revisaoAtualizada);
      
      await atualizarContadores();
      
      return revisaoAtualizada;
    } catch (error) {
      console.error('Erro ao reagendar revisão:', error);
      throw error;
    }
  };

  async function inicializar() {
    await carregarTodasRevisoes();
  };

  function limparStore() {
    revisoesMap.clear();
    contadorPendentes.value = 0;
    contadorAtrasadas.value = 0;
    loading.value = false;
  };

  return {
    todasRevisoes,
    contadorPendentes,
    contadorAtrasadas,
    contadorConcluidas, 
    loading,
    
    revisoesPendentes,
    revisoesAtrasadas,  
    revisoesConcluidas,
    
    carregarTodasRevisoes,
    concluirRevisao,
    reagendarDataRevisao,
    atualizarContadores,
    inicializar,
    limparStore,
    
    atualizarRevisaoNoMap
  }
});