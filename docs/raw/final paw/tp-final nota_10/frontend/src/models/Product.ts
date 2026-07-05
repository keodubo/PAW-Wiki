import type { Company } from './Company';
import type { Category } from './Category';

export interface Ratings {
  oneStar: number;
  twoStars: number;
  threeStars: number;
  fourStars: number;
  fiveStars: number;
}

export interface Product {
  id: number;
  name: string;
  description: string;
  price: number;
  active: boolean;
  rating: number;
  ratings: Ratings;
  canRetire: boolean;

  selfUri: string;
  imageUri: string;
  companyUri: string;
  categoryUri: string;
  poolsUri: string;
  reviewsUri: string;
}

export interface EnrichedProduct extends Product {
  companyData?: Company;
  categoryData?: Category;
}
