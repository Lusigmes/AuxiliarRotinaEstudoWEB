import { defineStore } from "pinia";
import { ref, computed } from "vue";
import { 
  listarRevisoes, 
  contarRevisoesPendentes, 
  contarRevisoesAtrasadas,
  concluirRevisao,
  reagendarDataRevisao
} from "@/api/RevisaoService";
import type { RevisaoResponseInterface } from "@/types";
import { converterStringParaData } from "@/utils/dateUtils";

export const useRevisaoStore = defineStore('revisao', () => {
  const revisoes = ref<RevisaoResponseInterface[]>([]);
  const contadorPendentes = ref(0);
  const contadorAtrasadas = ref(0);
  const loading = ref(false);
  
  const revisoesPendentes = computed(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    return revisoes.value
      .filter(revisao => !revisao.concluida)
      .filter(revisao => {
        const dataRevisao = converterStringParaData(revisao.dataRevisao);
        return dataRevisao <= hoje;
      })
      .sort((a, b) => {
        const dataA = converterStringParaData(a.dataRevisao);
        const dataB = converterStringParaData(b.dataRevisao);
        return dataA.getTime() - dataB.getTime();
      });
  });
  
  const revisoesAtrasadas = computed(() => {
    const hoje = new Date();
    hoje.setHours(0, 0, 0, 0);
    
    return revisoes.value
      .filter(revisao => !revisao.concluida)
      .filter(revisao => {
        const dataRevisao = converterStringParaData(revisao.dataRevisao);
        return dataRevisao < hoje;
      })
      .sort((a, b) => {
        const dataA = converterStringParaData(a.dataRevisao);
        const dataB = converterStringParaData(b.dataRevisao);
        return dataA.getTime() - dataB.getTime();
      });
  });
  
  async function carregarTodasRevisoes() {
    loading.value = true;
    try {
      revisoes.value = await listarRevisoes();
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
      [contadorPendentes.value, contadorAtrasadas.value] = await Promise.all([
        contarRevisoesPendentes(),
        contarRevisoesAtrasadas()
      ]);
    } catch (error) {
      console.error('Erro ao atualizar contadores:', error);
    }
  }
  
  async function concluir(idRevisao: number) {
    try {
      const revisaoAtualizada = await concluirRevisao(idRevisao);
      
      const index = revisoes.value.findIndex(r => r.id === idRevisao);
      if (index !== -1) {
        revisoes.value[index] = revisaoAtualizada;
      }
      
      await atualizarContadores();
      
      return revisaoAtualizada;
    } catch (error) {
      console.error('Erro ao concluir revisão:', error);
      throw error;
    }
  }
  
  async function reagendar(idRevisao: number, novaData: string) {
    try {
      const revisaoAtualizada = await reagendarDataRevisao(idRevisao, novaData);
      
      const index = revisoes.value.findIndex(r => r.id === idRevisao);
      if (index !== -1) {
        revisoes.value[index] = revisaoAtualizada;
      } else {
        revisoes.value.push(revisaoAtualizada);
      }
      
      await atualizarContadores();
      
      return revisaoAtualizada;
    } catch (error) {
      console.error('Erro ao reagendar revisão:', error);
      throw error;
    }
  }
  
  async function inicializar() {
    await carregarTodasRevisoes();
  }
  
  return {
    revisoes,
    contadorPendentes,
    contadorAtrasadas,
    loading,
    
    revisoesPendentes,
    revisoesAtrasadas,
    
    carregarTodasRevisoes,
    atualizarContadores,
    concluir,
    reagendar,
    inicializar
  };
});