import i18n from '@/i18n';

export const DEFAULT_CURRENCY = 'ARS';

const getCurrencyLocale = (locale: string): string => {
  if (locale.startsWith('es')) return 'es-AR';
  return 'en-US';
};

export const formatCurrency = (value: number, currency: string = DEFAULT_CURRENCY): string => {
  const currentLocale = i18n.global.locale.value as string;
  const currencyLocale = getCurrencyLocale(currentLocale);

  return new Intl.NumberFormat(currencyLocale, {
    style: 'currency',
    currency,
  }).format(value);
};

export const createCurrencyFormatter = (currency: string = DEFAULT_CURRENCY) => {
  return (value: number): string => formatCurrency(value, currency);
};
