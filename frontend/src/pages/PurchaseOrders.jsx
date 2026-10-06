import React, { useEffect, useState } from 'react';
import {
  getPurchaseOrders,
  submitPurchaseOrder,
  approvePurchaseOrder,
  cancelPurchaseOrder,
  receivePurchaseOrder,
} from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const statusBadge = (status) => {
  if (status === 'APPROVED' || status === 'RECEIVED') return 'badge-approved';
  if (status === 'CANCELLED') return 'badge-rejected';
  if (status === 'PENDING') return 'badge-pending';
  return 'badge-silver';
};

const PurchaseOrders = () => {
  const [orders, setOrders] = useState([]);
  const [selected, setSelected] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [busy, setBusy] = useState(false);
  const [filter, setFilter] = useState('ALL');

  const load = async (keepId) => {
    setLoading(true);
    try {
      const data = await getPurchaseOrders();
      setOrders(data);
      if (keepId) {
        setSelected(data.find((o) => o.id === keepId) || null);
      }
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load purchase orders');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    load();
  }, []);

  const run = async (fn, id) => {
    setBusy(true);
    setError('');
    setSuccess('');
    try {
      const po = await fn(id);
      setSuccess(
        po.status === 'APPROVED'
          ? `${po.poNumber} approved. It was not sent to ${po.supplierName}.`
          : `${po.poNumber} is now ${po.status}.`
      );
      await load(po.id);
    } catch (err) {
      setError(err?.response?.data?.message || 'Could not update the purchase order');
    } finally {
      setBusy(false);
    }
  };

  const visible = orders.filter((o) => (filter === 'ALL' ? true : o.status === filter));

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Purchase Orders</h1>
          <p className="page-subtitle">
            Internal orders only — RetailPro never emails or posts these to a supplier. A manager must approve before stock can be received.
          </p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {success && <div className="alert-banner alert-success">{success}</div>}

      <div className="tabs">
        {['ALL', 'DRAFT', 'PENDING', 'APPROVED', 'RECEIVED', 'CANCELLED'].map((id) => (
          <button key={id} className={`tab-btn ${filter === id ? 'active' : ''}`} onClick={() => setFilter(id)}>
            {id === 'ALL' ? 'All' : id.charAt(0) + id.slice(1).toLowerCase()}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="data-card"><div className="empty-state">Loading purchase orders…</div></div>
      ) : (
        <div className="data-card">
          {visible.length === 0 ? (
            <div className="empty-state">No purchase orders in this filter. Create them from Reorder Suggestions.</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>PO Number</th>
                  <th>Branch</th>
                  <th>Supplier</th>
                  <th>Expected delivery</th>
                  <th>Total</th>
                  <th>Status</th>
                </tr>
              </thead>
              <tbody>
                {visible.map((po) => (
                  <tr
                    key={po.id}
                    onClick={() => setSelected(po)}
                    style={{ cursor: 'pointer', background: selected?.id === po.id ? 'var(--bg-card-hover)' : undefined }}
                  >
                    <td>{po.poNumber}</td>
                    <td>{po.branchName}</td>
                    <td>{po.supplierName}</td>
                    <td>{po.expectedDeliveryDate || '—'}</td>
                    <td>₹{Number(po.totalAmount).toFixed(2)}</td>
                    <td><span className={`badge ${statusBadge(po.status)}`}>{po.status}</span></td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {selected && (
        <div className="data-card" style={{ marginTop: '1.25rem' }}>
          <h2 className="page-subtitle" style={{ marginBottom: '0.75rem' }}>{selected.poNumber}</h2>
          <p className="form-hint">{selected.note}</p>
          <p style={{ margin: '0.5rem 0' }}>
            <strong>Branch:</strong> {selected.branchName} · <strong>Supplier:</strong> {selected.supplierName} ·{' '}
            <strong>Expected delivery:</strong> {selected.expectedDeliveryDate || '—'}
          </p>
          <table className="data-table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Quantity</th>
                <th>Purchase price</th>
                <th>Line total</th>
              </tr>
            </thead>
            <tbody>
              {selected.items?.map((line) => (
                <tr key={line.productId}>
                  <td>{line.productName} <span style={{ color: 'var(--text-muted)' }}>({line.sku})</span></td>
                  <td>{line.quantity}</td>
                  <td>₹{Number(line.purchasePrice).toFixed(2)}</td>
                  <td>₹{Number(line.lineTotal).toFixed(2)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="actions-grid" style={{ marginTop: '1rem' }}>
            {selected.status === 'DRAFT' && (
              <button className="btn-primary" disabled={busy} onClick={() => run(submitPurchaseOrder, selected.id)}>
                Submit for approval
              </button>
            )}
            {(selected.status === 'DRAFT' || selected.status === 'PENDING') && (
              <button className="btn-primary" disabled={busy} onClick={() => run(approvePurchaseOrder, selected.id)}>
                Approve
              </button>
            )}
            {selected.status === 'APPROVED' && (
              <button className="btn-primary" disabled={busy} onClick={() => run(receivePurchaseOrder, selected.id)}>
                Mark received
              </button>
            )}
            {selected.status !== 'RECEIVED' && selected.status !== 'CANCELLED' && (
              <button className="action-btn" disabled={busy} onClick={() => run(cancelPurchaseOrder, selected.id)}>
                Cancel
              </button>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default PurchaseOrders;
