<template>
  <div class="my-pools-page">
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
                    :placeholder="$t('pools_search_placeholder')"
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

            <v-col cols="12" md="3" class="text-center text-md-right">
              <v-tooltip v-if="!hasCbu" :text="$t('cbu_required_create_pool')">
                <template v-slot:activator="{ props: tooltipProps }">
                  <v-btn v-bind="tooltipProps" color="primary" size="large" :disabled="true" prepend-icon="mdi-plus-circle" class="create-btn elevation-6 rounded-xl">
                    {{ $t('create_pool') }}
                  </v-btn>
                </template>
              </v-tooltip>
              <v-btn v-else color="primary" size="large" class="create-btn elevation-6 rounded-xl" to="/pools/create" prepend-icon="mdi-plus-circle">
                {{ $t('create_pool') }}
              </v-btn>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row justify="center" class="mb-4">
        <v-col cols="12" md="10" lg="8" class="d-flex justify-center ga-3">
          <v-menu>
            <template #activator="{ props: menuProps }">
              <v-btn v-bind="menuProps" variant="outlined" color="primary" size="large" class="text-none elevation-2" prepend-icon="mdi-chart-box-outline">
                {{ $t('status') }}: {{ $t(currentStatusText) }}
                <v-icon end>mdi-chevron-down</v-icon>
              </v-btn>
            </template>
            <v-list>
              <v-list-item v-for="option in statusOptions" :key="option.value ?? 'all'" :class="{ 'bg-primary-lighten-4': status === option.value }" @click="setStatus(option.value)">
                <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>

          <ProductSelectorDialog
            :company-id="authStore.getCompanyId ?? undefined"
            :active-only="false"
            @select="handleProductSelect"
          >
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
          <p class="text-body-2 text-medium-emphasis">{{ $t('showing') }} {{ enrichedPools.length }} {{ $t('of') }} {{ totalItems }} {{ $t('pools') }}</p>
        </v-col>
      </v-row>

      <div v-if="enrichedPools.length === 0 && !isLoading" class="text-center py-12">
        <v-img src="@/assets/empty.svg" alt="No pools found" max-width="300" class="mx-auto mb-4" />
        <h3 class="text-h6 text-medium-emphasis mb-4">{{ $t('no_pools_found') }}</h3>
        <p v-if="hasActiveFilters" class="text-body-1 text-medium-emphasis mb-4">{{ $t('try_adjusting_filters') }}</p>
        <p v-else class="text-body-1 text-medium-emphasis mb-4">{{ $t('no_pools_created_yet') }}</p>
        <v-btn v-if="hasActiveFilters" color="error" variant="outlined" @click="handleClearFilters" prepend-icon="mdi-filter-remove"> {{ $t('clear_filters') }} </v-btn>
        <v-btn v-else-if="hasCbu" color="primary" variant="elevated" @click="createPool" prepend-icon="mdi-plus-circle"> {{ $t('create_your_first_pool') }} </v-btn>
      </div>

      <v-row v-else justify="center">
        <v-col cols="12" md="10" lg="8">
          <template v-if="isLoading">
            <v-row>
              <v-col v-for="n in 6" :key="`skeleton-${n}`" cols="12" sm="6">
                <v-skeleton-loader type="card" />
              </v-col>
            </v-row>
          </template>

          <template v-else>
            <v-row>
              <v-col v-for="enrichedPool in enrichedPools" :key="enrichedPool.pool.id" cols="12" sm="6">
                <MyPoolCard :enrichedPool="enrichedPool" />
              </v-col>
            </v-row>
          </template>
        </v-col>
      </v-row>

      <v-row v-if="links" justify="center" class="mt-6">
        <v-col cols="12" md="10" lg="8" class="d-flex justify-center">
          <PaginationLinks :links="links" :current-page="currentPage" :total-pages="totalPages" @navigate="handleLinkNavigation" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import MyPoolCard from '@/components/MyPoolCard.vue';
import PaginationLinks from '@/components/PaginationLinks.vue';
import ProductSelectorDialog from '@/components/ProductSelectorDialog.vue';
import { useMyPoolsPage } from '@/composables/useMyPoolsPage';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { UserRole } from '@/models/UserRole';
import type { Product } from '@/models';
import { productService } from '@/services';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_PENDING, UserRole.COMPANY_UNVERIFIED, UserRole.COMPANY_VERIFIED],
  },
});

const myPoolsPage = useMyPoolsPage();
const authStore = useAuthStore();
const router = useRouter();

const searchForm = ref();
const selectedProduct = ref<Product | null>(null);

const {
  enrichedPools,
  sortOptions,
  statusOptions,

  isLoading,
  isSearching,
  filters,
  pagination,
  status,
  productId,

  currentPage,
  totalPages,
  totalItems,
  links,

  hasActiveFilters,
  currentSort,
  currentSortText,

  handleSearch,
  clearFilters,
  handlePageChange,
  handleLinkNavigation,
  setStatus,
  setProductId,
  setSortOrder,
  initialize,
} = myPoolsPage;

const hasCbu = computed(() => {
  return authStore.currentCompany?.cbu !== null && authStore.currentCompany?.cbu !== undefined && authStore.currentCompany?.cbu !== '';
});

const currentStatusText = computed(() => {
  const option = statusOptions.find((opt: { value: any; text: string }) => opt.value === status.value);
  return option ? option.text : 'all';
});

const handleProductSelect = (product: Product) => {
  selectedProduct.value = product;
  setProductId(product.id);
};

const clearProductFilter = () => {
  selectedProduct.value = null;
  setProductId(null);
};

const handleClearFilters = () => {
  selectedProduct.value = null;
  clearFilters();
};

const createPool = () => {
  router.push('/pools/create');
};

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
  if (productId.value) {
    try {
      selectedProduct.value = await productService.getById(productId.value);
    } catch {
      // silently ignore — filter still applies, just no label shown
    }
  }
});
</script>

<style scoped>
.my-pools-page {
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

.search-btn,
.create-btn {
  background: linear-gradient(135deg, #7f00ff 0%, #a855f7 100%) !important;
  color: white !important;
  transition: all 0.3s ease;
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

.product-filter-btn {
  max-width: 220px;
  overflow: hidden;
}

@media (max-width: 960px) {
  .my-pools-page {
    padding-top: 1rem;
  }
}
</style>
