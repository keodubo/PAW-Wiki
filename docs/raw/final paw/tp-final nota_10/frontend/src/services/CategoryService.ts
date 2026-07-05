import type { CachedListResponse } from '@/models/Http';
import api from './api';
import type { Category } from '@/models';

export class CategoryService {
  async list(currentEtag?: string | null): Promise<CachedListResponse<Category[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.category.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get('/categories', { headers });
    if (response.status === 304) {
      return {
        data: [],
        notModified: true,
        etag: currentEtag || null,
        totalCount: 0,
      };
    }
    const etag = response.headers['etag'] || null;
    return {
      data: response.data,
      notModified: false,
      etag: etag,
      totalCount: parseInt(response.headers['x-total-count'] || '0', 10),
    };
  }

  async getById(id: number): Promise<Category> {
    const response = await api.get(`/categories/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.category.v1+json',
      },
    });
    return response.data;
  }
}

export const categoryService = new CategoryService();
