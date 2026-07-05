<template>
  <div class="company-edit-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="company-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="company-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-office-building</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('edit_company_information') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('update_company_details') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleUpdate" ref="companyForm">
                <v-text-field
                  v-model="form.name"
                  :label="$t('company_name')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-domain"
                  color="primary"
                  class="mb-2"
                  :disabled="true"
                  :hint="$t('company_name_cannot_change')"
                  persistent-hint
                />

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.email"
                      :label="$t('company_email')"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-email"
                      color="primary"
                      class="mb-2"
                      :rules="emailRules"
                      :error-messages="fieldErrors.email"
                      required
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.phone"
                      :label="$t('phone_number')"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-phone"
                      color="primary"
                      class="mb-2"
                      :rules="phoneRules"
                      :error-messages="fieldErrors.phone"
                      counter="15"
                      required
                    />
                  </v-col>
                </v-row>

                <v-text-field
                  v-model="form.address"
                  :label="$t('address')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-map-marker"
                  color="primary"
                  class="mb-2"
                  :rules="addressRules"
                  :error-messages="fieldErrors.address"
                  counter="100"
                  required
                />

                <v-text-field
                  v-model="form.cbu"
                  :label="$t('cbu')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-bank"
                  color="primary"
                  class="mb-2"
                  :rules="cbuRules"
                  :error-messages="fieldErrors.cbu"
                  counter="22"
                  required
                />

                <v-card v-if="imagePreview || currentImageUrl" class="mb-4" outlined>
                  <v-card-text>
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="d-flex align-center">
                        <v-icon color="primary" class="mr-2">mdi-image</v-icon>
                        <span class="text-subtitle-1 font-weight-medium">
                          {{ imagePreview ? $t('company_logo_image') : $t('current_image') }}
                        </span>
                      </div>
                      <v-chip v-if="imagePreview" color="success" size="small">
                        <v-icon start size="small">mdi-check</v-icon>
                        {{ $t('new_image_selected') }}
                      </v-chip>
                      <v-chip v-else color="info" size="small">
                        <v-icon start size="small">mdi-check-circle</v-icon>
                        {{ $t('current') }}
                      </v-chip>
                    </div>
                    <v-img :src="imagePreview || currentImageUrl || undefined" max-height="200" contain class="rounded" />
                  </v-card-text>
                </v-card>

                <v-card class="image-upload-card mb-4" outlined>
                  <v-card-text>
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="d-flex align-center">
                        <v-icon color="primary" class="mr-2">mdi-image-edit</v-icon>
                        <span class="text-subtitle-1 font-weight-medium">{{ $t('change_image') }}</span>
                        <v-chip color="grey" size="x-small" class="ml-2">{{ $t('optional') }}</v-chip>
                      </div>
                    </div>

                    <v-file-input
                      v-model="imageFile"
                      :label="$t('choose_new_image')"
                      accept="image/jpeg,image/jpg,image/png"
                      prepend-icon=""
                      prepend-inner-icon="mdi-camera"
                      variant="outlined"
                      density="comfortable"
                      color="primary"
                      :rules="imageRules"
                      :error-messages="fieldErrors.document"
                      show-size
                      clearable
                      @update:model-value="handleImageChange"
                    />

                    <v-alert v-if="fieldErrors.document" type="error" density="compact" class="mt-2">
                      {{ fieldErrors.document }}
                    </v-alert>
                  </v-card-text>
                </v-card>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="company-btn mb-4" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-content-save</v-icon>
                  </template>
                  {{ $t('save_changes') }}
                </v-btn>

                <v-btn variant="outlined" color="grey" size="large" block @click="handleCancel" :disabled="isLoading">
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
import { useRouter, useRoute } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotifications } from '@/composables/useNotifications';
import { companyService, documentService, type EditCompanyData } from '@/services';
import { useAuthStore } from '@/stores/auth';
import type { Company } from '@/models';
import { UserRole } from '@/models/UserRole';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_PENDING, UserRole.COMPANY_UNVERIFIED, UserRole.COMPANY_VERIFIED],
  },
  beforeEnter: (to, from, next) => {
    const authStore = useAuthStore();

    if (authStore.currentCompany === null) {
      return next({ path: '/unauthorized' });
    }

    next();
  },
});

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const { showError, showSuccess, handleApiError } = useNotifications();
const authStore = useAuthStore();

const companyForm = ref();
const isLoading = ref(false);
const isLoadingCompany = ref(true);
const imageFile = ref<File | File[] | null>(null);
const imagePreview = ref<string | null>(null);
const company = ref<Company | null>(null);
const currentImageUrl = ref<string | null>(null);
const currentImageUri = ref<string | null>(null);

const form = ref({
  name: '',
  address: '',
  email: '',
  phone: '',
  cbu: '',
});

const fieldErrors = ref({
  email: '',
  phone: '',
  address: '',
  cbu: '',
  document: '',
});

const isFormValid = computed(() => {
  return form.value.email && form.value.address && form.value.phone && form.value.cbu;
});

const rules = {
  required: (v: any) => !!v || t('field_required'),
};

const emailRules = [
  (v: string) => !!v || t('field_required'),
  (v: string) => {
    const pattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
    return pattern.test(v) || t('invalid_email_format');
  },
  (v: string) => (v && v.length >= 1 && v.length <= 30) || t('company_email_length'),
];

const phoneRules = [
  (v: string) => !!v || t('field_required'),
  (v: string) => (v && v.length >= 1 && v.length <= 15) || t('company_phone_length'),
  (v: string) => {
    const pattern = /^\+?\d{1,15}$/;
    return pattern.test(v) || t('invalid_phone_format');
  },
];

const addressRules = [(v: string) => !!v || t('field_required'), (v: string) => (v && v.length >= 1 && v.length <= 100) || t('company_address_length')];

const cbuRules = [
  (v: string) => !!v || t('field_required'),
  (v: string) => {
    const pattern = /^\d{1,22}$/;
    return pattern.test(v) || t('cbu_length');
  },
];

const imageRules = [
  (v: File | File[] | null) => {
    if (!v) return true;

    const file = Array.isArray(v) ? v[0] : v;
    if (!file) return true;

    const validTypes = ['image/jpeg', 'image/jpg', 'image/png'];
    return validTypes.includes(file.type) || t('product_image_type');
  },
  (v: File | File[] | null) => {
    if (!v) return true;

    const file = Array.isArray(v) ? v[0] : v;
    if (!file) return true;

    const maxSize = 10 * 1024 * 1024;
    return file.size <= maxSize || t('file_size_exceeds_limit');
  },
];

const loadCompany = async () => {
  try {
    isLoadingCompany.value = true;
    company.value = authStore.currentCompany!;

    form.value.name = company.value.name;
    form.value.address = company.value.address;
    form.value.email = company.value.email;
    form.value.phone = company.value.phone;
    form.value.cbu = company.value.cbu;

    if (company.value.imageUri) {
      currentImageUrl.value = company.value.imageUri;
      currentImageUri.value = company.value.imageUri;
    }
  } catch (error) {
    console.error('Failed to load company:', error);
    handleApiError(error);
    router.push('/profile');
  } finally {
    isLoadingCompany.value = false;
  }
};

const handleImageChange = () => {
  if (!imageFile.value) {
    imagePreview.value = null;
    return;
  }

  const file = Array.isArray(imageFile.value) ? imageFile.value[0] : imageFile.value;

  if (!file || !(file instanceof File)) {
    imagePreview.value = null;
    return;
  }

  const reader = new FileReader();
  reader.onload = (e) => {
    imagePreview.value = e.target?.result as string;
  };
  reader.readAsDataURL(file);

  fieldErrors.value.document = '';
};

const handleUpdate = async () => {
  if (!companyForm.value) return;

  const { valid } = await companyForm.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;

    fieldErrors.value = {
      email: '',
      phone: '',
      address: '',
      cbu: '',
      document: '',
    };

    const updateData: EditCompanyData = {
      address: form.value.address,
      email: form.value.email,
      phone: form.value.phone,
      cbu: form.value.cbu,
    };

    if (imageFile.value) {
      const file = Array.isArray(imageFile.value) ? imageFile.value[0] : imageFile.value;

      if (file && file instanceof File) {
        const documentUri = await documentService.upload(file);
        updateData.imageUri = documentUri;
      }
    }

    await companyService.update(company.value!.id, updateData);
    await authStore.fetchUserCompanyAndLocation(authStore.userId!, authStore.role!);

    showSuccess('company_info_updated_success');
    router.push('/profile');
  } catch (err) {
    console.error('Company update failed:', err);
    const { validationErrors } = handleApiError(err);
    if (validationErrors && Object.keys(validationErrors).length > 0) {
      fieldErrors.value = {
        ...fieldErrors.value,
        ...validationErrors,
      };
    }
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.back();
};

onMounted(async () => {
  try {
    await loadCompany();
  } catch (error) {
    console.error('Failed to initialize edit page:', error);
    handleApiError(error);
  }
});
</script>

<style scoped>
.company-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.company-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.company-header::before {
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

.image-upload-card {
  background: #f8f9fa;
  border: 2px dashed rgba(var(--v-theme-primary), 0.3);
  transition: all 0.3s ease;
}

.image-upload-card:hover {
  border-color: rgba(var(--v-theme-primary), 0.6);
  background: #f1f3f4;
}

.company-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.company-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.v-text-field :deep(.v-field),
.v-file-input :deep(.v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.v-text-field :deep(.v-field:hover),
.v-file-input :deep(.v-field:hover) {
  background: #f1f3f4;
}

.v-text-field :deep(.v-field--focused .v-field__outline),
.v-file-input :deep(.v-field--focused .v-field__outline) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field--focused .v-label),
.v-file-input :deep(.v-field--focused .v-label) {
  color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field__prepend-inner .v-icon),
.v-file-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary));
}

.v-theme--dark .company-edit-page {
  background: #121212 !important;
}

.v-theme--dark .company-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.v-theme--dark .image-upload-card {
  background: #2a2a2a !important;
}

.v-theme--dark .image-upload-card:hover {
  background: #333333 !important;
}

.v-theme--dark .v-text-field :deep(.v-field),
.v-theme--dark .v-file-input :deep(.v-field) {
  background: #2a2a2a !important;
  color: white !important;
}

.v-theme--dark .v-text-field :deep(.v-field:hover),
.v-theme--dark .v-file-input :deep(.v-field:hover) {
  background: #333333 !important;
}

@media (max-width: 600px) {
  .company-card {
    margin: 16px;
  }

  .company-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
