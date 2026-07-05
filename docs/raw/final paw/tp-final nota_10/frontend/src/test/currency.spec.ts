import { describe, it, expect, vi, beforeEach } from 'vitest';
import { formatCurrency, createCurrencyFormatter } from '@/utils/currency';
import i18n from '@/i18n';

describe('currency utils', () => {
  beforeEach(() => {
    i18n.global.locale.value = 'es';
  });

  it('formats in ARS using the current locale', () => {
    i18n.global.locale.value = 'es';
    const result = formatCurrency(1234.5);
    expect(result).toContain('$');
    expect(result).toContain('1.234');
  });

  it('falls back to en-US when locale is not Spanish', () => {
    i18n.global.locale.value = 'en';
    const result = formatCurrency(1234.5, 'USD');
    expect(result).toBe('$1,234.50');
  });

  it('returns a reusable formatter via createCurrencyFormatter', () => {
    i18n.global.locale.value = 'en';
    const usdFormatter = createCurrencyFormatter('USD');
    const arsFormatter = createCurrencyFormatter();

    expect(usdFormatter(100)).toBe('$100.00');
    const ars = arsFormatter(100);
    expect(ars).toMatch(/ARS|\$/);
  });
});
