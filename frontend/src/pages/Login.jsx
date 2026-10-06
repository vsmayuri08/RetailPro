import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import logo from '../assets/img/logo.svg';
import signInImg from '../assets/img/signin.svg';
import '../styles/login.css';

const IconEye = ({ off }) => (
  <svg viewBox="0 0 24 24" className="field-svg" aria-hidden="true">
    {off ? (
      <>
        <path d="M3 3l18 18" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
        <path d="M10.5 6.2A11 11 0 0 1 12 6c5.5 0 9.5 4.5 10.5 6-.4.6-1.1 1.5-2.1 2.5M6.2 6.2C4.4 7.5 3.2 9.2 2.5 12c1 1.5 5 6 9.5 6 1.2 0 2.3-.2 3.4-.6" fill="none" stroke="currentColor" strokeWidth="1.7" strokeLinecap="round" />
      </>
    ) : (
      <>
        <path d="M2.5 12C3.5 9.5 7.5 6 12 6s8.5 3.5 9.5 6c-1 2.5-5 6-9.5 6S3.5 14.5 2.5 12z" fill="none" stroke="currentColor" strokeWidth="1.7" />
        <circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" strokeWidth="1.7" />
      </>
    )}
  </svg>
);

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [hint, setHint] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setHint('');
    setIsLoading(true);

    try {
      const loggedInUser = await login(email, password);

      if (loggedInUser.role === 'SUPER_ADMIN') navigate('/admin/dashboard');
      else if (loggedInUser.role === 'BRANCH_MANAGER') navigate('/manager/dashboard');
      else if (loggedInUser.role === 'CASHIER') navigate('/cashier/dashboard');
      else navigate('/');
    } catch (err) {
      const message = err?.response?.data?.message || 'Invalid email or password';
      setError(message);
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-page">
      <div className="widget-auth">
        <div className="auth-brand">
          <img src={logo} alt="" className="auth-brand-logo" />
          <span className="auth-brand-text"><strong>Retail</strong>Pro</span>
        </div>

        <h3 className="auth-title">Welcome back</h3>
        <p className="widget-auth-info">Sign in to your RetailPro account to manage sales, stock and customers.</p>

        {error && <div className="error-message">{error}</div>}
        {hint && !error && <div className="hint-message">{hint}</div>}

        <form onSubmit={handleSubmit} className="login-form">
          <div className="auth-group">
            <label htmlFor="login-email">Email address</label>
            <input
              id="login-email"
              type="email"
              placeholder="you@company.com"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              autoComplete="username"
            />
          </div>

          <div className="auth-group">
            <label htmlFor="login-password">Password</label>
            <div className="password-wrap">
              <input
                id="login-password"
                type={showPassword ? 'text' : 'password'}
                placeholder="Enter your password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                autoComplete="current-password"
              />
              <button
                type="button"
                className="toggle-password"
                onClick={() => setShowPassword(!showPassword)}
                aria-label={showPassword ? 'Hide password' : 'Show password'}
              >
                <IconEye off={showPassword} />
              </button>
            </div>
          </div>

          <button
            type="button"
            className="forgot-link"
            onClick={() => setHint('Ask a Super Admin or Branch Manager to reset your password.')}
          >
            Forgot password?
          </button>

          <button type="submit" className="login-submit auth-btn" disabled={isLoading}>
            {isLoading ? <span className="spinner" /> : 'Login'}
          </button>
        </form>

        <p className="widget-auth-info login-or-text">Or</p>
        <p className="login-signup">
          New to RetailPro?{' '}
          <button
            type="button"
            className="signup-link"
            onClick={() => setHint('Accounts are created by a Super Admin or Branch Manager.')}
          >
            Create an account
          </button>
        </p>

        <footer className="auth-footer-note">{new Date().getFullYear()} © RetailPro — Retail Management System</footer>
      </div>

      <img src={signInImg} alt="" className="backImg" aria-hidden="true" />
    </div>
  );
};

export default Login;
