import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick } from 'vue';
import { useLandingPage } from '@/composables/useLandingPage';
import { PoolStatus } from '@/models';
import { UserRole } from '@/models/UserRole';

const showError = vi.fn();
vi.mock('@/composables/useNotifications', () => ({
  useNotifications: () => ({
    showError,
  }),
}));

let routeState = { query: {} as Record<string, any> };
let routerPush = vi.fn(async (location: any) => {
  routeState.query = location?.query ?? {};
  return location;
});

vi.mock('vue-router', () => ({
  useRoute: () => routeState,
  useRouter: () => ({
    push: (location: any) => routerPush(location),
  }),
}));

const authStore = {
  hasRole: (role: UserRole) => role === UserRole.CLIENT,
  hasLocation: true,
  preferredLocation: { id: 3, name: 'BA' },
};

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => authStore,
}));

const categoriesFetchAll = vi.fn();
const categoriesFetch = vi.fn();
vi.mock('@/stores/categories', () => ({
  useCategoriesStore: () => ({
    fetchAll: categoriesFetchAll,
    fetch: categoriesFetch,
    items: [{ id: 1, name: 'Drinks' }],
  }),
}));

const productsFetch = vi.fn();
const locationsFetch = vi.fn();
const companiesFetch = vi.fn();
vi.mock('@/stores/products', () => ({
  useProductsStore: () => ({
    fetch: productsFetch,
  }),
}));
vi.mock('@/stores/locations', () => ({
  useLocationsStore: () => ({
    fetch: locationsFetch,
  }),
}));
vi.mock('@/stores/companies', () => ({
  useCompaniesStore: () => ({
    fetch: companiesFetch,
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
  status: PoolStatus.AVAILABLE,
  requestsStats: {
    pendingCount: 0,
    pendingSum: 0,
    acceptedCount: 0,
    acceptedSum: 0,
    rejectedCount: 0,
    deliveredCount: 0,
    deliveredSum: 0,
    acceptedOrDeliveredSum: 0,
    productsCount: 5,
  },
};

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useLandingPage', () => {
  beforeEach(() => {
    routeState = { query: {} };
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    showError.mockClear();
    categoriesFetchAll.mockClear();
    categoriesFetch.mockClear();
    categoriesFetch.mockClear();
    productsFetch.mockReset();
    locationsFetch.mockReset();
    companiesFetch.mockReset();
    poolListMock.mockReset();

    productsFetch.mockResolvedValue({ id: 10, name: 'Agua', company: '/companies/1', category: '/categories/1' });
    locationsFetch.mockResolvedValue({ id: 3, name: 'BA' });
    companiesFetch.mockResolvedValue({ id: 5, name: 'Acme' });
    categoriesFetch.mockResolvedValue({ id: 1, name: 'Drinks' });
    poolListMock.mockResolvedValue({
      data: [poolFixture],
      etag: 'etag-1',
      totalCount: 1,
      notModified: false,
    });
  });

  it('loads categories, hot pools, and near pools for client with location', async () => {
    const landing = useLandingPage();

    await landing.loadAll();
    await flush();
    await flush();

    expect(categoriesFetchAll).toHaveBeenCalled();
    // hot pools call
    expect(poolListMock).toHaveBeenCalledWith(
      expect.objectContaining({
        page: 0,
        order_by: 'requested_count',
        desc: true,
        status: PoolStatus.AVAILABLE,
      }),
      null,
    );
    expect(landing.hotPools.value).toHaveLength(1);
    expect(landing.nearPools.value).toHaveLength(1);
  });

  it('falls back to no near pools when user has no location', async () => {
    authStore.hasLocation = false;
    const landing = useLandingPage();
    await landing.loadAll();
    await flush();
    expect(landing.nearPools.value).toEqual([]);
    authStore.hasLocation = true;
  });

  it('handleSearch pushes to pools with query', async () => {
    const landing = useLandingPage();
    landing.handleSearch('agua');
    expect(routerPush).toHaveBeenCalledWith('/pools?search=agua');
  });
});
