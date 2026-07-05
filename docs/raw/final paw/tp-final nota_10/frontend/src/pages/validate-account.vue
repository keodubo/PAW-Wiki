<template>
  <v-container class="fill-height d-flex align-center justify-center py-8">
    <v-row justify="center">
      <v-col cols="12" sm="10" md="8" lg="6">
        <v-fade-transition>
          <v-card v-if="!validationSuccess" elevation="12" class="validation-card">
            <div class="card-header pa-6 text-center">
              <v-avatar color="primary" size="80" class="mb-4 elevation-4">
                <v-icon size="48" color="white">mdi-shield-check</v-icon>
              </v-avatar>
              <h1 class="text-h4 font-weight-bold mb-2">
                {{ $t('validate_account_title') }}
              </h1>
              <p class="text-body-1 text-medium-emphasis">
                {{ $t('validation_subtitle') }}
              </p>
            </div>

            <v-divider />

            <v-expand-transition>
              <v-alert v-if="error" type="error" variant="tonal" class="ma-6 mb-0" closable prominent @click:close="error = null">
                <template #prepend>
                  <v-icon size="32">mdi-alert-circle</v-icon>
                </template>
                {{ error }}
              </v-alert>
            </v-expand-transition>

            <v-card-text class="pa-6">
              <div class="mb-6">
                <div class="step-header mb-4">
                  <v-chip color="primary" size="small" class="mr-3 font-weight-bold"> {{ $t('step') }} 1 </v-chip>
                  <h2 class="text-h6 font-weight-bold d-inline">
                    {{ $t('step_request_token') }}
                  </h2>
                </div>
                <p class="text-body-2 text-medium-emphasis mb-4">
                  {{ $t('step_request_token_description') }}
                </p>
                <v-btn color="primary" size="large" block :loading="requestingToken" :disabled="tokenRequested || validationSuccess" @click="requestToken" class="elevation-2" prepend-icon="mdi-email-fast">
                  {{ $t('request_validation_email') }}
                </v-btn>
                <v-expand-transition>
                  <v-alert v-if="tokenRequested" type="success" variant="tonal" class="mt-4" prominent>
                    <template #prepend>
                      <v-icon>mdi-check-circle</v-icon>
                    </template>
                    {{ $t('token_sent_success') }}
                  </v-alert>
                </v-expand-transition>
              </div>

              <v-divider class="my-8">
                <v-chip size="small">
                  <v-icon start size="small">mdi-arrow-down</v-icon>
                  {{ $t('then') }}
                </v-chip>
              </v-divider>

              <div>
                <div class="step-header mb-4">
                  <v-chip color="success" size="small" class="mr-3 font-weight-bold"> {{ $t('step') }} 2 </v-chip>
                  <h2 class="text-h6 font-weight-bold d-inline">
                    {{ $t('step_enter_token') }}
                  </h2>
                </div>
                <p class="text-body-2 text-medium-emphasis mb-4">
                  {{ $t('step_enter_token_description') }}
                </p>
                <v-form @submit.prevent="validateAccount">
                  <v-text-field
                    v-model="validationToken"
                    :label="$t('validation_token_label')"
                    :placeholder="$t('validation_token_placeholder')"
                    variant="outlined"
                    :disabled="validationSuccess"
                    :rules="[rules.required, rules.tokenError]"
                    :error-messages="tokenFieldError"
                    prepend-inner-icon="mdi-key-variant"
                    class="mb-4"
                    density="comfortable"
                  />
                  <v-btn type="submit" color="success" size="large" block :loading="validating" :disabled="!validationToken || validationSuccess" class="elevation-2" prepend-icon="mdi-check-decagram">
                    {{ $t('validate_account_button') }}
                  </v-btn>
                </v-form>
              </div>
            </v-card-text>
          </v-card>
        </v-fade-transition>

        <v-fade-transition>
          <v-card v-if="validationSuccess" elevation="12" class="validation-card text-center pa-8">
            <v-avatar color="success" size="120" class="mb-6 elevation-8">
              <v-icon size="72" color="white">mdi-check-circle</v-icon>
            </v-avatar>
            <h1 class="text-h3 font-weight-bold mb-4 text-success">
              {{ $t('success_title') }}
            </h1>
            <p class="text-h6 text-medium-emphasis mb-6">
              {{ $t('validation_success') }}
            </p>
            <v-progress-linear color="success" height="6" indeterminate rounded class="mb-6" />
            <v-btn color="success" variant="elevated" size="x-large" @click="goToDashboard" prepend-icon="mdi-home" class="px-8">
              {{ $t('go_to_dashboard') }}
            </v-btn>
          </v-card>
        </v-fade-transition>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { userService } from '@/services';
import { useRouter } from 'vue-router';
import { useNotifications } from '@/composables/useNotifications';

definePage({
  meta: {
    requiresAuth: true,
  },
  beforeEnter: async (to, from, next) => {
    const authStore = useAuthStore();

    if (authStore.accountValidated) {
      return next({ path: '/' });
    }
    return next();
  },
});

const { t } = useI18n();
const authStore = useAuthStore();
const router = useRouter();
const { showSuccess, showError } = useNotifications();

const validationToken = ref('');
const requestingToken = ref(false);
const tokenRequested = ref(false);
const validating = ref(false);
const validationSuccess = ref(false);
const error = ref<string | null>(null);
const tokenFieldError = ref<string>('');

const rules = {
  required: (value: string) => !!value || t('field_required'),
  tokenError: () => !tokenFieldError.value || tokenFieldError.value,
};

watch(validationToken, () => {
  if (tokenFieldError.value) {
    tokenFieldError.value = '';
  }
});

const requestToken = async () => {
  if (!authStore.userId) {
    error.value = t('user_not_logged_in');
    return;
  }

  requestingToken.value = true;
  error.value = null;

  try {
    await userService.requestValidationEmail(authStore.userId);
    tokenRequested.value = true;
    showSuccess('token_sent_success');
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('token_request_failed');
    showError('token_request_failed');
  } finally {
    requestingToken.value = false;
  }
};

const validateAccount = async () => {
  if (!validationToken.value || !authStore.email) {
    error.value = t('missing_token_or_email');
    return;
  }

  validating.value = true;
  error.value = null;
  tokenFieldError.value = '';

  try {
    const authData = await userService.authenticate(
      {
        email: authStore.email,
        password: validationToken.value,
      },
      true,
    );

    authStore.setAuthData(authData);
    await authStore.fetchUserCompanyAndLocation(authData.userId, authData.role);
    if (authStore.currentUser) {
      authStore.currentUser.validated = true;
    }
    authStore.accountValidated = true;

    validationSuccess.value = true;
    showSuccess('validation_success');

    router.push('/profile');
  } catch (err: any) {
    if (err.response?.status === 401) {
      tokenFieldError.value = err.response?.data?.message || t('invalid_validation_token');
      error.value = null;
    } else {
      error.value = err instanceof Error ? err.message : t('validation_failed');
      showError('validation_failed');
    }
  } finally {
    validating.value = false;
  }
};

const goToDashboard = () => {
  router.push('/');
};
</script>

<style scoped>
.validation-card {
  border-radius: 16px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.card-header {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.step-card {
  background: rgba(var(--v-theme-surface-variant), 0.3);
  border-radius: 12px;
  padding: 24px;
  transition: all 0.3s ease;
}

.step-card:hover {
  background: rgba(var(--v-theme-surface-variant), 0.5);
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.step-header {
  display: flex;
  align-items: center;
}

.v-btn {
  transition: all 0.3s ease;
  text-transform: none;
  letter-spacing: 0.5px;
}

.v-btn:not(:disabled):hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.2);
}

.v-text-field {
  transition: all 0.3s ease;
}

:deep(.v-field--focused) {
  transform: scale(1.01);
}

.v-avatar {
  animation: pulse 2s ease-in-out infinite;
}

@keyframes pulse {
  0%,
  100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}
</style>
