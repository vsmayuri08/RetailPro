import React, { useEffect, useState } from 'react';
import { getRecentSales, processSaleReturn, downloadInvoicePdf } from '../api/cashierApi';
import Receipt from '../components/Receipt';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';
import '../styles/cashier.css';

const returnableQty = (item) => Math.max(0, item.quantity - (item.returnedQuantity || 0));
const canReturnSale = (sale) => sale.items?.some((item) => returnableQty(item) > 0);

const Bills = () => {
  const [sales, setSales] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [selectedSale, setSelectedSale] = useState(null);
  const [returningSale, setReturningSale] = useState(null);
  const [returnQty, setReturnQty] = useState({});
  const [returnReason, setReturnReason] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const loadSales = () => {
    setLoading(true);
    getRecentSales()
      .then(setSales)
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load bills'))
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    loadSales();
  }, []);

  const openReturn = (sale) => {
    const qty = {};
    sale.items.forEach((item) => {
      qty[item.id] = 0;
    });
    setReturnQty(qty);
    setReturnReason('');
    setError('');
    setReturningSale(sale);
  };

  const setLineQty = (item, value) => {
    const max = returnableQty(item);
    const next = Math.max(0, Math.min(max, Number(value) || 0));
    setReturnQty((prev) => ({ ...prev, [item.id]: next }));
  };

  const returnAllRemaining = () => {
    if (!returningSale) return;
    const qty = {};
    returningSale.items.forEach((item) => {
      qty[item.id] = returnableQty(item);
    });
    setReturnQty(qty);
  };

  const estimatedRefund = () => {
    if (!returningSale) return 0;
    const goods = returningSale.items.reduce(
      (sum, item) => sum + Number(item.unitPrice) * (returnQty[item.id] || 0),
      0,
    );
    const subtotal = Number(returningSale.subtotal);
    if (subtotal <= 0) return 0;
    return goods * (Number(returningSale.totalAmount) / subtotal);
  };

  const handleProcessReturn = async (e) => {
    e.preventDefault();
    const items = Object.entries(returnQty)
      .filter(([, qty]) => qty > 0)
      .map(([saleItemId, quantity]) => ({ saleItemId: Number(saleItemId), quantity }));
    if (items.length === 0) {
      setError('Select at least one item to return');
      return;
    }
    setSubmitting(true);
    setError('');
    try {
      const result = await processSaleReturn(returningSale.id, { items, reason: returnReason });
      setSuccess(`Return processed for ${result.invoiceNumber} — refund ₹${Number(result.refundAmount).toFixed(2)}`);
      setReturningSale(null);
      loadSales();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to process return');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Bills</h1>
          <p className="page-subtitle">Recent sales for your branch — view a receipt or process a return</p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {success && <div className="alert-banner alert-success">{success}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading bills…</div>
        ) : sales.length === 0 ? (
          <div className="empty-state">No sales yet — completed sales will show up here.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Invoice</th>
                <th>Date</th>
                <th>Customer</th>
                <th>Items</th>
                <th>Total</th>
                <th>Refunded</th>
                <th>Payment</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              {sales.map((s) => (
                <tr key={s.id}>
                  <td>{s.invoiceNumber}</td>
                  <td>{new Date(s.createdAt).toLocaleString()}</td>
                  <td>{s.customerName || 'Walk-in'}</td>
                  <td>{s.items.length}</td>
                  <td>₹{Number(s.totalAmount).toFixed(2)}</td>
                  <td>
                    {Number(s.refundedAmount) > 0
                      ? `₹${Number(s.refundedAmount).toFixed(2)}`
                      : '—'}
                  </td>
                  <td>{s.payment?.method}</td>
                  <td>
                    <button className="btn-secondary btn-sm" onClick={() => setSelectedSale(s)}>View</button>
                    {canReturnSale(s) && (
                      <button className="btn-secondary btn-sm" style={{ marginLeft: '0.4rem' }} onClick={() => openReturn(s)}>
                        Return
                      </button>
                    )}
                    {!canReturnSale(s) && Number(s.refundedAmount) > 0 && (
                      <span className="badge badge-silver" style={{ marginLeft: '0.4rem' }}>Returned</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {selectedSale && (
        <div className="modal-overlay" onClick={() => setSelectedSale(null)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <Receipt sale={selectedSale} onDownloadPdf={() => downloadInvoicePdf(selectedSale.id, selectedSale.invoiceNumber)} />
            <div className="form-actions">
              {canReturnSale(selectedSale) && (
                <button className="btn-secondary" onClick={() => { setSelectedSale(null); openReturn(selectedSale); }}>
                  Return items
                </button>
              )}
              <button className="btn-secondary" onClick={() => setSelectedSale(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {returningSale && (
        <div className="modal-overlay" onClick={() => !submitting && setReturningSale(null)}>
          <div className="modal-box" style={{ maxWidth: '640px' }} onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Return {returningSale.invoiceNumber}</h2>
            <p className="form-hint" style={{ marginBottom: '1rem' }}>
              Refunds the customer, restocks returned units, and reverses loyalty this sale earned — in one step.
            </p>
            <form onSubmit={handleProcessReturn}>
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Product</th>
                    <th>Sold</th>
                    <th>Already returned</th>
                    <th>Return now</th>
                  </tr>
                </thead>
                <tbody>
                  {returningSale.items.map((item) => (
                    <tr key={item.id}>
                      <td>{item.productName}</td>
                      <td>{item.quantity}</td>
                      <td>{item.returnedQuantity || 0}</td>
                      <td>
                        <input
                          type="number"
                          min="0"
                          max={returnableQty(item)}
                          value={returnQty[item.id] ?? 0}
                          onChange={(e) => setLineQty(item, e.target.value)}
                          disabled={returnableQty(item) === 0}
                          style={{ width: '5rem' }}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
              <div className="form-actions" style={{ justifyContent: 'flex-start' }}>
                <button type="button" className="btn-secondary btn-sm" onClick={returnAllRemaining}>
                  Return all remaining
                </button>
              </div>
              <div className="form-group" style={{ marginTop: '1rem' }}>
                <label>Reason</label>
                <textarea
                  value={returnReason}
                  onChange={(e) => setReturnReason(e.target.value)}
                  required
                  placeholder="Damaged, wrong item, customer changed mind…"
                />
              </div>
              <p className="form-hint">
                Estimated refund: ₹{estimatedRefund().toFixed(2)}
                {returningSale.loyaltyPointsEarned > 0 && ' · loyalty points for this sale will be reversed in proportion'}
              </p>
              <div className="form-actions">
                <button type="button" className="btn-secondary" disabled={submitting} onClick={() => setReturningSale(null)}>
                  Cancel
                </button>
                <button type="submit" className="btn-primary" disabled={submitting}>
                  {submitting ? 'Processing…' : 'Process return'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Bills;
