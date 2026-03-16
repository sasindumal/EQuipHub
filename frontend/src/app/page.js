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
        router.replace(getRedirectPath(user.role));
      } else {
        router.replace('/login');
      }
    }
  }, [user, loading, router, getRedirectPath]);

  return (
    <div className="page-loader">
      <div className="page-loader-spinner" />
    </div>
  );
}
