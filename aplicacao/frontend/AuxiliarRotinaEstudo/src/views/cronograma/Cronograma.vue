
<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import AdicionarItensForm from './AdicionarItensForm.vue'; 

import {
  verificarCronogramaDoUsuarioExiste,
  buscarCronogramaDoUsuario,
  criarCronograma,
  adicionarItemCronograma
} from '@/api/CronogramaService';
import { deletarItem as del, atualizarItem }  from '@/api/ItemCronogramaService';
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

const estado = ref<Estado>('semCronograma');
const modoAdicao = ref<ModoAdicao>('criar');
const loading = ref(false);

const cronograma = ref<CronogramaResponseInterface | null>(null);
const itensTemporarios = ref<ItemCronogramaInterface[]>([]);

const nomeDisciplinaInline = ref('');
const diaAdicionando = ref<DiasSemana | null>(null);

const modalFormAddItens = ref(false);

const itemHoverId = ref<number | null>(null);

const itemEditandoId = ref<number | null>(null);
const textoEditando = ref('');
const itemArrastando = ref<{id: number, nomeDisciplina:string} | null>(null);

const textoEditandoOriginal = ref('');

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

function getItensDoDia(dia: DiasSemana) {
  return itensDoDia.value.filter(item => item.diaSemana === dia);
};

function iniciarCriacaoCronograma() {
  modoAdicao.value = 'criar';
  itensTemporarios.value = [];
  modalFormAddItens.value = true;
};

//adicionar itens novos
function iniciarAdicaoItens() {
  modoAdicao.value = 'adicionar';
  itensTemporarios.value = [];
  modalFormAddItens.value = true;
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

async function finalizarAdicaoItens() {
  loading.value = true;
  try {
    if (modoAdicao.value === 'criar') {
      const cronogramaDTO: CronogramaInterface = { itensDoDia: itensTemporarios.value };
      cronograma.value = await criarCronograma(cronogramaDTO);
    } else {
      if (!cronograma.value) throw new Error('Cronograma não encontrado');
      cronograma.value = await adicionarItemCronograma(cronograma.value.id, { itensDoDia: itensTemporarios.value });
    }
   
    estado.value = 'visualizando';
    modalFormAddItens.value = false;
    itensTemporarios.value = [];
  
    atualizarMapItens();
  } catch (err) {
    console.error(err);
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

async function carregarCronograma() {
  loading.value = true;
  try {
    const existe = await verificarCronogramaDoUsuarioExiste();
   
    if (existe) {
      cronograma.value = await buscarCronogramaDoUsuario();
      estado.value = 'visualizando';
      atualizarMapItens();
    } else estado.value = 'semCronograma';
  
  } catch (err) {
    console.error('Erro ao carregar cronograma:', err);
    estado.value = 'semCronograma';
  } finally {
    loading.value = false;
  }
};

async function deletarItem(idItem: number){
  try {
    await del(idItem);
    
    if(cronograma.value) {
      itensMap.value.delete(idItem);
      cronograma.value.itensDoDia = cronograma.value.itensDoDia.filter(
        item => item.id !== idItem
      );
    }

  } catch (error) {
    console.error('Erro ao remover item:', error);
    alert('Erro ao remover disciplina. Tente novamente.');
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
    return;
  }
  loading.value = true;
  try {
  
    const targetItem = itensMap.value.get(idItem);
    if(targetItem && cronograma.value){
      targetItem.nomeDisciplina = textoEditando.value.trim();

      await atualizarItem(idItem, {...targetItem, nomeDisciplina: textoEditando.value.trim()} );
  
    }
  } catch (error) {
    console.error('Erro ao editar item:', error);
    alert('Erro ao editar disciplina. Tente novamente.');
    
    await carregarCronograma();
  } finally {
    loading.value = false;
    cancelarEdicao();
  }
};

//arrastar item
function iniciarArrastas(item: ItemCronogramaResponseInterface, event: DragEvent ){
  if(itemEditandoId.value) return;

  itemArrastando.value = {
    id: item.id,
    nomeDisciplina: item.nomeDisciplina
  };

  if(event.dataTransfer){
    event.dataTransfer.effectAllowed = 'move';
    event.dataTransfer.setData("text/plain",  item.id.toString());
  }
};

function finalizarArrastar(){
  itemArrastando.value = null;
};

function permitirDrop(event: DragEvent){
  event.preventDefault();
  if(event.dataTransfer){
    event.dataTransfer.dropEffect = 'move';
  }
};

async function soltarItem(novoDia: DiasSemana, event: DragEvent){
  event.preventDefault();
  if(!itemArrastando.value) return;
  const idItem = itemArrastando.value.id;

  try {
    const targetItem = itensMap.value.get(idItem);
    if(targetItem && cronograma.value){
      targetItem.diaSemana = novoDia;

      await atualizarItem(idItem, {...targetItem, diaSemana: novoDia } );
    }

  } catch (error) {
    console.error('Erro ao mover item:', error);
    alert('Erro ao mover disciplina. Tente novamente.');
    
    await carregarCronograma();
  } finally {
    itemArrastando.value = null;
  }
};

// adição rápida de item no dia desejado
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
  if (!nomeDisciplinaInline.value.trim() || !cronograma.value) return;

  const novoItem = { nomeDisciplina: nomeDisciplinaInline.value, diaSemana: dia };
  const atualizado = await adicionarItemCronograma(cronograma.value.id, { itensDoDia: [novoItem] });
  
  cronograma.value = atualizado;
  diaAdicionando.value = null;
  nomeDisciplinaInline.value = '';

  atualizarMapItens();
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
          <v-btn
            v-if="estado === 'visualizando'"
            color="primary"
            prepend-icon="mdi-plus"
            @click="iniciarAdicaoItens"
          >
            Adicionar Itens
          </v-btn>
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
                  <!-- arrastaavel -->
                  <div 
                    class="d-flex flex-wrap align-center gap-2 drop-zone"
                    :class="{'drop-zone-active': itemArrastando}"
                    @dragover="permitirDrop($event)"
                    @drop="soltarItem(dia.valor, $event)"
                  >
                    <!-- itens atuais-->
                    <div 
                      v-for="item in getItensDoDia(dia.valor)" 
                      :key="item.id" 
                      class="item-container"
                      @mouseenter="itemHoverId = item.id"
                      @mouseleave="itemHoverId = null"
                      @dblclick="iniciarEdicaoItem(item)"
                      draggable="true"
                      @dragstart="iniciarArrastas(item, $event)"
                      @dragend="finalizarArrastar"
                    >
                      <!-- editar -->
                      <div v-if="itemEditandoId === item.id" class="edit-container">
                        <v-text-field
                          v-model="textoEditando"
                          variant="outlined"
                          density="compact"
                          hide-details
                          class="edit-field"
                          style="min-width: 180px; max-width: 200px;"
                          @keyup.enter="salvarEdicaoItem(item.id)"
                          @keyup.esc="cancelarEdicao"
                          @blur="cancelarEdicao"
                          autofocus
                          rounded
                        />
                        <v-tooltip
                          location="bottom"
                          activator="parent"
                          open-delay="500"
                          :model-value="true"
                          persistent
                        >
                          <span class="d-flex align-center">
                            <v-icon small class="mr-1">mdi-keyboard-return</v-icon>
                            Aperte Enter para salvar
                          </span>
                        </v-tooltip>
                      </div>
                      
                      <!-- chip -->
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
                      <div class="add-rapido-container">
                        <v-text-field
                          v-model="nomeDisciplinaInline"
                          variant="outlined"
                          density="compact"
                          hide-details
                          placeholder="Nova disciplina"
                          style="min-width: 180px; max-width: 200px;"
                          @keyup.enter="salvarDisciplinaRapida(dia.valor)"
                          @keyup.esc="cancelarDisciplinaRapida"
                          @blur="cancelarDisciplinaRapida"
                          autofocus
                          rounded
                        />

                        <v-tooltip
                          location="bottom"
                          activator="parent"
                          open-delay="500"
                          :model-value="true"
                          persistent
                        >
                          <span class="d-flex align-center">
                            <v-icon small class="mr-1">mdi-keyboard-return</v-icon>
                            Aperte Enter para salvar
                          </span>
                        </v-tooltip>
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
}

.chip-hover {
  transform: scale(1.02);
  transition: transform 0.2s ease;
}

.delete-btn {
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.3);
  min-width: 24px !important;
  height: 24px !important;
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

@keyframes pulse {
  0% { opacity: 0.7; }
  50% { opacity: 1; }
  100% { opacity: 0.7; }
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

:deep(.v-tooltip__content) {
  background: rgba(0, 0, 0, 0.8);
  font-size: 0.75rem;
  padding: 4px 8px;
}

:deep(.edit-field .v-field),
:deep(.add-rapido-container .v-field) {
  padding-top: 0;
  padding-bottom: 0;
  min-height: 40px;
}

:deep(.edit-field .v-field__input),
:deep(.add-rapido-container .v-field__input) {
  min-height: 32px;
  padding: 0 12px;
  font-size: 0.875rem;
}
</style>