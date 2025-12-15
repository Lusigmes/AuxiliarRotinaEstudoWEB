<script setup lang="ts">
import { nomeDisciplinaDoEstudo } from '@/api/EstudoService';
import { carregarRevisoesAtrasadasPaginado, concluirRevisao } from '@/api/RevisaoService';
import { usePagination } from '@/composables/usePagination';
import type { RevisaoResponseInterface } from '@/types';
import { converterStringParaData } from '@/utils/dateUtils';
import { computed, ref, onMounted, watch } from 'vue';

const {
  items:revisoes,
  page,
  totalPages,
  atualizarPagina
} = usePagination<RevisaoResponseInterface>(carregarRevisoesAtrasadasPaginado, 6);

const emit = defineEmits<{
  (e: 'ver-detalhes', payload: RevisaoResponseInterface): void;
  (e: 'atualizar'): void;
  (e: 'fechar'): void;
}>();

const mudarPagina = async (novaPag: number) => {
  await atualizarPagina(novaPag - 1);
};

async function carregarRevisoes(pagina: number = page.value) {
  await atualizarPagina(pagina);
};

const nomeDisciplinas = ref<Record<number, string>>({});

async function fetchNomeDisciplina(idEstudo: number) {
  if (!idEstudo) return;
  if (nomeDisciplinas.value[idEstudo]) return;
  try {
    const nome = await nomeDisciplinaDoEstudo(idEstudo);
    nomeDisciplinas.value[idEstudo] = nome;
  } catch (err) {
    console.error('Erro ao buscar nome da disciplina:', err);
    nomeDisciplinas.value[idEstudo] = '—';
  }
}

const loading = ref<number | null>(null);

async function concluirRevisaoItem(revisao: RevisaoResponseInterface) {
  loading.value = revisao.id;
  try {
    await concluirRevisao(revisao.id);
  
    const index = revisoes.value.findIndex(r => r.id === revisao.id);
    if (index !== -1) {
      revisoes.value.splice(index, 1); 
      
      if (revisoes.value.length === 0 && page.value > 0) {
        await carregarRevisoes(page.value - 1);
      } else {
        await carregarRevisoes(page.value);
      }
    }
    
    emit('atualizar');
    
  } catch (error) {
    console.error('Erro ao concluir revisão:', error);
    alert('Erro ao concluir revisão');
    await carregarRevisoes(page.value);
  } finally {
    loading.value = null;
  }
};

const revisoesAtrasadas = computed(() => {
  const hoje = new Date();
  hoje.setHours(0, 0, 0, 0);
  
  const resultados = [];
  
  for (const revisao of revisoes.value) {
    if (revisao.concluida) continue;
    
    const dataRevisao = converterStringParaData(revisao.dataRevisao);
    if (dataRevisao >= hoje) continue;
    
    resultados.push(revisao);
  }
  return resultados.sort((a, b) => {
    const dataA = converterStringParaData(a.dataRevisao);
    const dataB = converterStringParaData(b.dataRevisao);
    return dataA.getTime() - dataB.getTime();
  });
});

onMounted(async () => {
  await carregarRevisoes(0);

  const promessas = revisoes.value.map(r => fetchNomeDisciplina(r.idEstudo));
  await Promise.all(promessas);
});

watch(() => revisoes.value, (newVal) => {
  setTimeout(() => {
    newVal.forEach(r => fetchNomeDisciplina(r.idEstudo));
  }, 100);
}, { deep: true });
</script>

<template>
  <v-card variant="flat">
    <v-card-title class="d-flex justify-space-between align-center">
      <span>Revisões Atrasadas</span>
      <v-chip color="error">
        {{ revisoesAtrasadas.length }}
      </v-chip>
    </v-card-title>
    
    <v-card-text>
      <v-table v-if="revisoesAtrasadas.length > 0">
        <thead>
          <tr>
            <th>Data</th>
            <th>Dias de Atraso</th>
            <th>Disciplina</th>
            <th>Status</th>
            <th class="text-right">Ações</th>
          </tr>
        </thead>
        <tbody>
          <tr 
            v-for="revisao in revisoesAtrasadas" 
            :key="revisao.id"
            class="revisao-atrasada-row"
            @click="$emit('ver-detalhes', revisao)"
            style="cursor: pointer;"
          >
            <td>
              <v-chip color="red">
                {{ revisao.dataRevisao }}
              </v-chip>
            </td>
            <td>
              {{ Math.floor((new Date().getTime() - converterStringParaData(revisao.dataRevisao).getTime()) / (1000 * 3600 * 24)) }} dias
            </td>
            <td>
              <v-chip color="primary" variant="flat">
                {{ nomeDisciplinas[revisao.idEstudo] || "Carregando..." }}
              </v-chip>
            </td>
            <td>
              <v-chip color="error">
                Atrasada
              </v-chip>
            </td>
            <td class="text-right">
              <v-btn
                size="small"
                color="success"
                @click.stop="concluirRevisaoItem(revisao)"
                :loading="loading === revisao.id"
              >
                <v-icon icon="mdi-check" class="mr-2" />
                Concluir
              </v-btn>
            </td>
          </tr>
        </tbody>
      </v-table>

      <v-card v-else variant="flat" class="text-center py-8">
        <v-card-text>
          <v-icon size="64" color="success" class="mb-4">mdi-check-circle</v-icon>
          <div class="text-h6 text-grey">Nenhuma revisão atrasada!</div>
          <div class="text-body-1 text-grey mt-2">Todas as revisões estão em dia</div>
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
.revisao-atrasada-row:hover {
  background-color: #ffebee;
}

.text-right {
  text-align: right;
}
</style>