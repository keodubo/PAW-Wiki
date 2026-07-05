<template>
  <div>
    <v-progress-linear v-if="loading" indeterminate color="primary" />

    <div v-else-if="requests && requests.length > 0">
      <v-table class="requests-table">
        <thead>
          <tr>
            <th class="text-left">#</th>
            <th class="text-left">{{ $t('pool_detail.email') }}</th>
            <th class="text-left">{{ $t('pool_detail.name') }}</th>
            <th class="text-left">{{ $t('pool_detail.block_status') }}</th>
            <th class="text-left">{{ $t('pool_detail.quantity_label') }}</th>
            <th class="text-left">{{ $t('pool_detail.price') }}</th>
            <th v-if="poolDownPayment > 0" class="text-left">{{ $t('pool_detail.down_payment', { percent: poolDownPayment }) }}</th>
            <th v-if="poolDownPayment > 0" class="text-left">{{ $t('pool_detail.receipt') }}</th>
            <th v-if="showFinalPaymentColumn" class="text-left">{{ $t('pool_detail.final_payment') }}</th>
            <th v-if="showFinalPaymentColumn" class="text-left">{{ $t('pool_detail.receipt') }}</th>
            <th class="text-right">{{ $t('pool_detail.actions') }}</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(request, index) in requests" :key="request.id">
            <td class="align-middle">{{ index + 1 }}</td>
            <td class="align-middle">
              <div v-if="getUser(request.userUri)" class="text-body-2">
                {{ getUser(request.userUri)?.email }}
              </div>
              <span v-else class="text-caption text-medium-emphasis">-</span>
            </td>
            <td class="align-middle">
              <div v-if="getUser(request.userUri)" class="text-body-2">
                {{ $t(getUserDisplayName(request.userUri)) }}
              </div>
              <span v-else class="text-caption text-medium-emphasis">-</span>
            </td>
            <td class="align-middle">
              <div v-if="getUser(request.userUri)">
                <block-status :user="getUser(request.userUri)!" />
              </div>
              <span v-else class="text-caption text-medium-emphasis">-</span>
            </td>
            <td class="align-middle">{{ request.quantity }}</td>
            <td class="align-middle">{{ formatCurrency(request.quantity * poolPrice) }}</td>

            <template v-if="poolDownPayment > 0">
              <td class="align-middle">
                {{ formatCurrency(request.quantity * poolPrice * (poolDownPayment / 100)) }}
              </td>
              <td class="align-middle">
                <v-btn v-if="request.downPaymentUri" variant="text" size="small" color="primary" @click="viewDocument(request.downPaymentUri)" :loading="viewingDocumentUri === request.downPaymentUri" :disabled="viewingDocumentUri !== null">
                  {{ $t('pool_detail.show') }}
                </v-btn>
                <span v-else class="text-medium-emphasis">{{ $t('pool_detail.not_uploaded') }}</span>
              </td>
            </template>

            <template v-if="showFinalPaymentColumn">
              <td class="align-middle">
                {{ formatCurrency(request.quantity * poolPrice * (1 - poolDownPayment / 100)) }}
              </td>
              <td class="align-middle">
                <v-btn
                  v-if="request.finalPaymentUri"
                  variant="text"
                  size="small"
                  color="primary"
                  @click="viewDocument(request.finalPaymentUri)"
                  :loading="viewingDocumentUri === request.finalPaymentUri"
                  :disabled="viewingDocumentUri !== null"
                >
                  {{ $t('pool_detail.show') }}
                </v-btn>
                <span v-else class="text-medium-emphasis">{{ $t('pool_detail.not_uploaded') }}</span>
              </td>
            </template>

            <td class="align-middle">
              <div class="d-flex gap-2 justify-end align-center">
                <template v-if="status === 'pending'">
                  <v-tooltip v-if="canAcceptRequest(request)" location="top">
                    <template #activator="{ props }">
                      <v-btn v-bind="props" icon="mdi-check" size="small" color="success" @click="handleAccept(request.id)" :loading="actionLoading === request.id" />
                    </template>
                    <span>{{ $t('pool_detail.accept') }}</span>
                  </v-tooltip>
                  <v-tooltip v-else location="top">
                    <template #activator="{ props }">
                      <v-btn v-bind="props" icon="mdi-check" size="small" disabled />
                    </template>
                    <span>{{ $t(getAcceptDisabledReason(request)) }}</span>
                  </v-tooltip>

                  <v-tooltip location="top">
                    <template #activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-close"
                        size="small"
                        color="error"
                        @click="
                          rejectingRequestId = request.id;
                          showRejectDialog = true;
                        "
                        :loading="actionLoading === request.id"
                      />
                    </template>
                    <span>{{ $t('pool_detail.reject') }}</span>
                  </v-tooltip>
                </template>

                <template v-if="status === 'accepted' || status === 'undelivered'">
                  <v-tooltip v-if="poolStatus === PoolStatus.AVAILABLE || poolStatus === PoolStatus.PAUSED" location="top">
                    <template #activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-undo"
                        size="small"
                        color="error"
                        @click="
                          unacceptingRequestId = request.id;
                          showUnacceptDialog = true;
                        "
                        :loading="actionLoading === request.id"
                      />
                    </template>
                    <span>{{ $t('pool_detail.unaccept') }}</span>
                  </v-tooltip>

                  <template v-else-if="poolStatus === PoolStatus.DELIVERING">
                    <v-tooltip v-if="request.finalPaymentUri" location="top">
                      <template #activator="{ props }">
                        <v-btn v-bind="props" icon="mdi-truck-delivery" size="small" color="success" @click="handleDeliver(request.id)" :loading="actionLoading === request.id" />
                      </template>
                      <span>{{ $t('pool_detail.deliver') }}</span>
                    </v-tooltip>
                    <v-tooltip v-else location="top">
                      <template #activator="{ props }">
                        <v-btn v-bind="props" icon="mdi-truck-delivery" size="small" disabled />
                      </template>
                      <span>{{ $t('pool_detail.needs_final_payment') }}</span>
                    </v-tooltip>
                  </template>
                </template>

                <template v-if="status === 'delivered'">
                  <v-tooltip v-if="poolStatus === PoolStatus.DELIVERING" location="top">
                    <template #activator="{ props }">
                      <v-btn
                        v-bind="props"
                        icon="mdi-undo"
                        size="small"
                        color="error"
                        @click="
                          undeliveringRequestId = request.id;
                          showUndeliverDialog = true;
                        "
                        :loading="actionLoading === request.id"
                      />
                    </template>
                    <span>{{ $t('pool_detail.undo_deliver') }}</span>
                  </v-tooltip>

                  <v-icon v-else color="success" size="small">mdi-check</v-icon>
                </template>

                <v-tooltip location="top">
                  <template #activator="{ props }">
                    <v-btn v-bind="props" icon="mdi-flag" size="small" color="error" variant="flat" @click="openReportDialog(request.userUri)" />
                  </template>
                  <span>{{ $t('pool_detail.report_user') }}</span>
                </v-tooltip>
              </div>
            </td>
          </tr>
        </tbody>
      </v-table>
    </div>

    <v-alert v-else type="info" variant="tonal" class="ma-4">
      <div class="text-center">
        <div>{{ $t('pool_detail.no_requests') }}</div>
      </div>
    </v-alert>

    <v-dialog v-model="showRejectDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-close-circle</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ $t('pool_detail.reject_request') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ $t('pool_detail.reject_request_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showRejectDialog = false" :disabled="actionLoading === rejectingRequestId">
            <template #prepend>
              <v-icon>mdi-arrow-left</v-icon>
            </template>
            {{ $t('pool_detail.back') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="handleReject(rejectingRequestId!)" :loading="actionLoading === rejectingRequestId">
            <template #prepend v-if="actionLoading !== rejectingRequestId">
              <v-icon>mdi-close-circle</v-icon>
            </template>
            {{ $t('pool_detail.reject') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showUnacceptDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-undo</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ $t('pool_detail.unaccept_request') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ $t('pool_detail.unaccept_request_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showUnacceptDialog = false" :disabled="actionLoading === unacceptingRequestId">
            <template #prepend>
              <v-icon>mdi-arrow-left</v-icon>
            </template>
            {{ $t('pool_detail.back') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="handleUnaccept(unacceptingRequestId!)" :loading="actionLoading === unacceptingRequestId">
            <template #prepend v-if="actionLoading !== unacceptingRequestId">
              <v-icon>mdi-undo-variant</v-icon>
            </template>
            {{ $t('pool_detail.unaccept') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showUndeliverDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-undo</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ $t('pool_detail.undeliver_request') }}
            </h2>
          </div>
        </v-card-title>
        <v-card-text class="pa-6">
          <v-alert type="warning" variant="tonal" density="comfortable" icon="mdi-alert" class="mb-4">
            {{ $t('pool_detail.undeliver_request_confirmation') }}
          </v-alert>
        </v-card-text>
        <v-card-actions class="px-6 pb-4 d-flex justify-end ga-2">
          <v-btn variant="outlined" color="grey" size="large" @click="showUndeliverDialog = false" :disabled="actionLoading === undeliveringRequestId">
            <template #prepend>
              <v-icon>mdi-arrow-left</v-icon>
            </template>
            {{ $t('pool_detail.back') }}
          </v-btn>
          <v-btn color="error" variant="flat" size="large" @click="handleUndeliver(undeliveringRequestId!)" :loading="actionLoading === undeliveringRequestId">
            <template #prepend v-if="actionLoading !== undeliveringRequestId">
              <v-icon>mdi-truck-remove</v-icon>
            </template>
            {{ $t('pool_detail.undo_deliver') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-dialog v-model="showReportDialog" max-width="550">
      <v-card class="join-dialog-card elevation-12 rounded-xl">
        <v-card-title class="join-dialog-header pa-6 text-center">
          <div class="header-section">
            <v-icon size="56" color="white" class="mb-2">mdi-flag</v-icon>
            <h2 class="text-h5 font-weight-bold text-white mb-1">
              {{ $t('pool_detail.report_dialog_title') }}
            </h2>
            <p class="text-body-2 text-white opacity-90">
              {{ $t('pool_detail.report_dialog_description') }}
            </p>
          </div>
        </v-card-title>

        <v-card-text class="pa-6">
          <v-form ref="reportForm" @submit.prevent="handleReport">
            <v-textarea
              v-model="reportDescription"
              :label="$t('pool_detail.report_description_label')"
              :rules="descriptionRules"
              variant="outlined"
              density="comfortable"
              prepend-inner-icon="mdi-text"
              color="primary"
              class="mb-4"
              rows="5"
              counter="1024"
              required
            />

            <v-alert type="info" variant="tonal" density="compact" class="mb-4" icon="mdi-information">
              {{ $t('pool_detail.report_info') }}
            </v-alert>

            <v-btn type="submit" color="error" size="large" block :loading="isSubmittingReport" class="dialog-action-btn mb-3">
              <template #prepend v-if="!isSubmittingReport">
                <v-icon>mdi-flag</v-icon>
              </template>
              {{ $t('pool_detail.submit_report') }}
            </v-btn>
            <v-btn variant="outlined" color="grey" size="large" block @click="closeReportDialog" :disabled="isSubmittingReport">
              <template #prepend>
                <v-icon>mdi-close</v-icon>
              </template>
              {{ $t('pool_detail.cancel') }}
            </v-btn>
          </v-form>
        </v-card-text>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import { ref, onMounted, watch, computed } from 'vue';
import type { Request, User } from '@/models';
import { PoolStatus } from '@/models/Pool';
import { useUsersStore } from '@/stores/users';
import { requestService, documentService, reportService } from '@/services';
import { useNotifications } from '@/composables/useNotifications';
import BlockStatus from '@/components/BlockStatus.vue';
import { formatCurrency } from '@/utils/currency';

interface Props {
  requests: Request[];
  loading: boolean;
  status: 'pending' | 'accepted' | 'undelivered' | 'delivered' | 'rejected';
  poolStatus?: PoolStatus;
  poolDownPayment?: number;
  poolPrice?: number;
}

const props = withDefaults(defineProps<Props>(), {
  poolDownPayment: 0,
  poolPrice: 0,
});

const emit = defineEmits<{
  reload: [];
}>();

const usersStore = useUsersStore();
const { showSuccess, showError, handleApiError } = useNotifications();
const { t } = useI18n();

const actionLoading = ref<number | null>(null);
const showRejectDialog = ref(false);
const showUnacceptDialog = ref(false);
const showUndeliverDialog = ref(false);
const rejectingRequestId = ref<number | null>(null);
const unacceptingRequestId = ref<number | null>(null);
const undeliveringRequestId = ref<number | null>(null);
const viewingDocumentUri = ref<string | null>(null);

const showReportDialog = ref(false);
const reportDescription = ref('');
const isSubmittingReport = ref(false);
const reportingUserId = ref<number | null>(null);
const reportForm = ref<any>(null);

const showFinalPaymentColumn = computed(() => {
  return props.poolStatus === PoolStatus.DELIVERING || props.poolStatus === PoolStatus.FINISHED;
});

const fetchUsers = async (requests: Request[]) => {
  const userUris = new Set<string>();
  requests.forEach((request) => {
    if (request.userUri) {
      userUris.add(request.userUri);
    }
  });

  const usersToFetch = Array.from(userUris).filter((uri) => !usersStore.getEntry(uri));

  if (usersToFetch.length > 0) {
    await Promise.all(
      usersToFetch.map((uri) =>
        usersStore.fetch(uri).catch((err) => {
          console.error(`Failed to fetch user ${uri}:`, err);
          return null;
        }),
      ),
    );
  }
};

onMounted(async () => {
  await fetchUsers(props.requests);
});

watch(
  () => props.requests,
  async (newRequests) => {
    if (newRequests && newRequests.length > 0) {
      await fetchUsers(newRequests);
    }
  },
  { deep: true },
);

const getUser = (userUri: string): User | undefined => {
  return usersStore.getEntry(userUri);
};

const getUserDisplayName = (userUri: string): string => {
  const user = getUser(userUri);
  if (!user) return 'pool_detail.user_loading';
  return `${user.firstName} ${user.lastName}`.trim() || user.email || 'pool_detail.unknown_user';
};

const canAcceptRequest = (request: Request): boolean => {
  const user = getUser(request.userUri);
  if (!user) return false;

  if (user.isBlocked) return false;

  if (props.poolDownPayment === 0) return true;
  return request.downPaymentUri != null;
};

const getAcceptDisabledReason = (request: Request): string => {
  const user = getUser(request.userUri);
  if (user?.isBlocked) {
    return 'pool_detail.user_blocked';
  }
  if (props.poolDownPayment > 0 && !request.downPaymentUri) {
    return 'pool_detail.needs_down_payment';
  }
  return '';
};

const viewDocument = async (documentUri: string) => {
  try {
    viewingDocumentUri.value = documentUri;
    const blob = await documentService.getByUri(documentUri);
    const blobUrl = URL.createObjectURL(blob);
    const newWindow = window.open(blobUrl, '_blank');
    if (!newWindow) URL.revokeObjectURL(blobUrl);
  } catch (error) {
    handleApiError(error);
  } finally {
    viewingDocumentUri.value = null;
  }
};

const handleAccept = async (requestId: number) => {
  try {
    actionLoading.value = requestId;
    await requestService.accept(requestId);
    showSuccess('pool_detail.request_accepted');
    emit('reload');
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = null;
  }
};

const handleReject = async (requestId: number) => {
  try {
    actionLoading.value = requestId;
    await requestService.reject(requestId);
    showSuccess('pool_detail.request_rejected');
    showRejectDialog.value = false;
    rejectingRequestId.value = null;
    emit('reload');
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = null;
  }
};

const handleUnaccept = async (requestId: number) => {
  try {
    actionLoading.value = requestId;
    await requestService.await(requestId);
    showSuccess('pool_detail.request_unaccepted');
    showUnacceptDialog.value = false;
    unacceptingRequestId.value = null;
    emit('reload');
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = null;
  }
};

const handleDeliver = async (requestId: number) => {
  try {
    actionLoading.value = requestId;
    await requestService.deliver(requestId);
    showSuccess('pool_detail.request_delivered');
    emit('reload');
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = null;
  }
};

const handleUndeliver = async (requestId: number) => {
  try {
    actionLoading.value = requestId;

    await requestService.accept(requestId);
    showSuccess('pool_detail.request_undelivered');
    showUndeliverDialog.value = false;
    undeliveringRequestId.value = null;
    emit('reload');
  } catch (error) {
    handleApiError(error);
  } finally {
    actionLoading.value = null;
  }
};

const descriptionRules = computed(() => [
  (v: string) => !!v || t('pool_detail.report_description_required'),
  (v: string) => (v && v.length >= 1) || t('pool_detail.report_description_min'),
  (v: string) => (v && v.length <= 1024) || t('pool_detail.report_description_max'),
]);

const openReportDialog = (userUri: string) => {
  const user = getUser(userUri);
  if (!user?.id) {
    showError('pool_detail.report_error_no_user');
    return;
  }
  reportingUserId.value = user.id;
  reportDescription.value = '';
  showReportDialog.value = true;
};

const closeReportDialog = () => {
  showReportDialog.value = false;
  reportDescription.value = '';
  reportingUserId.value = null;
  if (reportForm.value) {
    reportForm.value.resetValidation();
  }
};

const handleReport = async () => {
  if (!reportForm.value) return;

  const { valid } = await reportForm.value.validate();
  if (!valid) return;

  if (!reportingUserId.value) {
    showError('pool_detail.report_error_no_user');
    return;
  }

  try {
    isSubmittingReport.value = true;
    await reportService.createReportForUser(reportingUserId.value, reportDescription.value);
    showSuccess('pool_detail.report_success');
    closeReportDialog();
  } catch (error) {
    handleApiError(error);
  } finally {
    isSubmittingReport.value = false;
  }
};
</script>

<style scoped>
.requests-table {
  background: transparent;
}

.requests-table :deep(thead) {
  background: rgba(var(--v-theme-primary), 0.05);
}

.requests-table :deep(th) {
  font-weight: 600 !important;
  text-transform: uppercase;
  font-size: 0.75rem;
  letter-spacing: 0.05em;
}

.gap-2 {
  gap: 8px;
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

.dialog-action-btn {
  font-weight: 600;
  text-transform: none;
  letter-spacing: 0.5px;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.dialog-action-btn:hover:not(.v-btn--disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(var(--v-theme-error), 0.3) !important;
}

.join-dialog-card .v-card-text .v-btn[type='submit'],
.join-dialog-card .v-card-text .v-btn.block {
  width: 100% !important;
}

.join-dialog-card .v-card-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
