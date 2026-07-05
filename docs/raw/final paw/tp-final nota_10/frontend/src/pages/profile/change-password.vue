<template>
  <div class="change-password-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="password-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="password-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-lock-reset</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('change_password') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('change_password_subtitle') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-expand-transition>
                <v-alert v-if="error" type="error" variant="tonal" class="mb-4" closable prominent @click:close="error = null">
                  <template #prepend>
                    <v-icon size="32">mdi-alert-circle</v-icon>
                  </template>
                  {{ error }}
                </v-alert>
              </v-expand-transition>
              <v-form @submit.prevent="handleSubmit" ref="passwordForm">
                <v-text-field
                  v-model="form.oldPassword"
                  :label="$t('current_password')"
                  type="password"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock"
                  density="comfortable"
                  color="primary"
                  class="mb-2"
                  :rules="[rules.required]"
                  :error-messages="fieldErrors.oldPassword"
                  required
                />

                <v-text-field
                  v-model="form.password"
                  :label="$t('new_password')"
                  type="password"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock-outline"
                  density="comfortable"
                  color="primary"
                  class="mb-2"
                  :rules="newPasswordRules"
                  :error-messages="fieldErrors.password"
                  required
                />

                <v-text-field
                  v-model="form.confirmPassword"
                  :label="$t('confirm_new_password')"
                  type="password"
                  variant="outlined"
                  prepend-inner-icon="mdi-lock-check"
                  density="comfortable"
                  color="primary"
                  class="mb-2"
                  :rules="confirmPasswordRules"
                  :error-messages="fieldErrors.confirmPassword"
                  required
                />

                <v-btn type="submit" color="primary" size="x-large" block :loading="submitting" :disabled="!isFormValid" class="password-btn mb-4" elevation="2">
                  <template #prepend v-if="!submitting">
                    <v-icon>mdi-check</v-icon>
                  </template>
                  {{ submitting ? $t('changing_password') : $t('change_password') }}
                </v-btn>

                <v-btn variant="outlined" color="grey" size="large" block @click="$router.push('/profile')" :disabled="submitting">
                  <template #prepend>
                    <v-icon>mdi-close</v-icon>
                  </template>
                  {{ $t('cancel') }}
                </v-btn>
              </v-form>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotifications } from '@/composables/useNotifications';
import { userService } from '@/services';

const { t } = useI18n();
const router = useRouter();
const authStore = useAuthStore();
const { showSuccess, showError, handleApiError } = useNotifications();

const passwordForm = ref();
const submitting = ref(false);
const error = ref<string | null>(null);

const form = ref({
  oldPassword: '',
  password: '',
  confirmPassword: '',
});

const fieldErrors = ref({
  oldPassword: '',
  password: '',
  confirmPassword: '',
});

const rules = {
  required: (v: string) => !!v || t('field_required'),
};

const newPasswordRules = [(v: string) => !!v || t('field_required'), (v: string) => (v && v.length >= 8) || t('password_min_length')];

const confirmPasswordRules = [(v: string) => !!v || t('field_required'), (v: string) => v === form.value.password || t('passwords_must_match')];

const isFormValid = computed(() => {
  return form.value.oldPassword && form.value.password && form.value.confirmPassword && form.value.password.length >= 8 && form.value.password === form.value.confirmPassword;
});

const handleSubmit = async () => {
  if (!passwordForm.value) return;
  const { valid } = await passwordForm.value.validate();
  if (!valid) {
    error.value = t('please_fill_all_fields_correctly');
    return;
  }

  if (form.value.password !== form.value.confirmPassword) {
    error.value = t('passwords_must_match');
    return;
  }

  if (!authStore.email) {
    error.value = t('failed_to_change_password');
    return;
  }

  submitting.value = true;
  error.value = null;

  try {
    fieldErrors.value = {
      oldPassword: '',
      password: '',
      confirmPassword: '',
    };

    await userService.changePassword(authStore.userId!, authStore.email, form.value.oldPassword, form.value.password);
    showSuccess('password_changed_successfully');
    router.push('/profile');
  } catch (err: any) {
    console.error('Password change failed:', err);

    // Si la respuesta es 401 es porque el request con Authorization Basic falló (contraseña actual incorrecta)
    if (err?.response?.status === 401) {
      error.value = t('incorrect_current_password');
      fieldErrors.value = {
        oldPassword: t('incorrect_current_password'),
        password: '',
        confirmPassword: '',
      };
      return;
    }

    const { validationErrors } = handleApiError(err, { showToast: false });
    if (validationErrors && Object.keys(validationErrors).length > 0) {
      fieldErrors.value = {
        ...fieldErrors.value,
        ...validationErrors,
      };
      error.value = t('please_fix_errors');
    } else {
      const errorMessage = err?.response?.data?.message || err?.message || t('failed_to_change_password');
      error.value = errorMessage;
      showError(errorMessage);
    }
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.change-password-page {
  min-height: 100vh;
  background: #ffffff;
}

.password-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.password-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.password-header::before {
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

.password-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.password-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.v-text-field :deep(.v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.v-text-field :deep(.v-field:hover) {
  background: #f1f3f4;
}

.v-text-field :deep(.v-field--focused .v-field__outline) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field--focused .v-label) {
  color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary));
}

.v-theme--dark .change-password-page {
  background: #121212 !important;
}

.v-theme--dark .password-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.v-theme--dark .v-text-field :deep(.v-field) {
  background: #2a2a2a !important;
  color: white !important;
}

.v-theme--dark .v-text-field :deep(.v-field:hover) {
  background: #333333 !important;
}

@media (max-width: 600px) {
  .password-card {
    margin: 16px;
  }

  .password-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
