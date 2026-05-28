'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { useRouter } from 'next/navigation';
import { authAPI } from './api';

const AuthContext = createContext(null);

// Safely extract the user payload regardless of whether the backend
// wraps it as { success, data: {...} } or returns the object directly.
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
    const router                = useRouter();

    const fetchUser = useCallback(async () => {
        try {
            const token = localStorage.getItem('equiphub_token');
            if (!token) {
                setLoading(false);
                return;
            }
            const res    = await authAPI.getCurrentUser();
            const parsed = extractUser(res.data);
            if (!parsed) throw new Error('Invalid user payload from /auth/me');
            setUser(parsed);
        } catch (err) {
            const status = err?.response?.status;
            if (status === 401 || status === 403) {
                localStorage.removeItem('equiphub_token');
                localStorage.removeItem('equiphub_user');
                setUser(null);
            } else {
                // Network/server error — try to restore from localStorage cache
                const cached = localStorage.getItem('equiphub_user');
                if (cached) {
                    try {
                        // BUG-2 FIX: renamed inner variable to avoid shadowing outer `parsed`
                        const cachedUser = extractUser(JSON.parse(cached));
                        if (cachedUser) { setUser(cachedUser); return; }
                    } catch { /* ignore parse errors */ }
                }
                setUser(null);
            }
        } finally {
            setLoading(false);
        }
    }, []);

    useEffect(() => {
        fetchUser();
    }, [fetchUser]);

    const login = async (email, password) => {
        const res  = await authAPI.login({ email, password });
        const data = res.data;
        localStorage.setItem('equiphub_token', data.token);
        localStorage.setItem('equiphub_user', JSON.stringify(data));
        const parsed = extractUser(data);
        setUser(parsed);
        return data;
    };

    // BUG-10 FIX: use router.push instead of window.location.href to avoid full page reload
    const logout = () => {
        localStorage.removeItem('equiphub_token');
        localStorage.removeItem('equiphub_user');
        setUser(null);
        router.push('/login');
    };

    const getRedirectPath = (role) => {
        switch (role) {
            case 'SYSTEMADMIN':      return '/admin';
            case 'DEPARTMENTADMIN':
            case 'HEADOFDEPARTMENT': return '/department-admin';
            case 'TECHNICALOFFICER': return '/technical-officer';
            case 'LECTURER':
            case 'APPOINTEDLECTURER':
            case 'INSTRUCTOR':       return '/lecturer';
            case 'STUDENT':          return '/student';
            default:                 return '/login';
        }
    };

    return (
        <AuthContext.Provider value={{ user, loading, login, logout, fetchUser, getRedirectPath }}>
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within an AuthProvider');
    }
    return context;
}
