import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

const ProtectedRoute = ({ roles, children }) => {
  const { user, isAuthenticated, loading } = useAuth();

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  if (roles && user && !roles.includes(user.role)) {
    // Redirect based on actual role
    if (user.role === 'SUPER_ADMIN') return <Navigate to="/admin/dashboard" replace />;
    if (user.role === 'BRANCH_MANAGER') return <Navigate to="/manager/dashboard" replace />;
    if (user.role === 'CASHIER') return <Navigate to="/cashier/dashboard" replace />;
    return <Navigate to="/login" replace />;
  }

  return children;
};

export default ProtectedRoute;
