'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
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

    const fetchUser = useCallback(async () => {
        try {
            const token = localStorage.getItem('equiphub_token');
            if (!token) {
                setLoading(false);
                return;
            }
            const res  = await authAPI.getCurrentUser();
            // Backend wraps: { success, message, data: { userId, role, ... } }
            // Unwrap with extractUser so user.role is never undefined.
            const parsed = extractUser(res.data);
            if (!parsed) throw new Error('Invalid user payload from /auth/me');
            setUser(parsed);
        } catch {
            localStorage.removeItem('equiphub_token');
            localStorage.removeItem('equiphub_user');
            setUser(null);
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
        // Login endpoint returns token + user fields at top level
        localStorage.setItem('equiphub_token', data.token);
        localStorage.setItem('equiphub_user', JSON.stringify(data));
        const parsed = extractUser(data);
        setUser(parsed);
        return data;
    };

    const logout = () => {
        localStorage.removeItem('equiphub_token');
        localStorage.removeItem('equiphub_user');
        setUser(null);
        window.location.href = '/login';
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
