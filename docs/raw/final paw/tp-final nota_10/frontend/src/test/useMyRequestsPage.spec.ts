import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick } from 'vue';
import { useMyRequestsPage } from '@/composables/useMyRequestsPage';
import { PoolStatus, RequestStatus } from '@/models';

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
    push: (arg: any) => routerPush(arg),
    replace: (arg: any) => routerPush(arg),
  }),
}));

const authStore = { getCompanyId: 42, getUserId: 99 };

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
const poolsFetch = vi.fn();

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

vi.mock('@/stores/pools', () => ({
  usePoolsStore: () => ({
    fetch: poolsFetch,
  }),
}));

const requestListMock = vi.fn();
const poolServiceListMock = vi.fn();
const productServiceListMock = vi.fn();

vi.mock('@/services', () => ({
  requestService: {
    list: (...args: any[]) => requestListMock(...args),
  },
  poolService: {
    list: (...args: any[]) => poolServiceListMock(...args),
  },
}));

vi.mock('@/services/ProductService', () => {
  class ProductServiceMock {
    list = (...args: any[]) => productServiceListMock(...args);
  }
  return { ProductService: ProductServiceMock };
});

const requestFixture = {
  id: 1,
  pool: '/pools/5',
  status: RequestStatus.PENDING,
};

const poolFixture = {
  id: 5,
  product: '/products/10',
  location: '/locations/3',
  status: PoolStatus.AVAILABLE,
};

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useMyRequestsPage', () => {
  beforeEach(() => {
    routeState = { query: {} };
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
    poolsFetch.mockReset();
    requestListMock.mockReset();
    poolServiceListMock.mockReset();
    productServiceListMock.mockReset();
    authStore.getCompanyId = 42;

    productsFetch.mockResolvedValue({ id: 10, name: 'Agua', company: '/companies/1', category: '/categories/2' });
    locationsFetch.mockResolvedValue({ id: 3, name: 'BA' });
    companiesFetch.mockResolvedValue({ id: 1, name: 'Acme' });
    categoriesFetch.mockResolvedValue({ id: 2, name: 'Drinks' });
    poolsFetch.mockResolvedValue(poolFixture);
    requestListMock.mockResolvedValue({
      data: [requestFixture],
      etag: 'etag-1',
      totalCount: 12,
      notModified: false,
    });
    productServiceListMock.mockResolvedValue({ data: [{ id: 10, name: 'Agua' }] });
    poolServiceListMock.mockResolvedValue({ data: [{ id: 5, product: '/products/10' }] });
  });

  it('loads requests, enriches data, and builds product/company options', async () => {
    const page = useMyRequestsPage();
    await page.initialize();
    await flush();
    await page.loadRequests?.();
    await flush();

    expect(requestListMock).toHaveBeenCalledWith(
      expect.objectContaining({
        user_id: 99,
      }),
      null,
    );
    expect(page.enrichedRequests.value).toHaveLength(1);
    expect(page.enrichedRequests.value[0].product).toEqual(expect.objectContaining({ id: 10 }));
    expect(page.productOptions.value).toEqual(expect.arrayContaining([expect.objectContaining({ value: 0, text: 'all' }), expect.objectContaining({ value: 10 })]));
    expect(page.companyOptions.value).toEqual(expect.arrayContaining([expect.objectContaining({ value: 0, text: 'all' })]));
  });

  it('sets pool status filter and triggers reload', async () => {
    const page = useMyRequestsPage();
    await page.initialize();
    await flush();
    requestListMock.mockClear();

    page.setPoolStatus(PoolStatus.DELIVERING);
    await flush();
    await page.loadRequests?.();
    await flush();

    expect(requestListMock).toHaveBeenCalledWith(expect.objectContaining({ pool_status: PoolStatus.DELIVERING }), null);
  });

  it('clears filters, including statuses and product/company ids', async () => {
    const page = useMyRequestsPage();
    await page.initialize();
    await flush();

    page.setPoolStatus(PoolStatus.AVAILABLE);
    page.setRequestStatus(RequestStatus.PENDING);
    page.setProductId(10);
    page.setCompanyId(1);
    page.filters.value.search = 'agua';
    page.pagination.value.currentPage = 2;

    page.clearFilters();
    expect(page.poolStatus.value).toBeNull();
    expect(page.requestStatus.value).toBeNull();
    expect(page.productId.value).toBeNull();
    expect(page.companyId.value).toBeNull();
    expect(page.filters.value.search).toBe('');
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('shows error when no company id', async () => {
    authStore.getCompanyId = null as any;
    authStore.getUserId = null as any;
    const page = useMyRequestsPage();
    await page.initialize();
    await flush();

    expect(showError).toHaveBeenCalled();
    expect(requestListMock).not.toHaveBeenCalled();
  });
});
