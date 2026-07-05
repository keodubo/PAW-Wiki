import { ref, onMounted, onUnmounted } from 'vue';
import { useTheme as useVuetifyTheme } from 'vuetify';

export function useTheme() {
  const theme = useVuetifyTheme();
  const systemTheme = ref<'light' | 'dark'>('light');
  const isAutoTheme = ref(true);

  const getSystemTheme = (): 'light' | 'dark' => {
    if (typeof window === 'undefined') return 'light';
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  };

  const updateTheme = (themeName: 'light' | 'dark' | 'auto') => {
    if (themeName === 'auto') {
      isAutoTheme.value = true;
      systemTheme.value = getSystemTheme();
      theme.change(systemTheme.value);
      localStorage.setItem('theme-preference', 'auto');
    } else {
      isAutoTheme.value = false;
      theme.change(themeName);
      localStorage.setItem('theme-preference', themeName);
    }
  };

  const handleSystemThemeChange = (e: MediaQueryListEvent) => {
    if (isAutoTheme.value) {
      systemTheme.value = e.matches ? 'dark' : 'light';
      theme.change(systemTheme.value);
    }
  };

  const initializeTheme = () => {
    try {
      const savedPreference = localStorage.getItem('theme-preference');

      if (savedPreference && ['light', 'dark', 'auto'].includes(savedPreference)) {
        updateTheme(savedPreference as 'light' | 'dark' | 'auto');
      } else {
        updateTheme('auto');
      }
    } catch (error) {
      console.warn('Failed to load theme preference from localStorage, using auto mode');
      updateTheme('auto');
    }
  };

  const toggleTheme = () => {
    if (isAutoTheme.value) {
      const currentTheme = theme.global.name.value as string;
      updateTheme(currentTheme === 'dark' ? 'light' : 'dark');
    } else {
      const currentTheme = theme.global.name.value as string;
      updateTheme(currentTheme === 'dark' ? 'light' : 'dark');
    }
  };

  const getCurrentTheme = () => ({
    current: theme.global.name.value as string,
    isAuto: isAutoTheme.value,
    system: systemTheme.value,
  });

  let mediaQuery: MediaQueryList;

  onMounted(() => {
    if (typeof document !== 'undefined') {
      document.documentElement.classList.add('no-transition');

      initializeTheme();

      setTimeout(() => {
        document.documentElement.classList.remove('no-transition');
      }, 100);
    }

    if (typeof window !== 'undefined') {
      mediaQuery = window.matchMedia('(prefers-color-scheme: dark)');
      mediaQuery.addEventListener('change', handleSystemThemeChange);

      systemTheme.value = getSystemTheme();
    }
  });

  onUnmounted(() => {
    if (mediaQuery) {
      mediaQuery.removeEventListener('change', handleSystemThemeChange);
    }
  });

  return {
    updateTheme,
    toggleTheme,
    getCurrentTheme,
    isAutoTheme,
    systemTheme,
  };
}
