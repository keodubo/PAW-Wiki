<template>
  <v-card class="my-pool-card elevation-2" rounded="lg" :class="statusCardClass">
    <v-card-text class="pa-3">
      <div class="d-flex justify-space-between align-center mb-2">
        <v-chip :color="statusColor" variant="flat" size="small" class="font-weight-bold text-uppercase">
          <v-icon :icon="statusIcon" size="small" start></v-icon>
          {{ $t(`pool_status.${enrichedPool.pool.status}`) }}
        </v-chip>
      </div>

      <v-row class="mb-2">
        <v-col cols="6">
          <div class="d-flex align-center ga-3 mb-2">
            <v-avatar size="44" rounded="lg" class="flex-shrink-0">
              <v-img :src="enrichedPool.product.imageUri" :alt="enrichedPool.product.name">
                <template #placeholder>
                  <v-img src="@/assets/empty.svg" :alt="enrichedPool.product.name" />
                </template>
              </v-img>
            </v-avatar>
            <div class="min-width-0">
              <div class="d-flex align-center ga-1 flex-wrap">
                <h4 class="text-h6 font-weight-bold text-truncate" :title="`${enrichedPool.product.name} ${t('pool_location_in', { location: enrichedPool.location.name })}`">
                  {{ enrichedPool.product.name }}
                </h4>
                <v-chip v-if="!enrichedPool.product.active" color="error" variant="flat" size="x-small" class="font-weight-bold text-uppercase flex-shrink-0">
                  <v-icon start size="10">mdi-archive-off</v-icon>
                  {{ $t('product_retired') }}
                </v-chip>
              </div>
            </div>
          </div>
          <div class="d-flex align-center mb-1">
            <v-icon size="small" class="mr-2 text-medium-emphasis">mdi-map-marker</v-icon>
            <span class="text-body-2 text-medium-emphasis text-truncate">{{ enrichedPool.location.name }}</span>
          </div>

          <div class="d-flex align-center">
            <v-icon size="small" class="mr-2 text-medium-emphasis">mdi-tag</v-icon>
            <span class="text-body-2 text-medium-emphasis">{{ $t(`category.${enrichedPool.category.name}`) }}</span>
          </div>
        </v-col>

        <v-col cols="6" class="text-right">
          <h5 class="text-h6 font-weight-bold mb-1 text-nowrap">{{ formatCurrency(enrichedPool.pool.price) }}</h5>
          <span class="text-body-2 text-medium-emphasis">{{ t('pool_min_quantity') }}: {{ enrichedPool.pool.minQuantity }}</span>
        </v-col>
      </v-row>

      <v-divider class="my-2"></v-divider>

      <div v-if="isAvailableOrPaused" class="mb-2">
        <div class="d-flex justify-space-between mb-2">
          <span class="text-body-2">
            <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
            {{ $t('request_status.pending') }}: {{ enrichedPool.pool.requestsStats.pendingCount }} ({{ enrichedPool.pool.requestsStats.pendingSum }})
          </span>
          <span class="text-body-2">
            <v-icon size="small" class="mr-1">mdi-check-circle-outline</v-icon>
            {{ $t('request_status.accepted') }}: {{ enrichedPool.pool.requestsStats.acceptedCount }} ({{ enrichedPool.pool.requestsStats.acceptedSum }})
          </span>
        </div>
      </div>

      <div v-if="isDelivering" class="mb-2">
        <div class="d-flex justify-space-between mb-2">
          <span class="text-body-2">
            <v-icon size="small" class="mr-1">mdi-truck-outline</v-icon>
            {{ $t('request_status.not_delivered') }}: {{ enrichedPool.pool.requestsStats.acceptedCount }} ({{ enrichedPool.pool.requestsStats.acceptedSum }})
          </span>
          <span class="text-body-2 text-success">
            <v-icon size="small" class="mr-1">mdi-check-circle</v-icon>
            {{ $t('request_status.delivered') }}: {{ enrichedPool.pool.requestsStats.deliveredCount }} ({{ enrichedPool.pool.requestsStats.deliveredSum }})
          </span>
        </div>
      </div>

      <div v-if="isFinished" class="mb-2">
        <div class="d-flex justify-end mb-2">
          <span class="text-body-2 text-success">
            <v-icon size="small" class="mr-1">mdi-check-circle</v-icon>
            {{ $t('request_status.delivered') }}: {{ enrichedPool.pool.requestsStats.deliveredCount }} ({{ enrichedPool.pool.requestsStats.deliveredSum }})
          </span>
        </div>
      </div>

      <div v-if="isCancelled" class="mb-2">
        <div class="d-flex justify-space-between mb-2">
          <span class="text-body-2">
            <v-icon size="small" class="mr-1">mdi-clock-outline</v-icon>
            {{ $t('request_status.pending') }}: {{ enrichedPool.pool.requestsStats.pendingCount }} ({{ enrichedPool.pool.requestsStats.pendingSum }})
          </span>
          <span class="text-body-2 text-error">
            <v-icon size="small" class="mr-1">mdi-close-circle</v-icon>
            {{ $t('request_status.rejected') }}: {{ enrichedPool.pool.requestsStats.rejectedCount }}
          </span>
        </div>
      </div>

      <div class="mb-2">
        <v-progress-linear :model-value="progressPercentage" :color="progressColor" height="6" rounded class="mb-1">
          <template v-slot:default>
            <div v-if="pendingPercentage > 0" class="bg-pending" :style="{ width: pendingPercentage + '%', height: '100%', position: 'absolute', left: progressPercentage + '%' }"></div>
          </template>
        </v-progress-linear>
        <p class="text-center text-caption text-medium-emphasis ma-0">
          {{ enrichedPool.pool.requestsStats.acceptedSum + enrichedPool.pool.requestsStats.deliveredSum }} / {{ enrichedPool.pool.requestsStats.pendingSum + enrichedPool.pool.requestsStats.acceptedSum + enrichedPool.pool.requestsStats.deliveredSum }} /
          {{ enrichedPool.pool.minQuantity }}
        </p>
      </div>

      <v-row class="mt-1">
        <v-col cols="6">
          <v-btn color="primary" variant="flat" block @click="viewDetails">
            <v-icon start>mdi-text-box-outline</v-icon>
            {{ $t('details') }}
          </v-btn>
        </v-col>
        <v-col cols="6">
          <v-btn v-if="canEdit" color="primary" variant="outlined" block @click="editPool">
            <v-icon start>mdi-pencil</v-icon>
            {{ $t('edit') }}
          </v-btn>
          <v-btn v-else-if="canRestart" color="success" variant="flat" block @click="restartPool">
            <v-icon start>mdi-refresh</v-icon>
            {{ $t('restart') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { EnrichedPool } from '@/models';
import { PoolStatus } from '@/models';
import { useI18n } from 'vue-i18n';
import { poolService } from '@/services/PoolService';
import { formatCurrency } from '@/utils/currency';

interface Props {
  enrichedPool: EnrichedPool;
}

const props = defineProps<Props>();
const { t } = useI18n();
const router = useRouter();

const isAvailableOrPaused = computed(() => props.enrichedPool.pool.status === PoolStatus.AVAILABLE || props.enrichedPool.pool.status === PoolStatus.PAUSED);

const isDelivering = computed(() => props.enrichedPool.pool.status === PoolStatus.DELIVERING);

const isFinished = computed(() => props.enrichedPool.pool.status === PoolStatus.FINISHED);

const isCancelled = computed(() => props.enrichedPool.pool.status === PoolStatus.CANCELLED);

const statusColor = computed(() => {
  switch (props.enrichedPool.pool.status) {
    case PoolStatus.AVAILABLE:
      return 'success';
    case PoolStatus.DELIVERING:
      return 'warning';
    case PoolStatus.PAUSED:
      return 'info';
    case PoolStatus.CANCELLED:
      return 'error';
    case PoolStatus.FINISHED:
      return 'grey';
    default:
      return 'grey';
  }
});

const statusIcon = computed(() => {
  switch (props.enrichedPool.pool.status) {
    case PoolStatus.AVAILABLE:
      return 'mdi-clock-time-four-outline';
    case PoolStatus.DELIVERING:
      return 'mdi-truck-delivery';
    case PoolStatus.PAUSED:
      return 'mdi-pause-circle';
    case PoolStatus.CANCELLED:
      return 'mdi-cancel';
    case PoolStatus.FINISHED:
      return 'mdi-check-circle';
    default:
      return 'mdi-help-circle';
  }
});

const statusCardClass = computed(() => {
  return `status-${props.enrichedPool.pool.status.toLowerCase()}`;
});

const progressPercentage = computed(() => {
  const stats = props.enrichedPool.pool.requestsStats;
  const total = Math.max(stats.pendingSum + stats.acceptedSum + stats.deliveredSum, props.enrichedPool.pool.minQuantity);
  return ((stats.acceptedSum + stats.deliveredSum) * 100) / total;
});

const pendingPercentage = computed(() => {
  const stats = props.enrichedPool.pool.requestsStats;
  const total = Math.max(stats.pendingSum + stats.acceptedSum + stats.deliveredSum, props.enrichedPool.pool.minQuantity);
  return (stats.pendingSum * 100) / total;
});

const progressColor = computed(() => {
  if (progressPercentage.value < 15) return 'error';
  if (progressPercentage.value < 50) return 'warning';
  return 'success';
});

const canEdit = computed(() => props.enrichedPool.pool.status === PoolStatus.AVAILABLE || props.enrichedPool.pool.status === PoolStatus.PAUSED);

const canRestart = computed(
  () => (props.enrichedPool.pool.status === PoolStatus.CANCELLED || props.enrichedPool.pool.status === PoolStatus.FINISHED) && props.enrichedPool.product.active
);

const viewDetails = () => {
  router.push(`/pools/${props.enrichedPool.pool.id}`);
};

const editPool = () => {
  router.push(`/pools/${props.enrichedPool.pool.id}/edit`);
};

const restartPool = async () => {
  try {
    const newPoolId = await poolService.copy(props.enrichedPool.pool.id);
    router.push(`/pools/${newPoolId}`);
  } catch (error) {
    console.error('Failed to restart pool', error);
  }
};
</script>

<style scoped>
.my-pool-card {
  transition: all 0.3s ease;
  background-color: rgb(var(--v-theme-surface));
  border-left: 4px solid transparent;
}

.my-pool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.status-available {
  border-left-color: rgb(var(--v-theme-success));
}

.status-delivering {
  border-left-color: rgb(var(--v-theme-warning));
}

.status-paused {
  border-left-color: rgb(var(--v-theme-info));
}

.status-cancelled {
  border-left-color: rgb(var(--v-theme-error));
}

.status-finished {
  border-left-color: rgb(var(--v-theme-grey));
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.min-width-0 {
  min-width: 0;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.text-success {
  color: rgb(var(--v-theme-success)) !important;
}

.text-error {
  color: rgb(var(--v-theme-error)) !important;
}

.bg-pending {
  background-color: rgba(var(--v-theme-info), 0.3);
  border-radius: 4px;
  z-index: 1;
}

.v-progress-linear {
  border-radius: 4px;
  overflow: hidden;
}
</style>
