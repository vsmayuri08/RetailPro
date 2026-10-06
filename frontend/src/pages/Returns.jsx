import React, { useEffect, useState } from 'react';
import { getRecentReturns } from '../api/cashierApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const Returns = () => {
  const [returns, setReturns] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    getRecentReturns()
      .then(setReturns)
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load returns'))
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Returns</h1>
          <p className="page-subtitle">Processed returns for this branch — refund, restock, and loyalty reversal in each record</p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading returns…</div>
        ) : returns.length === 0 ? (
          <div className="empty-state">No returns yet. Open a bill and choose Return to process one.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Invoice</th>
                <th>Items</th>
                <th>Refund</th>
                <th>Loyalty reversed</th>
                <th>Processed by</th>
                <th>Reason</th>
              </tr>
            </thead>
            <tbody>
              {returns.map((row) => (
                <tr key={row.id}>
                  <td>{new Date(row.createdAt).toLocaleString()}</td>
                  <td>{row.invoiceNumber}</td>
                  <td>
                    {row.items.map((item) => `${item.productName} × ${item.quantity}`).join(', ')}
                  </td>
                  <td>₹{Number(row.refundAmount).toFixed(2)}</td>
                  <td>{row.loyaltyPointsReversed}</td>
                  <td>{row.processedByName}</td>
                  <td>{row.reason}</td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default Returns;
