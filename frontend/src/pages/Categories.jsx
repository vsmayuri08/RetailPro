import React, { useEffect, useState } from 'react';
import { getCategories, createCategory, updateCategory, activateCategory, deactivateCategory } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { name: '', description: '' };

const Categories = () => {
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadCategories = async () => {
    setLoading(true);
    try {
      const data = await getCategories();
      setCategories(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load categories');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadCategories(); }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm(emptyForm);
    setShowModal(true);
  };

  const openEdit = (category) => {
    setEditingId(category.id);
    setForm({ name: category.name, description: category.description || '' });
    setShowModal(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      if (editingId) {
        await updateCategory(editingId, form);
        setSuccessMsg('Category updated successfully');
      } else {
        await createCategory(form);
        setSuccessMsg('Category created successfully');
      }
      setShowModal(false);
      await loadCategories();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save category');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (category) => {
    try {
      if (category.active) {
        await deactivateCategory(category.id);
      } else {
        await activateCategory(category.id);
      }
      await loadCategories();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update category status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Categories</h1>
          <p className="page-subtitle">Manage the product categories used across every branch</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Category</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading categories…</div>
        ) : categories.length === 0 ? (
          <div className="empty-state">No categories yet. Add your first category to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Description</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {categories.map((c) => (
                <tr key={c.id}>
                  <td>{c.name}</td>
                  <td>{c.description || '—'}</td>
                  <td>
                    <span className={`badge ${c.active ? 'badge-active' : 'badge-inactive'}`}>
                      {c.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(c)}>Edit</button>
                    <button
                      className={`btn-sm ${c.active ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(c)}
                    >
                      {c.active ? 'Deactivate' : 'Activate'}
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
            <h2 className="modal-title">{editingId ? 'Edit Category' : 'New Category'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Category Name</label>
                <input name="name" value={form.name} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Description</label>
                <input name="description" value={form.description} onChange={handleChange} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Create Category'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Categories;
