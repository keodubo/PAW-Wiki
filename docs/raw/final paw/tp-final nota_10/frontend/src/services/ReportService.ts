import type { CachedListResponse } from '@/models/Http';
import api from './api';
import type { Report } from '@/models';

export class ReportService {
  async list(userId: number, currentEtag?: string | null): Promise<CachedListResponse<Report[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.report.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get(`/user/${userId}/reports`, { headers });
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

  async createReportForUser(userId: number, description: string): Promise<string> {
    const response = await api.post(
      `/users/${userId}/reports`,
      { description },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.report.v1+json',
        },
      },
    );

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on report creation response');
    }

    return location;
  }

  async update(id: number, report: Partial<Report>): Promise<Report> {
    const response = await api.put(`/reports/${id}`, report);
    return response.data;
  }

  async delete(id: number): Promise<void> {
    await api.delete(`/reports/${id}`);
  }
}

export const reportService = new ReportService();
