<script setup lang="ts">
import { computed, ref, watch } from 'vue';
import { useAuth } from '@/composables/useAuth';

const { tokenExpirationTime } = useAuth();
const showDetails = ref(false);

const TOTAL_TOKEN_TIME = 10 * 60 * 1000;

const WARNING_THRESHOLD = 1 * 60 * 1000;

const timeRemaining = computed(() => {
  return tokenExpirationTime.value || 0;
});

const progress = computed(() => {
  if (timeRemaining.value <= 0) return 100;
  return ((TOTAL_TOKEN_TIME - timeRemaining.value) / TOTAL_TOKEN_TIME) * 100;
});

const formattedTime = computed(() => {
  const seconds = Math.floor(timeRemaining.value / 1000);
  if (seconds <= 0) return '00:00';
  
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  
  return `${minutes.toString().padStart(2, '0')}:${remainingSeconds.toString().padStart(2, '0')}`;
});

const isAboutToExpire = computed(() => {
  return timeRemaining.value > 0 && timeRemaining.value <= WARNING_THRESHOLD;
});

const isCritical = computed(() => {
  return timeRemaining.value > 0 && timeRemaining.value <= 30000; 
});

const timerColor = computed(() => {
  if (!timeRemaining.value || timeRemaining.value <= 0) return 'error';
  if (isCritical.value) return 'error';
  if (isAboutToExpire.value) return 'warning';
  return 'success';
});

const timerIcon = computed(() => {
  if (isCritical.value) return 'mdi-clock-alert';
  if (isAboutToExpire.value) return 'mdi-clock-fast';
  return 'mdi-clock-outline';
});

const showTimer = computed(() => {
  return timeRemaining.value > 0 && timeRemaining.value <= TOTAL_TOKEN_TIME;
});

watch(timeRemaining, () => {
  if (showDetails.value) {
    setTimeout(() => {
      showDetails.value = false;
    }, 3000);
  }
});
</script>


<template>
  <div class="token-timer-indicator" v-if="showTimer">
    <div class="timer-wrapper" @click="showDetails = !showDetails">
      <v-progress-circular
        :model-value="progress"
        :size="36"
        :width="3"
        :color="timerColor"
        rotate="90"
        class="timer-circle"
      >
        <v-icon
          :size="16"
          :color="timerColor"
          :class="{ 'pulse-animation': isAboutToExpire }"
        >
          {{ timerIcon }}
        </v-icon>
      </v-progress-circular>
      
      <v-tooltip
        location="bottom"
        activator="parent"
        v-model="showDetails"
        :open-on-hover="false"
      >
        <template #default>
          <div class="timer-tooltip">
            <div class="timer-tooltip-header">
              <v-icon size="small" :color="timerColor" class="mr-1">
                {{ timerIcon }}
              </v-icon>
              <span class="font-weight-medium">Sessão</span>
            </div>
            <div class="timer-tooltip-content">
              <div class="time-remaining">
                <span class="time-value">{{ formattedTime }}</span>
                <span class="time-label">restantes</span>
              </div>
              <div class="time-progress">
                <v-progress-linear
                  :model-value="progress"
                  :color="timerColor"
                  height="4"
                  rounded
                  class="mt-1"
                />
                <div class="time-percentage text-caption mt-1">
                  {{ Math.round(progress) }}% do tempo utilizado
                </div>
              </div>
              <div v-if="isAboutToExpire" class="time-warning text-caption mt-2">
                <v-icon size="x-small" color="warning" class="mr-1">mdi-alert</v-icon>
                Renove em breve para continuar
              </div>
            </div>
          </div>
        </template>
      </v-tooltip>
    </div>
  </div>
</template>


<style scoped>
.token-timer-indicator {
  margin-right: 12px;
  height: 100%;
  display: flex;
  align-items: center;
}

.timer-wrapper {
  position: relative;
  cursor: pointer;
  transition: transform 0.2s ease;
}

.timer-wrapper:hover {
  transform: scale(1.05);
}

.timer-circle {
  transition: all 0.3s ease;
}

.timer-circle:hover {
  transform: rotate(45deg);
}

.pulse-animation {
  animation: pulse 1.5s infinite;
}

:deep(.v-tooltip__content) {
  background-color: white !important;
  color: #333 !important;
  border-radius: 8px !important;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15) !important;
  border: 1px solid #e0e0e0 !important;
  padding: 0 !important;
  overflow: hidden;
}

:deep(.v-overlay__content) {
  background-color: white !important;
}

.timer-tooltip {
  min-width: 180px;
  padding: 12px;
  background-color: white;
}

.timer-tooltip-header {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e0e0e0;
  color: #333;
}

.timer-tooltip-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #333;
}

.time-remaining {
  display: flex;
  align-items: baseline;
  gap: 4px;
}

.time-value {
  font-size: 1.5rem;
  font-weight: bold;
  font-family: monospace;
  color: rgb(var(--v-theme-primary));
}

.time-label {
  font-size: 0.75rem;
  color: #666;
}

.time-percentage {
  font-size: 0.7rem;
  color: #666;
  text-align: center;
}

.time-warning {
  display: flex;
  align-items: center;
  color: rgb(var(--v-theme-warning-darken-1));
  background: rgba(var(--v-theme-warning), 0.1);
  padding: 6px 10px;
  border-radius: 6px;
  margin-top: 6px;
  font-size: 0.75rem;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.7;
    transform: scale(1.1);
  }
}
</style>