<template>
  <div class="companies-admin-page">
    <v-container fluid>
      <v-row justify="center" class="mb-6">
        <v-col cols="12" md="10" lg="10">
          <h1 class="text-h4 font-weight-bold text-primary">{{ $t('admin.companies.title') }}</h1>
        </v-col>
      </v-row>

      <v-row justify="center" class="mb-4">
        <v-col cols="12" md="10" lg="10">
          <v-row align="center">
            <v-col cols="12" md="3">
              <v-select
                v-model="filters.validated"
                :items="validatedOptions"
                item-title="text"
                item-value="value"
                variant="outlined"
                density="comfortable"
                prepend-inner-icon="mdi-chart-pie"
                color="primary"
                class="filter-select elevation-2"
                hide-details
                @update:model-value="setValidatedFilter"
              >
                <template #selection="{ item }">
                  {{ $t(item.raw.text) }}
                </template>
                <template #item="{ props: itemProps, item }">
                  <v-list-item v-bind="itemProps" :title="$t(item.raw.text)" />
                </template>
              </v-select>
            </v-col>

            <v-col cols="12" md="6">
              <div class="search-container d-flex align-center ga-3">
                <v-form @submit.prevent="handleSearch" ref="searchForm" class="flex-grow-1">
                  <v-text-field
                    v-model="filters.search"
                    :placeholder="$t('admin.companies.search_placeholder')"
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

                <v-btn v-if="hasActiveFilters" color="error" variant="elevated" size="x-large" class="clear-btn elevation-6 rounded-xl" @click="clearFilters" :title="$t('admin.remove_filters')" icon>
                  <v-icon size="large">mdi-filter-remove</v-icon>
                </v-btn>
              </div>
            </v-col>

            <v-col cols="12" md="3"></v-col>
          </v-row>
        </v-col>
      </v-row>

      <v-row v-if="!isLoading" justify="center" class="mb-2">
        <v-col cols="12" md="10" lg="10">
          <p class="text-body-2 text-medium-emphasis">{{ $t('showing') }} {{ enrichedCompanies.length }} {{ $t('of') }} {{ pagination.totalItems }} {{ $t('navbar.companies').toLowerCase() }}</p>
        </v-col>
      </v-row>

      <div v-if="enrichedCompanies.length === 0 && !isLoading" class="text-center py-12">
        <v-img src="@/assets/empty.svg" alt="No companies found" max-width="300" class="mx-auto mb-4" />
        <h3 class="text-h6 text-medium-emphasis mb-4">{{ $t('admin.companies.no_companies_found') }}</h3>
        <p class="text-body-1 text-medium-emphasis mb-4">{{ $t('admin.try_different_search') }}</p>
        <v-btn v-if="hasActiveFilters" color="error" variant="outlined" @click="clearFilters" prepend-icon="mdi-filter-remove">
          {{ $t('clear_filters') }}
        </v-btn>
      </div>

      <v-row v-else justify="center">
        <v-col cols="12" md="10" lg="10">
          <v-card v-if="isLoading" class="elevation-4 rounded-xl">
            <v-card-text>
              <v-skeleton-loader type="table" />
            </v-card-text>
          </v-card>

          <v-card v-else class="elevation-4 rounded-xl">
            <v-card-text class="pa-0">
              <div class="table-responsive">
                <v-table>
                  <thead>
                    <tr>
                      <th class="text-left font-weight-bold">#</th>
                      <th class="text-left font-weight-bold">{{ $t('name') }}</th>
                      <th class="text-left font-weight-bold email-col">{{ $t('email') }}</th>
                      <th class="text-left font-weight-bold cbu-col">{{ $t('cbu') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('phone') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('address') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.companies.owner') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.companies.company_validated') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('actions') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(enrichedCompany, index) in enrichedCompanies" :key="enrichedCompany.company.id">
                      <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>

                      <td>{{ enrichedCompany.company.name }}</td>

                      <td class="email-col">
                        <span class="truncate-text">{{ enrichedCompany.company.email }}</span>
                      </td>

                      <td class="cbu-col">
                        <span v-if="enrichedCompany.company.cbu && enrichedCompany.company.cbu !== ''" class="truncate-text">
                          {{ enrichedCompany.company.cbu }}
                        </span>
                        <span v-else class="text-error truncate-text">
                          {{ $t('admin.companies.not_set') }}
                        </span>
                      </td>

                      <td>{{ enrichedCompany.company.phone }}</td>

                      <td>{{ enrichedCompany.company.address }}</td>

                      <td>
                        <router-link
                          v-if="enrichedCompany.owner && enrichedCompany.owner?.firstName && enrichedCompany.owner?.lastName"
                          :to="`/users?search=${encodeURIComponent(enrichedCompany.owner.email)}`"
                          class="text-primary text-decoration-none font-weight-medium"
                        >
                          {{ enrichedCompany.owner.firstName }} {{ enrichedCompany.owner.lastName }}
                        </router-link>
                        <router-link v-else-if="enrichedCompany.owner" :to="`/users?search=${encodeURIComponent(enrichedCompany.owner.email)}`" class="text-primary text-decoration-none font-weight-medium email-col">
                          <span class="truncate-text">{{ enrichedCompany.owner.email }}</span>
                        </router-link>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>

                      <td>
                        <span class="validated-icon-wrapper">
                          <span v-if="enrichedCompany.company.validated" class="validated-icon success">
                            <v-icon size="20">mdi-check</v-icon>
                          </span>
                          <span v-else class="validated-icon error">
                            <v-icon size="20">mdi-close</v-icon>
                          </span>
                        </span>
                      </td>

                      <td>
                        <div v-if="!enrichedCompany.company.validated" class="d-flex ga-2">
                          <v-tooltip :text="$t('admin.companies.validate')">
                            <template v-slot:activator="{ props: tooltipProps }">
                              <v-btn v-bind="tooltipProps" color="success" size="small" icon variant="flat" @click="openValidateDialog(enrichedCompany)">
                                <v-icon size="small">mdi-check-circle</v-icon>
                              </v-btn>
                            </template>
                          </v-tooltip>
                        </div>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>
                    </tr>
                  </tbody>
                </v-table>
              </div>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>

      <v-row v-if="pagination.links" justify="center" class="mt-6">
        <v-col cols="12" md="10" lg="10" class="d-flex justify-center">
          <PaginationLinks :links="pagination.links" :current-page="pagination.currentPage" :total-pages="pagination.totalPages" @navigate="handleLinkNavigation" />
        </v-col>
      </v-row>
    </v-container>

    <ValidateCompanyDialog v-model="validateDialogOpen" :company-id="selectedCompany?.company.id" @confirm="handleValidateCompany" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useCompaniesPage } from '@/composables/useCompaniesPage';
import PaginationLinks from '@/components/PaginationLinks.vue';
import ValidateCompanyDialog from '@/components/ValidateCompanyDialog.vue';
import type { EnrichedCompany } from '@/models';
import { UserRole } from '@/models/UserRole';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.ADMIN],
  },
});

const companiesPage = useCompaniesPage();

const searchForm = ref();

const { enrichedCompanies, isLoading, isSearching, filters, pagination, hasActiveFilters, currentValidatedText, handleSearch, handlePageChange, handleLinkNavigation, setValidatedFilter, clearFilters, validateCompany, initialize } =
  companiesPage;

const validatedOptions = [
  { value: null, text: 'admin.companies.all_status' },
  { value: 'true', text: 'admin.companies.validated' },
  { value: 'false', text: 'admin.companies.not_validated' },
];

const validateDialogOpen = ref(false);
const selectedCompany = ref<EnrichedCompany | null>(null);

const openValidateDialog = (enrichedCompany: EnrichedCompany) => {
  selectedCompany.value = enrichedCompany;
  validateDialogOpen.value = true;
};

const handleValidateCompany = async (companyId: number) => {
  await validateCompany(companyId);
  validateDialogOpen.value = false;
  selectedCompany.value = null;
};

onMounted(() => {
  initialize();
});
</script>

<style scoped>
.companies-admin-page {
  min-height: 100vh;
  background: rgb(var(--v-theme-background));
  padding-top: 2rem;
  padding-bottom: 2rem;
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

.filter-select :deep(.v-field) {
  background: rgb(var(--v-theme-surface)) !important;
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
  padding: 16px !important;
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

.email-col {
  max-width: 180px;
  width: 180px;
  word-break: break-all;
}
.cbu-col {
  max-width: 140px;
  width: 140px;
  word-break: break-all;
}
.truncate-text {
  display: block;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 110px;
}

.validated-icon-wrapper {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
}
.validated-icon {
  display: flex;
  justify-content: center;
  align-items: center;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: #e0e0e0;
}
.validated-icon.success {
  background-color: #4caf50;
  color: #fff;
}
.validated-icon.error {
  background-color: #f44336;
  color: #fff;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.text-decoration-none {
  text-decoration: none !important;
}

.text-decoration-none:hover {
  text-decoration: underline !important;
  opacity: 0.85;
}

@media (max-width: 960px) {
  .companies-admin-page {
    padding-top: 1rem;
  }

  .table-responsive {
    max-width: 100%;
  }

  .v-table {
    font-size: 0.875rem;
  }

  .v-table thead tr th,
  .v-table tbody tr td {
    padding: 8px 12px !important;
  }
}
</style>
