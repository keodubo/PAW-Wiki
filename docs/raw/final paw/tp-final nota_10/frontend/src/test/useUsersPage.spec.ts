import { describe, it, expect, vi, beforeEach } from 'vitest';
import { nextTick, reactive } from 'vue';
import { useUsersPage } from '@/composables/useUsersPage';

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

const companiesFetch = vi.fn();
vi.mock('@/stores/companies', () => ({
  useCompaniesStore: () => ({
    fetch: companiesFetch,
  }),
}));

const userListMock = vi.fn();
const blockUserMock = vi.fn();

vi.mock('@/services', () => ({
  userService: {
    list: (...args: any[]) => userListMock(...args),
    blockUser: (...args: any[]) => blockUserMock(...args),
  },
}));

const userFixture = {
  id: 1,
  email: 'a@b.com',
  companyUri: '/companies/1',
};

const flush = async () => {
  await nextTick();
  await nextTick();
};

describe('useUsersPage', () => {
  beforeEach(() => {
    routeState.query = {};
    routerPush = vi.fn(async (location: any) => {
      routeState.query = location?.query ?? {};
      return location;
    });
    showError.mockClear();
    showSuccess.mockClear();
    companiesFetch.mockReset();
    userListMock.mockReset();
    blockUserMock.mockReset();
    userListMock.mockResolvedValue({
      data: [userFixture],
      etag: 'etag-1',
      totalCount: 12,
      notModified: false,
    });
    companiesFetch.mockResolvedValue({ id: 1, name: 'Acme' });
    window.history.pushState({}, '', '/');
  });

  it('loads and enriches users', async () => {
    const page = useUsersPage();
    await page.initialize();
    await flush();

    expect(userListMock).toHaveBeenCalledWith({ page: 0 }, null);
    expect(page.enrichedUsers.value).toHaveLength(1);
    expect(page.enrichedUsers.value[0].company).toEqual(expect.objectContaining({ id: 1 }));
    expect(page.pagination.value.totalPages).toBe(1);
  });

  it('applies validated filter and resets page', async () => {
    const page = useUsersPage();
    await page.initialize();
    await flush();
    userListMock.mockClear();

    page.setValidatedFilter('true');
    await flush();

    expect(userListMock).toHaveBeenCalledWith({ page: 0, validated: true }, null);
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('handleSearch resets page and toggles searching flag', async () => {
    const page = useUsersPage();
    await page.initialize();
    await flush();
    userListMock.mockClear();

    page.filters.value.search = 'john';
    await page.handleSearch();
    await flush();
    await flush();

    expect(page.isSearching.value).toBe(false);
    expect(userListMock).toHaveBeenCalledWith({ page: 0, search: 'john' }, null);
    expect(page.pagination.value.currentPage).toBe(1);
  });

  it('blockUser calls service and reloads', async () => {
    blockUserMock.mockResolvedValue(undefined);
    const page = useUsersPage();
    await page.initialize();
    await flush();
    userListMock.mockClear();

    await page.blockUser(1, 0);
    await flush();

    expect(blockUserMock).toHaveBeenCalledWith(1, 0);
    expect(showSuccess).toHaveBeenCalled();
    expect(userListMock).toHaveBeenCalled();
  });
});
