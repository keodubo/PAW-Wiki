import { ref, computed, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useBaseSearch, type BaseFilters } from '@/composables/useBaseSearch';
import { productService } from '@/services';
import { useAuthStore } from '@/stores/auth';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import type { Product } from '@/models';

interface MyProductFilters extends BaseFilters {
  search: string;
  companyId: null;
  categoryId: null;
  priceMin: null;
  priceMax: null;
  orderBy: string;
  desc: boolean;
  status: null;
  active: boolean | null;
}

const initialFilters: MyProductFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'price',
  desc: false,
  status: null,
  active: true,
};

const sortOptions = [
  { value: 'name-asc', orderBy: 'name', desc: false, text: 'sort_name_az', icon: 'mdi-sort-alphabetical-ascending' },
  { value: 'name-desc', orderBy: 'name', desc: true, text: 'sort_name_za', icon: 'mdi-sort-alphabetical-descending' },
  { value: 'price-asc', orderBy: 'price', desc: false, text: 'sort_price_low_high', icon: 'mdi-sort-numeric-ascending' },
  { value: 'price-desc', orderBy: 'price', desc: true, text: 'sort_price_high_low', icon: 'mdi-sort-numeric-descending' },
  { value: 'rating-asc', orderBy: 'rating', desc: false, text: 'sort_rating_low_high', icon: 'mdi-sort-ascending' },
  { value: 'rating-desc', orderBy: 'rating', desc: true, text: 'sort_rating_high_low', icon: 'mdi-sort-descending' },
];

export function useMyProductsPage() {
  const { showError } = useNotifications();

  const companiesStore = useCompaniesStore();
  const categoriesStore = useCategoriesStore();

  const authStore = useAuthStore();

  const rawProducts = ref<Product[]>([]);
  const productsEtag = ref<string | null>(null);
  const data = ref<any[]>([]);

  const baseSearch = ref<any>(null);
  const isLoading = ref(true);

  watch(
    rawProducts,
    async (newProducts) => {
      if (newProducts.length === 0) {
        data.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const resolvedData = await Promise.all(
          newProducts.map(async (product: Product) => {
            const company = await companiesStore.fetch(product.companyUri);
            const category = await categoriesStore.fetch(product.categoryUri);

            return {
              product,
              company,
              category,
            };
          }),
        );

        data.value = resolvedData;
      } catch (e) {
        console.error('Error enriching products', e);
      } finally {
        isLoading.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadProducts = async () => {
    isLoading.value = true;
    try {
      const companyId = authStore.getCompanyId;
      if (!companyId) {
        rawProducts.value = [];
        isLoading.value = false;
        return;
      }
      const params: Record<string, any> = {
        page: baseSearch.value.pagination.currentPage - 1,
        order_by: baseSearch.value.filters.orderBy,
        desc: baseSearch.value.filters.desc,
        search: baseSearch.value.filters.search || undefined,
        company_id: companyId,
      };

      if (baseSearch.value.filters.active !== null) {
        params.active = baseSearch.value.filters.active;
      }

      const response = await productService.list(params, productsEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      rawProducts.value = response.data;
      productsEtag.value = response.etag;

      const pageSize = 12;
      baseSearch.value.pagination.totalItems = response.totalCount;
      baseSearch.value.pagination.totalPages = Math.ceil(response.totalCount / pageSize) || 1;
      baseSearch.value.pagination.links = response.links;
    } catch (error) {
      showError('errors.load_your_products');
      console.error(error);
      isLoading.value = false;
    }
  };

  baseSearch.value = useBaseSearch(initialFilters, loadProducts, {
    customURLBuilder: (filters, initial, query) => {
      if (filters.active !== initial.active) {
        if (filters.active === null) {
          query.active = 'all';
        } else {
          query.active = filters.active.toString();
        }
      }
    },
    customURLParser: (filters, query) => {
      if (query.active) {
        if (query.active === 'all') {
          filters.active = null;
        } else {
          filters.active = query.active === 'true';
        }
      } else {
        filters.active = initialFilters.active;
      }
    },
  });

  const resetCacheAndApply = () => {
    productsEtag.value = null;
    baseSearch.value.applyFilters();
  };

  const setSortOrder = (orderBy: string, desc: boolean) => {
    baseSearch.value.filters.orderBy = orderBy;
    baseSearch.value.filters.desc = desc;
    resetCacheAndApply();
  };

  const handleSearch = () => {
    productsEtag.value = null;
    baseSearch.value.handleSearch();
  };

  const currentSort = computed(() => {
    return `${baseSearch.value.filters.orderBy}-${baseSearch.value.filters.desc ? 'desc' : 'asc'}`;
  });

  const currentSortText = computed(() => {
    const option = sortOptions.find((opt) => opt.value === currentSort.value);
    return option ? option.text : 'sort_price_low_high';
  });

  const initialize = async () => {
    baseSearch.value.initializeFromURL();

    isLoading.value = true;
    await Promise.all([companiesStore.fetchAll(), categoriesStore.fetchAll()]);
    await loadProducts();
  };

  const hasActiveFilters = computed(() => {
    return (
      baseSearch.value.filters.search !== '' ||
      baseSearch.value.filters.orderBy !== initialFilters.orderBy ||
      baseSearch.value.filters.desc !== initialFilters.desc ||
      baseSearch.value.filters.active !== initialFilters.active
    );
  });

  const setActiveFilter = (value: boolean | null) => {
    baseSearch.value.filters.active = value;
    resetCacheAndApply();
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
    products: data,

    sortOptions,

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
    handleSearch,
    handlePageChange: (page: number) => baseSearch.value.handlePageChange(page),
    handleLinkNavigation,
    clearFilters: () => baseSearch.value.clearFilters(),

    isLoading,

    currentSort,
    currentSortText,

    setSortOrder,
    setActiveFilter,
    initialize,

    applyFilters: resetCacheAndApply,
    loadProducts,
  };
}
