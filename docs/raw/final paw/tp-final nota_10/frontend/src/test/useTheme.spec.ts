import { describe, it, expect, beforeEach, vi } from 'vitest';
import { flushPromises } from '@vue/test-utils';
import { useTheme } from '@/composables/useTheme';

const changeMock = vi.fn();
const themeMock = {
  change: changeMock,
  global: {
    name: { value: 'light' },
  },
};

vi.mock('vuetify', () => ({
  useTheme: () => themeMock,
}));

const mountComposable = async () => {
  let api: ReturnType<typeof useTheme> | null = null;
  // Mount a dummy component to give lifecycle context
  const app = {
    setup() {
      api = useTheme();
      return () => null;
    },
  };
  const { mount } = await import('@vue/test-utils');
  mount(app);
  await flushPromises();
  return api as unknown as ReturnType<typeof useTheme>;
};

describe('useTheme', () => {
  beforeEach(() => {
    changeMock.mockClear();
    themeMock.global.name.value = 'light';
    localStorage.clear();
    // default matchMedia to dark
    window.matchMedia = vi.fn().mockReturnValue({
      matches: true,
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
    }) as any;
  });

  it('sets manual theme and stores preference', async () => {
    const { updateTheme, isAutoTheme } = await mountComposable();

    updateTheme('dark');

    expect(isAutoTheme.value).toBe(false);
    expect(changeMock).toHaveBeenCalledWith('dark');
    expect(localStorage.getItem('theme-preference')).toBe('dark');
  });

  it('auto mode uses system theme and persists preference', async () => {
    const { updateTheme, isAutoTheme, systemTheme } = await mountComposable();

    updateTheme('auto');

    expect(isAutoTheme.value).toBe(true);
    expect(systemTheme.value).toBe('dark');
    expect(changeMock).toHaveBeenCalledWith('dark');
    expect(localStorage.getItem('theme-preference')).toBe('auto');
  });

  it('toggles between light and dark when set manually', async () => {
    const { updateTheme, toggleTheme } = await mountComposable();
    themeMock.global.name.value = 'dark';
    updateTheme('dark');
    changeMock.mockClear();

    toggleTheme();

    expect(changeMock).toHaveBeenCalledWith('light');
    expect(localStorage.getItem('theme-preference')).toBe('light');
  });
});
