import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from '../App';
import api from '../services/api';
import { product } from './fixtures';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
  getErrorMessage: () => 'No se pudo cargar el catálogo',
}));

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: [product] });
});

describe('Catálogo y filtros', () => {
  it('carga productos reales del contrato API simulado', async () => {
    render(<App />);
    expect(await screen.findByRole('link', { name: product.name })).toBeInTheDocument();
    expect(screen.getByText('15 disponibles')).toBeInTheDocument();
    expect(api.get).toHaveBeenCalledWith('/products', expect.objectContaining({ params: {} }));
  });

  it('envía nombre, categoría y rango de precio al backend', async () => {
    render(<App />);
    await screen.findByRole('link', { name: product.name });
    fireEvent.change(screen.getByPlaceholderText('Buscar productos'), { target: { value: 'Teclado' } });
    fireEvent.change(screen.getByPlaceholderText('Ej. Perifericos'), { target: { value: 'Perifericos' } });
    fireEvent.change(screen.getByPlaceholderText('$0'), { target: { value: '100000' } });
    fireEvent.change(screen.getByPlaceholderText('Sin límite'), { target: { value: '200000' } });
    fireEvent.click(screen.getByRole('button', { name: 'Buscar' }));
    await waitFor(() => expect(api.get).toHaveBeenLastCalledWith('/products', expect.objectContaining({
      params: { name: 'Teclado', category: 'Perifericos', minPrice: '100000', maxPrice: '200000' },
    })));
  });

  it('muestra estado vacío si la API no tiene productos', async () => {
    vi.mocked(api.get).mockResolvedValueOnce({ data: [] });
    render(<App />);
    expect(await screen.findByText('Sin resultados')).toBeInTheDocument();
  });

  it('muestra un error de conexión sin inventar productos', async () => {
    vi.mocked(api.get).mockRejectedValueOnce(new Error('Network Error'));
    render(<App />);
    expect(await screen.findByRole('alert')).toHaveTextContent('No se pudo cargar el catálogo');
    expect(screen.getByRole('button', { name: /Reintentar/i })).toBeInTheDocument();
  });
});
