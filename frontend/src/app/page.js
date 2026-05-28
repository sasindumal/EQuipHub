'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { useAuth } from '@/lib/auth';

export default function Home() {
  const router = useRouter();
  const { user, loading, getRedirectPath } = useAuth();

  useEffect(() => {
    if (!loading) {
      if (user) {
        // Authenticated users go to their role-specific dashboard
        router.replace(getRedirectPath(user.role));
      } else {
        // BUG-3 FIX: unauthenticated users land on the welcome/landing page
        router.replace('/welcome');
      }
    }
  }, [user, loading, router, getRedirectPath]);

  return (
    <div className="page-loader">
      <div className="page-loader-spinner" />
    </div>
  );
}
