import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Request } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { RequestService } from '@/services';

export const useRequestsStore = defineStore('requests', () => {
  const requestService = new RequestService();

  const itemsMap = reactive(new Map<string, Request>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Request>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Request | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Request> {
    return cache.get(uri);
  }

  async function fetchAll(): Promise<void> {
    const response = await requestService.list(undefined, listEtag.value);

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
