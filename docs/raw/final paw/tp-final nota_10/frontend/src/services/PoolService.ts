import type { CachedListResponse } from '@/models/Http';
import api from './api';
import type { Pool } from '@/models';
import { parseLinkHeader } from '@/utils/pagination';

export interface PoolListParams {
  product_id?: number;
  search?: string;
  company_id?: number;
  price_min?: number;
  price_max?: number;
  location_id?: number;
  category_id?: number;
  page?: number;
  order_by?: string;
  desc?: boolean;
  status?: string;
}

export interface CreatePoolData {
  minQuantity: number;
  downPayment: number;
  productUri: string;
  locationUri: string;
}

export interface EditPoolData {
  minQuantity: number;
}

export class PoolService {
  async list(params?: PoolListParams, currentEtag?: string | null): Promise<CachedListResponse<Pool[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.pool.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get('/pools', { params, headers });
    if (response.status === 304) {
      return {
        data: [],
        notModified: true,
        etag: currentEtag || null,
        totalCount: 0,
      };
    }
    const etag = response.headers['etag'] || null;
    const total = parseInt(response.headers['x-total-count'] || '0', 10);
    const linkHeader = response.headers['link'] || '';
    const links = parseLinkHeader(linkHeader);

    return {
      data: response.data,
      notModified: false,
      etag: etag,
      totalCount: total,
      links: links,
    };
  }

  async getById(id: number): Promise<Pool> {
    const response = await api.get(`/pools/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.pool.v1+json',
      },
    });
    return response.data;
  }

  async create(data: CreatePoolData): Promise<string> {
    const response = await api.post('/pools', data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on pool creation response');
    }

    return location;
  }

  async update(id: number, data: EditPoolData): Promise<void> {
    await api.patch(`/pools/${id}`, data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }

  async copy(id: number): Promise<number> {
    const pool = await this.getById(id);

    const response = await api.post(
      '/pools',
      {
        minQuantity: pool.minQuantity,
        downPayment: pool.downPayment,
        productUri: pool.productUri,
        locationUri: pool.locationUri,
      },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.pool.v1+json',
        },
      },
    );

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on pool creation response');
    }
    const newId = parseInt(location.split('/').filter(Boolean).pop() || '', 10);
    if (Number.isNaN(newId)) {
      throw new Error(`Could not parse pool id from Location header: ${location}`);
    }
    return newId;
  }

  async await(id: number): Promise<void> {
    await api.patch(`/pools/${id}`, { status: 'AVAILABLE' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }

  async pause(id: number): Promise<void> {
    await api.patch(`/pools/${id}`, { status: 'PAUSED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }

  async cancel(id: number): Promise<void> {
    await api.patch(`/pools/${id}`, { status: 'CANCELLED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }

  async deliver(id: number): Promise<void> {
    await api.patch(`/pools/${id}`, { status: 'DELIVERING' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }

  async finish(id: number): Promise<void> {
    await api.patch(`/pools/${id}`, { status: 'FINISHED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.pool.v1+json',
      },
    });
  }
}

export const poolService = new PoolService();
