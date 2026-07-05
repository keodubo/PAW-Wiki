import { ref, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useNotifications } from '@/composables/useNotifications';
import type { Company, EnrichedCompany } from '@/models';
import { companyService } from '@/services';
import { useUsersStore } from '@/stores/users';
import type { PaginationLinks } from '@/models/Http';

interface CompaniesPageFilters {
  search: string;
  validated: string | null;
  page: number;
}

const initialFilters: CompaniesPageFilters = {
  search: '',
  validated: null,
  page: 1,
};

export function useCompaniesPage() {
  const route = useRoute();
  const router = useRouter();
  const { showError, showSuccess } = useNotifications();

  const userStore = useUsersStore();
  const rawCompanies = ref<Company[]>([]);
  const enrichedCompanies = ref<EnrichedCompany[]>([]);
  const companiesEtag = ref<string | null>(null);
  const isLoading = ref(true);
  const isSearching = ref(false);

  const filters = ref<CompaniesPageFilters>({ ...initialFilters });

  const pagination = ref({
    currentPage: 1,
    totalPages: 1,
    totalItems: 0,
    pageSize: 12,
    links: undefined as PaginationLinks | undefined,
  });

  watch(
    rawCompanies,
    async (newCompanies) => {
      if (newCompanies.length === 0) {
        enrichedCompanies.value = [];
        isLoading.value = false;
        return;
      }

      try {
        const resolvedData: EnrichedCompany[] = await Promise.all(
          newCompanies.map(async (company: Company) => {
            const owner = await userStore.fetch(company.ownerUri);

            return {
              company: company,
              owner: owner,
              products: undefined,
            };
          }),
        );

        enrichedCompanies.value = resolvedData;
      } catch (e) {
        console.error('Error enriching companies', e);
      } finally {
        isLoading.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadCompanies = async () => {
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

      const response = await companyService.list(params, companiesEtag.value);

      if (response.notModified) {
        isLoading.value = false;
        return;
      }

      rawCompanies.value = response.data;
      companiesEtag.value = response.etag;

      const totalCount = response.totalCount;
      pagination.value.totalItems = totalCount;
      pagination.value.totalPages = Math.ceil(totalCount / pagination.value.pageSize) || 1;
      pagination.value.links = response.links;
    } catch (error) {
      console.error('Error loading companies', error);
      showError('errors.load_companies');
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
    companiesEtag.value = null;
    pagination.value.currentPage = 1;
    updateURL();
  };

  const handlePageChange = (newPage: number) => {
    pagination.value.currentPage = newPage;
    companiesEtag.value = null;
    updateURL();
  };

  const setValidatedFilter = (value: string | null) => {
    filters.value.validated = value;
    pagination.value.currentPage = 1;
    companiesEtag.value = null;
    updateURL();
  };

  const clearFilters = () => {
    filters.value = { ...initialFilters };
    pagination.value.currentPage = 1;
    companiesEtag.value = null;
    updateURL();
  };

  const validateCompany = async (companyId: number) => {
    try {
      await companyService.validate(companyId);
      showSuccess('admin.company_validated');
      companiesEtag.value = null;
      await loadCompanies();
    } catch (error) {
      showError('errors.validate_company');
      console.error(error);
    }
  };

  const hasActiveFilters = computed(() => {
    return filters.value.search !== '' || filters.value.validated !== null;
  });

  const currentValidatedText = computed(() => {
    if (filters.value.validated === null) return 'admin.companies.all_status';
    if (filters.value.validated === 'true') return 'admin.companies.validated';
    return 'admin.companies.not_validated';
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
    loadCompanies();
  };

  watch(
    () => route.query,
    async () => {
      initializeFromURL();
      await loadCompanies();
      isSearching.value = false;
    },
  );

  return {
    enrichedCompanies,
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
    validateCompany,
    initialize,
  };
}
