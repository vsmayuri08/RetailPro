import React, { useEffect, useState } from 'react';
import { getUsers, getBranches, createBranchManager, activateUser, deactivateUser } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { fullName: '', email: '', password: '', branchId: '' };

const Users = () => {
  const [users, setUsers] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadData = async () => {
    setLoading(true);
    try {
      const [usersData, branchesData] = await Promise.all([getUsers(), getBranches()]);
      setUsers(usersData);
      setBranches(branchesData.filter((b) => b.status === 'ACTIVE'));
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load users');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadData(); }, []);

  const openCreate = () => {
    setForm(emptyForm);
    setShowModal(true);
  };

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError('');
    try {
      await createBranchManager({ ...form, branchId: Number(form.branchId) });
      setSuccessMsg('Branch manager created successfully');
      setShowModal(false);
      await loadData();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create branch manager');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (user) => {
    try {
      if (user.active) {
        await deactivateUser(user.id);
      } else {
        await activateUser(user.id);
      }
      await loadData();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update user status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Users</h1>
          <p className="page-subtitle">Create branch managers and manage every account in the organization</p>
        </div>
        <button className="btn-primary" onClick={openCreate} disabled={branches.length === 0}>
          + New Branch Manager
        </button>
      </div>

      {branches.length === 0 && !loading && (
        <div className="alert-banner alert-error">Create an active branch first before adding a branch manager.</div>
      )}
      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading users…</div>
        ) : users.length === 0 ? (
          <div className="empty-state">No users yet.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Branch</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {users.map((u) => (
                <tr key={u.id}>
                  <td>{u.fullName}</td>
                  <td>{u.email}</td>
                  <td><span className="badge badge-role">{u.role.replace('_', ' ')}</span></td>
                  <td>{u.branchName || '—'}</td>
                  <td>
                    <span className={`badge ${u.active ? 'badge-active' : 'badge-inactive'}`}>
                      {u.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    {u.role !== 'SUPER_ADMIN' && (
                      <button
                        className={`btn-sm ${u.active ? 'btn-danger' : 'btn-success'}`}
                        onClick={() => toggleStatus(u)}
                      >
                        {u.active ? 'Deactivate' : 'Activate'}
                      </button>
                    )}
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
            <h2 className="modal-title">New Branch Manager</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Full Name</label>
                <input name="fullName" value={form.fullName} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Email</label>
                <input type="email" name="email" value={form.email} onChange={handleChange} required />
              </div>
              <div className="form-group">
                <label>Temporary Password</label>
                <input type="password" name="password" value={form.password} onChange={handleChange} minLength={6} required />
              </div>
              <div className="form-group">
                <label>Branch</label>
                <select name="branchId" value={form.branchId} onChange={handleChange} required>
                  <option value="" disabled>Select a branch</option>
                  {branches.map((b) => (
                    <option key={b.id} value={b.id}>{b.branchName} ({b.branchCode})</option>
                  ))}
                </select>
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Creating…' : 'Create Manager'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Users;
