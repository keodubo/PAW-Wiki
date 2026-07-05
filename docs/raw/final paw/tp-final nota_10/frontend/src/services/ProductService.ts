import api from './api';
import type { Product } from '@/models';
import type { CachedListResponse } from '@/models/Http';
import { parseLinkHeader } from '@/utils/pagination';

export interface ProductListParams {
  page?: number;
  company_id?: number;
  category_id?: number;
  search?: string;
  price_min?: number;
  price_max?: number;
  order_by?: string;
  desc?: boolean;
  active?: boolean;
}

export interface CreateProductData {
  name: string;
  description: string;
  price: number;
  imageUri: string;
  categoryUri: string;
}

export interface EditProductData {
  name?: string;
  description?: string;
  price?: number;
  categoryUri?: string | null;
  imageUri?: string | null;
}

export class ProductService {
  /**
   * Fetches products with Conditional GET support.
   */
  async list(params?: ProductListParams, currentEtag?: string | null): Promise<CachedListResponse<Product[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.product.v1+json',
    };

    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }

    const response = await api.get('/products', {
      params,
      headers,
    });

    if (response.status === 304) {
      return {
        data: [],
        notModified: true,
        etag: currentEtag ? currentEtag : null,
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

  async getById(id: number): Promise<Product> {
    const response = await api.get(`/products/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.product.v1+json',
      },
    });
    return response.data;
  }

  async create(data: CreateProductData): Promise<string> {
    const response = await api.post('/products', data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.product.v1+json',
      },
    });

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on product creation response');
    }

    return location;
  }

  async update(id: number, data: EditProductData): Promise<void> {
    await api.patch(`/products/${id}`, data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.product.v1+json',
      },
    });
  }

  async delete(id: number): Promise<void> {
    await api.patch(
      `/products/${id}`,
      { active: false },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.product.v1+json',
        },
      }
    );
  }
}

export const productService = new ProductService();
