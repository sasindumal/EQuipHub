'use client';

import { createContext, useContext, useState, useEffect, useCallback } from 'react';
import { authAPI } from './api';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    const fetchUser = useCallback(async () => {
        try {
            const token = localStorage.getItem('equiphub_token');
            if (!token) {
                setLoading(false);
                return;
            }
            const res = await authAPI.getCurrentUser();
            setUser(res.data);
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
        const res = await authAPI.login({ email, password });
        const data = res.data;
        localStorage.setItem('equiphub_token', data.token);
        localStorage.setItem('equiphub_user', JSON.stringify(data));
        setUser({
            userId:       data.userId,
            email:        data.email,
            firstName:    data.firstName,
            lastName:     data.lastName,
            role:         data.role,
            departmentId: data.departmentId,
        });
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
            case 'SYSTEMADMIN':
                return '/admin';
            case 'DEPARTMENTADMIN':
            case 'HEADOFDEPARTMENT':
                return '/department-admin';
            case 'TECHNICALOFFICER':
                return '/technical-officer';
            case 'LECTURER':
            case 'APPOINTEDLECTURER':
            case 'INSTRUCTOR':
                return '/lecturer';
            case 'STUDENT':
                return '/student';
            default:
                return '/login';
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
