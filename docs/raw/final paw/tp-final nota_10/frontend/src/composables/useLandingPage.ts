import { ref, computed, watch } from 'vue';
import { useRouter } from 'vue-router';
import { useNotifications } from '@/composables/useNotifications';
import { poolService } from '@/services';
import { useCategoriesStore } from '@/stores/categories';
import { useAuthStore } from '@/stores/auth';
import { UserRole } from '@/models/UserRole';
import { useProductsStore } from '@/stores/products';
import { useLocationsStore } from '@/stores/locations';
import { useCompaniesStore } from '@/stores/companies';
import type { Pool, EnrichedPool } from '@/models';
import { PoolStatus } from '@/models';

export function useLandingPage() {
  const router = useRouter();

  const { showError } = useNotifications();

  const categoriesStore = useCategoriesStore();
  const authStore = useAuthStore();
  const productsStore = useProductsStore();
  const locationsStore = useLocationsStore();
  const companiesStore = useCompaniesStore();

  const isLoadingHotPools = ref(false);
  const isLoadingNearPools = ref(false);
  const isLoadingCategories = ref(false);
  const searchQuery = ref('');

  const rawHotPools = ref<Pool[]>([]);
  const hotPoolsEtag = ref<string | null>(null);
  const enrichedHotPools = ref<EnrichedPool[]>([]);

  const rawNearPools = ref<Pool[]>([]);
  const nearPoolsEtag = ref<string | null>(null);
  const enrichedNearPools = ref<EnrichedPool[]>([]);

  const hasHotPools = computed(() => enrichedHotPools.value.length > 0);
  const hasNearPools = computed(() => enrichedNearPools.value.length > 0);

  const loadCategories = async () => {
    isLoadingCategories.value = true;
    try {
      await categoriesStore.fetchAll();
    } catch (error) {
      showError('errors.load_categories');
    } finally {
      isLoadingCategories.value = false;
    }
  };

  const enrichPool = async (pool: Pool): Promise<EnrichedPool | null> => {
    const product = await productsStore.fetch(pool.productUri);
    const location = await locationsStore.fetch(pool.locationUri);
    const company = product ? await companiesStore.fetch(product.companyUri) : null;
    const category = product ? await categoriesStore.fetch(product.categoryUri) : null;
    if (!product || !location || !company || !category) return null;
    return { pool, product, location, company, category };
  };

  watch(
    rawHotPools,
    async (newPools) => {
      if (newPools.length === 0) {
        enrichedHotPools.value = [];
        isLoadingHotPools.value = false;
        return;
      }
      try {
        const enriched = (await Promise.all(newPools.map(enrichPool))).filter(Boolean) as EnrichedPool[];
        enrichedHotPools.value = enriched;
      } catch (e) {
        console.error('Error enriching hot pools', e);
      } finally {
        isLoadingHotPools.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  watch(
    rawNearPools,
    async (newPools) => {
      if (newPools.length === 0) {
        enrichedNearPools.value = [];
        isLoadingNearPools.value = false;
        return;
      }
      try {
        const enriched = (await Promise.all(newPools.map(enrichPool))).filter(Boolean) as EnrichedPool[];
        enrichedNearPools.value = enriched;
      } catch (e) {
        console.error('Error enriching near pools', e);
      } finally {
        isLoadingNearPools.value = false;
      }
    },
    { deep: true, immediate: true },
  );

  const loadHotPools = async (limit: number = 6) => {
    isLoadingHotPools.value = true;
    try {
      const response = await poolService.list(
        {
          page: 0,
          order_by: 'requested_count',
          desc: true,
          status: PoolStatus.AVAILABLE,
        },
        hotPoolsEtag.value,
      );
      if (!response.notModified) {
        rawHotPools.value = response.data.slice(0, limit);
        hotPoolsEtag.value = response.etag;
      } else {
        isLoadingHotPools.value = false;
      }
    } catch (error) {
      showError('errors.load_hot_pools');
      isLoadingHotPools.value = false;
    }
  };

  const loadNearPools = async (limit: number = 6) => {
    isLoadingNearPools.value = true;
    try {
      const preferredLocation = authStore.preferredLocation;
      const params: any = {
        page: 0,
        order_by: 'requested_count',
        desc: true,
        status: PoolStatus.AVAILABLE,
      };
      if (preferredLocation && preferredLocation.id) {
        params.location_id = preferredLocation.id;
      }
      const response = await poolService.list(params, nearPoolsEtag.value);
      if (!response.notModified) {
        rawNearPools.value = response.data.slice(0, limit);
        nearPoolsEtag.value = response.etag;
      } else {
        isLoadingNearPools.value = false;
      }
    } catch (error) {
      showError('errors.load_pools_near_you');
      isLoadingNearPools.value = false;
    }
  };

  const handleSearch = (query: string) => {
    searchQuery.value = query;
    if (query.trim()) {
      router.push(`/pools?search=${encodeURIComponent(query.trim())}`);
    }
  };

  const loadAll = async () => {
    isLoadingHotPools.value = true;
    isLoadingNearPools.value = true;
    try {
      await loadCategories();
      await loadHotPools();

      if (authStore.hasRole(UserRole.CLIENT) && authStore.hasLocation) {
        await loadNearPools();
      } else {
        rawNearPools.value = [];
        isLoadingNearPools.value = false;
      }
    } finally {
    }
  };

  return {
    isLoadingHotPools: computed(() => isLoadingHotPools.value),
    isLoadingNearPools: computed(() => isLoadingNearPools.value),
    isLoadingCategories: computed(() => isLoadingCategories.value),
    categories: computed(() => categoriesStore.items),
    hotPools: computed(() => enrichedHotPools.value),
    nearPools: computed(() => enrichedNearPools.value),
    searchQuery,
    hasHotPools,
    hasNearPools,
    loadAll,
    loadCategories,
    handleSearch,
  };
}
