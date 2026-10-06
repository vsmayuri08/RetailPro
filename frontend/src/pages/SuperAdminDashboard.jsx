import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getOrgStats } from '../api/adminApi';
import { getSalesTrend } from '../api/managerApi';
import SalesTrendChart from '../components/SalesTrendChart';
import '../styles/dashboard.css';

const SuperAdminDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [salesTrend, setSalesTrend] = useState(null);

  useEffect(() => {
    getOrgStats().then(setStats).catch(() => {});
    getSalesTrend().then(setSalesTrend).catch(() => {});
  }, []);

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="welcome-text">Welcome, {user?.fullName || 'Super Admin'}</h1>
        <p className="dashboard-subtitle">Organization Overview</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-card-hero">
          <div className="stat-info">
            <h3>Revenue This Month</h3>
            <p className="stat-value">{stats ? `₹${Number(stats.totalRevenueThisMonth).toFixed(2)}` : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Total Branches</h3>
            <p className="stat-value">{stats ? stats.totalBranches : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Total Employees</h3>
            <p className="stat-value">{stats ? stats.totalEmployees : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Sales Today</h3>
            <p className="stat-value">{stats ? stats.totalSalesToday : '—'}</p>
          </div>
        </div>
        <div className="stat-card stat-card-actions">
          <h2>Quick Actions</h2>
          <div className="actions-grid">
            <button className="action-btn" onClick={() => navigate('/admin/branches')}>Manage Branches</button>
            <button className="action-btn" onClick={() => navigate('/admin/users')}>Manage Users</button>
            <button className="action-btn" onClick={() => navigate('/admin/reports')}>View Reports</button>
          </div>
        </div>
      </div>

      <SalesTrendChart trend={salesTrend} />
    </div>
  );
};

export default SuperAdminDashboard;
