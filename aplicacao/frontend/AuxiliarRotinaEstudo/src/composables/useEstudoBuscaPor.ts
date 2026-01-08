import { ref } from "vue";
import { usePagination } from "./usePagination";
import type { EstudoResponseInterface } from "@/types";
import { buscarEstudosPorAtributos } from "@/api/EstudoService";

export function useEstudoBuscaPor() {
    const termoBusca = ref('');
    const emBusca = ref(false);
    
    const {
        items: resultadosBusca,
        page: pageBusca,
        totalPages: totalPagesBusca,
        totalElements: totalElementosBusca,
        loading: loadingBusca,
        atualizarPagina
    } = usePagination<EstudoResponseInterface>(
        (pagina, tamanho) => buscarEstudosPorAtributos(termoBusca.value, pagina, tamanho), 
        6
    );

    const recarregarBuscaAtual = async () => {
        if (!emBusca.value || !termoBusca.value.trim()) return;
        
        try {
            await atualizarPagina(pageBusca.value);
        } catch (error) {
            console.error('Erro ao recarregar busca:', error);
            await atualizarPagina(0);
        }
    };

    const buscar = async (atributo: string, pagina: number = 0) => {
        if (!atributo.trim()) {
            emBusca.value = false;
            termoBusca.value = '';
            return false;
        }

        emBusca.value = true;
        termoBusca.value = atributo;
        
        try {
            await atualizarPagina(pagina);
            return true;
        } catch (error) {
            console.error('Erro na busca:', error);
            emBusca.value = false;
            return false;
        }
    };

    const mudarPaginaBusca = async (pagina: number) => {
        if (!emBusca.value || !termoBusca.value.trim()) return;
        await atualizarPagina(pagina);
    };

    const limparBusca = async () => {
        termoBusca.value = '';
        emBusca.value = false;
        resultadosBusca.value = [];
    };

    return {
        termoBusca,
        emBusca,
        resultadosBusca,
        pageBusca,
        totalPagesBusca,
        totalElementosBusca,
        loadingBusca,
        buscar,
        limparBusca,
        mudarPaginaBusca,
        recarregarBuscaAtual 
    };
}