import { defineStore } from 'pinia';
import { ref, computed, reactive } from 'vue';
import type { Company } from '@/models';
import { ResourceCache } from '@/utils/ResourceCache';
import { CompanyService } from '@/services';

export const useCompaniesStore = defineStore('companies', () => {
  const companyService = new CompanyService();

  const itemsMap = reactive(new Map<string, Company>());
  const listEtag = ref<string | null>(null);

  const cache = new ResourceCache<Company>(
    300,
    (uri, data) => itemsMap.set(uri, data),
    (uri) => itemsMap.get(uri),
  );

  const items = computed(() => Array.from(itemsMap.values()));

  function getEntry(uri: string): Company | undefined {
    return itemsMap.get(uri);
  }

  async function fetch(uri: string): Promise<Company> {
    return cache.get(uri);
  }

  async function fetchAll(): Promise<void> {
    const response = await companyService.list(undefined, listEtag.value);

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
