import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import * as adminApi from '../api/adminApi';
import * as managerApi from '../api/managerApi';
import { getBranches as getAllBranches } from '../api/adminApi';
import '../styles/dashboard.css';
import '../styles/admin.css';
import '../styles/manager.css';

const emptyForm = {
  employeeCode: '', fullName: '', employeeType: 'CASHIER', phone: '', email: '',
  branchId: '', joiningDate: '', basicSalary: '', allowances: '0', deductions: '0',
};

const emptyPayrollForm = { periodMonth: String(new Date().getMonth() + 1), periodYear: String(new Date().getFullYear()) };

const typeBadgeClass = (type) => {
  switch (type) {
    case 'ManagerEmployee': return 'badge-gold';
    case 'InventoryStaffEmployee': return 'badge-silver';
    default: return 'badge-bronze';
  }
};

const Staff = () => {
  const { user } = useAuth();
  const isAdmin = user?.role === 'SUPER_ADMIN';
  // Super Admin sees every branch and can appoint managers; a Branch Manager is scoped to
  // their own branch and can only add Cashier / Inventory Staff (the backend enforces this too).
  const api = isAdmin ? adminApi : managerApi;
  const allowedTypes = isAdmin
    ? [['MANAGER', 'Branch Manager'], ['CASHIER', 'Cashier'], ['INVENTORY_STAFF', 'Inventory Staff']]
    : [['CASHIER', 'Cashier'], ['INVENTORY_STAFF', 'Inventory Staff']];

  const [staff, setStaff] = useState([]);
  const [branches, setBranches] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [showModal, setShowModal] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [form, setForm] = useState(emptyForm);
  const [saving, setSaving] = useState(false);

  const [payrollTarget, setPayrollTarget] = useState(null); // employee whose payroll modal is open
  const [payrollForm, setPayrollForm] = useState(emptyPayrollForm);
  const [payrollHistory, setPayrollHistory] = useState([]);
  const [payrollLoading, setPayrollLoading] = useState(false);
  const [generating, setGenerating] = useState(false);

  const loadStaff = async () => {
    setLoading(true);
    try {
      const data = await api.getStaff();
      setStaff(data);
      setError('');
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load staff');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadStaff();
    if (isAdmin) {
      getAllBranches().then(setBranches).catch(() => {});
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);

  const openCreate = () => {
    setEditingId(null);
    setForm({ ...emptyForm, employeeType: allowedTypes[0][0] });
    setShowModal(true);
  };

  const openEdit = (employee) => {
    setEditingId(employee.id);
    setForm({
      employeeCode: employee.employeeCode,
      fullName: employee.fullName,
      employeeType: employee.employeeType === 'ManagerEmployee' ? 'MANAGER'
        : employee.employeeType === 'InventoryStaffEmployee' ? 'INVENTORY_STAFF' : 'CASHIER',
      phone: employee.phone || '',
      email: employee.email || '',
      branchId: String(employee.branchId || ''),
      joiningDate: employee.joiningDate || '',
      basicSalary: String(employee.basicSalary),
      allowances: String(employee.allowances),
      deductions: String(employee.deductions),
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
        ...form,
        branchId: form.branchId ? Number(form.branchId) : undefined,
        basicSalary: Number(form.basicSalary),
        allowances: Number(form.allowances || 0),
        deductions: Number(form.deductions || 0),
      };
      if (editingId) {
        await api.updateStaff(editingId, payload);
        setSuccessMsg('Employee updated successfully');
      } else {
        await api.createStaff(payload);
        setSuccessMsg('Employee added successfully');
      }
      setShowModal(false);
      await loadStaff();
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to save employee');
    } finally {
      setSaving(false);
    }
  };

  const toggleStatus = async (employee) => {
    try {
      if (employee.active) {
        await api.deactivateStaff(employee.id);
      } else {
        await api.activateStaff(employee.id);
      }
      await loadStaff();
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to update employee status');
    }
  };

  const openPayroll = async (employee) => {
    setPayrollTarget(employee);
    setPayrollForm(emptyPayrollForm);
    setPayrollLoading(true);
    try {
      const history = await api.getStaffPayrollHistory(employee.id);
      setPayrollHistory(history);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to load payroll history');
    } finally {
      setPayrollLoading(false);
    }
  };

  const handleGeneratePayroll = async (e) => {
    e.preventDefault();
    setGenerating(true);
    setError('');
    try {
      await api.generateStaffPayroll(payrollTarget.id, {
        periodMonth: Number(payrollForm.periodMonth),
        periodYear: Number(payrollForm.periodYear),
      });
      setSuccessMsg('Payroll generated');
      const history = await api.getStaffPayrollHistory(payrollTarget.id);
      setPayrollHistory(history);
      setTimeout(() => setSuccessMsg(''), 3000);
    } catch (err) {
      setError(err?.response?.data?.message || 'Failed to generate payroll');
    } finally {
      setGenerating(false);
    }
  };

  return (
    <div className="dashboard-container">
      <div className="page-header">
        <div>
          <h1 className="page-title">Staff &amp; Payroll</h1>
          <p className="page-subtitle">
            {isAdmin ? 'Employee HR records and payroll across every branch' : 'Employee HR records and payroll for your branch'}
          </p>
        </div>
        <button className="btn-primary" onClick={openCreate}>+ New Employee</button>
      </div>

      {error && <div className="alert-banner alert-error">{error}</div>}
      {successMsg && <div className="alert-banner alert-success">{successMsg}</div>}

      <div className="data-card">
        {loading ? (
          <div className="empty-state">Loading staff…</div>
        ) : staff.length === 0 ? (
          <div className="empty-state">No employees yet. Add your first employee to get started.</div>
        ) : (
          <table className="data-table">
            <thead>
              <tr>
                <th>Code</th>
                <th>Name</th>
                <th>Type</th>
                {isAdmin && <th>Branch</th>}
                <th>Basic Salary</th>
                <th>Net Pay</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {staff.map((s) => (
                <tr key={s.id}>
                  <td>{s.employeeCode}</td>
                  <td>{s.fullName}</td>
                  <td><span className={`badge ${typeBadgeClass(s.employeeType)}`}>{s.employeeTypeLabel}</span></td>
                  {isAdmin && <td>{s.branchName}</td>}
                  <td>₹{Number(s.basicSalary).toFixed(2)}</td>
                  <td>₹{Number(s.netPay).toFixed(2)}</td>
                  <td>
                    <span className={`badge ${s.active ? 'badge-active' : 'badge-inactive'}`}>
                      {s.active ? 'Active' : 'Inactive'}
                    </span>
                  </td>
                  <td style={{ display: 'flex', gap: '0.5rem' }}>
                    <button className="btn-secondary btn-sm" onClick={() => openEdit(s)}>Edit</button>
                    <button className="btn-secondary btn-sm" onClick={() => openPayroll(s)}>Payroll</button>
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
            <h2 className="modal-title">{editingId ? 'Edit Employee' : 'New Employee'}</h2>
            <form onSubmit={handleSubmit}>
              <div className="form-row">
                <div className="form-group">
                  <label>Employee Code</label>
                  <input name="employeeCode" value={form.employeeCode} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Full Name</label>
                  <input name="fullName" value={form.fullName} onChange={handleChange} required />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Role</label>
                  <select name="employeeType" value={form.employeeType} onChange={handleChange} disabled={!!editingId}>
                    {allowedTypes.map(([value, label]) => (
                      <option key={value} value={value}>{label}</option>
                    ))}
                  </select>
                  {editingId && <p className="form-hint">Role can't be changed after creation.</p>}
                </div>
                {isAdmin && (
                  <div className="form-group">
                    <label>Branch</label>
                    <select name="branchId" value={form.branchId} onChange={handleChange} required>
                      <option value="" disabled>Select a branch</option>
                      {branches.map((b) => (
                        <option key={b.id} value={b.id}>{b.branchName}</option>
                      ))}
                    </select>
                  </div>
                )}
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Phone</label>
                  <input name="phone" value={form.phone} onChange={handleChange} />
                </div>
                <div className="form-group">
                  <label>Email</label>
                  <input type="email" name="email" value={form.email} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label>Joining Date</label>
                <input type="date" name="joiningDate" value={form.joiningDate} onChange={handleChange} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Basic Salary (₹)</label>
                  <input type="number" step="0.01" min="0" name="basicSalary" value={form.basicSalary} onChange={handleChange} required />
                </div>
                <div className="form-group">
                  <label>Allowances (₹)</label>
                  <input type="number" step="0.01" min="0" name="allowances" value={form.allowances} onChange={handleChange} />
                </div>
              </div>
              <div className="form-group">
                <label>Deductions (₹)</label>
                <input type="number" step="0.01" min="0" name="deductions" value={form.deductions} onChange={handleChange} />
              </div>
              <div className="form-actions">
                <button type="button" className="btn-secondary" onClick={() => setShowModal(false)}>Cancel</button>
                <button type="submit" className="btn-primary" disabled={saving}>
                  {saving ? 'Saving…' : editingId ? 'Save Changes' : 'Add Employee'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {payrollTarget && (
        <div className="modal-overlay" onClick={() => setPayrollTarget(null)}>
          <div className="modal-box" onClick={(e) => e.stopPropagation()}>
            <h2 className="modal-title">Payroll — {payrollTarget.fullName}</h2>
            <form onSubmit={handleGeneratePayroll} className="form-row" style={{ alignItems: 'flex-end' }}>
              <div className="form-group">
                <label>Month</label>
                <select
                  value={payrollForm.periodMonth}
                  onChange={(e) => setPayrollForm({ ...payrollForm, periodMonth: e.target.value })}
                >
                  {Array.from({ length: 12 }, (_, i) => i + 1).map((m) => (
                    <option key={m} value={m}>{m}</option>
                  ))}
                </select>
              </div>
              <div className="form-group">
                <label>Year</label>
                <input
                  type="number"
                  value={payrollForm.periodYear}
                  onChange={(e) => setPayrollForm({ ...payrollForm, periodYear: e.target.value })}
                />
              </div>
              <div className="form-group">
                <button type="submit" className="btn-primary" disabled={generating}>
                  {generating ? 'Generating…' : 'Generate Payroll'}
                </button>
              </div>
            </form>

            <h3 style={{ marginTop: '1.5rem', marginBottom: '0.75rem' }}>Payroll History</h3>
            {payrollLoading ? (
              <div className="empty-state">Loading…</div>
            ) : payrollHistory.length === 0 ? (
              <div className="empty-state">No payroll generated yet for this employee.</div>
            ) : (
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Period</th>
                    <th>Basic</th>
                    <th>Allowances</th>
                    <th>Bonus</th>
                    <th>Deductions</th>
                    <th>Net Pay</th>
                  </tr>
                </thead>
                <tbody>
                  {payrollHistory.map((p) => (
                    <tr key={p.id}>
                      <td>{p.periodMonth}/{p.periodYear}</td>
                      <td>₹{Number(p.basicSalary).toFixed(2)}</td>
                      <td>₹{Number(p.allowances).toFixed(2)}</td>
                      <td>₹{Number(p.bonus).toFixed(2)}</td>
                      <td>₹{Number(p.deductions).toFixed(2)}</td>
                      <td><strong>₹{Number(p.netPay).toFixed(2)}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            )}

            <div className="form-actions">
              <button type="button" className="btn-secondary" onClick={() => setPayrollTarget(null)}>Close</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Staff;
