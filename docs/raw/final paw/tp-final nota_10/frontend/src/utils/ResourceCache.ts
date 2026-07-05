import api from '@/services/api';

interface CacheMetadata {
  etag: string | null;
  timestamp: number;
}

export class ResourceCache<T> {
  private metadata = new Map<string, CacheMetadata>();
  private inflight = new Map<string, Promise<T>>();

  private readonly ttl: number;

  private setData: (uri: string, data: T) => void;
  private getData: (uri: string) => T | undefined;

  constructor(ttlSeconds: number, setData: (uri: string, data: T) => void, getData: (uri: string) => T | undefined) {
    this.ttl = ttlSeconds * 1000;
    this.setData = setData;
    this.getData = getData;
  }

  async get(uri: string, customHeaders?: Record<string, string> | null): Promise<T> {
    if (this.inflight.has(uri)) return this.inflight.get(uri)!;

    const data = this.getData(uri);
    const meta = this.metadata.get(uri);

    const now = Date.now();
    const isExpired = meta ? now - meta.timestamp > this.ttl : true;

    if (data && !isExpired) return data;

    const promise = this.fetchResource(uri, meta?.etag, data, customHeaders);
    this.inflight.set(uri, promise);

    try {
      return await promise;
    } finally {
      this.inflight.delete(uri);
    }
  }

  private async fetchResource(uri: string, etag?: string | null, oldData?: T, customHeaders?: Record<string, string> | null): Promise<T> {
    try {
      const headers: Record<string, string> = {};
      if (etag) headers['If-None-Match'] = `"${etag}"`;
      if (customHeaders) Object.assign(headers, customHeaders);

      const response = await api.get(uri, { headers });

      if (response.status === 304 && oldData) {
        this.metadata.set(uri, { etag: etag || null, timestamp: Date.now() });
        return oldData;
      }

      const newEtag = response.headers['etag'] || '';

      this.setData(uri, response.data);
      this.metadata.set(uri, { etag: newEtag, timestamp: Date.now() });

      return response.data;
    } catch (error) {
      console.error(`[Cache] Error fetching ${uri}`, error);
      throw error;
    }
  }

  seed(uri: string, data: T, etag: string | null) {
    this.setData(uri, data);
    this.metadata.set(uri, { etag, timestamp: Date.now() });
  }
}
