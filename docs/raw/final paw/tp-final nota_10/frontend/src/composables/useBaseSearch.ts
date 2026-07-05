import { ref, reactive, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { PaginationLinks } from '@/models/Http';

export interface BaseFilters {
  search: string;
  companyId: number | null;
  categoryId: number | null;
  priceMin: number | null;
  priceMax: number | null;
  orderBy: string;
  desc: boolean;
  status: number | null;
}

export interface PaginationState {
  currentPage: number;
  totalPages: number;
  totalItems: number;
  links?: PaginationLinks;
}

export interface BaseSearchOptions<T extends BaseFilters> {
  customURLBuilder?: (filters: T, initialFilters: T, query: Record<string, string>) => void;
  customURLParser?: (filters: T, query: Record<string, any>) => void;
}

export function useBaseSearch<T extends BaseFilters>(initialFilters: T, loadDataFn: () => Promise<void>, options?: BaseSearchOptions<T>) {
  const route = useRoute();
  const router = useRouter();

  const _initialFilters = { ...initialFilters };

  const isSearching = ref(false);
  const filters = reactive<T>({ ..._initialFilters });
  const pagination = reactive<PaginationState>({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
  });

  const hasActiveFilters = computed(() => {
    const keys = Object.keys(_initialFilters) as (keyof T)[];
    return keys.some((key) => {
      const value = (filters as T)[key];
      const initialValue = _initialFilters[key];

      const isEmpty = (v: any) => v === null || v === undefined || v === '';

      if (isEmpty(value) && isEmpty(initialValue)) {
        return false;
      }

      return value !== initialValue;
    });
  });

  const handleSearch = () => {
    isSearching.value = true;
    pagination.currentPage = 1;
    updateURL();
  };

  const applyFilters = () => {
    if (filters.priceMin !== null && filters.priceMin !== undefined) {
      if (isNaN(filters.priceMin) || filters.priceMin < 0) {
        filters.priceMin = null;
      }
    }
    if (filters.priceMax !== null && filters.priceMax !== undefined) {
      if (isNaN(filters.priceMax) || filters.priceMax < 0) {
        filters.priceMax = null;
      }
    }
    if (filters.priceMin !== null && filters.priceMax !== null && filters.priceMax < filters.priceMin) {
      filters.priceMax = null;
    }

    pagination.currentPage = 1;
    updateURL();
  };

  const clearFilters = () => {
    Object.assign(filters, _initialFilters);
    pagination.currentPage = 1;
    updateURL();
  };

  const handlePageChange = (page: number) => {
    pagination.currentPage = page;
    updateURL();
  };

  const updateURL = () => {
    const query: Record<string, string> = {};

    if (filters.search) query.search = filters.search;
    if (filters.companyId) query.companyId = filters.companyId.toString();
    if (filters.categoryId) query.categoryId = filters.categoryId.toString();

    const priceMin = filters.priceMin as any;
    if (priceMin !== null && priceMin !== undefined && priceMin !== '' && String(priceMin).trim() !== '') {
      const numMin = Number(priceMin);
      if (!isNaN(numMin) && numMin >= 0) {
        query.priceMin = numMin.toString();
      }
    }

    const priceMax = filters.priceMax as any;
    if (priceMax !== null && priceMax !== undefined && priceMax !== '' && String(priceMax).trim() !== '') {
      const numMax = Number(priceMax);
      if (!isNaN(numMax) && numMax >= 0) {
        const minPrice = query.priceMin ? Number(query.priceMin) : 0;
        if (numMax >= minPrice) {
          query.priceMax = numMax.toString();
        }
      }
    }

    if (filters.orderBy !== _initialFilters.orderBy) query.orderBy = filters.orderBy;
    if (filters.desc !== _initialFilters.desc) query.desc = filters.desc.toString();
    if (pagination.currentPage > 1) query.page = pagination.currentPage.toString();

    if (options?.customURLBuilder) {
      options.customURLBuilder(filters as T, _initialFilters, query);
    }

    router.push({ query });
  };

  const initializeFromURL = () => {
    const query = route.query;

    filters.search = (query.search as string) || '';
    filters.companyId = query.companyId ? parseInt(query.companyId as string) : null;
    filters.categoryId = query.categoryId ? parseInt(query.categoryId as string) : null;

    if (query.priceMin) {
      const parsedMin = parseFloat(query.priceMin as string);
      filters.priceMin = !isNaN(parsedMin) && parsedMin >= 0 ? parsedMin : null;
    } else {
      filters.priceMin = null;
    }

    if (query.priceMax) {
      const parsedMax = parseFloat(query.priceMax as string);
      if (!isNaN(parsedMax) && parsedMax >= 0) {
        if (filters.priceMin !== null && parsedMax < filters.priceMin) {
          filters.priceMax = null;
        } else {
          filters.priceMax = parsedMax;
        }
      } else {
        filters.priceMax = null;
      }
    } else {
      filters.priceMax = null;
    }

    filters.orderBy = (query.orderBy as string) || initialFilters.orderBy;
    filters.desc = query.desc ? query.desc === 'true' : initialFilters.desc;
    pagination.currentPage = query.page ? parseInt(query.page as string) : 1;

    if (options?.customURLParser) {
      options.customURLParser(filters as T, query);
    }
  };

  watch(
    () => route.query,
    async () => {
      initializeFromURL();
      await loadDataFn();
      isSearching.value = false;
    },
  );

  return {
    isSearching,
    filters,
    pagination,

    hasActiveFilters,

    handleSearch,
    applyFilters,
    clearFilters,
    handlePageChange,
    updateURL,
    initializeFromURL,
  };
}
