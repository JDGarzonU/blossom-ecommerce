import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from '../App';
import api from '../services/api';
import { customerSession, order, product } from './fixtures';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
  getErrorMessage: () => 'No se pudo completar el pedido',
}));

beforeEach(() => {
  vi.mocked(api.get).mockImplementation((url) =>
    Promise.resolve({ data: url === '/orders/my-orders' ? [order] : [product] }));
});

describe('Detalle, carrito y checkout', () => {
  it('agrega un producto del catálogo al carrito y actualiza el total', async () => {
    render(<App />);
    await screen.findByRole('link', { name: product.name });
    fireEvent.click(screen.getByRole('button', { name: `Agregar ${product.name} al carrito` }));
    expect(JSON.parse(localStorage.getItem('blossom_cart'))).toEqual([
      expect.objectContaining({ id: 1, quantity: 1 }),
    ]);
    fireEvent.click(screen.getByRole('link', { name: /Carrito, 1 artículos/i }));
    expect(await screen.findByRole('heading', { name: 'Carrito de compras' })).toBeInTheDocument();
    expect(screen.getByRole('button', { name: 'Ingresar para comprar' })).toBeInTheDocument();
  });

  it('limita cantidades según stock y permite eliminar artículos', async () => {
    localStorage.setItem('blossom_cart', JSON.stringify([{ ...product, quantity: 2 }]));
    window.history.replaceState({}, '', '/cart');
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: 'Sumar' }));
    expect(JSON.parse(localStorage.getItem('blossom_cart'))[0].quantity).toBe(3);
    fireEvent.click(screen.getByRole('button', { name: 'Quitar producto' }));
    expect(await screen.findByText('Tu carrito está vacío')).toBeInTheDocument();
  });

  it('finaliza la compra y envía solamente IDs y cantidades al servidor', async () => {
    customerSession();
    localStorage.setItem('blossom_cart', JSON.stringify([{ ...product, quantity: 2 }]));
    vi.mocked(api.post).mockResolvedValueOnce({ data: order });
    window.history.replaceState({}, '', '/cart');
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: /Finalizar compra/i }));
    await waitFor(() => expect(api.post).toHaveBeenCalledWith('/orders', {
      items: [{ productId: 1, quantity: 2 }],
    }));
    expect(await screen.findByText('¡Pedido realizado correctamente!')).toBeInTheDocument();
    expect(localStorage.getItem('blossom_cart')).toBe('[]');
  });

  it('muestra error de compra y conserva el carrito para reintentar', async () => {
    customerSession();
    localStorage.setItem('blossom_cart', JSON.stringify([{ ...product, quantity: 2 }]));
    vi.mocked(api.post).mockRejectedValueOnce(new Error('Insufficient stock'));
    window.history.replaceState({}, '', '/cart');
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: /Finalizar compra/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo completar el pedido');
    expect(JSON.parse(localStorage.getItem('blossom_cart'))[0].quantity).toBe(2);
  });
});
