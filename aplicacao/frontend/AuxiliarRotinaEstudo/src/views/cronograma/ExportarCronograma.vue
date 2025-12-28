<script setup lang="ts">
import { ref } from 'vue';
import jsPDF from 'jspdf';
import autoTable from 'jspdf-autotable';
import type { CronogramaResponseInterface, ItemCronogramaResponseInterface } from '@/types';
import { DiasSemana } from '@/types/enums';

const props = defineProps<{
  cronograma: CronogramaResponseInterface;
  diasSemana: Array<{ nome: string; valor: DiasSemana; cor: string }>;
}>();

const emit = defineEmits(['exportar', 'exportado', 'cancelar', 'fechar']);

const titulo = ref('Meu Cronograma de Estudos');
const incluirObservacoes = ref(false);
const observacoes = ref('');
const exportando = ref(false);

function getItensDoDia(dia: DiasSemana): ItemCronogramaResponseInterface[] {
  return props.cronograma.itensDoDia
    .filter(item => item.diaSemana === dia)
    .sort((a, b) => (a.ordem || 0) - (b.ordem || 0)); 
}

async function gerarPDF() {
  if (!titulo.value.trim()) {
    alert('Digite um título para o cronograma');
    return;
  }

  exportando.value = true;
  emit('exportar');

  try {
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
    doc.setTextColor(0, 0, 139);
    doc.text(titulo.value, pageWidth / 2, 20, { align: 'center' });
    
    doc.setFontSize(10);
    doc.setTextColor(100, 100, 100);
    doc.text('Cronograma Semanal de Estudos', pageWidth / 2, 27, { align: 'center' });
    
    doc.setDrawColor(0, 0, 139);
    doc.setLineWidth(0.5);
    doc.line(20, 32, pageWidth - 20, 32);
    
    doc.setFontSize(10);
    doc.setTextColor(0, 0, 0);
    let yPos = 42;
    
    doc.text(`Emitido em: ${dataGeracao}`, 20, yPos);
    doc.text(`Total de disciplinas: ${props.cronograma.itensDoDia.length}`, pageWidth - 20, yPos, { align: 'right' });
    yPos += 15;
    
    // tabelA
    doc.setFontSize(14);
    doc.setTextColor(0, 0, 139);
    doc.text('CRONOGRAMA SEMANAL', 20, yPos);
    yPos += 10;
    
    const diasOrdenados = [...props.diasSemana];
    
    const tabelaData = diasOrdenados.map(dia => {
      const itens = getItensDoDia(dia.valor);
      const disciplinas = itens
        .map((item, index) => `${index + 1}. ${item.nomeDisciplina}`)
        .join(', ');

      return [dia.nome, disciplinas || 'Sem disciplinas'];
    });
    
    autoTable(doc, {
      startY: yPos,
      head: [['Dia da Semana', 'Disciplinas']],
      body: tabelaData,
      headStyles: {
        fillColor: [0, 0, 139],
        textColor: [255, 255, 255],
        fontSize: 10
      },
      bodyStyles: {
        fontSize: 9
      },
      margin: { left: 20, right: 20 },
      styles: {
        overflow: 'linebreak',
        cellWidth: 'wrap'
      },
      columnStyles: {
        0: { cellWidth: 50 },
        1: { cellWidth: 'auto' }
      }
    });
    
    yPos = (doc as any).lastAutoTable?.finalY + 15 || yPos + 15;
    
    if (incluirObservacoes.value && observacoes.value.trim()) {
      if (yPos > pageHeight - 50) {
        doc.addPage();
        yPos = 20;
      }
      
      doc.setFontSize(14);
      doc.setTextColor(0, 0, 139);
      doc.text('OBSERVAÇÕES', 20, yPos);
      yPos += 10;
      
      doc.setFontSize(10);
      doc.setTextColor(0, 0, 0);
      
      const observacoesLines = doc.splitTextToSize(observacoes.value, pageWidth - 40);
      doc.text(observacoesLines, 20, yPos);
      yPos += observacoesLines.length * 5 + 10;
    }
    
    const totalPages = doc.internal.pages.length - 1;
    
    for (let i = 1; i <= totalPages; i++) {
      doc.setPage(i);
      
      doc.setDrawColor(200, 200, 200);
      doc.setLineWidth(0.2);
      doc.line(20, pageHeight - 20, pageWidth - 20, pageHeight - 20);
      
      doc.setFontSize(8);
      doc.setTextColor(100, 100, 100);
      doc.text(
        `Página ${i} de ${totalPages} • Sistema de Gerenciamento de Estudos • ${dataGeracao}`,
        pageWidth / 2,
        pageHeight - 10,
        { align: 'center' }
      );
    }
    
    const filename = `cronograma-estudos-${new Date().toISOString().split('T')[0]}.pdf`;
    doc.save(filename);
    
    emit('exportado');
    emit('fechar');
    
  } catch (error) {
    console.error('Erro ao gerar PDF:', error);
    alert('Erro ao gerar o PDF. Tente novamente.');
  } finally {
    exportando.value = false;
  }
}
</script>



<template>
  <v-card variant="flat" elevation="2">
    <v-card-item>
      <template #prepend>
        <v-avatar color="primary" variant="tonal" size="48">
          <v-icon icon="mdi-file-pdf-box" />
        </v-avatar>
      </template>

      <v-card-title class="text-h4">Exportar Cronograma</v-card-title>
      <v-card-subtitle class="text-h6">
        Exporte seu cronograma de estudos em PDF
      </v-card-subtitle>
    </v-card-item>

    <v-card-text>
      <v-form @submit.prevent="gerarPDF" class="mb-4">
        <v-row>
          <v-col cols="12">
            <v-text-field
              v-model="titulo"
              label="Título do Cronograma"
              placeholder="Ex: Meu Cronograma de Estudos - 1º Semestre"
              variant="outlined"
              required
              maxlength="1000"
              counter
              :rules="[v => !!v || 'Título é obrigatório']"
            />
          </v-col>

          <v-col cols="12">
            <v-checkbox
              v-model="incluirObservacoes"
              label="Incluir espaço para observações"
              color="primary"
              hide-details
            />
          </v-col>

          <v-col cols="12" v-if="incluirObservacoes">
            <v-textarea
              v-model="observacoes"
              label="Observações"
              placeholder="Adicione observações sobre seu cronograma..."
              variant="outlined"
              rows="3"
              maxlength="1000"
              counter
              class="fixed-textarea"
            />
          </v-col>
        </v-row>
      </v-form>

      <div class="d-flex justify-center gap-4 mt-6">
        <v-btn variant="outlined" color="grey" @click="$emit('cancelar')" :disabled="exportando">
          <v-icon icon="mdi-close" class="mr-2" /> Cancelar
        </v-btn>
        <v-btn color="primary" variant="tonal" @click="gerarPDF" :loading="exportando">
          <v-icon icon="mdi-file-pdf-box" class="mr-2" /> Gerar PDF
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>


<style scoped>
.gap-4 {
  gap: 16px;
}
.fixed-textarea textarea {
  max-height: 150px;
  overflow-y: auto;
}
</style>