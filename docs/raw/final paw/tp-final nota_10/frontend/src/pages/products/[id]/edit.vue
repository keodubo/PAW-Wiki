<template>
  <div class="product-edit-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="product-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="product-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-package-variant-closed</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('edit_product') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('edit_product_subtitle') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleUpdate" ref="productForm">
                <v-text-field
                  v-model="form.name"
                  :label="$t('product_name')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-tag"
                  color="primary"
                  class="mb-2"
                  :rules="nameRules"
                  :error-messages="fieldErrors.name"
                  counter="30"
                  required
                />

                <v-textarea
                  v-model="form.description"
                  :label="$t('product_description')"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-text"
                  color="primary"
                  class="mb-2"
                  :rules="descriptionRules"
                  :error-messages="fieldErrors.description"
                  rows="4"
                  counter="1024"
                  required
                />

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model.number="form.price"
                      :label="$t('product_price')"
                      type="number"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-currency-usd"
                      color="primary"
                      class="mb-2"
                      :rules="priceRules"
                      :error-messages="fieldErrors.price"
                      step="0.01"
                      min="0"
                      required
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-select
                      v-model="form.categoryUri"
                      :label="$t('product_category')"
                      :items="categories"
                      item-title="name"
                      item-value="selfUri"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-shape"
                      color="primary"
                      class="mb-2"
                      :rules="[rules.required]"
                      :error-messages="fieldErrors.categoryUri"
                      required
                    />
                  </v-col>
                </v-row>

                <v-card v-if="imagePreview || currentImageUrl" class="mb-4" outlined>
                  <v-card-text>
                    <div class="d-flex align-center justify-space-between mb-2">
                      <div class="d-flex align-center">
                        <v-icon color="primary" class="mr-2">mdi-image</v-icon>
                        <span class="text-subtitle-1 font-weight-medium">
                          {{ imagePreview ? $t('product_image') : $t('current_image') }}
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
                    <v-img :src="(imagePreview || currentImageUrl) ?? undefined" max-height="200" contain class="rounded" />
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

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="product-btn mb-4" elevation="2">
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
import { productService, documentService, type EditProductData } from '@/services';
import { useCategoriesStore } from '@/stores/categories';
import { useCompaniesStore } from '@/stores/companies';
import { useProductsStore } from '@/stores/products';
import type { EnrichedProduct } from '@/models';
import { useAuthStore } from '@/stores/auth';
import { UserRole } from '@/models/UserRole';

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const { showSuccess, handleApiError } = useNotifications();
const categoriesStore = useCategoriesStore();
const companiesStore = useCompaniesStore();

const productForm = ref();
const isLoading = ref(false);
const imageFile = ref<File | File[] | null>(null);
const imagePreview = ref<string | null>(null);
const currentProduct = ref<EnrichedProduct | null>(null);
const currentImageUrl = ref<string | null>(null);
const currentImageUri = ref<string | null>(null);

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_VERIFIED],
  },
  beforeEnter: async (to, from, next) => {
    const authStore = useAuthStore();
    const companiesStore = useCompaniesStore();

    const productId = Number((to.params as { id?: string }).id ?? '0');

    try {
      const productData = await productService.getById(productId);

      const companyData = await companiesStore.fetch(productData.companyUri);

      const currentCompanyId = authStore.currentCompany?.id;

      if (currentCompanyId !== companyData.id) {
        return next({ path: '/unauthorized' });
      }

      next();
    } catch (error) {
      return next({ path: '/not-found' });
    }
  },
});

const form = ref({
  name: '',
  description: '',
  price: 0,
  categoryUri: null as string | null,
});

const fieldErrors = ref({
  name: '',
  description: '',
  price: '',
  categoryUri: '',
  document: '',
});

const categories = computed(() => categoriesStore.items);

const productId = computed(() => parseInt((route.params as { id?: string }).id ?? '0'));

const isFormValid = computed(() => {
  return form.value.name && form.value.description && form.value.price !== null && form.value.price >= 0 && form.value.categoryUri;
});

const rules = {
  required: (v: any) => !!v || t('field_required'),
};

const nameRules = [(v: string) => !!v || t('field_required'), (v: string) => (v && v.length >= 1 && v.length <= 30) || t('product_name_length_edit')];

const descriptionRules = [(v: string) => !!v || t('field_required'), (v: string) => (v && v.length >= 1 && v.length <= 1024) || t('product_description_length_edit')];

const priceRules = [
  (v: number) => (v !== null && v !== undefined) || t('field_required'),
  (v: number) => typeof v === 'number' || t('field_required'),
  (v: number) => v >= 0 || t('product_price_min_edit'),
  (v: number) => {
    const price = v.toString();
    const parts = price.split('.');
    if (parts.length > 1 && parts[1].length > 2) {
      return t('product_price_decimals');
    }
    return true;
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

    const maxSize = 5 * 1024 * 1024;
    return file.size <= maxSize || t('product_image_size');
  },
];

const loadProduct = async () => {
  const id = Number((route.params as { id?: string }).id ?? '0');
  if (Number.isNaN(id)) {
    router.replace('/products');
    return;
  }
  try {
    isLoading.value = true;
    const fetched = await productService.getById(id);

    const company = fetched.companyUri ? await companiesStore.fetch(fetched.companyUri) : undefined;
    const category = fetched.categoryUri ? await categoriesStore.fetch(fetched.categoryUri) : undefined;

    currentProduct.value = {
      ...fetched,
      companyData: company,
      categoryData: category,
    };

    form.value.name = currentProduct.value.name;
    form.value.description = currentProduct.value.description;
    form.value.price = currentProduct.value.price;
    if (category) form.value.categoryUri = category.selfUri;
    if (fetched.imageUri) {
      currentImageUrl.value = fetched.imageUri;
      currentImageUri.value = fetched.imageUri;
    }
  } finally {
    isLoading.value = false;
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
  if (!productForm.value) return;

  const { valid } = await productForm.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;

    fieldErrors.value = {
      name: '',
      description: '',
      price: '',
      categoryUri: '',
      document: '',
    };

    const updateData: EditProductData = {
      name: form.value.name,
      description: form.value.description,
      price: form.value.price,
      categoryUri: form.value.categoryUri!,
    };

    if (imageFile.value) {
      const file = Array.isArray(imageFile.value) ? imageFile.value[0] : imageFile.value;

      if (file && file instanceof File) {
        const documentUri = await documentService.upload(file);
        updateData.imageUri = documentUri;
      }
    }

    await productService.update(productId.value, updateData);
    showSuccess('product_updated_success');
    router.push('/my-products');
  } catch (err) {
    console.error('Product update failed:', err);
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
    await Promise.all([categoriesStore.fetchAll(), loadProduct()]);
  } catch (error) {
    console.error('Failed to initialize edit page:', error);
    handleApiError(error);
  }
});
</script>

<style scoped>
.product-edit-page {
  min-height: 100vh;
  background: #ffffff;
}

.product-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.product-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.product-header::before {
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

.product-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.product-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(var(--v-theme-primary), 0.4) !important;
}

.v-text-field :deep(.v-field),
.v-textarea :deep(.v-field),
.v-select :deep(.v-field),
.v-file-input :deep(.v-field) {
  border-radius: 12px;
  background: #f8f9fa;
}

.v-text-field :deep(.v-field:hover),
.v-textarea :deep(.v-field:hover),
.v-select :deep(.v-field:hover),
.v-file-input :deep(.v-field:hover) {
  background: #f1f3f4;
}

.v-text-field :deep(.v-field--focused .v-field__outline),
.v-textarea :deep(.v-field--focused .v-field__outline),
.v-select :deep(.v-field--focused .v-field__outline),
.v-file-input :deep(.v-field--focused .v-field__outline) {
  border-color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field--focused .v-label),
.v-textarea :deep(.v-field--focused .v-label),
.v-select :deep(.v-field--focused .v-label),
.v-file-input :deep(.v-field--focused .v-label) {
  color: rgb(var(--v-theme-primary)) !important;
}

.v-text-field :deep(.v-field__prepend-inner .v-icon),
.v-textarea :deep(.v-field__prepend-inner .v-icon),
.v-select :deep(.v-field__prepend-inner .v-icon),
.v-file-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary));
}

.v-theme--dark .product-edit-page {
  background: #121212 !important;
}

.v-theme--dark .product-card {
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
.v-theme--dark .v-textarea :deep(.v-field),
.v-theme--dark .v-select :deep(.v-field),
.v-theme--dark .v-file-input :deep(.v-field) {
  background: #2a2a2a !important;
  color: white !important;
}

.v-theme--dark .v-text-field :deep(.v-field:hover),
.v-theme--dark .v-textarea :deep(.v-field:hover),
.v-theme--dark .v-select :deep(.v-field:hover),
.v-theme--dark .v-file-input :deep(.v-field:hover) {
  background: #333333 !important;
}

@media (max-width: 600px) {
  .product-card {
    margin: 16px;
  }

  .product-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
