export interface PaginationLinks {
  first?: string;
  last?: string;
  next?: string;
  prev?: string;
}

export interface CachedResponse<T> {
  data: T;
  notModified: boolean;
  etag: string | null;
}

export interface CachedListResponse<T> extends CachedResponse<T> {
  totalCount: number;
  links?: PaginationLinks;
}
