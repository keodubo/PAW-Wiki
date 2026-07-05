import type { Pool } from './Pool';
import type { Product } from './Product';
import type { Company } from './Company';
import type { Category } from './Category';
import type { Location } from './Location';

export enum RequestStatus {
  PENDING = 'PENDING',
  ACCEPTED = 'ACCEPTED',
  REJECTED = 'REJECTED',
  DELIVERED = 'DELIVERED',
}

export interface Request {
  id: number;
  status: RequestStatus;
  quantity: number;
  total: number;

  selfUri: string;
  downPaymentUri?: string;
  finalPaymentUri?: string;
  userUri: string;
  poolUri: string;
}

export interface EnrichedRequest {
  request: Request;
  pool: Pool;
  product: Product;
  location: Location;
  company: Company;
  category: Category;
}
