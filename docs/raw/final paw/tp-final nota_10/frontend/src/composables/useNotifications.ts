import { ref } from 'vue';
import { useI18n } from 'vue-i18n';
import { extractValidationErrors, hasValidationErrors } from '@/utils/validationErrors';

export interface NotificationMessage {
  id: string;
  type: 'success' | 'error' | 'warning' | 'info';
  code: string;
  message: string;
  icon: string;
  timeout?: number;
  visible: boolean;
}

const notifications = ref<NotificationMessage[]>([]);

export function useNotifications() {
  const { t } = useI18n();

  const addNotification = (type: NotificationMessage['type'], message: string, code: string = '', timeout: number = 5000) => {
    const iconMap = {
      success: 'mdi-check-circle',
      error: 'mdi-alert-circle',
      warning: 'mdi-alert',
      info: 'mdi-information',
    };

    const notification: NotificationMessage = {
      id: Date.now().toString() + Math.random().toString(36),
      type,
      code: code || message,
      message,
      icon: iconMap[type],
      timeout,
      visible: true,
    };

    notifications.value.push(notification);

    if (timeout > 0) {
      setTimeout(() => {
        removeNotification(notification.id);
      }, timeout);
    }

    return notification.id;
  };

  const removeNotification = (id: string) => {
    const index = notifications.value.findIndex((n) => n.id === id);
    if (index > -1) notifications.value.splice(index, 1);
  };

  const clearAll = () => {
    notifications.value = [];
  };

  const showSuccess = (message: string, timeout?: number) => addNotification('success', message, '', timeout);
  const showError = (message: string, timeout?: number) => addNotification('error', message, '', timeout);
  const showWarning = (message: string, timeout?: number) => addNotification('warning', message, '', timeout);
  const showInfo = (message: string, timeout?: number) => addNotification('info', message, '', timeout);

  const handleApiError = (error: any, options: { showToast?: boolean } = {}) => {
    const { showToast = true } = options;

    if (hasValidationErrors(error)) {
      const fieldErrors = extractValidationErrors(error);

      if (showToast) {
        const title = error?.response?.data?.title || t('validation_error');
        showError(title);
      }

      return { validationErrors: fieldErrors };
    }

    let message = t('unexpected_error');

    if (error?.response?.data?.title) {
      message = error.response.data.title;
    } else if (error?.response?.data?.message) {
      message = error.response.data.message;
    } else if (error?.response?.data?.error) {
      message = error.response.data.error;
    } else if (error?.message) {
      if (error.message.includes('Network Error')) message = t('network_error_connection');
      else if (error.message.includes('timeout')) message = t('request_timeout');
      else if (error.message.includes('Failed to fetch')) message = t('server_unavailable');
      else message = error.message;
    } else if (error?.code) {
      switch (error.code) {
        case 'ECONNABORTED':
          message = t('request_timeout');
          break;
        case 'ERR_NETWORK':
          message = t('network_error');
          break;
        default:
          message = t('connection_error');
      }
    }

    if (showToast) showError(message);
    return { validationErrors: {} };
  };

  return {
    notifications,
    addNotification,
    removeNotification,
    clearAll,
    showSuccess,
    showError,
    showWarning,
    showInfo,
    handleApiError,
    extractValidationErrors,
    hasValidationErrors,
  };
}
