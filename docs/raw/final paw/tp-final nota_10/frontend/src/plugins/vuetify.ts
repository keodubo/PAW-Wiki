import '@mdi/font/css/materialdesignicons.css';
import 'vuetify/styles';

import { createVuetify } from 'vuetify';
import { aliases, mdi } from 'vuetify/iconsets/mdi';

import { VRating } from 'vuetify/components';

function getSystemTheme(): 'light' | 'dark' {
  if (typeof window !== 'undefined') {
    return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
  }
  return 'light';
}

export default createVuetify({
  components: {
    VRating,
  },
  theme: {
    defaultTheme: getSystemTheme(),
    themes: {
      light: {
        colors: {
          primary: '#7f00ff',
          secondary: '#9c27b0',
          accent: '#e91e63',
          error: '#f44336',
          warning: '#ff9800',
          info: '#2196f3',
          success: '#4caf50',
          background: '#e8e8e8',
          surface: '#ffffff',
        },
      },
      dark: {
        colors: {
          primary: '#bb86fc',
          secondary: '#cf6679',
          accent: '#03dac6',
          error: '#cf6679',
          warning: '#ffb74d',
          info: '#64b5f6',
          success: '#81c784',
        },
      },
    },
  },
  icons: {
    defaultSet: 'mdi',
    aliases,
    sets: {
      mdi,
    },
  },
});
