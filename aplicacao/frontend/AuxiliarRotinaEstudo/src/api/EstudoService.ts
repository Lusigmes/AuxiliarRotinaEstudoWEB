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

export const listarEstudosUsuarioPaginado = async (
    page: number = 0, 
    size: number = 10
): Promise<any> => {
    try {
        const { data } = await httpConnect.get("/estudos/page", {
            params: {
                page,
                size,
                sort: "id,desc"
            }
        });
        
        return data;
    } catch (error) {
        console.error('Erro na API paginada:', error);
        throw error;
    }
}


export const nomeDisciplinaDoEstudo = async (idEstudo: number): Promise<string> => {
    const response = await httpConnect.get(`/estudos/${idEstudo}/disciplina`);
    return response.data;
}



export const buscarEstudosPorAtributos= async (
    termo: string,
    page: number = 0,
    size: number = 10
): Promise<any> => {
    try {
        const { data } = await httpConnect.get("/estudos/buscar", {
            params: {
                termo,
                page,
                size,
                sort: "diaDoEstudo,desc"
            }
        });
        
        return data;
    } catch (error) {
        console.error('Erro na busca de estudos:', error);
        throw error;
    }
}