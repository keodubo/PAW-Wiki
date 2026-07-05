export interface Report {
  id: number;
  description: string;
  createdAt: string;
  userReported: boolean;

  selfUri: string;
  userUri: string;
  companyUri: string;
}
