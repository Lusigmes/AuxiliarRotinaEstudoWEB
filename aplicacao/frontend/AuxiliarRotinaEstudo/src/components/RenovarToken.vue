<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useAuth } from '@/composables/useAuth';

interface Props {
  segundosParaExpirar: number;
  visible: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  secondsUntilExpiration: 60, 
  visible: false
});

const emit = defineEmits<{
  (e: 'renovar'): void;
  (e: 'cancelar'): void;
  (e: 'update:visible', value: boolean): void;
}>();

const router = useRouter();
const { logout } = useAuth();

const countdown = ref(props.segundosParaExpirar);
const loading = ref(false);
const timeRemaining = ref(60 * 1000); 
const totalTime = 10 * 60 * 1000;

let countdownInterval: ReturnType<typeof setInterval> | null = null;

const isVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
});

const progress = computed(() => {
  return ((props.segundosParaExpirar - countdown.value) / props.segundosParaExpirar) * 100;
});

const timeMessage = computed(() => {
  if (countdown.value <= 10) {
    return `Sessão expira em ${countdown.value} segundo${countdown.value !== 1 ? 's' : ''}!`;
  }
  return `Sessão expira em ${countdown.value} segundos`;
});

function formatTime(ms: number): string {
  const seconds = Math.floor(ms / 1000);
  const minutes = Math.floor(seconds / 60);
  const remainingSeconds = seconds % 60;
  return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`;
}

function startCountdown() {
  if (countdownInterval) {
    clearInterval(countdownInterval);
  }

  countdown.value = props.segundosParaExpirar;
  timeRemaining.value = props.segundosParaExpirar * 1000;
  
  countdownInterval = setInterval(() => {
    if (countdown.value > 0) {
      countdown.value--;
      timeRemaining.value -= 1000;
    } else {
      clearInterval(countdownInterval!);
      handleCancel();
    }
  }, 1000);
}

async function handleRenovar() {
  loading.value = true;
  try {
    emit('renovar');
    isVisible.value = false;
  } finally {
    loading.value = false;
  }
}

function handleCancel() {
  if (countdownInterval) {
    clearInterval(countdownInterval);
  }
  
  logout();
  emit('cancelar');
  isVisible.value = false;
  
  setTimeout(() => {
    router.push('/');
  }, 300);
}

onMounted(() => {
  if (isVisible.value) {
    startCountdown();
  }
});

onUnmounted(() => {
  if (countdownInterval) {
    clearInterval(countdownInterval);
  }
});

watch(() => props.visible, (newVal) => {
  if (newVal) {
    startCountdown();
  } else if (countdownInterval) {
    clearInterval(countdownInterval);
  }
});

watch(() => props.segundosParaExpirar, () => {
  if (isVisible.value) {
    startCountdown();
  }
});
</script>

<template>
  <v-dialog
    v-model="isVisible"
    max-width="500"
    persistent
    :scrim="false"
    no-click-animation
  >
    <v-card class="token-renewal-card">
      <v-card-item class="pa-4">
        <template #prepend>
          <div class="timer-display mr-3">
            <v-progress-circular
              :model-value="progress"
              :size="48"
              :width="4"
              color="warning"
              rotate="90"
            >
              <span class="text-caption font-weight-bold">{{ countdown }}</span>
            </v-progress-circular>
          </div>
        </template>

        <v-card-title class="text-h6">Sessão prestes a expirar</v-card-title>
        <v-card-subtitle class="text-body-2">
          {{ timeMessage }}
        </v-card-subtitle>
      </v-card-item>

      <v-card-text class="pa-4">
        <div class="text-body-1 mb-4">
          Sua sessão de 10 minutos está quase terminando. 
          Renove para continuar usando o sistema sem interrupções.
        </div>

        <div class="time-info mb-4">
          <div class="d-flex justify-space-between text-caption text-medium-emphasis mb-1">
            <span>Tempo total da sessão:</span>
            <span class="font-weight-medium">10 minutos</span>
          </div>
          <div class="d-flex justify-space-between text-caption text-medium-emphasis">
            <span>Tempo utilizado:</span>
            <span class="font-weight-medium">{{ formatTime(totalTime - timeRemaining) }}</span>
          </div>
        </div>

        <div class="warning-box pa-3 rounded-lg mb-4">
          <div class="d-flex align-start">
            <v-icon color="warning" size="small" class="mr-2 mt-1">mdi-alert-circle</v-icon>
            <div class="text-caption">
              <strong>Atenção:</strong> Se não renovar, você será desconectado automaticamente em 
              <span class="font-weight-bold">{{ countdown }} segundos</span>.
            </div>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="pa-4 pt-0">
        <v-spacer />
        
        <div class="d-flex gap-3">
          <v-btn
            color="grey"
            variant="outlined"
            @click="handleCancel"
            :loading="loading"
            :disabled="loading"
          >
            Sair Agora
          </v-btn>
          
          <v-btn
            color="primary"
            @click="handleRenovar"
            :loading="loading"
          >
            Renovar Sessão
          </v-btn>
        </div>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>


<style scoped>
.token-renewal-card {
  border: 2px solid rgb(var(--v-theme-warning));
  animation: pulse 2s infinite;
}

.timer-display {
  display: flex;
  align-items: center;
}

.warning-box {
  background-color: rgba(var(--v-theme-warning), 0.1);
  border-left: 4px solid rgb(var(--v-theme-warning));
}

.gap-3 {
  gap: 12px;
}

@keyframes pulse {
  0%, 100% {
    border-color: rgb(var(--v-theme-warning));
    box-shadow: 0 0 0 0 rgba(var(--v-theme-warning), 0.4);
  }
  50% {
    border-color: rgb(var(--v-theme-warning-darken-1));
    box-shadow: 0 0 0 10px rgba(var(--v-theme-warning), 0);
  }
}
</style>