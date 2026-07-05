<template>
  <div class="d-flex flex-column min-vh-100">
    <AppBar />
    <v-main class="flex-grow-1">
      <router-view />
    </v-main>
  </div>
</template>

<script lang="ts" setup>
import AppBar from '@/components/AppBar.vue';
</script>
