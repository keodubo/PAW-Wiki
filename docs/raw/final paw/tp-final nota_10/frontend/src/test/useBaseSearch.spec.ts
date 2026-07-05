import { nextTick, reactive } from 'vue';
import { describe, expect, it, vi, beforeEach } from 'vitest';
import { useBaseSearch, type BaseFilters } from '@/composables/useBaseSearch';

let routeState = reactive<{ query: Record<string, any> }>({ query: {} });
let routerPush = vi.fn(async (location: any) => {
  routeState.query = location?.query ?? {};
  return location;
});

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: (arg: any) => routerPush(arg),
    replace: (arg: any) => routerPush(arg),
  }),
}));

const baseFilters: BaseFilters = {
  search: '',
  companyId: null,
  categoryId: null,
  priceMin: null,
  priceMax: null,
  orderBy: 'price',
  desc: false,
  status: null,
};

const createComposable = (loadSpy = vi.fn().mockResolvedValue(undefined)) => {
  const composable = useBaseSearch(baseFilters, loadSpy);
  return { composable, loadSpy };
};

beforeEach(() => {
  routeState = reactive({ query: {} });
  routerPush = vi.fn(async (location: any) => {
    routeState.query = location?.query ?? {};
    return location;
  });
});

describe('useBaseSearch', () => {
  it('initializes filters from the URL query string', () => {
    routeState.query = {
      search: 'mate',
      categoryId: '3',
      priceMax: '5000',
      orderBy: 'rating',
      desc: 'true',
      page: '2',
    };

    const { composable } = createComposable();
    composable.initializeFromURL();

    expect(composable.filters.search).toBe('mate');
    expect(composable.filters.categoryId).toBe(3);
    expect(composable.filters.priceMax).toBe(5000);
    expect(composable.filters.orderBy).toBe('rating');
    expect(composable.filters.desc).toBe(true);
    expect(composable.pagination.currentPage).toBe(2);
  });

  it('pushes new history entries when applying filters', () => {
    const { composable } = createComposable();

    composable.filters.search = 'agua';
    composable.filters.categoryId = 2;
    composable.filters.priceMax = 5000;
    composable.filters.orderBy = 'rating';
    composable.filters.desc = true;

    composable.applyFilters();

    expect(routerPush).toHaveBeenCalledWith({
      query: {
        search: 'agua',
        categoryId: '2',
        priceMax: '5000',
        orderBy: 'rating',
        desc: 'true',
      },
    });
    expect(composable.hasActiveFilters.value).toBe(true);
    expect(composable.pagination.currentPage).toBe(1);
  });

  it('detects active filters when only sort direction changes', () => {
    const { composable } = createComposable();
    composable.filters.desc = true;

    expect(composable.hasActiveFilters.value).toBe(true);
  });

  it('clears filters, resets pagination, and removes query params', () => {
    const { composable } = createComposable();

    composable.filters.search = 'agua';
    composable.filters.priceMax = 5000;
    composable.pagination.currentPage = 3;

    composable.clearFilters();

    expect(routerPush).toHaveBeenCalledWith({ query: {} });
    expect(composable.filters.search).toBe('');
    expect(composable.filters.priceMax).toBeNull();
    expect(composable.pagination.currentPage).toBe(1);
    expect(composable.hasActiveFilters.value).toBe(false);
  });

  it('handleSearch forces first page and toggles searching flag around load', async () => {
    const loadSpy = vi.fn().mockResolvedValue(undefined);
    const { composable } = createComposable(loadSpy);

    composable.pagination.currentPage = 4;
    composable.filters.search = 'yerba';
    composable.handleSearch();

    expect(routerPush).toHaveBeenCalledWith({ query: { search: 'yerba' } });
    expect(composable.pagination.currentPage).toBe(1);
    expect(composable.isSearching.value).toBe(true);

    await nextTick();
    await nextTick();

    expect(loadSpy).toHaveBeenCalledTimes(1);
    expect(composable.isSearching.value).toBe(false);
  });

  it('includes pagination when navigating pages', () => {
    const { composable } = createComposable();

    composable.handlePageChange(3);

    expect(routerPush).toHaveBeenCalledWith({ query: { page: '3' } });
    expect(composable.pagination.currentPage).toBe(3);
  });

  it('reloads data and clears searching flag when the URL changes (e.g., browser back)', async () => {
    const loadSpy = vi.fn().mockResolvedValue(undefined);
    const { composable } = createComposable(loadSpy);

    composable.filters.search = 'yerba';
    composable.handleSearch();

    expect(composable.isSearching.value).toBe(true);
    await nextTick(); // wait for router push + watcher to react
    await nextTick();

    expect(loadSpy).toHaveBeenCalledTimes(1);
    expect(composable.isSearching.value).toBe(false);

    routeState.query = { search: 'agua' };
    await nextTick();
    await nextTick();

    expect(loadSpy).toHaveBeenCalledTimes(2);
  });

  it('uses custom URL builder to append extra query params', () => {
    const loadSpy = vi.fn().mockResolvedValue(undefined);
    const customBuilder = vi.fn((filters, initial, query) => {
      if (filters.status !== null) query.status = `${filters.status}`;
    });

    const baseFiltersWithStatus = { ...baseFilters, status: 1 };
    const composable = useBaseSearch(baseFiltersWithStatus, loadSpy, { customURLBuilder: customBuilder });

    composable.applyFilters();

    expect(customBuilder).toHaveBeenCalled();
    expect(routerPush).toHaveBeenCalledWith({ query: { status: '1' } });
  });
});
