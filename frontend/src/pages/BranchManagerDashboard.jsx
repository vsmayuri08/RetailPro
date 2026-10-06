import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { getManagerStats, getSalesTrend } from '../api/managerApi';
import { getInventoryInsights } from '../api/aiApi';
import SalesTrendChart from '../components/SalesTrendChart';
import '../styles/dashboard.css';
import '../styles/manager.css';
import '../styles/ai-assistant.css';

const riskBadge = (level) => {
  if (level === 'HIGH') return 'badge-danger';
  if (level === 'MEDIUM') return 'badge-pending';
  return 'badge-approved';
};

const BranchManagerDashboard = () => {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [stats, setStats] = useState(null);
  const [insights, setInsights] = useState(null);
  const [insightsError, setInsightsError] = useState('');
  const [salesTrend, setSalesTrend] = useState(null);

  useEffect(() => {
    getManagerStats().then(setStats).catch(() => {});
    getSalesTrend().then(setSalesTrend).catch(() => {});
    getInventoryInsights()
      .then(setInsights)
      .catch((err) => {
        setInsightsError(err?.response?.data?.message || 'Could not load inventory insights.');
      });
  }, []);

  return (
    <div className="dashboard-container">
      <div className="dashboard-header">
        <h1 className="welcome-text">Welcome, {user?.fullName || 'Branch Manager'}</h1>
        <p className="dashboard-subtitle">Branch: {user?.branchName || 'Not Assigned'}</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card stat-card-hero">
          <div className="stat-info">
            <h3>Total Products</h3>
            <p className="stat-value">{stats ? stats.totalProducts : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Low Stock Items</h3>
            <p className="stat-value">{stats ? stats.lowStockCount : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Employees</h3>
            <p className="stat-value">{stats ? stats.totalEmployees : '—'}</p>
          </div>
        </div>
        <div className="stat-card">
          <div className="stat-info">
            <h3>Transfers Awaiting You</h3>
            <p className="stat-value">{stats ? stats.pendingIncomingTransfers : '—'}</p>
          </div>
        </div>
        <div className="stat-card stat-card-actions">
          <h2>Quick Actions</h2>
          <div className="actions-grid">
            <button className="action-btn" onClick={() => navigate('/manager/products')}>Add Product</button>
            <button className="action-btn" onClick={() => navigate('/manager/inventory')}>View Inventory</button>
            <button className="action-btn" onClick={() => navigate('/manager/employees')}>Add Employee</button>
            <button className="action-btn" onClick={() => navigate('/manager/sales')}>View Reports</button>
            <button className="action-btn" onClick={() => navigate('/manager/reorder-suggestions')}>Reorder Suggestions</button>
            <button className="action-btn" onClick={() => navigate('/manager/purchase-orders')}>Purchase Orders</button>
          </div>
        </div>
      </div>

      <SalesTrendChart trend={salesTrend} />

      <section className="insights-section">
        <h2>AI Inventory Insights</h2>
        <p className="dashboard-subtitle">
          Stock, daily sales, predicted demand, and days remaining are calculated in RetailPro. Gemini only explains those figures.
        </p>
        {insightsError && <p className="insights-error">{insightsError}</p>}
        {insights && (
          <>
            <p className="insights-narrative">{insights.narrative}</p>
            <div className="insights-counts">
              <span>Stockout risk: {insights.stockoutCount}</span>
              <span>Near expiry: {insights.nearExpiryCount}</span>
              <span>Dead stock: {insights.deadStockCount}</span>
              <span>Overstock: {insights.overstockCount}</span>
            </div>
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Insight</th>
                    <th>Product</th>
                    <th>Current Stock</th>
                    <th>Avg Daily Sales</th>
                    <th>Predicted Demand (7d)</th>
                    <th>Days of Stock Remaining</th>
                    <th>Risk</th>
                  </tr>
                </thead>
                <tbody>
                  {insights.items?.length ? insights.items.map((row) => (
                    <tr key={`${row.productId}-${row.insightType}`}>
                      <td>
                        <div className="insight-headline">{row.headline}</div>
                        <div className="insight-type">{row.insightType}</div>
                      </td>
                      <td>{row.productName}<br /><small>{row.sku}</small></td>
                      <td>{row.currentStock}</td>
                      <td>{row.averageDailySales ?? '—'}</td>
                      <td>{row.predictedDemand ?? '—'}</td>
                      <td>{row.daysOfStockRemaining ?? '—'}</td>
                      <td><span className={riskBadge(row.riskLevel)}>{row.riskLevel}</span></td>
                    </tr>
                  )) : (
                    <tr>
                      <td colSpan={7}>I don't have enough data to answer that.</td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>
          </>
        )}
        {!insights && !insightsError && <p className="dashboard-subtitle">Loading inventory insights…</p>}
      </section>
    </div>
  );
};

export default BranchManagerDashboard;
