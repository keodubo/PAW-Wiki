import { createI18n } from 'vue-i18n';
import en from './locales/en.json';
import es from './locales/es.json';

export type Locale = 'en' | 'es';

const defaultLocale: Locale = 'en';

const getLocale = (): Locale => {
  const savedLocale = localStorage.getItem('locale') as Locale | null;
  return savedLocale && ['en', 'es'].includes(savedLocale) ? savedLocale : defaultLocale;
};

const i18n = createI18n({
  legacy: false,
  locale: getLocale(),
  fallbackLocale: 'en',
  messages: { en, es },
  globalInjection: true,
});

export const setLocale = (locale: Locale) => {
  i18n.global.locale.value = locale;
  localStorage.setItem('locale', locale);
};

export const getCurrentLocale = (): Locale => {
  return i18n.global.locale.value as Locale;
};

export default i18n;
