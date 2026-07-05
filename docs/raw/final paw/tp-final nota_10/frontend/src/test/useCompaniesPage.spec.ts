import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick, reactive } from 'vue';
import { useCompaniesPage } from '@/composables/useCompaniesPage';

const showError = vi.fn();
const showSuccess = vi.fn();

vi.mock('@/composables/useNotifications', () => ({
  useNotifications: () => ({
    showError,
    showSuccess,
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

const userFetch = vi.fn();

vi.mock('@/stores/users', () => ({
  useUsersStore: () => ({
    fetch: userFetch,
  }),
}));

const companyListMock = vi.fn();
const companyValidateMock = vi.fn();

vi.mock('@/services', () => ({
  companyService: {
    list: (...args: any[]) => companyListMock(...args),
    validate: (...args: any[]) => companyValidateMock(...args),
  },
}));

const flush = async () => {
  await nextTick();
  await nextTick();
};

const companyFixture = {
  id: 1,
  name: 'Acme',
  owner: '/users/1',
};

describe('useCompaniesPage', () => {
  beforeEach(() => {
    routeState.query = {};
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    showError.mockClear();
    showSuccess.mockClear();
    userFetch.mockReset();
    companyListMock.mockReset();
    companyValidateMock.mockReset();
    companyListMock.mockResolvedValue({
      data: [companyFixture],
      etag: 'etag-1',
      totalCount: 12,
      notModified: false,
    });
    userFetch.mockResolvedValue({ id: 1, firstName: 'John' });
    // reset URL
    window.history.pushState({}, '', '/');
  });

  it('loads and enriches companies with pagination', async () => {
    const page = useCompaniesPage();

    await page.initialize();
    await flush();

    expect(companyListMock).toHaveBeenCalledWith({ page: 0 }, null);
    expect(page.enrichedCompanies.value).toHaveLength(1);
    expect(page.enrichedCompanies.value[0].owner).toEqual(expect.objectContaining({ id: 1 }));
    expect(page.pagination.value.totalPages).toBe(1);
    expect(page.isLoading.value).toBe(false);
  });

  it('applies validated filter and resets page', async () => {
    const page = useCompaniesPage();
    await page.initialize();
    await flush();
    companyListMock.mockClear();

    page.setValidatedFilter('true');
    await flush();

    expect(companyListMock).toHaveBeenCalledWith({ page: 0, validated: true }, null);
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('handleSearch toggles searching flag and resets page', async () => {
    const page = useCompaniesPage();
    await page.initialize();
    await flush();
    companyListMock.mockClear();

    page.filters.value.search = 'acme';
    await page.handleSearch();
    await flush();
    await flush();

    expect(page.isSearching.value).toBe(false);
    expect(companyListMock).toHaveBeenCalledWith({ page: 0, search: 'acme' }, null);
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('clearFilters resets filters and reloads', async () => {
    const page = useCompaniesPage();
    await page.initialize();
    await flush();
    companyListMock.mockClear();

    page.filters.value.search = 'foo';
    page.filters.value.validated = 'true';
    page.pagination.value.currentPage = 2;

    await page.clearFilters();
    await flush();

    expect(page.filters.value.search).toBe('');
    expect(page.filters.value.validated).toBeNull();
    expect(page.pagination.value.currentPage).toBe(1);
    expect(companyListMock).toHaveBeenCalledWith({ page: 0 }, null);
  });

  it('validateCompany calls service and reloads', async () => {
    companyValidateMock.mockResolvedValue(undefined);
    const page = useCompaniesPage();
    await page.initialize();
    await flush();
    companyListMock.mockClear();

    await page.validateCompany(1);
    await flush();

    expect(companyValidateMock).toHaveBeenCalledWith(1);
    expect(showSuccess).toHaveBeenCalled();
    expect(companyListMock).toHaveBeenCalled();
  });
});
