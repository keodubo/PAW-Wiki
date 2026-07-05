import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick } from 'vue';
import { useProductsPage } from '@/composables/useProductsPage';

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

const companiesFetchAll = vi.fn().mockResolvedValue([]);
const categoriesFetchAll = vi.fn().mockResolvedValue([]);
const companiesFetch = vi.fn().mockResolvedValue({ id: 1, name: 'Acme' });
const categoriesFetch = vi.fn().mockResolvedValue({ id: 2, name: 'Drinks' });

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

const productListMock = vi.fn();

vi.mock('@/services', () => ({
  productService: {
    list: (...args: any[]) => productListMock(...args),
  },
}));

const productFixture = {
  id: 1,
  name: 'Agua',
  price: 100,
  company: '/companies/1',
  category: '/categories/2',
};

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useProductsPage', () => {
  beforeEach(() => {
    routeState = { query: {} };
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    companiesFetchAll.mockClear();
    categoriesFetchAll.mockClear();
    companiesFetch.mockClear();
    categoriesFetch.mockClear();
    productListMock.mockReset();
  });

  it('loads products with filters and enriches them', async () => {
    productListMock.mockResolvedValue({
      data: [productFixture],
      etag: 'etag-1',
      totalCount: 12,
      notModified: false,
    });

    const page = useProductsPage();
    await page.initialize();
    await flush();

    expect(companiesFetchAll).toHaveBeenCalled();
    expect(categoriesFetchAll).toHaveBeenCalled();
    expect(productListMock).toHaveBeenCalledWith(
      {
        page: 0,
        order_by: 'price',
        desc: false,
        search: undefined,
        company_id: undefined,
        category_id: undefined,
        price_min: undefined,
        price_max: undefined,
        active: true,
      },
      null,
    );
    expect(page.products.value).toHaveLength(1);
    expect(page.products.value[0].company).toEqual(expect.objectContaining({ id: 1 }));
    expect(page.products.value[0].category).toEqual(expect.objectContaining({ id: 2 }));
    expect(page.pagination.totalPages).toBe(1);
  });

  it('toggles category filter and pushes history', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });

    const page = useProductsPage();
    await page.initialize();
    await flush();
    routerPush.mockClear();
    productListMock.mockClear();

    page.toggleCategory(2);
    expect(routerPush).toHaveBeenCalledWith({ query: { categoryId: '2' } });
  });

  it('applies sort order and resets cache', async () => {
    productListMock.mockResolvedValue({
      data: [],
      etag: null,
      totalCount: 0,
      notModified: false,
    });
    const page = useProductsPage();
    await page.initialize();
    await flush();
    routerPush.mockClear();
    productListMock.mockClear();

    page.setSortOrder('rating', true);
    expect(routerPush).toHaveBeenCalledWith({ query: { orderBy: 'rating', desc: 'true' } });
  });
});
