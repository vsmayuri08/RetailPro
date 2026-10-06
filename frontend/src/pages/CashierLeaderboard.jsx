import React, { useEffect, useState } from 'react';
import { getCashierLeaderboard as getManagerLeaderboard } from '../api/managerApi';
import { getOrgCashierLeaderboard } from '../api/adminApi';
import { getCashierLeaderboard } from '../api/cashierApi';
import { useAuth } from '../context/AuthContext';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const loadForRole = (role) => {
  if (role === 'SUPER_ADMIN') return getOrgCashierLeaderboard();
  if (role === 'CASHIER') return getCashierLeaderboard();
  return getManagerLeaderboard();
};

const CashierLeaderboard = () => {
  const { user } = useAuth();
  const [board, setBoard] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    loadForRole(user?.role)
      .then(setBoard)
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load leaderboard'));
  }, [user?.role]);

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Cashier Leaderboard</h1>
          <p className="page-subtitle">Top performer this month, ranked by revenue from real sales</p>
        </div>
      </div>
      {error && <div className="alert-banner alert-error">{error}</div>}
      {board && (
        <>
          <div className="stats-grid">
            <div className="stat-card stat-card-hero">
              <div className="stat-info">
                <h3>Top performer</h3>
                <p className="stat-value">{board.topPerformerName || '—'}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  {board.periodStart} to {board.periodEnd}
                </p>
              </div>
            </div>
          </div>
          <div className="data-card">
            {board.entries.length === 0 ? (
              <div className="empty-state">No sales this month yet.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Cashier</th>
                    <th>Sales</th>
                    <th>Items</th>
                    <th>Revenue</th>
                  </tr>
                </thead>
                <tbody>
                  {board.entries.map((row) => (
                    <tr key={row.cashierId}>
                      <td>{row.rank}</td>
                      <td>{row.cashierName}</td>
                      <td>{row.salesCount}</td>
                      <td>{row.itemsSold}</td>
                      <td>₹{Number(row.revenue).toFixed(2)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default CashierLeaderboard;
