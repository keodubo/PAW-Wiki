import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Category } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { CategoryService } from '@/services';

export const useCategoriesStore = defineStore('categories', () => {
  const categoryService = new CategoryService();

  const itemsMap = reactive(new Map<string, Category>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Category>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Category | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Category> {
    return cache.get(uri);
  }

  async function fetchAll(): Promise<void> {
    const response = await categoryService.list(listEtag.value);

    if (response.notModified) {
      return;
    }

    listEtag.value = response.etag || null;

    if (Array.isArray(response.data)) {
      for (const company of response.data) {
        if (company.selfUri) {
          cache.seed(company.selfUri, company, null);
        }
      }
    }
  }

  return {
    items,
    fetch,
    fetchAll,
    getEntry,
  };
});
