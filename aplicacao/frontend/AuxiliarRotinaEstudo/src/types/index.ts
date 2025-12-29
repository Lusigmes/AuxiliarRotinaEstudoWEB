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
export interface LoginTokenResponseInterface {
    token: string;
    refreshToken?: string;
    expiresIn: number;
}

export interface AuthResponseInterface {
    token: string;
    refreshToken: string;
    message: string;

}export interface RefreshTokenRequest {
    refreshToken: string;
}
    
export interface ItemCronogramaInterface{
    diaSemana:DiasSemana;
    nomeDisciplina: string;
    ordem?: number;
}

export interface ItemCronogramaResponseInterface{
    id: number;
    diaSemana:DiasSemana;
    nomeDisciplina: string;
    ordem?: number;
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

export interface RelatorioResumoInterface {
  periodoInicio: string;
  periodoFim: string;  
  totalEstudos: number;
  totalRevisoesConcluidas: number;
  totalRevisoesPendentes: number;
  totalRevisoesAtrasadas: number;
  mediaTempoDiario: number;
  disciplinaMaisEstudada: string;
  tempoTotal: number;
}

export interface EstatisticaDisciplinaInterface {
  disciplina: string;
  totalEstudos: number;
  totalTempo: number;
  mediaTempoEstudo: number;
  totalRevisoesConcluidas: number;
}

export interface EstudoDiarioInterface {
  data: string; 
  quantidadeEstudos: number;
  tempoTotal: number;
}

export interface RevisaoStatusInterface {
  status: string;
  quantidade: number;
}

export interface DashboardInterface {
  resumo: RelatorioResumoInterface;
  statusRevisoes: RevisaoStatusInterface[];
  topDisciplinas: EstatisticaDisciplinaInterface[];
}