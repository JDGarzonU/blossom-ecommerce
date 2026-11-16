
import '@testing-library/jest-dom/vitest';
import { cleanup } from '@testing-library/react';
import { afterEach, beforeEach, vi } from 'vitest';

// Almacenamiento independiente para las pruebas.
// Evita conflictos con el localStorage experimental de Node.js.
function createMemoryStorage() {
  const data = new Map();

  return {
    get length() {
      return data.size;
    },
    key(index) {
      return [...data.keys()][index] ?? null;
    },
    getItem(key) {
      return data.get(String(key)) ?? null;
    },
    setItem(key, value) {
      data.set(String(key), String(value));
    },
    removeItem(key) {
      data.delete(String(key));
    },
    clear() {
      data.clear();
    },
  };
}

const testLocalStorage = createMemoryStorage();

Object.defineProperty(globalThis, 'localStorage', {
  configurable: true,
  value: testLocalStorage,
});

beforeEach(() => {
  sessionStorage.clear();
  testLocalStorage.clear();
  window.history.replaceState({}, '', '/');
});

afterEach(() => {
  cleanup();
  vi.clearAllMocks();
  vi.restoreAllMocks();
});
