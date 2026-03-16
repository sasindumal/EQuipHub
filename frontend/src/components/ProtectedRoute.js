'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';

export default function ProtectedRoute({ children, allowedRoles }) {
    const router = useRouter();
    const { user, loading } = useAuth();

    useEffect(() => {
        if (!loading) {
            if (!user) {
                router.replace('/login');
            } else if (allowedRoles && !allowedRoles.includes(user.role)) {
                // Redirect to their correct dashboard
                const { getRedirectPath } = require('@/lib/auth');
                router.replace('/login');
            }
        }
    }, [user, loading, router, allowedRoles]);

    if (loading) {
        return (
            <div className="page-loader">
                <div className="page-loader-spinner" />
            </div>
        );
    }

    if (!user) return null;
    if (allowedRoles && !allowedRoles.includes(user.role)) return null;

    return children;
}
