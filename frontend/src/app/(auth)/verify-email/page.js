'use client';

import { useState, useRef, useEffect, Suspense } from 'react';
import { useRouter, useSearchParams } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { HiOutlineMail, HiOutlineShieldCheck } from 'react-icons/hi';
import { authAPI } from '@/lib/api';
import '../auth.css';

function VerifyEmailContent() {
    const router = useRouter();
    const searchParams = useSearchParams();
    const emailParam = searchParams.get('email') || '';
    const [code, setCode] = useState(['', '', '', '', '', '']);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);
    const [resending, setResending] = useState(false);
    const inputRefs = useRef([]);

    useEffect(() => {
        inputRefs.current[0]?.focus();
    }, []);

    const handleChange = (index, value) => {
        if (!/^\d*$/.test(value)) return;
        const newCode = [...code];
        newCode[index] = value.slice(-1);
        setCode(newCode);
        if (value && index < 5) {
            inputRefs.current[index + 1]?.focus();
        }
    };

    const handleKeyDown = (index, e) => {
        if (e.key === 'Backspace' && !code[index] && index > 0) {
            inputRefs.current[index - 1]?.focus();
        }
    };

    const handlePaste = (e) => {
        e.preventDefault();
        const pastedData = e.clipboardData.getData('text').slice(0, 6);
        if (/^\d+$/.test(pastedData)) {
            const newCode = [...code];
            for (let i = 0; i < pastedData.length; i++) {
                newCode[i] = pastedData[i];
            }
            setCode(newCode);
            const nextIndex = Math.min(pastedData.length, 5);
            inputRefs.current[nextIndex]?.focus();
        }
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setSuccess('');
        const fullCode = code.join('');
        if (fullCode.length !== 6) {
            setError('Please enter the complete 6-digit code');
            return;
        }
        setLoading(true);
        try {
            await authAPI.verifyEmail({ email: emailParam, code: fullCode });
            setSuccess('Email verified successfully! Redirecting to login...');
            setTimeout(() => router.push('/login'), 2000);
        } catch (err) {
            setError(err.response?.data?.message || 'Verification failed. Please try again.');
        } finally {
            setLoading(false);
        }
    };

    const handleResend = async () => {
        if (!emailParam) {
            setError('Email address is missing');
            return;
        }
        setResending(true);
        setError('');
        try {
            await authAPI.resendCode(emailParam);
            setSuccess('A new verification code has been sent to your email.');
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to resend code.');
        } finally {
            setResending(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-logo">
                        <Image src="/logo.png" alt="EQuipHub" width={56} height={56} priority />
                    </div>

                    <div style={{ display: 'flex', justifyContent: 'center', marginBottom: 16 }}>
                        <div style={{
                            width: 64, height: 64, borderRadius: '50%',
                            background: 'rgba(61, 82, 160, 0.08)',
                            display: 'flex', alignItems: 'center', justifyContent: 'center'
                        }}>
                            <HiOutlineShieldCheck style={{ fontSize: 32, color: 'var(--primary)' }} />
                        </div>
                    </div>

                    <h1 className="auth-title">Verify Your Email</h1>
                    <p className="auth-subtitle">
                        We sent a 6-digit code to<br />
                        <strong style={{ color: 'var(--primary)' }}>{emailParam || 'your email'}</strong>
                    </p>

                    {error && <div className="auth-alert error">{error}</div>}
                    {success && <div className="auth-alert success">{success}</div>}

                    <form onSubmit={handleSubmit}>
                        <div className="code-inputs" onPaste={handlePaste}>
                            {code.map((digit, i) => (
                                <input
                                    key={i}
                                    ref={(el) => (inputRefs.current[i] = el)}
                                    type="text"
                                    inputMode="numeric"
                                    className={`code-input ${digit ? 'filled' : ''}`}
                                    value={digit}
                                    onChange={(e) => handleChange(i, e.target.value)}
                                    onKeyDown={(e) => handleKeyDown(i, e)}
                                    maxLength={1}
                                    autoComplete="one-time-code"
                                />
                            ))}
                        </div>

                        <div className="auth-actions">
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? (
                                    <><span className="spinner" /> Verifying...</>
                                ) : (
                                    'Verify Email'
                                )}
                            </button>
                        </div>
                    </form>

                    <div className="auth-footer">
                        <p style={{ marginBottom: 8 }}>
                            Didn&apos;t receive the code?{' '}
                            <button
                                onClick={handleResend}
                                disabled={resending}
                                style={{
                                    background: 'none', border: 'none', color: 'var(--primary)',
                                    fontWeight: 600, cursor: 'pointer', fontFamily: 'var(--font-family)',
                                    fontSize: 'var(--font-size-sm)'
                                }}
                            >
                                {resending ? 'Sending...' : 'Resend Code'}
                            </button>
                        </p>
                        <Link href="/login">← Back to Login</Link>
                    </div>
                </div>
            </div>
        </div>
    );
}

export default function VerifyEmailPage() {
    return (
        <Suspense fallback={<div className="page-loader"><div className="page-loader-spinner" /></div>}>
            <VerifyEmailContent />
        </Suspense>
    );
}
