<template>
  <div class="my-requests-page">
    <v-container fluid>
      <v-row justify="center" class="mb-4">
        <v-col cols="12" md="10" lg="8">
          <v-row align="center">
            <v-col cols="12" md="3"> </v-col>

            <v-col cols="12" md="6">
              <div class="search-container d-flex align-center ga-3">
                <v-form @submit.prevent="handleSearch" ref="searchForm" class="flex-grow-1">
                  <v-text-field
                    v-model="filters.search"
                    :placeholder="$t('requests_search_placeholder')"
                    variant="solo"
                    density="compact"
                    prepend-inner-icon="mdi-magnify"
                    color="primary"
                    base-color="grey-lighten-1"
                    class="search-input elevation-6 rounded-xl"
                    hide-details
                    clearable
                    flat
                  />
                </v-form>

                <v-btn type="submit" color="primary" size="x-large" class="search-btn elevation-6 rounded-xl" :loading="isSearching" @click="handleSearch" icon>
                  <v-icon size="large">mdi-magnify</v-icon>
                </v-btn>

                <v-btn v-if="hasActiveFilters" color="error" variant="elevated" size="x-large" class="clear-btn elevation-6 rounded-xl" @click="handleClearFilters" :title="$t('clear_filters')" icon>
                  <v-icon size="large">mdi-filter-remove</v-icon>
                </v-btn>
              </div>
            </v-col>

            <v-col cols="12" md="3" class="text-center text-md-right"> </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row justify="center" class="mb-4">
        <v-col cols="12" md="10" lg="8" class="d-flex justify-center flex-wrap ga-3">
          <CompanySelectorDialog @select="handleCompanySelect">
            <template #activator="{ props: dialogProps }">
              <v-btn
                v-bind="dialogProps"
                variant="outlined"
                color="primary"
                size="large"
                class="text-none elevation-2 company-filter-btn"
                prepend-icon="mdi-domain"
              >
                <template v-if="selectedCompany">
                  <span class="text-truncate" style="max-width: 120px">{{ selectedCompany.name }}</span>
                  <v-btn
                    icon
                    size="x-small"
                    variant="text"
                    class="ml-1"
                    @click.stop="clearCompanyFilter"
                  >
                    <v-icon size="small">mdi-close</v-icon>
                  </v-btn>
                </template>
                <template v-else>
                  {{ $t('company') }}: {{ $t('all') }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </template>
              </v-btn>
            </template>
          </CompanySelectorDialog>

          <ProductSelectorDialog :active-only="false" @select="handleProductSelect">
            <template #activator="{ props: dialogProps }">
              <v-btn
                v-bind="dialogProps"
                variant="outlined"
                color="primary"
                size="large"
                class="text-none elevation-2 product-filter-btn"
                prepend-icon="mdi-package-variant"
              >
                <template v-if="selectedProduct">
                  <span class="text-truncate" style="max-width: 120px">{{ selectedProduct.name }}</span>
                  <v-btn
                    icon
                    size="x-small"
                    variant="text"
                    class="ml-1"
                    @click.stop="clearProductFilter"
                  >
                    <v-icon size="small">mdi-close</v-icon>
                  </v-btn>
                </template>
                <template v-else>
                  {{ $t('product') }}: {{ $t('all') }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </template>
              </v-btn>
            </template>
          </ProductSelectorDialog>

          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn v-bind="menuProps" variant="outlined" color="primary" size="large" class="text-none elevation-2" prepend-icon="mdi-chart-pie">
                {{ $t('pool_status_label') }}: {{ $t(currentPoolStatusText) }}
                <v-icon end>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-for="option in poolStatusOptions" :key="option.value ?? 'all'" :class="{ 'bg-primary-lighten-4': poolStatus === option.value }" @click="setPoolStatus(option.value)">
                <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>

          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn v-bind="menuProps" variant="outlined" color="primary" size="large" class="text-none elevation-2" prepend-icon="mdi-chart-box-outline">
                {{ $t('request_status_label') }}: {{ $t(currentRequestStatusText) }}
                <v-icon end>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-for="option in requestStatusOptions" :key="option.value ?? 'all'" :class="{ 'bg-primary-lighten-4': requestStatus === option.value }" @click="setRequestStatus(option.value)">
                <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>

          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn v-bind="menuProps" variant="outlined" color="primary" size="large" class="text-none elevation-2" prepend-icon="mdi-sort">
                {{ $t('sort') }}: {{ $t(currentSortText) }}
                <v-icon end>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-for="option in sortOptions" :key="option.value" :class="{ 'bg-primary-lighten-4': currentSort === option.value }" @click="setSortOrder(option.orderBy, option.desc)">
                <template #prepend>
                  <v-icon :icon="option.icon" size="small" class="mr-2" />
                </template>
                <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </v-col>
      </v-row>

      <v-row v-if="!isLoading" justify="center" class="mb-2">
        <v-col cols="12" md="10" lg="8">
          <p class="text-body-2 text-medium-emphasis">
            <span class="font-weight-bold">{{ $t('total') }}:</span> {{ enrichedRequests.length }} {{ enrichedRequests.length === 1 ? $t('request') : $t('requests') }}
          </p>
        </v-col>
      </v-row>

      <div v-if="enrichedRequests.length === 0 && !isLoading" class="text-center py-12">
        <v-img src="@/assets/empty.svg" alt="No requests found" max-width="300" class="mx-auto mb-4" />
        <h3 class="text-h6 text-medium-emphasis mb-4">{{ $t('no_requests_found') }}</h3>
        <p v-if="hasActiveFilters" class="text-body-1 text-medium-emphasis mb-4">{{ $t('try_adjusting_filters') }}</p>
        <p v-else class="text-body-1 text-medium-emphasis mb-4">{{ $t('no_requests_made_yet') }}</p>
        <v-btn v-if="hasActiveFilters" color="error" variant="outlined" @click="handleClearFilters" prepend-icon="mdi-filter-remove"> {{ $t('clear_filters') }} </v-btn>
        <v-btn v-else color="primary" variant="elevated" to="/pools" prepend-icon="mdi-magnify"> {{ $t('browse_pools') }} </v-btn>
      </div>

      <v-row v-else justify="center">
        <v-col cols="12" md="10" lg="8">
          <template v-if="isLoading">
            <v-row>
              <v-col v-for="n in 6" :key="`skeleton-${n}`" cols="12" sm="6" md="6" lg="4" xl="4">
                <v-skeleton-loader type="card" />
              </v-col>
            </v-row>
          </template>

          <template v-else>
            <v-row>
              <v-col v-for="enrichedRequest in enrichedRequests" :key="enrichedRequest.request.id" cols="12" sm="6" md="6" lg="4" xl="4">
                <PoolCard
                  :enrichedPool="{
                    pool: enrichedRequest.pool,
                    product: enrichedRequest.product,
                    location: enrichedRequest.location,
                    company: enrichedRequest.company,
                    category: enrichedRequest.category,
                  }"
                  :showProductDetails="true"
                  :showUserRequest="true"
                  :userRequest="{
                    id: enrichedRequest.request.id,
                    quantity: enrichedRequest.request.quantity,
                    status: enrichedRequest.request.status,
                    downPaymentUri: enrichedRequest.request.downPaymentUri,
                    finalPaymentUri: enrichedRequest.request.finalPaymentUri,
                  }"
                />
              </v-col>
            </v-row>
          </template>
        </v-col>
      </v-row>

      <v-row v-if="pagination.links" justify="center" class="mt-6">
        <v-col cols="12" md="10" lg="8" class="d-flex justify-center">
          <PaginationLinks :links="pagination.links" :current-page="pagination.currentPage" :total-pages="pagination.totalPages" @navigate="handleLinkNavigation" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import PoolCard from '@/components/PoolCard.vue';
import PaginationLinks from '@/components/PaginationLinks.vue';
import CompanySelectorDialog from '@/components/CompanySelectorDialog.vue';
import ProductSelectorDialog from '@/components/ProductSelectorDialog.vue';
import { useMyRequestsPage } from '@/composables/useMyRequestsPage';
import { UserRole } from '@/models/UserRole';
import type { Company, Product } from '@/models';
import { companyService, productService } from '@/services';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.CLIENT],
  },
});

const myRequestsPage = useMyRequestsPage();

const searchForm = ref();
const selectedCompany = ref<Company | null>(null);
const selectedProduct = ref<Product | null>(null);

const {
  enrichedRequests,
  sortOptions,
  poolStatusOptions,
  requestStatusOptions,

  isLoading,
  isSearching,
  filters,
  pagination,

  currentPage,
  totalPages,
  totalItems,
  links,

  hasActiveFilters,
  currentSort,
  currentSortText,

  poolStatus,
  requestStatus,
  productId,
  companyId,

  handleSearch,
  clearFilters,
  handlePageChange,
  handleLinkNavigation,
  setPoolStatus,
  setRequestStatus,
  setProductId,
  setCompanyId,
  setSortOrder,
  initialize,
} = myRequestsPage;

const currentPoolStatusText = computed(() => {
  const option = poolStatusOptions.find((opt: { value: any; text: string }) => opt.value === poolStatus.value);
  return option ? option.text : 'all';
});

const currentRequestStatusText = computed(() => {
  const option = requestStatusOptions.find((opt: { value: any; text: string }) => opt.value === requestStatus.value);
  return option ? option.text : 'all';
});

const handleCompanySelect = (company: Company) => {
  selectedCompany.value = company;
  setCompanyId(company.id);
};

const clearCompanyFilter = () => {
  selectedCompany.value = null;
  setCompanyId(null);
};

const handleProductSelect = (product: Product) => {
  selectedProduct.value = product;
  setProductId(product.id);
};

const clearProductFilter = () => {
  selectedProduct.value = null;
  setProductId(null);
};

const handleClearFilters = () => {
  selectedCompany.value = null;
  selectedProduct.value = null;
  clearFilters();
};

watch(companyId, async (newId) => {
  if (!newId) {
    selectedCompany.value = null;
  } else if (newId !== selectedCompany.value?.id) {
    try {
      selectedCompany.value = await companyService.getById(newId);
    } catch {
      selectedCompany.value = null;
    }
  }
});

watch(productId, async (newId) => {
  if (!newId) {
    selectedProduct.value = null;
  } else if (newId !== selectedProduct.value?.id) {
    try {
      selectedProduct.value = await productService.getById(newId);
    } catch {
      selectedProduct.value = null;
    }
  }
});

onMounted(async () => {
  await initialize();
  await Promise.all([
    companyId.value
      ? companyService.getById(companyId.value).then(c => { selectedCompany.value = c; }).catch(() => {})
      : Promise.resolve(),
    productId.value
      ? productService.getById(productId.value).then(p => { selectedProduct.value = p; }).catch(() => {})
      : Promise.resolve(),
  ]);
});
</script>

<style scoped>
.my-requests-page {
  min-height: 100vh;
  background: rgb(var(--v-theme-background));
  padding-top: 2rem;
}

.search-container {
  align-items: stretch;
}

.search-input :deep(.v-field) {
  height: 56px !important;
  background: rgb(var(--v-theme-surface)) !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(var(--v-theme-primary), 0.2) !important;
  border-radius: 12px !important;
}

.search-input :deep(.v-field--focused) {
  border: 2px solid rgb(var(--v-theme-primary)) !important;
  box-shadow: 0 8px 25px rgba(127, 0, 255, 0.3) !important;
}

.search-input :deep(.v-field__outline) {
  display: none !important;
}

.search-input :deep(.v-field__input) {
  min-height: 54px !important;
  padding-top: 0 !important;
  padding-bottom: 0 !important;
}

.search-input :deep(.v-field__prepend-inner) {
  padding-top: 0 !important;
  align-items: center !important;
}

.search-input :deep(.v-field__append-inner) {
  padding-top: 0 !important;
  align-items: center !important;
}

.search-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary)) !important;
}

.search-btn {
  background: linear-gradient(135deg, #7f00ff 0%, #a855f7 100%) !important;
  color: white !important;
  width: 56px;
  height: 56px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(127, 0, 255, 0.4) !important;
}

.clear-btn {
  width: 56px;
  height: 56px;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(244, 67, 54, 0.4) !important;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.bg-primary-lighten-4 {
  background-color: rgba(var(--v-theme-primary), 0.15) !important;
}

.company-filter-btn,
.product-filter-btn {
  max-width: 220px;
  overflow: hidden;
}

@media (max-width: 960px) {
  .my-requests-page {
    padding-top: 1rem;
  }
}
</style>
