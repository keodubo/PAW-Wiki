<template>
  <v-dialog v-model="dialog" max-width="860px" scrollable>
    <template #activator="{ props }">
      <slot name="activator" :props="props" />
    </template>

    <v-card class="selector-dialog">
      <!-- Header -->
      <v-card-title class="dialog-header pa-4 d-flex align-center">
        <v-icon start size="22">mdi-package-variant</v-icon>
        {{ $t('select_product') }}
        <v-spacer />
        <v-btn icon variant="text" size="small" @click="dialog = false" color="white">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>

      <!-- Search Bar -->
      <div class="pa-4 pb-2">
        <v-text-field
          v-model="searchQuery"
          :placeholder="$t('product_search_placeholder')"
          variant="solo"
          density="compact"
          prepend-inner-icon="mdi-magnify"
          color="primary"
          base-color="grey-lighten-1"
          class="search-input elevation-2 rounded-xl"
          hide-details
          clearable
          flat
          @update:model-value="debouncedSearch"
          @click:clear="onClearSearch"
        />
      </div>

      <!-- Count info -->
      <div v-if="!isLoading && totalCount > 0" class="px-4 pb-1">
        <span class="text-body-2 text-medium-emphasis">
          {{ $t('total') }}: {{ totalCount }} {{ $t('products') }}
        </span>
      </div>

      <v-divider />

      <v-card-text class="pa-4" style="min-height: 360px">
        <!-- Loading -->
        <div v-if="isLoading" class="d-flex justify-center align-center py-12">
          <v-progress-circular indeterminate color="primary" size="48" />
        </div>

        <!-- Empty -->
        <div v-else-if="products.length === 0" class="text-center py-12">
          <v-icon size="56" color="grey-lighten-1">mdi-package-variant-closed</v-icon>
          <p class="text-body-1 text-medium-emphasis mt-3">{{ $t('no_products_found') }}</p>
        </div>

        <!-- Grid -->
        <v-row v-else>
          <v-col
            v-for="product in products"
            :key="product.id"
            cols="12"
            sm="6"
          >
            <v-card
              class="product-item"
              :class="{ 'product-item--selected': selectedProductId === product.id }"
              variant="outlined"
              hover
              @click="selectProduct(product)"
            >
              <v-card-text class="pa-3 d-flex align-center ga-3">
                <v-avatar size="52" rounded="lg">
                  <v-img :src="product.imageUri" :alt="product.name">
                    <template #placeholder>
                      <v-img src="@/assets/empty.svg" :alt="product.name" />
                    </template>
                  </v-img>
                </v-avatar>
                <div class="flex-grow-1 min-width-0">
                  <div class="d-flex align-center ga-1 flex-wrap">
                    <span class="text-body-2 font-weight-semibold text-truncate">{{ product.name }}</span>
                    <v-chip v-if="!product.active" color="error" variant="flat" size="x-small" class="font-weight-bold text-uppercase flex-shrink-0">
                      <v-icon start size="10">mdi-archive-off</v-icon>
                      {{ $t('product_retired') }}
                    </v-chip>
                  </div>
                  <div class="text-body-2 text-primary font-weight-medium mt-1">
                    {{ formatCurrency(product.price) }}
                  </div>
                </div>
                <v-icon v-if="selectedProductId === product.id" color="primary" size="20">
                  mdi-check-circle
                </v-icon>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>

      <v-divider v-if="links && hasAnyLink" />

      <!-- Pagination -->
      <div v-if="links && hasAnyLink" class="d-flex justify-center pa-3">
        <div class="d-flex align-center ga-2">
          <v-btn
            :disabled="!links.prev"
            @click="navigateToLink(links.first)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.first')"
          >
            <v-icon>mdi-page-first</v-icon>
          </v-btn>

          <v-btn
            :disabled="!links.prev"
            @click="navigateToLink(links.prev)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.previous')"
          >
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>

          <span class="text-body-2 font-weight-medium px-2">
            {{ $t('pagination.current_page', { current: currentPage, total: totalPages }) }}
          </span>

          <v-btn
            :disabled="!links.next"
            @click="navigateToLink(links.next)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.next')"
          >
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>

          <v-btn
            :disabled="!links.next"
            @click="navigateToLink(links.last)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.last')"
          >
            <v-icon>mdi-page-last</v-icon>
          </v-btn>
        </div>
      </div>

      <!-- Cancel action -->
      <v-card-actions class="pa-3 pt-0">
        <v-spacer />
        <v-btn variant="text" color="grey" @click="dialog = false" prepend-icon="mdi-close">
          {{ $t('cancel') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { productService } from '@/services';
import type { Product } from '@/models';
import type { PaginationLinks } from '@/models/Http';
import { formatCurrency } from '@/utils/currency';
import { useNotifications } from '@/composables/useNotifications';

export interface ProductSelectorDialogProps {
  companyId?: number;
  activeOnly?: boolean;
}

const props = withDefaults(defineProps<ProductSelectorDialogProps>(), {
  activeOnly: true,
});

const emit = defineEmits<{
  select: [product: Product];
}>();

const { handleApiError } = useNotifications();

const dialog = ref(false);
const isLoading = ref(false);
const searchQuery = ref('');
const selectedProductId = ref<number | null>(null);
const products = ref<Product[]>([]);
const links = ref<PaginationLinks>({});
const currentPage = ref(1);
const totalPages = ref(1);
const totalCount = ref(0);

let searchTimeout: ReturnType<typeof setTimeout> | null = null;

const hasAnyLink = computed(() => !!(links.value.first || links.value.last || links.value.next || links.value.prev));

const debouncedSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
    loadProducts();
  }, 300);
};

const onClearSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  loadProducts();
};

const loadProducts = async (page = currentPage.value) => {
  try {
    isLoading.value = true;

    const params: any = { page: page - 1 };
    if (props.companyId) params.company_id = props.companyId;
    if (props.activeOnly) params.active = true;
    if (searchQuery.value) params.search = searchQuery.value;

    const response = await productService.list(params);

    products.value = response.data;
    totalCount.value = response.totalCount;
    links.value = response.links ?? {};

    // Derive current/total pages from link URLs
    if (response.links?.last) {
      const url = new URL(response.links.last);
      const lastPage = parseInt(url.searchParams.get('page') ?? '0', 10) + 1;
      totalPages.value = lastPage;
    } else {
      totalPages.value = page;
    }
    currentPage.value = page;
  } catch (error) {
    handleApiError(error);
    products.value = [];
  } finally {
    isLoading.value = false;
  }
};

const navigateToLink = (url?: string) => {
  if (!url) return;
  const urlObj = new URL(url);
  const pageParam = urlObj.searchParams.get('page');
  if (pageParam !== null) {
    const page = parseInt(pageParam, 10) + 1;
    loadProducts(page);
  }
};

const selectProduct = (product: Product) => {
  selectedProductId.value = product.id;
  emit('select', product);
  dialog.value = false;
};

watch(dialog, (open) => {
  if (open) {
    currentPage.value = 1;
    searchQuery.value = '';
    selectedProductId.value = null;
    loadProducts();
  }
});
</script>

<style scoped>
.selector-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.dialog-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  color: white;
}

.search-input :deep(.v-field) {
  background: rgb(var(--v-theme-surface)) !important;
  border: 1.5px solid rgba(var(--v-theme-primary), 0.2) !important;
  border-radius: 12px !important;
}

.search-input :deep(.v-field--focused) {
  border: 1.5px solid rgb(var(--v-theme-primary)) !important;
}

.search-input :deep(.v-field__outline) {
  display: none !important;
}

.search-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary)) !important;
}

.product-item {
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 10px !important;
}

.product-item:hover {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.04) !important;
}

.product-item--selected {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.06) !important;
}

.pagination-btn {
  min-width: 36px !important;
}

.pagination-btn:disabled {
  opacity: 0.4;
}

.min-width-0 {
  min-width: 0;
}
</style>
