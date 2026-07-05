import { ref, computed, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useBaseSearch, type BaseFilters } from '@/composables/useBaseSearch';
import { requestService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import { useLocationsStore } from '@/stores/locations';
import { useProductsStore } from '@/stores/products';
import { usePoolsStore } from '@/stores/pools';
import { ProductService } from '@/services/ProductService';
import { useAuthStore } from '@/stores/auth';
import type { Request, EnrichedRequest } from '@/models';
import { PoolStatus, RequestStatus } from '@/models';

const initialFilters: BaseFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'request_status',
  desc: false,
  status: null,
};

interface ProductOption {
  value: number;
  text: string;
}

interface CompanyOption {
  value: number;
  text: string;
}

const sortOptions = [
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
    value: 'pool_status-asc',
    orderBy: 'pool_status',
    desc: false,
    text: 'sort_pool_status_asc',
    icon: 'mdi-sort-ascending',
  },
  {
    value: 'pool_status-desc',
    orderBy: 'pool_status',
    desc: true,
    text: 'sort_pool_status_desc',
    icon: 'mdi-sort-descending',
  },
  {
    value: 'request_status-asc',
    orderBy: 'request_status',
    desc: false,
    text: 'sort_request_status_asc',
    icon: 'mdi-sort-ascending',
  },
  {
    value: 'request_status-desc',
    orderBy: 'request_status',
    desc: true,
    text: 'sort_request_status_desc',
    icon: 'mdi-sort-descending',
  },
];

const poolStatusOptions = [
  { value: null, text: 'all' },
  { value: PoolStatus.AVAILABLE, text: 'pool_status.AVAILABLE' },
  { value: PoolStatus.DELIVERING, text: 'pool_status.DELIVERING' },
  { value: PoolStatus.PAUSED, text: 'pool_status.PAUSED' },
  { value: PoolStatus.CANCELLED, text: 'pool_status.CANCELLED' },
  { value: PoolStatus.FINISHED, text: 'pool_status.FINISHED' },
];

const requestStatusOptions = [
  { value: null, text: 'all' },
  { value: RequestStatus.PENDING, text: 'request_status.pending' },
  { value: RequestStatus.ACCEPTED, text: 'request_status.accepted' },
  { value: RequestStatus.REJECTED, text: 'request_status.rejected' },
  { value: RequestStatus.DELIVERED, text: 'request_status.delivered' },
];

export function useMyRequestsPage() {
  const { showError } = useNotifications();
  const authStore = useAuthStore();

  const companiesStore = useCompaniesStore();
  const categoriesStore = useCategoriesStore();
  const locationsStore = useLocationsStore();
  const productsStore = useProductsStore();
  const poolsStore = usePoolsStore();

  const productsService = new ProductService();

  const rawRequests = ref<Request[]>([]);
  const requestsEtag = ref<string | null>(null);

  const enrichedRequests = ref<EnrichedRequest[]>([]);

  const baseSearch = ref<any>(null);
  const isLoading = ref(true);
  const poolStatus = ref<PoolStatus | null>(null);
  const requestStatus = ref<RequestStatus | null>(null);
  const productId = ref<number | null>(null);
  const companyId = ref<number | null>(null);
  const productOptions = ref<ProductOption[]>([{ value: 0, text: 'all' }]);
  const companyOptions = ref<CompanyOption[]>([{ value: 0, text: 'all' }]);
  const isLoadingProducts = ref(false);
  const isLoadingCompanies = ref(false);

  watch(
    rawRequests,
    async (newRequests) => {
      if (newRequests.length === 0) {
        enrichedRequests.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const resolvedData: EnrichedRequest[] = await Promise.all(
          newRequests.map(async (request: Request) => {
            const pool = await poolsStore.fetch(request.poolUri);

            const product = await productsStore.fetch(pool.productUri);
            const location = await locationsStore.fetch(pool.locationUri);
            const company = product ? await companiesStore.fetch(product.companyUri) : null;
            const category = product ? await categoriesStore.fetch(product.categoryUri) : null;

            if (!product || !location || !company || !category) {
              throw new Error('Failed to fetch required data for EnrichedRequest');
            }

            return {
              request,
              pool,
              product,
              location,
              company,
              category,
            };
          }),
        );

        enrichedRequests.value = resolvedData;
      } catch (e) {
        console.error('Error enriching requests', e);
      } finally {
        isLoading.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadRequests = async () => {
    isLoading.value = true;
    try {
      const userId = authStore.getUserId;
      if (!userId) {
        showError('errors.user_not_found');
        isLoading.value = false;
        return;
      }

      const params: any = {
        page: baseSearch.value.pagination.currentPage - 1,
        order_by: baseSearch.value.filters.orderBy,
        desc: baseSearch.value.filters.desc,
        user_id: userId,
      };

      if (baseSearch.value.filters.search) {
        params.search = baseSearch.value.filters.search;
      }
      if (poolStatus.value) {
        params.pool_status = poolStatus.value;
      }
      if (requestStatus.value) {
        params.status = requestStatus.value;
      }
      if (productId.value) {
        params.product_id = productId.value;
      }
      if (companyId.value) {
        params.company_id = companyId.value;
      }

      const response = await requestService.list(params, requestsEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      rawRequests.value = response.data;
      requestsEtag.value = response.etag;

      const pageSize = 12;
      baseSearch.value.pagination.totalItems = response.totalCount;
      baseSearch.value.pagination.totalPages = Math.ceil(response.totalCount / pageSize) || 1;
      baseSearch.value.pagination.links = response.links;
    } catch (error) {
      showError('errors.load_requests');
      console.error(error);
      isLoading.value = false;
    }
  };

  baseSearch.value = useBaseSearch(initialFilters, loadRequests, {
    customURLBuilder: (filters, initialFilters, query) => {
      if (poolStatus.value !== null) query.poolStatus = poolStatus.value.toString();
      if (requestStatus.value !== null) query.requestStatus = requestStatus.value.toString();
      if (productId.value !== null) query.productId = productId.value.toString();
      if (companyId.value !== null) query.companyId = companyId.value.toString();
    },
    customURLParser: (filters, query) => {
      poolStatus.value = query.poolStatus ? (query.poolStatus as PoolStatus) : null;
      requestStatus.value = query.requestStatus ? (query.requestStatus as RequestStatus) : null;
      productId.value = query.productId ? parseInt(query.productId as string) : null;
      companyId.value = query.companyId ? parseInt(query.companyId as string) : null;
    },
  });

  const resetCacheAndApply = () => {
    requestsEtag.value = null;
    baseSearch.value.applyFilters();
  };

  const loadProducts = async () => {
    isLoadingProducts.value = true;
    try {
      const allProducts = await productsService.list({});

      productOptions.value = [{ value: 0, text: 'all' }, ...allProducts.data.map((p) => ({ value: p.id, text: p.name }))];
    } catch (error) {
      showError('errors.load_products');
      console.error(error);
    } finally {
      isLoadingProducts.value = false;
    }
  };

  const loadCompanies = async () => {
    isLoadingCompanies.value = true;
    try {
      ('Loading companies for filter');
      await companiesStore.fetchAll();

      const companies = companiesStore.items;
      companyOptions.value = [{ value: 0, text: 'all' }, ...companies.map((c: any) => ({ value: c.id, text: c.name }))];
    } catch (error) {
      showError('errors.load_companies');
      console.error(error);
    } finally {
      isLoadingCompanies.value = false;
    }
  };

  const setProductId = (newProductId: number | null) => {
    productId.value = newProductId === 0 ? null : newProductId;
    requestsEtag.value = null;
    baseSearch.value.pagination.currentPage = 1;
    baseSearch.value.updateURL();
  };

  const setCompanyId = (newCompanyId: number | null) => {
    companyId.value = newCompanyId === 0 ? null : newCompanyId;
    requestsEtag.value = null;
    baseSearch.value.pagination.currentPage = 1;
    baseSearch.value.updateURL();
  };

  const setPoolStatus = (newStatus: PoolStatus | null) => {
    poolStatus.value = newStatus;
    requestsEtag.value = null;
    baseSearch.value.pagination.currentPage = 1;
    baseSearch.value.updateURL();
  };

  const setRequestStatus = (newStatus: RequestStatus | null) => {
    requestStatus.value = newStatus;
    requestsEtag.value = null;
    baseSearch.value.pagination.currentPage = 1;
    baseSearch.value.updateURL();
  };

  const setSortOrder = (orderBy: string, desc: boolean) => {
    baseSearch.value.filters.orderBy = orderBy;
    baseSearch.value.filters.desc = desc;
    resetCacheAndApply();
  };

  const handleSearch = () => {
    requestsEtag.value = null;
    baseSearch.value.handleSearch();
  };

  const currentSort = computed(() => {
    return `${baseSearch.value.filters.orderBy}-${baseSearch.value.filters.desc ? 'desc' : 'asc'}`;
  });

  const currentSortText = computed(() => {
    const option = sortOptions.find((opt) => opt.value === currentSort.value);
    return option ? option.text : 'sort_request_status_asc';
  });

  const initialize = async () => {
    baseSearch.value.initializeFromURL();

    isLoading.value = true;
    await Promise.all([companiesStore.fetchAll(), categoriesStore.fetchAll(), locationsStore.fetchAll(), loadProducts(), loadCompanies()]);
    await loadRequests();
  };

  const hasActiveFilters = computed(() => {
    return baseSearch.value.hasActiveFilters || poolStatus.value !== null || requestStatus.value !== null || productId.value !== null || companyId.value !== null;
  });

  const clearFilters = () => {
    requestsEtag.value = null;
    poolStatus.value = null;
    requestStatus.value = null;
    productId.value = null;
    companyId.value = null;
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
    enrichedRequests,

    sortOptions,
    poolStatusOptions,
    requestStatusOptions,
    productOptions,
    companyOptions,

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
    isLoadingCompanies,

    currentSort,
    currentSortText,

    poolStatus,
    requestStatus,
    productId,
    companyId,
    setPoolStatus,
    setRequestStatus,
    setProductId,
    setCompanyId,
    setSortOrder,
    initialize,

    applyFilters: resetCacheAndApply,
    loadRequests,
  };
}
