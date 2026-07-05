<template>
  <div class="reset-password-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="5" xl="4">
          <v-card class="reset-password-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="reset-password-header pa-8 text-center">
              <div class="logo-section mb-4">
                <div class="logo-wrapper mb-4">
                  <v-img src="@/assets/logo.png" alt="Logo" width="100" class="mx-auto shake rounded-circle" style="background: white; padding: 8px" />
                </div>
                <h1 class="text-h3 font-weight-bold text-white mb-2">
                  {{ $t('grupi') }}
                </h1>
                <p class="text-subtitle-1 text-white opacity-90">
                  {{ $t('reset_password_title') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-8">
              <v-alert type="info" variant="tonal" class="mb-6" icon="mdi-lock-reset">
                <div class="text-body-2">{{ $t('reset_password_subtitle') }}</div>
              </v-alert>

              <v-form @submit.prevent="handleSubmit" ref="resetForm" class="reset-password-form">
                <v-text-field
                  v-model="form.token"
                  :label="$t('recovery_token')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-key"
                  color="primary"
                  base-color="grey-lighten-1"
                  :rules="tokenRules"
                  :error-messages="fieldErrors.token"
                  class="mb-4"
                  required
                />

                <v-text-field
                  v-model="form.password"
                  :label="$t('new_password')"
                  :type="showPassword ? 'text' : 'password'"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-lock"
                  :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append-inner="showPassword = !showPassword"
                  color="primary"
                  class="mb-4"
                  :rules="passwordRules"
                  :error-messages="fieldErrors.password"
                  required
                />

                <v-text-field
                  v-model="form.passwordConfirmation"
                  :label="$t('confirm_new_password')"
                  :type="showConfirmPassword ? 'text' : 'password'"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-lock"
                  :append-inner-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append-inner="showConfirmPassword = !showConfirmPassword"
                  color="primary"
                  class="mb-6"
                  :rules="confirmPasswordRules"
                  :error-messages="fieldErrors.passwordConfirmation"
                  required
                />

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="reset-btn mb-6" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-content-save</v-icon>
                  </template>
                  {{ $t('update_password') }}
                </v-btn>

                <v-btn variant="outlined" color="primary" size="large" block to="/forgot-password" class="help-btn">
                  <template #prepend>
                    <v-icon>mdi-refresh</v-icon>
                  </template>
                  {{ $t('request_new_reset_link') }}
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
import { ref, reactive, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotifications } from '@/composables/useNotifications';
import { userService } from '@/services';

definePage({
  meta: {
    requiresGuest: true,
  },
});

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const { showSuccess, showError, handleApiError } = useNotifications();

const resetForm = ref();
const isLoading = ref(false);
const showPassword = ref(false);
const showConfirmPassword = ref(false);

const form = reactive({
  email: '',
  token: '',
  password: '',
  passwordConfirmation: '',
});

const fieldErrors = ref({
  token: '',
  password: '',
  passwordConfirmation: '',
});

const isFormValid = computed(() => {
  return form.email && form.email.includes('@') && form.token && form.password && form.passwordConfirmation && form.password === form.passwordConfirmation;
});

const tokenRules = [(v: string) => !!v || t('token_required')];

const passwordRules = [(v: string) => !!v || t('password_required'), (v: string) => v.length >= 8 || t('password_min_length')];

const confirmPasswordRules = [(v: string) => !!v || t('password_confirm_required'), (v: string) => v === form.password || t('passwords_must_match')];

onMounted(() => {
  const email = route.query.email as string;

  if (!email) {
    showError('email_required');
    router.push('/forgot-password');
    return;
  }

  form.email = email;

  const token = route.query.token as string;
  if (token) {
    form.token = token;
  }
});

const handleSubmit = async () => {
  if (!resetForm.value) return;

  const { valid } = await resetForm.value.validate();
  if (!valid) return;

  if (!form.token) {
    showError('invalid_recovery_token');
    return;
  }

  if (!form.email) {
    showError('email_required');
    return;
  }

  isLoading.value = true;

  try {
    fieldErrors.value = { token: '', password: '', passwordConfirmation: '' };
    await userService.recoverPassword(form.email, form.token, { password: form.password });
    showSuccess('password_updated_successfully');
    router.push('/login');
  } catch (err) {
    console.error('Password reset failed:', err);
    const { validationErrors } = handleApiError(err, { showToast: false });
    if (validationErrors && Object.keys(validationErrors).length > 0) {
      fieldErrors.value = {
        ...fieldErrors.value,
        ...validationErrors,
      };
    } else {
      const error = err as any;
      if (error.response?.status === 400 || error.response?.status === 401) showError('invalid_or_expired_token');
      else if (error.response?.data?.message) showError(error.response.data.message);
      else showError('failed_to_reset_password');
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.reset-password-page {
  min-height: 100vh;
  position: relative;
}

.reset-password-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
  max-width: 450px;
  margin: 0 auto;
}

.reset-password-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.reset-password-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.logo-section {
  position: relative;
  z-index: 2;
}

.logo-wrapper {
  position: relative;
  display: inline-block;
}

.shake {
  cursor: pointer;
  transition: all 0.3s ease;
  filter: drop-shadow(0 4px 8px rgba(0, 0, 0, 0.1));
}

.shake:hover {
  animation: animate-logo 0.8s ease-in-out;
  transform: scale(1.05);
}

@keyframes animate-logo {
  0%,
  100% {
    transform: scale(1.05) rotate(0deg);
  }
  25% {
    transform: scale(1.05) rotate(10deg);
  }
  50% {
    transform: scale(1.05) rotate(-10deg);
  }
  75% {
    transform: scale(1.05) rotate(5deg);
  }
}

.reset-password-form .v-text-field {
  margin-bottom: 8px;
}

.reset-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.reset-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.help-btn {
  height: 52px !important;
  font-weight: 500;
  text-transform: none;
  border-radius: 12px;
  border: 2px solid rgb(var(--v-theme-primary)) !important;
  color: rgb(var(--v-theme-primary)) !important;
  transition: all 0.3s ease;
}

.help-btn:hover {
  background: rgb(var(--v-theme-primary)) !important;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(var(--v-theme-primary), 0.3);
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

.v-alert {
  border-radius: 12px;
}

.v-theme--dark .reset-password-page {
  background: #121212 !important;
}

.v-theme--dark .reset-password-card {
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
  .reset-password-card {
    margin: 16px;
  }

  .reset-password-header {
    padding: 24px 16px !important;
  }

  .v-card-text {
    padding: 24px 16px !important;
  }
}

.v-btn--loading {
  pointer-events: none;
}

.v-btn--disabled {
  opacity: 0.6 !important;
}
</style>
