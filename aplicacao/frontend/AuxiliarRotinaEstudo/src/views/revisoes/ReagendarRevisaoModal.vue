<script setup lang="ts">
import type { RevisaoResponseInterface } from '@/types';
import { validarFormatoData, converterStringParaData } from '@/utils/dateUtils';
import { onBeforeUnmount, onMounted, ref, watch } from 'vue';
import * as yup from 'yup';

const props = defineProps<{
    modelValue: boolean;
    revisao?: RevisaoResponseInterface | null;
    titulo?: string; 
    verLimiteInfo?: boolean; 
}>();

const emit = defineEmits<{
    (e: 'update:modelValue', value: boolean): void; 
    (e: 'reagendar', data: string): void;
}>();

const novaData = ref('');
const loading = ref(false);
const error = ref<string | undefined | null>(undefined);
const showTooltip = ref(false);

const schema = yup.string()
    .required('Data é obrigatória')
    .test('formato-valido', 'Data inválida. Use o formato DD/MM/AAAA', (value) => {
        return validarFormatoData(value || '');
    })
    .test('data-nao-muito-antiga', 'Limite de 5 dias atrás referente ao dia atual.', (value) => {
        if (!value) return true;
        
        const dataInformada = converterStringParaData(value);
        const cincoDiasAtras = new Date();
        cincoDiasAtras.setDate(cincoDiasAtras.getDate() - 5);
        cincoDiasAtras.setHours(0, 0, 0, 0);
        
        return dataInformada >= cincoDiasAtras;
    });

function fecharModal() {
    emit('update:modelValue', false);
    novaData.value = '';
    loading.value = false;
    error.value = null;
    showTooltip.value = false;
}

watch(() => props.revisao, (revisao) => {
    if (revisao) {
        novaData.value = revisao.dataRevisao;
        error.value = null;
    } else {
        novaData.value = '';
    }
}, { immediate: true });

function startTooltipTimer() {
    showTooltip.value = true;
    setTimeout(() => {
        showTooltip.value = false;
    }, 5000);
}

async function confirmarReagendamento() {
    try {
        error.value = null;
        
        await schema.validate(novaData.value, { abortEarly: true });
        
        loading.value = true;
        emit('reagendar', novaData.value);
        
    } catch (validationError: any) {
        if (validationError instanceof yup.ValidationError) {
            error.value = validationError.errors[0];
        } else {
            error.value = 'Erro ao validar data';
            console.error('Erro de validação:', validationError);
        }
    } finally {
        loading.value = false;
    }
}

function onKeydown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
        fecharModal();
    }
}

onMounted(() => {
    document.addEventListener('keydown', onKeydown);
});

onBeforeUnmount(() => {
    document.removeEventListener('keydown', onKeydown);
});

function limparErro() {
    error.value = null;
}

function onFocus() {
    if (!showTooltip.value) {
        startTooltipTimer();
    }
}
</script>

<template>
    <v-dialog
        :model-value="modelValue"
        @update:model-value="fecharModal"
        max-width="500px"
        persistent
        @keydown.esc="fecharModal"
        @opened="startTooltipTimer"
    >
        <v-card v-if="revisao">
            <v-card-title class="d-flex justify-space-between align-center">
                <span>{{ props.titulo || 'Reagendar Revisão' }}</span>
                <v-btn icon variant="text" @click="fecharModal">
                    <v-icon>mdi-close</v-icon>
                </v-btn>
            </v-card-title>

            <v-card-text>
                <div v-if="revisao.dataRevisao" class="text-caption text-grey mb-3">
                    <v-icon icon="mdi-calendar" size="small" class="mr-1" />
                    Data atual: {{ revisao.dataRevisao }}
                </div>

                <v-text-field
                    v-model="novaData"
                    label="Nova Data"
                    placeholder="DD/MM/AAAA"
                    v-mask="'##/##/####'"
                    variant="outlined"
                    density="compact"
                    :error="!!error"
                    :error-messages="error"
                    @input="limparErro"
                    @keyup.enter="confirmarReagendamento"
                    @focus="onFocus"
                    autofocus
                />
                
                <div v-if="props.verLimiteInfo !== false" class="text-caption text-blue mt-1">
                    <v-icon icon="mdi-information" size="small" class="mr-1" />
                    Limite para datas passadas: 5 dias
                </div>
            </v-card-text>

            <v-divider />

            <v-card-actions class="pa-4">
                <v-spacer />
                <div class="d-flex gap-2">
                    <v-btn
                        color="grey"
                        variant="outlined"
                        @click="fecharModal"
                        :disabled="loading"
                    >
                        <v-icon icon="mdi-close" class="mr-2" size="small" />
                        Cancelar
                    </v-btn>

                    <v-tooltip
                        v-model="showTooltip"
                        location="top"
                        :open-delay="0"
                        :close-delay="0"
                        :hide-overlay="true"
                        transition="slide-y-transition"
                    >
                        <template #activator="{ props: tooltipProps }">
                            <v-btn
                                color="primary"
                                @click="confirmarReagendamento"
                                :loading="loading"
                                :disabled="!novaData.trim()"
                                v-bind="tooltipProps"
                            >
                                <v-icon icon="mdi-check" class="mr-2" size="small" />
                                Reagendar
                            </v-btn>
                        </template>
                        <div class="d-flex align-center">
                            <v-icon icon="mdi-keyboard-return" size="small" class="mr-1" />
                            <span>Aperte Enter para confirmar</span>
                        </div>
                    </v-tooltip>
                </div>
            </v-card-actions>
        </v-card>
    </v-dialog>
</template>