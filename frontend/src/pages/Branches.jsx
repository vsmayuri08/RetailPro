import React, { useEffect, useState } from 'react';
import { getBranches, createBranch, updateBranch, activateBranch, deactivateBranch } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { branchName: '', branchCode: '', address: '', city: '', phone: '' };

const Branches = () => {
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadBranches = async () => {
    setLoading(true);
    try {
      const data = await getBranches();
      setBranches(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load branches');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadBranches(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (branch) => {
    setEditingId(branch.id);
    setForm({
      branchName: branch.branchName,
      branchCode: branch.branchCode,
      address: branch.address || '',
      city: branch.city || '',
      phone: branch.phone || '',
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
        await updateBranch(editingId, form);
        setSuccessMsg('Branch updated successfully');
      } else {
        await createBranch(form);
        setSuccessMsg('Branch created successfully');
      }
      setShowModal(false);
      await loadBranches();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save branch');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (branch) => {
    try {
      if (branch.status === 'ACTIVE') {
        await deactivateBranch(branch.id);
      } else {
        await activateBranch(branch.id);
      }
      await loadBranches();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update branch status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Branches</h1>
          <p className="page-subtitle">Create, edit, activate and deactivate retail branches</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Branch</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading branches…</div>
        ) : branches.length === 0 ? (
          <div className="empty-state">No branches yet. Create your first branch to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Branch</th>
                <th>Code</th>
                <th>City</th>
                <th>Manager(s)</th>
                <th>Cashiers</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {branches.map((b) => (
                <tr key={b.id}>
                  <td>{b.branchName}</td>
                  <td>{b.branchCode}</td>
                  <td>{b.city || '—'}</td>
                  <td>{b.managerCount}</td>
                  <td>{b.cashierCount}</td>
                  <td>
                    <span className={`badge ${b.status === 'ACTIVE' ? 'badge-active' : 'badge-inactive'}`}>
                      {b.status}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(b)}>Edit</button>
                    <button
                      className={`btn-sm ${b.status === 'ACTIVE' ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(b)}
                    >
                      {b.status === 'ACTIVE' ? 'Deactivate' : 'Activate'}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>

      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">{editingId ? 'Edit Branch' : 'New Branch'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Branch Name</label>
                <input name="branchName" value={form.branchName} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Branch Code</label>
                <input name="branchCode" value={form.branchCode} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>City</label>
                <input name="city" value={form.city} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Address</label>
                <input name="address" value={form.address} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Phone</label>
                <input name="phone" value={form.phone} onChange={handleChange} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Create Branch'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Branches;
