import { createContext, useContext, useMemo, useState } from 'react';
import api from '../services/api';

const AuthContext = createContext(null);
const storageKey = 'blossom_user';
const tokenKey = 'blossom_token';

function getInitialUser() {
  try {
    const raw = sessionStorage.getItem(storageKey);
    if (!sessionStorage.getItem(tokenKey) || !raw) return null;
    return JSON.parse(raw);
  } catch { return null; }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(getInitialUser);

  function saveSession(data) {
    const session = { id: data.userId, name: data.name, email: data.email, role: data.role };
    sessionStorage.setItem(tokenKey, data.accessToken);
    sessionStorage.setItem(storageKey, JSON.stringify(session));
    setUser(session);
    return session;
  }

  async function login(payload) {
    const { data } = await api.post('/auth/login', payload);
    return saveSession(data);
  }

  async function register(payload) {
    const { data } = await api.post('/auth/register', payload);
    return saveSession(data);
  }

  function logout() {
    sessionStorage.removeItem(tokenKey);
    sessionStorage.removeItem(storageKey);
    setUser(null);
  }

  const value = useMemo(() => ({ user, login, register, logout, isAuthenticated: !!user }), [user]);
  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth debe usarse dentro de AuthProvider');
  return ctx;
}
