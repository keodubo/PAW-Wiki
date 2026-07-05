<template>
  <v-container class="py-8 product-detail-page" fluid>
    <div class="page-wrap">
      <v-card class="product-hero elevation-3 rounded-xl">
        <v-row class="ma-0" no-gutters align="start">
          <v-col cols="12" md="6" class="image-col d-flex align-center justify-center">
            <v-skeleton-loader v-if="isLoading" type="image" class="h-100" />
            <v-img v-else :src="productImage" :alt="product?.name" max-height="420" contain class="product-image">
              <template #placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-progress-circular indeterminate color="grey"></v-progress-circular>
                </div>
              </template>
            </v-img>
          </v-col>

          <v-col cols="12" md="6" class="pa-8 d-flex flex-column">
            <div v-if="isLoading">
              <v-skeleton-loader type="heading, text@3, actions"></v-skeleton-loader>
            </div>
            <div v-else-if="product">
              <div class="d-flex align-center mb-3 ga-2 flex-wrap">
                <v-chip v-if="!product.active" color="error" variant="flat" size="large" prepend-icon="mdi-delete-alert" class="deleted-chip elevation-2 mr-2">
                  {{ t('product_deleted_badge') }}
                </v-chip>
                <v-chip v-if="product.categoryData" color="primary" variant="flat" size="large" prepend-icon="mdi-tag" class="category-chip elevation-2 mr-2" @click="router.push(`/products?categoryId=${product.categoryData.id}`)">
                  {{ $t(`category.${product.categoryData?.name}`) ?? t('product_detail.uncategorized') }}
                </v-chip>
                <v-chip v-if="product.companyData" color="secondary" variant="flat" size="large" prepend-icon="mdi-domain" class="company-chip elevation-2" @click="router.push(`/companies/${product.companyData.id}`)">
                  {{ product.companyData?.name ?? t('product_detail.unknown_company') }}
                </v-chip>
              </div>

              <h1 class="text-h4 font-weight-bold mb-2">{{ product.name }}</h1>

              <div class="d-flex align-center mb-3">
                <div class="text-h4 font-weight-bold text-success mr-4">
                  {{ formatCurrency(product.price) }}
                </div>
                <div class="d-flex align-center">
                  <v-rating :model-value="averageRating" color="warning" density="compact" half-increments readonly size="small" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mr-1" />
                  <span class="text-body-2 text-medium-emphasis">
                    {{ ratingLabel }}
                  </span>
                </div>
              </div>

              <div class="d-flex align-center flex-wrap ga-2 mb-4">
                <v-chip size="small" color="primary" variant="tonal" prepend-icon="mdi-ray-start-arrow">
                  {{ t('product_detail.active_pools_count', { count: activePoolsCount }) }}
                </v-chip>
                <v-chip size="small" color="secondary" variant="tonal" prepend-icon="mdi-finance">
                  {{ t('product_detail.total_pools_count', { count: totalPoolsCount }) }}
                </v-chip>
                <v-chip size="small" color="success" variant="tonal" prepend-icon="mdi-check-decagram">
                  {{ t('product_detail.finished_pools_count', { count: finishedPoolsCount }) }}
                </v-chip>
                <v-chip size="small" color="warning" variant="tonal" prepend-icon="mdi-message-text-outline">
                  {{ t('product_detail.reviews_count', { count: reviewItems.length }) }}
                </v-chip>
              </div>

              <p class="text-body-1 text-medium-emphasis mb-6">
                {{ product.description }}
              </p>

              <v-divider class="mb-4"></v-divider>

              <div class="d-flex align-center ga-3 flex-wrap">
                <div class="d-flex align-center">
                  <v-icon color="primary" class="mr-2">mdi-office-building</v-icon>
                  <span class="text-body-2 text-medium-emphasis">
                    {{ product.companyData?.name ?? t('product_detail.unknown_company') }}
                  </span>
                </div>
                <div class="d-flex align-center">
                  <v-icon color="primary" class="mr-2">mdi-tag-multiple</v-icon>
                  <span class="text-body-2 text-medium-emphasis">
                    {{ $t(`category.${product.categoryData?.name}`) ?? t('product_detail.no_category') }}
                  </span>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-8 text-medium-emphasis">Could not load product.</div>
          </v-col>
        </v-row>
      </v-card>

      <section v-if="activePools.length" class="mt-8 section-block">
        <router-link :to="activePoolsLink" class="d-flex align-center mb-4 text-decoration-none text-primary pools-header-link">
          <h2 class="text-h4 font-weight-bold text-primary mb-0">
            {{ t('product_detail.active_pools_section') }}
          </h2>
          <v-icon color="primary" class="ml-2">mdi-chevron-right</v-icon>
        </router-link>
        <v-divider class="mb-4" />
        <div class="horizontal-scroll-pools">
          <div v-for="pool in activePools" :key="pool.pool.id" class="pool-card-scroll">
            <PoolCard :enriched-pool="pool" :show-product-details="false" :show-user-request="false" />
          </div>
        </div>
      </section>

      <v-divider class="my-8"></v-divider>

      <section class="section-block">
        <h2 class="text-h4 font-weight-bold text-primary mb-4">
          {{ t('product_detail.reviews_title') }}
        </h2>
        <v-divider class="mb-4" />

        <div id="reviews"></div>
        <v-row v-if="product" class="reviews-layout">
          <v-col cols="12" md="4" lg="3">
            <v-card class="pa-4 elevation-1 reviews-summary-card">
              <div class="d-flex flex-column ga-4">
                <div class="d-flex align-center">
                  <div class="text-h3 font-weight-bold mr-3">{{ averageRating.toFixed(1) }}</div>
                  <div>
                    <v-rating :model-value="averageRating" color="warning" density="comfortable" half-increments readonly size="large" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mb-1" />
                    <div class="text-body-2 text-medium-emphasis">
                      {{ t('product_detail.total_ratings', { count: totalRatings }) }}
                    </div>
                  </div>
                </div>

                <div class="flex-grow-1">
                  <div v-for="star in [5, 4, 3, 2, 1]" :key="star" class="d-flex align-center mb-1">
                    <span class="text-caption text-medium-emphasis mr-2" style="width: 12px">{{ star }}</span>
                    <v-progress-linear :model-value="ratingPercents[star] || 0" height="8" color="primary" bg-color="grey-lighten-3" class="flex-grow-1 mr-2" />
                    <v-icon size="x-small" color="warning">mdi-star</v-icon>
                  </div>
                </div>
              </div>
            </v-card>
            <PaginationLinks
              v-if="reviewPagination.links"
              :links="reviewPagination.links"
              :current-page="reviewPagination.currentPage"
              :total-pages="reviewPagination.totalPages"
              :show-text="false"
              @navigate="handleReviewLinkNavigation"
              class="mt-4"
            />
          </v-col>

          <v-col cols="12" md="8" lg="9">
            <div v-if="canReviewProduct" class="mb-6">
              <ReviewForm
                :rating="reviewForm.rating"
                :description="reviewForm.description"
                :has-existing-review="hasUserReview"
                :is-submitting="isSubmittingReview"
                :can-submit="canSubmitReview"
                :title="reviewFormTitle"
                :subtitle="reviewFormSubtitle"
                @update:rating="reviewForm.rating = $event"
                @update:description="reviewForm.description = $event"
                @submit="handleSubmitReview"
                @delete="handleDeleteReview"
              />
            </div>

            <div v-if="isLoadingReviews" class="reviews-list">
              <v-skeleton-loader v-for="n in 3" :key="`review-skeleton-${n}`" :elevation="1" type="paragraph" class="mb-3 rounded-lg" />
            </div>

            <div v-else-if="reviewItems.length" class="reviews-list">
              <v-card v-for="review in reviewItems" :key="review.id" class="mb-3 rounded-lg elevation-1">
                <v-card-text>
                  <div class="d-flex align-center justify-space-between mb-2">
                    <div class="d-flex align-center">
                      <v-rating :model-value="review.rating" color="warning" density="compact" half-increments readonly size="small" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mr-2" />
                      <span class="text-caption text-medium-emphasis"> {{ review.rating.toFixed(1) }} / 5 </span>
                    </div>
                    <div class="d-flex align-center text-caption text-medium-emphasis">
                      <v-icon size="small" color="primary" class="mr-1">mdi-calendar</v-icon>
                      <span>{{ formatDate(review.createdAt) || t('product_detail.unknown_date') }}</span>
                    </div>
                  </div>
                  <div v-if="review.reviewer" class="mb-2">
                    <div class="d-flex align-center mb-1">
                      <v-icon size="small" color="primary" class="mr-1">mdi-account</v-icon>
                      <span class="text-body-2 font-weight-medium"> {{ review.reviewer.firstName }} {{ review.reviewer.lastName }} </span>
                    </div>
                  </div>
                  <p class="text-body-2 text-medium-emphasis mb-0">
                    {{ review.description || t('product_detail.no_comment') }}
                  </p>
                </v-card-text>
              </v-card>
            </div>
            <div v-else class="text-medium-emphasis text-center py-8">{{ t('product_detail.no_reviews') }}</div>
          </v-col>
        </v-row>
      </section>

      <v-divider class="my-8"></v-divider>

      <section v-if="similarProducts.length" class="mt-10 section-block">
        <router-link :to="similarProductsLink" class="d-flex align-center mb-4 text-decoration-none text-primary pools-header-link">
          <h2 class="text-h4 font-weight-bold text-primary mb-0">
            {{ t('product_detail.similar_products') }}
          </h2>
          <v-icon color="primary" class="ml-2">mdi-chevron-right</v-icon>
        </router-link>
        <v-divider class="mb-4" />
        <div class="horizontal-scroll-pools">
          <div v-for="item in similarProducts" :key="item.product.id" class="pool-card-scroll">
            <ProductCard :product="item.product" :company="item.company" :category="item.category" />
          </div>
        </div>
      </section>

      <section v-if="otherPools.length" class="mt-10 section-block">
        <router-link :to="otherPoolsLink" class="d-flex align-center mb-4 text-decoration-none text-primary pools-header-link">
          <h2 class="text-h4 font-weight-bold text-primary mb-0">
            {{ t('product_detail.other_pools') }}
          </h2>
          <v-icon color="primary" class="ml-2">mdi-chevron-right</v-icon>
        </router-link>
        <v-divider class="mb-4" />
        <div class="horizontal-scroll-pools">
          <div v-for="pool in otherPools" :key="pool.pool.id" class="pool-card-scroll">
            <PoolCard :enriched-pool="pool" />
          </div>
        </div>
      </section>
    </div>

    <v-dialog v-model="showDeleteReviewDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-trash-can</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ t('product_detail.review_form.delete_review') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ t('product_detail.review_form.delete_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showDeleteReviewDialog = false" :disabled="isDeletingReview">
            <template #prepend>
              <v-icon>mdi-close</v-icon>
            </template>
            {{ t('pool_detail.cancel') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="confirmDeleteReview" :loading="isDeletingReview">
            <template #prepend v-if="!isDeletingReview">
              <v-icon>mdi-trash-can</v-icon>
            </template>
            {{ t('pool_detail.delete') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useI18n } from 'vue-i18n';
import type { EnrichedProduct, Product } from '@/models/Product';
import type { EnrichedPool, Pool } from '@/models';
import type { Company, Category } from '@/models';
import ProductCard from '@/components/ProductCard.vue';
import PoolCard from '@/components/PoolCard.vue';
import { productService, poolService, requestService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';
import { useLocationsStore } from '@/stores/locations';
import { useProductsStore } from '@/stores/products';
import ReviewForm from '@/components/ReviewForm.vue';
import { useProductReviews } from '@/composables/useProductReviews';
import { useAuthStore } from '@/stores/auth';
import { RequestStatus } from '@/models/Request';
import { PoolStatus } from '@/models/Pool';
import { formatCurrency } from '@/utils/currency';

const route = useRoute();
const router = useRouter();
const companiesStore = useCompaniesStore();
const categoriesStore = useCategoriesStore();
const locationsStore = useLocationsStore();
const productsStore = useProductsStore();
const authStore = useAuthStore();
const { t } = useI18n();

const product = ref<EnrichedProduct | null>(null);
const isLoading = ref(true);
const activePools = ref<EnrichedPool[]>([]);
const otherPools = ref<EnrichedPool[]>([]);
const similarProducts = ref<{ product: Product; company?: Company; category?: Category }[]>([]);

const showDeleteReviewDialog = ref(false);
const isDeletingReview = ref(false);

const {
  form: reviewForm,
  items: reviewItems,
  isLoading: isLoadingReviews,
  isSubmitting: isSubmittingReview,
  hasUserReview,
  canSubmit: canSubmitReview,
  pagination: reviewPagination,
  submitReview,
  loadReviews: loadProductReviews,
  deleteReview,
  setCurrentUserId,
  setProductId,
  handlePageChange: handleReviewPageChange,
  handleLinkNavigation: handleReviewLinkNavigation,
} = useProductReviews(null);

const rawProductId = Number((route.params as { id?: string }).id);
const normalizedProductId = Number.isNaN(rawProductId) ? null : rawProductId;

if (normalizedProductId !== null) {
  setProductId(normalizedProductId);
}

const productImage = computed(() => {
  if (product.value?.imageUri) {
    return product.value.imageUri;
  }
  return '';
});

const activePoolsCount = computed(() => activePools.value.length);
const totalPoolsCount = computed(() => activePools.value.length + otherPools.value.length);
const finishedPoolsCount = computed(() => [...activePools.value, ...otherPools.value].filter((p) => getStatusName(p.pool.status) === 'FINISHED').length);

const hasEligibleDeliveredRequest = ref(false);
const isLoadingEligibilityCheck = ref(false);

const checkEligibilityForReview = async () => {
  if (!authStore.isAuthenticated || !authStore.isUser || !authStore.accountValidated || !product.value || !authStore.userId) {
    hasEligibleDeliveredRequest.value = false;
    return;
  }

  isLoadingEligibilityCheck.value = true;
  try {
    const response = await requestService.list({
      user_id: authStore.userId,
      pool_status: PoolStatus.FINISHED,
      status: RequestStatus.DELIVERED,
      product_id: product.value.id,
    });

    hasEligibleDeliveredRequest.value = response.totalCount > 0 || (response.data && response.data.length > 0);
  } catch (error) {
    console.error('Failed to check review eligibility', error);
    hasEligibleDeliveredRequest.value = false;
  } finally {
    isLoadingEligibilityCheck.value = false;
  }
};

const canReviewProduct = computed(() => {
  if (!authStore.isAuthenticated || !authStore.isUser) {
    return false;
  }

  return hasEligibleDeliveredRequest.value;
});
const reviewFormTitle = computed(() => (hasUserReview.value ? t('product_detail.review_form.title_edit') : t('product_detail.review_form.title_create')));
const reviewFormSubtitle = computed(() => (hasUserReview.value ? t('product_detail.review_form.subtitle_edit') : t('product_detail.review_form.subtitle_create')));

const reviewCount = computed(() => reviewItems.value.length);

const aggregatedDistribution = computed(() => {
  if (!product.value || !product.value.ratings) {
    return {
      total: 0,
      counts: { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 } as Record<number, number>,
    };
  }
  const { oneStar = 0, twoStars = 0, threeStars = 0, fourStars = 0, fiveStars = 0 } = product.value.ratings;
  return {
    total: oneStar + twoStars + threeStars + fourStars + fiveStars,
    counts: {
      1: oneStar,
      2: twoStars,
      3: threeStars,
      4: fourStars,
      5: fiveStars,
    } as Record<number, number>,
  };
});

const averageRating = computed(() => {
  const aggregatedTotal = aggregatedDistribution.value.total;
  if (aggregatedTotal > 0 && typeof product.value?.rating === 'number') {
    return product.value.rating;
  }
  if (reviewCount.value > 0) {
    const total = reviewItems.value.reduce((acc, entry) => acc + entry.rating, 0);
    return total / reviewCount.value;
  }
  return 0;
});

const totalRatings = computed(() => {
  const aggregatedTotal = aggregatedDistribution.value.total;
  if (aggregatedTotal > 0) {
    return aggregatedTotal;
  }
  return reviewCount.value;
});

const ratingPercents = computed<Record<number, number>>(() => {
  const { total, counts } = aggregatedDistribution.value;
  if (total > 0) {
    return {
      5: (counts[5] * 100) / total,
      4: (counts[4] * 100) / total,
      3: (counts[3] * 100) / total,
      2: (counts[2] * 100) / total,
      1: (counts[1] * 100) / total,
    };
  }

  if (reviewCount.value > 0) {
    const buckets: Record<number, number> = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
    for (const entry of reviewItems.value) {
      const normalized = Number.isFinite(entry.rating) ? entry.rating : 0;
      const rounded = Math.min(5, Math.max(1, Math.round(normalized)));
      buckets[rounded] += 1;
    }
    return {
      5: (buckets[5] * 100) / reviewCount.value,
      4: (buckets[4] * 100) / reviewCount.value,
      3: (buckets[3] * 100) / reviewCount.value,
      2: (buckets[2] * 100) / reviewCount.value,
      1: (buckets[1] * 100) / reviewCount.value,
    };
  }

  return { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 };
});

const ratingLabel = computed(() => {
  const aggregatedTotal = aggregatedDistribution.value.total;
  if (aggregatedTotal > 0 && typeof product.value?.rating === 'number' && product.value.rating > 0) {
    return `${product.value.rating.toFixed(1)} / 5`;
  }
  if (reviewCount.value > 0) {
    return `${averageRating.value.toFixed(1)} / 5`;
  }
  return t('product_detail.no_reviews');
});

const loadProduct = async () => {
  const id = Number((route.params as { id?: string }).id);
  if (Number.isNaN(id)) {
    router.replace('/products');
    return;
  }
  try {
    isLoading.value = true;
    const fetched = await productService.getById(id);

    const company = fetched.companyUri ? await companiesStore.fetch(fetched.companyUri) : undefined;
    const category = fetched.categoryUri ? await categoriesStore.fetch(fetched.categoryUri) : undefined;

    product.value = {
      ...fetched,
      companyData: company,
      categoryData: category,
    };

    setProductId(fetched.id);

    await Promise.all([loadActivePools(fetched.id, fetched), loadSimilarProducts(category, company, fetched.id), loadOtherPools(category, fetched.id), loadProductReviews(), checkEligibilityForReview()]);
  } finally {
    isLoading.value = false;
  }
};

const goBack = () => {
  router.push('/products');
};

onMounted(() => {
  window.scrollTo({ top: 0, behavior: 'smooth' });
  loadProduct();
});

watch(
  () => (route.params as { id?: string }).id,
  (newId, oldId) => {
    if (newId !== oldId && newId) {
      window.scrollTo({ top: 0, behavior: 'smooth' });
      loadProduct();
    }
  },
);

const currentUserId = computed(() => authStore.currentUser?.id ?? authStore.userId ?? null);

watch(
  currentUserId,
  (id) => {
    setCurrentUserId(id ?? null);

    if (product.value) {
      checkEligibilityForReview();
    }
  },
  { immediate: true },
);

watch(
  () => product.value?.id,
  () => {
    if (product.value) {
      checkEligibilityForReview();
    }
  },
);

const refreshProductMetrics = async () => {
  if (!product.value) {
    return;
  }
  try {
    const updated = await productService.getById(product.value.id);
    product.value = {
      ...product.value,
      ...updated,
    };
  } catch (error) {
    console.error('Failed to refresh product metrics', error);
  }
};

const handleSubmitReview = async () => {
  await submitReview();
  await refreshProductMetrics();
};

const handleDeleteReview = () => {
  if (!hasUserReview.value) {
    return;
  }
  showDeleteReviewDialog.value = true;
};

const confirmDeleteReview = async () => {
  try {
    isDeletingReview.value = true;
    await deleteReview();
    await refreshProductMetrics();
    showDeleteReviewDialog.value = false;
  } catch (error) {
    console.error('Failed to delete review:', error);
  } finally {
    isDeletingReview.value = false;
  }
};

const idFromUri = (uri?: string | null): number | null => {
  if (!uri) return null;
  const match = uri.match(/\/(\d+)(\/)?$/);
  return match ? Number(match[1]) : null;
};

const statusMap: Record<number, string> = {
  0: 'AVAILABLE',
  1: 'DELIVERING',
  2: 'PAUSED',
  3: 'CANCELLED',
  4: 'FINISHED',
};

const getStatusName = (status: any): string => {
  if (typeof status === 'number') {
    return statusMap[status] || '';
  }
  return typeof status === 'string' ? status : status?.name || '';
};

const enrichPool = async (pool: Pool, productOverride?: Product): Promise<EnrichedPool | null> => {
  try {
    const poolProduct = productOverride ?? (await productsStore.fetch(pool.productUri));
    const location = await locationsStore.fetch(pool.locationUri);
    const company = poolProduct ? await companiesStore.fetch(poolProduct.companyUri) : null;
    const category = poolProduct ? await categoriesStore.fetch(poolProduct.categoryUri) : null;
    if (!poolProduct || !location || !company || !category) return null;
    return {
      pool,
      product: poolProduct,
      location,
      company,
      category,
    };
  } catch (e) {
    console.error('Failed to enrich pool', e);
    return null;
  }
};

const loadActivePools = async (productId: number, productData: Product) => {
  try {
    const response = await poolService.list({ product_id: productId, status: PoolStatus.AVAILABLE });
    const enriched = await Promise.all(response.data.map((p) => enrichPool(p, productData)));
    activePools.value = enriched.filter((p): p is EnrichedPool => Boolean(p));
  } catch (e) {
    console.error('Failed to load active pools', e);
  }
};

const loadOtherPools = async (category?: Category, productId?: number) => {
  try {
    const catId = idFromUri(category?.selfUri) ?? category?.id;
    const response = await poolService.list({ category_id: catId || undefined, status: PoolStatus.AVAILABLE });
    const pools = response.data.filter((p) => idFromUri(p.productUri) !== productId);
    const enriched = await Promise.all(pools.map((p) => enrichPool(p)));
    otherPools.value = enriched.filter((p): p is EnrichedPool => Boolean(p));
  } catch (e) {
    console.error('Failed to load other pools', e);
  }
};

const loadSimilarProducts = async (category?: Category, company?: Company, currentId?: number) => {
  try {
    const catId = category?.id ?? idFromUri(category?.selfUri);
    const companyId = company?.id ?? idFromUri(company?.selfUri);
    const params: Record<string, any> = { active: true };
    if (catId) params.category_id = catId;
    else if (companyId) params.company_id = companyId;
    const response = await productService.list(params);
    const items = response.data.filter((p) => p.id !== currentId).slice(0, 10);

    const enriched = await Promise.all(
      items.map(async (p) => {
        const comp = p.companyUri ? await companiesStore.fetch(p.companyUri) : undefined;
        const cat = p.categoryUri ? await categoriesStore.fetch(p.categoryUri) : undefined;
        return { product: { ...p, companyData: comp, categoryData: cat }, company: comp, category: cat };
      }),
    );
    similarProducts.value = enriched;
  } catch (e) {
    console.error('Failed to load similar products', e);
  }
};

const formatDate = (value?: string) => {
  if (!value) return '';
  const d = new Date(value);
  return isNaN(d.getTime()) ? '' : d.toLocaleDateString();
};

const activePoolsLink = computed(() => {
  if (!product.value?.id) return '/pools';
  return `/pools?productId=${product.value.id}`;
});

const similarProductsLink = computed(() => {
  if (!product.value) return '/products';
  const categoryId = product.value.categoryData?.id;
  const companyId = product.value.companyData?.id;
  if (categoryId) {
    return `/products?categoryId=${categoryId}`;
  }
  if (companyId) {
    return `/products?companyId=${companyId}`;
  }
  return '/products';
});

const otherPoolsLink = computed(() => {
  if (!product.value?.categoryData?.id) return '/pools';
  return `/pools?categoryId=${product.value.categoryData.id}`;
});
</script>

<style scoped>
.product-detail-page {
  background: rgb(var(--v-theme-background));
  min-height: 100vh;
}

.page-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.product-hero {
  border-radius: 18px !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.product-image {
  min-height: 0;
  max-height: 420px;
  object-fit: contain;
  background-color: rgba(var(--v-theme-primary), 0.03);
}

.image-col {
  min-height: 420px;
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.06), rgba(var(--v-theme-primary), 0.02));
}

.section-block {
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

.reviews-layout {
  margin-bottom: 0;
}

.reviews-summary-card {
  height: fit-content;
}

.reviews-list {
  max-height: 800px;
  overflow-y: auto;
  padding-right: 8px;
  scrollbar-width: thin;
  scrollbar-color: rgb(var(--v-theme-primary)) #eee;
}

.reviews-list::-webkit-scrollbar {
  width: 8px;
}

.reviews-list::-webkit-scrollbar-thumb {
  background: rgb(var(--v-theme-primary));
  border-radius: 4px;
}

.category-chip,
.company-chip,
.deleted-chip {
  cursor: pointer !important;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  font-weight: 600 !important;
  letter-spacing: 0.5px;
  padding: 20px 16px !important;
  border-radius: 12px !important;
}

.deleted-chip {
  cursor: default !important;
}

.category-chip:hover,
.company-chip:hover {
  transform: translateY(-2px);
}

.category-chip:hover {
  box-shadow: 0 6px 20px rgba(var(--v-theme-primary), 0.4) !important;
}

.company-chip:hover {
  box-shadow: 0 6px 20px rgba(var(--v-theme-secondary), 0.4) !important;
}

.category-chip:active,
.company-chip:active {
  transform: translateY(0px);
}

.category-chip:active {
  box-shadow: 0 2px 8px rgba(var(--v-theme-primary), 0.3) !important;
}

.company-chip:active {
  box-shadow: 0 2px 8px rgba(var(--v-theme-secondary), 0.3) !important;
}

.category-chip :deep(.v-chip__prepend),
.company-chip :deep(.v-chip__prepend) {
  margin-inline-end: 8px;
}

.category-chip :deep(.v-icon),
.company-chip :deep(.v-icon) {
  transition: transform 0.3s ease;
}

.category-chip:hover :deep(.v-icon),
.company-chip:hover :deep(.v-icon) {
  transform: scale(1.1);
}

.v-theme--dark .category-chip:hover {
  box-shadow: 0 6px 20px rgba(var(--v-theme-primary), 0.6) !important;
}

.v-theme--dark .company-chip:hover {
  box-shadow: 0 6px 20px rgba(var(--v-theme-secondary), 0.6) !important;
}

.join-dialog-card {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
}

.join-dialog-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.join-dialog-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.1) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
}

.header-section {
  position: relative;
  z-index: 2;
}

.v-theme--dark .join-dialog-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.join-dialog-card .v-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
