<template>
  <v-card class="pool-card shadow-card elevation-2" rounded="lg" hover @click="navigateToPool">
    <v-card-text class="pa-4">
      <v-row>
        <v-col :cols="showProductDetails ? 8 : 12">
          <div v-if="showProductDetails">
            <h4 class="text-h6 font-weight-bold logo-color mb-1 text-truncate" :title="enrichedPool.product.name">
              {{ enrichedPool.product.name || $t('product_detail.uncategorized') }}
            </h4>

            <div class="d-flex align-center mb-1">
              <v-icon size="small" class="mr-2 text-medium-emphasis">mdi-domain</v-icon>
              <span class="text-body-2 text-medium-emphasis text-truncate" :title="enrichedPool.company.name">
                {{ enrichedPool.company.name || $t('product_detail.unknown_company') }}
              </span>
            </div>

            <div class="d-flex align-center mb-1">
              <v-icon size="small" class="mr-2 text-medium-emphasis">mdi-tag</v-icon>
              <span class="text-body-2 text-medium-emphasis text-truncate" :title="$t(`category.${enrichedPool.category.name}`)">
                {{ $t(`category.${enrichedPool.category.name}`) || $t('product_detail.uncategorized') }}
              </span>
            </div>

            <div class="d-flex align-center mb-2">
              <v-icon size="small" class="mr-2 text-medium-emphasis">mdi-map-marker</v-icon>
              <span class="text-body-2 text-medium-emphasis text-truncate" :title="enrichedPool.location.name">
                {{ enrichedPool.location.name || $t('pool_detail.no_location') }}
              </span>
            </div>
          </div>

          <div v-else class="mb-2">
            <h4 class="text-h6 font-weight-bold logo-color pb-1">{{ $t('pool_detail.pool_fallback') }} {{ $t('pool_location_in', { location: enrichedPool.location.name || $t('pool_detail.no_location') }) }}</h4>
          </div>

          <h5 class="text-h6 font-weight-bold pt-2 mb-2">
            {{ formatCurrency(enrichedPool.pool.price) }}
          </h5>

          <div v-if="showProductDetails" class="mb-2">
            <div v-if="enrichedPool.product.rating && enrichedPool.product.rating > 0" class="pb-2 d-flex align-center" :title="enrichedPool.product.rating.toFixed(1)">
              <v-rating :model-value="enrichedPool.product.rating" color="warning" density="compact" half-increments readonly size="small" empty-icon="mdi-star-outline" full-icon="mdi-star" half-icon="mdi-star-half-full" class="mr-2" />
              <span class="text-caption text-medium-emphasis"> ({{ enrichedPool.product.rating.toFixed(1) }}) </span>
            </div>
            <div v-else class="pb-2">
              <span class="text-caption text-medium-emphasis">{{ $t('product_detail.no_reviews') }}</span>
            </div>
          </div>

          <div v-if="showUserRequest" class="mt-2 mb-4">
            <p class="font-weight-bold mt-2 text-uppercase mb-0" :class="getStatusClass(enrichedPool.pool.status)">
              {{ $t(getStatusText(enrichedPool.pool.status)) }}
            </p>
          </div>
        </v-col>

        <v-col v-if="showProductDetails" cols="4" class="d-flex align-center">
          <v-img :src="enrichedPool.product.imageUri || ''" :alt="enrichedPool.product.name || 'Product'" height="150" class="rounded" cover>
            <template v-slot:placeholder>
              <div class="d-flex align-center justify-center fill-height">
                <v-progress-circular indeterminate color="grey"></v-progress-circular>
              </div>
            </template>
          </v-img>
        </v-col>
      </v-row>

      <div v-if="showUserRequest && userRequest">
        <v-divider class="mt-0 mb-3"></v-divider>

        <h5 class="text-h6 mb-2">{{ $t('products_requested') }}: {{ userRequest.quantity }}</h5>

        <p class="text-body-1 mb-2">{{ $t('total') }}: {{ formatCurrency(enrichedPool.pool.price * userRequest.quantity) }}</p>

        <p class="font-weight-bold text-uppercase" :class="getRequestStatusClass(userRequest.status, enrichedPool.pool.status)">
          {{ $t(getRequestStatusText(userRequest, enrichedPool)) }}
        </p>
      </div>

      <div class="mt-2 mb-2">
        <v-progress-linear
          :model-value="acceptedPercentage"
          :color="getProgressColor(acceptedPercentage)"
          height="8"
          :rounded="false"
          :class="['mb-1 progress-container', progressBarClass]"
          :title="$t('pool_progress', { percent: acceptedPercentage.toFixed(1) })"
        >
          <template v-slot:default>
            <div :class="['bg-requests', pendingBarClass]" :style="{ width: pendingPercentage + '%', height: '100%', position: 'absolute', left: acceptedPercentage + '%' }"></div>
          </template>
        </v-progress-linear>

        <p class="text-center text-medium-emphasis ma-0 text-caption" :title="$t('pool_progress_legend')" style="user-select: none">
          {{ enrichedPool.pool.requestsStats.acceptedSum + enrichedPool.pool.requestsStats.deliveredSum }} / {{ enrichedPool.pool.requestsStats.pendingSum + enrichedPool.pool.requestsStats.acceptedSum + enrichedPool.pool.requestsStats.deliveredSum }} /
          {{ enrichedPool.pool.minQuantity }}
        </p>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import type { EnrichedPool } from '@/models';
import { RequestStatus } from '@/models/Request';
import { PoolStatus } from '@/models/Pool';
import { formatCurrency } from '@/utils/currency';

interface UserRequest {
  id: number;
  quantity: number;
  status: string;
  downPaymentUri?: string;
  finalPaymentUri?: string;
}

interface Props {
  enrichedPool: EnrichedPool;
  showProductDetails?: boolean;
  showUserRequest?: boolean;
  userRequest?: UserRequest;
}

const props = withDefaults(defineProps<Props>(), {
  showProductDetails: true,
  showUserRequest: false,
});

const router = useRouter();

const locationText = computed(() => {
  if (!props.enrichedPool.location || props.enrichedPool.location.id === null || props.enrichedPool.location.id === 1) {
    return 'No location';
  }
  return props.enrichedPool.location.name;
});

const acceptedPercentage = computed(() => {
  const stats = props.enrichedPool.pool.requestsStats;
  const total = Math.max(stats.pendingSum + stats.acceptedSum + stats.deliveredSum, props.enrichedPool.pool.minQuantity);
  return ((stats.acceptedSum + stats.deliveredSum) * 100) / total;
});

const pendingPercentage = computed(() => {
  const stats = props.enrichedPool.pool.requestsStats;
  const total = Math.max(stats.pendingSum + stats.acceptedSum + stats.deliveredSum, props.enrichedPool.pool.minQuantity);
  return (stats.pendingSum * 100) / total;
});

const hasAccepted = computed(() => acceptedPercentage.value > 0);
const hasPending = computed(() => pendingPercentage.value > 0);
const totalProgress = computed(() => acceptedPercentage.value + pendingPercentage.value);

const progressBarClass = computed(() => {
  const classes = [];

  if (hasAccepted.value && hasPending.value) {
    classes.push('progress-bar-left-rounded');
  } else if (hasAccepted.value && !hasPending.value) {
    if (totalProgress.value >= 100) {
      classes.push('progress-bar-fully-rounded');
    } else {
      classes.push('progress-bar-left-rounded');
    }
  }

  return classes.join(' ');
});

const pendingBarClass = computed(() => {
  const classes = [];

  if (hasPending.value) {
    if (hasAccepted.value) {
      if (totalProgress.value >= 100) {
        classes.push('pending-bar-right-rounded');
      } else {
        classes.push('pending-bar-flat');
      }
    } else {
      if (totalProgress.value >= 100) {
        classes.push('pending-bar-fully-rounded');
      } else {
        classes.push('pending-bar-left-rounded');
      }
    }
  }

  return classes.join(' ');
});

const getProgressColor = (percentage: number): string => {
  if (percentage < 15) return 'error';
  if (percentage < 50) return 'warning';
  return 'success';
};

const getStatusClass = (status: any): string => {
  switch (status?.name || status) {
    case 'CANCELLED':
      return 'text-error';
    case 'DELIVERING':
      return 'text-warning';
    case 'FINISHED':
      return 'text-medium-emphasis';
    case 'PAUSED':
      return 'text-info';
    case 'AVAILABLE':
      return 'text-success';
    default:
      return 'text-medium-emphasis';
  }
};

const getStatusText = (status: any): string => {
  switch (status?.name || status) {
    case 'CANCELLED':
      return 'pool_status.CANCELLED';
    case 'DELIVERING':
      return 'pool_status.DELIVERING';
    case 'FINISHED':
      return 'pool_status.FINISHED';
    case 'PAUSED':
      return 'pool_status.PAUSED';
    case 'AVAILABLE':
      return 'pool_status.AVAILABLE';
    default:
      return 'unknown';
  }
};

const getRequestStatusClass = (requestStatus: string, poolStatus: any): string => {
  if (requestStatus === 'PENDING') {
    if (!props.userRequest?.downPaymentUri && props.enrichedPool.pool.downPayment !== 0) {
      return 'text-medium-emphasis';
    }
    return 'text-medium-emphasis';
  }

  if (requestStatus === 'ACCEPTED') {
    if (poolStatus?.name === 'AVAILABLE') {
      return 'text-info';
    }
    if (poolStatus?.name === 'DELIVERING') {
      if (!props.userRequest?.finalPaymentUri) {
        return 'text-info';
      }
      return 'text-info';
    }
  }

  if (requestStatus === 'DELIVERED') {
    return 'text-success';
  }

  if (requestStatus === 'REJECTED') {
    return 'text-error';
  }

  return 'text-medium-emphasis';
};

const getRequestStatusText = (userRequest: UserRequest, enrichedPool: EnrichedPool): string => {
  const status = userRequest.status;

  if (status === RequestStatus.PENDING) {
    if (enrichedPool.pool.downPayment && !userRequest.downPaymentUri) {
      return 'pool_detail.status_pending_down_payment';
    }
    return 'pool_detail.status_pending';
  }

  if (status === RequestStatus.ACCEPTED) {
    if (enrichedPool.pool.status === PoolStatus.DELIVERING) {
      if (!userRequest.finalPaymentUri) {
        return 'pool_detail.status_pending_final_payment';
      }
      return 'pool_detail.status_not_delivered';
    }
    return 'pool_detail.status_accepted';
  }

  if (status === RequestStatus.DELIVERED) {
    return 'pool_detail.status_delivered';
  }

  if (status === RequestStatus.REJECTED) {
    return 'pool_detail.status_rejected';
  }

  return 'unknown';
};

const navigateToPool = () => {
  router.push(`/pools/${props.enrichedPool.pool.id}`);
};
</script>

<style scoped>
.pool-card {
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: rgb(var(--v-theme-surface));
  color: rgb(var(--v-theme-on-surface));
}

.pool-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15) !important;
}

.shadow-card {
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.logo-color {
  color: rgb(var(--v-theme-primary));
}

.text-truncate {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.bg-requests {
  background-color: rgba(var(--v-theme-info), 0.3);
  border-radius: 0;
  z-index: 1;
}

.pending-bar-fully-rounded {
  border-radius: 4px !important;
}

.pending-bar-left-rounded {
  border-radius: 4px 0 0 4px !important;
}

.pending-bar-right-rounded {
  border-radius: 0 4px 4px 0 !important;
}

.pending-bar-flat {
  border-radius: 0 !important;
}

.text-error {
  color: rgb(var(--v-theme-error)) !important;
}

.text-warning {
  color: rgb(var(--v-theme-warning)) !important;
}

.text-success {
  color: rgb(var(--v-theme-success)) !important;
}

.text-info {
  color: rgb(var(--v-theme-info)) !important;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.v-progress-linear {
  border-radius: 0px;
  overflow: hidden;
}

.progress-container {
  position: relative;
  border-radius: 4px;
}

.progress-container.progress-bar-fully-rounded {
  border-radius: 4px;
}

.progress-container.progress-bar-left-rounded {
  border-radius: 4px 0 0 4px;
}

.progress-container.progress-bar-fully-rounded :deep(.v-progress-linear__determinate) {
  border-radius: 4px;
}

.progress-container.progress-bar-left-rounded :deep(.v-progress-linear__determinate) {
  border-radius: 4px 0 0 4px;
}

.progress-container :deep(.v-progress-linear__background) {
  border-radius: 4px;
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
