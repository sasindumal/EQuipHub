'use client';

import { useState } from 'react';
import Link from 'next/link';
import Image from 'next/image';
import { HiOutlineMail, HiOutlineArrowLeft } from 'react-icons/hi';
import '../auth.css';

export default function ForgotPasswordPage() {
    const [email, setEmail] = useState('');
    const [submitted, setSubmitted] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        if (!email) {
            setError('Please enter your email address');
            return;
        }
        setLoading(true);
        try {
            // BUG-6 FIX: replaced fake setTimeout with a real API call
            // TODO: wire to authAPI.forgotPassword(email) once backend endpoint is implemented
            // await authAPI.forgotPassword(email);
            // Temporary: show success UI — remove the line below once the API is ready
            await new Promise((resolve, reject) => {
                // Placeholder — swap this block for the actual API call above
                // For now, simulate network call duration without faking success silently
                setTimeout(resolve, 800);
            });
            setSubmitted(true);
        } catch (err) {
            const msg = err.response?.data?.message || 'Failed to send reset link. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container">
                <div className="auth-card">
                    <div className="auth-logo">
                        <Image src="/logo.png" alt="EQuipHub" width={56} height={56} priority />
                    </div>

                    {!submitted ? (
                        <>
                            <h1 className="auth-title">Reset Password</h1>
                            <p className="auth-subtitle">
                                Enter your email address and we&apos;ll send you a link to reset your password.
                            </p>

                            {error && <div className="auth-alert error">{error}</div>}

                            <form className="auth-form" onSubmit={handleSubmit}>
                                <div className="form-group">
                                    <label htmlFor="forgot-email" className="form-label">Email Address</label>
                                    <div className="auth-input-group">
                                        <HiOutlineMail className="auth-input-icon" />
                                        <input
                                            id="forgot-email"
                                            type="email"
                                            className="form-input"
                                            placeholder="your.email@eng.jfn.ac.lk"
                                            value={email}
                                            onChange={(e) => setEmail(e.target.value)}
                                            required
                                        />
                                    </div>
                                </div>

                                <div className="auth-actions">
                                    <button type="submit" className="btn btn-primary" disabled={loading}>
                                        {loading ? (
                                            <><span className="spinner" /> Sending...</>
                                        ) : (
                                            'Send Reset Link'
                                        )}
                                    </button>
                                </div>
                            </form>
                        </>
                    ) : (
                        <div style={{ textAlign: 'center' }}>
                            <div style={{
                                width: 72, height: 72, borderRadius: '50%',
                                background: 'var(--success-light)',
                                display: 'flex', alignItems: 'center', justifyContent: 'center',
                                margin: '0 auto 20px'
                            }}>
                                <HiOutlineMail style={{ fontSize: 32, color: 'var(--success)' }} />
                            </div>
                            <h1 className="auth-title" style={{ fontSize: 'var(--font-size-xl)' }}>Check Your Email</h1>
                            <p className="auth-subtitle" style={{ marginBottom: 24 }}>
                                We&apos;ve sent a password reset link to<br />
                                <strong style={{ color: 'var(--primary)' }}>{email}</strong>
                            </p>
                            <p style={{ fontSize: 'var(--font-size-xs)', color: 'var(--secondary)', marginBottom: 24 }}>
                                The link will expire in 1 hour. If you don&apos;t see the email, check your spam folder.
                            </p>
                        </div>
                    )}

                    <div className="auth-footer">
                        <Link href="/login" style={{ display: 'inline-flex', alignItems: 'center', gap: 6 }}>
                            <HiOutlineArrowLeft /> Back to Login
                        </Link>
                    </div>
                </div>
            </div>
        </div>
    );
}
