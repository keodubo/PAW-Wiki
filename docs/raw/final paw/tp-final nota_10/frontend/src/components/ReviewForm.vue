<template>
  <v-card class="review-form elevation-2 rounded-xl">
    <v-card-text>
      <div class="d-flex justify-space-between align-center mb-4">
        <div>
          <h3 class="text-subtitle-1 font-weight-bold mb-1">{{ title }}</h3>
          <p v-if="subtitle" class="text-caption text-medium-emphasis mb-0">{{ subtitle }}</p>
        </div>
        <v-btn v-if="hasExistingReview" variant="text" color="error" @click="$emit('delete')" :disabled="isSubmitting" prepend-icon="mdi-delete">
          {{ $t('product_detail.review_form.delete_cta') }}
        </v-btn>
      </div>

      <div class="rating-wrapper mb-4">
        <span class="text-body-2 font-weight-medium mr-3">{{ $t('product_detail.review_form.rating_label') }}</span>
        <v-rating :model-value="rating" color="warning" density="comfortable" size="large" full-icon="mdi-star" empty-icon="mdi-star-outline" :half-increments="false" @update:model-value="$emit('update:rating', $event)" />
      </div>

      <v-textarea
        :model-value="description"
        :label="$t('product_detail.review_form.description_label')"
        :placeholder="$t('product_detail.review_form.description_placeholder')"
        :counter="1024"
        auto-grow
        rows="4"
        max-rows="8"
        class="mb-4"
        @update:model-value="$emit('update:description', $event)"
      />

      <div class="d-flex justify-end ga-3">
        <v-btn color="primary" variant="flat" class="submit-btn" :loading="isSubmitting" :disabled="!canSubmit" @click="$emit('submit')">
          <v-icon start>mdi-send</v-icon>
          {{ hasExistingReview ? $t('product_detail.review_form.update_cta') : $t('product_detail.review_form.submit_cta') }}
        </v-btn>
      </div>
    </v-card-text>
  </v-card>
</template>

<script setup lang="ts">
const props = defineProps({
  rating: {
    type: Number,
    default: undefined,
  },
  description: {
    type: String,
    default: '',
  },
  title: {
    type: String,
    default: '',
  },
  subtitle: {
    type: String,
    default: '',
  },
  hasExistingReview: {
    type: Boolean,
    default: false,
  },
  isSubmitting: {
    type: Boolean,
    default: false,
  },
  canSubmit: {
    type: Boolean,
    default: true,
  },
});

const emit = defineEmits(['submit', 'delete', 'update:rating', 'update:description']);
</script>

<style scoped>
.review-form {
  border: 1px solid rgba(var(--v-theme-primary), 0.12);
  background: rgb(var(--v-theme-surface));
}

.submit-btn {
  text-transform: none;
  font-weight: 600;
  border-radius: 10px;
}

.rating-wrapper {
  display: flex;
  align-items: center;
}
</style>
