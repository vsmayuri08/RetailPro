import React, { useEffect, useState } from 'react';
import { getAbcAnalysis } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const classBadgeClass = (abcClass) => {
  switch (abcClass) {
    case 'A': return 'badge-approved';
    case 'B': return 'badge-pending';
    default: return 'badge-silver';
  }
};

const classDescription = {
  A: 'Top revenue drivers — prioritize these for reordering and never let them go out of stock.',
  B: 'Moderate contributors — worth regular attention, but less urgent than Class A.',
  C: 'Long tail, including anything with zero sales in the period — watch for overstocked slow movers here.',
};

const AbcAnalysis = () => {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [classFilter, setClassFilter] = useState('ALL');

  const loadAnalysis = async (start, end) => {
    setLoading(true);
    try {
      const data = await getAbcAnalysis(start || undefined, end || undefined);
      setAnalysis(data);
      setStartDate(data.periodStart);
      setEndDate(data.periodEnd);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load ABC analysis');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadAnalysis();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRunAnalysis = (e) => {
    e.preventDefault();
    loadAnalysis(startDate, endDate);
  };

  const classSummaryFor = (abcClass) => analysis?.classSummaries.find((c) => c.abcClass === abcClass);

  const visibleItems = analysis
    ? analysis.items.filter((i) => classFilter === 'ALL' || i.abcClass === classFilter)
    : [];

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">ABC Analysis</h1>
          <p className="page-subtitle">
            Products ranked by real revenue contribution — Class A drives most of your revenue, Class C is the long tail
          </p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      <form className="form-row" onSubmit={handleRunAnalysis} style={{ alignItems: 'flex-end', marginBottom: '1.5rem' }}>
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
        <div className="data-card"><div className="empty-state">Analyzing sales data…</div></div>
      ) : analysis && (
        <>
          <p className="form-hint" style={{ marginBottom: '1rem' }}>
            Period: {analysis.periodStart} to {analysis.periodEnd} · Total revenue: ₹{Number(analysis.totalRevenue).toFixed(2)} · {analysis.totalProducts} active products
          </p>

          <div className="stats-grid">
            {['A', 'B', 'C'].map((cls) => {
              const summary = classSummaryFor(cls);
              if (!summary) return null;
              return (
                <div className={`stat-card ${cls === 'A' ? 'stat-card-hero' : ''}`} key={cls}>
                  <div className="stat-icon">
                    <span className={`badge ${classBadgeClass(cls)}`} style={{ fontSize: '1rem', padding: '0.3rem 0.7rem' }}>
                      Class {cls}
                    </span>
                  </div>
                  <div className="stat-info">
                    <h3>{summary.itemCount} products ({Number(summary.itemCountPercent).toFixed(0)}%)</h3>
                    <p className="stat-value">{Number(summary.revenuePercent).toFixed(1)}% of revenue</p>
                    <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                      ₹{Number(summary.revenue).toFixed(2)} revenue · ₹{Number(summary.stockValue).toFixed(2)} stock value
                    </p>
                  </div>
                </div>
              );
            })}
          </div>

          <p className="form-hint" style={{ margin: '1rem 0' }}>
            {classDescription[classFilter] || 'Every product this branch sells, ranked by revenue contribution.'}
          </p>

          <div className="tabs">
            {['ALL', 'A', 'B', 'C'].map((cls) => (
              <button
                key={cls}
                className={`tab-btn ${classFilter === cls ? 'active' : ''}`}
                onClick={() => setClassFilter(cls)}
              >
                {cls === 'ALL' ? 'All Products' : `Class ${cls}`}
              </button>
            ))}
          </div>

          <div className="data-card">
            {visibleItems.length === 0 ? (
              <div className="empty-state">No products in this class.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Units Sold</th>
                    <th>Revenue</th>
                    <th>% of Revenue</th>
                    <th>Cumulative %</th>
                    <th>Class</th>
                    <th>Current Stock</th>
                  </tr>
                </thead>
                <tbody>
                  {visibleItems.map((item) => (
                    <tr key={item.productId}>
                      <td>{item.rank}</td>
                      <td>{item.productName} <span style={{ color: 'var(--text-muted)' }}>({item.sku})</span></td>
                      <td>{item.category || '—'}</td>
                      <td>{item.unitsSold}</td>
                      <td>₹{Number(item.revenue).toFixed(2)}</td>
                      <td>{Number(item.revenuePercent).toFixed(1)}%</td>
                      <td>{Number(item.cumulativeRevenuePercent).toFixed(1)}%</td>
                      <td>
                        <span className={`badge ${classBadgeClass(item.abcClass)}`}>{item.abcClass}</span>
                        {!item.hasSales && <span className="badge badge-danger" style={{ marginLeft: '0.4rem' }}>No sales</span>}
                      </td>
                      <td>{item.currentStock}</td>
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

export default AbcAnalysis;
