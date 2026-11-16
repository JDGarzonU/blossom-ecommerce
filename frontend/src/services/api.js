import axios from 'axios';

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080/api',
  timeout: 15000,
});

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem('blossom_token');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export function getErrorMessage(error) {
  if (!error.response) return 'No fue posible conectar con el servidor. Comprueba que Spring Boot esté activo.';
  const status = error.response.status;
  if (status === 401) return 'Sesión vencida o credenciales incorrectas. Inicia sesión nuevamente.';
  if (status === 403) return 'No tienes permisos para realizar esta operación.';
  const data = error.response.data;
  if (status === 409) return data?.detail || 'Conflicto: correo registrado o stock insuficiente.';
  if (status === 404) return 'El recurso solicitado no existe.';
  if (status === 400) return 'Verifica la información ingresada.';
  return status >= 500 ? 'Ocurrió un error en el servidor.' : (data?.detail || 'No fue posible completar la operación.');
}

export default api;
