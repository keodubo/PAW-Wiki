<template>
  <div class="users-admin-page">
    <v-container fluid>
      <v-row justify="center" class="mb-6">
        <v-col cols="12" md="10" lg="10">
          <h1 class="text-h4 font-weight-bold text-primary">{{ $t('admin.users') }}</h1>
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
                    :placeholder="$t('admin.user_search_placeholder')"
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
          <p class="text-body-2 text-medium-emphasis">{{ $t('showing') }} {{ enrichedUsers.length }} {{ $t('of') }} {{ pagination.totalItems }} {{ $t('navbar.users').toLowerCase() }}</p>
        </v-col>
      </v-row>

      <div v-if="enrichedUsers.length === 0 && !isLoading" class="text-center py-12">
        <v-img src="@/assets/empty.svg" alt="No users found" max-width="300" class="mx-auto mb-4" />
        <h3 class="text-h6 text-medium-emphasis mb-4">{{ $t('admin.no_users_found') }}</h3>
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
                      <th class="text-left font-weight-bold">{{ $t('email') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('name') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.user_validated') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.company_name') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.block_level') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.block_until') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('admin.reports') }}</th>
                      <th class="text-left font-weight-bold">{{ $t('actions') }}</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="(enrichedUser, index) in enrichedUsers" :key="enrichedUser.user.id">
                      <td>{{ (pagination.currentPage - 1) * pagination.pageSize + index + 1 }}</td>

                      <td>{{ enrichedUser.user.email }}</td>

                      <td>
                        <span v-if="enrichedUser.user.firstName && enrichedUser.user.lastName"> {{ enrichedUser.user.firstName }} {{ enrichedUser.user.lastName }} </span>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>

                      <td>
                        <v-chip v-if="enrichedUser.user.validated" color="success" size="small" variant="flat">
                          <v-icon start size="small">mdi-check</v-icon>
                          {{ $t('admin.validated') }}
                        </v-chip>
                        <v-chip v-else color="error" size="small" variant="flat">
                          <v-icon start size="small">mdi-close</v-icon>
                          {{ $t('admin.not_validated') }}
                        </v-chip>
                      </td>

                      <td>
                        <router-link v-if="enrichedUser.company" :to="`/companies/${enrichedUser.company.id}`" class="text-primary text-decoration-none font-weight-medium">
                          {{ enrichedUser.company.name }}
                        </router-link>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>

                      <td>
                        <v-chip :color="enrichedUser.user.blockLevel === 0 ? 'success' : enrichedUser.user.blockLevel === 4 ? 'error' : 'warning'" size="small" variant="flat">
                          {{ enrichedUser.user.blockLevel || 0 }}
                        </v-chip>
                      </td>

                      <td>
                        <span v-if="enrichedUser.user.blockLevel === 0" class="text-success">
                          {{ $t('admin.not_blocked') }}
                        </span>
                        <span v-else-if="enrichedUser.user.blockLevel === 4" class="text-error">
                          {{ $t('admin.blocked_permanently') }}
                        </span>
                        <span v-else-if="enrichedUser.user.blockedUntil">
                          {{ formatDate(enrichedUser.user.blockedUntil) }}
                        </span>
                        <span v-else class="text-medium-emphasis">---</span>
                      </td>

                      <td>
                        <span v-if="enrichedUser.user.hasReports" class="text-primary cursor-pointer" @click="viewReports(enrichedUser)">
                          {{ $t('admin.show') }}
                        </span>
                        <span v-else class="text-success">
                          {{ $t('admin.no_reports') }}
                        </span>
                      </td>

                      <td>
                        <div v-if="canBlockUser(enrichedUser)" class="d-flex ga-2">
                          <v-tooltip :text="$t('admin.block_user')">
                            <template v-slot:activator="{ props: tooltipProps }">
                              <v-btn v-bind="tooltipProps" color="error" size="small" icon variant="flat" @click="openBlockDialog(enrichedUser)">
                                <v-icon size="small">mdi-account-cancel</v-icon>
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

    <BlockUserDialog v-model="blockDialogOpen" :user="selectedUser" @confirm="handleBlockUser" />

    <ReportsDialog v-model="reportsDialogOpen" :enriched-user="selectedUserForReports" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue';
import { useUsersPage } from '@/composables/useUsersPage';
import PaginationLinks from '@/components/PaginationLinks.vue';
import BlockUserDialog from '@/components/BlockUserDialog.vue';
import ReportsDialog from '@/components/ReportsDialog.vue';
import type { EnrichedUser } from '@/models';
import { UserRole } from '@/models/UserRole';

definePage({
  meta: {
    requiresAuth: true,
    allowedRoles: [UserRole.ADMIN],
  },
});

const usersPage = useUsersPage();

const searchForm = ref();

const { enrichedUsers, isLoading, isSearching, filters, pagination, hasActiveFilters, currentValidatedText, handleSearch, handlePageChange, handleLinkNavigation, setValidatedFilter, clearFilters, blockUser, initialize } = usersPage;

const validatedOptions = [
  { value: null, text: 'admin.all_status' },
  { value: 'true', text: 'admin.validated' },
  { value: 'false', text: 'admin.not_validated' },
];

const blockDialogOpen = ref(false);
const selectedUser = ref<EnrichedUser | null>(null);

const reportsDialogOpen = ref(false);
const selectedUserForReports = ref<EnrichedUser | null>(null);

const formatDate = (dateString: string): string => {
  try {
    const date = new Date(dateString);
    return date.toLocaleString();
  } catch (error) {
    return dateString;
  }
};

const viewReports = (enrichedUser: EnrichedUser) => {
  if (enrichedUser.user.reportsUri) {
    selectedUserForReports.value = enrichedUser;
    reportsDialogOpen.value = true;
  }
};

const canBlockUser = (enrichedUser: EnrichedUser): boolean => {
  return !enrichedUser.user.companyUri && enrichedUser.user.blockLevel !== 4;
};

const openBlockDialog = (enrichedUser: EnrichedUser) => {
  selectedUser.value = enrichedUser;
  blockDialogOpen.value = true;
};

const handleBlockUser = async (userId: number) => {
  const currentBlockLevel = selectedUser.value?.user.blockLevel ?? 0;
  await blockUser(userId, currentBlockLevel);
  blockDialogOpen.value = false;
  selectedUser.value = null;
};

onMounted(() => {
  initialize();
});
</script>

<style scoped>
.users-admin-page {
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

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.cursor-pointer {
  cursor: pointer;
  text-decoration: underline;
}

.cursor-pointer:hover {
  color: rgb(var(--v-theme-primary)) !important;
  opacity: 0.8;
}

.text-decoration-none {
  text-decoration: none !important;
}

.text-decoration-none:hover {
  text-decoration: underline !important;
  opacity: 0.85;
}

@media (max-width: 960px) {
  .users-admin-page {
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
