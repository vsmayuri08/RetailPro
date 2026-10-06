import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { getReorderSuggestions, createPurchaseOrdersFromReorder } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const urgencyBadgeClass = (urgency) => {
  switch (urgency) {
    case 'CRITICAL': return 'badge-danger';
    case 'REORDER': return 'badge-pending';
    default: return 'badge-approved';
  }
};

const ReorderSuggestions = () => {
  const navigate = useNavigate();
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [creating, setCreating] = useState(false);
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [filter, setFilter] = useState('ALL');

  const loadSuggestions = async (start, end) => {
    setLoading(true);
    try {
      const data = await getReorderSuggestions(start || undefined, end || undefined);
      setAnalysis(data);
      setStartDate(data.periodStart);
      setEndDate(data.periodEnd);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load reorder suggestions');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadSuggestions();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleCreatePo = async () => {
    setCreating(true);
    setError('');
    setSuccess('');
    try {
      const result = await createPurchaseOrdersFromReorder();
      const count = result.data?.orders?.length || 0;
      setSuccess(result.message || `Created ${count} draft purchase order(s). They were not sent to suppliers.`);
      if (count > 0) {
        navigate('/manager/purchase-orders');
      }
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not create purchase orders');
    } finally {
      setCreating(false);
    }
  };

  const handleRun = (e) => {
    e.preventDefault();
    loadSuggestions(startDate, endDate);
  };

  const visibleItems = analysis
    ? analysis.items.filter((item) => {
      if (filter === 'NEEDS_REORDER') return item.suggestedQty > 0;
      if (filter === 'CRITICAL') return item.urgency === 'CRITICAL' && item.suggestedQty > 0;
      if (filter === 'NO_SUPPLIER') return item.suggestedQty > 0 && !item.suggestedSupplierId;
      return true;
    })
    : [];

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Reorder Suggestions</h1>
          <p className="page-subtitle">
            How much to order and from which supplier — based on sales velocity and purchase-batch history, not the catalog reorder level
          </p>
        </div>
        <button
          type="button"
          className="btn-primary"
          disabled={creating || loading}
          onClick={handleCreatePo}
        >
          {creating ? 'Creating…' : 'Create Purchase Order'}
        </button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {success && <div className="alert-banner alert-success">{success}</div>}

      <form className="form-row" onSubmit={handleRun} style={{ alignItems: 'flex-end', marginBottom: '1.5rem' }}>
        <div className="form-group">
          <label>From</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="form-group">
          <label>To</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <div className="form-group">
          <button type="submit" className="btn-primary">Run Analysis</button>
        </div>
      </form>

      {loading ? (
        <div className="data-card"><div className="empty-state">Calculating reorder quantities…</div></div>
      ) : analysis && (
        <>
          <p className="form-hint" style={{ marginBottom: '1rem' }}>
            Period: {analysis.periodStart} to {analysis.periodEnd} · {analysis.lookbackDays} days · {analysis.productsWithSales} products with sales
          </p>

          <div className="stats-grid">
            <div className="stat-card stat-card-hero">
              <div className="stat-info">
                <h3>Need reorder</h3>
                <p className="stat-value">{analysis.productsNeedingReorder}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Target stock = daily sales × (lead time + safety days)
                </p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Critical</h3>
                <p className="stat-value">{analysis.criticalCount}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Out of stock, or days of stock ≤ lead time
                </p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>No supplier history</h3>
                <p className="stat-value">{analysis.missingSupplierCount}</p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Est. order cost</h3>
                <p className="stat-value">₹{Number(analysis.estimatedOrderCost || 0).toFixed(2)}</p>
              </div>
            </div>
          </div>

          <div className="tabs">
            {[
              { id: 'ALL', label: 'All with sales' },
              { id: 'NEEDS_REORDER', label: 'Needs reorder' },
              { id: 'CRITICAL', label: 'Critical' },
              { id: 'NO_SUPPLIER', label: 'Missing supplier' },
            ].map((tab) => (
              <button
                key={tab.id}
                className={`tab-btn ${filter === tab.id ? 'active' : ''}`}
                onClick={() => setFilter(tab.id)}
              >
                {tab.label}
              </button>
            ))}
          </div>

          <div className="data-card">
            {visibleItems.length === 0 ? (
              <div className="empty-state">
                {filter === 'ALL'
                  ? (analysis.message || 'No products with sales in this period.')
                  : (filter === 'NEEDS_REORDER'
                    ? (analysis.message || 'No products in this filter.')
                    : 'No products in this filter.')}
              </div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Urgency</th>
                    <th>Product</th>
                    <th>Stock</th>
                    <th>Daily sales</th>
                    <th>Days left</th>
                    <th>Cover</th>
                    <th>Order qty</th>
                    <th>Supplier</th>
                    <th>Est. cost</th>
                  </tr>
                </thead>
                <tbody>
                  {visibleItems.map((item) => (
                    <tr key={item.productId}>
                      <td>
                        <span className={`badge ${urgencyBadgeClass(item.urgency)}`}>{item.urgency}</span>
                      </td>
                      <td>
                        {item.productName} <span style={{ color: 'var(--text-muted)' }}>({item.sku})</span>
                        <div className="form-hint">
                          Catalog reorder level: {item.staticReorderLevel}
                          {item.typicalReceiptQty != null ? ` · Usual receipt: ${item.typicalReceiptQty}` : ''}
                        </div>
                      </td>
                      <td>{item.currentStock}</td>
                      <td>
                        {Number(item.averageDailyUnitsSold).toFixed(2)}
                        <div className="form-hint">{item.unitsSold} sold in period</div>
                      </td>
                      <td>
                        {item.estimatedDaysUntilStockout == null ? '—' : `~${item.estimatedDaysUntilStockout}d`}
                      </td>
                      <td>
                        {item.coverDays}d
                        <div className="form-hint">Lead {item.leadTimeDays}d + safety {item.safetyDays}d</div>
                      </td>
                      <td>
                        <strong>{item.suggestedQty}</strong>
                        <div className="form-hint">Target {item.targetStock}</div>
                      </td>
                      <td>
                        {item.suggestedSupplierName || '—'}
                        <div className="form-hint">{item.supplierReason}</div>
                      </td>
                      <td>
                        {item.estimatedOrderCost != null
                          ? `₹${Number(item.estimatedOrderCost).toFixed(2)}`
                          : '—'}
                        {item.lastUnitCost != null && (
                          <div className="form-hint">@ ₹{Number(item.lastUnitCost).toFixed(2)}</div>
                        )}
                      </td>
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

export default ReorderSuggestions;
