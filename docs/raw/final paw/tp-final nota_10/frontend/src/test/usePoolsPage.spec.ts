import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick, reactive } from 'vue';
import { usePoolsPage } from '@/composables/usePoolsPage';
import { PoolStatus } from '@/models';

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
  await Promise.resolve();
};

describe('usePoolsPage', () => {
  beforeEach(() => {
    routeState.query = {};
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    companiesFetchAll.mockClear();
    categoriesFetchAll.mockClear();
    locationsFetchAll.mockClear();
    productsFetch.mockReset();
    locationsFetch.mockReset();
    companiesFetch.mockReset();
    categoriesFetch.mockReset();
    poolListMock.mockReset();

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
  });

  it('loads pools, enriches data, and sets pagination', async () => {
    const page = usePoolsPage();
    await page.initialize();
    await page.loadPools?.();
    await flush();
    await flush();

    expect(poolListMock).toHaveBeenCalled();
    expect(poolListMock).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0,
        order_by: 'requested_count',
        desc: true,
        status: PoolStatus.AVAILABLE,
      }),
      null,
    );
    expect(page.enrichedPools.value).toHaveLength(1);
    expect(page.enrichedPools.value[0].product).toEqual(expect.objectContaining({ id: 10 }));
    expect(page.enrichedPools.value[0].location).toEqual(expect.objectContaining({ id: 3 }));
    expect(page.pagination.currentPage).toBe(1);
    expect(page.pagination.totalPages).toBe(1);
  });

  it('toggleCategory updates filter and pushes query', async () => {
    const page = usePoolsPage();
    await page.initialize();
    await flush();
    routerPush.mockClear();

    page.toggleCategory(2);
    expect(routerPush).toHaveBeenCalledWith({ query: { categoryId: '2' } });
  });

  it('toggleLocation updates filter and pushes query', async () => {
    const page = usePoolsPage();
    await page.initialize();
    await flush();
    routerPush.mockClear();

    page.toggleLocation(3);
    expect(routerPush).toHaveBeenCalledWith({ query: { locationId: '3' } });
  });
});
