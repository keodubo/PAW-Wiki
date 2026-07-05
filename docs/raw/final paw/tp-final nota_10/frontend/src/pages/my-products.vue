<template>
  <div class="my-products-page">
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
                    :placeholder="$t('my_products_search_placeholder')"
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

                <v-btn v-if="hasActiveFilters" color="error" variant="elevated" size="x-large" class="clear-btn elevation-6 rounded-xl" @click="clearFilters" :title="$t('clear_filters')" icon>
                  <v-icon size="large">mdi-filter-remove</v-icon>
                </v-btn>
              </div>
            </v-col>

            <v-col cols="12" md="3" class="text-center text-md-right">
              <v-btn color="primary" size="large" class="create-btn elevation-6 rounded-xl" to="/products/create" prepend-icon="mdi-plus-circle"> {{ $t('create_product') }} </v-btn>
            </v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" md="10" lg="8">
          <div class="d-flex justify-end mb-4 ga-3">
            <v-menu>
              <template #activator="{ props }">
                <v-btn v-bind="props" variant="outlined" color="primary" class="text-none" prepend-icon="mdi-filter-variant">
                  {{ $t(activeFilterText) }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </v-btn>
              </template>
              <v-list>
                <v-list-item
                  v-for="option in activeFilterOptions"
                  :key="option.value"
                  :class="{
                    'bg-primary-lighten-4': currentActiveFilter === option.value,
                  }"
                  @click="setActiveFilter(option.filterValue)"
                >
                  <template #prepend>
                    <v-icon :icon="option.icon" size="small" class="mr-2" />
                  </template>
                  <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>

            <v-menu>
              <template #activator="{ props }">
                <v-btn v-bind="props" variant="outlined" color="primary" class="text-none" prepend-icon="mdi-sort">
                  {{ $t('sort') }}: {{ $t(currentSortText) }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </v-btn>
              </template>
              <v-list>
                <v-list-item
                  v-for="option in sortOptions"
                  :key="option.value"
                  :class="{
                    'bg-primary-lighten-4': currentSort === option.value,
                  }"
                  @click="setSortOrder(option.orderBy, option.desc)"
                >
                  <template #prepend>
                    <v-icon :icon="option.icon" size="small" class="mr-2" />
                  </template>
                  <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>

          <div v-if="products.length === 0 && !isLoading" class="text-center py-8">
            <v-img src="@/assets/empty.svg" alt="No products found" max-width="250" class="mx-auto mb-4" />
            <h3 class="text-h6 text-medium-emphasis mb-4">
              {{ hasActiveFilters ? $t('no_products_found') : $t('no_products_created_yet') }}
            </h3>
            <v-btn v-if="hasActiveFilters" color="error" variant="outlined" @click="clearFilters" prepend-icon="mdi-filter-remove" class="mb-2"> {{ $t('clear_filters') }} </v-btn>
            <v-btn v-else color="primary" variant="elevated" to="/products/create" prepend-icon="mdi-plus-circle"> {{ $t('create_your_first_product') }} </v-btn>
          </div>

          <v-row v-else>
            <template v-if="isLoading">
              <v-col v-for="n in 6" :key="`skeleton-${n}`" cols="12" sm="6">
                <v-skeleton-loader type="card" />
              </v-col>
            </template>

            <template v-else>
              <v-col v-for="data in products" :key="data.product.id" cols="12" sm="6">
                <ProductCard :product="data.product" :company="data.company" :category="data.category" :my-product="true" @delete="handleDeleteProduct" />
              </v-col>
            </template>
          </v-row>

          <PaginationLinks v-if="links" :links="links" :current-page="currentPage" :total-pages="totalPages" @navigate="handleLinkNavigation" class="mt-6" />
        </v-col>
      </v-row>
    </v-container>

    <v-dialog v-model="deleteDialog" max-width="560">
      <v-card class="delete-dialog-card elevation-12 rounded-xl">
        <div class="delete-dialog__header pa-6">
          <div class="delete-dialog__icon elevation-2">
            <v-icon color="white" size="36">mdi-archive-arrow-down</v-icon>
          </div>
          <div class="delete-dialog__header-text">
            <div v-if="productBeingDeleted" class="header-overline">{{ $t('product') }} #{{ productBeingDeleted.product.id }}</div>
            <h2 class="text-h5 font-weight-bold text-white mb-1">{{ $t('delete_product') }}</h2>
            <p v-if="deleteProductName" class="text-body-2 text-white header-subtitle">
              {{ deleteProductName }}
            </p>
          </div>
        </div>

        <v-card-text class="pa-6">
          <div v-if="productBeingDeleted" class="delete-dialog__body mb-5">
            <div class="product-preview-wrapper">
              <v-img v-if="deleteProductImage" :src="deleteProductImage" :alt="deleteProductName" height="120" width="120" cover class="rounded-lg product-preview-image elevation-2" />
              <div v-else class="product-preview-placeholder rounded-lg elevation-1">
                <v-icon size="40" color="primary">mdi-image-off</v-icon>
              </div>
            </div>

            <div class="delete-dialog__meta flex-grow-1">
              <div class="meta-grid">
                <div v-for="meta in deletionMeta" :key="meta.key" class="meta-entry">
                  <span class="meta-label">{{ $t(meta.label) }}</span>
                  <span class="meta-value">{{ $t(meta.value) }}</span>
                </div>
              </div>

              <p v-if="deleteProductDescription" class="text-body-2 text-medium-emphasis mt-4">
                {{ deleteProductDescription }}
              </p>
            </div>
          </div>

          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert-circle" class="delete-warning">
            {{ $t('delete_product_confirmation') }}
          </v-alert>
        </v-card-text>

        <v-card-actions class="px-6 pb-6 pt-0 d-flex justify-end ga-3">
          <v-btn color="secondary" variant="text" @click="deleteDialog = false" :disabled="isDeleting">
            <v-icon start>mdi-close</v-icon>
            {{ $t('cancel') }}
          </v-btn>
          <v-btn color="warning" variant="flat" :loading="isDeleting" class="delete-dialog__action" @click="confirmDelete">
            <v-icon start>mdi-archive-arrow-down</v-icon>
            {{ $t('delete') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import ProductCard from '@/components/ProductCard.vue';
import PaginationLinks from '@/components/PaginationLinks.vue';
import { useMyProductsPage } from '@/composables/useMyProductsPage';
import { productService } from '@/services';
import { useNotifications } from '@/composables/useNotifications';
import { UserRole } from '@/models/UserRole';
import type { Product, Company, Category } from '@/models';
import { formatCurrency } from '@/utils/currency';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.COMPANY_PENDING, UserRole.COMPANY_UNVERIFIED, UserRole.COMPANY_VERIFIED],
  },
});

const myProductsPage = useMyProductsPage();
const { showSuccess, showError } = useNotifications();
const { locale } = useI18n();

const searchForm = ref();

const {
  products,
  sortOptions,

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

  handleSearch,
  applyFilters,
  clearFilters,
  handlePageChange,
  handleLinkNavigation,
  setSortOrder,
  setActiveFilter,
  initialize,
  loadProducts,
} = myProductsPage;

const activeFilterOptions = [
  { value: 'active', filterValue: true, text: 'product_filter_active', icon: 'mdi-check-circle' },
  { value: 'deleted', filterValue: false, text: 'product_filter_deleted', icon: 'mdi-archive-arrow-down' },
];

const currentActiveFilter = computed(() => {
  const activeValue = filters.value.active;
  if (activeValue === false) return 'deleted';
  return 'active'; // Default to 'active' for true or null
});

const activeFilterText = computed(() => {
  const option = activeFilterOptions.find((opt) => opt.value === currentActiveFilter.value);
  return option ? option.text : 'product_filter_active';
});

const deleteDialog = ref(false);
const isDeleting = ref(false);
const productToDeleteId = ref<number | null>(null);

type ProductListItem = {
  product: Product;
  company?: Company;
  category?: Category;
};

const truncateText = (text: string, maxLength: number): string => {
  if (!text) return '';
  if (text.length <= maxLength) return text;
  return `${text.substring(0, maxLength)}...`;
};

const productBeingDeleted = computed<ProductListItem | null>(() => {
  if (productToDeleteId.value === null) {
    return null;
  }

  const list = products.value as ProductListItem[];
  return list.find((item) => item.product.id === productToDeleteId.value) ?? null;
});

const deletionMeta = computed(() => {
  const current = productBeingDeleted.value;
  if (!current) {
    return [] as Array<{ key: string; label: string; value: string }>;
  }

  const entries: Array<{ key: string; label: string; value: string }> = [];

  if (typeof current.product.price === 'number') {
    entries.push({ key: 'price', label: 'product_price', value: formatCurrency(current.product.price) });
  }

  entries.push({
    key: 'category',
    label: 'product_category',
    value: current.category?.name ?? 'product_detail.uncategorized',
  });

  if (current.company?.name) {
    entries.push({ key: 'company', label: 'admin.company_name', value: current.company.name });
  }

  return entries;
});

const deleteProductName = computed(() => productBeingDeleted.value?.product.name ?? '');
const deleteProductImage = computed(() => productBeingDeleted.value?.product.imageUri ?? '');
const deleteProductDescription = computed(() => (productBeingDeleted.value?.product.description ? truncateText(productBeingDeleted.value.product.description, 180) : ''));

const handleDeleteProduct = (productId: number) => {
  productToDeleteId.value = productId;
  deleteDialog.value = true;
};

const confirmDelete = async () => {
  if (productToDeleteId.value === null) return;

  isDeleting.value = true;
  try {
    await productService.delete(productToDeleteId.value);
    showSuccess('product_deleted_success');
    deleteDialog.value = false;
    productToDeleteId.value = null;

    await loadProducts();
  } catch (error) {
    showError('product_delete_failed');
    console.error(error);
  } finally {
    isDeleting.value = false;
  }
};

watch(deleteDialog, (isOpen) => {
  if (!isOpen) {
    productToDeleteId.value = null;
  }
});

onMounted(() => {
  initialize();
});
</script>

<style scoped>
.my-products-page {
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

.search-btn,
.create-btn {
  background: linear-gradient(135deg, #7f00ff 0%, #a855f7 100%) !important;
  color: white !important;
  transition: all 0.3s ease;
}

.search-btn {
  width: 56px;
  height: 56px;
}

.search-btn:hover,
.create-btn:hover {
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

.bg-primary-lighten-4 {
  background-color: rgba(var(--v-theme-primary), 0.15) !important;
}

.delete-dialog-card {
  overflow: hidden;
  background: rgb(var(--v-theme-surface));
}

.v-theme--dark .delete-dialog-card {
  background: #1e1e1e;
}

.delete-dialog__header {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  background: linear-gradient(135deg, #f59e0b 0%, #f97316 100%);
  color: #ffffff;
}

.delete-dialog__icon {
  height: 56px;
  width: 56px;
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.14);
}

.header-overline {
  font-size: 0.75rem;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  opacity: 0.85;
}

.header-subtitle {
  opacity: 0.85;
}

.delete-dialog__body {
  display: flex;
  flex-wrap: wrap;
  gap: 1.5rem;
}

.product-preview-wrapper {
  flex-shrink: 0;
}

.product-preview-image,
.product-preview-placeholder {
  width: 120px;
  height: 120px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(var(--v-theme-surface), 0.8);
  border-radius: 16px;
}

.product-preview-placeholder {
  border: 1px dashed rgba(var(--v-theme-on-surface), 0.2);
}

.v-theme--dark .product-preview-placeholder {
  border-color: rgba(255, 255, 255, 0.2);
}

.delete-dialog__meta {
  min-width: 0;
}

.meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.meta-entry {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.meta-label {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  color: rgba(var(--v-theme-on-surface), 0.55);
}

.meta-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
}

.delete-warning {
  border-radius: 14px;
}

.delete-dialog__action {
  font-weight: 600;
  text-transform: none;
  border-radius: 12px;
  padding: 0 20px;
}

@media (max-width: 960px) {
  .my-products-page {
    padding-top: 1rem;
  }

  .delete-dialog__body {
    flex-direction: column;
    align-items: stretch;
  }

  .product-preview-wrapper {
    display: flex;
    justify-content: center;
  }
}
</style>
