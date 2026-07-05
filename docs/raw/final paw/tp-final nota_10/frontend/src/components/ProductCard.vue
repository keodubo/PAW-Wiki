<template>
  <v-card class="product-card elevation-2 h-100" :class="{ 'product-card--deleted': !product.active }" rounded="xl" hover @click="goToProduct">
    <v-card-title class="px-4 py-3 company-header" :class="{ 'company-header--deleted': !product.active }">
      <div class="d-flex align-center justify-space-between text-white">
        <div class="d-flex align-center">
          <v-icon class="mr-2" color="white">mdi-domain</v-icon>
          <span class="text-body-1 font-weight-medium">{{ company ? company.name : $t('product_detail.unknown_company') }}</span>
        </div>

        <div v-if="myProduct" class="action-buttons" @click.stop>
          <v-btn v-if="product.active" icon size="small" variant="text" color="white" @click="handleEdit" :title="$t('edit_product')">
            <v-icon size="small">mdi-pencil</v-icon>
          </v-btn>
          <v-btn v-if="product.active && product.canRetire" icon size="small" variant="text" color="white" @click="handleDelete" :title="$t('delete_product')">
            <v-icon size="small">mdi-archive-arrow-down</v-icon>
          </v-btn>
        </div>
      </div>
    </v-card-title>

    <v-card-text class="pa-4">
      <v-row align="center">
        <v-col cols="8">
          <h5 class="text-h6 font-weight-bold text-primary mb-2" :title="product.name">
            {{ truncateText(product.name, 40) }}
          </h5>

          <div class="d-flex align-center mb-2">
            <v-chip size="small" color="primary" variant="tonal" prepend-icon="mdi-tag">
              {{ category ? $t(`category.${category.name}`) : $t('product_detail.uncategorized') }}
            </v-chip>
          </div>

          <div class="text-h6 font-weight-bold text-success mb-3">
            {{ formatCurrency(product.price) }}
          </div>

          <p class="text-body-2 text-medium-emphasis mb-3" :title="product.description">
            {{ truncateText(product.description, 80) }}
          </p>

          <div v-if="product.rating && product.rating > 0" class="d-flex align-center">
            <v-rating :model-value="product.rating" color="warning" density="compact" half-increments readonly size="small" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mr-2" />
            <span class="text-caption text-medium-emphasis"> ({{ product.rating.toFixed(1) }}) </span>
          </div>
          <div v-else class="text-caption text-medium-emphasis">{{ $t('product_detail.no_reviews') }}</div>
        </v-col>

        <v-col cols="4" class="text-center">
          <v-img :src="product.imageUri" :alt="product.name" height="120" class="rounded" cover />
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router';
import type { Product } from '@/models/Product';
import type { Category, Company } from '@/models';
import { formatCurrency } from '@/utils/currency';

const props = defineProps<{
  product: Product;
  company: Company | undefined;
  category: Category | undefined;
  myProduct?: boolean;
}>();

const emit = defineEmits<{
  delete: [productId: number];
  edit: [productId: number];
}>();

const router = useRouter();

const truncateText = (text: string, maxLength: number): string => {
  if (text.length <= maxLength) return text;
  return text.substring(0, maxLength) + '...';
};

const goToProduct = () => {
  router.push(`/products/${props.product.id}`);
};

const handleEdit = () => {
  router.push(`/products/${props.product.id}/edit`);
};

const handleDelete = () => {
  emit('delete', props.product.id);
};
</script>

<style scoped>
.product-card {
  transition: all 0.3s ease;
  border-radius: 12px !important;
  background-color: rgb(var(--v-theme-surface));
  color: rgb(var(--v-theme-on-surface));
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.15) !important;
}

.product-card--deleted {
  opacity: 0.85;
}

.product-card--deleted:hover {
  box-shadow: 0 12px 30px rgba(220, 38, 38, 0.2) !important;
}

.company-header {
  background: linear-gradient(135deg, #7f00ff 0%, #a855f7 100%);
}

.company-header--deleted {
  background: linear-gradient(135deg, #dc2626 0%, #ef4444 100%);
}

.action-buttons {
  display: flex;
  gap: 4px;
}

.action-buttons .v-btn {
  opacity: 0.9;
  transition: opacity 0.2s ease;
}

.action-buttons .v-btn:hover {
  opacity: 1;
  background-color: rgba(255, 255, 255, 0.1);
}

.v-rating :deep(.v-icon) {
  color: rgb(var(--v-theme-warning)) !important;
  opacity: 1 !important;
}

.v-rating :deep(.v-icon.v-icon--disabled) {
  color: rgba(var(--v-theme-on-surface), 0.3) !important;
  opacity: 0.5 !important;
}

.v-rating :deep(.mdi-star) {
  color: rgb(var(--v-theme-warning)) !important;
}

.v-rating :deep(.mdi-star-outline) {
  color: rgba(var(--v-theme-on-surface), 0.3) !important;
}

.v-rating :deep(.mdi-star-half-full) {
  color: rgb(var(--v-theme-warning)) !important;
}
</style>
