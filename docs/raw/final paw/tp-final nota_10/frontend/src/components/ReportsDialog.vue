<template>
  <v-dialog v-model="dialog" max-width="900px">
    <v-card class="join-dialog-card elevation-12 rounded-xl">
      <v-card-title class="join-dialog-header pa-6 text-center">
        <div class="header-section">
          <v-icon size="56" color="white" class="mb-2">mdi-flag</v-icon>
          <h2 class="text-h5 font-weight-bold text-white mb-1">
            {{ $t('admin.user_reports') }}
          </h2>
        </div>
      </v-card-title>

      <v-card-text class="pa-6">
        <div v-if="isLoading" class="py-6">
          <v-skeleton-loader type="article, table" />
        </div>

        <div v-else-if="enrichedReports.length === 0" class="text-center py-12">
          <v-img src="@/assets/empty.svg" alt="No reports" max-width="250" class="mx-auto mb-4" />
          <h3 class="text-h6 text-medium-emphasis mb-2">
            {{ $t('admin.no_reports_for_user', [enrichedUser?.user.email || '']) }}
          </h3>
        </div>

        <div v-else>
          <h3 class="text-h6 text-center mb-4 font-weight-bold">
            {{ $t('admin.reports_for_user', [enrichedUser?.user.email || '']) }}
          </h3>

          <div class="d-flex justify-end mb-4">
            <v-menu>
              <template #activator="{ props: menuProps }">
                <v-btn v-bind="menuProps" variant="outlined" color="primary" size="small" class="text-none" prepend-icon="mdi-filter">
                  {{ $t('admin.order_by') }}: {{ $t(currentSortLabel) }}
                  <v-icon end>mdi-chevron-down</v-icon>
                </v-btn>
              </template>
              <v-list>
                <v-list-item v-for="option in sortOptions" :key="option.value" :class="{ 'bg-primary-lighten-4': currentSort === option.value }" @click="setSortOrder(option.orderBy, option.desc)">
                  <template #prepend>
                    <v-icon :icon="option.icon" size="small" class="mr-2" />
                  </template>
                  <v-list-item-title>{{ $t(option.label) }}</v-list-item-title>
                </v-list-item>
              </v-list>
            </v-menu>
          </div>

          <v-card class="elevation-2 rounded-lg">
            <v-card-text class="pa-0">
              <div class="table-responsive">
                <v-table>
                  <thead>
                    <tr>
                      <th class="text-left font-weight-bold">#</th>
                      <th class="text-left font-weight-bold">{{ $t('company') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.description') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.created_at') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(enrichedReport, index) in paginatedReports" :key="enrichedReport.report.id">
                      <td>{{ (currentPage - 1) * pageSize + index + 1 }}</td>
                      <td>
                        <router-link v-if="enrichedReport.company" :to="`/companies/${enrichedReport.company.id}`" class="text-primary text-decoration-none">
                          {{ enrichedReport.company.name }}
                        </router-link>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>
                      <td>{{ enrichedReport.report.description }}</td>
                      <td>{{ formatDate(enrichedReport.report.createdAt) }}</td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </v-card-text>
          </v-card>

          <div v-if="totalPages > 1" class="d-flex justify-center mt-4">
            <v-pagination v-model="currentPage" :length="totalPages" :total-visible="5" color="primary" size="small" />
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="px-6 pb-4">
        <v-spacer />
        <v-btn variant="outlined" color="grey" size="large" @click="close">
          <template #prepend>
            <v-icon>mdi-arrow-left</v-icon>
          </template>
          {{ $t('back') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import type { Report, Company, EnrichedUser } from '@/models';
import { useCompaniesStore } from '@/stores/companies';
import api from '@/services/api';

interface Props {
  modelValue: boolean;
  enrichedUser: EnrichedUser | null;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
}

interface EnrichedReport {
  report: Report;
  company: Company | null;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();

const companiesStore = useCompaniesStore();

const dialog = ref(props.modelValue);
const isLoading = ref(false);
const rawReports = ref<Report[]>([]);
const enrichedReports = ref<EnrichedReport[]>([]);

const sortBy = ref('createdAt');
const sortDesc = ref(true);

const currentPage = ref(1);
const pageSize = 10;

const sortOptions = [
  {
    value: 'createdAt-asc',
    orderBy: 'createdAt',
    desc: false,
    label: 'admin.creation_date_asc',
    icon: 'mdi-sort-calendar-ascending',
  },
  {
    value: 'createdAt-desc',
    orderBy: 'createdAt',
    desc: true,
    label: 'admin.creation_date_desc',
    icon: 'mdi-sort-calendar-descending',
  },
];

const currentSort = computed(() => `${sortBy.value}-${sortDesc.value ? 'desc' : 'asc'}`);

const currentSortLabel = computed(() => {
  const option = sortOptions.find((opt) => opt.value === currentSort.value);
  return option?.label || sortOptions[1].label;
});

watch(
  rawReports,
  async (newReports) => {
    if (newReports.length === 0) {
      enrichedReports.value = [];
      isLoading.value = false;
      return;
    }

    try {
      const enriched: EnrichedReport[] = await Promise.all(
        newReports.map(async (report: Report) => {
          let company: Company | null = null;

          if (report.companyUri) {
            try {
              company = await companiesStore.fetch(report.companyUri);
            } catch (e) {
              console.error(`Failed to fetch company for report ${report.id}`, e);
            }
          }

          return {
            report,
            company,
          };
        }),
      );

      enrichedReports.value = enriched;
    } catch (e) {
      console.error('Error enriching reports', e);
    } finally {
      isLoading.value = false;
    }
  },
  { deep: true, immediate: true },
);

const sortedReports = computed(() => {
  const reports = [...enrichedReports.value];

  reports.sort((a, b) => {
    const dateA = new Date(a.report.createdAt).getTime();
    const dateB = new Date(b.report.createdAt).getTime();

    return sortDesc.value ? dateB - dateA : dateA - dateB;
  });

  return reports;
});

const totalPages = computed(() => Math.ceil(sortedReports.value.length / pageSize) || 1);

const paginatedReports = computed(() => {
  const start = (currentPage.value - 1) * pageSize;
  const end = start + pageSize;
  return sortedReports.value.slice(start, end);
});

watch(
  () => props.modelValue,
  (newValue) => {
    dialog.value = newValue;
    if (newValue && props.enrichedUser?.user.reportsUri) {
      loadReports();
    }
  },
);

watch(dialog, (newValue) => {
  emit('update:modelValue', newValue);
  if (!newValue) {
    rawReports.value = [];
    enrichedReports.value = [];
    currentPage.value = 1;
  }
});

const loadReports = async () => {
  if (!props.enrichedUser?.user.reportsUri) return;

  isLoading.value = true;
  try {
    const response = await api.get(props.enrichedUser.user.reportsUri);
    rawReports.value = response.data;
  } catch (error) {
    console.error('Failed to load reports', error);
    rawReports.value = [];
    isLoading.value = false;
  }
};

const setSortOrder = (orderBy: string, desc: boolean) => {
  sortBy.value = orderBy;
  sortDesc.value = desc;
  currentPage.value = 1;
};

const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return date.toLocaleString();
  } catch (error) {
    return dateString;
  }
};

const close = () => {
  dialog.value = false;
};
</script>

<style scoped>
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

.table-responsive {
  overflow-x: auto;
}

.v-table {
  background: transparent !important;
}

.v-table thead tr th {
  background: rgba(var(--v-theme-primary), 0.05) !important;
  color: rgb(var(--v-theme-primary)) !important;
  font-weight: 700 !important;
  padding: 12px 16px !important;
  font-size: 0.875rem !important;
}

.v-table tbody tr {
  transition: background-color 0.2s ease;
}

.v-table tbody tr:hover {
  background: rgba(var(--v-theme-primary), 0.03) !important;
}

.v-table tbody tr td {
  padding: 12px 16px !important;
  vertical-align: middle !important;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.bg-primary-lighten-4 {
  background-color: rgba(var(--v-theme-primary), 0.15) !important;
}

.v-theme--dark .join-dialog-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}
</style>
