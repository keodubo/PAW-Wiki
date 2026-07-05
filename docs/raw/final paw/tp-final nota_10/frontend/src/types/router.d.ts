import 'vue-router';
import { UserRole } from '@/models/UserRole';

declare module 'vue-router' {
  interface RouteMeta {
    allowedRoles?: UserRole[];
    requiresAuth?: boolean;
    requiresGuest?: boolean;
  }
}
