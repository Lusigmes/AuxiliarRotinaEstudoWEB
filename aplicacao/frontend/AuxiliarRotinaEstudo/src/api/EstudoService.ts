import httpConnect from "./connect/connect";
import type { EstudoInterface, EstudoResponseInterface } from "@/types";

export const criarEstudo = async (dados: EstudoInterface
) : Promise<EstudoResponseInterface> => {
    const response = await httpConnect.post('/estudos', dados);
    return response.data;
};


export const atualizarEstudo = async (
    idEstudo: number,
    dados: EstudoInterface
) : Promise<EstudoResponseInterface> => {
    const response = await httpConnect.put(`/estudos/${idEstudo}`, dados);
    return response.data;
};

export const deletarEstudo = async (idEstudo: number): Promise<void> => {
    await httpConnect.delete(`/estudos/${idEstudo}`);
};

export const listarEstudosUsuario = async (
    
) : Promise<EstudoResponseInterface[]> => {
    const response = await httpConnect.get('/estudos');
    return response.data;
}