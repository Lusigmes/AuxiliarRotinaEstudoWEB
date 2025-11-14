<template>
  <v-app>
    <router-view></router-view>

    <v-fab-transition>
      <!-- <v-btn
        v-if="$route.name === 'Dashboard'"
        color="primary"
        size="large"
        icon
        fab
        fixed
        top
        left
        @click="navigateTo('registro-estudo')"
      >
        <v-icon size="28">mdi-plus</v-icon>
      </v-btn> -->
    </v-fab-transition>
  </v-app>
</template>

<script setup lang="ts">
// pode conter outras funcionalidades (extras) que eu quiser

import { useRouter } from 'vue-router';

const router = useRouter();

function navigateTo(destino: string) {
  router.push(`/tela-principal/${destino}`);
}



</script>