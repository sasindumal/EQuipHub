import React, { useState } from 'react';
import './LoginForm.css';
import logo from '../assets/iconlogo.png';

interface LoginFormState {
  email: string;
  password: string;
  rememberMe: boolean;
  isLoading: boolean;
}

export const LoginForm: React.FC = () => {
  const [formData, setFormData] = useState<LoginFormState>({
    email: '',
    password: '',
    rememberMe: false,
    isLoading: false,
  });

  const [errors, setErrors] = useState<{ email?: string; password?: string }>({});
  const [showPassword, setShowPassword] = useState(false);
  const [loginSuccess, setLoginSuccess] = useState(false);

  const validateEmail = (email: string): boolean => {
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    return emailRegex.test(email);
  };

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value,
    }));
    // Clear error when user starts typing
    if (errors[name as keyof typeof errors]) {
      setErrors((prev) => ({ ...prev, [name]: undefined }));
    }
  };

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    const newErrors: { email?: string; password?: string } = {};

    if (!formData.email) {
      newErrors.email = 'Email is required';
    } else if (!validateEmail(formData.email)) {
      newErrors.email = 'Please enter a valid email';
    }

    if (!formData.password) {
      newErrors.password = 'Password is required';
    } else if (formData.password.length < 6) {
      newErrors.password = 'Password must be at least 6 characters';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    // Simulate login API call
    setFormData((prev) => ({ ...prev, isLoading: true }));
    
    setTimeout(() => {
      setFormData((prev) => ({ ...prev, isLoading: false }));
      setLoginSuccess(true);
      
      // Reset form after success
      setTimeout(() => {
        setFormData({
          email: '',
          password: '',
          rememberMe: false,
          isLoading: false,
        });
        setLoginSuccess(false);
      }, 2000);
    }, 1500);
  };

  if (loginSuccess) {
    return (
      <div className="login-container">
        <div className="success-message">
          <div className="success-icon">✓</div>
          <h2>Welcome Back!</h2>
          <p>You have successfully logged in</p>
        </div>
      </div>
    );
  }

  return (
    <div className="login-container">
      <div className="login-wrapper">
        {/* Left Side - Branding */}
        <div className="login-branding">
          <div className="branding-content">
            <img src={logo} alt="EQuipHub" className="logo" />
            <h1>EQuipHub</h1>
            <p>Professional Equipment Management System</p>
            <div className="floating-shapes">
              <div className="shape shape-1"></div>
              <div className="shape shape-2"></div>
              <div className="shape shape-3"></div>
            </div>
          </div>
        </div>

        {/* Right Side - Form */}
        <div className="login-form-section">
          <div className="form-header">
            <h2>Welcome Back</h2>
            <p>Sign in to your EQuipHub account</p>
          </div>

          <form onSubmit={handleSubmit} className="form">
            {/* Email Field */}
            <div className="form-group">
              <label htmlFor="email" className="form-label">
                Email Address
              </label>
              <input
                type="email"
                id="email"
                name="email"
                value={formData.email}
                onChange={handleInputChange}
                placeholder="your.email@example.com"
                className={`form-input ${errors.email ? 'error' : ''}`}
                disabled={formData.isLoading}
              />
              {errors.email && <span className="error-message">{errors.email}</span>}
            </div>

            {/* Password Field */}
            <div className="form-group">
              <label htmlFor="password" className="form-label">
                Password
              </label>
              <div className="password-wrapper">
                <input
                  type={showPassword ? 'text' : 'password'}
                  id="password"
                  name="password"
                  value={formData.password}
                  onChange={handleInputChange}
                  placeholder="••••••••"
                  className={`form-input ${errors.password ? 'error' : ''}`}
                  disabled={formData.isLoading}
                />
                <button
                  type="button"
                  className="toggle-password"
                  onClick={() => setShowPassword(!showPassword)}
                  disabled={formData.isLoading}
                >
                  {showPassword ? '👁' : '👁‍🗨'}
                </button>
              </div>
              {errors.password && <span className="error-message">{errors.password}</span>}
            </div>

            {/* Remember Me & Forgot Password */}
            <div className="form-options">
              <label className="checkbox-label">
                <input
                  type="checkbox"
                  name="rememberMe"
                  checked={formData.rememberMe}
                  onChange={handleInputChange}
                  disabled={formData.isLoading}
                />
                <span>Remember me</span>
              </label>
              <a href="#forgot" className="forgot-link">
                Forgot Password?
              </a>
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              className={`submit-btn ${formData.isLoading ? 'loading' : ''}`}
              disabled={formData.isLoading}
            >
              {formData.isLoading ? (
                <>
                  <span className="spinner"></span>
                  Signing in...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Sign Up Link */}
          <div className="signup-prompt">
            <p>
              Don't have an account?{' '}
              <a href="#signup" className="signup-link">
                Create one now
              </a>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
