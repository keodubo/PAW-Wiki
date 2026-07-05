<template>
  <div class="pool-edit-page">
    <v-container fluid class="d-flex justify-center align-center min-vh-100 pa-4">
      <v-row justify="center" class="w-100 ma-0">
        <v-col cols="12" sm="10" md="8" lg="6" xl="5">
          <v-card class="pool-card elevation-12 rounded-xl overflow-hidden">
            <v-card-title class="pool-header pa-6 text-center">
              <div class="header-section">
                <v-icon size="64" color="white" class="mb-3">mdi-account-group</v-icon>
                <h1 class="text-h4 font-weight-bold text-white mb-1">
                  {{ $t('edit_pool') }}
                </h1>
                <p class="text-subtitle-2 text-white opacity-90">
                  {{ $t('edit_pool_subtitle') }}
                </p>
              </div>
            </v-card-title>

            <v-card-text class="pa-6">
              <v-form @submit.prevent="handleUpdate" ref="poolForm">
                <v-card v-if="currentPool" class="mb-4" outlined>
                  <v-card-text>
                    <div class="text-subtitle-2 text-medium-emphasis mb-3">{{ $t('pool_information') }}</div>

                    <div class="d-flex align-center mb-2">
                      <v-icon size="small" class="mr-2" color="primary">mdi-package-variant</v-icon>
                      <span class="text-body-2">
                        <strong>{{ $t('product') }}:</strong> {{ productName }}
                      </span>
                    </div>

                    <div class="d-flex align-center mb-2">
                      <v-icon size="small" class="mr-2" color="primary">mdi-map-marker</v-icon>
                      <span class="text-body-2">
                        <strong>{{ $t('location') }}:</strong> {{ locationName }}
                      </span>
                    </div>

                    <div class="d-flex align-center mb-2">
                      <v-icon size="small" class="mr-2" color="primary">mdi-percent</v-icon>
                      <span class="text-body-2">
                        <strong>{{ $t('down_payment') }}:</strong> {{ currentPool.downPayment }}%
                      </span>
                    </div>

                    <div class="d-flex align-center">
                      <v-icon size="small" class="mr-2" color="primary">mdi-information</v-icon>
                      <span class="text-body-2">
                        <strong>{{ $t('status') }}:</strong>
                        <v-chip size="x-small" :color="getStatusColor(currentPool.status)" class="ml-2">
                          {{ $t(`pool_status.${currentPool.status}`) }}
                        </v-chip>
                      </span>
                    </div>
                  </v-card-text>
                </v-card>

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

                <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
                  {{ $t('pool_edit_info_message') }}
                </v-alert>

                <v-btn type="submit" color="primary" size="x-large" block :loading="isLoading" :disabled="!isFormValid" class="pool-btn mb-4" elevation="2">
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
import { poolService } from '@/services';
import { useProductsStore } from '@/stores/products';
import { useLocationsStore } from '@/stores/locations';
import type { Pool } from '@/models';
import { UserRole } from '@/models/UserRole';
import { useAuthStore } from '@/stores/auth';
import { useCompaniesStore } from '@/stores/companies';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_VERIFIED],
  },
  beforeEnter: async (to, from, next) => {
    const authStore = useAuthStore();
    const productsStore = useProductsStore();
    const companiesStore = useCompaniesStore();

    const poolId = Number((to.params as { id?: string }).id ?? '0');

    try {
      const poolData = await poolService.getById(poolId);

      const productData = await productsStore.fetch(poolData.productUri);
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

const router = useRouter();
const route = useRoute();
const { t } = useI18n();
const { showSuccess, handleApiError } = useNotifications();
const productsStore = useProductsStore();
const locationsStore = useLocationsStore();

const poolForm = ref();
const isLoading = ref(false);
const isLoadingPool = ref(true);
const currentPool = ref<Pool | null>(null);

const form = ref({
  minQuantity: null as number | null,
});

const fieldErrors = ref({
  minQuantity: '',
});

const poolId = computed(() => parseInt((route.params as { id: string }).id));

const isFormValid = computed(() => {
  return form.value.minQuantity !== null && form.value.minQuantity >= 1;
});

const productName = computed(() => {
  if (!currentPool.value) return '';
  const product = productsStore.getEntry(currentPool.value.productUri);
  return product?.name || t('loading');
});

const locationName = computed(() => {
  if (!currentPool.value) return '';
  const location = locationsStore.getEntry(currentPool.value.locationUri);
  return location?.name || t('loading');
});

const minQuantityRules = [(v: number) => (v !== null && v !== undefined) || t('field_required'), (v: number) => v >= 1 || t('pool_min_quantity_min')];

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    AVAILABLE: 'success',
    DELIVERING: 'info',
    PAUSED: 'warning',
    CANCELLED: 'error',
    FINISHED: 'grey',
  };
  return colors[status] || 'grey';
};

const loadPool = async () => {
  try {
    isLoadingPool.value = true;
    const pool = await poolService.getById(poolId.value);
    currentPool.value = pool;

    form.value.minQuantity = pool.minQuantity;

    await Promise.all([productsStore.fetch(pool.productUri), locationsStore.fetch(pool.locationUri)]);
  } catch (error) {
    console.error('Failed to load pool:', error);
    handleApiError(error);
    router.push('/pools');
  } finally {
    isLoadingPool.value = false;
  }
};

const handleUpdate = async () => {
  if (!poolForm.value) return;

  const { valid } = await poolForm.value.validate();
  if (!valid) return;

  try {
    isLoading.value = true;

    fieldErrors.value = {
      minQuantity: '',
    };

    const updateData = {
      minQuantity: form.value.minQuantity!,
    };

    await poolService.update(poolId.value, updateData);
    showSuccess('pool_updated_success');
    router.push(`/pools/${poolId.value}`);
  } catch (err) {
    console.error('Pool update failed:', err);
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
    await loadPool();
  } catch (error) {
    console.error('Failed to initialize edit page:', error);
    handleApiError(error);
  }
});
</script>

<style scoped>
.pool-edit-page {
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

.v-theme--dark .pool-edit-page {
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
