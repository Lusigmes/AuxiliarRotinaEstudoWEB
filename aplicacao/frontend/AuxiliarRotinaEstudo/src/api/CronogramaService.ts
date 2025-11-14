import httpConnect from "./connect/connect";
import type { CronogramaInterface,ItemCronogramaInterface,CronogramaResponseInterface, ItemCronogramaResponseInterface } from "@/types";

export const criarCronograma = async (dados: CronogramaInterface
) : Promise<CronogramaResponseInterface> => {
    const response = await httpConnect.post('/cronogramas', dados);
    return response.data;
};


export const adicionarItemCronograma = async (
    idCronograma: number,
    itens: {itensDoDia: ItemCronogramaInterface[]} 
): Promise<CronogramaResponseInterface> => {
    const response = await httpConnect.post(`cronogramas/${idCronograma}/adicionarItens`, itens);
    return response.data;
};


export const atualizarCronograma = async (
    idCronograma: number,
    dados: CronogramaInterface
) : Promise<CronogramaResponseInterface> => {
    const response = await httpConnect.put(`/cronogramas/${idCronograma}`, dados);
    return response.data;
};

export const verificarCronogramaDoUsuarioExiste = async(): Promise<boolean> => {
    const response = await httpConnect.get(`/cronogramas/existe`);
    return response.data;
};

export const buscarCronogramaDoUsuario = async(): Promise<CronogramaResponseInterface> => {
    const response = await httpConnect.get(`/cronogramas`);
    return response.data;
};

export const deletarCronograma = async (idCronograma: number): Promise<void> => {
    await httpConnect.delete(`/cronogramas/${idCronograma}`);
};