import { defineStore } from 'pinia';
import { userService, api } from '@/services';
import type { LoginCredentials } from '@/services/UserService';
import type { User, Company, Location } from '@/models';
import { UserRole } from '@/models/UserRole';
import router from '@/router';
import { decodeAndValidateJwt, extractRole } from '@/utils/jwtUtils';

import { setLocale, type Locale } from '@/i18n';

interface AuthState {
  isLoading: boolean;
  error: string | null;
  accessToken: string | null;
  refreshToken: string | null;
  userId: number | null;
  email: string | null;
  role: string | null;
  currentUser: User | null;
  currentCompany: Company | null;
  preferredLocation: Location | null;
  accountValidated: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    isLoading: false,
    error: null,
    accessToken: null,
    refreshToken: null,
    userId: null,
    email: null,
    role: null,
    currentUser: null,
    currentCompany: null,
    preferredLocation: null,
    accountValidated: true,
  }),

  getters: {
    isAuthenticated: (state) => !!state.accessToken,
    isAdmin: (state) => {
      if (state.currentUser?.admin) return true;
      return state.role === UserRole.ADMIN;
    },
    isCompany: (state) => {
      if (state.currentUser?.isCompany) return true;
      return state.role === UserRole.COMPANY_VERIFIED || state.role === UserRole.COMPANY_UNVERIFIED || state.role === UserRole.COMPANY_PENDING;
    },
    isUser: (state) => {
      if (state.currentUser?.admin) return false;
      if (!state.currentUser?.isCompany) return true;
      return state.role === UserRole.CLIENT;
    },
    hasRole: (state) => (role: UserRole | string) => state.role === role,

    hasLocation: (state): boolean => {
      const locId = state.preferredLocation?.id;
      return locId !== undefined && locId !== null && locId !== 0;
    },

    getUserId: (state): number | undefined => {
      return state.currentUser?.id;
    },

    getCompanyId: (state): number | undefined => {
      return state.currentCompany?.id;
    },

    companiesLink: (state): string | undefined => {
      return state.currentUser?.companyUri;
    },

    getRole: (state): string | null => {
      return state.role;
    },
  },

  actions: {
    setUser(user: User) {
      this.currentUser = user;
      this.error = null;
    },

    setCompany(company: Company) {
      this.currentCompany = company;
    },

    clearCompany() {
      this.currentCompany = null;
    },

    setPreferredLocation(location: Location) {
      this.preferredLocation = location;
    },

    clearPreferredLocation() {
      this.preferredLocation = null;
    },

    clearError() {
      this.error = null;
    },

    async fetchCurrentUser(userId?: number) {
      this.isLoading = true;
      this.error = null;
      try {
        const id = userId || this.currentUser?.id;
        if (!id) {
          throw new Error('No user ID provided');
        }

        const user = await userService.getById(id);
        this.setUser(user);
        return user;
      } catch (error) {
        console.error('Failed to fetch current user:', error);
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async login(credentials: LoginCredentials) {
      this.isLoading = true;
      this.error = null;

      try {
        const authData = await userService.authenticate(credentials);
        this.setAuthData(authData);

        await this.fetchUserCompanyAndLocation(authData.userId, authData.role);
        this.accountValidated = this.currentUser?.validated || false;
      } catch (err: any) {
        this.error = err?.message || 'Login failed';
        this.logout();
        throw err;
      } finally {
        this.isLoading = false;
      }
    },

    async fetchUserCompanyAndLocation(userId: number, role: string) {
      try {
        const user = await userService.getById(userId);
        this.setUser(user);
        this.applyPreferredLanguage(user);

        const isCompanyUser = user.isCompany || ['ROLE_COMPANY_VERIFIED', 'ROLE_COMPANY_UNVERIFIED', 'ROLE_COMPANY_PENDING'].includes(role);

        const companiesLink = user.companyUri;
        const locationLink = user.locationUri;

        if (isCompanyUser) {
          if (companiesLink) {
            try {
              const response = await api.get(companiesLink);

              if (response.data) {
                const currentCompanyValidated = response.data.validated ?? false;
                this.setCompany(response.data);

                if (currentCompanyValidated && this.refreshToken) {
                  const isOldCompanyRole = role === UserRole.COMPANY_UNVERIFIED || role === UserRole.COMPANY_PENDING;

                  if (isOldCompanyRole) {
                    try {
                      await this.refreshTokens();
                    } catch (refreshError) {
                      console.error('Failed to refresh tokens after company validation:', refreshError);
                    }
                  }
                }
              } else {
                console.warn('User has company role but API returned no company.');
                this.clearCompany();
              }
            } catch (companyErr) {
              console.error('Failed to fetch company via link', companyErr);
              this.clearCompany();
            }
          } else {
            this.clearCompany();
          }
        } else {
          this.clearCompany();
        }

        if (locationLink) {
          try {
            const response = await api.get(locationLink);
            this.setPreferredLocation(response.data);
          } catch (locationErr) {
            console.error('Failed to fetch location via link', locationErr);
            this.clearPreferredLocation();
          }
        }
      } catch (err) {
        console.error('Failed to fetch user profile', err);
        throw err;
      }
    },

    setAuthData(data: { accessToken: string; refreshToken: string; userId: number; email: string; role: string; accountValidated?: boolean }) {
      this.accessToken = data.accessToken;
      this.refreshToken = data.refreshToken;
      this.userId = data.userId;
      this.email = data.email;
      this.role = data.role;
      if (typeof data.accountValidated === 'boolean') {
        this.accountValidated = data.accountValidated;
      }

      localStorage.setItem('auth_token', data.accessToken);
      localStorage.setItem('refresh_token', data.refreshToken);
      localStorage.setItem('user_id', String(data.userId));
      localStorage.setItem('user_email', data.email);
      localStorage.setItem('user_role', data.role);
      if (typeof data.accountValidated === 'boolean') {
        localStorage.setItem('account_validated', String(data.accountValidated));
      }
    },

    setTokens(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;

      localStorage.setItem('auth_token', accessToken);
      localStorage.setItem('refresh_token', refreshToken);
    },

    async refreshTokens(): Promise<{ accessToken: string; refreshToken: string; role: string }> {
      if (!this.refreshToken) {
        throw new Error('No refresh token available');
      }

      try {
        const response = await api.head('/users', {
          headers: {
            Authorization: `Bearer ${this.refreshToken}`,
            Accept: 'application/vnd.grupi.user.v1+json',
          },
        });

        const accessToken = response.headers['x-grupi-access-token'];
        const refreshToken = response.headers['x-grupi-refresh-token'];
        const accountValidatedHeader = response.headers['x-grupi-account-validated'];

        if (!accessToken || !refreshToken) {
          throw new Error('Token refresh failed: Missing required headers');
        }

        const payload = decodeAndValidateJwt(accessToken);
        const role = extractRole(payload);

        if (accountValidatedHeader === 'true') {
          this.accountValidated = true;
          localStorage.setItem('account_validated', 'true');
        } else if (accountValidatedHeader === 'false') {
          this.accountValidated = false;
          localStorage.setItem('account_validated', 'false');
        }

        this.setTokens(accessToken, refreshToken);
        this.role = role;
        localStorage.setItem('user_role', role);

        return { accessToken, refreshToken, role };
      } catch (error) {
        console.error('Failed to refresh tokens:', error);
        throw error;
      }
    },

    logout() {
      router.push('/login');

      this.$reset();

      this.currentUser = null;
      this.clearCompany();
      this.clearPreferredLocation();

      localStorage.removeItem('auth_token');
      localStorage.removeItem('refresh_token');
      localStorage.removeItem('user_id');
      localStorage.removeItem('user_email');
      localStorage.removeItem('user_role');
      localStorage.removeItem('account_validated');
    },

    async initializeAuth() {
      const token = localStorage.getItem('auth_token');
      const refreshToken = localStorage.getItem('refresh_token');
      const userId = localStorage.getItem('user_id');
      const role = localStorage.getItem('user_role');
      const email = localStorage.getItem('user_email');
      const accountValidated = localStorage.getItem('account_validated');

      if (token && refreshToken && userId && role && email) {
        this.accessToken = token;
        this.refreshToken = refreshToken;
        this.userId = parseInt(userId);
        this.email = email;
        this.role = role;
        if (accountValidated !== null) {
          this.accountValidated = accountValidated === 'true';
        }

        try {
          await this.fetchUserCompanyAndLocation(this.userId, this.role);
        } catch (error) {
          console.error('Session invalid', error);
          this.logout();
        }
      }
    },

    applyPreferredLanguage(user?: User) {
      const lang = user?.preferredLanguage;
      if (lang === 'en' || lang === 'es') {
        setLocale(lang as Locale);
      }
    },
  },
});
