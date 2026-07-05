import type { Product } from './Product';
import type { User } from './User';

export interface Company {
  id: number;
  name: string;
  address: string;
  email: string;
  phone: string;
  validated: boolean;
  cbu: string;

  selfUri: string;
  imageUri?: string;
  ownerUri: string;
  productsUri: string;
}

export interface EnrichedCompany {
  company: Company;
  owner?: User;
  products?: Product[];
}
