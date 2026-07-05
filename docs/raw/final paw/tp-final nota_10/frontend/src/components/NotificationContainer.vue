<template>
  <v-snackbar v-for="notification in notifications" :key="notification.id" v-model="notification.visible" :timeout="notification.timeout" :color="notification.type" location="top center" variant="elevated" class="notification-snackbar">
    <div class="d-flex align-center">
      <v-icon :icon="notification.icon" class="me-3"></v-icon>
      <span class="text-body-1 font-weight-medium">
        {{ $t(notification.message) }}
      </span>
    </div>

    <template #actions>
      <v-btn icon variant="text" size="small" @click="removeNotification(notification.id)">
        <v-icon>mdi-close</v-icon>
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script setup lang="ts">
import { useNotifications } from '@/composables/useNotifications';

const { notifications, removeNotification } = useNotifications();
</script>

<style scoped>
:deep(.v-snackbar--location-top-center) {
  top: 60px !important;
}

:deep(.v-snackbar--top) {
  top: 60px !important;
}
</style>
