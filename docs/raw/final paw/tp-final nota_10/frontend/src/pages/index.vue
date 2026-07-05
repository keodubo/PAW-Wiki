<template>
  <div class="landing-page">
    <v-container fluid class="pa-6">
      <v-row justify="center" class="mb-6">
        <v-col cols="12" md="8" lg="6" xl="5">
          <div class="search-container d-flex align-center ga-3">
            <v-form @submit.prevent="onSearchSubmit" ref="searchForm" class="flex-grow-1">
              <v-text-field
                v-model="searchQuery"
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

            <v-btn type="submit" color="primary" size="x-large" class="search-btn elevation-6 rounded-xl" @click="onSearchSubmit" icon>
              <v-icon size="large">mdi-magnify</v-icon>
            </v-btn>

            <v-btn v-if="searchQuery" color="error" variant="elevated" size="x-large" class="clear-btn elevation-6 rounded-xl" @click="clearSearch" :title="$t('clear_filters')" icon>
              <v-icon size="large">mdi-filter-remove</v-icon>
            </v-btn>
          </div>
        </v-col>
      </v-row>

      <CategoryList :categories="categories || []" :selected-category-id="selectedCategoryId" :isLoadingCategories="isLoadingCategories" />

      <div class="mb-8">
        <router-link to="/pools" class="d-flex align-center mb-4 text-decoration-none text-primary pools-header-link">
          <h2 class="text-h4 font-weight-bold text-primary mb-0">
            {{ $t('hottest_pools') }}
          </h2>
          <v-icon color="primary" class="ml-2">mdi-chevron-right</v-icon>
        </router-link>
        <v-divider class="mb-4" />
        <div v-if="isLoadingHotPools" class="horizontal-scroll-pools">
          <div v-for="n in 6" :key="`hot-skeleton-${n}`" class="pool-card-scroll">
            <v-skeleton-loader :loading="true" type="image, heading, text" height="300" width="100%" />
          </div>
        </div>
        <div v-else-if="hotPools && hotPools.length" class="horizontal-scroll-pools">
          <div v-for="enrichedPool in hotPools" :key="enrichedPool.pool.id" class="pool-card-scroll">
            <PoolCard :enrichedPool="enrichedPool" :show-product-details="true" :show-user-request="false" />
          </div>
        </div>
        <div v-else class="text-center py-8">
          <v-img src="@/assets/empty.svg" alt="Empty list" max-width="150" class="mx-auto mb-4" />
          <p class="text-body-1 text-grey-darken-1">No hot pools available</p>
        </div>
      </div>

      <div v-if="authStore.hasRole('ROLE_CLIENT')" class="mb-8">
        <router-link :to="nearYouLink" class="d-flex align-center mb-4 text-decoration-none text-primary pools-header-link">
          <h2 class="text-h4 font-weight-bold text-primary mb-0">
            {{ $t('pools_near_you') }}
          </h2>
          <v-icon color="primary" class="ml-2">mdi-chevron-right</v-icon>
        </router-link>
        <v-divider class="mb-4" />
        <div v-if="isLoadingNearPools" class="horizontal-scroll-pools">
          <div v-for="n in 6" :key="`near-skeleton-${n}`" class="pool-card-scroll">
            <v-skeleton-loader :loading="true" type="image, heading, text" height="300" width="100%" />
          </div>
        </div>
        <div v-else-if="nearPools && nearPools.length" class="horizontal-scroll-pools">
          <div v-for="enrichedPool in nearPools" :key="enrichedPool.pool.id" class="pool-card-scroll">
            <PoolCard :enrichedPool="enrichedPool" :show-product-details="true" :show-user-request="false" />
          </div>
        </div>
        <div v-else class="text-center py-8">
          <v-img src="@/assets/empty.svg" alt="Empty list" max-width="150" class="mx-auto mb-4" />
          <p class="text-body-1 text-grey-darken-1">No pools near you available</p>
        </div>
      </div>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import CategoryList from '@/components/CategoryList.vue';
import PoolCard from '@/components/PoolCard.vue';

import { useLandingPage } from '@/composables/useLandingPage';

const authStore = useAuthStore();

const landingData = useLandingPage();
const { isLoadingHotPools, isLoadingNearPools, isLoadingCategories, categories, hotPools, nearPools, searchQuery, handleSearch, loadAll } = landingData;

const searchForm = ref();

const selectedCategoryId = ref<number | undefined>();

const onSearchSubmit = () => {
  handleSearch(searchQuery.value);
};

const clearSearch = () => {
  searchQuery.value = '';
};

onMounted(async () => {
  await loadAll();
});

const nearYouLink = computed(() => {
  return authStore.preferredLocation ? `/pools?locationId=${authStore.preferredLocation.id}` : '/pools';
});
</script>

<style scoped>
.horizontal-scroll-pools {
  display: flex;
  flex-direction: row;
  gap: 24px;
  overflow-x: auto;
  padding-bottom: 8px;
  margin-left: -8px;
  margin-right: -8px;
  scrollbar-width: thin;
  scrollbar-color: rgb(var(--v-theme-primary)) #eee;
}
.horizontal-scroll-pools::-webkit-scrollbar {
  height: 8px;
}
.horizontal-scroll-pools::-webkit-scrollbar-thumb {
  background: rgb(var(--v-theme-primary));
  border-radius: 4px;
}
.pool-card-scroll {
  min-width: 320px;
  max-width: 340px;
  flex: 0 0 auto;

  padding-top: 8px;
  padding-bottom: 8px;
}
.pools-header-link {
  cursor: pointer;
  transition: color 0.2s;
}
.pools-header-link:hover .text-primary,
.pools-header-link:hover .v-icon {
  color: rgb(var(--v-theme-secondary)) !important;
}
.landing-page {
  min-height: 100vh;
}

.text-primary {
  color: rgb(var(--v-theme-primary)) !important;
}

.text-primary:hover {
  color: rgb(var(--v-theme-secondary)) !important;
  transition: color 0.2s ease;
}

h2.text-primary {
  position: relative;
  display: inline-block;
}

h2.text-primary::after {
  content: '';
  position: absolute;
  bottom: -4px;
  left: 0;
  width: 50px;
  height: 3px;
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  border-radius: 2px;
}

.v-divider {
  opacity: 0.3;
  margin: 16px 0;
}

.v-col:hover {
  transform: translateY(-2px);
  transition: transform 0.3s ease;
}

.v-progress-circular {
  margin: 20px auto;
}

.v-alert {
  border-radius: 12px;
  font-weight: 500;
}

router-link {
  transition: all 0.2s ease;
}

router-link:hover {
  text-decoration: none !important;
}

.text-grey-darken-1 {
  font-size: 16px;
  font-weight: 500;
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
</style>
