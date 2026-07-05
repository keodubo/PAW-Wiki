import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Product } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { ProductService } from '@/services';

export const useProductsStore = defineStore('products', () => {
  const productService = new ProductService();

  const itemsMap = reactive(new Map<string, Product>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Product>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Product | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Product> {
    return cache.get(uri);
  }

  async function fetchAll(): Promise<void> {
    const response = await productService.list({ active: true }, listEtag.value);

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
