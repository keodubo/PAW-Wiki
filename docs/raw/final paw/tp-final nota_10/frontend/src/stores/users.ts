import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { User } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { UserService } from '@/services';

export const useUsersStore = defineStore('users', () => {
  const userService = new UserService();

  const itemsMap = reactive(new Map<string, User>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<User>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): User | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<User> {
    return cache.get(uri, { Accept: 'application/vnd.grupi.user.v1+json' });
  }

  async function fetchAll(): Promise<void> {
    const response = await userService.list(undefined, listEtag.value);

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
