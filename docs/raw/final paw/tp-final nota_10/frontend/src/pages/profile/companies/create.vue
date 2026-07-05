<template>
  <div class="company-create-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="company-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="company-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-office-building</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('add_company_information') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('fill_company_details') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleCreate" ref="companyForm">
                <v-text-field
                  v-model="form.name"
                  :label="$t('company_name')"
                  :placeholder="$t('company_name_placeholder')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-domain"
                  color="primary"
                  class="mb-2"
                  :rules="nameRules"
                  :error-messages="fieldErrors.name"
                  counter="30"
                  required
                />

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.email"
                      :label="$t('company_email')"
                      placeholder="company@example.com"
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
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model="form.phone"
                      :label="$t('phone_number')"
                      :placeholder="$t('phone_placeholder')"
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
                  :placeholder="$t('company_address_placeholder')"
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
                  :placeholder="$t('cbu_placeholder')"
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

                <v-card class="image-upload-card mb-4" outlined>
                  <v-card-text>
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="d-flex align-center">
                        <v-icon color="primary" class="mr-2">mdi-image</v-icon>
                        <span class="text-subtitle-1 font-weight-medium">{{ $t('company_logo_image') }}</span>
                      </div>
                    </div>

                    <v-file-input
                      v-model="imageFile"
                      :label="$t('select_image')"
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

                    <p class="text-caption text-medium-emphasis mt-2">
                      {{ $t('upload_logo_required') }}
                    </p>
                  </v-card-text>
                </v-card>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="company-btn mb-4" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-plus-circle</v-icon>
                  </template>
                  {{ $t('submit') }}
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
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotifications } from '@/composables/useNotifications';
import { companyService, documentService } from '@/services';
import { useAuthStore } from '@/stores/auth';
import type { CreateCompanyData } from '@/services/CompanyService';
import { UserRole } from '@/models/UserRole';

const router = useRouter();
const { t } = useI18n();
const { showSuccess, handleApiError } = useNotifications();
const authStore = useAuthStore();

const companyForm = ref();
const isLoading = ref(false);
const imageFile = ref<File | File[] | null>(null);
const imagePreview = ref<string | null>(null);

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_PENDING],
  },
});

const form = ref({
  name: '',
  address: '',
  email: '',
  phone: '',
  cbu: '',
});

const fieldErrors = ref({
  name: '',
  email: '',
  phone: '',
  address: '',
  cbu: '',
  document: '',
});

const isFormValid = computed(() => {
  return form.value.name && form.value.email && form.value.address && form.value.phone && form.value.cbu && imageFile.value !== null;
});

const rules = {
  required: (v: any) => !!v || t('field_required'),
};

const nameRules = [(v: string) => !!v || t('field_required'), (v: string) => (v && v.length >= 1 && v.length <= 30) || t('company_name_length')];

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
    if (!v) return t('company_image_required');

    const file = Array.isArray(v) ? v[0] : v;
    if (!file) return t('company_image_required');

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

const handleCreate = async () => {
  if (!companyForm.value) return;

  const { valid } = await companyForm.value.validate();
  if (!valid) return;

  if (!imageFile.value) {
    fieldErrors.value.document = t('company_image_required');
    return;
  }

  try {
    isLoading.value = true;

    fieldErrors.value = {
      name: '',
      email: '',
      phone: '',
      address: '',
      cbu: '',
      document: '',
    };

    const file = Array.isArray(imageFile.value) ? imageFile.value[0] : imageFile.value;
    if (!file || !(file instanceof File)) {
      fieldErrors.value.document = t('company_image_required');
      isLoading.value = false;
      return;
    }

    const documentUri = await documentService.upload(file);

    const companyData: CreateCompanyData = {
      ...form.value,
      imageUri: documentUri,
    };

    await companyService.create(companyData);
    await authStore.fetchUserCompanyAndLocation(authStore.userId!, authStore.role!);

    showSuccess('company_info_added_success');
    router.push('/profile');
  } catch (err) {
    console.error('Company creation failed:', err);
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

.v-theme--dark .company-create-page {
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
