import type { Company } from './Company';
import type { Location } from './Location';
import type { Report } from './Report';

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  isCompany: boolean;
  validated: boolean;
  admin: boolean;
  blockLevel: number;
  blockedUntil?: string;
  preferredLanguage?: 'en' | 'es';
  hasReports: boolean;
  isBlocked: boolean;

  selfUri: string;
  locationUri?: string;
  companyUri?: string;
  reportsUri: string;
  requestsUri: string;
}

export interface EnrichedUser {
  user: User;
  company?: Company;
  location?: Location;
  reports?: Report[];
}
