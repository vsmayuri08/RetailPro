import React, { useEffect, useState } from 'react';
import { getCustomers, registerCustomer, updateCustomer, adjustLoyaltyPoints, getCustomerPurchases } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const emptyForm = { fullName: '', phone: '', email: '', address: '' };

const tierBadgeClass = (tier) => {
  switch (tier) {
    case 'SILVER': return 'badge-silver';
    case 'GOLD': return 'badge-gold';
    case 'PLATINUM': return 'badge-platinum';
    default: return 'badge-bronze';
  }
};

const Customers = () => {
  const [customers, setCustomers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);
  const [historyTarget, setHistoryTarget] = useState(null);
  const [purchaseHistory, setPurchaseHistory] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  const loadCustomers = async (query) => {
    setLoading(true);
    try {
      const data = await getCustomers(query);
      setCustomers(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load customers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadCustomers(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadCustomers(searchQuery);
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (customer) => {
    setEditingId(customer.id);
    setForm({
      fullName: customer.fullName,
      phone: customer.phone,
      email: customer.email || '',
      address: customer.address || '',
    });
    setShowModal(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateCustomer(editingId, form);
        setSuccessMsg('Customer updated successfully');
      } else {
        await registerCustomer(form);
        setSuccessMsg('Customer registered successfully');
      }
      setShowModal(false);
      await loadCustomers(searchQuery);
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save customer');
    } finally {
      setSaving(false);
    }
  };

  const handlePointsStep = async (customer, delta) => {
    if (customer.loyaltyPoints + delta < 0) return;
    try {
      await adjustLoyaltyPoints(customer.id, delta, delta > 0 ? 'Manual credit' : 'Manual deduction');
      await loadCustomers(searchQuery);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to adjust loyalty points');
    }
  };

  const openHistory = async (customer) => {
    setHistoryTarget(customer);
    setHistoryLoading(true);
    try {
      const purchases = await getCustomerPurchases(customer.id);
      setPurchaseHistory(purchases);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load purchase history');
    } finally {
      setHistoryLoading(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Customers</h1>
          <p className="page-subtitle">Register customers and manage loyalty points</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Customer</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <form className="search-bar" onSubmit={handleSearch} style={{ marginBottom: '1rem' }}>
        <input
          placeholder="Search by name or phone number…"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button type="submit" className="btn-secondary">Search</button>
      </form>

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading customers…</div>
        ) : customers.length === 0 ? (
          <div className="empty-state">No customers found. Register your first customer to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Phone</th>
                <th>Email</th>
                <th>Loyalty Points</th>
                <th>Tier</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {customers.map((c) => (
                <tr key={c.id}>
                  <td>{c.fullName}</td>
                  <td>{c.phone}</td>
                  <td>{c.email || '—'}</td>
                  <td>
                    <div className="points-stepper">
                      <button type="button" onClick={() => handlePointsStep(c, -50)} disabled={c.loyaltyPoints === 0}>−</button>
                      <span className="stock-qty">{c.loyaltyPoints}</span>
                      <button type="button" onClick={() => handlePointsStep(c, 50)}>+</button>
                    </div>
                  </td>
                  <td><span className={`badge ${tierBadgeClass(c.loyaltyTier)}`}>{c.loyaltyTier}</span></td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(c)}>Edit</button>
                    <button className="btn-secondary btn-sm" onClick={() => openHistory(c)}>History</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {historyTarget && (
        <div className="modal-overlay" onClick={() => setHistoryTarget(null)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Purchase History — {historyTarget.fullName}</h2>
            {historyLoading ? (
              <div className="empty-state">Loading…</div>
            ) : purchaseHistory.length === 0 ? (
              <div className="empty-state">No purchases yet for this customer.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Invoice</th>
                    <th>Date</th>
                    <th>Items</th>
                    <th>Total</th>
                    <th>Points Earned</th>
                  </tr>
                </thead>
                <tbody>
                  {purchaseHistory.map((s) => (
                    <tr key={s.id}>
                      <td>{s.invoiceNumber}</td>
                      <td>{new Date(s.createdAt).toLocaleDateString()}</td>
                      <td>{s.items.length}</td>
                      <td>₹{Number(s.totalAmount).toFixed(2)}</td>
                      <td>+{s.loyaltyPointsEarned}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}
            <div className="form-actions">
              <button className="btn-secondary" onClick={() => setHistoryTarget(null)}>Close</button>
            </div>
          </div>
        </div>
      )}

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">{editingId ? 'Edit Customer' : 'New Customer'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input name="fullName" value={form.fullName} onChange={handleChange} required />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Phone Number</label>
                  <input name="phone" value={form.phone} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Email (optional)</label>
                  <input type="email" name="email" value={form.email} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label>Address (optional)</label>
                <input name="address" value={form.address} onChange={handleChange} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Register Customer'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Customers;
