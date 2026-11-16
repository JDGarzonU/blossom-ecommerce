import { createContext, useContext, useMemo, useState } from 'react';

const CartContext = createContext(null);
const key = 'blossom_cart';
function getInitialCart() {
  try {
    const saved = JSON.parse(localStorage.getItem(key) || '[]');
    return Array.isArray(saved) ? saved : [];
  } catch { return []; }
}

export function CartProvider({ children }) {
  const [items, setItems] = useState(getInitialCart);
  function update(next) { setItems(next); localStorage.setItem(key, JSON.stringify(next)); }
  function add(product, quantity = 1) {
    if (!product?.active || Number(product.stock) < 1) return;
    const current = items.find((i) => i.id === product.id);
    const allowed = Number(product.stock);
    const updated = current
      ? items.map(i => i.id === product.id ? { ...product, quantity: Math.min(allowed, i.quantity + quantity) } : i)
      : [...items, { ...product, quantity: Math.min(allowed, quantity) }];
    update(updated);
  }
  function changeQuantity(id, quantity) {
    if (quantity <= 0) { remove(id); return; }
    update(items.map(i => i.id === id ? { ...i, quantity: Math.min(Number(i.stock), quantity) } : i));
  }
  function remove(id) { update(items.filter(i => i.id !== id)); }
  function clear() { update([]); }
  const count = items.reduce((s, i) => s + i.quantity, 0);
  const total = items.reduce((s, i) => s + Number(i.price) * i.quantity, 0);
  const value = useMemo(() => ({ items, add, changeQuantity, remove, clear, count, total }), [items, count, total]);
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const ctx = useContext(CartContext);
  if (!ctx) throw new Error('useCart debe usarse dentro de CartProvider');
  return ctx;
}
