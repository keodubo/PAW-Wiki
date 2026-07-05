<template>
  <form @submit.prevent="handleSearch">
    <v-row justify="center" class="mb-4">
      <v-col cols="12" xl="4" lg="6">
        <div class="text-center">
          <v-text-field
            v-model="model"
            :placeholder="$t('product_search_placeholder')"
            variant="outlined"
            density="compact"
            hide-details
            class="search-input elevation-2 rounded-lg"
            style="background-color: white"
            @keyup.enter="handleSearch"
          >
            <template #append-inner>
              <v-btn color="primary" icon="mdi-magnify" variant="flat" size="small" @click="handleSearch" />
            </template>
          </v-text-field>
        </div>
      </v-col>
    </v-row>
  </form>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue';
import { useRouter } from 'vue-router';

const props = defineProps<{ modelValue: string }>();
const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void;
  (e: 'search', value: string): void;
}>();

const router = useRouter();
const model = ref(props.modelValue);

watch(
  () => props.modelValue,
  (val) => (model.value = val),
);
watch(model, (val) => emit('update:modelValue', val));

const handleSearch = () => {
  emit('search', model.value);

  router.push({
    path: '/pools',
    query: { search: model.value || undefined },
  });
};
</script>

<style scoped>
.search-input {
  transition: all 0.3s ease;
}

.search-input:hover {
  box-shadow: 0 4px 15px rgba(86, 90, 198, 0.15) !important;
}

.search-input :deep(.v-field--focused) {
  box-shadow: 0 4px 20px rgba(86, 90, 198, 0.25) !important;
}

.search-input :deep(.v-field) {
  border-radius: 12px;
  font-size: 16px;
}

.search-input :deep(.v-field__input) {
  padding: 8px 16px;
}

.search-input :deep(.v-btn) {
  border-radius: 8px;
  margin-right: 4px;
}
</style>
