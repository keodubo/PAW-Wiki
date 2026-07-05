import axios from 'axios';
import { useAuthStore } from '@/stores/auth';
import i18n from '@/i18n';
import router from '@/router';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
  validateStatus: (status) => {
    return (status >= 200 && status < 300) || status === 304;
  },
});

api.interceptors.request.use(
  (config) => {
    if (!config.headers.Authorization) {
      const authStore = useAuthStore();
      if (authStore.accessToken) {
        config.headers = config.headers || {};
        config.headers.Authorization = `Bearer ${authStore.accessToken}`;
      }
    }

    const currentLocale = i18n.global.locale.value;
    config.headers = config.headers || {};
    config.headers['Accept-Language'] = currentLocale;

    if (config.data instanceof FormData) {
      delete config.headers['Content-Type'];
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const authStore = useAuthStore();

    const skip401Handler = (originalRequest as any).skip401Handler === true;

    if (error.response?.status === 401 && !originalRequest._retry && !skip401Handler) {
      originalRequest._retry = true;

      if (authStore.refreshToken) {
        try {
          originalRequest.headers.Authorization = `Bearer ${authStore.refreshToken}`;

          const response = await api(originalRequest);

          const accessToken = response.headers['x-grupi-access-token'];
          const refreshToken = response.headers['x-grupi-refresh-token'];

          if (!accessToken || !refreshToken) {
            throw new Error('Token refresh failed: Missing required headers');
          }

          authStore.setTokens(accessToken, refreshToken);

          return response;
        } catch (refreshError: any) {
          if (refreshError.response?.status === 401) {
            handleLogout(authStore);
            return Promise.reject(refreshError);
          }

          if (refreshError.response?.headers) {
            const accessToken = refreshError.response.headers['x-grupi-access-token'];
            const refreshToken = refreshError.response.headers['x-grupi-refresh-token'];

            if (accessToken && refreshToken) {
              authStore.setTokens(accessToken, refreshToken);
            }
          }

          return Promise.reject(refreshError);
        }
      } else {
        handleLogout(authStore);
      }
    }

    if (error.response?.status === 403) {
      router.push('/unauthorized');
      return Promise.reject(error);
    }

    if (error.response?.status === 500) {
      router.push('/internal-server-error');
      return Promise.reject(error);
    }

    if (error.response?.status === 404) {
      router.push('/not-found');
      return Promise.reject(error);
    }

    return Promise.reject(error);
  }
);

function handleLogout(authStore: ReturnType<typeof useAuthStore>) {
  authStore.logout();
}

export default api;
