import React, { useEffect, useState } from 'react';
import {
  getProducts, getProductAlerts, adjustStock,
  getOtherBranches, browseBranchCatalog, requestStockTransfer,
  getIncomingTransfers, getOutgoingTransfers, approveTransfer, rejectTransfer,
  receiveStock, getStockHistory, getManagerSuppliers,
} from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const statusBadgeClass = (status) => {
  if (status === 'APPROVED') return 'badge-approved';
  if (status === 'REJECTED') return 'badge-rejected';
  return 'badge-pending';
};

const emptyTransferForm = { branchId: '', productId: '', quantity: 1, note: '' };
const emptyReceiveForm = { batchNumber: '', quantityReceived: '', purchasePrice: '', expiryDate: '', supplierId: '', receivedDate: '' };

const Inventory = () => {
  const [tab, setTab] = useState('alerts');

  const [alerts, setAlerts] = useState([]);
  const [products, setProducts] = useState([]);
  const [loadingAlerts, setLoadingAlerts] = useState(true);

  const [incoming, setIncoming] = useState([]);
  const [outgoing, setOutgoing] = useState([]);
  const [loadingTransfers, setLoadingTransfers] = useState(true);

  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [showTransferModal, setShowTransferModal] = useState(false);
  const [otherBranches, setOtherBranches] = useState([]);
  const [catalog, setCatalog] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(false);
  const [transferForm, setTransferForm] = useState(emptyTransferForm);
  const [submitting, setSubmitting] = useState(false);

  const [showReceiveModal, setShowReceiveModal] = useState(false);
  const [receiveTarget, setReceiveTarget] = useState(null);
  const [receiveForm, setReceiveForm] = useState(emptyReceiveForm);
  const [suppliers, setSuppliers] = useState([]);
  const [receiving, setReceiving] = useState(false);

  const [historyTarget, setHistoryTarget] = useState(null);
  const [stockHistory, setStockHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  const loadInventory = async () => {
    setLoadingAlerts(true);
    try {
      const [alertData, productData] = await Promise.all([getProductAlerts(), getProducts()]);
      setAlerts(alertData);
      setProducts(productData);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load inventory');
    } finally {
      setLoadingAlerts(false);
    }
  };

  const loadTransfers = async () => {
    setLoadingTransfers(true);
    try {
      const [incomingData, outgoingData] = await Promise.all([getIncomingTransfers(), getOutgoingTransfers()]);
      setIncoming(incomingData);
      setOutgoing(outgoingData);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load stock transfers');
    } finally {
      setLoadingTransfers(false);
    }
  };

  useEffect(() => {
    loadInventory();
    loadTransfers();
  }, []);

  const handleStep = async (product, delta) => {
    if (product.quantity + delta < 0) return;
    try {
      await adjustStock(product.id, { delta, reason: delta > 0 ? 'Restock' : 'Manual adjustment' });
      await loadInventory();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to adjust stock');
    }
  };

  const openTransferModal = async () => {
    setTransferForm(emptyTransferForm);
    setCatalog([]);
    setShowTransferModal(true);
    try {
      const branches = await getOtherBranches();
      setOtherBranches(branches);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load branches');
    }
  };

  const handleBranchSelect = async (e) => {
    const branchId = e.target.value;
    setTransferForm({ ...transferForm, branchId, productId: '' });
    if (!branchId) { setCatalog([]); return; }
    setCatalogLoading(true);
    try {
      const items = await browseBranchCatalog(branchId);
      setCatalog(items);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load that branch\'s catalog');
    } finally {
      setCatalogLoading(false);
    }
  };

  const handleTransferSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    setError('');
    try {
      await requestStockTransfer({
        productId: Number(transferForm.productId),
        quantity: Number(transferForm.quantity),
        note: transferForm.note,
      });
      setSuccessMsg('Transfer request submitted');
      setShowTransferModal(false);
      await loadTransfers();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to submit transfer request');
    } finally {
      setSubmitting(false);
    }
  };

  const handleDecision = async (id, approve) => {
    try {
      if (approve) {
        await approveTransfer(id);
      } else {
        await rejectTransfer(id);
      }
      await Promise.all([loadTransfers(), loadInventory()]);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to record decision');
    }
  };

  const openReceiveModal = async (product) => {
    setReceiveTarget(product);
    setReceiveForm({ ...emptyReceiveForm, receivedDate: new Date().toISOString().slice(0, 10) });
    setShowReceiveModal(true);
    try {
      const data = await getManagerSuppliers();
      setSuppliers(data);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load suppliers');
    }
  };

  const handleReceiveSubmit = async (e) => {
    e.preventDefault();
    setReceiving(true);
    setError('');
    try {
      await receiveStock(receiveTarget.id, {
        batchNumber: receiveForm.batchNumber || undefined,
        quantityReceived: Number(receiveForm.quantityReceived),
        purchasePrice: receiveForm.purchasePrice ? Number(receiveForm.purchasePrice) : undefined,
        expiryDate: receiveForm.expiryDate || undefined,
        supplierId: receiveForm.supplierId ? Number(receiveForm.supplierId) : undefined,
        receivedDate: receiveForm.receivedDate || undefined,
      });
      setSuccessMsg(`Stock received for ${receiveTarget.name}`);
      setShowReceiveModal(false);
      await loadInventory();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to record received stock');
    } finally {
      setReceiving(false);
    }
  };

  const openHistory = async (product) => {
    setHistoryTarget(product);
    setHistoryLoading(true);
    try {
      const history = await getStockHistory(product.id);
      setStockHistory(history);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load stock history');
    } finally {
      setHistoryLoading(false);
    }
  };

  const pendingIncomingCount = incoming.filter((t) => t.status === 'PENDING').length;

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Inventory</h1>
          <p className="page-subtitle">Stock levels, alerts, and stock transfers for your branch</p>
        </div>
        <button className="btn-primary" onClick={openTransferModal}>+ Request Transfer</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="tabs">
        <button className={`tab-btn ${tab === 'alerts' ? 'active' : ''}`} onClick={() => setTab('alerts')}>
          Low Stock &amp; Near Expiry {alerts.length > 0 && <span className="tab-count">{alerts.length}</span>}
        </button>
        <button className={`tab-btn ${tab === 'transfers' ? 'active' : ''}`} onClick={() => setTab('transfers')}>
          Stock Transfers {pendingIncomingCount > 0 && <span className="tab-count">{pendingIncomingCount}</span>}
        </button>
        <button className={`tab-btn ${tab === 'all' ? 'active' : ''}`} onClick={() => setTab('all')}>
          All Products
        </button>
      </div>

      {tab === 'alerts' && (
        <div className="data-card">
          {loadingAlerts ? (
            <div className="empty-state">Loading alerts…</div>
          ) : alerts.length === 0 ? (
            <div className="empty-state">Nothing needs attention — no low-stock or near-expiry items.</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Stock</th>
                  <th>Reorder Level</th>
                  <th>Expiry</th>
                  <th>Current Discount</th>
                  <th>Flags</th>
                  <th>Adjust</th>
                </tr>
              </thead>
              <tbody>
                {alerts.map((p) => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td>{p.sku}</td>
                    <td>{p.quantity}</td>
                    <td>{p.reorderLevel}</td>
                    <td>{p.expiryDate || '—'}</td>
                    <td>
                      {Number(p.expiryDiscountPercent) > 0
                        ? <span className="badge badge-gold">-{Number(p.expiryDiscountPercent).toFixed(0)}% (₹{Number(p.discountedPrice).toFixed(2)})</span>
                        : '—'}
                    </td>
                    <td>
                      <div className="tag-row">
                        {p.lowStock && <span className="badge badge-warning">Low stock</span>}
                        {p.expired && <span className="badge badge-danger">Cannot be billed</span>}
                        {!p.expired && p.nearExpiry && <span className="badge badge-warning">Near expiry</span>}
                      </div>
                    </td>
                    <td>
                      <div className="stock-stepper">
                        <button type="button" onClick={() => handleStep(p, -1)} disabled={p.quantity === 0}>−</button>
                        <span className="stock-qty">{p.quantity}</span>
                        <button type="button" onClick={() => handleStep(p, 1)}>+</button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'all' && (
        <div className="data-card">
          {products.length === 0 ? (
            <div className="empty-state">No products yet — add some from the Products page first.</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Product</th>
                  <th>SKU</th>
                  <th>Category</th>
                  <th>Stock</th>
                  <th>Adjust</th>
                  <th>Batches</th>
                </tr>
              </thead>
              <tbody>
                {products.filter((p) => p.active).map((p) => (
                  <tr key={p.id}>
                    <td>{p.name}</td>
                    <td>{p.sku}</td>
                    <td>{p.category || '—'}</td>
                    <td>{p.quantity}</td>
                    <td>
                      <div className="stock-stepper">
                        <button type="button" onClick={() => handleStep(p, -1)} disabled={p.quantity === 0}>−</button>
                        <span className="stock-qty">{p.quantity}</span>
                        <button type="button" onClick={() => handleStep(p, 1)}>+</button>
                      </div>
                    </td>
                    <td style={{ display: 'flex', gap: '0.5rem' }}>
                      <button className="btn-secondary btn-sm" onClick={() => openReceiveModal(p)}>Receive Stock</button>
                      <button className="btn-secondary btn-sm" onClick={() => openHistory(p)}>History</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          )}
        </div>
      )}

      {tab === 'transfers' && (
        <>
          <div className="quick-actions-section">
            <h2>Incoming Requests <span className="empty-hint">— other branches asking for your stock; only you can approve these</span></h2>
            <div className="data-card" style={{ marginTop: '1rem' }}>
              {loadingTransfers ? (
                <div className="empty-state">Loading…</div>
              ) : incoming.length === 0 ? (
                <div className="empty-state">No incoming transfer requests.</div>
              ) : (
                incoming.map((t) => (
                  <div className="transfer-card" key={t.id}>
                    <div className="transfer-main">
                      <span className="transfer-title">{t.quantity} × {t.productName} ({t.productSku})</span>
                      <span className="transfer-meta">Requested by {t.destinationBranchName} · {t.requestedByName}</span>
                      {t.note && <span className="transfer-meta">Note: {t.note}</span>}
                    </div>
                    <div className="transfer-actions">
                      <span className={`badge ${statusBadgeClass(t.status)}`}>{t.status}</span>
                      {t.status === 'PENDING' && (
                        <>
                          <button className="btn-sm btn-success" onClick={() => handleDecision(t.id, true)}>Approve</button>
                          <button className="btn-sm btn-danger" onClick={() => handleDecision(t.id, false)}>Reject</button>
                        </>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          <div className="quick-actions-section" style={{ marginTop: '2.5rem' }}>
            <h2>My Requests <span className="empty-hint">— stock you've requested from other branches</span></h2>
            <div className="data-card" style={{ marginTop: '1rem' }}>
              {loadingTransfers ? (
                <div className="empty-state">Loading…</div>
              ) : outgoing.length === 0 ? (
                <div className="empty-state">You haven't requested any stock transfers yet.</div>
              ) : (
                outgoing.map((t) => (
                  <div className="transfer-card" key={t.id}>
                    <div className="transfer-main">
                      <span className="transfer-title">{t.quantity} × {t.productName} ({t.productSku})</span>
                      <span className="transfer-meta">From {t.sourceBranchName}</span>
                    </div>
                    <span className={`badge ${statusBadgeClass(t.status)}`}>{t.status}</span>
                  </div>
                ))
              )}
            </div>
          </div>
        </>
      )}

      {showTransferModal && (
        <div className="modal-overlay" onClick={() => setShowTransferModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Request Stock Transfer</h2>
            <form onSubmit={handleTransferSubmit}>
              <div className="form-group">
                <label>Source Branch</label>
                <select value={transferForm.branchId} onChange={handleBranchSelect} required>
                  <option value="" disabled>Select a branch to request stock from</option>
                  {otherBranches.map((b) => (
                    <option key={b.id} value={b.id}>{b.branchName} ({b.branchCode})</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Product</label>
                <select
                  value={transferForm.productId}
                  onChange={(e) => setTransferForm({ ...transferForm, productId: e.target.value })}
                  disabled={!transferForm.branchId || catalogLoading}
                  required
                >
                  <option value="" disabled>
                    {catalogLoading ? 'Loading catalog…' : 'Select a product'}
                  </option>
                  {catalog.map((p) => (
                    <option key={p.id} value={p.id}>{p.name} ({p.sku}) — {p.quantity} in stock</option>
                  ))}
                </select>
                {transferForm.branchId && !catalogLoading && catalog.length === 0 && (
                  <p className="form-hint">That branch has no active products yet.</p>
                )}
              </div>
              <div className="form-group">
                <label>Quantity</label>
                <input
                  type="number"
                  min="1"
                  value={transferForm.quantity}
                  onChange={(e) => setTransferForm({ ...transferForm, quantity: e.target.value })}
                  required
                />
              </div>
              <div className="form-group">
                <label>Note (optional)</label>
                <input
                  value={transferForm.note}
                  onChange={(e) => setTransferForm({ ...transferForm, note: e.target.value })}
                  placeholder="e.g. running low ahead of the weekend"
                />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowTransferModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={submitting || !transferForm.productId}>
                  {submitting ? 'Submitting…' : 'Submit Request'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showReceiveModal && receiveTarget && (
        <div className="modal-overlay" onClick={() => setShowReceiveModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Receive Stock — {receiveTarget.name}</h2>
            <form onSubmit={handleReceiveSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Batch Number (optional)</label>
                  <input
                    value={receiveForm.batchNumber}
                    onChange={(e) => setReceiveForm({ ...receiveForm, batchNumber: e.target.value })}
                    placeholder="Auto-generated if left blank"
                  />
                </div>
                <div className="form-group">
                  <label>Quantity Received</label>
                  <input
                    type="number" min="1"
                    value={receiveForm.quantityReceived}
                    onChange={(e) => setReceiveForm({ ...receiveForm, quantityReceived: e.target.value })}
                    required
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Purchase Price (₹, optional)</label>
                  <input
                    type="number" step="0.01" min="0"
                    value={receiveForm.purchasePrice}
                    onChange={(e) => setReceiveForm({ ...receiveForm, purchasePrice: e.target.value })}
                  />
                </div>
                <div className="form-group">
                  <label>Expiry Date (optional)</label>
                  <input
                    type="date"
                    value={receiveForm.expiryDate}
                    onChange={(e) => setReceiveForm({ ...receiveForm, expiryDate: e.target.value })}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Supplier (optional)</label>
                  <select
                    value={receiveForm.supplierId}
                    onChange={(e) => setReceiveForm({ ...receiveForm, supplierId: e.target.value })}
                  >
                    <option value="">None</option>
                    {suppliers.map((s) => (
                      <option key={s.id} value={s.id}>{s.name}</option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Received Date</label>
                  <input
                    type="date"
                    value={receiveForm.receivedDate}
                    onChange={(e) => setReceiveForm({ ...receiveForm, receivedDate: e.target.value })}
                  />
                </div>
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowReceiveModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={receiving}>
                  {receiving ? 'Recording…' : 'Record Receipt'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {historyTarget && (
        <div className="modal-overlay" onClick={() => setHistoryTarget(null)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Stock History — {historyTarget.name}</h2>
            {historyLoading ? (
              <div className="empty-state">Loading…</div>
            ) : stockHistory.length === 0 ? (
              <div className="empty-state">No batches recorded yet for this product.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Batch</th>
                    <th>Received</th>
                    <th>Qty Received</th>
                    <th>Available</th>
                    <th>Expiry</th>
                    <th>Supplier</th>
                  </tr>
                </thead>
                <tbody>
                  {stockHistory.map((b) => (
                    <tr key={b.id}>
                      <td>{b.batchNumber}</td>
                      <td>{b.receivedDate}</td>
                      <td>{b.quantityReceived}</td>
                      <td>{b.availableQuantity}</td>
                      <td>{b.expiryDate || '—'}</td>
                      <td>{b.supplierName || '—'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <div className="form-actions">
              <button type="button" className="btn-secondary" onClick={() => setHistoryTarget(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Inventory;
