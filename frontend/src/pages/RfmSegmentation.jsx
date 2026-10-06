import React, { useEffect, useState } from 'react';
import { getRfmSegmentation } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const SEGMENT_ORDER = ['Champions', 'Loyal', 'New', 'Potential', 'At Risk', 'Lost'];

const segmentBadgeClass = (segment) => {
  switch (segment) {
    case 'Champions': return 'badge-approved';
    case 'Loyal': return 'badge-active';
    case 'New': return 'badge-pending';
    case 'Potential': return 'badge-silver';
    case 'At Risk': return 'badge-warning';
    default: return 'badge-danger';
  }
};

const segmentDescription = {
  Champions: 'Bought recently and often — your highest-priority cross-sell and retain group.',
  Loyal: 'Steady buyers who still shop regularly — keep them engaged.',
  New: 'Recent first purchases with little history yet — a chance to convert them.',
  Potential: 'Mid-range recency and frequency — worth a nudge before they cool off.',
  'At Risk': 'Used to buy often but have gone quiet — win-back matters here.',
  Lost: 'Long gap and few purchases — cheapest not to over-invest unless they return.',
};

const RfmSegmentation = () => {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [segmentFilter, setSegmentFilter] = useState('ALL');

  const loadAnalysis = async (start, end) => {
    setLoading(true);
    try {
      const data = await getRfmSegmentation(start || undefined, end || undefined);
      setAnalysis(data);
      setStartDate(data.periodStart);
      setEndDate(data.periodEnd);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load RFM segmentation');
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

  const summaryFor = (segment) => analysis?.segmentSummaries.find((s) => s.segment === segment);

  const visibleCustomers = analysis
    ? analysis.customers.filter((c) => segmentFilter === 'ALL' || c.segment === segmentFilter)
    : [];

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">RFM Segmentation</h1>
          <p className="page-subtitle">
            Customers scored on recency, frequency, and spend — a sharper cut than Bronze–Platinum loyalty points
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
        <div className="data-card"><div className="empty-state">Scoring customers…</div></div>
      ) : analysis && (
        <>
          <p className="form-hint" style={{ marginBottom: '1rem' }}>
            Period: {analysis.periodStart} to {analysis.periodEnd} · {analysis.totalCustomers} customers ·
            ₹{Number(analysis.totalMonetary).toFixed(2)} spend
          </p>

          {analysis.message && (!analysis.customers || analysis.customers.length === 0) ? (
            <div className="data-card">
              <div className="empty-state">{analysis.message}</div>
            </div>
          ) : (
            <>
              <div className="stats-grid">
                {SEGMENT_ORDER.map((segment, index) => {
                  const summary = summaryFor(segment);
                  if (!summary) return null;
                  return (
                    <div className={`stat-card ${index === 0 ? 'stat-card-hero' : ''}`} key={segment}>
                      <div className="stat-info">
                        <h3>
                          <span className={`badge ${segmentBadgeClass(segment)}`}>{segment}</span>
                        </h3>
                        <p className="stat-value">{summary.customerCount} customers</p>
                        <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                          {Number(summary.customerCountPercent).toFixed(0)}% · ₹{Number(summary.totalMonetary).toFixed(2)} spend
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>

              <p className="form-hint" style={{ margin: '1rem 0' }}>
                {segmentDescription[segmentFilter] || 'Every registered customer who bought at this branch in the period, scored 1–5 on recency, frequency, and monetary value.'}
              </p>

              <div className="tabs">
                {['ALL', ...SEGMENT_ORDER].map((seg) => (
                  <button
                    key={seg}
                    className={`tab-btn ${segmentFilter === seg ? 'active' : ''}`}
                    onClick={() => setSegmentFilter(seg)}
                  >
                    {seg === 'ALL' ? 'All Customers' : seg}
                  </button>
                ))}
              </div>

              <div className="data-card">
                {visibleCustomers.length === 0 ? (
                  <div className="empty-state">No customers in this segment.</div>
                ) : (
                  <table className="data-table">
                    <thead>
                      <tr>
                        <th>Customer</th>
                        <th>Loyalty tier</th>
                        <th>Recency (days)</th>
                        <th>Frequency</th>
                        <th>Monetary</th>
                        <th>R / F / M</th>
                        <th>Segment</th>
                      </tr>
                    </thead>
                    <tbody>
                      {visibleCustomers.map((row) => (
                        <tr key={row.customerId}>
                          <td>
                            {row.customerName}{' '}
                            <span style={{ color: 'var(--text-muted)' }}>({row.phone})</span>
                          </td>
                          <td>{row.loyaltyTier || '—'}</td>
                          <td>{row.recencyDays}</td>
                          <td>{row.frequency}</td>
                          <td>₹{Number(row.monetary).toFixed(2)}</td>
                          <td>{row.recencyScore} / {row.frequencyScore} / {row.monetaryScore}</td>
                          <td>
                            <span className={`badge ${segmentBadgeClass(row.segment)}`}>{row.segment}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                )}
              </div>
            </>
          )}
        </>
      )}
    </div>
  );
};

export default RfmSegmentation;
