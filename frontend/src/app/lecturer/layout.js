'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';

export default function LecturerLayout({ children }) {
    const router = useRouter();
    const { user, loading, getRedirectPath } = useAuth();

    useEffect(() => {
        if (!loading) {
            if (!user) {
                router.replace('/login');
            } else if (!['LECTURER', 'APPOINTEDLECTURER', 'INSTRUCTOR', 'HEADOFDEPARTMENT', 'SYSTEMADMIN'].includes(user.role)) {
                router.replace(getRedirectPath(user.role));
            }
        }
    }, [user, loading, router, getRedirectPath]);

    if (loading) return <div className="page-loader"><div className="page-loader-spinner" /></div>;
    if (!user || !['LECTURER', 'APPOINTEDLECTURER', 'INSTRUCTOR', 'HEADOFDEPARTMENT', 'SYSTEMADMIN'].includes(user.role)) return null;

    return <>{children}</>;
}
