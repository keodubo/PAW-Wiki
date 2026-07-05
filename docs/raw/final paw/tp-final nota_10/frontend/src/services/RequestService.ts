import type { CachedListResponse } from '@/models/Http';
import api from './api';
import { parseLinkHeader } from '@/utils/pagination';
import type { Request } from '@/models';

export interface RequestListParams {
  pool_id?: number;
  user_id?: number;
  status?: string;
  pool_status?: string;
  company_id?: number;
  product_id?: number;
  search?: string;
  page?: number;
  order_by?: string;
  desc?: boolean;
}

export interface EditRequestData {
  quantity: number;
}


export class RequestService {
  async list(params?: RequestListParams, currentEtag?: string | null): Promise<CachedListResponse<Request[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.request.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get('/requests', { params, headers });
    if (response.status === 304) {
      return {
        data: [],
        notModified: true,
        etag: currentEtag || null,
        totalCount: 0,
      };
    }
    const etag = response.headers['etag'] || null;
    const linkHeader = response.headers['link'] || '';
    const links = parseLinkHeader(linkHeader);
    return {
      data: response.data,
      notModified: false,
      etag: etag,
      totalCount: parseInt(response.headers['x-total-count'] || '0', 10),
      links: links,
    };
  }

  async create(poolUri: string, quantity: number): Promise<string> {
    const response = await api.post(
      '/requests',
      { poolUri, quantity },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.request.v1+json',
        },
      },
    );

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on request creation response');
    }

    return location;
  }

  async edit(id: number, data: EditRequestData): Promise<void> {
    await api.patch(`/requests/${id}`, data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async uploadDownPayment(id: number, documentUri: string): Promise<void> {
    await api.patch(`/requests/${id}`, { downPaymentUri: documentUri }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async uploadFinalPayment(id: number, documentUri: string): Promise<void> {
    await api.patch(`/requests/${id}`, { finalPaymentUri: documentUri }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async delete(id: number): Promise<void> {
    await api.delete(`/requests/${id}`);
  }

  async await(id: number): Promise<void> {
    await api.patch(`/requests/${id}`, { status: 'PENDING' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async accept(id: number): Promise<void> {
    await api.patch(`/requests/${id}`, { status: 'ACCEPTED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async reject(id: number): Promise<void> {
    await api.patch(`/requests/${id}`, { status: 'REJECTED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }

  async deliver(id: number): Promise<void> {
    await api.patch(`/requests/${id}`, { status: 'DELIVERED' }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.request.v1+json',
      },
    });
  }
}

export const requestService = new RequestService();
