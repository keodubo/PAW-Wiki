export interface Review {
  id: number;
  rating: number;
  description: string;
  createdAt: string;

  selfUri: string;
  reviewerUri: string;
  productUri: string;
}
