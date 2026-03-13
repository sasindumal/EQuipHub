'use client';

import { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import Image from 'next/image';
import { HiOutlineMail, HiOutlineLockClosed, HiOutlineEye, HiOutlineEyeOff, HiOutlineUser, HiOutlineAcademicCap, HiOutlineIdentification } from 'react-icons/hi';
import { authAPI } from '@/lib/api';
import '../auth.css';

export default function RegisterPage() {
    const router = useRouter();
    const [form, setForm] = useState({
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        semesterYear: '',
        indexNumber: '',
    });
    const [showPassword, setShowPassword] = useState(false);
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    const handleChange = (e) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const validate = () => {
        if (!form.email || !form.password || !form.firstName || !form.lastName || !form.semesterYear || !form.indexNumber) {
            return 'All fields are required';
        }
        const emailRegex = /^20\d{2}[A-Za-z]\d{3}@eng\.jfn\.ac\.lk$/;
        if (!emailRegex.test(form.email)) {
            return 'Email must be in format: 20xxExxx@eng.jfn.ac.lk';
        }
        if (form.password.length < 8) {
            return 'Password must be at least 8 characters';
        }
        if (form.password !== form.confirmPassword) {
            return 'Passwords do not match';
        }
        const indexRegex = /^\d{2}[A-Za-z]\d{3}$/;
        if (!indexRegex.test(form.indexNumber)) {
            return 'Index number must be in format: 21E001';
        }
        const sem = parseInt(form.semesterYear);
        if (isNaN(sem) || sem < 1 || sem > 8) {
            return 'Semester year must be between 1 and 8';
        }
        return null;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');

        const validationError = validate();
        if (validationError) {
            setError(validationError);
            return;
        }

        setLoading(true);
        try {
            await authAPI.register({
                email: form.email,
                password: form.password,
                firstName: form.firstName,
                lastName: form.lastName,
                role: 'STUDENT',
                semesterYear: parseInt(form.semesterYear),
                indexNumber: form.indexNumber.toUpperCase(),
            });
            router.push(`/verify-email?email=${encodeURIComponent(form.email)}`);
        } catch (err) {
            const msg = err.response?.data?.message || 'Registration failed. Please try again.';
            setError(msg);
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <div className="auth-container" style={{ maxWidth: 500 }}>
                <div className="auth-card">
                    <div className="auth-logo">
                        <Image src="/logo.png" alt="EQuipHub" width={56} height={56} priority />
                    </div>
                    <h1 className="auth-title">Create Account</h1>
                    <p className="auth-subtitle">Register as a student to get started</p>

                    {error && <div className="auth-alert error">{error}</div>}

                    <form className="auth-form" onSubmit={handleSubmit}>
                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">First Name</label>
                                <div className="auth-input-group">
                                    <HiOutlineUser className="auth-input-icon" />
                                    <input
                                        type="text"
                                        name="firstName"
                                        className="form-input"
                                        placeholder="John"
                                        value={form.firstName}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Last Name</label>
                                <div className="auth-input-group">
                                    <HiOutlineUser className="auth-input-icon" />
                                    <input
                                        type="text"
                                        name="lastName"
                                        className="form-input"
                                        placeholder="Doe"
                                        value={form.lastName}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">University Email</label>
                            <div className="auth-input-group">
                                <HiOutlineMail className="auth-input-icon" />
                                <input
                                    type="email"
                                    name="email"
                                    className="form-input"
                                    placeholder="2021E001@eng.jfn.ac.lk"
                                    value={form.email}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="form-row">
                            <div className="form-group">
                                <label className="form-label">Index Number</label>
                                <div className="auth-input-group">
                                    <HiOutlineIdentification className="auth-input-icon" />
                                    <input
                                        type="text"
                                        name="indexNumber"
                                        className="form-input"
                                        placeholder="21E001"
                                        value={form.indexNumber}
                                        onChange={handleChange}
                                        required
                                    />
                                </div>
                            </div>
                            <div className="form-group">
                                <label className="form-label">Semester Year</label>
                                <div className="auth-input-group">
                                    <HiOutlineAcademicCap className="auth-input-icon" />
                                    <select
                                        name="semesterYear"
                                        className="form-input"
                                        value={form.semesterYear}
                                        onChange={handleChange}
                                        required
                                        style={{ paddingLeft: 42 }}
                                    >
                                        <option value="">Select</option>
                                        {[1, 2, 3, 4, 5, 6, 7, 8].map((y) => (
                                            <option key={y} value={y}>Semester {y}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Password</label>
                            <div className="auth-input-group">
                                <HiOutlineLockClosed className="auth-input-icon" />
                                <input
                                    type={showPassword ? 'text' : 'password'}
                                    name="password"
                                    className="form-input"
                                    placeholder="Min 8 characters"
                                    value={form.password}
                                    onChange={handleChange}
                                    required
                                />
                                <button
                                    type="button"
                                    className="password-toggle"
                                    onClick={() => setShowPassword(!showPassword)}
                                >
                                    {showPassword ? <HiOutlineEyeOff /> : <HiOutlineEye />}
                                </button>
                            </div>
                        </div>

                        <div className="form-group">
                            <label className="form-label">Confirm Password</label>
                            <div className="auth-input-group">
                                <HiOutlineLockClosed className="auth-input-icon" />
                                <input
                                    type="password"
                                    name="confirmPassword"
                                    className="form-input"
                                    placeholder="Re-enter your password"
                                    value={form.confirmPassword}
                                    onChange={handleChange}
                                    required
                                />
                            </div>
                        </div>

                        <div className="auth-actions" style={{ marginTop: 16 }}>
                            <button type="submit" className="btn btn-primary" disabled={loading}>
                                {loading ? (
                                    <>
                                        <span className="spinner" />
                                        Creating Account...
                                    </>
                                ) : (
                                    'Create Account'
                                )}
                            </button>
                        </div>
                    </form>

                    <div className="auth-footer">
                        Already have an account?{' '}
                        <Link href="/login">Sign in</Link>
                    </div>
                </div>
            </div>
        </div>
    );
}
