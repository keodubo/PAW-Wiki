import type { CachedListResponse } from '@/models/Http';
import api from './api';
import type { Location } from '@/models';

export class LocationService {
  async list(currentEtag?: string | null): Promise<CachedListResponse<Location[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.location.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get('/locations', { headers });
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

  async getAll(): Promise<Location[]> {
    const response = await this.list();
    return response.data;
  }

  async getById(id: number): Promise<Location> {
    const response = await api.get(`/locations/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.location.v1+json',
      },
    });
    return response.data;
  }
}

export const locationService = new LocationService();
