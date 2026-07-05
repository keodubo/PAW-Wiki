<template>
  <v-dialog v-model="dialog" max-width="860px" scrollable>
    <template #activator="{ props }">
      <slot name="activator" :props="props" />
    </template>

    <v-card class="selector-dialog">
      <!-- Header -->
      <v-card-title class="dialog-header pa-4 d-flex align-center">
        <v-icon start size="22">mdi-office-building</v-icon>
        {{ $t('select_company') }}
        <v-spacer />
        <v-btn icon variant="text" size="small" @click="dialog = false" color="white">
          <v-icon>mdi-close</v-icon>
        </v-btn>
      </v-card-title>

      <!-- Search Bar -->
      <div class="pa-4 pb-2">
        <v-text-field
          v-model="searchQuery"
          :placeholder="$t('company_search_placeholder')"
          variant="solo"
          density="compact"
          prepend-inner-icon="mdi-magnify"
          color="primary"
          base-color="grey-lighten-1"
          class="search-input elevation-2 rounded-xl"
          hide-details
          clearable
          flat
          @update:model-value="debouncedSearch"
          @click:clear="onClearSearch"
        />
      </div>

      <!-- Count info -->
      <div v-if="!isLoading && totalCount > 0" class="px-4 pb-1">
        <span class="text-body-2 text-medium-emphasis">
          {{ $t('total') }}: {{ totalCount }} {{ $t('navbar.companies').toLowerCase() }}
        </span>
      </div>

      <v-divider />

      <v-card-text class="pa-4" style="min-height: 360px">
        <!-- Loading -->
        <div v-if="isLoading" class="d-flex justify-center align-center py-12">
          <v-progress-circular indeterminate color="primary" size="48" />
        </div>

        <!-- Empty -->
        <div v-else-if="companies.length === 0" class="text-center py-12">
          <v-icon size="56" color="grey-lighten-1">mdi-office-building-outline</v-icon>
          <p class="text-body-1 text-medium-emphasis mt-3">{{ $t('no_companies_found') }}</p>
        </div>

        <!-- Grid -->
        <v-row v-else>
          <v-col
            v-for="company in companies"
            :key="company.id"
            cols="12"
            sm="6"
          >
            <v-card
              class="company-item"
              :class="{ 'company-item--selected': selectedCompanyId === company.id }"
              variant="outlined"
              hover
              @click="selectCompany(company)"
            >
              <v-card-text class="pa-3 d-flex align-center ga-3">
                <v-avatar size="52" rounded="lg">
                  <v-img :src="company.imageUri" :alt="company.name">
                    <template #placeholder>
                      <v-img src="@/assets/empty.svg" :alt="company.name" />
                    </template>
                  </v-img>
                </v-avatar>
                <div class="flex-grow-1 min-width-0">
                  <div class="text-body-2 font-weight-semibold text-truncate">{{ company.name }}</div>
                  <div v-if="company.email" class="text-caption text-medium-emphasis text-truncate mt-1">
                    {{ company.email }}
                  </div>
                </div>
                <v-icon v-if="selectedCompanyId === company.id" color="primary" size="20">
                  mdi-check-circle
                </v-icon>
              </v-card-text>
            </v-card>
          </v-col>
        </v-row>
      </v-card-text>

      <v-divider v-if="links && hasAnyLink" />

      <!-- Pagination -->
      <div v-if="links && hasAnyLink" class="d-flex justify-center pa-3">
        <div class="d-flex align-center ga-2">
          <v-btn
            :disabled="!links.prev"
            @click="navigateToLink(links.first)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.first')"
          >
            <v-icon>mdi-page-first</v-icon>
          </v-btn>

          <v-btn
            :disabled="!links.prev"
            @click="navigateToLink(links.prev)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.previous')"
          >
            <v-icon>mdi-chevron-left</v-icon>
          </v-btn>

          <span class="text-body-2 font-weight-medium px-2">
            {{ $t('pagination.current_page', { current: currentPage, total: totalPages }) }}
          </span>

          <v-btn
            :disabled="!links.next"
            @click="navigateToLink(links.next)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.next')"
          >
            <v-icon>mdi-chevron-right</v-icon>
          </v-btn>

          <v-btn
            :disabled="!links.next"
            @click="navigateToLink(links.last)"
            color="primary"
            variant="elevated"
            size="small"
            class="pagination-btn"
            :title="$t('pagination.last')"
          >
            <v-icon>mdi-page-last</v-icon>
          </v-btn>
        </div>
      </div>

      <!-- Cancel action -->
      <v-card-actions class="pa-3 pt-0">
        <v-spacer />
        <v-btn variant="text" color="grey" @click="dialog = false" prepend-icon="mdi-close">
          {{ $t('cancel') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { companyService } from '@/services';
import type { Company } from '@/models';
import type { PaginationLinks } from '@/models/Http';
import { useNotifications } from '@/composables/useNotifications';

export interface CompanySelectorDialogProps {
  validatedOnly?: boolean;
}

const props = withDefaults(defineProps<CompanySelectorDialogProps>(), {
  validatedOnly: true,
});

const emit = defineEmits<{
  select: [company: Company];
}>();

const { handleApiError } = useNotifications();

const dialog = ref(false);
const isLoading = ref(false);
const searchQuery = ref('');
const selectedCompanyId = ref<number | null>(null);
const companies = ref<Company[]>([]);
const links = ref<PaginationLinks>({});
const currentPage = ref(1);
const totalPages = ref(1);
const totalCount = ref(0);

let searchTimeout: ReturnType<typeof setTimeout> | null = null;

const hasAnyLink = computed(() => !!(links.value.first || links.value.last || links.value.next || links.value.prev));

const debouncedSearch = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = setTimeout(() => {
    currentPage.value = 1;
    loadCompanies();
  }, 300);
};

const onClearSearch = () => {
  searchQuery.value = '';
  currentPage.value = 1;
  loadCompanies();
};

const loadCompanies = async (page = currentPage.value) => {
  try {
    isLoading.value = true;

    const params: any = { page: page - 1 };
    if (props.validatedOnly) params.validated = true;
    if (searchQuery.value) params.search = searchQuery.value;

    const response = await companyService.list(params);

    companies.value = response.data;
    totalCount.value = response.totalCount;
    links.value = response.links ?? {};

    // Derive total pages from last link URL
    if (response.links?.last) {
      const url = new URL(response.links.last);
      const lastPage = parseInt(url.searchParams.get('page') ?? '0', 10) + 1;
      totalPages.value = lastPage;
    } else {
      totalPages.value = page;
    }
    currentPage.value = page;
  } catch (error) {
    handleApiError(error);
    companies.value = [];
  } finally {
    isLoading.value = false;
  }
};

const navigateToLink = (url?: string) => {
  if (!url) return;
  const urlObj = new URL(url);
  const pageParam = urlObj.searchParams.get('page');
  if (pageParam !== null) {
    const page = parseInt(pageParam, 10) + 1;
    loadCompanies(page);
  }
};

const selectCompany = (company: Company) => {
  selectedCompanyId.value = company.id;
  emit('select', company);
  dialog.value = false;
};

watch(dialog, (open) => {
  if (open) {
    currentPage.value = 1;
    searchQuery.value = '';
    selectedCompanyId.value = null;
    loadCompanies();
  }
});
</script>

<style scoped>
.selector-dialog {
  border-radius: 16px !important;
  overflow: hidden;
}

.dialog-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  color: white;
}

.search-input :deep(.v-field) {
  background: rgb(var(--v-theme-surface)) !important;
  border: 1.5px solid rgba(var(--v-theme-primary), 0.2) !important;
  border-radius: 12px !important;
}

.search-input :deep(.v-field--focused) {
  border: 1.5px solid rgb(var(--v-theme-primary)) !important;
}

.search-input :deep(.v-field__outline) {
  display: none !important;
}

.search-input :deep(.v-field__prepend-inner .v-icon) {
  color: rgb(var(--v-theme-primary)) !important;
}

.company-item {
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 10px !important;
}

.company-item:hover {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.04) !important;
}

.company-item--selected {
  border-color: rgb(var(--v-theme-primary)) !important;
  background-color: rgba(var(--v-theme-primary), 0.06) !important;
}

.pagination-btn {
  min-width: 36px !important;
}

.pagination-btn:disabled {
  opacity: 0.4;
}

.min-width-0 {
  min-width: 0;
}
</style>
