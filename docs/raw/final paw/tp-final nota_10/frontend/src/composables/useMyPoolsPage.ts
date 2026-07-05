import { ref, computed, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useBaseSearch, type BaseFilters } from '@/composables/useBaseSearch';
import { poolService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import { useLocationsStore } from '@/stores/locations';
import { useProductsStore } from '@/stores/products';
import { ProductService } from '@/services/ProductService';
import { useAuthStore } from '@/stores/auth';
import type { EnrichedPool, Pool } from '@/models';
import { PoolStatus } from '@/models';

const initialFilters: BaseFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'requested_count',
  desc: true,
  status: null,
};

interface ProductOption {
  value: number;
  text: string;
}

const sortOptions = [
  {
    value: 'product_name-asc',
    orderBy: 'product_name',
    desc: false,
    text: 'sort_name_az',
    icon: 'mdi-sort-alphabetical-ascending',
  },
  {
    value: 'product_name-desc',
    orderBy: 'product_name',
    desc: true,
    text: 'sort_name_za',
    icon: 'mdi-sort-alphabetical-descending',
  },
  {
    value: 'price-asc',
    orderBy: 'price',
    desc: false,
    text: 'sort_price_low_high',
    icon: 'mdi-sort-numeric-ascending',
  },
  {
    value: 'price-desc',
    orderBy: 'price',
    desc: true,
    text: 'sort_price_high_low',
    icon: 'mdi-sort-numeric-descending',
  },
  {
    value: 'product_rating-asc',
    orderBy: 'product_rating',
    desc: false,
    text: 'sort_rating_low_high',
    icon: 'mdi-sort-ascending',
  },
  {
    value: 'product_rating-desc',
    orderBy: 'product_rating',
    desc: true,
    text: 'sort_rating_high_low',
    icon: 'mdi-sort-descending',
  },
  {
    value: 'requested_count-asc',
    orderBy: 'requested_count',
    desc: false,
    text: 'sort_requests_low_high',
    icon: 'mdi-sort-ascending',
  },
  {
    value: 'requested_count-desc',
    orderBy: 'requested_count',
    desc: true,
    text: 'sort_requests_high_low',
    icon: 'mdi-sort-descending',
  },
];

const statusOptions = [
  { value: null, text: 'all' },
  { value: PoolStatus.AVAILABLE, text: 'pool_status.AVAILABLE' },
  { value: PoolStatus.DELIVERING, text: 'pool_status.DELIVERING' },
  { value: PoolStatus.PAUSED, text: 'pool_status.PAUSED' },
  { value: PoolStatus.CANCELLED, text: 'pool_status.CANCELLED' },
  { value: PoolStatus.FINISHED, text: 'pool_status.FINISHED' },
];

export function useMyPoolsPage() {
  const { showError } = useNotifications();
  const authStore = useAuthStore();

  const companiesStore = useCompaniesStore();
  const categoriesStore = useCategoriesStore();
  const locationsStore = useLocationsStore();
  const productsStore = useProductsStore();

  const productsService = new ProductService();

  const rawPools = ref<Pool[]>([]);
  const poolsEtag = ref<string | null>(null);

  const enrichedPools = ref<EnrichedPool[]>([]);

  const baseSearch = ref<any>(null);
  const isLoading = ref(true);
  const status = ref<PoolStatus | null>(null);
  const productId = ref<number | null>(null);
  const productOptions = ref<ProductOption[]>([{ value: 0, text: 'all' }]);
  const isLoadingProducts = ref(false);

  watch(
    rawPools,
    async (newPools) => {
      if (newPools.length === 0) {
        enrichedPools.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const resolvedData: EnrichedPool[] = await Promise.all(
          newPools.map(async (pool: Pool) => {
            const product = await productsStore.fetch(pool.productUri);
            const location = await locationsStore.fetch(pool.locationUri);
            const company = product ? await companiesStore.fetch(product.companyUri) : null;
            const category = product ? await categoriesStore.fetch(product.categoryUri) : null;

            if (!product || !location || !company || !category) {
              throw new Error('Failed to fetch required data for EnrichedPool');
            }

            return {
              pool,
              product,
              location,
              company,
              category,
            };
          }),
        );

        enrichedPools.value = resolvedData;
      } catch (e) {
        console.error('Error enriching pools', e);
      } finally {
        isLoading.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadPools = async () => {
    isLoading.value = true;
    try {
      const companyId = authStore.getCompanyId;
      if (!companyId) {
        showError('errors.company_not_found');
        isLoading.value = false;
        return;
      }

      const params = {
        page: baseSearch.value.pagination.currentPage - 1,
        order_by: baseSearch.value.filters.orderBy,
        desc: baseSearch.value.filters.desc,
        search: baseSearch.value.filters.search || undefined,
        company_id: companyId,
        status: status.value || undefined,
        product_id: productId.value || undefined,
      };

      const response = await poolService.list(params, poolsEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      rawPools.value = response.data;
      poolsEtag.value = response.etag;

      const pageSize = 12;
      baseSearch.value.pagination.totalItems = response.totalCount;
      baseSearch.value.pagination.totalPages = Math.ceil(response.totalCount / pageSize) || 1;
      baseSearch.value.pagination.links = response.links;
    } catch (error) {
      showError('errors.load_pools');
      console.error(error);
      isLoading.value = false;
    }
  };

  baseSearch.value = useBaseSearch(initialFilters, loadPools, {
    customURLBuilder: (filters, initialFilters, query) => {
      if (status.value !== null) query.status = status.value.toString();
      if (productId.value !== null) query.productId = productId.value.toString();
    },
    customURLParser: (filters, query) => {
      status.value = query.status ? (query.status as PoolStatus) : null;
      productId.value = query.productId ? parseInt(query.productId as string) : null;
    },
  });

  const resetCacheAndApply = () => {
    poolsEtag.value = null;
    baseSearch.value.applyFilters();
  };

  const loadProducts = async () => {
    isLoadingProducts.value = true;
    try {
      const companyId = authStore.getCompanyId;
      if (!companyId) {
        showError('errors.company_not_found');
        isLoadingProducts.value = false;
        return;
      }

      const companyProducts = await productsService.list({
        company_id: companyId,
      });

      productOptions.value = [{ value: 0, text: 'all' }, ...companyProducts.data.map((p) => ({ value: p.id, text: p.name }))];
    } catch (error) {
      showError('errors.load_products');
      console.error(error);
    } finally {
      isLoadingProducts.value = false;
    }
  };

  const setProductId = (newProductId: number | null) => {
    productId.value = newProductId === 0 ? null : newProductId;
    resetCacheAndApply();
  };

  const setStatus = (newStatus: PoolStatus | null) => {
    status.value = newStatus;
    resetCacheAndApply();
  };

  const setSortOrder = (orderBy: string, desc: boolean) => {
    baseSearch.value.filters.orderBy = orderBy;
    baseSearch.value.filters.desc = desc;
    resetCacheAndApply();
  };

  const handleSearch = () => {
    poolsEtag.value = null;
    baseSearch.value.handleSearch();
  };

  const currentSort = computed(() => {
    return `${baseSearch.value.filters.orderBy}-${baseSearch.value.filters.desc ? 'desc' : 'asc'}`;
  });

  const currentSortText = computed(() => {
    const option = sortOptions.find((opt) => opt.value === currentSort.value);
    return option ? option.text : 'sort_requests_high_low';
  });

  const initialize = async () => {
    baseSearch.value.initializeFromURL();

    isLoading.value = true;
    await Promise.all([companiesStore.fetchAll(), categoriesStore.fetchAll(), locationsStore.fetchAll(), loadProducts()]);
    await loadPools();
  };

  const hasActiveFilters = computed(() => {
    return baseSearch.value.hasActiveFilters || status.value !== null || productId.value !== null;
  });

  const clearFilters = () => {
    poolsEtag.value = null;
    status.value = null;
    productId.value = null;
    baseSearch.value.clearFilters();
  };

  const handleLinkNavigation = (url: string) => {
    const urlObj = new URL(url);
    const pageParam = urlObj.searchParams.get('page');
    if (pageParam !== null) {
      const page = parseInt(pageParam, 10);
      if (!isNaN(page)) {
        baseSearch.value.handlePageChange(page + 1);
      }
    }
  };

  const currentPage = computed(() => baseSearch.value?.pagination.currentPage || 1);
  const totalPages = computed(() => baseSearch.value?.pagination.totalPages || 1);
  const totalItems = computed(() => baseSearch.value?.pagination.totalItems || 0);
  const links = computed(() => baseSearch.value?.pagination.links || undefined);

  return {
    enrichedPools,

    sortOptions,
    statusOptions,
    productOptions,

    filters: computed(() => baseSearch.value?.filters || initialFilters),
    pagination: computed(
      () =>
        baseSearch.value?.pagination || {
          currentPage: 1,
          totalPages: 1,
          totalItems: 0,
        },
    ),
    isSearching: computed(() => baseSearch.value?.isSearching || false),

    currentPage,
    totalPages,
    totalItems,
    links,

    hasActiveFilters,
    clearFilters,
    handleSearch,
    handlePageChange: (page: number) => baseSearch.value.handlePageChange(page),
    handleLinkNavigation,

    isLoading,
    isLoadingProducts,

    currentSort,
    currentSortText,

    status,
    productId,
    setStatus,
    setProductId,
    setSortOrder,
    initialize,
    loadPools,

    applyFilters: resetCacheAndApply,
  };
}
