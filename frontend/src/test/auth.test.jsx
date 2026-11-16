import { beforeEach, describe, expect, it, vi } from 'vitest';
import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from '../App';
import api from '../services/api';
import { authResponse } from './fixtures';

vi.mock('../services/api', () => ({
  default: { get: vi.fn(), post: vi.fn() },
  getErrorMessage: () => 'Credenciales incorrectas',
}));

beforeEach(() => {
  vi.mocked(api.get).mockResolvedValue({ data: [] });
});

describe('Login y registro', () => {
  it('inicia sesión, guarda el token y muestra la cuenta', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({ data: authResponse });
    window.history.replaceState({}, '', '/login');
    render(<App />);

    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'cliente@blossom.test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Tu contraseña'), {
      target: { value: 'Blossom123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Ingresar/i }));

    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/auth/login', {
        email: 'cliente@blossom.test', password: 'Blossom123!',
      });
      expect(sessionStorage.getItem('blossom_token')).toBe('unit-test-token');
    });
    expect(await screen.findByRole('button', { name: /Salir/i })).toBeInTheDocument();
  });

  it('registra clientes y les asigna sesión', async () => {
    vi.mocked(api.post).mockResolvedValueOnce({ data: authResponse });
    window.history.replaceState({}, '', '/register');
    render(<App />);
    fireEvent.change(screen.getByPlaceholderText('Tu nombre'), {
      target: { value: 'Cliente Prueba' },
    });
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'cliente@blossom.test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Tu contraseña'), {
      target: { value: 'Blossom123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Crear cuenta/i }));
    await waitFor(() => {
      expect(api.post).toHaveBeenCalledWith('/auth/register', expect.objectContaining({
        name: 'Cliente Prueba', email: 'cliente@blossom.test',
      }));
    });
    expect(sessionStorage.getItem('blossom_token')).toBe('unit-test-token');
  });

  it('muestra error al fallar el login sin guardar credenciales', async () => {
    vi.mocked(api.post).mockRejectedValueOnce(new Error('Unauthorized'));
    window.history.replaceState({}, '', '/login');
    render(<App />);
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'cliente@blossom.test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Tu contraseña'), {
      target: { value: 'Incorrecta123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Ingresar/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Credenciales incorrectas');
    expect(sessionStorage.getItem('blossom_token')).toBeNull();
  });

  it('rechaza nombre demasiado corto antes de registrar', async () => {
    window.history.replaceState({}, '', '/register');
    render(<App />);
    fireEvent.change(screen.getByPlaceholderText('Tu nombre'), { target: { value: 'A' } });
    fireEvent.change(screen.getByPlaceholderText('correo@ejemplo.com'), {
      target: { value: 'cliente@blossom.test' },
    });
    fireEvent.change(screen.getByPlaceholderText('Tu contraseña'), {
      target: { value: 'Blossom123!' },
    });
    fireEvent.click(screen.getByRole('button', { name: /Crear cuenta/i }));
    expect(await screen.findByRole('alert')).toHaveTextContent('Ingresa un nombre válido');
    expect(api.post).not.toHaveBeenCalled();
  });
});
