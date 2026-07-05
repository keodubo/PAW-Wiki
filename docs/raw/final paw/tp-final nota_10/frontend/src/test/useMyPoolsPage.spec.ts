import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick, reactive } from 'vue';
import { useMyPoolsPage } from '@/composables/useMyPoolsPage';
import { PoolStatus } from '@/models';

const showError = vi.fn();

vi.mock('@/composables/useNotifications', () => ({
  useNotifications: () => ({
    showError,
  }),
}));

let routeState = reactive({ query: {} as Record<string, any> });
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

let authStoreCompanyId: number | null = 42;
const authStore = {
  get getCompanyId() {
    return authStoreCompanyId;
  },
};

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}));

const companiesFetchAll = vi.fn().mockResolvedValue([]);
const categoriesFetchAll = vi.fn().mockResolvedValue([]);
const locationsFetchAll = vi.fn().mockResolvedValue([]);
const productsFetch = vi.fn();
const locationsFetch = vi.fn();
const companiesFetch = vi.fn();
const categoriesFetch = vi.fn();

vi.mock('@/stores/companies', () => ({
  useCompaniesStore: () => ({
    fetchAll: companiesFetchAll,
    fetch: companiesFetch,
    items: [{ id: 1, name: 'Acme' }],
  }),
}));

vi.mock('@/stores/categories', () => ({
  useCategoriesStore: () => ({
    fetchAll: categoriesFetchAll,
    fetch: categoriesFetch,
    items: [{ id: 2, name: 'Drinks' }],
  }),
}));

vi.mock('@/stores/locations', () => ({
  useLocationsStore: () => ({
    fetchAll: locationsFetchAll,
    fetch: locationsFetch,
    items: [{ id: 3, name: 'BA' }],
  }),
}));

vi.mock('@/stores/products', () => ({
  useProductsStore: () => ({
    fetch: productsFetch,
  }),
}));

const poolListMock = vi.fn();
const productServiceList = vi.fn();

vi.mock('@/services/ProductService', () => {
  class ProductServiceMock {
    list = (...args: any[]) => productServiceList(...args);
  }
  return { ProductService: ProductServiceMock };
});

vi.mock('@/services', () => ({
  poolService: {
    list: (...args: any[]) => poolListMock(...args),
  },
}));

const poolFixture = {
  id: 1,
  product: '/products/10',
  location: '/locations/3',
  price: 100,
  minQuantity: 10,
  requestsStats: {
    pendingCount: 0,
    pendingSum: 1,
    acceptedCount: 0,
    acceptedSum: 2,
    rejectedCount: 0,
    deliveredCount: 0,
    deliveredSum: 0,
    acceptedOrDeliveredSum: 2,
    productsCount: 0,
  },
  status: PoolStatus.AVAILABLE,
};

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useMyPoolsPage', () => {
  beforeEach(() => {
    routeState.query = {};
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    showError.mockClear();
    companiesFetchAll.mockClear();
    categoriesFetchAll.mockClear();
    locationsFetchAll.mockClear();
    productsFetch.mockReset();
    locationsFetch.mockReset();
    companiesFetch.mockReset();
    categoriesFetch.mockReset();
    poolListMock.mockReset();
    productServiceList.mockReset();
    authStoreCompanyId = 42;

    productsFetch.mockResolvedValue({ id: 10, name: 'Agua', company: '/companies/1', category: '/categories/2' });
    locationsFetch.mockResolvedValue({ id: 3, name: 'BA' });
    companiesFetch.mockResolvedValue({ id: 1, name: 'Acme' });
    categoriesFetch.mockResolvedValue({ id: 2, name: 'Drinks' });

    poolListMock.mockResolvedValue({
      data: [poolFixture],
      etag: 'etag-1',
      totalCount: 12,
      notModified: false,
    });
    productServiceList.mockResolvedValue({ data: [{ id: 10, name: 'Agua' }] });
  });

  it('loads pools for current company and enriches data', async () => {
    const page = useMyPoolsPage();
    await page.initialize();
    await flush();
    await page.loadPools?.();
    await flush();

    expect(poolListMock).toHaveBeenCalledWith(
      expect.objectContaining({
        company_id: 42,
        status: undefined,
        product_id: undefined,
      }),
      null,
    );
    expect(page.enrichedPools.value).toHaveLength(1);
    expect(page.enrichedPools.value[0].product).toEqual(expect.objectContaining({ id: 10 }));
    expect(page.productOptions.value).toEqual(expect.arrayContaining([expect.objectContaining({ value: 0, text: 'all' }), expect.objectContaining({ value: 10, text: 'Agua' })]));
  });

  it('sets status filter and pushes query', async () => {
    const page = useMyPoolsPage();
    await page.initialize();
    await flush();
    poolListMock.mockClear();

    page.setStatus(PoolStatus.DELIVERING);
    await flush();
    await flush();
    expect(poolListMock).toHaveBeenCalledWith(
      expect.objectContaining({
        status: PoolStatus.DELIVERING,
      }),
      null,
    );
  });

  it('clears filters, including status and productId', async () => {
    const page = useMyPoolsPage();
    await page.initialize();
    await flush();

    page.setStatus(PoolStatus.AVAILABLE);
    page.setProductId(10);
    page.filters.value.search = 'agua';
    page.pagination.value.currentPage = 2;

    page.clearFilters();
    expect(page.status.value).toBeNull();
    expect(page.productId.value).toBeNull();
    expect(page.filters.value.search).toBe('');
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('shows error when no company id is present', async () => {
    authStoreCompanyId = null;
    poolListMock.mockClear();
    const page = useMyPoolsPage();
    await page.initialize();
    await flush();

    expect(showError).toHaveBeenCalled();
    expect(poolListMock).not.toHaveBeenCalled();
  });
});
