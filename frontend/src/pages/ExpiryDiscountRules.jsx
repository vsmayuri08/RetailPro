import React, { useEffect, useState } from 'react';
import {
  getExpiryDiscountRules, createExpiryDiscountRule, updateExpiryDiscountRule,
  activateExpiryDiscountRule, deactivateExpiryDiscountRule, deleteExpiryDiscountRule,
} from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { label: '', minDaysBeforeExpiry: '', maxDaysBeforeExpiry: '', discountPercent: '' };

const ExpiryDiscountRules = () => {
  const [rules, setRules] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadRules = async () => {
    setLoading(true);
    try {
      const data = await getExpiryDiscountRules();
      setRules(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load discount rules');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadRules(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (rule) => {
    setEditingId(rule.id);
    setForm({
      label: rule.label || '',
      minDaysBeforeExpiry: String(rule.minDaysBeforeExpiry),
      maxDaysBeforeExpiry: String(rule.maxDaysBeforeExpiry),
      discountPercent: String(rule.discountPercent),
    });
    setShowModal(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      const payload = {
        label: form.label,
        minDaysBeforeExpiry: Number(form.minDaysBeforeExpiry),
        maxDaysBeforeExpiry: Number(form.maxDaysBeforeExpiry),
        discountPercent: Number(form.discountPercent),
      };
      if (editingId) {
        await updateExpiryDiscountRule(editingId, payload);
        setSuccessMsg('Discount rule updated');
      } else {
        await createExpiryDiscountRule(payload);
        setSuccessMsg('Discount rule created');
      }
      setShowModal(false);
      await loadRules();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save discount rule');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (rule) => {
    try {
      if (rule.active) {
        await deactivateExpiryDiscountRule(rule.id);
      } else {
        await activateExpiryDiscountRule(rule.id);
      }
      await loadRules();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update rule status');
    }
  };

  const handleDelete = async (rule) => {
    if (!window.confirm(`Delete the "${rule.label || 'this'}" discount rule?`)) return;
    try {
      await deleteExpiryDiscountRule(rule.id);
      await loadRules();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to delete discount rule');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Expiry Discount Rules</h1>
          <p className="page-subtitle">Automatic discounts applied at checkout as products approach their expiry date</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Rule</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading rules…</div>
        ) : rules.length === 0 ? (
          <div className="empty-state">No discount rules yet. Add one to start applying automatic expiry discounts.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Label</th>
                <th>Days Before Expiry</th>
                <th>Discount</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {rules.map((r) => (
                <tr key={r.id}>
                  <td>{r.label || '—'}</td>
                  <td>{r.minDaysBeforeExpiry}–{r.maxDaysBeforeExpiry} days</td>
                  <td>{Number(r.discountPercent).toFixed(2)}%</td>
                  <td>
                    <span className={`badge ${r.active ? 'badge-active' : 'badge-inactive'}`}>
                      {r.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(r)}>Edit</button>
                    <button
                      className={`btn-sm ${r.active ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(r)}
                    >
                      {r.active ? 'Deactivate' : 'Activate'}
                    </button>
                    <button className="btn-danger btn-sm" onClick={() => handleDelete(r)}>Delete</button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      <p className="form-hint" style={{ marginTop: '1rem' }}>
        Products more than every rule's range from expiring get no discount. Already-expired
        products are always blocked from sale entirely — that's not a discount, it's a hard stop.
      </p>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">{editingId ? 'Edit Discount Rule' : 'New Discount Rule'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Label</label>
                <input name="label" value={form.label} onChange={handleChange} placeholder="e.g. Near expiry" />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Min Days Before Expiry</label>
                  <input type="number" min="0" name="minDaysBeforeExpiry" value={form.minDaysBeforeExpiry} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Max Days Before Expiry</label>
                  <input type="number" min="0" name="maxDaysBeforeExpiry" value={form.maxDaysBeforeExpiry} onChange={handleChange} required />
                </div>
              </div>
              <div className="form-group">
                <label>Discount (%)</label>
                <input type="number" step="0.01" min="0" max="100" name="discountPercent" value={form.discountPercent} onChange={handleChange} required />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Create Rule'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default ExpiryDiscountRules;
