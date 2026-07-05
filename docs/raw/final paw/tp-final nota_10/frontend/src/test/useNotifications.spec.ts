import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { useNotifications } from '@/composables/useNotifications';

describe('useNotifications', () => {
  beforeEach(() => {
    const { clearAll } = useNotifications();
    clearAll();
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('adds and removes notifications manually', () => {
    const { notifications, addNotification, removeNotification } = useNotifications();
    const id = addNotification('success', 'ok', 'code-1', 0);

    expect(notifications.value).toHaveLength(1);
    expect(notifications.value[0]).toMatchObject({
      id,
      type: 'success',
      code: 'code-1',
      message: 'ok',
      icon: 'mdi-check-circle',
      visible: true,
    });

    removeNotification(id);
    expect(notifications.value).toHaveLength(0);
  });

  it('auto-removes notifications after timeout', () => {
    const { notifications, addNotification } = useNotifications();
    addNotification('error', 'fail', '', 100);

    expect(notifications.value).toHaveLength(1);
    vi.advanceTimersByTime(100);

    expect(notifications.value).toHaveLength(0);
  });

  it('helper methods map to addNotification with proper icons', () => {
    const { notifications, showSuccess, showError, showWarning, showInfo } = useNotifications();

    showSuccess('ok', 0);
    showError('bad', 0);
    showWarning('warn', 0);
    showInfo('info', 0);

    expect(notifications.value.map((n) => n.icon)).toEqual(['mdi-check-circle', 'mdi-alert-circle', 'mdi-alert', 'mdi-information']);
  });

  it('handleApiError derives user-friendly messages', () => {
    const { notifications, handleApiError, clearAll } = useNotifications();
    clearAll();

    handleApiError({ message: 'Network Error' });
    expect(notifications.value[0].message).toContain('network_error_connection');
    clearAll();

    handleApiError({ code: 'ERR_NETWORK' });
    expect(notifications.value[0].message).toContain('network_error');
  });
});
