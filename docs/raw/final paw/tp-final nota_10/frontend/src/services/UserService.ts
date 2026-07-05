import type { CachedListResponse } from '@/models/Http';
import api from './api';
import { parseLinkHeader } from '@/utils/pagination';
import type { User } from '@/models';
import { decodeAndValidateJwt, extractRole } from '@/utils/jwtUtils';

export interface LoginCredentials {
  email: string;
  password: string;
}

export interface RegisterData {
  name: string;
  email: string;
  password: string;
  confirmPassword: string;
  locationUri?: string;
  preferredLanguage?: 'en' | 'es';
}

export interface CompanyRegisterData extends RegisterData {
  companyName: string;
  cbu: string;
  companyImage?: File;
}

export interface PasswordResetData {
  password: string;
  passwordConfirmation: string;
  recoveryToken: string;
}

export interface CreateUserData {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  locationUri: string;
  isCompany: boolean;
  preferredLanguage?: 'en' | 'es';
}

export interface EditUserData {
  firstName: string;
  lastName: string;
  locationUri: string;
}

export interface RecoverPasswordEmailData {
  email: string;
}

export interface RecoverPasswordData {
  password: string;
}

export interface UserListParams {}

export class UserService {
  async authenticate(
    credentials: LoginCredentials,
    skip401Handler = false,
  ): Promise<{
    accessToken: string;
    refreshToken: string;
    userId: number;
    email: string;
    role: string;
    accountValidated?: boolean;
  }> {
    const encodedCredentials = btoa(`${credentials.email}:${credentials.password}`);

    const response = await api.head('/users', {
      headers: {
        Authorization: `Basic ${encodedCredentials}`,
        Accept: 'application/vnd.grupi.user.v1+json',
      },
      skip401Handler,
    } as any);

    const accessToken = response.headers['x-grupi-access-token'];
    const refreshToken = response.headers['x-grupi-refresh-token'];
    const accountValidatedHeader = response.headers['x-grupi-account-validated'];

    if (!accessToken || !refreshToken) {
      throw new Error('Authentication failed: Missing required headers');
    }

    const payload = decodeAndValidateJwt(accessToken);
    const role = extractRole(payload);

    let accountValidated: boolean | undefined = undefined;
    if (accountValidatedHeader === 'true') {
      accountValidated = true;
    } else if (accountValidatedHeader === 'false') {
      accountValidated = false;
    }

    return {
      accessToken,
      refreshToken,
      userId: payload.userId,
      email: payload.sub,
      role,
      accountValidated,
    };
  }

  async list(params?: UserListParams, currentEtag?: string | null): Promise<CachedListResponse<User[]>> {
    const headers: Record<string, string> = {
      Accept: 'application/vnd.grupi.user.v1+json',
    };
    if (currentEtag) {
      headers['If-None-Match'] = `"${currentEtag}"`;
    }
    const response = await api.get('/users', { params, headers });
    if (response.status === 304) {
      return {
        data: [],
        notModified: true,
        etag: currentEtag || null,
        totalCount: 0,
      };
    }
    const etag = response.headers['etag'] || null;
    const linkHeader = response.headers['link'] || '';
    const links = parseLinkHeader(linkHeader);
    return {
      data: response.data,
      notModified: false,
      etag: etag,
      totalCount: parseInt(response.headers['x-total-count'] || '0', 10),
      links: links,
    };
  }

  async getById(id: number): Promise<User> {
    const response = await api.get(`/users/${id}`, {
      headers: {
        Accept: 'application/vnd.grupi.user.v1+json',
      },
    });
    return response.data;
  }

  async create(data: CreateUserData): Promise<string> {
    const response = await api.post('/users', data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.user.v1+json',
      },
    });

    const location = response.headers['location'];
    if (!location) {
      throw new Error('Missing Location header on user creation response');
    }

    return location;
  }

  async update(id: number, data: EditUserData): Promise<void> {
    await api.patch(`/users/${id}`, data, {
      headers: {
        'Content-Type': 'application/vnd.grupi.user.v1+json',
      },
    });
  }

  async requestPasswordRecovery(data: RecoverPasswordEmailData): Promise<void> {
    const encodedCredentials = btoa(`${data.email}:recover-password`);
    await api.patch(
      '/users',
      {},
      {
        headers: {
          'X-Grupi-Action': 'recover-password',
          Authorization: `Basic ${encodedCredentials}`,
          'Content-Type': 'application/vnd.grupi.user.v1+json',
        },
      },
    );
  }

  async recoverPassword(email: string, token: string, data: RecoverPasswordData): Promise<void> {
    const encodedCredentials = btoa(`${email}:${token}`);

    const authResponse = await api.head('/users', {
      headers: {
        Authorization: `Basic ${encodedCredentials}`,
        Accept: 'application/vnd.grupi.user.v1+json',
      },
    });

    const userId = authResponse.headers['x-grupi-user-id'];
    if (!userId) {
      throw new Error('Failed to authenticate with recovery token');
    }

    await api.patch(`/users/${userId}`, data, {
      headers: {
        Authorization: `Basic ${encodedCredentials}`,
        'Content-Type': 'application/vnd.grupi.user.v1+json',
      },
    });
  }

  async changePassword(userId: number, email: string, oldPassword: string, newPassword: string): Promise<void> {
    const encodedCredentials = btoa(`${email}:${oldPassword}`);
    await api.patch(`/users/${userId}`, { password: newPassword }, {
      headers: {
        Authorization: `Basic ${encodedCredentials}`,
        'Content-Type': 'application/vnd.grupi.user.v1+json',
      },
      skip401Handler: true,
    } as any);
  }

  async getUserPools(userId?: number): Promise<User[]> {
    const endpoint = userId ? `/users/${userId}/pools` : '/user/pools';
    const response = await api.get(endpoint);
    return response.data;
  }

  async updatePreferredLanguage(userId: number, language: 'en' | 'es'): Promise<void> {
    await api.patch(
      `/users/${userId}`,
      { preferredLanguage: language },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.user.v1+json',
        },
      },
    );
  }

  async blockUser(userId: number, currentBlockLevel: number): Promise<void> {
    const newBlockLevel = Math.min(currentBlockLevel + 1, 4);
    await api.patch(
      `/users/${userId}`,
      { blockLevel: newBlockLevel },
      {
        headers: {
          'Content-Type': 'application/vnd.grupi.user.v1+json',
        },
      },
    );
  }

  async requestValidationEmail(userId: number): Promise<void> {
    await api.patch(
      `/users/${userId}`,
      {},
      {
        headers: {
          'X-Grupi-Action': 'resend-validation',
          'Content-Type': 'application/vnd.grupi.user.v1+json',
        },
      },
    );
  }
}

export const userService = new UserService();
