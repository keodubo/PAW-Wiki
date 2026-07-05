<template>
  <v-container class="py-8 company-detail-page" fluid>
    <div class="page-wrap">
      <v-card class="company-hero elevation-3 rounded-xl mb-8">
        <v-row class="ma-0" no-gutters align="start">
          <v-col cols="12" md="5" class="image-col d-flex align-center justify-center pa-8">
            <v-skeleton-loader v-if="isLoading" type="image" class="h-100 w-100" />
            <v-img v-else-if="companyImage" :src="companyImage" :alt="company?.name" max-height="400" max-width="400" contain class="company-image rounded-xl">
              <template #placeholder>
                <div class="d-flex align-center justify-center fill-height">
                  <v-progress-circular indeterminate color="primary"></v-progress-circular>
                </div>
              </template>
              <template #error>
                <v-img src="@/assets/empty.svg" alt="No image" max-height="400" max-width="400" contain class="empty-image" />
              </template>
            </v-img>
            <v-img v-else src="@/assets/empty.svg" alt="No image" max-height="400" max-width="400" contain class="empty-image rounded-xl" />
          </v-col>

          <v-col cols="12" md="7" class="pa-8 d-flex flex-column">
            <div v-if="isLoading">
              <v-skeleton-loader type="heading, text@4, actions"></v-skeleton-loader>
            </div>
            <div v-else-if="company">
              <div class="d-flex align-center justify-space-between mb-4 flex-wrap">
                <h1 class="text-h3 font-weight-bold">{{ company.name }}</h1>
                <v-chip :color="company.validated ? 'success' : 'warning'" variant="flat" size="large" :prepend-icon="company.validated ? 'mdi-check-circle' : 'mdi-clock-alert'" class="status-chip">
                  {{ company.validated ? $t('company_detail.verified') : $t('company_detail.pending_verification') }}
                </v-chip>
              </div>

              <v-card class="info-card elevation-1 rounded-lg mb-4" variant="outlined">
                <v-card-text class="pa-4">
                  <div class="d-flex flex-column ga-3">
                    <div v-if="company.address" class="d-flex align-center">
                      <v-icon color="primary" class="mr-3">mdi-map-marker</v-icon>
                      <span class="text-body-1">{{ company.address }}</span>
                    </div>

                    <div v-if="company.email" class="d-flex align-center">
                      <v-icon color="primary" class="mr-3">mdi-email</v-icon>
                      <a :href="`mailto:${company.email}`" class="text-body-1 text-primary text-decoration-none">
                        {{ company.email }}
                      </a>
                    </div>

                    <div v-if="company.phone" class="d-flex align-center">
                      <v-icon color="primary" class="mr-3">mdi-phone</v-icon>
                      <a :href="`tel:${company.phone}`" class="text-body-1 text-primary text-decoration-none">
                        {{ company.phone }}
                      </a>
                    </div>

                    <div v-if="company.cbu" class="d-flex align-center">
                      <v-icon color="primary" class="mr-3">mdi-bank</v-icon>
                      <span class="text-body-1 font-weight-medium">{{ company.cbu }}</span>
                    </div>
                  </div>
                </v-card-text>
              </v-card>

              <v-card class="stats-card elevation-1 rounded-lg mt-4" variant="outlined">
                <v-card-text class="pa-4">
                  <div class="d-flex flex-column ga-3">
                    <div class="d-flex align-center justify-space-between">
                      <div class="d-flex align-center">
                        <v-icon color="primary" size="small" class="mr-2">mdi-package-variant</v-icon>
                        <span class="text-body-1">{{ $t('company_detail.total_products') }}:</span>
                      </div>
                      <span class="text-body-1 font-weight-bold text-primary">{{ totalProductsCount }}</span>
                    </div>

                    <div v-if="averageRating > 0" class="d-flex align-center justify-space-between">
                      <div class="d-flex align-center">
                        <v-icon color="warning" size="small" class="mr-2">mdi-star</v-icon>
                        <span class="text-body-1">{{ $t('company_detail.average_rating') }}:</span>
                      </div>
                      <div class="d-flex align-center">
                        <v-rating :model-value="averageRating" color="warning" density="compact" half-increments readonly size="small" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mr-2" />
                        <span class="text-body-1 font-weight-bold">{{ averageRating.toFixed(1) }}</span>
                      </div>
                    </div>

                    <div v-if="companyCategories.length > 0">
                      <div class="d-flex align-center mb-2">
                        <v-icon color="primary" size="small" class="mr-2">mdi-tag-multiple</v-icon>
                        <span class="text-body-1">{{ $t('company_detail.categories') }}:</span>
                      </div>
                      <div class="d-flex flex-wrap ga-2">
                        <v-chip v-for="category in companyCategories" :key="category.id" size="small" color="primary" variant="tonal" prepend-icon="mdi-tag">
                          {{ $t(`category.${category.name}`) }}
                        </v-chip>
                      </div>
                    </div>
                  </div>
                </v-card-text>
              </v-card>
            </div>
            <div v-else class="text-center py-8 text-medium-emphasis">
              {{ $t('company_detail.company_not_found') }}
            </div>
          </v-col>
        </v-row>
      </v-card>

      <section v-if="products.length > 0" class="mt-8 section-block">
        <div class="section-header mb-4">
          <h2 class="text-h5 font-weight-bold section-title">
            <v-icon class="mr-2" color="primary">mdi-star</v-icon>
            {{ $t('company_detail.popular_products') }}
          </h2>
          <p class="text-body-2 text-medium-emphasis mt-1">
            {{ $t('company_detail.products_count', { count: totalProductsCount }) }}
          </p>
        </div>

        <v-row>
          <template v-if="isLoadingProducts">
            <v-col v-for="n in 6" :key="`skeleton-${n}`" cols="12" sm="6" md="4" lg="3">
              <v-skeleton-loader type="card" />
            </v-col>
          </template>
          <template v-else>
            <v-col v-for="item in products" :key="item.product.id" cols="12" sm="6" md="4" lg="3">
              <ProductCard :product="item.product" :company="item.company" :category="item.category" />
            </v-col>
          </template>
        </v-row>

        <div v-if="totalProductsCount > products.length" class="d-flex justify-center mt-6">
          <v-btn color="primary" variant="outlined" size="large" @click="viewAllProducts" prepend-icon="mdi-arrow-right">
            {{ $t('company_detail.view_all_products') }}
          </v-btn>
        </div>
      </section>

      <section v-else-if="!isLoadingProducts" class="mt-8 section-block">
        <v-card class="elevation-1 rounded-xl">
          <v-card-text class="text-center py-12">
            <v-icon size="64" color="grey-lighten-1" class="mb-4">mdi-package-variant</v-icon>
            <h3 class="text-h6 text-medium-emphasis mb-2">
              {{ $t('company_detail.no_products') }}
            </h3>
            <p class="text-body-2 text-medium-emphasis">
              {{ $t('company_detail.no_products_description') }}
            </p>
          </v-card-text>
        </v-card>
      </section>
    </div>
  </v-container>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import type { Company } from '@/models/Company';
import type { Product } from '@/models/Product';
import type { Category } from '@/models';
import ProductCard from '@/components/ProductCard.vue';
import { companyService, productService } from '@/services';
import { useCompaniesStore } from '@/stores/companies';
import { useCategoriesStore } from '@/stores/categories';

const route = useRoute();
const router = useRouter();
const companiesStore = useCompaniesStore();
const categoriesStore = useCategoriesStore();

const company = ref<Company | null>(null);
const isLoading = ref(true);
const isLoadingProducts = ref(true);
const products = ref<{ product: Product; company?: Company; category?: Category }[]>([]);
const totalProductsCount = ref(0);
const allProducts = ref<Product[]>([]);

definePage({
  meta: {
    requiresAuth: false,
  },
});

const companyImage = computed(() => {
  return company.value?.imageUri || null;
});

const loadCompany = async () => {
  const id = Number((route.params as { id: string }).id);
  if (Number.isNaN(id)) {
    router.replace('/products');
    return;
  }
  try {
    isLoading.value = true;
    const fetched = await companyService.getById(id);
    company.value = fetched;

    await loadProducts(id);
  } catch (error) {
    console.error('Failed to load company:', error);
    company.value = null;
  } finally {
    isLoading.value = false;
  }
};

const loadProducts = async (companyId: number) => {
  try {
    isLoadingProducts.value = true;
    const response = await productService.list({ company_id: companyId, order_by: 'rating', desc: true, active: true });
    totalProductsCount.value = response.totalCount;
    allProducts.value = response.data;

    const sortedProducts = [...response.data].sort((a, b) => {
      const ratingA = a.rating || 0;
      const ratingB = b.rating || 0;
      return ratingB - ratingA;
    });

    const popularProducts = sortedProducts.slice(0, 12);

    const enriched = await Promise.all(
      popularProducts.map(async (p) => {
        const comp = p.companyUri ? await companiesStore.fetch(p.companyUri) : undefined;
        const cat = p.categoryUri ? await categoriesStore.fetch(p.categoryUri) : undefined;
        return {
          product: p,
          company: comp,
          category: cat,
        };
      }),
    );

    products.value = enriched;
  } catch (error) {
    console.error('Failed to load products:', error);
    products.value = [];
    allProducts.value = [];
  } finally {
    isLoadingProducts.value = false;
  }
};

const averageRating = computed(() => {
  if (allProducts.value.length === 0) return 0;
  const totalRating = allProducts.value.reduce((sum, p) => sum + (p.rating || 0), 0);
  return totalRating / allProducts.value.length;
});

const companyCategories = computed(() => {
  const categoryMap = new Map<number, Category>();

  products.value.forEach((item) => {
    if (item.category && item.category.id) {
      categoryMap.set(item.category.id, item.category);
    }
  });

  return Array.from(categoryMap.values());
});

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/products');
  }
};

const viewAllProducts = () => {
  if (company.value?.id) {
    router.push(`/products?company_id=${company.value.id}`);
  }
};

onMounted(() => {
  loadCompany();
});
</script>

<style scoped>
.company-detail-page {
  background: rgb(var(--v-theme-background));
  min-height: 100vh;
}

.page-wrap {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
}

.company-hero {
  border-radius: 18px !important;
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  overflow: hidden;
}

.company-image {
  min-height: 300px;
  max-height: 400px;
  object-fit: contain;
  background-color: rgba(var(--v-theme-primary), 0.03);
  border: 2px solid rgba(var(--v-theme-primary), 0.1);
}

.empty-image {
  min-height: 300px;
  max-height: 400px;
  object-fit: contain;
  background-color: rgba(var(--v-theme-primary), 0.03);
  border: 2px solid rgba(var(--v-theme-primary), 0.1);
  opacity: 0.6;
}

.stats-card {
  background: rgba(var(--v-theme-surface), 0.8);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.stats-card:hover {
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.1) !important;
}

.stats-card :deep(.v-card-text) {
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.02), rgba(var(--v-theme-primary), 0.05));
}

.image-col {
  min-height: 400px;
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.06), rgba(var(--v-theme-primary), 0.02));
}

.info-card {
  background: rgba(var(--v-theme-surface), 0.8);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.info-card:hover {
  box-shadow: 0 4px 12px rgba(var(--v-theme-primary), 0.1) !important;
}

.info-card :deep(.v-card-text) {
  background: linear-gradient(135deg, rgba(var(--v-theme-primary), 0.02), rgba(var(--v-theme-primary), 0.05));
}

.status-chip {
  font-weight: 600;
  letter-spacing: 0.5px;
}

.section-block {
  padding-bottom: 8px;
}

.section-header {
  padding-bottom: 16px;
}

.section-title {
  font-weight: 600;
  color: rgba(var(--v-theme-on-background), 0.9);
  display: flex;
  align-items: center;
}

a.text-primary:hover {
  text-decoration: underline !important;
  opacity: 0.8;
}

@media (max-width: 960px) {
  .company-hero .v-row {
    flex-direction: column-reverse;
  }

  .image-col {
    min-height: 250px;
  }

  .company-image {
    max-height: 300px;
  }
}
</style>
