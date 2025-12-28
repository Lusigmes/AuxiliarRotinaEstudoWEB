import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import type { 
  RelatorioResumoInterface, 
  EstatisticaDisciplinaInterface,
  EstudoDiarioInterface,
  RevisaoStatusInterface 
} from '@/types';

export class PdfExportService {
  
  private formatarData(data?: string): string {
    if (!data) return '';
    if (data.includes('-')) {
      const [ano, mes, dia] = data.split('-');
      return `${dia}/${mes}/${ano}`;
    }
    return data;
  }

  private calcularPercentualTempo(tempoDisciplina: number, tempoTotal: number): number {
    if (!tempoTotal || tempoTotal === 0) return 0;
    const percentual = (tempoDisciplina / tempoTotal) * 100;
    return Number(percentual.toFixed(1));
  }

  public async generatePDF(data: {
    resumo: RelatorioResumoInterface | null;
    estatisticasDisciplinas: EstatisticaDisciplinaInterface[];
    estudosDiario: EstudoDiarioInterface[];
    statusRevisoes: RevisaoStatusInterface[];
    tempoPorDisciplina: Record<string, number> | null;
    topDisciplinas: EstatisticaDisciplinaInterface[];
    periodoInicio: string;
    periodoFim: string;
  }): Promise<void> {
    try {
      
      const {
        resumo,
        estatisticasDisciplinas,
        estudosDiario,
        statusRevisoes,
        tempoPorDisciplina,
        topDisciplinas,
        periodoInicio,
        periodoFim
      } = data;

      const today = new Date();
      const defaultEnd = today.toISOString().split('T')[0];
      const defaultStartDate = new Date(today);
      defaultStartDate.setDate(today.getDate() - 29); // últimos 30 dias incluindo hoje
      const defaultStart = defaultStartDate.toISOString().split('T')[0];

      const periodoInicioEfetivo = periodoInicio && String(periodoInicio).trim() ? periodoInicio : defaultStart;
      const periodoFimEfetivo = periodoFim && String(periodoFim).trim() ? periodoFim : defaultEnd;

      const doc = new jsPDF('p', 'mm', 'a4');
      const pageWidth = doc.internal.pageSize.getWidth();
      const pageHeight = doc.internal.pageSize.getHeight();
      
      const dataGeracao = new Date().toLocaleDateString('pt-BR', {
        day: '2-digit',
        month: 'long',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
      });

      doc.setFontSize(20);
      doc.setTextColor(75, 0, 224);
      doc.text('RELATÓRIO DE ESTUDOS', pageWidth / 2, 20, { align: 'center' });
      
      doc.setFontSize(10);
      doc.setTextColor(100, 100, 100);
      doc.text('Emissão dos dados de desempenho de estudo cadastrado no sistema', pageWidth / 2, 27, { align: 'center' });
      
      doc.setDrawColor(75, 0, 224);
      doc.setLineWidth(0.5);
      doc.line(20, 32, pageWidth - 20, 32);
      
      doc.setFontSize(10);
      doc.setTextColor(0, 0, 0);
      doc.text(`Período: ${this.formatarData(periodoInicioEfetivo)} a ${this.formatarData(periodoFimEfetivo)}`, 20, 42);
      doc.text(`Emitido em: ${dataGeracao}`, 20, 48);
      
      if (statusRevisoes?.length > 0) {
        const statusText = statusRevisoes.map(s => `${s.status}: ${s.quantidade}`).join(' • ');
        doc.text(`Quantidade de Revisões: ${statusText}`, 20, 54);
      }
      
      let yPos = 65;
      
      doc.setFontSize(14);
      doc.setTextColor(75, 0, 224);
      doc.text('RESUMO GERAL', 20, yPos);
      yPos += 10;
      
      doc.setFontSize(11);
      doc.setTextColor(0, 0, 0);
      
      doc.text(`• Estudos Realizados: ${resumo?.totalEstudos || 0}`, 25, yPos);
      yPos += 7;
      doc.text(`• Revisões Concluídas: ${resumo?.totalRevisoesConcluidas || 0}`, 25, yPos);
      yPos += 7;
      doc.text(`• Tempo Total: ${resumo?.tempoTotal || 0}h`, 25, yPos);
      yPos += 7;
      doc.text(`• Média Diária: ${(resumo?.mediaTempoDiario || 0).toFixed(1)}h`, 25, yPos);
      yPos += 7;
      doc.text(`• Disciplina Mais Estudada: ${resumo?.disciplinaMaisEstudada || 'Nenhuma'}`, 25, yPos);
      yPos += 15;
      
      doc.setFontSize(14);
      doc.setTextColor(75, 0, 224);
      doc.text('TOP 5 DISCIPLINAS', 20, yPos);
      yPos += 10;
      
      if (topDisciplinas.length > 0) {
        const topDisciplinasData = topDisciplinas.map((disciplina, index) => [
          `${index + 1}º`,
          disciplina.disciplina,
          `${disciplina.totalEstudos || 0}`,
          `${disciplina.totalTempo || 0}h`,
          `${(disciplina.mediaTempoEstudo || 0).toFixed(1)}h`
        ]);
        
        autoTable(doc, {
          startY: yPos,
          head: [['#', 'Disciplina', 'Estudos', 'Tempo', 'Média']],
          body: topDisciplinasData,
          headStyles: {
            fillColor: [75, 0, 224],
            textColor: [255, 255, 255],
            fontSize: 10
          },
          bodyStyles: {
            fontSize: 9
          },
          margin: { left: 20, right: 20 }
        });
        
        yPos = (doc as any).lastAutoTable?.finalY + 15 || yPos + 15;
      } else {
        doc.setFontSize(10);
        doc.setTextColor(100, 100, 100);
        doc.text('Nenhuma disciplina encontrada', 20, yPos);
        yPos += 10;
      }
      
      if (yPos > pageHeight - 100) {
        doc.addPage();
        yPos = 20;
      }
      
      doc.setFontSize(14);
      doc.setTextColor(75, 0, 224);
      doc.text('VISÃO GERAL DAS DISCIPLINAS', 20, yPos);
      yPos += 10;
      
      if (estatisticasDisciplinas.length > 0) {
        const tempoTotal = resumo?.tempoTotal || 
          estatisticasDisciplinas.reduce((acc, d) => acc + (d.totalTempo || 0), 0);
        
        const tabelaData = estatisticasDisciplinas.map(disciplina => [
          disciplina.disciplina,
          `${disciplina.totalEstudos || 0}`,
          `${disciplina.totalTempo || 0}h`,
          `${(disciplina.mediaTempoEstudo || 0).toFixed(1)}h`,
          `${disciplina.totalRevisoesConcluidas || 0}`,
          `${this.calcularPercentualTempo(disciplina.totalTempo || 0, tempoTotal)}%`
        ]);
        
        const totalEstudos = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalEstudos || 0), 0);
        const totalTempo = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalTempo || 0), 0);
        const totalRevisoes = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalRevisoesConcluidas || 0), 0);
        
        tabelaData.push([
          'TOTAIS',
          totalEstudos.toString(),
          totalTempo.toFixed(1) + 'h',
          '',
          totalRevisoes.toString(),
          '100%'
        ]);
        
        autoTable(doc, {
          startY: yPos,
          head: [['Disciplina', 'Estudos', 'Tempo', 'Média', 'Revisões', '%']],
          body: tabelaData,
          headStyles: {
            fillColor: [59, 130, 246],
            textColor: [255, 255, 255],
            fontSize: 9
          },
          bodyStyles: {
            fontSize: 8
          },
          margin: { left: 20, right: 20 },
          styles: {
            overflow: 'linebreak',
            cellWidth: 'wrap'
          },
          willDrawCell: (data: any) => {
            if (data.row.index === estatisticasDisciplinas.length) {
              doc.setFillColor(245, 245, 245);
              doc.rect(data.cell.x, data.cell.y, data.cell.width, data.cell.height, 'F');
              doc.setFont('helvetica', 'bold');
              doc.setTextColor(75, 0, 224);
            }
          }
        });
        
        yPos = (doc as any).lastAutoTable?.finalY + 15 || yPos + 15;
      }
      
      if (yPos > pageHeight - 50) {
        doc.addPage();
        yPos = 20;
      }
      
      if (estudosDiario.length > 0) {
        doc.setFontSize(14);
        doc.setTextColor(75, 0, 224);
        doc.text('ESTUDOS POR DIA', 20, yPos);
        yPos += 10;
        
        const estudosLimitados = estudosDiario.slice(0, 10); //limitar
        
        const estudosData = estudosLimitados.map(dia => [
          this.formatarData(dia.data || ''),
          `${dia.quantidadeEstudos || 0}`,
          `${dia.tempoTotal || 0}h`
        ]);
        
        autoTable(doc, {
          startY: yPos,
          head: [['Data', 'Estudos', 'Tempo']],
          body: estudosData,
          headStyles: {
            fillColor: [245, 158, 11],
            textColor: [255, 255, 255],
            fontSize: 9
          },
          bodyStyles: {
            fontSize: 9
          },
          margin: { left: 20, right: 20 }
        });
        
        if (estudosDiario.length > 10) {
          doc.setFontSize(8);
          doc.setTextColor(100, 100, 100);
          const finalY = (doc as any).lastAutoTable?.finalY || yPos;
          doc.text(`* Mostrando ${estudosLimitados.length} de ${estudosDiario.length} dias`, 20, finalY + 5);
        }
      }
      
      const totalPages = doc.internal.pages.length - 1;
      
      for (let i = 1; i <= totalPages; i++) {
        doc.setPage(i);
        
        doc.setDrawColor(200, 200, 200);
        doc.setLineWidth(0.2);
        doc.line(20, pageHeight - 20, pageWidth - 20, pageHeight - 20);
        
        // Texto do rodapé
        doc.setFontSize(8);
        doc.setTextColor(100, 100, 100);
        doc.text(
          `Página ${i} de ${totalPages} • Gerado em ${dataGeracao}`,
          pageWidth / 2,
          pageHeight - 10,
          { align: 'center' }
        );
      }
      
      const filename = `relatorio-estudos-${new Date().toISOString().split('T')[0]}.pdf`;
      doc.save(filename);
      
      
    } catch (error) {
      console.error('Erro ao gerar PDF:', error);
      throw new Error(`Falha ao gerar o PDF: ${error instanceof Error ? error.message : 'Erro desconhecido'}`);
    }
  }
  
  private calcularTotalTempo(tempoPorDisciplina: Record<string, number> | null): number {
    if (!tempoPorDisciplina) return 0;
    return Object.values(tempoPorDisciplina).reduce((acc, val) => acc + val, 0);
  }

  public exportTabelaCSV(estatisticasDisciplinas: EstatisticaDisciplinaInterface[], resumo: RelatorioResumoInterface | null): void {
    try {
      if (!estatisticasDisciplinas.length) {
        alert('Não há dados para exportar');
        return;
      }

      const tempoTotal = resumo?.tempoTotal || 
        estatisticasDisciplinas.reduce((acc, d) => acc + (d.totalTempo || 0), 0);
      
      const headers = ['Disciplina', 'Estudos', 'Tempo Total (h)', 'Média por Estudo (h)', 'Revisões Concluídas', '% do Tempo Total'];
      
      const rows = estatisticasDisciplinas.map(disciplina => [
        `"${disciplina.disciplina}"`,
        disciplina.totalEstudos || 0,
        disciplina.totalTempo || 0,
        (disciplina.mediaTempoEstudo || 0).toFixed(2),
        disciplina.totalRevisoesConcluidas || 0,
        `${this.calcularPercentualTempo(disciplina.totalTempo || 0, tempoTotal).toFixed(2)}%`
      ]);
      
      const totalEstudos = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalEstudos || 0), 0);
      const totalTempo = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalTempo || 0), 0);
      const totalRevisoes = estatisticasDisciplinas.reduce((sum, d) => sum + (d.totalRevisoesConcluidas || 0), 0);
      
      rows.push([
        '"TOTAIS"',
        totalEstudos,
        totalTempo.toFixed(2),
        '',
        totalRevisoes,
        '100%'
      ]);
      
      const csvContent = [
        headers.join(';'),
        ...rows.map(row => row.join(';'))
      ].join('\n');
      
      const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
      const link = document.createElement('a');
      const url = URL.createObjectURL(blob);
      
      link.setAttribute('href', url);
      link.setAttribute('download', `estatisticas-disciplinas-${new Date().toISOString().split('T')[0]}.csv`);
      link.style.visibility = 'hidden';
      
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      
    } catch (error) {
      console.error('Erro ao exportar CSV:', error);
      alert('Erro ao exportar CSV. Tente novamente.');
    }
  }
}

export const exportarPdfRelatorio = new PdfExportService();