import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen } from '@testing-library/react';
import App from '../App';
import api from '../services/api';
import { customerSession, order, product } from './fixtures';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
  getErrorMessage: () => 'Error de red',
}));

beforeEach(() => {
  vi.mocked(api.get).mockImplementation((url) =>
    Promise.resolve({ data: url === '/orders/my-orders' ? [order] : url.startsWith('/products/') ? product : [product] }));
});

describe('Detalle, navegación y rutas protegidas', () => {
  it('abre la página de detalle y permite elegir cantidad', async () => {
    window.history.replaceState({}, '', '/products/1');
    render(<App />);
    expect(await screen.findByRole('heading', { name: product.name })).toBeInTheDocument();
    fireEvent.click(screen.getByRole('button', { name: 'Sumar cantidad' }));
    fireEvent.click(screen.getByRole('button', { name: /Agregar al carrito/i }));
    expect(JSON.parse(localStorage.getItem('blossom_cart'))[0].quantity).toBe(2);
  });

  it('redirige al login cuando un visitante intenta consultar pedidos', async () => {
    window.history.replaceState({}, '', '/orders');
    render(<App />);
    expect(await screen.findByRole('heading', { name: 'Inicia sesión' })).toBeInTheDocument();
    expect(api.get).not.toHaveBeenCalledWith('/orders/my-orders');
  });

  it('permite a un cliente ver su historial', async () => {
    customerSession();
    window.history.replaceState({}, '', '/orders');
    render(<App />);
    expect(await screen.findByText(/PEDIDO #1/)).toBeInTheDocument();
    expect(screen.getByText('Total del pedido')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/orders/my-orders');
  });

  it('cierra sesión y elimina el token de la sesión', async () => {
    customerSession();
    render(<App />);
    fireEvent.click(screen.getByRole('button', { name: /Salir/i }));
    expect(sessionStorage.getItem('blossom_token')).toBeNull();
    expect(await screen.findByRole('link', { name: /Ingresar/i })).toBeInTheDocument();
  });
});
