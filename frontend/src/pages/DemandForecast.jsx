import React, { useEffect, useState } from 'react';
import { getDemandForecast } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const STOCKOUT_WARN_DAYS = 7;

const trendBadgeClass = (trend) => {
  switch (trend) {
    case 'RISING': return 'badge-approved';
    case 'FALLING': return 'badge-danger';
    default: return 'badge-silver';
  }
};

const stockoutBadge = (item) => {
  if (!item.hasSales) {
    return { className: 'badge-silver', label: 'No sales' };
  }
  if (item.estimatedDaysUntilStockout == null) {
    return { className: 'badge-approved', label: 'No stockout risk' };
  }
  if (item.estimatedDaysUntilStockout < STOCKOUT_WARN_DAYS) {
    return { className: 'badge-danger', label: `Stockout in ~${item.estimatedDaysUntilStockout}d` };
  }
  if (item.stockoutRisk) {
    return { className: 'badge-warning', label: `At risk (~${item.estimatedDaysUntilStockout}d)` };
  }
  return { className: 'badge-approved', label: `~${item.estimatedDaysUntilStockout}d of stock` };
};

const DemandForecast = () => {
  const [forecast, setForecast] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');
  const [forecastDays, setForecastDays] = useState(7);
  const [filter, setFilter] = useState('ALL');

  const loadForecast = async (start, end, days) => {
    setLoading(true);
    try {
      const data = await getDemandForecast(start || undefined, end || undefined, days || undefined);
      setForecast(data);
      setStartDate(data.periodStart);
      setEndDate(data.periodEnd);
      setForecastDays(data.forecastDays);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load demand forecast');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadForecast();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const handleRunForecast = (e) => {
    e.preventDefault();
    loadForecast(startDate, endDate, forecastDays);
  };

  const visibleItems = forecast
    ? forecast.items.filter((item) => {
      if (filter === 'AT_RISK') return item.stockoutRisk;
      if (filter === 'RISING') return item.trend === 'RISING';
      if (filter === 'FALLING') return item.trend === 'FALLING';
      if (filter === 'NO_SALES') return !item.hasSales;
      return true;
    })
    : [];

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Demand Forecast</h1>
          <p className="page-subtitle">
            Expected units over the next few days from a simple moving average of real sales — flag products that would stock out at the current pace
          </p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}

      <form className="form-row" onSubmit={handleRunForecast} style={{ alignItems: 'flex-end', marginBottom: '1.5rem' }}>
        <div className="form-group">
          <label>From</label>
          <input type="date" value={startDate} onChange={(e) => setStartDate(e.target.value)} />
        </div>
        <div className="form-group">
          <label>To</label>
          <input type="date" value={endDate} onChange={(e) => setEndDate(e.target.value)} />
        </div>
        <div className="form-group">
          <label>Forecast horizon (days)</label>
          <input
            type="number"
            min="1"
            max="90"
            value={forecastDays}
            onChange={(e) => setForecastDays(Number(e.target.value))}
          />
        </div>
        <div className="form-group">
          <button type="submit" className="btn-primary">Run Forecast</button>
        </div>
      </form>

      {loading ? (
        <div className="data-card"><div className="empty-state">Forecasting from sales history…</div></div>
      ) : forecast && (
        <>
          <p className="form-hint" style={{ marginBottom: '1rem' }}>
            Lookback: {forecast.periodStart} to {forecast.periodEnd} ({forecast.lookbackDays} days) · Horizon: next {forecast.forecastDays} days · {forecast.totalProducts} active products
          </p>

          {forecast.totalProducts === 0 ? (
            <div className="data-card"><div className="empty-state">No active products in this branch yet.</div></div>
          ) : forecast.items.every((i) => !i.hasSales) ? (
            <div className="data-card" style={{ marginBottom: '1.5rem' }}>
              <div className="empty-state">
                Not enough sales history yet to forecast demand. Once this branch records sales, averages and stockout risk will appear here.
              </div>
            </div>
          ) : null}

          <div className="stats-grid">
            <div className="stat-card stat-card-hero">
              <div className="stat-info">
                <h3>Stockout risk</h3>
                <p className="stat-value">{forecast.stockoutRiskCount}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Projected demand over {forecast.forecastDays} days exceeds current stock
                </p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Rising demand</h3>
                <p className="stat-value">{forecast.risingCount}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Late-window pace is up vs the earliest third of the lookback
                </p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>Falling demand</h3>
                <p className="stat-value">{forecast.fallingCount}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Late-window pace is down vs the earliest third of the lookback
                </p>
              </div>
            </div>
            <div className="stat-card">
              <div className="stat-info">
                <h3>No sales in period</h3>
                <p className="stat-value">{forecast.productsWithNoSales}</p>
                <p className="form-hint" style={{ marginTop: '0.25rem' }}>
                  Zero units sold — no forecastable stockout from demand
                </p>
              </div>
            </div>
          </div>

          <div className="tabs">
            {[
              { id: 'ALL', label: 'All Products' },
              { id: 'AT_RISK', label: 'At Risk' },
              { id: 'RISING', label: 'Rising' },
              { id: 'FALLING', label: 'Falling' },
              { id: 'NO_SALES', label: 'No Sales' },
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
              <div className="empty-state">No products in this view.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Category</th>
                    <th>Units Sold</th>
                    <th>Avg / Day</th>
                    <th>Forecast ({forecast.forecastDays}d)</th>
                    <th>Trend</th>
                    <th>Current Stock</th>
                    <th>Days until stockout</th>
                    <th>Status</th>
                  </tr>
                </thead>
                <tbody>
                  {visibleItems.map((item) => {
                    const badge = stockoutBadge(item);
                    return (
                      <tr key={item.productId}>
                        <td>{item.productName} <span style={{ color: 'var(--text-muted)' }}>({item.sku})</span></td>
                        <td>{item.category || '—'}</td>
                        <td>{item.unitsSold}</td>
                        <td>{Number(item.averageDailyUnitsSold).toFixed(2)}</td>
                        <td>{Number(item.forecastedDemand).toFixed(2)}</td>
                        <td>
                          <span className={`badge ${trendBadgeClass(item.trend)}`}>{item.trend}</span>
                        </td>
                        <td>{item.currentStock}</td>
                        <td>
                          {item.estimatedDaysUntilStockout == null
                            ? '—'
                            : item.estimatedDaysUntilStockout}
                        </td>
                        <td>
                          <span className={`badge ${badge.className}`}>{badge.label}</span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            )}
          </div>
        </>
      )}
    </div>
  );
};

export default DemandForecast;
