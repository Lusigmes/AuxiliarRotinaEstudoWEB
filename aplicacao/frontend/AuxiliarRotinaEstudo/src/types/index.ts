import type { DiasSemana } from "./enums";

export type Estado = 'semCronograma' | 'adicionandoItens' | 'visualizando';
export type ModoAdicao = 'criar' | 'adicionar';

export interface RegistroUsuarioInterface{
    nome: string;
    email: string;
    senha: string;
    // tipoUsuario: "ALUNO" | "PROFESSOR";
    // roleUsuario:string;

}
export interface UsuarioResponseInterface{
    id:number;
    nome: string;
    email: string;
    // roleUsuario:string;
}

export interface LoginUsuarioInterface{
    email: string;
    senha: string;
}
export interface LoginTokenResponseInterface{
    token:string;
    expiresIn: number
}
    
export interface ItemCronogramaInterface{
    diaSemana:DiasSemana;
    nomeDisciplina: string;
}

export interface ItemCronogramaResponseInterface{
    id: number;
    diaSemana:DiasSemana;
    nomeDisciplina: string;
    // ordem?: number;
}
    
export interface CronogramaInterface{ // usa para criar cronograma e adicionar itens
    itensDoDia: ItemCronogramaInterface[];
}


export interface CronogramaResponseInterface{
    id:number;
    idUsuario: number;
    itensDoDia: ItemCronogramaResponseInterface[];
}

export interface EstudoInterface{
    nomeDisciplina: string;
    tema: string;
    tempoDeEstudo: number;
    diaDoEstudo: string;
}

export interface EstudoResponseInterface{
    id:number;
    nomeDisciplina: string;
    tema: string;
    tempoDeEstudo: number;
    diaDoEstudo: string;
}


export interface RevisaoInterface{
    dataRevisao: string;
    concluida: boolean;
    idEstudo: number;
}

export interface RevisaoResponseInterface{
    id: number;
    dataRevisao: string;
    concluida: boolean;
    idEstudo: number;
}   


    // public record RevisaoUpdateDTO(
    // @JsonFormat(pattern = "dd/MM/yyyy")
    // LocalDate dataRevisao

