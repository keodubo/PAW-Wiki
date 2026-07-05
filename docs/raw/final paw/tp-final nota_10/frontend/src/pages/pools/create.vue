<template>
  <div class="pool-create-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="pool-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="pool-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-account-group-outline</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('create_pool') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('create_pool_subtitle') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleCreate" ref="poolForm">
                <!-- Product Selector -->
                <div class="mb-4">
                  <label class="text-subtitle-2 font-weight-bold d-block mb-2">
                    {{ $t('pool_product') }}
                  </label>

                  <ProductSelectorDialog
                    :company-id="authStore.currentCompany?.id"
                    :active-only="true"
                    @select="handleProductSelect"
                  >
                    <template #activator="{ props }">
                      <v-card
                        v-bind="props"
                        class="product-selector-card"
                        :class="{ 'error-border': fieldErrors.productUri }"
                        variant="outlined"
                        hover
                      >
                        <v-card-text v-if="!selectedProduct" class="text-center pa-6">
                          <v-icon size="48" color="primary">mdi-package-variant</v-icon>
                          <p class="text-body-2 mt-2 mb-0">{{ $t('click_to_select_product') }}</p>
                        </v-card-text>

                        <v-card-text v-else class="d-flex align-center pa-4">
                          <v-avatar size="60" rounded class="mr-4">
                            <v-img :src="selectedProduct.imageUri" :alt="selectedProduct.name">
                              <template #placeholder>
                                <v-img src="@/assets/empty.svg" :alt="selectedProduct.name" />
                              </template>
                            </v-img>
                          </v-avatar>
                          <div class="flex-grow-1">
                            <div class="font-weight-medium">{{ selectedProduct.name }}</div>
                            <div class="text-body-2 text-medium-emphasis">
                              {{ formatCurrency(selectedProduct.price) }}
                            </div>
                          </div>
                          <v-icon color="primary">mdi-chevron-right</v-icon>
                        </v-card-text>
                      </v-card>
                    </template>
                  </ProductSelectorDialog>

                  <div v-if="fieldErrors.productUri" class="text-error text-caption mt-1 ml-3">
                    {{ fieldErrors.productUri }}
                  </div>
                </div>

                <v-select
                  v-model="form.locationUri"
                  :label="$t('pool_location')"
                  :items="locations"
                  item-title="name"
                  item-value="selfUri"
                  variant="outlined"
                  density="comfortable"
                  prepend-inner-icon="mdi-map-marker"
                  color="primary"
                  class="mb-2"
                  :rules="[rules.required]"
                  :error-messages="fieldErrors.locationUri"
                  required
                />

                <v-row>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model.number="form.minQuantity"
                      :label="$t('pool_min_quantity')"
                      type="number"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-counter"
                      color="primary"
                      class="mb-2"
                      :rules="minQuantityRules"
                      :error-messages="fieldErrors.minQuantity"
                      min="1"
                      required
                    />
                  </v-col>
                  <v-col cols="12" sm="6">
                    <v-text-field
                      v-model.number="form.downPayment"
                      :label="$t('pool_down_payment')"
                      type="number"
                      variant="outlined"
                      density="comfortable"
                      prepend-inner-icon="mdi-percent"
                      color="primary"
                      class="mb-2"
                      :rules="downPaymentRules"
                      :error-messages="fieldErrors.downPayment"
                      suffix="%"
                      min="0"
                      max="100"
                      required
                    />
                  </v-col>
                </v-row>

                <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
                  {{ $t('pool_info_message') }}
                </v-alert>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="pool-btn mb-4" elevation="2">
                  <template #prepend v-if="!isLoading">
                    <v-icon>mdi-plus-circle</v-icon>
                  </template>
                  {{ $t('create_pool') }}
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
import { useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import { useNotifications } from '@/composables/useNotifications';
import { poolService } from '@/services';
import { useAuthStore } from '@/stores/auth';
import { useLocationsStore } from '@/stores/locations';
import type { Product } from '@/models';
import { UserRole } from '@/models/UserRole';
import { formatCurrency } from '@/utils/currency';
import ProductSelectorDialog from '@/components/ProductSelectorDialog.vue';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_VERIFIED],
  },
});

const router = useRouter();
const { t } = useI18n();
const { showSuccess, handleApiError } = useNotifications();
const authStore = useAuthStore();
const locationsStore = useLocationsStore();

const poolForm = ref();
const isLoading = ref(false);
const selectedProduct = ref<Product | null>(null);

const form = ref({
  productUri: null as string | null,
  locationUri: null as string | null,
  minQuantity: null as number | null,
  downPayment: 0,
});

const fieldErrors = ref({
  productUri: '',
  locationUri: '',
  minQuantity: '',
  downPayment: '',
});

const locations = computed(() => locationsStore.items);

const isFormValid = computed(() => {
  return form.value.productUri !== null && form.value.locationUri !== null && form.value.minQuantity !== null && form.value.minQuantity >= 1 && form.value.downPayment !== null && form.value.downPayment >= 0 && form.value.downPayment <= 100;
});

const rules = {
  required: (v: any) => !!v || t('field_required'),
};

const minQuantityRules = [(v: number) => (v !== null && v !== undefined) || t('field_required'), (v: number) => v >= 1 || t('pool_min_quantity_min')];

const downPaymentRules = [(v: number) => (v !== null && v !== undefined) || t('field_required'), (v: number) => v >= 0 || t('pool_down_payment_min'), (v: number) => v <= 100 || t('pool_down_payment_max')];

const handleProductSelect = (product: Product) => {
  selectedProduct.value = product;
  form.value.productUri = product.selfUri;
  fieldErrors.value.productUri = '';
};

const loadLocations = async () => {
  try {
    await locationsStore.fetchAll();
  } catch (error) {
    console.error('Failed to load locations:', error);
    handleApiError(error);
  }
};

const handleCreate = async () => {
  if (!poolForm.value) return;

  const { valid } = await poolForm.value.validate();
  if (!valid) return;

  if (!form.value.productUri) {
    fieldErrors.value.productUri = t('field_required');
    return;
  }

  try {
    isLoading.value = true;

    fieldErrors.value = {
      productUri: '',
      locationUri: '',
      minQuantity: '',
      downPayment: '',
    };

    const createData = {
      productUri: form.value.productUri!,
      locationUri: form.value.locationUri!,
      minQuantity: form.value.minQuantity!,
      downPayment: form.value.downPayment,
    };

    await poolService.create(createData);
    showSuccess('pool_created_success');
    router.push('/pools');
  } catch (err) {
    console.error('Pool creation failed:', err);
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
    await loadLocations();
  } catch (error) {
    console.error('Failed to initialize pool creation:', error);
    handleApiError(error);
  }
});
</script>

<style scoped>
.pool-create-page {
  min-height: 100vh;
  background: #ffffff;
}

.pool-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  max-width: 700px;
  margin: 0 auto;
}

.pool-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.pool-header::before {
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

.product-selector-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px !important;
}

.product-selector-card:hover {
  border-color: rgb(var(--v-theme-primary)) !important;
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.15);
}

.product-selector-card.error-border {
  border-color: rgb(var(--v-theme-error)) !important;
}

.pool-btn {
  height: 56px !important;
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  transition: all 0.3s ease;
}

.pool-btn:hover:not(.v-btn--disabled) {
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

.v-theme--dark .pool-create-page {
  background: #121212 !important;
}

.v-theme--dark .pool-card {
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
  .pool-card {
    margin: 16px;
  }

  .pool-header {
    padding: 20px 16px !important;
  }

  .v-card-text {
    padding: 20px 16px !important;
  }
}
</style>
