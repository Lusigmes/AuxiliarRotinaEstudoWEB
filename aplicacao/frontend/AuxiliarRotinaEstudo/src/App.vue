<template>
  <v-app>
    <Toolbar/>
    <v-main>
      <router-view></router-view>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import Toolbar from '@/layouts/Toolbar.vue';

</script>