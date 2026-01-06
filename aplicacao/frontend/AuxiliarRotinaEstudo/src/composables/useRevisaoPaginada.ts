import { ref } from 'vue'
import { useRevisaoStore } from '@/stores/revisaoStore'
import type { RevisaoResponseInterface } from '@/types'
import { converterStringParaData } from '@/utils/dateUtils'  

export function useRevisaoPaginada(
  funcaoDeBuscaPaginada: (page: number, size: number) => Promise<any>,
  tipo: 'pendentes' | 'atrasadas' | 'concluidas',
  initialSize = 6
) {
  const revisaoStore = useRevisaoStore()
  
  const items = ref<RevisaoResponseInterface[]>([])
  const page = ref(0)
  const size = ref(initialSize)
  const totalPages = ref(0)
  const totalElements = ref(0)
  const loading = ref(false)

  const atualizarPagina = async (pagina = page.value) => {
    loading.value = true
    try {
      const response = await funcaoDeBuscaPaginada(pagina, size.value)
      
      items.value = response.content || []
      totalPages.value = response.totalPages || 0
      totalElements.value = response.totalElements || 0
      page.value = pagina
      
      if (response.content) {
        response.content.forEach((revisao: RevisaoResponseInterface) => {
          revisaoStore.atualizarRevisaoNoMap(revisao)
        })
      }
      
    } catch (error) {
      console.error('Erro na paginação:', error)
      items.value = []
      totalPages.value = 0
      totalElements.value = 0
    } finally {
      loading.value = false
    }
  }

const verificarEAjustarPagina = async () => {
  if (items.value.length === 0 && page.value > 0) {
    const paginaAnterior = Math.max(0, page.value - 1);
    await atualizarPagina(paginaAnterior);
  }
}

const sincronizarComStore = () => {
  const idsDaPagina = new Set(items.value.map(r => r.id))
  
  const novosItems = items.value.map(revisao => {
    const revisaoAtualizada = revisaoStore.todasRevisoes.find(r => r.id === revisao.id)
    return revisaoAtualizada || revisao
  })
  
  const hoje = new Date()
  hoje.setHours(0, 0, 0, 0)
      
  const itemsFiltrados = novosItems.filter(revisao => {
    if (tipo === 'atrasadas') {
      const dataRevisao = converterStringParaData(revisao.dataRevisao)
      return dataRevisao.getTime() < hoje.getTime() && !revisao.concluida
    } else if (tipo === 'pendentes') {
      return !revisao.concluida
    } else {
      return revisao.concluida
    }
  })
  
  if (JSON.stringify(items.value) !== JSON.stringify(itemsFiltrados)) {
    items.value = itemsFiltrados
    
    if (items.value.length === 0) {
      setTimeout(() => {
        verificarEAjustarPagina();
      }, 100);
    }
  }
}

  const recarregarPaginaAtual = async () => {
    await atualizarPagina(page.value)
  }

  return {
    items,
    page,
    size,
    totalPages,
    totalElements,
    loading,
    atualizarPagina,
    revisaoStore,
    sincronizarComStore,
    recarregarPaginaAtual,
    verificarEAjustarPagina 
  }
}