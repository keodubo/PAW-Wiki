import { afterEach, vi } from 'vitest';
import { cleanup } from '@testing-library/vue';

afterEach(() => {
  cleanup();
});

if (!('localStorage' in globalThis) || typeof localStorage.getItem !== 'function') {
  const localStorageMock = (() => {
    let store: Record<string, string> = {};
    return {
      getItem: (key: string) => store[key] || null,
      setItem: (key: string, value: string) => {
        store[key] = value.toString();
      },
      removeItem: (key: string) => {
        delete store[key];
      },
      clear: () => {
        store = {};
      },
      get length() {
        return Object.keys(store).length;
      },
      key: (index: number) => {
        const keys = Object.keys(store);
        return keys[index] || null;
      },
    };
  })();

  Object.defineProperty(globalThis, 'localStorage', {
    value: localStorageMock,
    writable: true,
    configurable: true,
  });
}

if (!('matchMedia' in window)) {
  Object.defineProperty(window, 'matchMedia', {
    writable: true,
    value: (query: string) => ({
      media: query,
      matches: false,
      onchange: null,
      addListener: vi.fn(),
      removeListener: vi.fn(),
      addEventListener: vi.fn(),
      removeEventListener: vi.fn(),
      dispatchEvent: vi.fn(),
    }),
  });
}

class ResizeObserverStub implements ResizeObserver {
  observe = vi.fn();
  unobserve = vi.fn();
  disconnect = vi.fn();
}

if (!('ResizeObserver' in window)) {
  // @ts-expect-error allow installing stub globally for tests
  window.ResizeObserver = ResizeObserverStub;
}

const vuetifyCssModules = ['VBtn', 'VCard', 'VCardText', 'VCardTitle', 'VRow', 'VCol', 'VChip', 'VIcon', 'VImg', 'VRating', 'VProgressLinear', 'VDivider'];
vuetifyCssModules.forEach((name) => {
  vi.mock(`vuetify/lib/components/${name}/${name}.css`, () => ({}));
});

vi.mock('vue-i18n', async (importOriginal) => {
  const actual = await importOriginal<typeof import('vue-i18n')>();

  return {
    ...actual,
    useI18n: () => ({
      t: (key: string) => key,
      d: (date: any) => date,
      n: (val: any) => val,
    }),
  };
});
