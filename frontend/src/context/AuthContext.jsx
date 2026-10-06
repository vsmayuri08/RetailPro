import React, { createContext, useContext, useState, useEffect } from 'react';
import axios from '../api/axios';

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const storedToken = localStorage.getItem('retail_token');
    const storedUser = localStorage.getItem('retail_user');

    if (storedToken && storedUser) {
      setToken(storedToken);
      setUser(JSON.parse(storedUser));
    }
    setLoading(false);
  }, []);

  const login = async (email, password) => {
    const response = await axios.post('/auth/login', { email, password });
    const data = response.data.data; // LoginResponse: token, id, fullName, email, role, branchId, branchName

    const loggedInUser = {
      id: data.id,
      fullName: data.fullName,
      email: data.email,
      role: data.role,
      branchId: data.branchId,
      branchName: data.branchName,
    };

    setToken(data.token);
    setUser(loggedInUser);
    localStorage.setItem('retail_token', data.token);
    localStorage.setItem('retail_user', JSON.stringify(loggedInUser));
    return loggedInUser;
  };

  const logout = () => {
    setToken(null);
    setUser(null);
    localStorage.removeItem('retail_token');
    localStorage.removeItem('retail_user');
    window.location.href = '/login';
  };

  const getCurrentUser = async () => {
    const response = await axios.get('/auth/me');
    return response.data;
  };

  const isAuthenticated = !!token;

  return (
    <AuthContext.Provider value={{ user, token, loading, login, logout, getCurrentUser, isAuthenticated }}>
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => useContext(AuthContext);
