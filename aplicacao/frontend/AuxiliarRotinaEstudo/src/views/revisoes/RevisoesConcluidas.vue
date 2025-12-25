<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { carregarRevisoesConcluidasPaginado } from '@/api/RevisaoService';
import { useRevisaoPaginada } from '@/composables/useRevisaoPaginada';
import type { RevisaoResponseInterface } from '@/types';
import { onMounted } from 'vue';
import { ref, watch } from 'vue';

const props = defineProps<{
  key?: number
}>();

const {
  items: revisoesDaPagina,
  page,
  totalPages,
  atualizarPagina,
} = useRevisaoPaginada(carregarRevisoesConcluidasPaginado, 'concluidas', 6);

const emit = defineEmits<{
  (e: 'ver-detalhes', payload: RevisaoResponseInterface): void;
}>();

const nomeDisciplinas = ref<Record<number, string>>({});
const loadingNomes = ref(false);

async function mudarPagina(novaPag: number) {
  await atualizarPagina(novaPag - 1);
};

async function carregarRevisoes(pagina: number = page.value) {
  await atualizarPagina(pagina);
};

function abrirDetalhes(revisao: RevisaoResponseInterface) {
  emit('ver-detalhes', revisao);
}

async function fetchNomeDisciplina(idEstudo: number) {
  if (!idEstudo) return;
  try {
    const nome = await nomeDisciplinaDoEstudo(idEstudo);
    nomeDisciplinas.value[idEstudo] = nome;
  } catch (err) {
    console.error('Erro ao buscar nome da disciplina:', err);
    nomeDisciplinas.value[idEstudo] = '—';
  }
}

const carregarNomesDisciplinas = async () => {
  loadingNomes.value = true;
  try {
    const promessas = revisoesDaPagina.value.map(r => fetchNomeDisciplina(r.idEstudo));
    await Promise.all(promessas);
  } finally {
    loadingNomes.value = false;
  }
};

onMounted(async () => {
  await carregarRevisoes(0);
  await carregarNomesDisciplinas();
});

watch(
  () => revisoesDaPagina.value,
  async () => {
    await carregarNomesDisciplinas();
  },
  { deep: true }
);

watch(
  () => page.value,
  async () => {
    await carregarNomesDisciplinas();
  }
);

watch(
  () => props.key,
  async () => {
    await carregarRevisoes(0);
    await carregarNomesDisciplinas();
  }
);
</script>

<template>
  <v-card variant="flat">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Revisões Concluídas</span>
    </v-card-title>
    
    <v-card-text>
      <div v-if="loadingNomes" class="text-center py-4">
        <v-progress-circular indeterminate size="20" />
        <div class="text-caption mt-2">Carregando revisões...</div>
      </div>

      <v-table v-else-if="revisoesDaPagina.length > 0">
        <thead>
          <tr>
            <th>Data</th>
            <th>Disciplina</th>
            <th>Status</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="revisao in revisoesDaPagina" 
            :key="revisao.id"
            class="revisao-concluida-row"
            @click="abrirDetalhes(revisao)"
            style="cursor: pointer;"
          >
            <td>
              <v-chip color="success">
                {{ revisao.dataRevisao }}
              </v-chip>
            </td>
            <td>
              <v-chip color="primary" variant="flat">
                {{ nomeDisciplinas[revisao.idEstudo] || "Carregando..." }}
              </v-chip>
            </td>
            <td>
              <v-chip color="success">
                Concluída
              </v-chip>
            </td>
          </tr>
        </tbody>
      </v-table>

      <v-card v-else variant="flat" class="text-center py-8">
        <v-card-text>
          <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-checkbox-marked-circle-outline</v-icon>
          <div class="text-h6 text-grey">Nenhuma revisão concluída!</div>
          <div class="text-body-1 text-grey mt-2">
            Conclua algumas revisões para vê-las aqui
          </div>
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
    </v-card-text>
  </v-card>
</template>

<style scoped>
.revisao-concluida-row:hover {
  background-color: rgba(76, 175, 80, 0.04);
}

.gap-1 {
  gap: 4px;
}

.gap-2 {
  gap: 8px;
}

.text-right {
  text-align: right;
}
</style>