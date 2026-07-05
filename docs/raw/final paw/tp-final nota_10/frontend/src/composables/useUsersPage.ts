import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useNotifications } from '@/composables/useNotifications';
import type { User, EnrichedUser } from '@/models';
import { useCompaniesStore } from '@/stores/companies';
import { userService } from '@/services';
import type { PaginationLinks } from '@/models/Http';

interface UsersPageFilters {
  search: string;
  validated: string | null;
  page: number;
}

const initialFilters: UsersPageFilters = {
  search: '',
  validated: null,
  page: 1,
};

export function useUsersPage() {
  const route = useRoute();
  const router = useRouter();
  const { showError, showSuccess } = useNotifications();

  const companiesStore = useCompaniesStore();
  const rawUsers = ref<User[]>([]);
  const enrichedUsers = ref<EnrichedUser[]>([]);
  const usersEtag = ref<string | null>(null);
  const isLoading = ref(true);
  const isSearching = ref(false);

  const filters = ref<UsersPageFilters>({ ...initialFilters });

  const pagination = ref({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
    pageSize: 12,
    links: undefined as PaginationLinks | undefined,
  });

  watch(
    rawUsers,
    async (newUsers) => {
      if (newUsers.length === 0) {
        enrichedUsers.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const resolvedData: EnrichedUser[] = await Promise.all(
          newUsers.map(async (user: User) => {
            let company = undefined;
            if (user.companyUri) {
              company = await companiesStore.fetch(user.companyUri);
            }

            return {
              user: user,
              company: company,
              location: undefined,
              reports: undefined,
            };
          }),
        );

        enrichedUsers.value = resolvedData;
      } catch (e) {
        console.error('Error enriching users', e);
      } finally {
        isLoading.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadUsers = async () => {
    isLoading.value = true;

    try {
      const params: any = {
        page: pagination.value.currentPage - 1,
      };

      if (filters.value.search) {
        params.search = filters.value.search;
      }

      if (filters.value.validated !== null) {
        params.validated = filters.value.validated === 'true';
      }

      const response = await userService.list(params, usersEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      rawUsers.value = response.data;
      usersEtag.value = response.etag;

      const totalCount = response.totalCount;
      pagination.value.totalItems = totalCount;
      pagination.value.totalPages = Math.ceil(totalCount / pagination.value.pageSize) || 1;
      pagination.value.links = response.links;
    } catch (error) {
      console.error(error);
      showError('errors.load_users');
    }
  };

  const updateURL = () => {
    const query: Record<string, string> = {};

    if (filters.value.search) {
      query.search = filters.value.search;
    }

    if (filters.value.validated !== null) {
      query.validated = filters.value.validated;
    }

    if (pagination.value.currentPage > 1) {
      query.page = pagination.value.currentPage.toString();
    }

    router.push({ query });
  };

  const initializeFromURL = () => {
    const query = route.query;

    filters.value.search = (query.search as string) || '';
    filters.value.validated = (query.validated as string) || null;
    pagination.value.currentPage = query.page ? parseInt(query.page as string) : 1;
  };

  const handleSearch = () => {
    isSearching.value = true;
    usersEtag.value = null;
    pagination.value.currentPage = 1;
    updateURL();
  };

  const handlePageChange = (newPage: number) => {
    pagination.value.currentPage = newPage;
    usersEtag.value = null;
    updateURL();
  };

  const setValidatedFilter = (value: string | null) => {
    filters.value.validated = value;
    pagination.value.currentPage = 1;
    usersEtag.value = null;
    updateURL();
  };

  const clearFilters = () => {
    filters.value = { ...initialFilters };
    pagination.value.currentPage = 1;
    usersEtag.value = null;
    updateURL();
  };

  const blockUser = async (userId: number, currentBlockLevel: number) => {
    try {
      await userService.blockUser(userId, currentBlockLevel);
      showSuccess('admin.user_blocked');
      usersEtag.value = null;
      await loadUsers();
    } catch (error) {
      showError('errors.block_user');
      console.error(error);
    }
  };

  const hasActiveFilters = computed(() => {
    return filters.value.search !== '' || filters.value.validated !== null;
  });

  const currentValidatedText = computed(() => {
    if (filters.value.validated === null) return 'admin.all_status';
    if (filters.value.validated === 'true') return 'admin.validated';
    return 'admin.not_validated';
  });

  const handleLinkNavigation = (url: string) => {
    const urlObj = new URL(url);
    const pageParam = urlObj.searchParams.get('page');
    if (pageParam !== null) {
      const page = parseInt(pageParam, 10);
      if (!isNaN(page)) {
        handlePageChange(page + 1);
      }
    }
  };

  const initialize = () => {
    initializeFromURL();
    loadUsers();
  };

  watch(
    () => route.query,
    async () => {
      initializeFromURL();
      await loadUsers();
      isSearching.value = false;
    },
  );

  return {
    enrichedUsers,
    isLoading,
    isSearching,
    filters,
    pagination,
    hasActiveFilters,
    currentValidatedText,

    handleSearch,
    handlePageChange,
    handleLinkNavigation,
    setValidatedFilter,
    clearFilters,
    blockUser,
    initialize,
  };
}
