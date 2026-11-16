import { useContext } from 'react';
import { CartContext } from './CartContextValue';

export function useCart() {
  const value = useContext(CartContext);
  if (!value) throw new Error('useCart requiere CartProvider');
  return value;
}
