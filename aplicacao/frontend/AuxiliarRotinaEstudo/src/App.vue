<template>
  <v-app>
    <Toolbar v-if="autenticado"/>
    <v-main>
      <router-view></router-view>
    </v-main>
    <NotificationProvider />
    
    <RenovarToken
      :visible="showRenewalModal"
      :segundos-para-expirar="60"
      @renovar="handleTokenRenew"
      @cancelar="handleTokenCancel"
      @update:visible="(value) => showRenewalModal = value"
    />
  </v-app>
</template>

<script setup lang="ts">
import Toolbar from '@/layouts/Toolbar.vue';
import NotificationProvider from '@/components/NotificationProvider.vue';
import RenovarToken from './components/RenovarToken.vue';
import { useAuth } from '@/composables/useAuth';
import { computed } from 'vue';

const { showRenewalModal, handleTokenRenewal, handleTokenRenewalCancel, token  } = useAuth();

const autenticado = computed(() => !!token.value);

const handleTokenRenew = async () => {
  await handleTokenRenewal();
};

const handleTokenCancel = () => {
  handleTokenRenewalCancel();
};
</script>