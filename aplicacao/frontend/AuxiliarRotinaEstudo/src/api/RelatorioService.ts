import httpConnect from "./connect/connect";
import type { 
  RelatorioResumoInterface, 
  EstatisticaDisciplinaInterface,
  EstudoDiarioInterface,
  RevisaoStatusInterface,
  DashboardInterface 
} from "@/types";

export const obterResumo = async (inicio?: string, fim?: string
): Promise<RelatorioResumoInterface> => {
  const params: any = {};
  if (inicio) params.inicio = inicio;
  if (fim) params.fim = fim;
  
  const response = await httpConnect.get('/relatorios/resumo', { params });
  return response.data;
};

export const obterEstatisticasDisciplinas = async (inicio?: string, fim?: string
): Promise<EstatisticaDisciplinaInterface[]> => {
  const params: any = {};
  if (inicio) params.inicio = inicio;
  if (fim) params.fim = fim;
  
  const response = await httpConnect.get('/relatorios/disciplinas', { params });
  return response.data;
};

export const obterEstudosDiario = async (inicio?: string, fim?: string
): Promise<EstudoDiarioInterface[]> => {
  const params: any = {};
  if (inicio) params.inicio = inicio;
  if (fim) params.fim = fim;
  
  const response = await httpConnect.get('/relatorios/estudosDiario', { params });
  return response.data;
};

export const obterStatusRevisoes = async (): Promise<RevisaoStatusInterface[]> => {
  const response = await httpConnect.get('/relatorios/statusRevisoes');
  return response.data;
};

export const obterTempoPorDisciplina = async (inicio?: string, fim?: string
): Promise<Record<string, number>> => {
  const params: any = {};
  if (inicio) params.inicio = inicio;
  if (fim) params.fim = fim;
  
  const response = await httpConnect.get('/relatorios/tempoDisciplina', { params });
  return response.data;
};

export const obterDashboard = async (): Promise<DashboardInterface> => {
  const response = await httpConnect.get('/relatorios/dashboard');
  return response.data;
};