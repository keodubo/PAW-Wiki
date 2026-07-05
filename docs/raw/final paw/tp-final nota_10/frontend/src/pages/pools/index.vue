<template>
  <div class="pools-page">
    <v-container fluid>
      <v-row justify="center" class="mb-4">
        <v-col cols="12" md="8" lg="6" xl="5">
          <div class="search-container d-flex align-center ga-3">
            <v-form @submit.prevent="handleSearch" ref="searchForm" class="flex-grow-1">
              <v-text-field
                v-model="filters.search"
                :placeholder="$t('pools_search_placeholder')"
                variant="solo"
                density="compact"
                prepend-inner-icon="mdi-magnify"
                color="primary"
                base-color="grey-lighten-1"
                class="search-input elevation-6 rounded-xl"
                hide-details
                clearable
                flat
              />
            </v-form>

            <v-btn type="submit" color="primary" size="x-large" class="search-btn elevation-6 rounded-xl" :loading="isSearching" @click="handleSearch" icon>
              <v-icon size="large">mdi-magnify</v-icon>
            </v-btn>

            <v-btn v-if="hasActiveFilters || filters.search" color="error" variant="elevated" size="x-large" class="clear-btn elevation-6 rounded-xl" @click="handleClearFilters" :title="$t('clear_filters')" icon>
              <v-icon size="large">mdi-filter-remove</v-icon>
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12" md="3" lg="3" xl="3" class="mb-4">
          <v-card class="filters-card elevation-2 rounded-xl sticky-filters">
            <v-card-title class="px-4 py-3 bg-primary text-white">
              <v-icon start>mdi-filter</v-icon>
              {{ $t('filters') }}
            </v-card-title>
            <v-card-text class="pa-4">
              <div class="mb-3">
                <h6 class="text-subtitle-2 font-weight-bold mb-1">
                  {{ $t('total') }}: <span class="text-medium-emphasis">{{ totalPools }} {{ $t('pools') }}</span>
                </h6>
              </div>

              <div class="mb-3">
                <h6 class="text-subtitle-2 font-weight-bold mb-2">{{ $t('company') }}</h6>

                <CompanySelectorDialog :validated-only="true" @select="handleCompanySelect">
                  <template #activator="{ props }">
                    <v-card
                      v-bind="props"
                      variant="outlined"
                      class="company-filter-card"
                      hover
                    >
                      <v-card-text class="pa-3">
                        <div v-if="!selectedCompany" class="d-flex align-center text-medium-emphasis">
                          <v-icon start>mdi-office-building</v-icon>
                          <span class="text-body-2">{{ $t('all_companies') }}</span>
                          <v-spacer />
                          <v-icon>mdi-chevron-down</v-icon>
                        </div>
                        <div v-else class="d-flex align-center">
                          <v-avatar size="32" class="mr-2">
                            <v-img :src="selectedCompany.imageUri" :alt="selectedCompany.name">
                              <template #placeholder>
                                <v-img src="@/assets/empty.svg" :alt="selectedCompany.name" />
                              </template>
                            </v-img>
                          </v-avatar>
                          <div class="flex-grow-1">
                            <div class="text-body-2 font-weight-medium">{{ selectedCompany.name }}</div>
                          </div>
                          <v-btn
                            icon
                            size="x-small"
                            variant="text"
                            @click.stop="clearCompanyFilter"
                          >
                            <v-icon size="small">mdi-close</v-icon>
                          </v-btn>
                        </div>
                      </v-card-text>
                    </v-card>
                  </template>
                </CompanySelectorDialog>
              </div>

              <div class="mb-3">
                <h6 class="text-subtitle-2 font-weight-bold mb-2">{{ $t('price_range') }}</h6>
                <v-row>
                  <v-col cols="6">
                    <v-text-field v-model.number="filters.priceMin" label="Min" type="number" variant="outlined" density="compact" min="0" :rules="priceMinRules" @blur="applyFilters" @keyup.enter="applyFilters" />
                  </v-col>
                  <v-col cols="6">
                    <v-text-field v-model.number="filters.priceMax" label="Max" type="number" variant="outlined" density="compact" min="0" :rules="priceMaxRules" @blur="applyFilters" @keyup.enter="applyFilters" />
                  </v-col>
                </v-row>
              </div>

              <div v-if="locations.length > 0" class="mb-3">
                <h6 class="text-subtitle-2 font-weight-bold mb-2">{{ $t('location') }}</h6>
                <v-list class="pa-0" density="compact">
                  <v-list-item
                    v-for="location in locations"
                    :key="location.id"
                    v-show="location.id !== 1"
                    :class="{ 'bg-primary-lighten-5': filters.locationId === location.id }"
                    class="px-2 py-1 rounded cursor-pointer"
                    @click="toggleLocation(location.id)"
                  >
                    <template #prepend>
                      <v-icon v-if="filters.locationId === location.id" color="primary" size="small"> mdi-chevron-right </v-icon>
                    </template>
                    <v-list-item-title class="text-body-2"> {{ location.name }} ({{ location.poolsCount }}) </v-list-item-title>
                  </v-list-item>
                </v-list>
              </div>

              <div v-if="categories.length > 0" class="mb-3">
                <h6 class="text-subtitle-2 font-weight-bold mb-2">{{ $t('categories') }}</h6>
                <v-list class="pa-0" density="compact">
                  <v-list-item v-for="category in categories" :key="category.id" :class="{ 'bg-primary-lighten-5': filters.categoryId === category.id }" class="px-2 py-1 rounded cursor-pointer" @click="toggleCategory(category.id)">
                    <template #prepend>
                      <v-icon v-if="filters.categoryId === category.id" color="primary" size="small"> mdi-chevron-right </v-icon>
                    </template>
                    <v-list-item-title class="text-body-2"> {{ $t(`category.${category.name}`) }} ({{ category.poolsCount }}) </v-list-item-title>
                  </v-list-item>
                </v-list>
              </div>
            </v-card-text>
          </v-card>
        </v-col>

        <v-col cols="12" md="9" lg="9" xl="9">
          <div class="d-flex justify-end mb-4">
            <v-menu>
              <template #activator="{ props }">
                <v-btn v-bind="props" variant="outlined" color="primary" class="text-none" prepend-icon="mdi-sort">
                  {{ $t('sort') }}: {{ $t(currentSortText) }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </v-btn>
              </template>
              <v-list>
                <v-list-item v-for="option in sortOptions" :key="option.value" :class="{ 'bg-primary-lighten-4': currentSort === option.value }" @click="setSortOrder(option.orderBy, option.desc)">
                  <template #prepend>
                    <v-icon :icon="option.icon" size="small" class="mr-2" />
                  </template>
                  <v-list-item-title>{{ $t(option.text) }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>

          <div v-if="enrichedPools.length === 0 && !isLoading" class="text-center py-8">
            <v-img src="@/assets/empty.svg" alt="No pools found" max-width="300" class="mx-auto mb-4" />
            <h3 class="text-h6 text-medium-emphasis mb-4">{{ $t('no_pools_found') }}</h3>
            <v-btn v-if="hasActiveFilters" color="error" variant="outlined" @click="handleClearFilters" prepend-icon="mdi-filter-remove"> {{ $t('clear_filters') }} </v-btn>
          </div>

          <v-row v-else>
            <template v-if="isLoading">
              <v-col v-for="n in 8" :key="`skeleton-${n}`" cols="12" sm="6" md="6" lg="6" xl="6">
                <v-skeleton-loader type="card" />
              </v-col>
            </template>

            <template v-else>
              <v-col v-for="enrichedPool in enrichedPools" :key="enrichedPool.pool.id" cols="12" sm="6" lg="6" xl="6">
                <PoolCard :enrichedPool="enrichedPool" :show-product-details="true" :show-user-request="false" />
              </v-col>
            </template>
          </v-row>

          <PaginationLinks v-if="pagination.links" :links="pagination.links" :current-page="currentPage" :total-pages="totalPages" @navigate="handleLinkNavigation" class="mt-6" />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue';
import PoolCard from '@/components/PoolCard.vue';
import PaginationLinks from '@/components/PaginationLinks.vue';
import CompanySelectorDialog from '@/components/CompanySelectorDialog.vue';
import { usePoolsPage } from '@/composables/usePoolsPage';
import { useI18n } from 'vue-i18n';
import type { Company } from '@/models';
import { companyService } from '@/services';

const { t } = useI18n();
const poolsPage = usePoolsPage();

const selectedCompany = ref<Company | null>(null);

const {
  enrichedPools,
  categories,
  locations,
  sortOptions,

  isLoading,
  isSearching,
  filters,
  pagination,

  hasActiveFilters,
  currentSort,
  currentSortText,

  handleSearch,
  applyFilters,
  clearFilters,
  handlePageChange,
  toggleCategory,
  toggleLocation,
  setSortOrder,
  initialize,
} = poolsPage;

const handleCompanySelect = (company: Company) => {
  selectedCompany.value = company;
  filters.companyId = company.id;
  applyFilters();
};

const clearCompanyFilter = () => {
  selectedCompany.value = null;
  filters.companyId = null;
  applyFilters();
};

const handleClearFilters = () => {
  selectedCompany.value = null;
  clearFilters();
};

const priceMinRules = [
  (v: any) => {
    if (v === null || v === undefined || v === '') return true;
    const num = Number(v);
    if (isNaN(num)) return t('invalid_number');
    if (num < 0) return t('price_must_be_positive');
    return true;
  },
];

const priceMaxRules = [
  (v: any) => {
    if (v === null || v === undefined || v === '') return true;
    const num = Number(v);
    if (isNaN(num)) return t('invalid_number');
    if (num < 0) return t('price_must_be_positive');
    const minPrice = filters.priceMin;
    if (minPrice !== null && minPrice !== undefined && minPrice !== '' && num < Number(minPrice)) {
      return t('price_max_must_be_greater_than_min');
    }
    return true;
  },
];

const totalPools = computed(() => pagination.totalItems);
const currentPage = computed(() => pagination.currentPage);
const totalPages = computed(() => pagination.totalPages);

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

watch(() => filters.companyId, async (newId) => {
  if (!newId) {
    selectedCompany.value = null;
  } else if (newId !== selectedCompany.value?.id) {
    try {
      selectedCompany.value = await companyService.getById(newId);
    } catch {
      selectedCompany.value = null;
    }
  }
});

onMounted(async () => {
  await initialize();
  if (filters.companyId) {
    try {
      selectedCompany.value = await companyService.getById(filters.companyId);
    } catch {
      // silently ignore
    }
  }
});
</script>

<style scoped>
.pools-page {
  min-height: 100vh;
  background: rgb(var(--v-theme-background));
}

.search-container {
  align-items: stretch;
}

.search-input :deep(.v-field) {
  height: 56px !important;
  background: rgb(var(--v-theme-surface)) !important;
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
  border: 2px solid rgba(var(--v-theme-primary), 0.2) !important;
  border-radius: 12px !important;
}

.search-input :deep(.v-field--focused) {
  border: 2px solid rgb(var(--v-theme-primary)) !important;
  box-shadow: 0 8px 25px rgba(127, 0, 255, 0.3) !important;
}

.search-input :deep(.v-field__outline) {
  display: none !important;
}

.search-input :deep(.v-field__input) {
  min-height: 54px !important;
  padding-top: 0 !important;
  padding-bottom: 0 !important;
}

.search-input :deep(.v-field__prepend-inner) {
  padding-top: 0 !important;
  align-items: center !important;
}

.search-input :deep(.v-field__append-inner) {
  padding-top: 0 !important;
  align-items: center !important;
}

.search-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary)) !important;
}

.search-btn {
  background: linear-gradient(135deg, #7f00ff 0%, #a855f7 100%) !important;
  color: white !important;
  width: 56px;
  height: 56px;
  transition: all 0.3s ease;
}

.search-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(127, 0, 255, 0.4) !important;
}

.clear-btn {
  width: 56px;
  height: 56px;
  transition: all 0.3s ease;
}

.clear-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(244, 67, 54, 0.4) !important;
}

.filters-card {
  background: rgb(var(--v-theme-surface));
}

.sticky-filters {
  position: sticky;
  top: 80px;
  max-height: calc(100vh - 100px);
  overflow-y: auto;
  scrollbar-width: none;
  -ms-overflow-style: none;
}

.sticky-filters::-webkit-scrollbar {
  width: 0;
  height: 0;
}

.filters-card .v-card-title {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%) !important;
  border-radius: 12px 12px 0 0;
}

.filters-card .v-select :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary)) !important;
}

.v-list-item.cursor-pointer {
  transition: all 0.2s ease;
}

.v-list-item.cursor-pointer:hover {
  background-color: rgba(var(--v-theme-primary), 0.05) !important;
}

.bg-primary-lighten-5 {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
}

.bg-primary-lighten-4 {
  background-color: rgba(var(--v-theme-primary), 0.15) !important;
}

.company-filter-card {
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 8px;
}

.company-filter-card:hover {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.02) !important;
}

@media (max-width: 960px) {
  .sticky-filters {
    position: relative;
    top: 0;
    max-height: none;
  }
}
</style>
