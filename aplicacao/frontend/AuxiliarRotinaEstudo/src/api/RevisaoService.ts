import httpConnect from "./connect/connect";
import type { RevisaoResponseInterface } from "@/types";

export const listarRevisoesPendentes = async (): Promise<RevisaoResponseInterface[]> => {
    const response = await httpConnect.get('revisoes/pendentes');
    return response.data;
}

export const listarRevisoesAtrasadas = async (): Promise<RevisaoResponseInterface[]> => {
    const response = await httpConnect.get('revisoes/atrasadas');
    return response.data;
}

export const listarRevisoes = async (): Promise<RevisaoResponseInterface[]> => {
    const response = await httpConnect.get('revisoes');
    return response.data;
}

export const concluirRevisao = async (idRevisao: number): Promise<RevisaoResponseInterface> => {
    const response = await httpConnect.put(`revisoes/${idRevisao}/concluir`);
    return response.data;
}

export const reagendarDataRevisao = async (idRevisao: number, data: string): Promise<RevisaoResponseInterface> => {
    const response = await httpConnect.put(`revisoes/${idRevisao}/nova_data`, {dataRevisao: data});
    return response.data;
}

export const carregarRevisoesPendentesPaginado = async (page: number = 0, size: number = 6): Promise<any> => {
    const response = await httpConnect.get('revisoes/pendentes/page', {
        params: { page, size, sort: 'dataRevisao,DESC' }
    });
    return response.data;
}

export const carregarRevisoesAtrasadasPaginado = async (page: number = 0, size: number = 6): Promise<any> => {
    const response = await httpConnect.get('revisoes/atrasadas/page', {
        params: { page, size, sort: 'dataRevisao,DESC' }
    });
    return response.data;
}  

export const contarRevisoesPendentes = async (): Promise<number> => {
    try {
        const response = await httpConnect.get('revisoes/pendentes/contar');
        return response.data;
    } catch (error) {
        console.error('Erro ao contar revisões pendentes:', error);
        return 0;
    }
}

export const contarRevisoesAtrasadas = async (): Promise<number> => {
    try {
        const response = await httpConnect.get('revisoes/atrasadas/contar');
        return response.data;
    } catch (error) {
        console.error('Erro ao contar revisões atrasadas:', error);
        return 0;
    }
}

export const listarRevisoesConcluidas = async (): Promise<RevisaoResponseInterface[]> => {
    const response = await httpConnect.get('revisoes/concluidas');
    return response.data;
}

export const carregarRevisoesConcluidasPaginado = async (page: number = 0, size: number = 6): Promise<any> => {
    const response = await httpConnect.get('revisoes/concluidas/page', {
        params: { page, size, sort: 'dataRevisao,DESC' }
    });
    return response.data;
}

export const contarRevisoesConcluidas = async (): Promise<number> => {
    try {
        const response = await httpConnect.get('revisoes/concluidas/contar');
        return response.data;
    } catch (error) {
        console.error('Erro ao contar revisões concluídas:', error);
        return 0;
    }
}