<template>
  <div>
    <div v-if="!user.isBlocked">
      <div v-if="user.blockLevel === 0" class="text-success text-body-2">
        {{ t('pool_detail.never_been_blocked') }}
      </div>
      <div v-else :class="user.blockLevel === 3 ? 'text-warning' : 'text-info'" class="text-body-2">
        {{ t('pool_detail.times_blocked', { count: user.blockLevel.toString() }) }}
      </div>
    </div>
    <div v-else>
      <div v-if="user.blockLevel === 4" class="text-error text-body-2 font-weight-bold">
        {{ t('pool_detail.blocked_permanently') }}
      </div>
      <div v-else class="text-error text-body-2 font-weight-bold">
        {{ t('pool_detail.currently_blocked') }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n';
import type { User } from '@/models';

interface Props {
  user: User;
}

defineProps<Props>();
const { t } = useI18n();
</script>
