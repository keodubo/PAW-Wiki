<template>
  <v-app>
    <div v-if="isInitializing" class="d-flex justify-center align-center h-screen">
      <v-progress-circular indeterminate color="primary" size="64"></v-progress-circular>
    </div>
    <template v-else>
      <router-view />
      <NotificationContainer />
    </template>
  </v-app>
</template>

<script lang="ts" setup>
import { ref, onMounted } from 'vue';
import { useTheme } from '@/composables/useTheme';
import NotificationContainer from '@/components/NotificationContainer.vue';
import { useAuthStore } from '@/stores/auth';

useTheme();

const authStore = useAuthStore();
const isInitializing = ref(true);

onMounted(async () => {
  await authStore.initializeAuth();

  isInitializing.value = false;
});
</script>

<style>
.v-application {
  min-height: 100vh;
  transition:
    background-color 0.3s ease,
    color 0.3s ease;
}

.min-vh-100 {
  min-height: 100vh;
}

* {
  transition:
    background-color 0.3s ease,
    color 0.3s ease,
    border-color 0.3s ease;
}

.v-application.no-transition * {
  transition: none !important;
}

.v-theme--light {
  --v-theme-background: #e8e8e8;
  --v-theme-surface: #ffffff;
}

.v-theme--light .login-page {
  background: #e8e8e8 !important;
}

.v-theme--dark {
  --v-theme-surface: #121212;
}

.v-theme--dark .login-page {
  background: #121212 !important;
}
</style>
