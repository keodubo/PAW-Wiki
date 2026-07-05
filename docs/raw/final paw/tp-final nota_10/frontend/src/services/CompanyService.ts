import type { CachedListResponse } from '@/models/Http';
import api from './api';
import { parseLinkHeader } from '@/utils/pagination';
import type { Company } from '@/models';

export interface CompanyListParams {
  page?: number;
  search?: string;
  validated?: boolean;
}

export interface CreateCompanyData {
  name: string;
  address: string;
  email: string;
  phone: string;
  cbu: string;
  imageUri: string;
}

export interface EditCompanyData {
  address?: string;
  email?: string;
  phone?: string;
  cbu?: string;
  imageUri?: string | null;
}

export class CompanyService {
  async list(params?: CompanyListParams, currentEtag?: string | null): Promise<CachedListResponse<Company[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.company.v1+json',
    };

    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }

    const response = await api.get('/companies', {
      params,
      headers,
    });

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

  async getById(id: number): Promise<Company> {
    const response = await api.get(`/companies/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.company.v1+json',
      },
    });
    return response.data;
  }

  async create(data: CreateCompanyData): Promise<string> {
    const response = await api.post('/companies', data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.company.v1+json',
      },
    });

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on company creation response');
    }

    return location;
  }

  async update(id: number, data: EditCompanyData): Promise<void> {
    await api.patch(`/companies/${id}`, data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.company.v1+json',
      },
    });
  }

  async validate(id: number): Promise<void> {
    await api.patch(`/companies/${id}`, { validated: true }, {
      headers: {
        'Content-Type': 'application/vnd.grupi.company.v1+json',
      },
    });
  }
}

export const companyService = new CompanyService();
