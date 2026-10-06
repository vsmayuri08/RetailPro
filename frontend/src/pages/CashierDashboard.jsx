import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getRecentSales, getCashierLeaderboard } from '../api/cashierApi';
import '../styles/dashboard.css';

const CashierDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [leaderboard, setLeaderboard] = useState(null);

  useEffect(() => {
    getRecentSales().then((sales) => {
      const today = new Date().toDateString();
      const todaysSales = sales.filter((s) => new Date(s.createdAt).toDateString() === today);
      setStats({
        todaysTransactions: todaysSales.length,
        todaysTotal: todaysSales.reduce((sum, s) => sum + Number(s.totalAmount), 0),
        itemsSold: todaysSales.reduce((sum, s) => sum + s.items.reduce((n, i) => n + i.quantity, 0), 0),
      });
    }).catch(() => {});
    getCashierLeaderboard().then(setLeaderboard).catch(() => {});
  }, []);

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="welcome-text">Welcome, {user?.fullName || 'Cashier'}</h1>
        <p className="dashboard-subtitle">Point of Sale — {user?.branchName || 'Not Assigned'}</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-card-hero">
          <div className="stat-info">
            <h3>Today's Total</h3>
            <p className="stat-value">{stats ? `₹${stats.todaysTotal.toFixed(2)}` : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Today's Transactions</h3>
            <p className="stat-value">{stats ? stats.todaysTransactions : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Items Sold</h3>
            <p className="stat-value">{stats ? stats.itemsSold : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Top this month</h3>
            <p className="stat-value">{leaderboard?.topPerformerName || '—'}</p>
          </div>
        </div>
        <div className="stat-card stat-card-actions">
          <h2>Quick Actions</h2>
          <div className="actions-grid">
            <button className="action-btn action-primary" onClick={() => navigate('/cashier/sale')}>New Sale</button>
            <button className="action-btn" onClick={() => navigate('/cashier/bills')}>View Bills</button>
            <button className="action-btn" onClick={() => navigate('/cashier/returns')}>Returns</button>
            <button className="action-btn" onClick={() => navigate('/cashier/leaderboard')}>Leaderboard</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CashierDashboard;
