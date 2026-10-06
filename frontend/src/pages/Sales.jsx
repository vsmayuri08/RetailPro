import React, { useEffect, useState } from 'react';
import { getManagerStats, getProductAlerts } from '../api/managerApi';
import { getRecentSales } from '../api/cashierApi';
import { useAuth } from '../context/AuthContext';
import '../styles/dashboard.css';
import '../styles/admin.css';

const Sales = () => {
  const { user } = useAuth();
  const [stats, setStats] = useState(null);
  const [alerts, setAlerts] = useState([]);
  const [sales, setSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([getManagerStats(), getProductAlerts(), getRecentSales()])
      .then(([statsData, alertData, salesData]) => {
        setStats(statsData);
        setAlerts(alertData);
        setSales(salesData);
      })
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load branch reports'))
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <div className="dashboard-container"><div className="empty-state">Loading reports…</div></div>;

  const today = new Date().toDateString();
  const todaysSales = sales.filter((s) => new Date(s.createdAt).toDateString() === today);
  const todaysRevenue = todaysSales.reduce((sum, s) => sum + Number(s.totalAmount), 0);
  const monthRevenue = sales
    .filter((s) => new Date(s.createdAt).getMonth() === new Date().getMonth() && new Date(s.createdAt).getFullYear() === new Date().getFullYear())
    .reduce((sum, s) => sum + Number(s.totalAmount), 0);

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="welcome-text">Branch Reports</h1>
        <p className="dashboard-subtitle">{user?.branchName || 'Your branch'} — sales and inventory snapshot</p>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      {stats && (
        <>
          <div className="stats-grid">
            <div className="stat-card stat-card-hero">
              <div className="stat-info">
                <h3>Today's Sales</h3>
                <p className="stat-value">₹{todaysRevenue.toFixed(2)}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>This Month</h3>
                <p className="stat-value">₹{monthRevenue.toFixed(2)}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Active Products</h3>
                <p className="stat-value">{stats.totalProducts}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Inventory Value</h3>
                <p className="stat-value">₹{Number(stats.stockValue).toFixed(2)}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Low Stock Items</h3>
                <p className="stat-value">{stats.lowStockCount}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Near-Expiry Items</h3>
                <p className="stat-value">{stats.nearExpiryCount}</p>
              </div>
            </div>
          </div>

          <div className="quick-actions-section">
            <h2>Inventory Alerts</h2>
            <div className="data-card" style={{ marginTop: '1.25rem' }}>
              {alerts.length === 0 ? (
                <div className="empty-state">Nothing needs attention right now.</div>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Product</th>
                      <th>SKU</th>
                      <th>Stock</th>
                      <th>Expiry</th>
                    </tr>
                  </thead>
                  <tbody>
                    {alerts.map((p) => (
                      <tr key={p.id}>
                        <td>{p.name}</td>
                        <td>{p.sku}</td>
                        <td>{p.quantity} <span style={{ color: 'var(--text-muted)' }}>/ reorder {p.reorderLevel}</span></td>
                        <td>{p.expiryDate || '—'}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>

          <div className="quick-actions-section" style={{ marginTop: '2.5rem' }}>
            <h2>Recent Sales</h2>
            <div className="data-card" style={{ marginTop: '1.25rem' }}>
              {sales.length === 0 ? (
                <div className="empty-state">No sales recorded yet for this branch.</div>
              ) : (
                <table className="data-table">
                  <thead>
                    <tr>
                      <th>Invoice</th>
                      <th>Date</th>
                      <th>Customer</th>
                      <th>Total</th>
                      <th>Refunded</th>
                      <th>Payment</th>
                    </tr>
                  </thead>
                  <tbody>
                    {sales.slice(0, 15).map((s) => (
                      <tr key={s.id}>
                        <td>{s.invoiceNumber}</td>
                        <td>{new Date(s.createdAt).toLocaleString()}</td>
                        <td>{s.customerName || 'Walk-in'}</td>
                        <td>₹{Number(s.totalAmount).toFixed(2)}</td>
                        <td>{Number(s.refundedAmount) > 0 ? `₹${Number(s.refundedAmount).toFixed(2)}` : '—'}</td>
                        <td>{s.payment?.method}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              )}
            </div>
          </div>
        </>
      )}
    </div>
  );
};

export default Sales;
