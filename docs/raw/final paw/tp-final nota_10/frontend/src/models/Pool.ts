import type { Company } from './Company';
import type { Category } from './Category';
import type { Location } from './Location';
import type { Product } from './Product';

export enum PoolStatus {
  AVAILABLE = 'AVAILABLE',
  DELIVERING = 'DELIVERING',
  PAUSED = 'PAUSED',
  CANCELLED = 'CANCELLED',
  FINISHED = 'FINISHED',
}

export interface RequestsStats {
  pendingCount: number;
  pendingSum: number;
  acceptedCount: number;
  acceptedSum: number;
  rejectedCount: number;
  deliveredCount: number;
  deliveredSum: number;
  acceptedOrDeliveredSum: number;
  productsCount: number;
}

export interface Pool {
  id: number;
  minQuantity: number;
  status: PoolStatus;
  createdAt: string;
  downPayment: number;
  price: number;
  requestsStats: RequestsStats;

  selfUri: string;
  productUri: string;
  locationUri: string;
  requestsUri: string;
}

export interface EnrichedPool {
  pool: Pool;
  location: Location;
  product: Product;
  company: Company;
  category: Category;
}
