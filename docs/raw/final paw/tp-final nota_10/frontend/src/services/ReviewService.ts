import api from './api';
import type { CachedListResponse } from '@/models/Http';
import type { Review } from '@/models';
import { parseLinkHeader } from '@/utils/pagination';

export interface CreateReviewPayload {
  rating: number | undefined;
  description: string;
}

export class ReviewService {
  async create(productId: number, payload: CreateReviewPayload): Promise<string> {
    const response = await api.post(`/products/${productId}/reviews`, payload, {
      headers: {
        'Content-Type': 'application/vnd.grupi.review.v1+json',
        Accept: 'application/json',
      },
    });

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on review creation response');
    }

    return location;
  }

  async update(productId: number, reviewId: number, payload: CreateReviewPayload) {
    return api.patch(`/products/${productId}/reviews/${reviewId}`, payload, {
      headers: {
        'Content-Type': 'application/vnd.grupi.review.v1+json',
        Accept: 'application/json',
      },
    });
  }

  async remove(productId: number, reviewId: number) {
    return api.delete(`/products/${productId}/reviews/${reviewId}`);
  }

  async list(productId: number, params?: Record<string, any>, currentEtag?: string | null): Promise<CachedListResponse<Review[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.review.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get(`/products/${productId}/reviews`, { params, headers });
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
}

export const reviewService = new ReviewService();
