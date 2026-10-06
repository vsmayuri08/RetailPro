import React, { useEffect, useState } from 'react';
import { getBillingSettings, updateBillingSettings } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const BillingSettings = () => {
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  useEffect(() => {
    getBillingSettings()
      .then((s) => setForm({
        taxRatePercent: String(s.taxRatePercent),
        loyaltyPointsPerAmount: String(s.loyaltyPointsPerAmount),
        loyaltyAmountThreshold: String(s.loyaltyAmountThreshold),
      }))
      .catch((err) => setError(err?.response?.data?.message || 'Failed to load billing settings'))
      .finally(() => setLoading(false));
  }, []);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await updateBillingSettings({
        taxRatePercent: Number(form.taxRatePercent),
        loyaltyPointsPerAmount: Number(form.loyaltyPointsPerAmount),
        loyaltyAmountThreshold: Number(form.loyaltyAmountThreshold),
      });
      setSuccessMsg('Billing settings saved');
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save billing settings');
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Billing Settings</h1>
          <p className="page-subtitle">Controls the tax rate and loyalty points every checkout uses</p>
        </div>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      {loading || !form ? (
        <div className="data-card"><div className="empty-state">Loading settings…</div></div>
      ) : (
        <div className="data-card" style={{ padding: '1.5rem', maxWidth: '480px' }}>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Tax Rate (%)</label>
              <input
                type="number" step="0.01" min="0"
                name="taxRatePercent" value={form.taxRatePercent} onChange={handleChange} required
              />
              <p className="form-hint">Applied to every sale's taxable amount (subtotal minus discount).</p>
            </div>

            <div className="form-group">
              <label>Loyalty Points Earned</label>
              <input
                type="number" step="1" min="0"
                name="loyaltyPointsPerAmount" value={form.loyaltyPointsPerAmount} onChange={handleChange} required
              />
            </div>
            <div className="form-group">
              <label>Per Amount Spent (₹)</label>
              <input
                type="number" step="0.01" min="0.01"
                name="loyaltyAmountThreshold" value={form.loyaltyAmountThreshold} onChange={handleChange} required
              />
              <p className="form-hint">
                E.g. {form.loyaltyPointsPerAmount || 0} point(s) for every ₹{form.loyaltyAmountThreshold || 0} spent
                on a sale with a customer attached. Walk-in sales (no customer selected) never earn points.
              </p>
            </div>

            <div className="form-actions">
              <button type="submit" className="btn-primary" disabled={saving}>
                {saving ? 'Saving…' : 'Save Settings'}
              </button>
            </div>
          </form>
        </div>
      )}
    </div>
  );
};

export default BillingSettings;
