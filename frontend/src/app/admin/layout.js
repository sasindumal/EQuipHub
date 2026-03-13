'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';

export default function AdminLayout({ children }) {
    const router = useRouter();
    const { user, loading } = useAuth();

    useEffect(() => {
        if (!loading) {
            if (!user) {
                router.replace('/login');
            } else if (user.role !== 'SYSTEMADMIN') {
                router.replace('/login');
            }
        }
    }, [user, loading, router]);

    if (loading) {
        return (
            <div className="page-loader">
                <div className="page-loader-spinner" />
            </div>
        );
    }

    if (!user || user.role !== 'SYSTEMADMIN') return null;

    return <>{children}</>;
}
