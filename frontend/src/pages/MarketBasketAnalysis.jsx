import React, { useEffect, useState } from 'react';
import { getMarketBasket } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const MarketBasketAnalysis = () => {
  const [analysis, setAnalysis] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const loadAnalysis = async (start, end) => {
    setLoading(true);
    try {
      const data = await getMarketBasket(start || undefined, end || undefined);
      setAnalysis(data);
      setStartDate(data.periodStart);
      setEndDate(data.periodEnd);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load market basket analysis');
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

  const pairs = analysis?.pairs || [];

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Market Basket Analysis</h1>
          <p className="page-subtitle">
            Products frequently bought together in the same sale — use this to spot cross-sell opportunities
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
            Period: {analysis.periodStart} to {analysis.periodEnd} · {analysis.totalTransactions} sales ·
            min co-occurrence: {analysis.minSupport}
          </p>

          <div className="data-card">
            {pairs.length === 0 ? (
              <div className="empty-state">{analysis.message || 'Not enough data yet'}</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>#</th>
                    <th>If they bought</th>
                    <th>They also bought</th>
                    <th>Times together</th>
                    <th>Support</th>
                    <th>Confidence</th>
                  </tr>
                </thead>
                <tbody>
                  {pairs.map((pair, index) => (
                    <tr key={`${pair.productAName}-${pair.productBName}-${index}`}>
                      <td>{index + 1}</td>
                      <td>{pair.productAName}</td>
                      <td>{pair.productBName}</td>
                      <td>{pair.timesTogether}</td>
                      <td>{Number(pair.supportPercent).toFixed(1)}%</td>
                      <td>
                        <span className="badge badge-approved">{Number(pair.confidencePercent).toFixed(0)}%</span>
                        <span className="form-hint" style={{ marginLeft: '0.5rem' }}>
                          of customers who bought {pair.productAName} also bought {pair.productBName}
                        </span>
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

export default MarketBasketAnalysis;
