<template>
  <v-container fluid class="pa-6">
    <v-breadcrumbs :items="breadcrumbs" class="px-0 mb-4">
      <template v-slot:divider>
        <v-icon>mdi-chevron-right</v-icon>
      </template>
    </v-breadcrumbs>

    <v-row class="mb-6">
      <v-col cols="12" sm="6" md="3">
        <v-card 
          class="dashboard-card" 
          variant="flat" 
          color="blue-lighten-5"
          @click="navigateTo('cronograma')"
        >
          <v-card-item>
            <template #prepend>
              <v-avatar color="blue" variant="tonal" size="48">
                <v-icon icon="mdi-calendar-week" />
              </v-avatar>
            </template>
            
            <v-card-title class="text-h6">Cronograma</v-card-title>
            <v-card-subtitle>Planeje sua semana</v-card-subtitle>
          </v-card-item>
        </v-card>
      </v-col>
    
      <v-col cols="12" sm="6" md="3">
        <v-card 
          class="dashboard-card" 
          variant="flat" 
          color="green-lighten-5"
          @click="navigateTo('estudo')"

        >
          <v-card-item>
            <template #prepend>
              <v-avatar color="green" variant="tonal" size="48">
                <v-icon icon="mdi-book-education" />
              </v-avatar>
            </template>
            
            <v-card-title class="text-h6">Estudos</v-card-title>
            <v-card-subtitle>Registre seu estudo diário</v-card-subtitle>
          </v-card-item>
        </v-card>
      </v-col>

      <v-col cols="12" sm="6" md="3">
        <v-card 
          class="dashboard-card"
          variant="flat"
          color="orange-lighten-5" 
          @click="navigateTo('revisao')">
          <v-card-item>
            <template #prepend>
              <v-avatar color="orange" variant="tonal" size="48">
                <v-icon icon="mdi-calendar-check" />
              </v-avatar>
            </template>
            <v-card-title class="text-h6">Revisões</v-card-title>
            <v-card-subtitle>Gerencie suas revisões</v-card-subtitle>
          </v-card-item>
        </v-card>
      </v-col>

      
    </v-row>


    <v-row>
      <v-col cols="12" md="8">
        <!-- Estudos de Hoje -->
      </v-col>
      <v-col cols="12" md="4">
        <!-- Revisões Urgentes -->
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts"> 

import { computed } from 'vue';
import { useRouter } from 'vue-router';

const router = useRouter();

const breadcrumbs = computed(() => [
  { title: 'Dashboard', disabled: false, to: '/tela-principal' }
]);

function navigateTo(destino: string) {
  router.push(`/tela-principal/${destino}`);
}
</script>




<style scoped>
.dashboard-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.dashboard-card:hover {
  transform: translateY(-4px);
  border-color: rgb(var(--v-theme-primary));
}
</style>