<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { criarEstudo, atualizarEstudo, deletarEstudo as del, listarEstudosUsuarioPaginado } from '@/api/EstudoService';
import type { EstudoInterface, EstudoResponseInterface } from '@/types';
import { converterStringParaData } from '@/utils/dateUtils';
import AdicionarEstudoForm from './AdicionarEstudoForm.vue';
import { usePagination } from '@/composables/usePagination';
import { useNotification } from '@/composables/useNotification';
import { useEstudoBuscaPor } from '@/composables/useEstudoBuscaPor';

const breadcrumbs = computed(() => [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Estudo', disabled: true }
]);

const estado = ref<'semEstudos' |'visualizando'>('visualizando');
const removerEstudo = ref<number | null>(null);
const modalAddEstudo = ref(false);

const editandoEstudoId = ref<number | null>(null);
const textoEditando = ref({
  nomeDisciplina: '',
  tema:'',
  tempoDeEstudo: 0,
  diaDoEstudo: ''
});

const todosEstudosMap = ref(new Map<number, EstudoResponseInterface>());

const {
  items: estudosPaginados,
  page,
  totalPages,
  loading,
  atualizarPagina
} = usePagination<EstudoResponseInterface>(listarEstudosUsuarioPaginado, 6);

const {
  termoBusca,
  emBusca,
  resultadosBusca,
  pageBusca,
  totalPagesBusca,
  totalElementosBusca,
  loadingBusca,
  buscar,
  limparBusca,
  mudarPaginaBusca, recarregarBuscaAtual 
} = useEstudoBuscaPor();

const { showNotification } = useNotification();

const estudos = computed(() => {
  if (emBusca.value) {
    return resultadosBusca.value;
  }
  
  const estudosDaPagina: EstudoResponseInterface[] = [];
  
  estudosPaginados.value.forEach(estudoPaginado => {
    const estudoCompleto = todosEstudosMap.value.get(estudoPaginado.id);
    if (estudoCompleto) {
      estudosDaPagina.push(estudoCompleto);
    } else {
      estudosDaPagina.push(estudoPaginado);
      todosEstudosMap.value.set(estudoPaginado.id, estudoPaginado);
    }
  });
  
  return estudosDaPagina;
});

const paginaAtual = computed(() => {
  return emBusca.value ? pageBusca.value : page.value;
});

const totalPaginasAtual = computed(() => {
  return emBusca.value ? totalPagesBusca.value : totalPages.value;
});

const loadingAtual = computed(() => {
  return emBusca.value ? loadingBusca.value : loading.value;
});

const mudarPagina = async (novaPagina: number) => {
  if (emBusca.value) {
    await mudarPaginaBusca(novaPagina - 1);
  } else {
    await atualizarPagina(novaPagina - 1);
  }
};

async function carregarEstudos(pagina: number = page.value) {
  await atualizarPagina(pagina);
  
  estudosPaginados.value.forEach(estudo => {
    todosEstudosMap.value.set(estudo.id, estudo);
  })
  
  estado.value = estudosPaginados.value.length === 0 ? 'semEstudos' : 'visualizando';
};

const estudosOrdenados = computed(() => {
  const estudosParaOrdenar = [...estudos.value];
  
  return estudosParaOrdenar.sort((a, b) => {
    const dataA = converterStringParaData(a.diaDoEstudo);
    const dataB = converterStringParaData(b.diaDoEstudo);
    return dataB.getTime() - dataA.getTime();
  })
});

const mostrarTooltipDoBotao = ref(false);

const executarBusca = async () => {
  if (!termoBusca.value.trim() && emBusca.value) {
    await limparBusca();
    showNotification('Busca limpa', 'info');
    return;
  }
  
  if (!termoBusca.value.trim()) {
    return;
  }
  
  if (emBusca.value) {
    return;
  }
  
  const sucesso = await buscar(termoBusca.value, 0);
  if (sucesso) {
    if (resultadosBusca.value.length === 0) {
      showNotification(`Nenhum estudo encontrado para "${termoBusca.value}"`, 'info');
    } else {
      showNotification(`Encontrados ${totalElementosBusca.value} estudo(s)`, 'success');
    }
    mostrarTooltipDoBotao.value = true
    setTimeout(() => {
      mostrarTooltipDoBotao.value = false
    }, 5000);
  }
}

async function salvarEstudo(estudo: EstudoInterface){
  loading.value = true;
  try {
    const estudoNovo = await criarEstudo(estudo);

    todosEstudosMap.value.set(estudoNovo.id, estudoNovo);
    
    estado.value = 'visualizando';
    modalAddEstudo.value = false;

    await carregarEstudos(0);
    
    showNotification('Estudo registrado com sucesso!', 'success');
  } catch (error) {
    console.error('Erro ao adicionar estudo:', error);
    showNotification('Erro ao adicionar estudo. Tente novamente.', 'error');
  }finally{
    loading.value = false;
  }
};

async function deletarEstudo(id: number){
  if (!confirm('Tem certeza que deseja excluir este estudo?')) {
    return;
  }
  
  removerEstudo.value = id;
  const estudosBackup = [...estudos.value];
  
  try {
    todosEstudosMap.value.delete(id);
    await del(id);
    
    if (emBusca.value) {
      await recarregarBuscaAtual();
      
      if (resultadosBusca.value.length === 0 && pageBusca.value > 0) {
        await buscar(termoBusca.value, pageBusca.value - 1);
      }
    } else {
      await carregarEstudos();
      
      if (page.value > 0 && estudosPaginados.value.length === 0 && totalPages.value > 1) {
        await carregarEstudos(page.value - 1);
      }
    }
    
    showNotification('Estudo excluído com sucesso!', 'success');
    
  } catch (error) {
    console.error('Erro ao deletar estudo:', error);
    
    const estudoBackup = estudosBackup.find(e => e.id === id);
    if (estudoBackup) {
      todosEstudosMap.value.set(id, estudoBackup);
    }
    
    showNotification('Erro ao excluir estudo. Tente novamente.', 'error');
  } finally {
    removerEstudo.value = null;
  }
};

function iniciarAdicaoEstudo(){
  modalAddEstudo.value = true;
  showNotification('Adicionando novo estudo', 'info');
};

function fecharModalAdicaoEstudo(){
  modalAddEstudo.value = false;
};

function iniciarEdicaoEstudo(estudo: EstudoResponseInterface){
  editandoEstudoId.value = estudo.id;
  textoEditando.value = {
    nomeDisciplina: estudo.nomeDisciplina,
    tema: estudo.tema,
    tempoDeEstudo: estudo.tempoDeEstudo,
    diaDoEstudo: estudo.diaDoEstudo
  };
  showNotification('Editando estudo - Pressione Enter para salvar ou Esc para cancelar', 'info');
};

function cancelarEdicao(){
  editandoEstudoId.value = null;
  textoEditando.value = {
    nomeDisciplina: '',
    tema:'',
    tempoDeEstudo: 0,
    diaDoEstudo: ''
  };
};

async function salvarEdicaoEstudo(idEstudo: number){
  if(!textoEditando.value.nomeDisciplina.trim() || !textoEditando.value.tema.trim()){
    showNotification('Disciplina e tema são obrigatórios', 'warning');
    return;
  }

  if(textoEditando.value.tempoDeEstudo < 1 || textoEditando.value.tempoDeEstudo > 8){
    showNotification('Tempo de estudo deve ser entre 1 e 8 horas', 'warning');
    return;
  }

  loading.value = true;
  try {
    const targetEstudo = todosEstudosMap.value.get(idEstudo);
    
    if(targetEstudo){
      const estudoAtualizado = {
        ...targetEstudo,
        nomeDisciplina: textoEditando.value.nomeDisciplina.trim(),
        tema: textoEditando.value.tema.trim(),
        tempoDeEstudo: textoEditando.value.tempoDeEstudo,
        diaDoEstudo: textoEditando.value.diaDoEstudo,
      };
      
      todosEstudosMap.value.set(idEstudo, estudoAtualizado);
      
      await atualizarEstudo(idEstudo, {
        nomeDisciplina: textoEditando.value.nomeDisciplina.trim(),
        tema: textoEditando.value.tema.trim(),
        tempoDeEstudo: textoEditando.value.tempoDeEstudo,
        diaDoEstudo: textoEditando.value.diaDoEstudo,
      });
      
      cancelarEdicao();
      await carregarEstudos(); 
      
      showNotification('Estudo atualizado com sucesso!', 'success');
    }
  } catch (error) {
    console.error('Erro ao editar estudo:', error);
    showNotification('Erro ao editar estudo. Tente novamente.', 'error');
    await carregarEstudos(); 
  } finally {
    loading.value = false;
  }
};

onMounted( async () => {
  await carregarEstudos(0);
  showNotification('Estudos carregados com sucesso!', 'success');
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
          <v-avatar color="green" variant="tonal" size="48">
            <v-icon icon="mdi-book-education" />
          </v-avatar>
        </template>

        <v-card-title class="text-h4">Registro de Estudos</v-card-title>
        <v-card-subtitle class="text-h6">
          <span v-if="emBusca && resultadosBusca.length > 0">
            {{ totalElementosBusca }} estudo(s) encontrado(s) para "{{ termoBusca }}"
          </span>
          <span v-else-if="emBusca && resultadosBusca.length === 0">
            Nenhum resultado para "{{ termoBusca }}"
          </span>
          <span v-else>
            {{ estado === 'semEstudos' ? 'Comece registrando seus estudos' : 'Acompanhe seus estudos diários' }}
          </span>
        </v-card-subtitle>

        <template #append>
          <div class="d-flex align-center gap-2">
            <!-- BUSCA -->
            <v-text-field
              v-model="termoBusca"
              placeholder="Buscar..."
              variant="outlined"
              density="compact"
              hide-details
              prepend-inner-icon="mdi-magnify"
              style="width: 180px;"
              @keyup.enter="executarBusca"
            >
            <template v-if="emBusca" #append-inner>
              <v-tooltip
                v-model="mostrarTooltipDoBotao"
                location="bottom"
                :open-delay="0"
                :close-delay="5000"
              >
                <template #activator="{ props }">
                  <v-btn
                    icon
                    size="x-small"
                    variant="text"
                    color="grey"
                    @click="limparBusca"
                    class="ml-1"
                    :disabled="loadingBusca"
                    v-bind="props"
                  >
                    <v-icon size="small">mdi-close</v-icon>
                  </v-btn>
                </template>
                <span class="d-flex align-center">
                  Clique em 'x' para limpar a busca e voltar à listagem geral
                </span>
              </v-tooltip>
            </template>
            </v-text-field>
            <v-btn
              icon
              variant="text"
              color="primary"
              @click="executarBusca"
              :loading="loadingBusca"
              :disabled="!termoBusca.trim()"
              class="ml-1"
            >
              <v-icon>mdi-magnify</v-icon>
            </v-btn>

            <v-btn
              color="primary"
              prepend-icon="mdi-plus"
              @click="iniciarAdicaoEstudo"
              :disabled="loading || loadingBusca"
            >
              Adicionar Estudo
            </v-btn>
          </div>
        </template>
      </v-card-item>
    </v-card>
    
    <!-- SEM ESTUDOS NA BUSCA -->
    <div v-if="emBusca && resultadosBusca.length === 0" class="text-center py-12">
        <v-card variant="flat">
          <v-card-item>
            <v-avatar color="grey-lighten-3" size="80" class="mb-4">
              <v-icon icon="mdi-magnify-close" size="48" color="grey" />
            </v-avatar>
            <v-card-title class="text-h5 mb-2">Nenhum estudo encontrado</v-card-title>
            <v-card-subtitle class="text-h6 mb-6">
              Não encontramos resultados para "{{ termoBusca }}"
            </v-card-subtitle>
            <v-btn
              color="primary"
              variant="outlined"
              @click="limparBusca"
              :loading="loadingBusca"
            >
              <v-icon icon="mdi-arrow-left" class="mr-2" />
              Voltar para todos os estudos
            </v-btn>
          </v-card-item>
        </v-card>
      </div>
  
    <!-- SEM ESTUDOS NORMAL -->
    <div v-else-if="!emBusca && estudosPaginados.length === 0" class="text-center py-12">
      <v-card variant="flat">
        <v-card-item>
          <v-avatar color="grey-lighten-3" size="80" class="mb-4">
            <v-icon icon="mdi-book-open-variant" size="48" color="grey" />
          </v-avatar>
          <v-card-title class="text-h5 mb-2">Nenhum estudo registrado</v-card-title>
          <v-card-subtitle class="text-h6 mb-6">
            Comece registrando seu primeiro estudo
          </v-card-subtitle>
        </v-card-item>
      </v-card>
    </div>

    <div v-else-if="estado === 'visualizando'">
      <v-card variant="flat" elevation="2">
        <v-card-title class="d-flex justify-space-between align-center">

        </v-card-title>

        <v-card-text>
          <v-table>
            <thead>
              <tr>
                <th class="text-left">Disciplina</th>
                <th class="text-left">Tema</th>
                <th class="text-left">Tempo</th>
                <th class="text-left">Data</th>
                <th class="text-left" style="width: 140px;">Ações</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="estudo in estudosOrdenados" :key="estudo.id">
                <td>
                  <div v-if="editandoEstudoId === estudo.id" class="edit-container">
                    <v-text-field
                      v-model="textoEditando.nomeDisciplina"
                      variant="outlined"
                      density="compact"
                      hide-details
                      class="edit-field"
                      placeholder="Nome da disciplina"
                      @keyup.enter="salvarEdicaoEstudo(estudo.id)"
                      @keyup.esc="cancelarEdicao"
                      autofocus
                    />
                  </div>
                  <span v-else class="font-weight-bold">
                    {{ estudo.nomeDisciplina }}
                  </span>
                </td>

                <td>
                  <div v-if="editandoEstudoId === estudo.id" class="edit-container">
                    <v-text-field
                      v-model="textoEditando.tema"
                      variant="outlined"
                      density="compact"
                      hide-details
                      class="edit-field"
                      placeholder="Tema do estudo"
                      @keyup.enter="salvarEdicaoEstudo(estudo.id)"
                      @keyup.esc="cancelarEdicao"
                    />
                  </div>
                  <span v-else>
                    {{ estudo.tema }}
                  </span>
                </td>

                <td>
                  <div v-if="editandoEstudoId === estudo.id" class="edit-container">
                    <v-text-field
                      v-model.number="textoEditando.tempoDeEstudo"
                      type="number"
                      variant="outlined"
                      density="compact"
                      hide-details
                      class="edit-field"
                      style="max-width: 100px;"
                      min="1"
                      max="480"
                      @keyup.enter="salvarEdicaoEstudo(estudo.id)"
                      @keyup.esc="cancelarEdicao"
                    />
                  </div>
                  <v-chip v-else color="blue" variant="flat">
                    {{ estudo.tempoDeEstudo }} h
                  </v-chip>
                </td>

                <td>
                  <div v-if="editandoEstudoId === estudo.id" class="edit-container">
                    <DataInput
                      v-model="textoEditando.diaDoEstudo"
                      variant="outlined"
                      density="compact"
                      hide-details
                      class="edit-field"
                      style="max-width: 150px;"
                      @update:valorData="textoEditando.diaDoEstudo = $event"
                      @keyup.enter="salvarEdicaoEstudo(estudo.id)"
                      @keyup.esc="cancelarEdicao"
                    />
                  </div>
                  <span v-else>
                    {{ estudo.diaDoEstudo }}
                  </span>
                </td>

                <td>
                  <div class="d-flex align-center gap-1">
                    <!-- BOTÃO PARA CONCLUIR/FINALIZAR EDIÇÃO -->
                    <template v-if="editandoEstudoId === estudo.id">
                      <div>
                      <v-btn
                          icon
                          size="small"
                          variant="text"
                          color="green"
                          @click="salvarEdicaoEstudo(estudo.id)"
                          :loading="loading"
                        >
                          <v-icon>mdi-check</v-icon>
                        </v-btn>
                        <v-tooltip
                          location="bottom"
                          activator="parent"
                          open-delay="500"
                        >
                          <span class="d-flex align-center">
                            <v-icon small class="mr-1">mdi-keyboard-return</v-icon>
                            Aperte Enter para salvar
                          </span>
                        </v-tooltip>
                      </div>
                      <div>
                        <v-btn
                          icon
                          size="small"
                          variant="text"
                          color="error"
                          @click="cancelarEdicao"
                          :disabled="loading"
                        >
                          <v-icon>mdi-close</v-icon>
                        </v-btn>
                        <v-tooltip
                          location="bottom"
                          activator="parent"
                          open-delay="500"
                        >
                          <span class="d-flex align-center">
                            <v-icon small class="mr-1">mdi-escape</v-icon>
                            Aperte Esc para cancelar
                          </span>
                        </v-tooltip>
                      </div>
                    </template>

                    <!-- BOTÕES AÇÃO BASE -->
                    <template v-else>
                      <v-btn
                        icon
                        size="small"
                        variant="text"
                        color="primary"
                        @click="iniciarEdicaoEstudo(estudo)"
                        :disabled="!!editandoEstudoId"
                      >
                        <v-icon>mdi-pencil</v-icon>
                      </v-btn>
                      <v-btn
                        icon
                        size="small"
                        variant="text"
                        color="red"
                        @click="deletarEstudo(estudo.id)"
                        :loading="loading && removerEstudo === estudo.id"
                        :disabled="!!editandoEstudoId"
                      >
                        <v-icon>mdi-delete</v-icon>
                      </v-btn>
                    </template>
                  </div>
                </td>
              </tr>
            </tbody>
          </v-table>
        </v-card-text>
      </v-card>

      <v-row 
        justify="center" 
        class="mt-4" 
        v-if="totalPaginasAtual > 1 && Number.isInteger(totalPaginasAtual)"
      >
        <v-pagination
          :length="totalPaginasAtual"
          :model-value="paginaAtual + 1"
          @update:model-value="mudarPagina"
          color="primary"
          size="small"
          rounded
          :show-first-last-page="true"   
          :total-visible="0"
          :disabled="loadingAtual"
        />
      </v-row>
    </div>

    <v-dialog v-model="modalAddEstudo" max-width="800px" persistent>
      <AdicionarEstudoForm
        :loading="loading"
        @adicionar-estudo="salvarEstudo"
        @cancelar="fecharModalAdicaoEstudo"
        @fechar="fecharModalAdicaoEstudo"
      />
    </v-dialog>

    <v-card v-if="loadingBusca && emBusca" variant="flat" class="text-center py-12">
      <v-card-text>
        <v-progress-circular indeterminate color="primary" size="64" />
        <div class="text-h6 mt-4">Buscando estudos...</div>
      </v-card-text>
    </v-card>
    <!-- Loading -->
    <v-card v-if="loading && estudos.length === 0" variant="flat" class="text-center py-12 mt-4">
      <v-card-text>
        <v-progress-circular indeterminate color="primary" size="64" />
        <div class="text-h6 mt-4">Carregando estudos...</div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<style scoped>
.gap-1 {
  gap: 4px;
}

.gap-2 {
  gap: 8px;
}

.edit-container {
  display: flex;
  align-items: center;
}

.edit-field {
  min-width: 120px;
}

span {
  user-select: none;
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
}

:deep(.edit-field .v-field) {
  padding-top: 0;
  padding-bottom: 0;
  min-height: 40px;
}

:deep(.edit-field .v-field__input) {
  min-height: 32px;
  padding: 0 8px;
  font-size: 0.875rem;
}

:deep(.v-tooltip__content) {
  background: rgba(0, 0, 0, 0.8);
  font-size: 0.75rem;
  padding: 4px 8px;
}
</style>