import React, { useEffect, useState } from 'react';
import { getEmployees, createEmployee, activateEmployee, deactivateEmployee } from '../api/managerApi';
import '../styles/dashboard.css';
import '../styles/admin.css';

const emptyForm = { fullName: '', email: '', password: '' };

const Employees = () => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [showModal, setShowModal] = useState(false);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const loadEmployees = async () => {
    setLoading(true);
    try {
      const data = await getEmployees();
      setEmployees(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load employees');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => { loadEmployees(); }, []);

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
      await createEmployee(form);
      setSuccessMsg('Employee created successfully');
      setShowModal(false);
      await loadEmployees();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to create employee');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (employee) => {
    try {
      if (employee.active) {
        await deactivateEmployee(employee.id);
      } else {
        await activateEmployee(employee.id);
      }
      await loadEmployees();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update employee status');
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Employees</h1>
          <p className="page-subtitle">Add and manage cashiers at your branch</p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Employee</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading employees…</div>
        ) : employees.length === 0 ? (
          <div className="empty-state">No employees yet. Add your first cashier to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {employees.map((e) => (
                <tr key={e.id}>
                  <td>{e.fullName}</td>
                  <td>{e.email}</td>
                  <td><span className="badge badge-role">{e.role.replace('_', ' ')}</span></td>
                  <td>
                    <span className={`badge ${e.active ? 'badge-active' : 'badge-inactive'}`}>
                      {e.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td>
                    <button
                      className={`btn-sm ${e.active ? 'btn-danger' : 'btn-success'}`}
                      onClick={() => toggleStatus(e)}
                    >
                      {e.active ? 'Deactivate' : 'Activate'}
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
            <h2 className="modal-title">New Employee</h2>
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
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Creating…' : 'Create Employee'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Employees;
