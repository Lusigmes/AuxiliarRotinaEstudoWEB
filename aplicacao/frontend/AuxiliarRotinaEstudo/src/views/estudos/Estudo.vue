<script setup lang="ts">
import { computed, ref, onMounted } from 'vue';
import { criarEstudo, atualizarEstudo, deletarEstudo as del, listarEstudosUsuarioPaginado } from '@/api/EstudoService';
import type { EstudoInterface, EstudoResponseInterface } from '@/types';
import { converterStringParaData } from '@/utils/dateUtils';
import AdicionarEstudoForm from './AdicionarEstudoForm.vue';
import { usePagination } from '@/composables/usePagination';

const breadcrumbs = computed(() => [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Estudo', disabled: true }
]);

const estado = ref<'semEstudos' |'visualizando'>('visualizando');
// const loading = ref(false);
// const estudos = ref<EstudoResponseInterface[]>([]);
const removerEstudo = ref<number | null>(null);
const modalAddEstudo = ref(false);

const editandoEstudoId = ref<number | null>(null);
const textoEditando = ref({
  nomeDisciplina: '',
  tema:'',
  tempoDeEstudo: 0,
  diaDoEstudo: ''
});

// paginando
const {
  items: estudos,
  page,
  totalPages,
  loading,
  atualizarPagina
} = usePagination<EstudoResponseInterface>(listarEstudosUsuarioPaginado, 6);

const mudarPagina = async (novaPagina: number) => {
  await atualizarPagina(novaPagina - 1); 
};

async function carregarEstudos(pagina: number = page.value) {
  await atualizarPagina(pagina);
  estado.value = estudos.value.length === 0 ? 'semEstudos' : 'visualizando';
  atualizarEstudoMap();

}
//
const estudosMap = ref(new Map<number, EstudoResponseInterface>());

function atualizarEstudoMap(){
  if(estudos.value){
    estudosMap.value.clear();
    estudos.value.forEach(estudo => {
      estudosMap.value.set(estudo.id, estudo);
    });
  }
};

const estudosOrdenados = computed(() => {
  return [...estudos.value].sort((a, b) => {
    const dataA = converterStringParaData(a.diaDoEstudo);
    const dataB = converterStringParaData(b.diaDoEstudo);
    return dataB.getTime() - dataA.getTime();
  });
});

// async function carregarEstudos(){
//   loading.value = true;
//   try {
//     estudos.value = await listarEstudosUsuario();

//     estado.value = estudos.value.length === 0 ? 'semEstudos' : 'visualizando';
//     atualizarEstudoMap();
//   } catch (error) {
//     console.error('Erro ao carregar estudos:', error);
//     alert('Erro ao carregar estudos. Tente novamente.');
//   }finally{
//     loading.value = false;
//   }
// };

async function salvarEstudo(estudo: EstudoInterface){
  loading.value = true;
  try {
    const estudoNovo = await criarEstudo(estudo);

    estudos.value.push(estudoNovo);
    estudosMap.value.set(estudoNovo.id, estudoNovo);

    estado.value = 'visualizando';
    modalAddEstudo.value = false;

    await carregarEstudos(0);
  } catch (error) {
    console.error('Erro ao adicionar estudo:', error);
    alert('Erro ao adicionar estudo. Tente novamente.');
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
    estudos.value = estudos.value.filter(estudo => estudo.id !== id);
    estudosMap.value.delete(id);
    
    await del(id);
    
    await carregarEstudos();
    
  } catch (error) {
    console.error('Erro ao deletar estudo:', error);
    
    estudos.value = estudosBackup;
    estado.value = estudosBackup.length === 0 ? 'semEstudos' : 'visualizando';
    
    alert('Erro ao deletar estudo. Tente novamente.');
  } finally {
    removerEstudo.value = null;
  }
}

function iniciarAdicaoEstudo(){
  modalAddEstudo.value = true;
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
    alert('Disciplina e tema são obrigatórios');
    return;
  }

  loading.value = true;
  try {
    const targetEstudo = estudosMap.value.get(idEstudo);

    if(targetEstudo){
      targetEstudo.nomeDisciplina = textoEditando.value.nomeDisciplina.trim();
      targetEstudo.tema = textoEditando.value.tema.trim();
      targetEstudo.tempoDeEstudo = textoEditando.value.tempoDeEstudo;
      targetEstudo.diaDoEstudo = textoEditando.value.diaDoEstudo;

      await atualizarEstudo(idEstudo, {
        nomeDisciplina: textoEditando.value.nomeDisciplina.trim(),
        tema: textoEditando.value.tema.trim(),
        tempoDeEstudo: textoEditando.value.tempoDeEstudo,
        diaDoEstudo: textoEditando.value.diaDoEstudo,
      });
      cancelarEdicao();
      await carregarEstudos();
    }
  } catch (error) {
    console.error('Erro ao editar estudo:', error);
    alert('Erro ao editar estudo. Tente novamente.');
    await carregarEstudos(); 
  } finally {
    loading.value = false;
  }
};

onMounted( async () => {
  carregarEstudos(0);
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
          {{ estado === 'semEstudos' ? 'Comece registrando seus estudos' : 'Acompanhe seus estudos diários' }}
        </v-card-subtitle>

        <template #append>
          <v-btn
            v-if="estado === 'visualizando'"
            color="primary"
            prepend-icon="mdi-plus"
            @click="iniciarAdicaoEstudo"
          >
            Adicionar Estudo
          </v-btn>
        </template>
      </v-card-item>
    </v-card>

    <!-- SEM ESTUDOS -->
    <div v-if="estado === 'semEstudos'">
      <v-card variant="flat" class="text-center py-12">
        <v-card-item>
          <v-avatar color="green-lighten-5" size="120" class="mb-4">
            <v-icon icon="mdi-book-plus" size="64" color="green" />
          </v-avatar>
          <v-card-title class="text-h3 mb-2">Registre Seu Primeiro Estudo</v-card-title>
          <v-card-subtitle class="text-h6 mb-6">
            Comece acompanhando seu progresso de estudos
          </v-card-subtitle>
          <v-btn
            color="primary"
            size="x-large"
            prepend-icon="mdi-plus"
            @click="iniciarAdicaoEstudo"
            :loading="loading"
          >
            Registrar Estudo
          </v-btn>
        </v-card-item>
      </v-card>
    </div>

    <!-- VISUALIZANDO -->
    <div v-else-if="estado === 'visualizando'">
      <v-card variant="flat" elevation="2">
        <v-card-title class="d-flex justify-space-between align-center">
          <!-- <span>Meus Estudos</span> -->
          <!-- <div class="d-flex align-center gap-2">
            <v-chip color="primary" variant="flat">
              Estudo(s): {{ estudos.length }}
            </v-chip>
          </div> -->
        </v-card-title>

        <v-card-text>
          <!-- COM ESTUDOS -->
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
                <!-- DISCIPLINA -->
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

                <!-- TEMA -->
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

                <!-- TEMPO -->
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

                <!-- DATA -->
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

                <!-- AÇÕES -->
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
        v-if="totalPages > 1 && Number.isInteger(totalPages)"
      >
        <v-pagination
          :length="totalPages"
          :model-value="page + 1"
          @update:model-value="mudarPagina"
          color="primary"
          size="small"
          rounded
          :show-first-last-page="true"   
          :total-visible="0"   
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