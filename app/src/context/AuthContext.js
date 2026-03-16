import React, { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI, tokenStore, setLogoutHandler } from '../lib/api';

const AuthContext = createContext(null);

const extractUser = (payload) => {
  const raw = payload?.data ?? payload;
  if (!raw?.userId && !raw?.role) return null;
  return {
    userId:       raw.userId,
    email:        raw.email,
    firstName:    raw.firstName,
    lastName:     raw.lastName,
    role:         raw.role,
    departmentId: raw.departmentId,
    indexNumber:  raw.indexNumber,
    status:       raw.status,
  };
};

export function AuthProvider({ children }) {
  const [user, setUser]       = useState(null);
  const [loading, setLoading] = useState(true);

  const clearSession = useCallback(() => {
    tokenStore.clear();
    setUser(null);
  }, []);

  useEffect(() => {
    setLogoutHandler(clearSession);
    bootstrap();
  }, []);

  const bootstrap = async () => {
    try {
      const token = await tokenStore.getToken();
      if (!token) { setLoading(false); return; }
      const res = await authAPI.getCurrentUser();
      const parsed = extractUser(res.data);
      if (!parsed) throw new Error('bad payload');
      setUser(parsed);
    } catch {
      await tokenStore.clear();
      setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const login = async (email, password) => {
    const res  = await authAPI.login({ email, password });
    const data = res.data;
    await tokenStore.setToken(data.token);
    await tokenStore.setUser(data);
    const parsed = extractUser(data);
    setUser(parsed);
    return data;
  };

  const logout = async () => {
    await tokenStore.clear();
    setUser(null);
  };

  const register = async (formData) => {
    await authAPI.register(formData);
  };

  const verifyEmail = async (email, code) => {
    await authAPI.verifyEmail({ email, code });
  };

  const resendCode = async (email) => {
    await authAPI.resendCode(email);
  };

  return (
    <AuthContext.Provider value={{ user, loading, login, logout, register, verifyEmail, resendCode }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be inside AuthProvider');
  return ctx;
}
