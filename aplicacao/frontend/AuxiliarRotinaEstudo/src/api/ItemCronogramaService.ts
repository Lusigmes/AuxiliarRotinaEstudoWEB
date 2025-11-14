import type { ItemCronogramaInterface } from "@/types";
import httpConnect from "./connect/connect";

export const deletarItem = async (idItem: number): Promise<void> => {
    await httpConnect.delete(`/itens/${idItem}`);
};

export const atualizarItem = async ( 
    idItem: number, 
    dados: Partial<ItemCronogramaInterface>): 
Promise<ItemCronogramaInterface> => {
    const itensAatualizados = await httpConnect.put(`/itens/${idItem}`, dados);
    return itensAatualizados.data;
};