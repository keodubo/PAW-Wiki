<template>
  <v-dialog v-model="dialog" max-width="550">
    <v-card class="join-dialog-card elevation-12 rounded-xl">
      <v-card-title class="join-dialog-header pa-6 text-center">
        <div class="header-section">
          <v-icon size="56" color="white" class="mb-2">mdi-account-cancel</v-icon>
          <h2 class="text-h5 font-weight-bold text-white mb-1">
            {{ $t('admin.block_user') }}
          </h2>
        </div>
      </v-card-title>

      <v-card-text class="pa-6">
        <v-alert type="warning" variant="tonal" density="comfortable" prepend-icon="mdi-alert-octagon" class="mb-4 block-warning">
          <div class="alert-heading">{{ $t('admin.block_dialog_warning_title') }}</div>
          <div class="alert-body">{{ $t('admin.block_user_confirmation') }}</div>
        </v-alert>

        <div v-if="user" class="user-info-card pa-5 rounded-xl">
          <div class="section-title">{{ $t('admin.block_dialog_user_section') }}</div>
          <div class="user-meta-grid mt-3">
            <div v-for="entry in userMeta" :key="entry.key" class="meta-item">
              <div class="meta-label">{{ entry.label }}</div>
              <div class="meta-value">{{ entry.value }}</div>
            </div>
          </div>

          <v-divider class="my-5" />

          <div class="section-title">{{ $t('admin.block_dialog_status_section') }}</div>
          <div class="status-chip-list mt-3">
            <div v-for="chip in statusChips" :key="chip.key" class="status-chip">
              <v-chip :color="chip.color" size="small" variant="flat" class="status-chip__chip">
                <v-icon start size="small">{{ chip.icon }}</v-icon>
                {{ chip.label }}
              </v-chip>
              <div v-if="chip.description" class="chip-description text-medium-emphasis">
                {{ chip.description }}
              </div>
            </div>
          </div>

          <div v-if="reportsInsight" class="mt-5">
            <div class="section-title mb-2">{{ $t('admin.block_dialog_reports_section') }}</div>
            <v-alert :type="reportsInsight.type" :variant="reportsInsight.variant" :density="reportsInsight.density" class="reports-alert" :icon="reportsInsight.icon">
              {{ reportsInsight.text }}
            </v-alert>
          </div>
        </div>
      </v-card-text>

      <v-card-actions class="px-6 pb-4">
        <v-spacer />
        <v-btn variant="outlined" color="grey" size="large" @click="close" :disabled="isBlocking" class="mr-2">
          <template #prepend>
            <v-icon>mdi-arrow-left</v-icon>
          </template>
          {{ $t('back') }}
        </v-btn>
        <v-btn color="error" variant="flat" size="large" @click="confirmBlock" :loading="isBlocking" class="dialog-action-btn">
          <template #prepend v-if="!isBlocking">
            <v-icon>mdi-account-cancel</v-icon>
          </template>
          {{ $t('admin.block') }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script setup lang="ts">
import { ref, watch, computed } from 'vue';
import { useI18n } from 'vue-i18n';
import type { EnrichedUser } from '@/models';

interface Props {
  modelValue: boolean;
  user: EnrichedUser | null;
}

interface Emits {
  (e: 'update:modelValue', value: boolean): void;
  (e: 'confirm', userId: number): void;
}

const props = defineProps<Props>();
const emit = defineEmits<Emits>();
const { t, locale } = useI18n();

const dialog = ref(props.modelValue);
const isBlocking = ref(false);
const user = computed(() => props.user);

const formatDateTime = (dateString: string): string => {
  try {
    const targetDate = new Date(dateString);
    if (Number.isNaN(targetDate.getTime())) {
      return dateString;
    }

    const localeTag = locale.value.startsWith('es') ? 'es-AR' : 'en-US';
    const formatter = new Intl.DateTimeFormat(localeTag, {
      dateStyle: 'medium',
      timeStyle: 'short',
    });

    return formatter.format(targetDate);
  } catch (error) {
    return dateString;
  }
};

interface MetaEntry {
  key: string;
  label: string;
  value: string;
}

interface StatusChip {
  key: string;
  color: string;
  icon: string;
  label: string;
  description?: string;
}

interface ReportsInsight {
  type: 'success' | 'warning';
  variant: 'tonal';
  density: 'comfortable' | 'compact';
  icon: string;
  text: string;
}

const buildFullName = (firstName?: string, lastName?: string): string | null => {
  const fullName = [firstName, lastName].filter(Boolean).join(' ').trim();
  return fullName.length > 0 ? fullName : null;
};

const userMeta = computed<MetaEntry[]>(() => {
  if (!user.value) {
    return [];
  }

  const entries: MetaEntry[] = [
    { key: 'id', label: t('admin.block_dialog_user_id'), value: `#${user.value.user.id}` },
    { key: 'email', label: t('email'), value: user.value.user.email },
  ];

  const fullName = buildFullName(user.value.user.firstName, user.value.user.lastName);
  if (fullName) {
    entries.push({ key: 'name', label: t('name'), value: fullName });
  }

  if (user.value.company?.name) {
    entries.push({ key: 'company', label: t('admin.company_name'), value: user.value.company.name });
  }

  entries.push({ key: 'blockLevel', label: t('admin.block_level'), value: String(user.value.user.blockLevel ?? 0) });

  if (user.value.user.blockedUntil) {
    entries.push({ key: 'blockedUntil', label: t('admin.block_until'), value: formatDateTime(user.value.user.blockedUntil) });
  }

  return entries;
});

const statusChips = computed<StatusChip[]>(() => {
  if (!user.value) {
    return [];
  }

  const chips: StatusChip[] = [];
  const { validated, blockLevel = 0, blockedUntil } = user.value.user;

  chips.push({
    key: 'validation',
    color: validated ? 'success' : 'warning',
    icon: validated ? 'mdi-shield-check' : 'mdi-alert-circle',
    label: validated ? t('admin.validated') : t('admin.not_validated'),
  });

  if (blockLevel === 0) {
    chips.push({
      key: 'block-status',
      color: 'success',
      icon: 'mdi-account-check',
      label: t('admin.not_blocked'),
    });
  } else if (blockLevel >= 4) {
    chips.push({
      key: 'block-status',
      color: 'error',
      icon: 'mdi-lock-alert',
      label: t('admin.blocked_permanently'),
    });
  } else {
    const description = blockedUntil ? t('admin.block_dialog_block_until', { date: formatDateTime(blockedUntil) }) : undefined;

    chips.push({
      key: 'block-status',
      color: 'warning',
      icon: 'mdi-shield-alert',
      label: `${t('admin.block_level')} ${blockLevel}`,
      description,
    });
  }

  return chips;
});

const reportsInsight = computed<ReportsInsight | null>(() => {
  if (!user.value) {
    return null;
  }

  if (user.value.user.hasReports) {
    return {
      type: 'warning',
      variant: 'tonal',
      density: 'comfortable',
      icon: 'mdi-alert-decagram',
      text: t('admin.block_dialog_reports_flagged'),
    };
  }

  return {
    type: 'success',
    variant: 'tonal',
    density: 'compact',
    icon: 'mdi-shield-check',
    text: t('admin.block_dialog_reports_clean'),
  };
});

watch(
  () => props.modelValue,
  (newValue) => {
    dialog.value = newValue;
  },
);

watch(dialog, (newValue) => {
  emit('update:modelValue', newValue);
});

const close = () => {
  if (!isBlocking.value) {
    dialog.value = false;
  }
};

const confirmBlock = async () => {
  if (!props.user) return;

  isBlocking.value = true;
  emit('confirm', props.user.user.id);
};

watch(dialog, (newValue) => {
  if (!newValue) {
    isBlocking.value = false;
  }
});
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

.block-warning {
  border-left: 4px solid rgba(var(--v-theme-warning), 0.6);
}

.alert-heading {
  font-weight: 600;
  font-size: 1rem;
  margin-bottom: 0.25rem;
}

.alert-body {
  font-size: 0.9rem;
}

.user-info-card {
  background: rgba(var(--v-theme-surface), 0.7);
  border: 1px solid rgba(var(--v-theme-on-surface), 0.08);
}

.user-meta-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 1rem;
}

.meta-item {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.meta-label {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.05em;
  text-transform: uppercase;
  color: rgba(var(--v-theme-on-surface), 0.55);
}

.meta-value {
  font-size: 0.95rem;
  font-weight: 600;
  color: rgb(var(--v-theme-on-surface));
}

.section-title {
  font-size: 0.75rem;
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  color: rgba(var(--v-theme-on-surface), 0.6);
}

.status-chip-list {
  display: flex;
  flex-wrap: wrap;
  gap: 0.75rem;
}

.status-chip {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
}

.status-chip__chip {
  border-radius: 999px;
  font-weight: 600;
}

.chip-description {
  font-size: 0.75rem;
}

.text-medium-emphasis {
  color: rgba(var(--v-theme-on-surface), 0.6) !important;
}

.reports-alert {
  border-radius: 12px;
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

.v-theme--dark .join-dialog-card {
  background: #1e1e1e !important;
  border: 1px solid rgba(255, 255, 255, 0.12) !important;
}

.v-theme--dark .user-info-card {
  background: rgba(30, 30, 30, 0.9);
  border-color: rgba(255, 255, 255, 0.12);
}
</style>
