<template>
  <div class="categories-horizontal-scroll mb-6">
    <div :class="['categories-container', { centered: shouldCenterCategories }]">
      <template v-if="props.isLoadingCategories">
        <v-card v-for="n in 6" :key="`cat-skeleton-${n}`" class="category-card text-center pa-4 elevation-2 rounded-xl flex-shrink-0" width="140" height="140">
          <div class="d-flex flex-column align-center h-100 justify-center">
            <div class="icon-container mb-2">
              <v-skeleton-loader type="avatar" width="56" height="56" class="skeleton-avatar" />
            </div>
            <v-skeleton-loader type="text" width="80" height="20" />
          </div>
        </v-card>
      </template>
      <template v-else>
        <v-card
          v-for="category in filteredCategories"
          :key="category.id"
          @click="navigateToCategory(category.id)"
          class="category-card text-center pa-4 elevation-2 rounded-xl flex-shrink-0"
          :class="{ selected: props.selectedCategoryId === category.id }"
          width="140"
          height="140"
          hover
        >
          <div class="d-flex flex-column align-center h-100 justify-center">
            <div class="icon-container mb-2">
              <v-icon size="56" color="primary">
                {{ 'mdi-' + category.iconName }}
              </v-icon>
            </div>
            <div class="text-body-2 font-weight-medium text-center">
              {{ $t(`category.${category.name}`) }}
            </div>
          </div>
        </v-card>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import type { Category } from '@/models';

const props = defineProps<{
  categories: Category[];
  selectedCategoryId?: number;
  isLoadingCategories?: boolean;
}>();

const router = useRouter();

const filteredCategories = computed(() => {
  return (props.categories || []).filter((category) => category.name !== 'Otros');
});

const shouldCenterCategories = computed(() => filteredCategories.value.length <= 6);

const navigateToCategory = (categoryId: number) => {
  router.push({
    path: '/pools',
    query: { categoryId },
  });
};
</script>

<style scoped>
.skeleton-avatar {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.skeleton-avatar :deep(.v-skeleton-loader__avatar) {
  margin: 0 !important;
}

.categories-horizontal-scroll {
  width: 100%;
  overflow: visible;
  z-index: 1;
  position: relative;
  margin-bottom: 2rem;
}

.categories-container {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  overflow-y: hidden;
  padding: 16px 40px 32px 40px;
  scrollbar-width: thin;
  justify-content: flex-start;
  align-items: center;
}

.categories-container.centered {
  justify-content: center;
}

.categories-container::-webkit-scrollbar {
  height: 8px;
}

.categories-container::-webkit-scrollbar-track {
  background: rgba(var(--v-theme-on-surface), 0.1);
  border-radius: 4px;
}

.categories-container::-webkit-scrollbar-thumb {
  background: rgba(var(--v-theme-primary), 0.5);
  border-radius: 4px;
}

.categories-container::-webkit-scrollbar-thumb:hover {
  background: rgba(var(--v-theme-primary), 0.7);
}

.category-card {
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: rgb(var(--v-theme-surface));
  color: rgb(var(--v-theme-on-surface));
  border-radius: 16px !important;
  min-width: 140px;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.category-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
  z-index: 10;
  position: relative;
}

.icon-container {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 100%;
  height: 56px;
}

.icon-container .v-icon {
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.category-card.selected {
  background-color: rgba(var(--v-theme-primary), 0.1);
  border: 2px solid rgb(var(--v-theme-primary));
}
</style>
