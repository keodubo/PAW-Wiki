import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Reviewer } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';

export const useReviewersStore = defineStore('reviewers', () => {
  const itemsMap = reactive(new Map<string, Reviewer>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Reviewer>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Reviewer | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Reviewer> {
    return cache.get(uri, { Accept: 'application/vnd.grupi.user.reviewer.v1+json' });
  }

  return {
    items,
    fetch,
    getEntry,
  };
});
