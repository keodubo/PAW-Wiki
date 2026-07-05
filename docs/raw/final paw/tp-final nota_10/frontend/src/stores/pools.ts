import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Pool } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { PoolService } from '@/services';

export const usePoolsStore = defineStore('pools', () => {
  const poolService = new PoolService();

  const itemsMap = reactive(new Map<string, Pool>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Pool>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Pool | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Pool> {
    return cache.get(uri);
  }

  async function fetchAll(): Promise<void> {
    const response = await poolService.list(undefined, listEtag.value);

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
