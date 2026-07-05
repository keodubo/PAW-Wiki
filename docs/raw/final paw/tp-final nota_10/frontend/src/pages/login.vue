<template>
  <div class="login-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="5" xl="4">
          <v-card class="login-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="login-header pa-8 text-center">
              <div class="logo-section mb-4">
                <div class="logo-wrapper mb-4">
                  <v-img src="@/assets/logo.png" alt="Logo" width="100" class="mx-auto shake rounded-circle" style="background: white; padding: 8px" />
                </div>
                <h1 class="text-h3 font-weight-bold text-white mb-2">
                  {{ $t('grupi') }}
                </h1>
                <p class="text-subtitle-1 text-white opacity-90">
                  {{ $t('welcome_back') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-8">
              <v-form @submit.prevent="handleLogin" ref="loginForm" class="login-form">
                <v-text-field
                  v-model="credentials.email"
                  :label="$t('email')"
                  type="email"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-email"
                  color="primary"
                  base-color="grey-lighten-1"
                  :rules="emailRules"
                  :error-messages="fieldErrors.email"
                  class="mb-4"
                  required
                />

                <v-text-field
                  v-model="credentials.password"
                  :label="$t('password')"
                  :type="showPassword ? 'text' : 'password'"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-lock"
                  :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click:append-inner="showPassword = !showPassword"
                  color="primary"
                  base-color="grey-lighten-1"
                  :rules="passwordRules"
                  :error-messages="fieldErrors.password"
                  class="mb-2"
                  required
                />

                <div class="text-right mb-6">
                  <router-link to="/forgot-password" class="forgot-password-link">
                    <small>{{ $t('forgot_password') }}</small>
                  </router-link>
                </div>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="login-btn mb-6" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-login</v-icon>
                  </template>
                  {{ $t('login') }}
                </v-btn>

                <v-btn variant="outlined" color="primary" size="large" block to="/register" class="register-btn">
                  <template #prepend>
                    <v-icon>mdi-account-plus</v-icon>
                  </template>
                  {{ $t('dont_have_account') }}
                </v-btn>
              </v-form>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<style scoped>
.login-page {
  min-height: 100vh;
  position: relative;
}

.login-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 1;
  max-width: 450px;
  margin: 0 auto;
}

.login-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.login-header::before {
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

.login-form .v-text-field {
  margin-bottom: 8px;
}

.login-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.login-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.register-btn {
  height: 52px !important;
  font-weight: 500;
  text-transform: none;
  border-radius: 12px;
  border: 2px solid rgb(var(--v-theme-primary)) !important;
  color: rgb(var(--v-theme-primary)) !important;
  transition: all 0.3s ease;
}

.register-btn:hover {
  background: rgb(var(--v-theme-primary)) !important;
  color: white !important;
  transform: translateY(-1px);
  box-shadow: 0 4px 15px rgba(var(--v-theme-primary), 0.3);
}

.forgot-password-link {
  color: rgb(var(--v-theme-primary));
  text-decoration: none;
  transition: all 0.2s ease;
  font-weight: 500;
}

.forgot-password-link:hover {
  color: rgb(var(--v-theme-secondary));
  text-decoration: underline;
}

.divider-text {
  background: white;
  color: #666;
  font-weight: 500;
  font-size: 14px;
}

.v-checkbox :deep(.v-selection-control__input) {
  color: rgb(var(--v-theme-primary));
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

.v-theme--dark .login-page {
  background: #121212 !important;
}

.v-theme--dark .login-card {
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

.v-theme--dark .divider-text {
  background: #1e1e1e !important;
  color: rgba(255, 255, 255, 0.7) !important;
}

@media (max-width: 600px) {
  .login-card {
    margin: 16px;
  }

  .login-header {
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

.v-checkbox :deep(.v-selection-control__wrapper) {
  margin-left: 4px;
}

.login-form .v-text-field {
  margin-bottom: 4px !important;
}

.text-right.mb-6 {
  margin-bottom: 16px !important;
}

.remember-simple {
  margin: 8px 0;
  align-items: center;
}

.remember-checkbox {
  min-height: 32px !important;
  flex-shrink: 0;
}

.remember-checkbox :deep(.v-selection-control) {
  min-height: 32px !important;
  display: flex;
  align-items: center;
}

.remember-checkbox :deep(.v-selection-control__wrapper) {
  width: 20px !important;
  height: 20px !important;
  margin-right: 8px;
  border: 2px solid rgba(0, 0, 0, 0.38) !important;
  border-radius: 2px;
  background-color: transparent !important;
  transition: all 0.2s ease;
}

.remember-checkbox :deep(.v-selection-control__input) {
  width: 20px !important;
  height: 20px !important;
  opacity: 1 !important;
}

.remember-checkbox :deep(.v-selection-control__input .v-icon) {
  font-size: 16px !important;
  color: white !important;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.remember-checkbox.checkbox-checked :deep(.v-selection-control__wrapper) {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgb(var(--v-theme-primary)) !important;
}

.remember-checkbox.checkbox-checked :deep(.v-selection-control__input .v-icon) {
  opacity: 1 !important;
}

.remember-checkbox:hover :deep(.v-selection-control__wrapper) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.remember-text {
  color: rgba(0, 0, 0, 0.87) !important;
  font-size: 0.875rem !important;
  line-height: 1.25rem;
  font-weight: 400;
  cursor: pointer;
  user-select: none;
}

.v-theme--dark .remember-checkbox :deep(.v-selection-control__wrapper) {
  border-color: rgba(255, 255, 255, 0.38) !important;
}

.v-theme--dark .remember-checkbox:hover :deep(.v-selection-control__wrapper) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.v-theme--dark .remember-text {
  color: rgba(255, 255, 255, 0.87) !important;
}
</style>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotifications } from '@/composables/useNotifications';
import type { LoginCredentials } from '@/services/UserService';

definePage({
  meta: {
    requiresGuest: true,
  },
});

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const authStore = useAuthStore();
const { showError, handleApiError } = useNotifications();

const loginForm = ref();
const showPassword = ref(false);

const credentials = ref<LoginCredentials>({
  email: '',
  password: '',
});

const fieldErrors = ref({
  email: '',
  password: '',
});

const isLoading = computed(() => authStore.isLoading);

const isFormValid = computed(() => {
  return credentials.value.email && credentials.value.password && credentials.value.email.includes('@');
});

const emailRules = [(v: string) => !!v || t('email_required'), (v: string) => /.+@.+\..+/.test(v) || t('email_invalid')];

const passwordRules = [(v: string) => !!v || t('password_required')];

const handleLogin = async () => {
  if (!loginForm.value) return;

  const { valid } = await loginForm.value.validate();
  if (!valid) return;

  try {
    fieldErrors.value = { email: '', password: '' };
    await authStore.login(credentials.value);
    const redirect = (route.query.redirect as string) || '/';
    router.push(redirect);
  } catch (err) {
    console.error('Login failed:', err);
    const { validationErrors } = handleApiError(err, { showToast: false });
    if (validationErrors && Object.keys(validationErrors).length > 0) {
      fieldErrors.value = {
        ...fieldErrors.value,
        ...validationErrors,
      };
    } else {
      showError('invalid_login');
    }
  }
};

onMounted(() => {
  authStore.initializeAuth();
  if (authStore.isAuthenticated) {
    router.push('/');
    return;
  }
  if (route.query.error) showError('invalid_login');
});
</script>
