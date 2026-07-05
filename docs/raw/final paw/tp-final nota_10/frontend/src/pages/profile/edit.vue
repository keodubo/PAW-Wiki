<template>
  <div class="profile-edit-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="profile-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="profile-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-account</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('edit_profile') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('update_personal_information') }}
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
              <v-form @submit.prevent="handleSubmit" ref="profileForm">
                <v-row>
                  <v-col cols="12">
                    <v-text-field v-model="form.email" :label="$t('email')" variant="outlined" prepend-inner-icon="mdi-email" density="comfortable" color="primary" class="mb-2" disabled />
                  </v-col>

                  <v-col cols="12" sm="6">
                    <v-text-field v-model="form.firstName" :label="$t('first_name')" variant="outlined" prepend-inner-icon="mdi-account" density="comfortable" color="primary" class="mb-2" :rules="[rules.required]" required />
                  </v-col>

                  <v-col cols="12" sm="6">
                    <v-text-field v-model="form.lastName" :label="$t('last_name')" variant="outlined" prepend-inner-icon="mdi-account" density="comfortable" color="primary" class="mb-2" :rules="[rules.required]" required />
                  </v-col>

                  <v-col cols="12">
                    <v-select
                      v-model="form.locationUri"
                      :label="$t('preferred_location')"
                      :items="locations"
                      item-title="name"
                      item-value="selfUri"
                      variant="outlined"
                      prepend-inner-icon="mdi-map-marker"
                      density="comfortable"
                      color="primary"
                      class="mb-2"
                      :rules="[rules.required]"
                      required
                    />
                  </v-col>
                </v-row>

                <v-btn type="submit" color="primary" size="x-large" block :loading="submitting" :disabled="!isFormValid" class="profile-btn mb-4" elevation="2">
                  <template #prepend v-if="!submitting">
                    <v-icon>mdi-check</v-icon>
                  </template>
                  {{ submitting ? $t('saving') : $t('save_changes') }}
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
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useLocationsStore } from '@/stores/locations';
import { useNotifications } from '@/composables/useNotifications';
import { userService } from '@/services';

definePage({
  meta: {
    requiresAuth: true,
  },
});

const { t } = useI18n();

const router = useRouter();
const authStore = useAuthStore();
const locationsStore = useLocationsStore();
const { showSuccess, showError } = useNotifications();

const profileForm = ref();
const submitting = ref(false);
const error = ref<string | null>(null);

const form = ref({
  email: '',
  firstName: '',
  lastName: '',
  locationUri: null as string | null,
});

const rules = {
  required: (v: string) => !!v || t('field_required'),
};

const locations = computed(() => locationsStore.items);

const isFormValid = computed(() => {
  return form.value.firstName && form.value.lastName && form.value.locationUri;
});

onMounted(async () => {
  await locationsStore.fetchAll();
  const user = authStore.currentUser;
  if (user) {
    form.value.email = user.email;
    form.value.firstName = user.firstName;
    form.value.lastName = user.lastName;
    form.value.locationUri = authStore.preferredLocation?.selfUri || null;
  }
});

const handleSubmit = async () => {
  if (!profileForm.value) return;
  const { valid } = await profileForm.value.validate();
  if (!valid) {
    error.value = t('please_fill_all_fields_correctly');
    return;
  }
  submitting.value = true;
  error.value = null;
  try {
    await userService.update(authStore.currentUser!.id, {
      firstName: form.value.firstName,
      lastName: form.value.lastName,
      locationUri: form.value.locationUri!,
    });
    await authStore.fetchUserCompanyAndLocation(authStore.userId!, authStore.role!);
    showSuccess('profile_updated_successfully');
    router.push('/profile');
  } catch (err) {
    error.value = err instanceof Error ? err.message : t('failed_to_update_profile');
    showError('failed_to_update_profile');
  } finally {
    submitting.value = false;
  }
};
</script>

<style scoped>
.profile-edit-page {
  min-height: 100vh;
  background: #ffffff;
}

.profile-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.profile-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.profile-header::before {
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

.profile-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.profile-btn:hover:not(.v-btn--disabled) {
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

.v-theme--dark .profile-edit-page {
  background: #121212 !important;
}

.v-theme--dark .profile-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
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
  .profile-card {
    margin: 16px;
  }

  .profile-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
