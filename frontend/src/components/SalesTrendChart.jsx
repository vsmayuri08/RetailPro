import React, { useMemo } from 'react';

export const formatRupees = (value) => {
  const n = Number(value);
  if (!Number.isFinite(n)) return '—';
  if (Math.abs(n) >= 100000) return `₹${(n / 100000).toFixed(1)}L`;
  return `₹${n.toFixed(2)}`;
};

const formatGrowth = (pct) => {
  if (pct == null || pct === '') return '—';
  const n = Number(pct);
  const sign = n > 0 ? '+' : '';
  return `${sign}${n.toFixed(1)}%`;
};

const toLinePath = (coords) => {
  if (coords.length === 0) return '';
  return coords.map((p, i) => `${i === 0 ? 'M' : 'L'} ${p.x} ${p.y}`).join(' ');
};

const SalesTrendChart = ({ trend }) => {
  const layout = useMemo(() => {
    const points = trend?.points || [];
    if (!points.length) return null;
    const width = 640;
    const height = 280;
    const padL = 58;
    const padR = 88;
    const padT = 28;
    const padB = 48;
    const max = Math.max(...points.map((p) => Number(p.revenue) || 0), 1);
    const span = Math.max(points.length - 1, 1);
    const coords = points.map((p, i) => ({
      ...p,
      x: padL + (i / span) * (width - padL - padR),
      y: padT + (1 - Number(p.revenue) / max) * (height - padT - padB),
    }));
    const predictedIndex = coords.findIndex((p) => p.kind === 'PREDICTED');
    const hist = predictedIndex >= 0 ? coords.slice(0, predictedIndex + 1) : coords;
    const histOnly = predictedIndex >= 0 ? coords.slice(0, predictedIndex) : coords;
    const predSeg = predictedIndex > 0 ? coords.slice(predictedIndex - 1, predictedIndex + 1) : [];
    return { width, height, padL, padT, padB, coords, histOnly, predSeg };
  }, [trend]);

  if (!trend) {
    return <p className="dashboard-subtitle">Loading sales trend…</p>;
  }

  const growthClass = Number(trend.expectedGrowthPercent) > 0
    ? 'trend-up'
    : Number(trend.expectedGrowthPercent) < 0 ? 'trend-down' : '';

  return (
    <section className="sales-trend-section">
      <div className="sales-trend-header">
        <div>
          <h2>Sales Trend Prediction</h2>
          <p className="dashboard-subtitle">
            Historical months are live invoice totals. Next month is calculated in RetailPro ({trend.method}).
          </p>
        </div>
      </div>
      <div className="sales-trend-kpis">
        <div>
          <span>Predicted next-month revenue</span>
          <strong>{formatRupees(trend.predictedNextMonthRevenue)}</strong>
        </div>
        <div>
          <span>Expected growth</span>
          <strong className={growthClass}>{formatGrowth(trend.expectedGrowthPercent)}</strong>
        </div>
      </div>
      {layout && (
        <div className="sales-trend-chart-wrap">
          <svg className="sales-trend-chart" viewBox={`0 0 ${layout.width} ${layout.height}`} role="img" aria-label="Revenue history and next-month prediction">
            <text x="14" y="16" className="chart-axis-title">Revenue</text>
            <line x1={layout.padL} y1={layout.padT} x2={layout.padL} y2={layout.height - layout.padB} className="chart-axis" />
            <line x1={layout.padL} y1={layout.height - layout.padB} x2={layout.width - 20} y2={layout.height - layout.padB} className="chart-axis" />
            {layout.histOnly.length > 1 && (
              <path d={toLinePath(layout.histOnly)} className="chart-line-hist" fill="none" />
            )}
            {layout.predSeg.length === 2 && (
              <line
                x1={layout.predSeg[0].x}
                y1={layout.predSeg[0].y}
                x2={layout.predSeg[1].x}
                y2={layout.predSeg[1].y}
                className="chart-line-pred"
              />
            )}
            {layout.coords.map((p) => (
              <g key={p.yearMonth}>
                <circle cx={p.x} cy={p.y} r="5" className={p.kind === 'PREDICTED' ? 'chart-dot-pred' : 'chart-dot-hist'} />
                <text x={p.x} y={layout.height - 18} textAnchor="middle" className="chart-tick">{p.label.replace(' (MTD)', '')}</text>
              </g>
            ))}
            {layout.predSeg.length === 2 && (
              <text x={layout.predSeg[1].x + 10} y={layout.predSeg[1].y + 4} textAnchor="start" className="chart-pred-label">predicted</text>
            )}
            <text x={(layout.width + layout.padL) / 2} y={layout.height - 2} textAnchor="middle" className="chart-axis-caption">Historical → Future</text>
          </svg>
        </div>
      )}
    </section>
  );
};

export default SalesTrendChart;
