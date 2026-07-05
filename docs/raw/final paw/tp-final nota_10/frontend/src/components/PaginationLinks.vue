<template>
  <div v-if="hasAnyLink" class="pagination-links">
    <div class="d-flex align-center justify-center ga-2">
      <v-btn :disabled="!links?.prev" @click="navigateToLink(links?.first)" color="primary" variant="elevated" size="large" class="pagination-btn" :title="$t('pagination.first')">
        <v-icon>mdi-page-first</v-icon>
        <span v-if="showText" class="ml-2 d-none d-sm-inline">{{ $t('pagination.first') }}</span>
      </v-btn>

      <v-btn :disabled="!links?.prev" @click="navigateToLink(links?.prev)" color="primary" variant="elevated" size="large" class="pagination-btn" :title="$t('pagination.previous')">
        <v-icon>mdi-chevron-left</v-icon>
        <span v-if="showText" class="ml-2 d-none d-sm-inline">{{ $t('pagination.previous') }}</span>
      </v-btn>

      <div v-if="showText" class="pagination-info px-4">
        <span class="text-body-1 font-weight-medium">
          {{ currentPageDisplay }}
        </span>
      </div>

      <v-btn :disabled="!links?.next" @click="navigateToLink(links?.next)" color="primary" variant="elevated" size="large" class="pagination-btn" :title="$t('pagination.next')">
        <span v-if="showText" class="mr-2 d-none d-sm-inline">{{ $t('pagination.next') }}</span>
        <v-icon>mdi-chevron-right</v-icon>
      </v-btn>

      <v-btn :disabled="!links?.next" @click="navigateToLink(links?.last)" color="primary" variant="elevated" size="large" class="pagination-btn" :title="$t('pagination.last')">
        <span v-if="showText" class="mr-2 d-none d-sm-inline">{{ $t('pagination.last') }}</span>
        <v-icon>mdi-page-last</v-icon>
      </v-btn>
    </div>
    <div v-if="!showText && currentPageDisplay" class="pagination-info-below">
      <span class="text-body-1 font-weight-medium">
        {{ currentPageDisplay }}
      </span>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { PaginationLinks } from '@/models/Http';

interface Props {
  links?: PaginationLinks;
  currentPage?: number;
  totalPages?: number;
  showText?: boolean;
}

const props = withDefaults(defineProps<Props>(), {
  showText: true,
});
const emit = defineEmits<{
  navigate: [url: string];
}>();

const { t } = useI18n();

const hasAnyLink = computed(() => {
  return !!(props.links?.first || props.links?.last || props.links?.next || props.links?.prev);
});

const currentPageDisplay = computed(() => {
  if (props.currentPage && props.totalPages) {
    return t('pagination.current_page', { current: props.currentPage, total: props.totalPages });
  }
  return '';
});

const navigateToLink = (url?: string) => {
  if (url) {
    emit('navigate', url);
  }
};
</script>

<style scoped>
.pagination-links {
  padding: 1rem 0;
}

.pagination-btn {
  min-width: 44px;
  text-transform: none;
  letter-spacing: normal;
}

.pagination-btn:disabled {
  opacity: 0.4;
}

.pagination-info {
  min-width: 100px;
  text-align: center;
}

.pagination-info-below {
  text-align: center;
  padding-top: 0.5rem;
}

@media (max-width: 600px) {
  .pagination-btn {
    min-width: 40px;
    padding: 0 8px;
  }

  .pagination-info {
    min-width: 80px;
    padding: 0 8px;
  }
}
</style>
