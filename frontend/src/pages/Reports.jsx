import React, { useEffect, useState } from 'react';
import { getOrgStats } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const Reports = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getOrgStats()
      .then(setStats)
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load reports'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="dashboard-container"><div className="empty-state">Loading reports…</div></div>;

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="welcome-text">Organization Reports</h1>
        <p className="dashboard-subtitle">Branch performance comparison and organization-wide summary</p>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      {stats && (
        <>
          <div className="stats-grid">
            <div className="stat-card stat-card-hero">
              <div className="stat-info">
                <h3>Revenue This Month</h3>
                <p className="stat-value">₹{Number(stats.totalRevenueThisMonth).toFixed(2)}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Total Branches</h3>
                <p className="stat-value">{stats.totalBranches}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Active Branches</h3>
                <p className="stat-value">{stats.activeBranches}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Branch Managers</h3>
                <p className="stat-value">{stats.totalManagers}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Cashiers</h3>
                <p className="stat-value">{stats.totalCashiers}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Revenue Today</h3>
                <p className="stat-value">₹{Number(stats.totalRevenueToday).toFixed(2)}</p>
              </div>
            </div>
          </div>

          <div className="quick-actions-section">
            <h2>Branch Comparison</h2>
            <div className="data-card" style={{ marginTop: '1.25rem' }}>
              {stats.branches.length === 0 ? (
                <div className="empty-state">No branches to compare yet.</div>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Branch</th>
                      <th>City</th>
                      <th>Managers</th>
                      <th>Cashiers</th>
                      <th>Total Staff</th>
                      <th>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {stats.branches.map((b) => (
                      <tr key={b.id}>
                        <td>{b.branchName} <span style={{ color: 'var(--text-muted)' }}>({b.branchCode})</span></td>
                        <td>{b.city || '—'}</td>
                        <td>{b.managerCount}</td>
                        <td>{b.cashierCount}</td>
                        <td>{b.employeeCount}</td>
                        <td>
                          <span className={`badge ${b.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}`}>
                            {b.status}
                          </span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="quick-actions-section" style={{ marginTop: '2.5rem' }}>
            <h2>Branch Sales Comparison</h2>
            <div className="data-card" style={{ marginTop: '1.25rem' }}>
              {!stats.branchSales || stats.branchSales.length === 0 ? (
                <div className="empty-state">No branches to compare yet.</div>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Branch</th>
                      <th>Sales Today</th>
                      <th>Revenue Today</th>
                      <th>Sales This Month</th>
                      <th>Revenue This Month</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[...stats.branchSales]
                      .sort((a, b) => Number(b.revenueThisMonth) - Number(a.revenueThisMonth))
                      .map((b) => (
                        <tr key={b.branchId}>
                          <td>{b.branchName}</td>
                          <td>{b.salesCountToday}</td>
                          <td>₹{Number(b.revenueToday).toFixed(2)}</td>
                          <td>{b.salesCountThisMonth}</td>
                          <td>₹{Number(b.revenueThisMonth).toFixed(2)}</td>
                        </tr>
                      ))}
                  </tbody>
                </table>
              )}
            </div>
            <p className="form-hint" style={{ marginTop: '0.75rem' }}>
              Ranked by this month's revenue — the top row is currently the best-performing branch.
            </p>
          </div>
        </>
      )}
    </div>
  );
};

export default Reports;
