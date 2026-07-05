import { ref, computed, watch } from 'vue';
import { useNotifications } from '@/composables/useNotifications';
import { useBaseSearch, type BaseFilters } from '@/composables/useBaseSearch';
import { productService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import type { Product } from '@/models';

interface ProductFilters extends BaseFilters {
  search: string;
  companyId: number | null;
  categoryId: number | null;
  priceMin: number | null;
  priceMax: number | null;
  orderBy: string;
  desc: boolean;
  status: null;
}

const initialFilters: ProductFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'price',
  desc: false,
  status: null,
};

const sortOptions = [
  { value: 'name-asc', orderBy: 'name', desc: false, text: 'sort_name_az', icon: 'mdi-sort-alphabetical-ascending' },
  { value: 'name-desc', orderBy: 'name', desc: true, text: 'sort_name_za', icon: 'mdi-sort-alphabetical-descending' },
  { value: 'price-asc', orderBy: 'price', desc: false, text: 'sort_price_low_high', icon: 'mdi-sort-numeric-ascending' },
  { value: 'price-desc', orderBy: 'price', desc: true, text: 'sort_price_high_low', icon: 'mdi-sort-numeric-descending' },
  { value: 'rating-asc', orderBy: 'rating', desc: false, text: 'sort_rating_low_high', icon: 'mdi-sort-ascending' },
  { value: 'rating-desc', orderBy: 'rating', desc: true, text: 'sort_rating_high_low', icon: 'mdi-sort-descending' },
];

export function useProductsPage() {
  const { showError } = useNotifications();

  const companiesStore = useCompaniesStore();
  const categoriesStore = useCategoriesStore();

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
      const params = {
        page: baseSearch.value.pagination.currentPage - 1,
        order_by: baseSearch.value.filters.orderBy,
        desc: baseSearch.value.filters.desc,
        search: baseSearch.value.filters.search || undefined,
        company_id: baseSearch.value.filters.companyId || undefined,
        category_id: baseSearch.value.filters.categoryId || undefined,
        price_min: baseSearch.value.filters.priceMin || undefined,
        price_max: baseSearch.value.filters.priceMax || undefined,
        active: true,
      };

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
      showError('errors.load_products');
      console.error(error);
      isLoading.value = false;
    }
  };

  baseSearch.value = useBaseSearch(initialFilters, loadProducts);
  const resetCacheAndApply = () => {
    productsEtag.value = null;
    baseSearch.value.applyFilters();
  };

  const toggleCategory = (categoryId: number) => {
    baseSearch.value.filters.categoryId = baseSearch.value.filters.categoryId === categoryId ? null : categoryId;
    resetCacheAndApply();
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
    return baseSearch.value.hasActiveFilters;
  });

  return {
    products: data,

    companies: computed(() => companiesStore.items),
    categories: computed(() => categoriesStore.items),

    sortOptions,

    ...baseSearch.value,
    pagination: baseSearch.value.pagination,
    hasActiveFilters,
    handleSearch,

    isLoading,

    currentSort,
    currentSortText,

    toggleCategory,
    setSortOrder,
    initialize,

    applyFilters: resetCacheAndApply,
  };
}
