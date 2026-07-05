<template>
  <v-container class="py-8 profile-container">
    <template v-if="isCompanyUser && user">
      <v-row class="profile-row">
        <v-col cols="12" md="6" class="profile-column">
          <v-card class="profile-card elevation-4" :class="{ 'mb-6': !isCompanyUser }">
            <v-card-title class="profile-header pa-6 text-center">
              <div class="profile-avatar mb-4">
                <v-avatar size="120" color="primary" class="elevation-6">
                  <span class="text-h3 text-white">
                    {{ getInitials(user) }}
                  </span>
                </v-avatar>
              </div>
              <div class="d-flex flex-column flex-sm-row align-center justify-center mb-2 gap-2">
                <h1 class="text-h4 font-weight-bold text-white mb-0 me-2">{{ user.firstName }} {{ user.lastName }}</h1>
                <v-chip :color="user.validated ? 'success' : 'warning'" size="small" variant="flat" class="text-white">
                  {{ user.validated ? $t('verified') : $t('pending_verification') }}
                </v-chip>
              </div>
            </v-card-title>

            <v-divider />

            <v-card-text class="pa-4">
              <v-list class="bg-transparent">
                <v-list-item class="info-item">
                  <template v-slot:prepend>
                    <v-icon color="primary" size="20">mdi-email</v-icon>
                  </template>
                  <v-list-item-title class="font-weight-medium text-body-2">{{ $t('email') }}</v-list-item-title>
                  <v-list-item-subtitle class="text-body-2">{{ user.email }}</v-list-item-subtitle>
                </v-list-item>

                <v-divider class="my-1" />

                <v-list-item v-if="user.firstName" class="info-item">
                  <template v-slot:prepend>
                    <v-icon color="primary" size="20">mdi-account</v-icon>
                  </template>
                  <v-list-item-title class="font-weight-medium text-body-2">{{ $t('name') }}</v-list-item-title>
                  <v-list-item-subtitle class="text-body-2">{{ user.firstName }} {{ user.lastName }}</v-list-item-subtitle>
                </v-list-item>

                <v-divider class="my-1" v-if="user.firstName" />

                <v-list-item v-if="authStore.preferredLocation" class="info-item">
                  <template v-slot:prepend>
                    <v-icon color="primary" size="20">mdi-map-marker</v-icon>
                  </template>
                  <v-list-item-title class="font-weight-medium text-body-2">{{ $t('preferred_location') }}</v-list-item-title>
                  <v-list-item-subtitle class="text-body-2">{{ authStore.preferredLocation.name }}</v-list-item-subtitle>
                </v-list-item>

                <v-divider class="my-1" v-if="authStore.preferredLocation" />

                <v-list-item v-if="user.admin" class="info-item">
                  <template v-slot:prepend>
                    <v-icon color="error" size="20">mdi-shield-crown</v-icon>
                  </template>
                  <v-list-item-title class="font-weight-medium text-body-2">{{ $t('role') }}</v-list-item-title>
                  <v-list-item-subtitle>
                    <v-chip color="error" size="small" variant="flat">{{ $t('administrator') }}</v-chip>
                  </v-list-item-subtitle>
                </v-list-item>
              </v-list>
            </v-card-text>

            <v-card-text v-if="isCurrentUser && user && !user.validated" class="pa-4 pt-0">
              <v-alert type="warning" variant="tonal" density="compact" class="mb-0">
                <template #prepend>
                  <v-icon size="24">mdi-shield-alert</v-icon>
                </template>
                <div class="d-flex flex-column flex-sm-row align-start align-sm-center justify-space-between ga-2">
                  <div>
                    <div class="text-body-2 font-weight-bold mb-1">{{ $t('account_validation_required') }}</div>
                    <div class="text-caption">{{ $t('validate_account_message') }}</div>
                  </div>
                  <v-btn color="warning" variant="flat" size="small" to="/validate-account" prepend-icon="mdi-check-decagram" class="text-none">{{ $t('validate_now') }}</v-btn>
                </div>
              </v-alert>
            </v-card-text>

            <v-card-actions class="pa-4 pt-0" v-if="isCurrentUser">
              <div class="w-100 d-flex flex-column ga-2">
                <v-btn color="primary" variant="flat" size="default" block prepend-icon="mdi-pencil" to="/profile/edit" class="text-none">{{ $t('edit_profile') }}</v-btn>
                <v-btn color="secondary" variant="flat" size="default" block prepend-icon="mdi-lock-reset" to="/profile/change-password" class="text-none">{{ $t('change_password') }}</v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </v-col>

        <v-col cols="12" md="6" class="profile-column">
          <v-card class="company-card elevation-4 mb-2">
            <v-card-title :class="`company-header ${companyStatusColor} pa-6 text-center`">
              <div class="company-avatar mb-4">
                <v-avatar v-if="currentCompany && companyImageUrl" size="120" class="elevation-6">
                  <v-img :src="companyImageUrl" :alt="currentCompany?.name || 'Company'" cover />
                </v-avatar>
                <v-avatar v-else :color="companyStatusColor" size="120" class="elevation-6">
                  <v-icon size="60" color="white">
                    {{ companyStatusIcon }}
                  </v-icon>
                </v-avatar>
              </div>
              <h1 class="text-h4 font-weight-bold text-white mb-2">
                {{ currentCompany?.name || $t('company_information') }}
              </h1>
              <v-chip :color="companyStatusColor" size="small" variant="flat" class="text-white">
                {{ $t(companyStatusChip) }}
              </v-chip>
            </v-card-title>

            <v-divider />

            <v-card-text class="pa-4">
              <div v-if="!currentCompany" class="text-center py-6">
                <v-icon size="48" color="grey-lighten-1" class="mb-3">mdi-domain-off</v-icon>
                <p class="text-body-2 mb-3">{{ $t(companyStatusMessage) }}</p>

                <template v-if="user && !user.validated">
                  <v-alert type="info" variant="tonal" density="compact" class="mb-0 text-start">
                    <div class="text-body-2">
                      {{ $t('validate_account_to_add_company') }}
                    </div>
                  </v-alert>
                </template>

                <v-btn v-else color="primary" variant="flat" size="default" to="/profile/companies/create" prepend-icon="mdi-plus-circle" class="text-none">{{ $t('add_company_information') }}</v-btn>
              </div>

              <template v-else>
                <v-list class="bg-transparent">
                  <v-list-item class="info-item">
                    <template v-slot:prepend>
                      <v-icon color="primary" size="20">mdi-domain</v-icon>
                    </template>
                    <v-list-item-title class="font-weight-medium text-body-2">{{ $t('company_name') }}</v-list-item-title>
                    <v-list-item-subtitle class="text-body-2">{{ currentCompany.name }}</v-list-item-subtitle>
                  </v-list-item>

                  <v-divider class="my-1" />

                  <v-list-item class="info-item">
                    <template v-slot:prepend>
                      <v-icon color="primary" size="20">mdi-email</v-icon>
                    </template>
                    <v-list-item-title class="font-weight-medium text-body-2">{{ $t('email') }}</v-list-item-title>
                    <v-list-item-subtitle class="text-body-2">{{ currentCompany.email }}</v-list-item-subtitle>
                  </v-list-item>

                  <v-divider class="my-1" />

                  <v-list-item class="info-item">
                    <template v-slot:prepend>
                      <v-icon color="primary" size="20">mdi-map-marker</v-icon>
                    </template>
                    <v-list-item-title class="font-weight-medium text-body-2">{{ $t('address') }}</v-list-item-title>
                    <v-list-item-subtitle class="text-body-2">{{ currentCompany.address }}</v-list-item-subtitle>
                  </v-list-item>

                  <v-divider class="my-1" v-if="currentCompany.phone || currentCompany.cbu" />

                  <v-list-item v-if="currentCompany.phone" class="info-item">
                    <template v-slot:prepend>
                      <v-icon color="primary" size="20">mdi-phone</v-icon>
                    </template>
                    <v-list-item-title class="font-weight-medium text-body-2">{{ $t('phone') }}</v-list-item-title>
                    <v-list-item-subtitle class="text-body-2">{{ currentCompany.phone }}</v-list-item-subtitle>
                  </v-list-item>

                  <v-divider class="my-1" v-if="currentCompany.phone && currentCompany.cbu" />

                  <v-list-item v-if="currentCompany.cbu" class="info-item">
                    <template v-slot:prepend>
                      <v-icon color="primary" size="20">mdi-bank</v-icon>
                    </template>
                    <v-list-item-title class="font-weight-medium text-body-2">{{ $t('cbu') }}</v-list-item-title>
                    <v-list-item-subtitle class="text-body-2">{{ currentCompany.cbu }}</v-list-item-subtitle>
                  </v-list-item>
                </v-list>
              </template>
            </v-card-text>

            <v-card-actions v-if="currentCompany" class="pa-4 pt-0">
              <div class="w-100 d-flex flex-column ga-2">
                <v-btn v-if="companyId" color="secondary" variant="flat" size="default" block :to="`/companies/${companyId}`" prepend-icon="mdi-eye" class="text-none">
                  {{ $t('view_company_details') }}
                </v-btn>
                <v-btn color="primary" variant="flat" size="default" block :to="`/profile/companies/edit`" prepend-icon="mdi-pencil" class="text-none">
                  {{ $t('edit_company_information') }}
                </v-btn>
              </div>
            </v-card-actions>
          </v-card>
        </v-col>
      </v-row>
    </template>

    <template v-else>
      <v-row justify="center">
        <v-col cols="12" md="8" lg="6">
          <v-card v-if="user" class="profile-card elevation-4">
            <v-card-title class="profile-header pa-6 text-center">
              <div class="profile-avatar mb-4">
                <v-avatar size="120" color="primary" class="elevation-6">
                  <span class="text-h3 text-white">
                    {{ getInitials(user) }}
                  </span>
                </v-avatar>
              </div>
              <div class="d-flex flex-column flex-sm-row align-center justify-center mb-2 gap-2">
                <h1 class="text-h4 font-weight-bold text-white mb-0 me-2">{{ user.firstName }} {{ user.lastName }}</h1>
                <v-chip :color="user.validated ? 'success' : 'warning'" size="small" variant="flat" class="text-white">
                  {{ user.validated ? $t('verified') : $t('pending_verification') }}
                </v-chip>
              </div>
            </v-card-title>

            <v-divider />

            <v-card-text class="pa-4">
              <v-row>
                <v-col cols="12" md="6">
                  <v-list class="bg-transparent">
                    <v-list-item class="info-item">
                      <template v-slot:prepend>
                        <v-icon color="primary" size="20">mdi-email</v-icon>
                      </template>
                      <v-list-item-title class="font-weight-medium text-body-2">{{ $t('email') }}</v-list-item-title>
                      <v-list-item-subtitle class="text-body-2">{{ user.email }}</v-list-item-subtitle>
                    </v-list-item>

                    <v-divider class="my-1" />

                    <v-list-item v-if="user.firstName" class="info-item">
                      <template v-slot:prepend>
                        <v-icon color="primary" size="20">mdi-account</v-icon>
                      </template>
                      <v-list-item-title class="font-weight-medium text-body-2">{{ $t('name') }}</v-list-item-title>
                      <v-list-item-subtitle class="text-body-2">{{ user.firstName }} {{ user.lastName }}</v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>

                <v-col cols="12" md="6">
                  <v-list class="bg-transparent">
                    <v-list-item v-if="authStore.preferredLocation" class="info-item">
                      <template v-slot:prepend>
                        <v-icon color="primary" size="20">mdi-map-marker</v-icon>
                      </template>
                      <v-list-item-title class="font-weight-medium text-body-2">{{ $t('preferred_location') }}</v-list-item-title>
                      <v-list-item-subtitle class="text-body-2">{{ authStore.preferredLocation.name }}</v-list-item-subtitle>
                    </v-list-item>

                    <v-divider class="my-1" v-if="authStore.preferredLocation" />

                    <v-list-item v-if="user.admin" class="info-item">
                      <template v-slot:prepend>
                        <v-icon color="error" size="20">mdi-shield-crown</v-icon>
                      </template>
                      <v-list-item-title class="font-weight-medium text-body-2">{{ $t('role') }}</v-list-item-title>
                      <v-list-item-subtitle>
                        <v-chip color="error" size="small" variant="flat">{{ $t('administrator') }}</v-chip>
                      </v-list-item-subtitle>
                    </v-list-item>
                  </v-list>
                </v-col>
              </v-row>
            </v-card-text>

            <v-card-text v-if="isCurrentUser && user && !user.validated" class="pa-4 pt-0">
              <v-alert type="warning" variant="tonal" density="compact" class="mb-0">
                <template #prepend>
                  <v-icon size="24">mdi-shield-alert</v-icon>
                </template>
                <div class="d-flex flex-column flex-sm-row align-start align-sm-center justify-space-between ga-2">
                  <div>
                    <div class="text-body-2 font-weight-bold mb-1">{{ $t('account_validation_required') }}</div>
                    <div class="text-caption">{{ $t('validate_account_message') }}</div>
                  </div>
                  <v-btn color="warning" variant="flat" size="small" to="/validate-account" prepend-icon="mdi-check-decagram" class="text-none">{{ $t('validate_now') }}</v-btn>
                </div>
              </v-alert>
            </v-card-text>

            <v-card-actions class="pa-4 pt-0" v-if="isCurrentUser">
              <div class="w-100 d-flex flex-column ga-2">
                <v-btn color="primary" variant="flat" size="default" block prepend-icon="mdi-pencil" to="/profile/edit" class="text-none">{{ $t('edit_profile') }}</v-btn>
                <v-btn color="secondary" variant="flat" size="default" block prepend-icon="mdi-lock-reset" to="/profile/change-password" class="text-none">{{ $t('change_password') }}</v-btn>
              </div>
            </v-card-actions>
          </v-card>

          <v-card v-else-if="isLoading" class="elevation-4">
            <v-card-text class="text-center py-12">
              <v-progress-circular indeterminate color="primary" size="64" />
              <p class="mt-4 text-body-1">{{ $t('loading_user_profile') }}</p>
            </v-card-text>
          </v-card>

          <v-card v-else class="elevation-4">
            <v-card-text class="text-center py-12">
              <v-icon size="64" color="error" class="mb-4">mdi-alert-circle</v-icon>
              <p class="text-h6 text-error mb-2">{{ $t('user_not_found') }}</p>
              <p class="text-body-2 text-medium-emphasis">{{ $t('user_not_found_message') }}</p>
              <v-btn color="primary" variant="flat" class="mt-4 text-none" to="/">{{ $t('go_home') }}</v-btn>
            </v-card-text>
          </v-card>
        </v-col>
      </v-row>
    </template>
  </v-container>
</template>

<style scoped>
.profile-container {
  max-width: 1200px;
}

.profile-row {
  margin: 0;
}

.profile-column {
  padding: 0 12px;
}

.profile-card,
.company-card {
  width: 100%;
  max-width: 100%;
  box-sizing: border-box;
  overflow: hidden;
}

.profile-card {
  border-radius: 16px;
  display: flex;
  flex-direction: column;
}

.company-card {
  border-radius: 16px;
  display: flex;
  flex-direction: column;
}

.profile-column {
  max-width: 100%;
  overflow: hidden;
}

.profile-header,
.company-header {
  background: linear-gradient(135deg, rgb(var(--v-theme-primary)) 0%, rgb(var(--v-theme-secondary)) 100%);
  position: relative;
  overflow: hidden;
}

.profile-header::before,
.company-header::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: radial-gradient(circle at 30% 70%, rgba(255, 255, 255, 0.15) 0%, transparent 50%), radial-gradient(circle at 70% 30%, rgba(255, 255, 255, 0.15) 0%, transparent 50%);
  pointer-events: none;
}

.company-header.info {
  background: linear-gradient(135deg, #2196f3 0%, #1976d2 100%);
}

.company-header.warning {
  background: linear-gradient(135deg, #ff9800 0%, #f57c00 100%);
}

.company-header.success {
  background: linear-gradient(135deg, #4caf50 0%, #388e3c 100%);
}

.profile-avatar,
.company-avatar {
  position: relative;
  z-index: 2;
}

.info-item {
  min-height: auto;
  padding: 0;
  margin: 0;
}

.info-item :deep(.v-list-item__prepend) {
  margin-inline-end: 12px;
}

.info-item :deep(.v-list-item-title) {
  margin-bottom: 2px;
}

.v-list-item {
  min-height: auto;
  padding: 4px 0;
}

.v-theme--dark .profile-card {
  background: #1e1e1e !important;
}

@media (max-width: 960px) {
  .profile-column {
    padding: 0;
    margin-bottom: 24px;
  }

  .profile-row {
    gap: 0;
  }
}
</style>

<script setup lang="ts">
import { computed, onMounted } from 'vue';
import { useAuthStore } from '@/stores/auth';
import type { User } from '@/models';

definePage({
  meta: {
    requiresAuth: true,
  },
});

const authStore = useAuthStore();

const user = computed(() => authStore.currentUser);
const isLoading = computed(() => authStore.isLoading);
const currentCompany = computed(() => authStore.currentCompany);

const isCurrentUser = computed(() => true);
const isCompanyUser = computed(() => authStore.isCompany);

const companyId = computed(() => {
  if (currentCompany.value?.id) {
    return currentCompany.value.id;
  }

  if (user.value?.companyUri) {
    const match = user.value.companyUri.match(/\/(\d+)(\/)?$/);
    return match ? Number(match[1]) : null;
  }
  return null;
});

const companyStatusColor = computed(() => {
  if (!currentCompany.value) {
    return 'info';
  }
  if (!currentCompany.value.validated) {
    return 'warning';
  }
  return 'success';
});

const companyStatusIcon = computed(() => {
  if (!currentCompany.value) {
    return 'mdi-information';
  }
  if (!currentCompany.value.validated) {
    return 'mdi-clock-alert';
  }
  return 'mdi-check-circle';
});

const companyStatusMessage = computed(() => {
  if (!currentCompany.value) {
    return 'company_info_not_added';
  }
  if (!currentCompany.value.validated) {
    return 'company_pending_verification';
  }
  return 'company_verified_active';
});

const companyStatusChip = computed(() => {
  if (!currentCompany.value) {
    return 'info_pending';
  }
  if (!currentCompany.value.validated) {
    return 'pending_verification';
  }
  return 'verified';
});

const companyImageUrl = computed(() => {
  return currentCompany.value?.imageUri || null;
});

const getInitials = (user: User): string => {
  if (user.firstName && user.lastName) {
    return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
  }
  if (user.firstName) {
    const name = user.firstName || '';
    return name.substring(0, 2).toUpperCase();
  }
  if (user.email) {
    return user.email.substring(0, 2).toUpperCase();
  }
  return 'U';
};

onMounted(async () => {
  if (authStore.isAuthenticated && authStore.userId && authStore.role) {
    try {
      await authStore.fetchUserCompanyAndLocation(authStore.userId, authStore.role);
    } catch (error) {
      console.error('Failed to fetch user and company data on profile page:', error);
    }
  }
});
</script>
