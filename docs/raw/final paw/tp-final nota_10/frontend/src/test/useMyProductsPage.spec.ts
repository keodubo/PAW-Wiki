import { reactive, nextTick } from 'vue';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { useMyProductsPage } from '@/composables/useMyProductsPage';

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

let mockCompanyId: number | null = 42;
const companiesFetchAll = vi.fn().mockResolvedValue([]);
const categoriesFetchAll = vi.fn().mockResolvedValue([]);
const companiesFetch = vi.fn().mockResolvedValue({ id: 1, name: 'Acme' });
const categoriesFetch = vi.fn().mockResolvedValue({ id: 2, name: 'Drinks' });

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    getCompanyId: mockCompanyId,
  }),
}));

vi.mock('@/stores/companies', () => ({
  useCompaniesStore: () => ({
    fetchAll: companiesFetchAll,
    fetch: companiesFetch,
  }),
}));

vi.mock('@/stores/categories', () => ({
  useCategoriesStore: () => ({
    fetchAll: categoriesFetchAll,
    fetch: categoriesFetch,
  }),
}));

const productListMock = vi.fn();

vi.mock('@/services', () => ({
  productService: {
    list: (...args: any[]) => productListMock(...args),
    delete: vi.fn(),
  },
}));

const productFixture = {
  id: 10,
  name: 'Soda',
  company: '/companies/1',
  category: '/categories/2',
  price: 100,
};

beforeEach(() => {
  routeState = reactive({ query: {} });
  routerPush = vi.fn(async (location: any) => {
    routeState.query = location?.query ?? {};
    return location;
  });
  mockCompanyId = 42;
  companiesFetchAll.mockClear();
  categoriesFetchAll.mockClear();
  companiesFetch.mockClear();
  categoriesFetch.mockClear();
  productListMock.mockReset();
});

const flushWatchers = async () => {
  await nextTick();
  await nextTick();
};

describe('useMyProductsPage', () => {
  it('loads products for the current company and enriches them', async () => {
    productListMock.mockResolvedValue({
      data: [productFixture],
      etag: 'etag-1',
      totalCount: 24,
      notModified: false,
    });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();

    expect(productListMock).toHaveBeenCalledWith(
      {
        page: 0,
        order_by: 'price',
        desc: false,
        search: undefined,
        company_id: mockCompanyId,
        active: true,
      },
      null,
    );
    expect(page.products.value).toHaveLength(1);
    expect(page.products.value[0].company).toEqual(expect.objectContaining({ id: 1 }));
    expect(page.products.value[0].category).toEqual(expect.objectContaining({ id: 2 }));
    expect(page.pagination.value.totalPages).toBe(2);
    expect(page.isLoading.value).toBe(false);
  });

  it('skips loading when the user has no company', async () => {
    mockCompanyId = null;
    const page = useMyProductsPage();

    await page.initialize();
    await flushWatchers();

    expect(productListMock).not.toHaveBeenCalled();
    expect(page.products.value).toEqual([]);
    expect(page.isLoading.value).toBe(false);
  });

  it('applies sort changes via history navigation', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();
    productListMock.mockClear();
    routerPush.mockClear();

    await page.setSortOrder('rating', true);
    await flushWatchers();

    expect(routerPush).toHaveBeenCalledWith({
      query: {
        orderBy: 'rating',
        desc: 'true',
      },
    });
    expect(productListMock).toHaveBeenCalledWith(expect.objectContaining({ order_by: 'rating', desc: true }), null);
  });

  it('handles search by pushing query and loading with search term', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();
    routerPush.mockClear();
    productListMock.mockClear();

    page.filters.value.search = 'agua';
    page.pagination.value.currentPage = 3;
    page.handleSearch();

    expect(routerPush).toHaveBeenCalledWith({ query: { search: 'agua' } });
    expect(page.pagination.value.currentPage).toBe(1);

    await flushWatchers();

    expect(productListMock).toHaveBeenCalledWith(expect.objectContaining({ search: 'agua' }), null);
  });

  it('clears filters and removes query params', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();
    routerPush.mockClear();

    page.filters.value.search = 'agua';
    page.filters.value.priceMax = 5000;
    page.pagination.value.currentPage = 2;

    page.clearFilters();
    expect(routerPush).toHaveBeenCalledWith({ query: {} });
    expect(page.filters.value.search).toBe('');
    expect(page.filters.value.priceMax).toBeNull();
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('short-circuits when response is not modified and leaves data intact', async () => {
    productListMock
      .mockResolvedValueOnce({
        data: [productFixture],
        etag: 'etag-1',
        totalCount: 1,
        notModified: false,
      })
      .mockResolvedValueOnce({
        data: [],
        etag: 'etag-1',
        totalCount: 1,
        notModified: true,
      });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();

    expect(page.products.value).toHaveLength(1);

    // second call with notModified should keep previous data
    await page.loadProducts();
    await flushWatchers();

    expect(page.products.value).toHaveLength(1);
    expect(page.isLoading.value).toBe(false);
  });

  it('initializes by fetching companies and categories before loading products', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });

    const page = useMyProductsPage();
    await page.initialize();
    await flushWatchers();

    expect(companiesFetchAll).toHaveBeenCalled();
    expect(categoriesFetchAll).toHaveBeenCalled();
    expect(productListMock).toHaveBeenCalled();
  });
});
