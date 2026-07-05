<template>
  <div class="register-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="register-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="register-header pa-6 text-center">
              <div class="logo-section">
                <div class="logo-wrapper mb-3">
                  <v-img src="@/assets/logo.png" alt="Logo" width="80" class="mx-auto shake rounded-circle" style="background: white; padding: 6px" />
                </div>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('grupi') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('create_account') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleRegister" ref="registerForm">
                <div class="mb-6">
                  <h3 class="text-h6 font-weight-bold mb-3">
                    {{ $t('account_type') }}
                  </h3>
                  <v-row class="ma-0">
                    <v-col cols="12" sm="6" class="pa-2">
                      <v-card :class="['account-type-card', { selected: !form.isCompany }]" @click="form.isCompany = false">
                        <div class="pa-4 text-center">
                          <v-icon size="48" color="primary" class="mb-2">mdi-account</v-icon>
                          <h4 class="text-subtitle-1 font-weight-bold">
                            {{ $t('client') }}
                          </h4>
                          <p class="text-caption text-grey-darken-1 mb-0">
                            {{ $t('client_description') }}
                          </p>
                        </div>
                      </v-card>
                    </v-col>

                    <v-col cols="12" sm="6" class="pa-2">
                      <v-card :class="['account-type-card', { selected: form.isCompany }]" @click="form.isCompany = true">
                        <div class="pa-4 text-center">
                          <v-icon size="48" color="primary" class="mb-2">mdi-office-building</v-icon>
                          <h4 class="text-subtitle-1 font-weight-bold">
                            {{ $t('company') }}
                          </h4>
                          <p class="text-caption text-grey-darken-1 mb-0">
                            {{ $t('company_description') }}
                          </p>
                        </div>
                      </v-card>
                    </v-col>
                  </v-row>
                </div>

                <v-divider class="mb-4" />

                <h3 class="text-h6 font-weight-bold mb-4">
                  {{ $t('personal_information') }}
                </h3>

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.firstName"
                      :label="$t('first_name')"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-account"
                      color="primary"
                      class="mb-2"
                      :rules="[rules.required]"
                      :error-messages="fieldErrors.firstName"
                      required
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.lastName"
                      :label="$t('last_name')"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-account"
                      color="primary"
                      class="mb-2"
                      :rules="[rules.required]"
                      :error-messages="fieldErrors.lastName"
                      required
                    />
                  </v-col>
                </v-row>

                <v-text-field
                  v-model="form.email"
                  :label="$t('email')"
                  type="email"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-email"
                  color="primary"
                  class="mb-2"
                  :rules="emailRules"
                  :error-messages="fieldErrors.email"
                  required
                />

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.password"
                      :label="$t('password')"
                      :type="showPassword ? 'text' : 'password'"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-lock"
                      :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showPassword = !showPassword"
                      color="primary"
                      class="mb-2"
                      :rules="passwordRules"
                      :error-messages="fieldErrors.password"
                      required
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.passwordConfirmation"
                      :label="$t('repeat_password')"
                      :type="showPasswordConfirm ? 'text' : 'password'"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-lock"
                      :append-inner-icon="showPasswordConfirm ? 'mdi-eye' : 'mdi-eye-off'"
                      @click:append-inner="showPasswordConfirm = !showPasswordConfirm"
                      color="primary"
                      class="mb-2"
                      :rules="passwordConfirmRules"
                      :error-messages="fieldErrors.passwordConfirmation"
                      required
                    />
                  </v-col>
                </v-row>

                <v-select
                  v-model="form.locationUri"
                  :label="$t('preferred_location')"
                  :items="locations"
                  item-title="name"
                  item-value="selfUri"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-map-marker"
                  color="primary"
                  :rules="[rules.required]"
                  :error-messages="fieldErrors.locationUri"
                  class="mb-6"
                  required
                />

                <v-alert v-if="form.isCompany" type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
                  {{ $t('company_info_message') }}
                </v-alert>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="register-btn mb-4" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>{{ form.isCompany ? 'mdi-office-building-plus' : 'mdi-account-plus' }}</v-icon>
                  </template>
                  {{ $t('register') }}
                </v-btn>

                <div class="text-center">
                  <p class="text-body-2 text-grey-darken-1 mb-2">
                    {{ $t('already_have_account') }}
                  </p>
                  <v-btn variant="text" color="primary" to="/login">
                    {{ $t('sign_in') }}
                  </v-btn>
                </div>
              </v-form>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useNotifications } from '@/composables/useNotifications';
import { useLocationsStore } from '@/stores/locations';
import { userService } from '@/services';

definePage({
  meta: {
    requiresGuest: true,
  },
});

const router = useRouter();
const { t } = useI18n();
const authStore = useAuthStore();
const { showSuccess, handleApiError } = useNotifications();
const locationsStore = useLocationsStore();

const registerForm = ref();
const showPassword = ref(false);
const showPasswordConfirm = ref(false);

const form = ref({
  isCompany: false,
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  locationUri: null as string | null,
  preferredLanguage: null as 'en' | 'es' | null,
});

const fieldErrors = ref({
  firstName: '',
  lastName: '',
  email: '',
  password: '',
  passwordConfirmation: '',
  locationUri: '',
});

const locations = computed(() => locationsStore.items);

const isLoading = computed(() => authStore.isLoading);

const isFormValid = computed(() => {
  return (
    form.value.firstName &&
    form.value.lastName &&
    form.value.email &&
    form.value.password &&
    form.value.passwordConfirmation &&
    form.value.locationUri &&
    form.value.email.includes('@') &&
    form.value.password === form.value.passwordConfirmation
  );
});

const rules = {
  required: (v: string) => !!v || t('field_required'),
};

const emailRules = [(v: string) => !!v || t('email_required'), (v: string) => /.+@.+\..+/.test(v) || t('email_invalid')];

const passwordRules = [(v: string) => !!v || t('password_required'), (v: string) => v.length >= 8 || t('password_min_length')];

const passwordConfirmRules = [(v: string) => !!v || t('password_confirm_required'), (v: string) => v === form.value.password || t('passwords_must_match')];

const handleRegister = async () => {
  if (!registerForm.value) return;

  const { valid } = await registerForm.value.validate();
  if (!valid) return;

  try {
    fieldErrors.value = {
      firstName: '',
      lastName: '',
      email: '',
      password: '',
      passwordConfirmation: '',
      locationUri: '',
    };

    const registerData = {
      email: form.value.email,
      password: form.value.password,
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      locationUri: form.value.locationUri!,
      isCompany: form.value.isCompany,
      preferredLanguage: (localStorage.getItem('locale') as 'en' | 'es') || undefined,
    };

    await userService.create(registerData);

    showSuccess(form.value.isCompany ? t('register_success_company') : t('register_success'));

    router.push('/login');
  } catch (err) {
    console.error('Registration failed:', err);

    const { validationErrors } = handleApiError(err);

    if (validationErrors && Object.keys(validationErrors).length > 0) {
      fieldErrors.value = {
        ...fieldErrors.value,
        ...validationErrors,
      };
    }
  }
};

const loadLocations = async () => {
  try {
    await locationsStore.fetchAll();
  } catch (error) {
    console.error('Failed to load locations:', error);
    handleApiError(error);
  }
};

onMounted(() => {
  loadLocations();
});
</script>

<style scoped>
.register-page {
  min-height: 100vh;
}

.register-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.register-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.register-header::before {
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

.account-type-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border: 2px solid transparent;
  background: #f8f9fa;
  height: 100%;
}

.account-type-card:hover {
  transform: translateY(-2px);
  border-color: rgba(var(--v-theme-primary), 0.3);
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.15);
}

.account-type-card.selected {
  border-color: rgb(var(--v-theme-primary));
  background: rgba(var(--v-theme-primary), 0.05);
  box-shadow: 0 4px 16px rgba(var(--v-theme-primary), 0.2);
}

.register-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.register-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.v-text-field :deep(.v-field),
.v-select :deep(.v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.v-text-field :deep(.v-field:hover),
.v-select :deep(.v-field:hover) {
  background: #f1f3f4;
}

.v-text-field :deep(.v-field--focused .v-field__outline),
.v-select :deep(.v-field--focused .v-field__outline) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field--focused .v-label),
.v-select :deep(.v-field--focused .v-label) {
  color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field__prepend-inner .v-icon),
.v-select :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary));
}

.v-theme--dark .register-page {
  background: #121212 !important;
}

.v-theme--dark .register-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.v-theme--dark .account-type-card {
  background: #2a2a2a !important;
}

.v-theme--dark .account-type-card:hover {
  background: #333333 !important;
}

.v-theme--dark .account-type-card.selected {
  background: rgba(var(--v-theme-primary), 0.15) !important;
}

.v-theme--dark .v-text-field :deep(.v-field),
.v-theme--dark .v-select :deep(.v-field) {
  background: #2a2a2a !important;
  color: white !important;
}

.v-theme--dark .v-text-field :deep(.v-field:hover),
.v-theme--dark .v-select :deep(.v-field:hover) {
  background: #333333 !important;
}

@media (max-width: 600px) {
  .register-card {
    margin: 16px;
  }

  .register-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
