<template>
  <div class="home">
    <h1>Bem-vindo ao Sistema de Estudos</h1>
    <p>Sistema de gerenciamento de estudos e revisões</p>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue';

// Componente Home básico
onMounted(async () => {
  console.log("ok");
});
</script>

<style scoped>
.home {
  text-align: center;
  padding: 2rem;
}
</style>