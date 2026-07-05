import { ref, computed, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useBaseSearch } from '@/composables/useBaseSearch';
import { poolService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import { useLocationsStore } from '@/stores/locations';
import { useProductsStore } from '@/stores/products';
import { PoolStatus, type EnrichedPool, type Pool } from '@/models';

interface PoolFilters {
  search: string;
  companyId: number | null;
  categoryId: number | null;
  locationId: number | null;
  productId: number | null;
  priceMin: number | null;
  priceMax: number | null;
  orderBy: string;
  desc: boolean;
  status: number | null;
}

const initialFilters: PoolFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  locationId: null,
  productId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'requested_count',
  desc: true,
  status: null,
};

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

export function usePoolsPage() {
  const { showError } = useNotifications();

  const companiesStore = useCompaniesStore();
  const categoriesStore = useCategoriesStore();
  const locationsStore = useLocationsStore();
  const productsStore = useProductsStore();

  const rawPools = ref<Pool[]>([]);
  const poolsEtag = ref<string | null>(null);

  const enrichedPools = ref<EnrichedPool[]>([]);

  const baseSearch = ref<any>(null);
  const isLoading = ref(true);

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
      const params = {
        page: baseSearch.value.pagination.currentPage - 1,
        order_by: baseSearch.value.filters.orderBy,
        desc: baseSearch.value.filters.desc,
        search: baseSearch.value.filters.search || undefined,
        company_id: baseSearch.value.filters.companyId || undefined,
        category_id: baseSearch.value.filters.categoryId || undefined,
        location_id: baseSearch.value.filters.locationId || undefined,
        product_id: baseSearch.value.filters.productId || undefined,
        price_min: baseSearch.value.filters.priceMin || undefined,
        price_max: baseSearch.value.filters.priceMax || undefined,
        status: PoolStatus.AVAILABLE,
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
      if (filters.locationId) query.locationId = filters.locationId.toString();
      if (filters.categoryId) query.categoryId = filters.categoryId.toString();
      if (filters.productId) query.productId = filters.productId.toString();
    },
    customURLParser: (filters, query) => {
      filters.locationId = query.locationId ? parseInt(query.locationId as string) : null;
      filters.categoryId = query.categoryId ? parseInt(query.categoryId as string) : null;
      filters.productId = query.productId ? parseInt(query.productId as string) : null;
    },
  });

  const resetCacheAndApply = () => {
    poolsEtag.value = null;
    baseSearch.value.applyFilters();
  };

  const toggleCategory = (categoryId: number) => {
    baseSearch.value.filters.categoryId = baseSearch.value.filters.categoryId === categoryId ? null : categoryId;
    resetCacheAndApply();
  };

  const toggleLocation = (locationId: number) => {
    baseSearch.value.filters.locationId = baseSearch.value.filters.locationId === locationId ? null : locationId;
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
    return option ? option.text : 'sort_requests_made_high_low';
  });

  const initialize = async () => {
    baseSearch.value.initializeFromURL();

    isLoading.value = true;
    await Promise.all([companiesStore.fetchAll(), categoriesStore.fetchAll(), locationsStore.fetchAll()]);
    await loadPools();
  };

  const hasActiveFilters = computed(() => {
    const filters = baseSearch.value.filters;
    const initial = initialFilters;
    return (
      filters.search !== initial.search ||
      filters.companyId !== initial.companyId ||
      filters.categoryId !== initial.categoryId ||
      filters.locationId !== initial.locationId ||
      filters.priceMin !== initial.priceMin ||
      filters.priceMax !== initial.priceMax ||
      filters.orderBy !== initial.orderBy ||
      filters.desc !== initial.desc
    );
  });

  return {
    enrichedPools,

    companies: computed(() => companiesStore.items),
    categories: computed(() => categoriesStore.items),
    locations: computed(() => locationsStore.items),

    sortOptions,

    ...baseSearch.value,
    pagination: baseSearch.value.pagination,
    hasActiveFilters,
    handleSearch,

    isLoading,

    currentSort,
    currentSortText,

    toggleCategory,
    toggleLocation,
    setSortOrder,
    initialize,

    applyFilters: resetCacheAndApply,
  };
}
