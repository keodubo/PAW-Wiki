<template>
  <v-dialog v-model="dialog" max-width="550">
    <v-card class="join-dialog-card elevation-12 rounded-xl">
      <v-card-title class="join-dialog-header pa-6 text-center">
        <div class="header-section">
          <v-icon size="56" color="white" class="mb-2">mdi-check-circle</v-icon>
          <h2 class="text-h5 font-weight-bold text-white mb-1">
            {{ $t('admin.companies.validate_company') }}
          </h2>
        </div>
      </v-card-title>

      <v-card-text class="pa-6">
        <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
          {{ $t('admin.companies.validate_company_confirmation') }}
        </v-alert>
      </v-card-text>

      <v-card-actions class="px-6 pb-4">
        <v-spacer />
        <v-btn variant="outlined" color="grey" size="large" @click="close" :disabled="isValidating" class="mr-2">
          <template #prepend>
            <v-icon>mdi-arrow-left</v-icon>
          </template>
          {{ $t('back') }}
        </v-btn>
        <v-btn color="success" variant="flat" size="large" @click="confirmValidate" :loading="isValidating" class="dialog-action-btn">
          <template #prepend v-if="!isValidating">
            <v-icon>mdi-check-circle</v-icon>
          </template>
          {{ $t('admin.companies.validate') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';

interface Props {
  modelValue: boolean;
  companyId?: number;
}

const props = defineProps<Props>();

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm', companyId: number): void;
}

const emit = defineEmits<Emits>();

const dialog = ref(props.modelValue);
const isValidating = ref(false);

watch(
  () => props.modelValue,
  (newValue) => {
    dialog.value = newValue;
  },
);

watch(dialog, (newValue) => {
  emit('update:modelValue', newValue);
});

const close = () => {
  if (!isValidating.value) {
    dialog.value = false;
  }
};

const confirmValidate = async () => {
  if (!props.companyId) return;
  isValidating.value = true;
  emit('confirm', props.companyId);
};

watch(dialog, (newValue) => {
  if (!newValue) {
    isValidating.value = false;
  }
});
</script>

<style scoped>
.join-dialog-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.join-dialog-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.join-dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.header-section {
  position: relative;
  z-index: 2;
}

.dialog-action-btn {
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.dialog-action-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--v-theme-success), 0.3) !important;
}

.v-theme--dark .join-dialog-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}
</style>
