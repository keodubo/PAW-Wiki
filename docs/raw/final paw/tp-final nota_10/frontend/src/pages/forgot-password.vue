<template>
  <div class="forgot-password-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="5" xl="4">
          <v-card class="forgot-password-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="forgot-password-header pa-8 text-center">
              <div class="logo-section mb-4">
                <div class="logo-wrapper mb-4">
                  <v-img src="@/assets/logo.png" alt="Logo" width="100" class="mx-auto shake rounded-circle" style="background: white; padding: 8px" />
                </div>
                <h1 class="text-h3 font-weight-bold text-white mb-2">
                  {{ $t('grupi') }}
                </h1>
                <p class="text-subtitle-1 text-white opacity-90">
                  {{ $t('recover_password_title') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-8">
              <v-alert type="info" variant="tonal" class="mb-6" icon="mdi-information-outline">
                <div class="text-body-2">{{ $t('recover_password_subtitle') }}</div>
              </v-alert>

              <v-form @submit.prevent="handleSubmit" ref="emailForm" class="forgot-password-form">
                <v-text-field
                  v-model="form.email"
                  :label="$t('email')"
                  type="email"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-email"
                  color="primary"
                  base-color="grey-lighten-1"
                  :rules="emailRules"
                  :error-messages="formErrors.email"
                  class="mb-4"
                  required
                  clearable
                  :loading="isLoading"
                />

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="forgot-btn mb-6" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-email-send</v-icon>
                  </template>
                  {{ $t('send_recovery_email') }}
                </v-btn>

                <v-btn variant="outlined" color="primary" size="large" block to="/login" class="back-btn">
                  <template #prepend>
                    <v-icon>mdi-chevron-left</v-icon>
                  </template>
                  {{ $t('remember_password_signin') }}
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
import { ref, reactive, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotifications } from '@/composables/useNotifications';
import { userService } from '@/services';

definePage({
  meta: {
    requiresGuest: true,
  },
});

const router = useRouter();
const { t } = useI18n();
const { showSuccess, showError, handleApiError } = useNotifications();

const emailForm = ref();
const isLoading = ref(false);

const form = reactive({
  email: '',
});

const formErrors = ref({
  email: '',
});

const isFormValid = computed(() => {
  return form.email && form.email.includes('@');
});

const emailRules = [(v: string) => !!v || t('email_required'), (v: string) => /.+@.+\..+/.test(v) || t('email_invalid')];

const handleSubmit = async () => {
  if (!emailForm.value) return;

  const { valid } = await emailForm.value.validate();
  if (!valid) return;

  isLoading.value = true;

  try {
    formErrors.value = { email: '' };
    await userService.requestPasswordRecovery({ email: form.email });
    showSuccess('recovery_email_sent_toast');
    router.push({ path: '/reset-password', query: { email: form.email } });
  } catch (err) {
    console.error('Password recovery request failed:', err);
    const { validationErrors } = handleApiError(err, { showToast: false });
    if (validationErrors && Object.keys(validationErrors).length > 0) {
      formErrors.value = {
        ...formErrors.value,
        ...validationErrors,
      };
    } else {
      const error = err as any;
      if (error.response?.status === 404) showError('no_account_found');
      else if (error.response?.data?.message) showError(error.response.data.message);
      else showError('failed_send_recovery_email');
    }
  } finally {
    isLoading.value = false;
  }
};
</script>

<style scoped>
.forgot-password-page {
  min-height: 100vh;
  position: relative;
}

.forgot-password-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
  max-width: 450px;
  margin: 0 auto;
}

.forgot-password-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.forgot-password-header::before {
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

.forgot-password-form .v-text-field {
  margin-bottom: 8px;
}

.forgot-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.forgot-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.back-btn {
  height: 52px !important;
  font-weight: 500;
  text-transform: none;
  border-radius: 12px;
  border: 2px solid rgb(var(--v-theme-primary)) !important;
  color: rgb(var(--v-theme-primary)) !important;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background: rgb(var(--v-theme-primary)) !important;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(var(--v-theme-primary), 0.3);
}

.v-text-field :deep(.v-field--focused .v-field__outline) {
  --v-field-border-opacity: 1;
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

.v-theme--dark .forgot-password-page {
  background: #121212 !important;
}

.v-theme--dark .forgot-password-card {
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
  .forgot-password-card {
    margin: 16px;
  }

  .forgot-password-header {
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

.v-text-field :deep(.v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.v-text-field :deep(.v-field:hover) {
  background: #f1f3f4;
}
</style>
