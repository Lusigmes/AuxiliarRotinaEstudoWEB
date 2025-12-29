<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AdicionarItensForm from './AdicionarItensForm.vue';
import ExportarCronograma from './ExportarCronograma.vue';
import { useNotification } from '@/composables/useNotification';

import {
  verificarCronogramaDoUsuarioExiste,
  buscarCronogramaDoUsuario,
  criarCronograma,
  adicionarItemCronograma,
  deletarCronograma
} from '@/api/CronogramaService';
import { deletarItem as del, atualizarItem, reordenarItensDia }  from '@/api/ItemCronogramaService';
import {
  type CronogramaResponseInterface,
  type CronogramaInterface,
  type ItemCronogramaInterface,
  type Estado,
  type ModoAdicao,
  type ItemCronogramaResponseInterface
} from '@/types';
import { DiasSemana } from '@/types/enums';

const router = useRouter();
const { showNotification } = useNotification();

const estado = ref<Estado>('semCronograma');
const modoAdicao = ref<ModoAdicao>('criar');
const loading = ref(false);

const cronograma = ref<CronogramaResponseInterface | null>(null);
const itensTemporarios = ref<ItemCronogramaInterface[]>([]);

const nomeDisciplinaInline = ref('');
const diaAdicionando = ref<DiasSemana | null>(null);

const modalFormAddItens = ref(false);
const modalExportar = ref(false);

const itemHoverId = ref<number | null>(null);
const itemEditandoId = ref<number | null>(null);
const textoEditando = ref('');
const itemArrastando = ref<{id: number, nomeDisciplina:string, diaSemana: DiasSemana, index: number} | null>(null);
const textoEditandoOriginal = ref('');

const ordemItensPorDia = ref<Map<DiasSemana, number[]>>(new Map());

const dropTarget = ref<{dia: DiasSemana | null, index: number | null}>({ dia: null, index: null });

let debounceTimeout: ReturnType<typeof setTimeout> | null = null;
let ultimoDiaReordenado: DiasSemana | null = null;

const coresDiasDaSemana = [
  { nome: 'Segunda-feira', valor: DiasSemana.SEGUNDA, cor: 'red' },
  { nome: 'Terça-feira', valor: DiasSemana.TERCA, cor: 'orange' },
  { nome: 'Quarta-feira', valor: DiasSemana.QUARTA, cor: 'yellow' },
  { nome: 'Quinta-feira', valor: DiasSemana.QUINTA, cor: 'green' },
  { nome: 'Sexta-feira', valor: DiasSemana.SEXTA, cor: 'blue' },
  { nome: 'Sábado', valor: DiasSemana.SABADO, cor: 'purple' },
  { nome: 'Domingo', valor: DiasSemana.DOMINGO, cor: 'pink' }
];

const breadcrumbs = computed(() => [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Cronograma', disabled: true }
]);

const subtitulo = computed(() => {
  switch (estado.value) {
    case 'semCronograma': return 'Crie seu primeiro cronograma';
    case 'visualizando': return 'Seu cronograma de estudos';
    default: return '';
  }
});

const itensDoDia = computed(() => cronograma.value?.itensDoDia || []);


function getItensDoDia(dia: DiasSemana): ItemCronogramaResponseInterface[] {
  const itensDia = itensDoDia.value.filter(item => item.diaSemana === dia);
  
  const ordemLocal = ordemItensPorDia.value.get(dia);
  if (ordemLocal && ordemLocal.length > 0) {
    const mapaItens = new Map(itensDia.map(item => [item.id, item]));
    const itensOrdenados: ItemCronogramaResponseInterface[] = [];
    
    for (const id of ordemLocal) {
      const item = mapaItens.get(id);
      if (item) itensOrdenados.push(item);
    }
    
    for (const item of itensDia) {
      if (!ordemLocal.includes(item.id)) {
        itensOrdenados.push(item);
      }
    }
    
    return itensOrdenados;
  }
  
  return [...itensDia].sort((a, b) => (a.ordem || 0) - (b.ordem || 0));
}

function iniciarCriacaoCronograma() {
  modoAdicao.value = 'criar';
  itensTemporarios.value = [];
  modalFormAddItens.value = true;
  showNotification('Criando novo cronograma', 'info');
};

function iniciarAdicaoItens() {
  modoAdicao.value = 'adicionar';
  itensTemporarios.value = [];
  modalFormAddItens.value = true;
  showNotification('Adicionando itens ao cronograma', 'info');
};

function iniciarExportarPDF() {
  modalExportar.value = true;
  showNotification('Preparando exportação em PDF', 'info');
};

function fecharModalExportar() {
  modalExportar.value = false;
};

function fecharModalAdicaoItens() {
  modalFormAddItens.value = false; 
  if (cronograma.value) estado.value = 'visualizando';
  else estado.value = 'semCronograma';
  itensTemporarios.value = [];
};

function adicionarItem(item: ItemCronogramaInterface) {
  itensTemporarios.value.push(item);
};

function removerItemTemporario(index: number) {
  itensTemporarios.value.splice(index, 1);
};

// add itens
async function finalizarAdicaoItens() {
  loading.value = true;
  try {
    if (modoAdicao.value === 'criar') {
      const cronogramaDTO: CronogramaInterface = { itensDoDia: itensTemporarios.value };
      cronograma.value = await criarCronograma(cronogramaDTO);
      showNotification('Cronograma criado com sucesso!', 'success');
    } else {
      if (!cronograma.value) throw new Error('Cronograma não encontrado');
      
      const itensPorDia = new Map<DiasSemana, ItemCronogramaInterface[]>();
      
      itensTemporarios.value.forEach(item => {
        if (!itensPorDia.has(item.diaSemana)) {
          itensPorDia.set(item.diaSemana, []);
        }
        itensPorDia.get(item.diaSemana)!.push(item);
      });
      
      const itensComOrdem: ItemCronogramaInterface[] = [];
      
      itensPorDia.forEach((itensDoDia, dia) => {
        const itensExistentes = cronograma.value!.itensDoDia.filter(
          item => item.diaSemana === dia
        ).length;
        
        itensDoDia.forEach((item, index) => {
          itensComOrdem.push({
            ...item,
            ordem: itensExistentes + index  
          });
        });
      });
      
      cronograma.value = await adicionarItemCronograma(
        cronograma.value.id, 
        { itensDoDia: itensComOrdem }
      );
      showNotification(`${itensTemporarios.value.length} itens adicionados ao cronograma`, 'success');
    }
   
    estado.value = 'visualizando';
    modalFormAddItens.value = false;
    itensTemporarios.value = [];
  
    atualizarMapItens();
    inicializarOrdemItens();
  } catch (err) {
    console.error(err);
    showNotification('Erro ao salvar cronograma. Tente novamente.', 'error');
  } finally {
    loading.value = false;
  }
};

const itensMap = ref(new Map<number, ItemCronogramaResponseInterface>());

function atualizarMapItens(){
  if(cronograma.value){
    itensMap.value.clear();
    cronograma.value.itensDoDia.forEach(item => {
      itensMap.value.set(item.id, item);
    });
  }
};

function inicializarOrdemItens() {
  if (!cronograma.value) return;
  
  ordemItensPorDia.value.clear();
  
  coresDiasDaSemana.forEach(dia => {
    const itensDia = cronograma.value!.itensDoDia
      .filter(item => item.diaSemana === dia.valor)
      .sort((a, b) => (a.ordem || 0) - (b.ordem || 0)); 
      
    const ids = itensDia.map(item => item.id);
    ordemItensPorDia.value.set(dia.valor, ids);
  });
};

async function carregarCronograma() {
  loading.value = true;
  try {
    const existe = await verificarCronogramaDoUsuarioExiste();
   
    if (existe) {
      cronograma.value = await buscarCronogramaDoUsuario();
      estado.value = 'visualizando';
      atualizarMapItens();
      inicializarOrdemItens();
      
      sincronizarOrdemComBackend();
      showNotification('Cronograma carregado com sucesso!', 'success');
    } else estado.value = 'semCronograma';
  
  } catch (err) {
    console.error('Erro ao carregar cronograma:', err);
    estado.value = 'semCronograma';
    showNotification('Erro ao carregar cronograma. Tente novamente.', 'error');
  } finally {
    loading.value = false;
  }
};

function sincronizarOrdemComBackend() {
  if (!cronograma.value) return;
  
  coresDiasDaSemana.forEach(dia => {
    const itensDia = cronograma.value!.itensDoDia
      .filter(item => item.diaSemana === dia.valor)
      .sort((a, b) => (a.ordem || 0) - (b.ordem || 0));
    
    const ids = itensDia.map(item => item.id);
    ordemItensPorDia.value.set(dia.valor, ids);
  });
};

async function deletarItem(idItem: number){
  try {
    await del(idItem);
    
    if(cronograma.value) {
      itensMap.value.delete(idItem);
      cronograma.value.itensDoDia = cronograma.value.itensDoDia.filter(
        item => item.id !== idItem
      );
      
      coresDiasDaSemana.forEach(dia => {
        const ordem = ordemItensPorDia.value.get(dia.valor);
        if (ordem) {
          const novaOrdem = ordem.filter(id => id !== idItem);
          ordemItensPorDia.value.set(dia.valor, novaOrdem);
        }
      });
    }

    showNotification('Disciplina removida com sucesso!', 'success');
  } catch (error) {
    console.error('Erro ao remover item:', error);
    showNotification('Erro ao remover disciplina. Tente novamente.', 'error');
  } finally {
    loading.value = false;
    itemHoverId.value = null;
  }
};

//editar
function iniciarEdicaoItem(item: ItemCronogramaResponseInterface){
  itemEditandoId.value = item.id;
  textoEditando.value = item.nomeDisciplina;
  textoEditandoOriginal.value = item.nomeDisciplina;
  itemHoverId.value = null;
  setTimeout(() => {
    const input = document.querySelector('.edit-field input');
    if (input) {
      (input as HTMLInputElement).focus();
      (input as HTMLInputElement).select();
    }
  }, 50);
};

function cancelarEdicao(){
  itemEditandoId.value = null;
  textoEditando.value = "";
};

async function salvarEdicaoItem(idItem: number){
  if(!textoEditando.value.trim()){
    cancelarEdicao();
    showNotification('Nome da disciplina não pode estar vazio', 'warning');
    return;
  }
  
  if(textoEditando.value.trim() === textoEditandoOriginal.value){
    cancelarEdicao();
    return;
  }
  
  loading.value = true;
  try {
    const targetItem = itensMap.value.get(idItem);
    if(targetItem && cronograma.value){
      targetItem.nomeDisciplina = textoEditando.value.trim();
      await atualizarItem(idItem, {...targetItem, nomeDisciplina: textoEditando.value.trim()} );
      showNotification('Disciplina editada com sucesso!', 'success');
    }
  } catch (error) {
    console.error('Erro ao editar item:', error);
    showNotification('Erro ao editar disciplina. Tente novamente.', 'error');
    await carregarCronograma();
  } finally {
    loading.value = false;
    cancelarEdicao();
  }
};

//reordenar itens no dia
function reordenarArray<T>(array: T[], fromIndex: number, toIndex: number): T[] {
  const newArray = [...array];
  const [removed] = newArray.splice(fromIndex, 1);
  newArray.splice(toIndex, 0, removed!);
  return newArray;
}

async function salvarReordenacaoComDebounce(dia: DiasSemana, idsOrdenados: number[]) {
  if (debounceTimeout) {
    clearTimeout(debounceTimeout);
  }
  
  debounceTimeout = setTimeout(async () => {
    try {
      await reordenarItensDia(dia, idsOrdenados);
      
      if (cronograma.value) {
        const itensDia = cronograma.value.itensDoDia.filter(item => item.diaSemana === dia);
        
        idsOrdenados.forEach((id, index) => {
          const item = itensDia.find(item => item.id === id);
          if (item) {
            item.ordem = index;
          }
        });
      }
      

      ultimoDiaReordenado = null;
      
    } catch (error) {
      console.warn('Erro ao salvar reordenação:', error);
      showNotification('Erro ao salvar ordenação das disciplinas', 'warning');
    }
  }, 50);
};
function limparDebounce() {
  if (debounceTimeout) {
    clearTimeout(debounceTimeout);
    debounceTimeout = null;
  }
  ultimoDiaReordenado = null;
}
function iniciarArrastar(item: ItemCronogramaResponseInterface, index: number, event: DragEvent){
  if(itemEditandoId.value) return;

  itemArrastando.value = {
    id: item.id,
    nomeDisciplina: item.nomeDisciplina,
    diaSemana: item.diaSemana,
    index: index
  };

  if(event.dataTransfer){
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData("text/plain", item.id.toString());
    const element = event.target as HTMLElement;
    element.classList.add('dragging-active');
  }
};

function finalizarArrastar(event: DragEvent){
  if (event.target instanceof HTMLElement) {
    event.target.classList.remove('dragging-active');
  }
  
  itemArrastando.value = null;
  dropTarget.value = { dia: null, index: null };
  
  ultimoDiaReordenado = null;
 
  // if (ultimoDiaReordenado) {
  //   const ordem = ordemItensPorDia.value.get(ultimoDiaReordenado);
  //   if (ordem) {
  //     salvarReordenacaoComDebounce(ultimoDiaReordenado, ordem);
  //   }
  // }
};

function permitirDrop(event: DragEvent){
  event.preventDefault();
  if(event.dataTransfer){
    event.dataTransfer.dropEffect = 'move';
  }
};

function sobreItem(dia: DiasSemana, index: number) {
  if (itemArrastando.value && itemArrastando.value.diaSemana === dia) {
    dropTarget.value = { dia, index };
  }
};

function sairDoItem() {
  dropTarget.value = { dia: null, index: null };
};

async function soltarItem(dia: DiasSemana, event: DragEvent){
  event.preventDefault();
  
  if(!itemArrastando.value || itemArrastando.value.diaSemana !== dia) {
    if(itemArrastando.value) {
      await moverEntreDias(itemArrastando.value.id, dia);
    }
    finalizarArrastar(event);
    return;
  }
  
  // const idItem = itemArrastando.value.id;
  const itemIndex = itemArrastando.value.index;
  const dropIndex = dropTarget.value.index ?? itemIndex;

  if (dropIndex === itemIndex) {
    finalizarArrastar(event);
    return;
  }

  try {
    const ordemAtual = ordemItensPorDia.value.get(dia) || [];
    const novaOrdem = reordenarArray(ordemAtual, itemIndex, dropIndex);
    
    ordemItensPorDia.value.set(dia, novaOrdem);
    
    if (cronograma.value) {
      novaOrdem.forEach((id, index) => {
        const item = cronograma.value!.itensDoDia.find(i => i.id === id);
        if (item && item.diaSemana === dia) {
          item.ordem = index;
        }
      });
    }
    
    await salvarReordenacaoComDebounce(dia, novaOrdem);
    
    showNotification('Ordem das disciplinas atualizada', 'success');
    
  } catch (error) {
    console.error('Erro ao reordenar item:', error);
    showNotification('Erro ao reordenar disciplina. Tente novamente.', 'error');
    await carregarCronograma(); 
  } finally {
    finalizarArrastar(event);
  }
};

async function moverEntreDias(idItem: number, novoDia: DiasSemana) {
  limparDebounce();
  try {
    const targetItem = itensMap.value.get(idItem);
    if(!targetItem || !cronograma.value) return;
    
    const diaAntigo = targetItem.diaSemana;
    const itensNoNovoDia = cronograma.value.itensDoDia.filter(item => 
      item.diaSemana === novoDia && item.id !== idItem
    );
    const novaOrdem = itensNoNovoDia.length; 
    
    await atualizarItem(idItem, {
      ...targetItem, 
      diaSemana: novoDia,
      ordem: novaOrdem  
    });
    
    targetItem.diaSemana = novoDia;
    targetItem.ordem = novaOrdem;
    
    if (diaAntigo !== novoDia) {
      const itensDiaAntigo = cronograma.value.itensDoDia
        .filter(item => item.diaSemana === diaAntigo && item.id !== idItem)
        .sort((a, b) => (a.ordem || 0) - (b.ordem || 0));
      
      
      itensDiaAntigo.forEach((item, index) => {
        if (item.ordem !== index) {
          item.ordem = index;
        }
      });
      
      if (itensDiaAntigo.length > 0) {
        const idsDiaAntigo = itensDiaAntigo.map(item => item.id);
        ordemItensPorDia.value.set(diaAntigo, idsDiaAntigo);
        
        const itensComOrdemAlterada = itensDiaAntigo.filter(item => 
          item.ordem !== itensDiaAntigo.indexOf(item)
        );
        
        if (itensComOrdemAlterada.length > 0) {
          await salvarReordenacaoComDebounce(diaAntigo, idsDiaAntigo);
        }
      } else {
        ordemItensPorDia.value.set(diaAntigo, []);
      }
    }
    
    const idsNovoDia = cronograma.value.itensDoDia
      .filter(item => item.diaSemana === novoDia)
      .sort((a, b) => (a.ordem || 0) - (b.ordem || 0))
      .map(item => item.id);
    
    ordemItensPorDia.value.set(novoDia, idsNovoDia);
    

    
    showNotification('Disciplina movida para outro dia', 'success');
    
  } catch (error) {
    console.error('Erro ao mover item entre dias:', error);
    showNotification('Erro ao mover disciplina. Tente novamente.', 'error');
    await carregarCronograma();
    throw error;
  }
};

// add inline
function ativarInputRapido(dia: DiasSemana) {
  diaAdicionando.value = dia;
  nomeDisciplinaInline.value = '';
  setTimeout(() => {
    const input = document.querySelector('.add-rapido-container input');
    if (input) {
      (input as HTMLInputElement).focus();
    }
  }, 50);
};

function cancelarDisciplinaRapida() {
  diaAdicionando.value = null;
  nomeDisciplinaInline.value = '';
};

async function salvarDisciplinaRapida(dia: DiasSemana) {
  if (!nomeDisciplinaInline.value.trim() || !cronograma.value) {
    showNotification('Digite o nome da disciplina', 'warning');
    return;
  }

  const itensDoDia = cronograma.value.itensDoDia.filter(item => item.diaSemana === dia);
  const proximaOrdem = itensDoDia.length; 

  const novoItem = { 
    nomeDisciplina: nomeDisciplinaInline.value.trim(), 
    diaSemana: dia,
    ordem: proximaOrdem 
  };
  
  try {
    const atualizado = await adicionarItemCronograma(cronograma.value.id, { itensDoDia: [novoItem] });
    
    cronograma.value = atualizado;
    diaAdicionando.value = null;
    nomeDisciplinaInline.value = '';

    atualizarMapItens();
    inicializarOrdemItens();
    
    showNotification('Disciplina adicionada com sucesso!', 'success');
  } catch (error) {
    console.error('Erro ao adicionar disciplina:', error);
    showNotification('Erro ao adicionar disciplina. Tente novamente.', 'error');
  }
};
// Adicione esta função junto com os outros métodos, antes do onMounted
async function deletarCronogramaConfirm() {
  if (!cronograma.value) return;
  
  if (confirm('Tem certeza que deseja deletar o cronograma atual? Esta ação não pode ser desfeita.')) {
    loading.value = true;
    try {
      await deletarCronograma(cronograma.value.id);
      showNotification('Cronograma deletado com sucesso!', 'success');
      
      // Resetar completamente o estado local
      cronograma.value = null;
      itensMap.value.clear();
      ordemItensPorDia.value.clear();
      estado.value = 'semCronograma';
      
      // Forçar recarregamento dos dados
      await carregarCronograma();
      
    } catch (error: any) {
      console.error('Erro ao deletar cronograma:', error);
      
      // Verificar o tipo de erro
      if (error.response?.status === 404) {
        // Cronograma já não existe mais
        showNotification('Cronograma já foi removido', 'info');
        cronograma.value = null;
        estado.value = 'semCronograma';
      } else if (error.response?.status === 403) {
        showNotification('Acesso negado para deletar este cronograma', 'error');
      } else {
        showNotification('Erro ao deletar cronograma. Tente novamente.', 'error');
      }
    } finally {
      loading.value = false;
    }
  }
};
onMounted(async () => {
  await carregarCronograma();
});
  
</script>
<template>
  <v-container fluid class="pa-6">
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template #divider><v-icon>mdi-chevron-right</v-icon></template>
    </v-breadcrumbs>

    <v-card variant="flat" class="mb-6">
      <v-card-item>
        <template #prepend>
          <v-avatar color="blue" variant="tonal" size="48">
            <v-icon icon="mdi-calendar-week" />
          </v-avatar>
        </template>

        <v-card-title class="text-h4">Cronograma</v-card-title>
        <v-card-subtitle class="text-h6">{{ subtitulo }}</v-card-subtitle>

        <template #append>
          <div class="d-flex gap-2">
            <v-btn
              v-if="estado === 'visualizando' && cronograma"
              color="error"
              variant="tonal"
              @click="deletarCronogramaConfirm"
              class="btn-delete-cronograma"
              :loading="loading"
            >
              <v-icon>mdi-delete</v-icon>
              <v-tooltip activator="parent" location="bottom">
                Deletar cronograma atual
              </v-tooltip>
            </v-btn>
            <v-btn
              v-if="estado === 'visualizando'"
              color="primary"
              prepend-icon="mdi-file-pdf-box"
              @click="iniciarExportarPDF"
            >
              Exportar PDF
            </v-btn>
            <v-btn
              v-if="estado === 'visualizando'"
              color="primary"
              prepend-icon="mdi-plus"
              @click="iniciarAdicaoItens"
            >
              Adicionar Itens
            </v-btn>
          </div>
        </template>
      </v-card-item>
    </v-card>

    <!-- SEM CRONOGRAMA -->
    <div v-if="estado === 'semCronograma'">
      <v-card variant="flat" class="text-center py-12">
        <v-card-item>
          <v-avatar color="blue-lighten-5" size="120" class="mb-4">
            <v-icon icon="mdi-calendar-plus" size="64" color="blue" />
          </v-avatar>
          <v-card-title class="text-h3 mb-2">Criar Seu Cronograma</v-card-title>
          <v-card-subtitle class="text-h6 mb-6">
            Comece organizando seus estudos para a semana
          </v-card-subtitle>
          <v-btn
            color="primary"
            size="x-large"
            prepend-icon="mdi-plus"
            @click="iniciarCriacaoCronograma"
            :loading="loading"
          >
            Criar Meu Cronograma
          </v-btn>
        </v-card-item>
      </v-card>
    </div>

    <!-- VISUALIZANDO CRONOGRAMA -->
    <div v-else-if="estado === 'visualizando'">
      <v-card variant="flat" elevation="2">
        <v-card-text class="pa-0">
          <v-table>
            <thead>
              <tr>
                <th class="text-left" style="width: 150px;">Dia da Semana</th>
                <th class="text-left">Disciplinas</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="dia in coresDiasDaSemana" :key="dia.nome">
                <td class="font-weight-bold">
                  <div class="d-flex align-center">
                    <v-avatar :color="dia.cor" size="32" class="mr-3">
                      <v-icon icon="mdi-calendar" size="18" color="white" />
                    </v-avatar>
                    {{ dia.nome }}
                  </div>
                </td>

                <td>
                  <!-- drop -->
                  <div 
                    class="d-flex flex-wrap align-center gap-2 drop-zone"
                    :class="{
                      'drop-zone-active': itemArrastando && itemArrastando.diaSemana === dia.valor,
                      'drop-zone-between': itemArrastando && itemArrastando.diaSemana !== dia.valor
                    }"
                    @dragover="permitirDrop($event)"
                    @drop="soltarItem(dia.valor, $event)"
                  >
                    <!-- itens atuais-->
                    <div 
                      v-for="(item, index) in getItensDoDia(dia.valor)" 
                      :key="item.id" 
                      class="item-container"
                      :class="{
                        'drop-before': dropTarget.dia === dia.valor && dropTarget.index === index,
                        'dragging-item': itemArrastando?.id === item.id
                      }"
                      @mouseenter="itemHoverId = item.id"
                      @mouseleave="itemHoverId = null"
                      @dblclick="iniciarEdicaoItem(item)"
                      draggable="true"
                      @dragstart="iniciarArrastar(item, index, $event)"
                      @dragend="finalizarArrastar"
                      @dragover="permitirDrop($event); sobreItem(dia.valor, index)"
                      @dragleave="sairDoItem"
                    >
                      <!-- editar inline  -->
                      <div v-if="itemEditandoId === item.id" class="edit-container d-flex align-center">
                        <div class="d-flex align-center">
                          <v-text-field
                            v-model="textoEditando"
                            variant="outlined"
                            density="compact"
                            hide-details
                            class="edit-field"
                            style="min-width: 180px;"
                            @keyup.enter="salvarEdicaoItem(item.id)"
                            @keyup.esc="cancelarEdicao"
                            autofocus
                            rounded
                          >
                            <template #append-inner>
                              <v-btn
                                icon
                                size="small"
                                variant="text"
                                color="success"
                                @click="salvarEdicaoItem(item.id)"
                                class="ml-1"
                                :disabled="!textoEditando.trim()"
                              >
                                <v-icon size="18">mdi-check</v-icon>
                              </v-btn>
                              <v-btn
                                icon
                                size="small"
                                variant="text"
                                color="grey"
                                @click="cancelarEdicao"
                                class="ml-1"
                              >
                                <v-icon size="18">mdi-close</v-icon>
                              </v-btn>
                            </template>
                          </v-text-field>
                        </div>
                      </div>
                      
                      <!-- chip normal -->
                      <v-chip
                        v-else
                        color="blue"
                        variant="flat"
                        class="ma-1 chip-draggable"
                        :class="{
                          'chip-hover': itemHoverId === item.id, 
                          'chip-dragging': itemArrastando?.id === item.id 
                        }"
                      >
                        {{ item.nomeDisciplina }}
                        
                        <!-- deletar hover -->
                        <v-btn
                          v-if="itemHoverId === item.id"
                          icon
                          size="x-small"
                          variant="flat"
                          color="red"
                          @click.stop="deletarItem(item.id)"
                          class="ml-2 delete-btn"
                        >
                          <v-icon size="14">mdi-close</v-icon>
                        </v-btn>

                        <div v-if="itemHoverId === item.id" class="hover-indicator"></div>
                      </v-chip>
                    </div>

                    <!-- add inline -->
                    <template v-if="diaAdicionando === dia.valor">
                      <div class="add-rapido-container d-flex align-center">
                        <v-text-field
                          v-model="nomeDisciplinaInline"
                          variant="outlined"
                          density="compact"
                          hide-details
                          placeholder="Nova disciplina"
                          style="min-width: 180px;"
                          @keyup.enter="salvarDisciplinaRapida(dia.valor)"
                          @keyup.esc="cancelarDisciplinaRapida"
                          autofocus
                          rounded
                        >
                          <template #append-inner>
                            <v-btn
                              icon
                              size="small"
                              variant="text"
                              color="success"
                              @click="salvarDisciplinaRapida(dia.valor)"
                              class="ml-1"
                              :disabled="!nomeDisciplinaInline.trim()"
                            >
                              <v-icon size="18">mdi-check</v-icon>
                            </v-btn>
                            <v-btn
                              icon
                              size="small"
                              variant="text"
                              color="grey"
                              @click="cancelarDisciplinaRapida"
                              class="ml-1"
                            >
                              <v-icon size="18">mdi-close</v-icon>
                            </v-btn>
                          </template>
                        </v-text-field>
                      </div>
                    </template>
                    <v-btn
                      v-else
                      icon
                      size="small"
                      variant="text"
                      color="primary"
                      @click="ativarInputRapido(dia.valor)"
                    >
                      <v-icon>mdi-plus</v-icon>
                    </v-btn>
                  </div>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>

      <!-- SEM ITENS -->
      <v-card v-if="itensDoDia.length === 0" variant="flat" class="text-center py-8 mt-4">
        <v-card-text>
          <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-calendar-remove</v-icon>
          <div class="text-h6 text-grey">Nenhum item no cronograma</div>
          <div class="text-body-1 text-grey mt-2">Adicione itens para começar a organizar seus estudos</div>
          <v-btn color="primary" class="mt-4" @click="iniciarAdicaoItens">
            <v-icon icon="mdi-plus" class="mr-2" /> Adicionar Itens
          </v-btn>
        </v-card-text>
      </v-card>
    </div>

    <!-- MODAL DE ADIÇÃO -->
    <v-dialog v-model="modalFormAddItens" max-width="1000px" persistent>
      <template #default>
        <div class="modal-overlay">
          <AdicionarItensForm
            :modo-adicao="modoAdicao"
            :itens-temporarios="itensTemporarios"
            @cancelar="fecharModalAdicaoItens"
            @finalizar="finalizarAdicaoItens"
            @adicionar-item="adicionarItem"
            @remover-item="removerItemTemporario"
          />
        </div>
      </template>
    </v-dialog>

    <!-- MODAL DE EXPORTAR -->
    <v-dialog v-model="modalExportar" max-width="800px" persistent>
      <template #default>
        <div class="modal-overlay">
          <ExportarCronograma
            v-if="cronograma"
            :cronograma="cronograma"
            :dias-semana="coresDiasDaSemana"
            @cancelar="fecharModalExportar"
            @fechar="fecharModalExportar"
          />
        </div>
      </template>
    </v-dialog>

    <v-card v-if="loading" variant="flat" class="text-center py-12">
      <v-card-text>
        <v-progress-circular indeterminate color="primary" size="64" />
        <div class="text-h6 mt-4">Carregando...</div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<style scoped>
.gap-2 { gap: 8px; }

.modal-overlay {
  position: relative;
  z-index: 1000;
}

.item-container {
  position: relative;
  display: inline-flex;
  align-items: center;
  transition: all 0.2s ease;
}

.item-container.drop-before {
  position: relative;
}

.item-container.drop-before::before {
  content: '';
  position: absolute;
  top: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: #2196F3;
  border-radius: 1px;
  z-index: 10;
  animation: pulse 1.5s infinite;
}

.item-container:last-child.drop-before::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  right: 0;
  height: 2px;
  background: #2196F3;
  border-radius: 1px;
  z-index: 10;
  animation: pulse 1.5s infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 0.6; }
  50% { opacity: 1; }
}

.chip-hover {
  transform: scale(1.02);
  transition: transform 0.2s ease;
}

.chip-draggable {
  cursor: move;
  user-select: none;
  transition: all 0.3s ease;
}

.chip-dragging {
  opacity: 0.4;
  transform: scale(0.95);
}

.dragging-active {
  opacity: 0.4;
  transform: scale(0.95);
  transition: all 0.2s ease;
}

.drag-handle {
  opacity: 0.7;
  cursor: move;
  transition: opacity 0.2s ease;
}

.drag-handle:hover {
  opacity: 1;
}

.delete-btn {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  min-width: 24px !important;
  height: 24px !important;
  transition: all 0.2s ease;
}

.delete-btn:hover {
  transform: scale(1.1);
  box-shadow: 0 3px 6px rgba(0, 0, 0, 0.4);
}

.hover-indicator {
  position: absolute;
  top: -2px;
  right: -2px;
  width: 8px;
  height: 8px;
  background: #ff4444;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}

.position-relative {
  position: relative;
}

.item-container:has(.edit-container) {
  z-index: 5;
}

.add-rapido-container {
  position: relative;
  display: inline-flex;
}

.drop-zone {
  min-height: 60px;
  padding: 8px;
  border-radius: 8px;
  transition: all 0.2s ease;
}

.drop-zone-active {
  background-color: rgba(33, 150, 243, 0.05);
}

.drop-zone-between {
  background-color: rgba(56, 228, 222, 0.05);
  border: 2px dashed rgb(43, 58, 158)
}

.edit-container .v-field {
  min-height: 40px !important;
  transition: all 0.2s ease;
}

.edit-container .v-field__input {
  min-height: 32px !important;
  padding: 0 12px !important;
  font-size: 0.875rem !important;
}

.edit-container .v-btn {
  min-width: 32px !important;
  width: 32px !important;
  height: 32px !important;
  transition: all 0.2s ease;
}

.edit-container .v-btn:hover {
  transform: scale(1.1);
}

.add-rapido-container .v-field {
  min-height: 40px !important;
}

.add-rapido-container .v-field__input {
  min-height: 32px !important;
  padding: 0 12px !important;
  font-size: 0.875rem !important;
}

.add-rapido-container .v-btn {
  min-width: 32px !important;
  width: 32px !important;
  height: 32px !important;
}

.add-rapido-container .v-btn:hover {
  transform: scale(1.1);
}

:deep(.v-tooltip__content) {
  background: rgba(0, 0, 0, 0.8);
  font-size: 0.75rem;
  padding: 4px 8px;
}

.item-container[draggable="true"] {
  cursor: move;
}

.item-container[draggable="true"]:active {
  cursor: grabbing;
}

.drop-zone-active .item-container:not(.dragging-item) {
  transition: transform 0.3s ease;
}

.drop-zone-active .item-container:not(.dragging-item):hover {
  transform: translateY(-2px);
}
.btn-delete-cronograma {
  min-width: 40px !important;
  width: 40px !important;
  padding: 0 8px !important;
}

.btn-delete-cronograma .v-icon {
  margin: 0 !important;
}

.d-flex.gap-2 .v-btn {
  height: 36px !important;
}
</style>