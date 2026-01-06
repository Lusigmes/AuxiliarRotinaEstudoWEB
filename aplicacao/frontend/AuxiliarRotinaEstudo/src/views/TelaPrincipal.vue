<template>
    <router-view></router-view>
</template>

<script setup lang="ts">
// pode conter outras funcionalidades (extras) que eu quiser

import { useRouter } from 'vue-router';

const router = useRouter();

function navigateTo(destino: string) {
  router.push(`/tela-principal/${destino}`);
}



</script>