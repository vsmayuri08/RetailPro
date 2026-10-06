import React, { useEffect, useState } from 'react';
import { getSuppliers, createSupplier, updateSupplier, activateSupplier, deactivateSupplier } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { name: '', contactPerson: '', phone: '', email: '', address: '', gstNumber: '' };

const Suppliers = () => {
  const [suppliers, setSuppliers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadSuppliers = async (query) => {
    setLoading(true);
    try {
      const data = await getSuppliers(query);
      setSuppliers(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load suppliers');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadSuppliers(); }, []);

  const handleSearch = (e) => {
    e.preventDefault();
    loadSuppliers(searchQuery);
  };

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (supplier) => {
    setEditingId(supplier.id);
    setForm({
      name: supplier.name,
      contactPerson: supplier.contactPerson || '',
      phone: supplier.phone || '',
      email: supplier.email || '',
      address: supplier.address || '',
      gstNumber: supplier.gstNumber || '',
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
        await updateSupplier(editingId, form);
        setSuccessMsg('Supplier updated successfully');
      } else {
        await createSupplier(form);
        setSuccessMsg('Supplier created successfully');
      }
      setShowModal(false);
      await loadSuppliers(searchQuery);
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save supplier');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (supplier) => {
    try {
      if (supplier.active) {
        await deactivateSupplier(supplier.id);
      } else {
        await activateSupplier(supplier.id);
      }
      await loadSuppliers(searchQuery);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update supplier status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Suppliers</h1>
          <p className="page-subtitle">Manage suppliers used across the organization</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Supplier</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <form className="search-bar" onSubmit={handleSearch} style={{ marginBottom: '1rem' }}>
        <input
          placeholder="Search suppliers by name…"
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
        />
        <button type="submit" className="btn-secondary">Search</button>
      </form>

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading suppliers…</div>
        ) : suppliers.length === 0 ? (
          <div className="empty-state">No suppliers found. Add your first supplier to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Contact Person</th>
                <th>Phone</th>
                <th>Email</th>
                <th>GST No.</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {suppliers.map((s) => (
                <tr key={s.id}>
                  <td>{s.name}</td>
                  <td>{s.contactPerson || '—'}</td>
                  <td>{s.phone || '—'}</td>
                  <td>{s.email || '—'}</td>
                  <td>{s.gstNumber || '—'}</td>
                  <td>
                    <span className={`badge ${s.active ? 'badge-active' : 'badge-inactive'}`}>
                      {s.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(s)}>Edit</button>
                    <button
                      className={`btn-sm ${s.active ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(s)}
                    >
                      {s.active ? 'Deactivate' : 'Activate'}
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
            <h2 className="modal-title">{editingId ? 'Edit Supplier' : 'New Supplier'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Supplier Name</label>
                <input name="name" value={form.name} onChange={handleChange} required />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Contact Person</label>
                  <input name="contactPerson" value={form.contactPerson} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Phone</label>
                  <input name="phone" value={form.phone} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" name="email" value={form.email} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Address</label>
                <input name="address" value={form.address} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>GST Number (optional)</label>
                <input name="gstNumber" value={form.gstNumber} onChange={handleChange} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Create Supplier'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Suppliers;
