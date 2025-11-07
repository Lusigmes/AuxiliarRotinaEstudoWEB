<template>
  <v-container fluid class="pa-6">
    <!-- breadcrumb -->
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template v-slot:divider>
        <v-icon>mdi-chevron-right</v-icon>
      </template>
    </v-breadcrumbs>

    <v-card variant="flat" class="mb-6">
      <v-card-item>
        <template #prepend>
          <v-avatar color="blue" variant="tonal" size="48">
            <v-icon icon="mdi-calendar-week" />
          </v-avatar>
        </template>
        
        <v-card-title class="text-h4">Cronograma Semanal</v-card-title>
        <v-card-subtitle class="text-h6">
          Planeje seus estudos para a semana
        </v-card-subtitle>
        
        <template #append>
          <v-btn color="primary" prepend-icon="mdi-plus">
            Adicionar Disciplina
          </v-btn>
        </template>
      </v-card-item>
    </v-card>

    <v-card variant="flat" elevation="2">
      <v-card-text class="text-center pa-8">
        <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-calendar-text</v-icon>
        <div class="text-h6 text-grey">Cronograma em desenvolvimento</div>
        <div class="text-body-1 text-grey mt-2">
          Em breve você poderá planejar sua semana aqui!
        </div>
      </v-card-text>
    </v-card>
  </v-container>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const breadcrumbs = computed(() => [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' },
  { title: 'Cronograma', disabled: true }
]);

function navigateTo(destino: string) {
  router.push(`/tela-principal/${destino}`);
}
</script>