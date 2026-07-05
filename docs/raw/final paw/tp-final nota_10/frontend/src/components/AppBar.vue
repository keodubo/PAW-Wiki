<template>
  <v-app-bar color="primary" dark elevation="2" height="64" class="px-4">
    <router-link to="/" class="text-decoration-none d-flex align-center navbar-brand ml-2 mr-5">
      <div class="logo-container">
        <v-img src="@/assets/logo.png" alt="logo" width="30" height="30" class="shake" />
      </div>
      <div class="text-h6 font-weight-bold text-white ml-2" style="white-space: nowrap; min-width: max-content">
        {{ $t('grupi') }}
      </div>
    </router-link>

    <v-spacer />

    <div class="d-none d-md-flex align-center w-100 justify-space-between">
      <div class="d-flex align-center">
        <v-btn variant="text" class="mx-1 nav-link-btn" to="/pools">
          <v-icon start>mdi-gavel</v-icon>
          {{ $t('navbar.pools') }}
        </v-btn>
        <v-btn variant="text" class="mx-1 nav-link-btn" to="/products">
          <v-icon start>mdi-package-variant</v-icon>
          {{ $t('navbar.products') }}
        </v-btn>
      </div>

      <div class="d-flex align-center">
        <v-btn v-if="authStore.isUser && !authStore.isCompany && authStore.currentUser?.validated" variant="text" class="mx-1 nav-link-btn" to="/my-requests">
          <v-icon start>mdi-hand-pointing-up</v-icon>
          {{ $t('navbar.user_requests') }}
        </v-btn>

        <template v-if="authStore.isCompany && authStore.currentCompany && authStore.currentCompany.validated">
          <v-btn variant="text" class="mx-1 nav-link-btn" to="/my-pools">
            <v-icon start>mdi-gavel</v-icon>
            {{ $t('navbar.company_pools') }}
          </v-btn>
          <v-btn variant="text" class="mx-1 nav-link-btn" to="/my-products">
            <v-icon start>mdi-package-variant</v-icon>
            {{ $t('navbar.company_products') }}
          </v-btn>
        </template>

        <template v-if="authStore.isAdmin">
          <v-btn variant="text" class="mx-1 nav-link-btn" to="/users">
            <v-icon start>mdi-account-group</v-icon>
            {{ $t('navbar.users') }}
          </v-btn>
          <v-btn variant="text" class="mx-1 nav-link-btn" to="/companies">
            <v-icon start>mdi-office-building</v-icon>
            {{ $t('navbar.companies') }}
          </v-btn>
        </template>

        <v-menu offset-y>
          <template #activator="{ props }">
            <v-btn v-bind="props" variant="text" icon class="mx-1" :title="$t('language')">
              <v-icon>mdi-translate</v-icon>
            </v-btn>
          </template>
          <v-list>
            <v-list-item @click="changeLocale('en')" :class="{ 'bg-primary': currentLocale === 'en' }">
              <template #prepend>
                <div style="display: flex; align-items: center; justify-content: center; width: 24px; min-width: 0; margin-right: 8px">
                  <v-icon :color="currentLocale === 'en' ? 'white' : ''">
                    {{ currentLocale === 'en' ? 'mdi-check' : '' }}
                  </v-icon>
                </div>
              </template>
              <v-list-item-title :class="{ 'text-white': currentLocale === 'en' }">
                {{ $t('language.english') }}
              </v-list-item-title>
            </v-list-item>
            <v-list-item @click="changeLocale('es')" :class="{ 'bg-primary': currentLocale === 'es' }">
              <template #prepend>
                <div style="display: flex; align-items: center; justify-content: center; width: 24px; min-width: 0; margin-right: 8px">
                  <v-icon :color="currentLocale === 'es' ? 'white' : ''">
                    {{ currentLocale === 'es' ? 'mdi-check' : '' }}
                  </v-icon>
                </div>
              </template>
              <v-list-item-title :class="{ 'text-white': currentLocale === 'es' }">
                {{ $t('language.spanish') }}
              </v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>

        <v-btn variant="text" icon class="mx-1" @click="toggleTheme" :title="getThemeTooltip()">
          <v-icon>{{ getThemeIcon() }}</v-icon>
        </v-btn>

        <template v-if="authStore.isAuthenticated && authStore.currentUser">
          <v-menu offset-y>
            <template #activator="{ props }">
              <v-btn v-bind="props" variant="text" class="ml-2 user-menu-btn">
                <v-avatar size="32" color="secondary" class="mr-2">
                  <span class="text-caption font-weight-bold">
                    {{ getUserInitials() }}
                  </span>
                </v-avatar>
                <div class="d-flex flex-column align-start">
                  <span class="text-body-2 font-weight-medium text-white">
                    {{ getUserDisplayName() }}
                  </span>
                  <div v-if="!authStore.currentUser.validated || (authStore.isCompany && (!authStore.currentCompany || !authStore.currentCompany.validated))" class="d-flex flex-column ga-1 mt-1">
                    <v-chip v-if="!authStore.currentUser.validated" color="error" size="x-small" density="compact" variant="flat" prepend-icon="mdi-alert-circle" class="status-chip">
                      {{ $t('not_validated') }}
                    </v-chip>

                    <v-chip v-else-if="authStore.isCompany && !authStore.currentCompany" color="info" size="x-small" density="compact" variant="flat" prepend-icon="mdi-information" class="status-chip">
                      {{ $t('add_company') }}
                    </v-chip>

                    <v-chip
                      v-else-if="authStore.isCompany && authStore.currentCompany && !authStore.currentCompany.validated"
                      color="warning"
                      size="x-small"
                      density="compact"
                      variant="flat"
                      prepend-icon="mdi-clock-alert"
                      class="status-chip"
                    >
                      {{ $t('pending') }}
                    </v-chip>
                  </div>
                </div>
                <v-icon end size="small" class="ml-1">mdi-chevron-down</v-icon>
              </v-btn>
            </template>

            <v-list class="user-dropdown-menu">
              <v-list-item class="user-info-header">
                <template #prepend>
                  <v-avatar size="40" color="secondary">
                    <span class="text-body-1 font-weight-bold">
                      {{ getUserInitials() }}
                    </span>
                  </v-avatar>
                </template>
                <v-list-item-title class="font-weight-bold">
                  {{ getUserDisplayName() }}
                </v-list-item-title>
                <v-list-item-subtitle>
                  {{ authStore.currentUser.email }}
                </v-list-item-subtitle>
              </v-list-item>

              <v-divider />

              <v-list-item v-if="!authStore.currentUser.validated" class="validation-warning" @click="goToValidateAccount">
                <template #prepend>
                  <v-icon color="error" size="22">mdi-alert-circle</v-icon>
                </template>
                <v-list-item-title class="text-error font-weight-bold">
                  {{ $t('validate_account') }}
                </v-list-item-title>
              </v-list-item>

              <v-list-item v-else-if="authStore.isCompany && !authStore.currentCompany" class="info-warning" :to="'/profile/companies/create'">
                <template #prepend>
                  <v-icon color="info" size="22">mdi-information</v-icon>
                </template>
                <v-list-item-title class="text-info font-weight-bold">
                  {{ $t('add_company') }}
                </v-list-item-title>
              </v-list-item>

              <v-list-item v-else-if="authStore.isCompany && authStore.currentCompany && !authStore.currentCompany.validated" class="validation-warning" disabled>
                <template #prepend>
                  <v-icon color="warning" size="22">mdi-clock-alert</v-icon>
                </template>
                <v-list-item-title class="text-warning font-weight-bold">
                  {{ $t('pending_verification') }}
                </v-list-item-title>
              </v-list-item>

              <v-divider v-if="!authStore.currentUser.validated || (authStore.isCompany && (!authStore.currentCompany || !authStore.currentCompany.validated))" />

              <v-list-item to="/profile" class="menu-item">
                <template #prepend>
                  <v-icon color="primary" size="22">mdi-account-circle</v-icon>
                </template>
                <v-list-item-title class="font-weight-medium">{{ $t('profile') }}</v-list-item-title>
              </v-list-item>

              <v-divider />

              <v-list-item @click="handleLogout" class="menu-item logout-item">
                <template #prepend>
                  <v-icon color="error" size="22">mdi-logout</v-icon>
                </template>
                <v-list-item-title class="font-weight-medium text-error">{{ $t('logout') }}</v-list-item-title>
              </v-list-item>
            </v-list>
          </v-menu>
        </template>

        <template v-else>
          <v-btn variant="text" to="/login" class="mx-1 nav-link-btn">
            <v-icon start>mdi-login</v-icon>
            {{ $t('login') }}
          </v-btn>
        </template>
      </div>
    </div>

    <div class="d-flex d-md-none align-center">
      <v-menu offset-y>
        <template #activator="{ props }">
          <v-btn v-bind="props" variant="text" icon class="mx-1" :title="$t('language')">
            <v-icon>mdi-translate</v-icon>
          </v-btn>
        </template>
        <v-list>
          <v-list-item @click="changeLocale('en')" :class="{ 'bg-primary': currentLocale === 'en' }">
            <template #prepend>
              <div style="display: flex; align-items: center; justify-content: center; width: 24px; min-width: 0; margin-right: 8px">
                <v-icon :color="currentLocale === 'en' ? 'white' : ''">
                  {{ currentLocale === 'en' ? 'mdi-check' : '' }}
                </v-icon>
              </div>
            </template>
            <v-list-item-title :class="{ 'text-white': currentLocale === 'en' }">
              {{ $t('language.english') }}
            </v-list-item-title>
          </v-list-item>
          <v-list-item @click="changeLocale('es')" :class="{ 'bg-primary': currentLocale === 'es' }">
            <template #prepend>
              <div style="display: flex; align-items: center; justify-content: center; width: 24px; min-width: 0; margin-right: 8px">
                <v-icon :color="currentLocale === 'es' ? 'white' : ''">
                  {{ currentLocale === 'es' ? 'mdi-check' : '' }}
                </v-icon>
              </div>
            </template>
            <v-list-item-title :class="{ 'text-white': currentLocale === 'es' }">
              {{ $t('language.spanish') }}
            </v-list-item-title>
          </v-list-item>
        </v-list>
      </v-menu>

      <v-btn variant="text" icon class="mx-1" @click="toggleTheme" :title="getThemeTooltip()">
        <v-icon>{{ getThemeIcon() }}</v-icon>
      </v-btn>

      <template v-if="authStore.isAuthenticated && authStore.currentUser">
        <v-menu offset-y>
          <template #activator="{ props }">
            <v-btn v-bind="props" variant="text" icon class="ml-1">
              <v-avatar size="32" color="secondary">
                <span class="text-caption font-weight-bold">
                  {{ getUserInitials() }}
                </span>
              </v-avatar>
            </v-btn>
          </template>

          <v-list class="user-dropdown-menu">
            <v-list-item class="user-info-header">
              <template #prepend>
                <v-avatar size="40" color="secondary">
                  <span class="text-body-1 font-weight-bold">
                    {{ getUserInitials() }}
                  </span>
                </v-avatar>
              </template>
              <v-list-item-title class="font-weight-bold">
                {{ getUserDisplayName() }}
              </v-list-item-title>
              <v-list-item-subtitle>
                {{ authStore.currentUser.email }}
              </v-list-item-subtitle>
            </v-list-item>

            <v-divider />

            <v-list-item v-if="!authStore.currentUser.validated" class="validation-warning" @click="goToValidateAccount">
              <template #prepend>
                <v-icon color="error" size="22">mdi-alert-circle</v-icon>
              </template>
              <v-list-item-title class="text-error font-weight-bold">
                {{ $t('validate_account') }}
              </v-list-item-title>
            </v-list-item>

            <v-list-item v-else-if="authStore.isCompany && !authStore.currentCompany" class="info-warning" :to="'/profile/companies/create'">
              <template #prepend>
                <v-icon color="info" size="22">mdi-information</v-icon>
              </template>
              <v-list-item-title class="text-info font-weight-bold">
                {{ $t('add_company') }}
              </v-list-item-title>
            </v-list-item>

            <v-list-item v-else-if="authStore.isCompany && authStore.currentCompany && !authStore.currentCompany.validated" class="validation-warning" disabled>
              <template #prepend>
                <v-icon color="warning" size="22">mdi-clock-alert</v-icon>
              </template>
              <v-list-item-title class="text-warning font-weight-bold">
                {{ $t('pending_verification') }}
              </v-list-item-title>
            </v-list-item>

            <v-divider v-if="!authStore.currentUser.validated || (authStore.isCompany && (!authStore.currentCompany || !authStore.currentCompany.validated))" />

            <v-list-item to="/profile" class="menu-item">
              <template #prepend>
                <v-icon color="primary" size="22">mdi-account-circle</v-icon>
              </template>
              <v-list-item-title class="font-weight-medium">{{ $t('profile') }}</v-list-item-title>
            </v-list-item>

            <v-divider />

            <v-list-item @click="handleLogout" class="menu-item logout-item">
              <template #prepend>
                <v-icon color="error" size="22">mdi-logout</v-icon>
              </template>
              <v-list-item-title class="font-weight-medium text-error">{{ $t('logout') }}</v-list-item-title>
            </v-list-item>
          </v-list>
        </v-menu>
      </template>

      <template v-else>
        <v-btn variant="text" to="/login" icon class="mx-1">
          <v-icon>mdi-login</v-icon>
        </v-btn>
      </template>

      <v-menu offset-y>
        <template #activator="{ props }">
          <v-btn v-bind="props" variant="text" icon class="ml-1">
            <v-icon>mdi-menu</v-icon>
          </v-btn>
        </template>

        <v-list>
          <v-list-item to="/pools">
            <template #prepend>
              <v-icon>mdi-gavel</v-icon>
            </template>
            <v-list-item-title>{{ $t('navbar.pools') }}</v-list-item-title>
          </v-list-item>

          <v-list-item to="/products">
            <template #prepend>
              <v-icon>mdi-package-variant</v-icon>
            </template>
            <v-list-item-title>{{ $t('navbar.products') }}</v-list-item-title>
          </v-list-item>

          <v-list-item v-if="authStore.isUser && !authStore.isCompany && authStore.currentUser?.validated" to="/my-requests">
            <template #prepend>
              <v-icon>mdi-hand-pointing-up</v-icon>
            </template>
            <v-list-item-title>{{ $t('navbar.user_requests') }}</v-list-item-title>
          </v-list-item>

          <template v-if="authStore.isCompany && authStore.currentCompany && authStore.currentCompany.validated">
            <v-list-item to="/my-pools">
              <template #prepend>
                <v-icon>mdi-gavel</v-icon>
              </template>
              <v-list-item-title>{{ $t('navbar.company_pools') }}</v-list-item-title>
            </v-list-item>

            <v-list-item to="/my-products">
              <template #prepend>
                <v-icon>mdi-package-variant</v-icon>
              </template>
              <v-list-item-title>{{ $t('navbar.company_products') }}</v-list-item-title>
            </v-list-item>
          </template>

          <template v-if="authStore.isAdmin">
            <v-list-item to="/users">
              <template #prepend>
                <v-icon>mdi-account-group</v-icon>
              </template>
              <v-list-item-title>{{ $t('navbar.users') }}</v-list-item-title>
            </v-list-item>

            <v-list-item to="/companies">
              <template #prepend>
                <v-icon>mdi-office-building</v-icon>
              </template>
              <v-list-item-title>{{ $t('navbar.companies') }}</v-list-item-title>
            </v-list-item>
          </template>
        </v-list>
      </v-menu>
    </div>
  </v-app-bar>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue';
import { useI18n } from 'vue-i18n';
import { useAuthStore } from '@/stores/auth';
import { useRouter } from 'vue-router';
import { useTheme } from '@/composables/useTheme';
import { useTheme as useVuetifyTheme } from 'vuetify';
import { setLocale, type Locale } from '@/i18n';
import { userService } from '@/services/UserService';

const authStore = useAuthStore();
const router = useRouter();
const { toggleTheme, getCurrentTheme } = useTheme();
const vuetifyTheme = useVuetifyTheme();
const { locale } = useI18n();

const currentLocale = computed<Locale>(() => locale.value as Locale);

const changeLocale = async (newLocale: Locale) => {
  if (locale.value === newLocale) return;

  setLocale(newLocale);

  if (authStore.isAuthenticated && authStore.currentUser) {
    try {
      await userService.updatePreferredLanguage(authStore.currentUser.id, newLocale);
      authStore.setUser({ ...authStore.currentUser, preferredLanguage: newLocale });
    } catch (err) {
      console.error('Failed to update preferred language', err);
    }
  }
};

const getThemeIcon = () => {
  const currentTheme = vuetifyTheme.global.name.value;
  return currentTheme === 'dark' ? 'mdi-weather-sunny' : 'mdi-weather-night';
};

const getThemeTooltip = () => {
  const currentTheme = vuetifyTheme.global.name.value;
  const themeInfo = getCurrentTheme();

  if (themeInfo.isAuto) {
    return `Auto (${currentTheme === 'dark' ? 'Dark' : 'Light'}) - Click to override`;
  }
  return `Switch to ${currentTheme === 'dark' ? 'Light' : 'Dark'} theme`;
};

const handleLogout = async () => {
  await authStore.logout();
};

const getUserDisplayName = () => {
  if (!authStore.currentUser) return 'User';
  return `${authStore.currentUser.firstName} ${authStore.currentUser.lastName}`.trim() || authStore.currentUser.email?.split('@')[0] || 'User';
};

const getUserInitials = () => {
  if (!authStore.currentUser) return 'U';

  const user = authStore.currentUser;
  if (user.firstName && user.lastName) {
    return `${user.firstName[0]}${user.lastName[0]}`.toUpperCase();
  }
  if (user.firstName || user.lastName) {
    const name = user.firstName || user.lastName || '';
    return name.substring(0, 2).toUpperCase();
  }
  if (user.email) {
    return user.email.substring(0, 2).toUpperCase();
  }
  return 'U';
};

const goToValidateAccount = () => {
  router.push('/validate-account');
};
</script>

<style scoped>
.navbar-brand {
  transition: transform 0.1s;
  text-decoration: none !important;
  overflow: visible;
}

.navbar-brand:hover {
  transform: scale(1.1);
}

.logo-container {
  display: flex;
  align-items: center;
  justify-content: center;
  min-width: 30px;
  min-height: 30px;
  padding-left: 8px;
  padding-right: 8px;
  overflow: visible;
}

.shake {
  transform-origin: center center;
}

.shake:hover {
  animation: animate-logo 1s;
}

@keyframes animate-logo {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(20deg);
  }
  35% {
    transform: rotate(-10deg);
  }
  45% {
    transform: rotate(10deg);
  }
  55% {
    transform: rotate(-5deg);
  }
  65% {
    transform: rotate(5deg);
  }
  75% {
    transform: rotate(-2deg);
  }
  100% {
    transform: rotate(0deg);
  }
}

.nav-link-btn {
  border-radius: 20px !important;
  transition:
    background-color 0.3s,
    color 0.3s;
  margin: 0 4px;
}

.nav-link-btn:hover {
  background-color: rgba(255, 255, 255, 0.1) !important;
  color: #ffffff !important;
}

.nav-link-btn.router-link-active {
  background-color: rgba(255, 255, 255, 0.2) !important;
  color: #ffffff !important;
}

.user-menu-btn {
  border-radius: 24px !important;
  padding: 4px 12px !important;
  height: auto !important;
  min-height: 40px !important;
  transition: background-color 0.3s;
}

.user-menu-btn:hover {
  background-color: rgba(255, 255, 255, 0.15) !important;
}

.user-dropdown-menu {
  min-width: 280px;
  max-width: 320px;
  overflow-x: hidden !important;
}

.user-dropdown-menu :deep(.v-list-item) {
  overflow-x: hidden !important;
}

.user-dropdown-menu :deep(.v-list-item__content) {
  overflow-x: hidden !important;
}

.user-dropdown-menu :deep(.v-list-item-title) {
  white-space: normal !important;
  word-break: break-word;
  overflow-wrap: break-word;
}

.user-dropdown-menu :deep(.v-list-item-subtitle) {
  white-space: normal !important;
  word-break: break-word;
  overflow-wrap: break-word;
}

.user-info-header {
  background-color: rgba(0, 0, 0, 0.02);
  pointer-events: none;
}

.v-theme--dark .user-info-header {
  background-color: rgba(255, 255, 255, 0.05);
}

.status-chip {
  font-weight: 600 !important;
  letter-spacing: 0.3px;
  min-height: 20px !important;
  padding: 2px 8px !important;
}

.status-chip :deep(.v-chip__prepend) {
  margin-inline-end: 4px;
}

.validation-warning {
  background-color: rgba(211, 47, 47, 0.12);
  opacity: 1 !important;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 48px;
  padding: 12px 16px;
  margin: 4px 8px;
  border-radius: 8px;
}

.validation-warning:hover {
  background-color: rgba(211, 47, 47, 0.2) !important;
  transform: translateX(2px);
}

.v-theme--dark .validation-warning {
  background-color: rgba(211, 47, 47, 0.18);
}

.v-theme--dark .validation-warning:hover {
  background-color: rgba(211, 47, 47, 0.25) !important;
}

.validation-warning :deep(.v-list-item-title) {
  font-size: 0.875rem;
  line-height: 1.5;
}

.info-warning {
  background-color: rgba(33, 150, 243, 0.12);
  opacity: 1 !important;
  cursor: pointer;
  transition: all 0.2s;
  min-height: 48px;
  padding: 12px 16px;
  margin: 4px 8px;
  border-radius: 8px;
}

.info-warning:hover {
  background-color: rgba(33, 150, 243, 0.2) !important;
  transform: translateX(2px);
}

.v-theme--dark .info-warning {
  background-color: rgba(33, 150, 243, 0.18);
}

.v-theme--dark .info-warning:hover {
  background-color: rgba(33, 150, 243, 0.25) !important;
}

.info-warning :deep(.v-list-item-title) {
  font-size: 0.875rem;
  line-height: 1.5;
}

.menu-item {
  transition: all 0.2s;
  min-height: 48px;
  padding: 12px 16px;
}

.menu-item:hover {
  background-color: rgba(var(--v-theme-primary), 0.1) !important;
  transform: translateX(2px);
}

.v-theme--dark .menu-item:hover {
  background-color: rgba(255, 255, 255, 0.08) !important;
}

.menu-item :deep(.v-list-item-title) {
  font-size: 0.9375rem;
  font-weight: 500;
}

.logout-item:hover {
  background-color: rgba(211, 47, 47, 0.1) !important;
}

.v-theme--dark .logout-item:hover {
  background-color: rgba(211, 47, 47, 0.15) !important;
}

.v-toolbar-title {
  white-space: nowrap !important;
  overflow: visible !important;
  text-overflow: clip !important;
}
</style>
