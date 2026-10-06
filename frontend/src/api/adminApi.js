import axios from './axios';

// ---- Branches ----
export const getBranches = () => axios.get('/admin/branches').then(r => r.data.data);
export const getOrgStats = () => axios.get('/admin/branches/stats').then(r => r.data.data);
export const createBranch = (payload) => axios.post('/admin/branches', payload).then(r => r.data.data);
export const updateBranch = (id, payload) => axios.put(`/admin/branches/${id}`, payload).then(r => r.data.data);
export const activateBranch = (id) => axios.patch(`/admin/branches/${id}/activate`).then(r => r.data.data);
export const deactivateBranch = (id) => axios.patch(`/admin/branches/${id}/deactivate`).then(r => r.data.data);

// ---- Users ----
export const getUsers = (params) => axios.get('/admin/users', { params }).then(r => r.data.data);
export const createBranchManager = (payload) => axios.post('/admin/users/managers', payload).then(r => r.data.data);
export const activateUser = (id) => axios.patch(`/admin/users/${id}/activate`).then(r => r.data.data);
export const deactivateUser = (id) => axios.patch(`/admin/users/${id}/deactivate`).then(r => r.data.data);

// ---- Categories ----
export const getCategories = () => axios.get('/admin/categories').then(r => r.data.data);
export const createCategory = (payload) => axios.post('/admin/categories', payload).then(r => r.data.data);
export const updateCategory = (id, payload) => axios.put(`/admin/categories/${id}`, payload).then(r => r.data.data);
export const activateCategory = (id) => axios.patch(`/admin/categories/${id}/activate`).then(r => r.data.data);
export const deactivateCategory = (id) => axios.patch(`/admin/categories/${id}/deactivate`).then(r => r.data.data);

// ---- Suppliers ----
export const getSuppliers = (query) => axios.get('/admin/suppliers', { params: query ? { query } : {} }).then(r => r.data.data);
export const createSupplier = (payload) => axios.post('/admin/suppliers', payload).then(r => r.data.data);
export const updateSupplier = (id, payload) => axios.put(`/admin/suppliers/${id}`, payload).then(r => r.data.data);
export const activateSupplier = (id) => axios.patch(`/admin/suppliers/${id}/activate`).then(r => r.data.data);
export const deactivateSupplier = (id) => axios.patch(`/admin/suppliers/${id}/deactivate`).then(r => r.data.data);

// ---- Staff (Employee HR records) — org-wide, any branch ----
export const getStaff = () => axios.get('/admin/staff').then(r => r.data.data);
export const createStaff = (payload) => axios.post('/admin/staff', payload).then(r => r.data.data);
export const updateStaff = (id, payload) => axios.put(`/admin/staff/${id}`, payload).then(r => r.data.data);
export const activateStaff = (id) => axios.patch(`/admin/staff/${id}/activate`).then(r => r.data.data);
export const deactivateStaff = (id) => axios.patch(`/admin/staff/${id}/deactivate`).then(r => r.data.data);
export const generateStaffPayroll = (id, payload) => axios.post(`/admin/staff/${id}/payroll`, payload).then(r => r.data.data);
export const getStaffPayrollHistory = (id) => axios.get(`/admin/staff/${id}/payroll`).then(r => r.data.data);

// ---- Billing Settings — tax rate + loyalty points earn rate (both admin-configurable) ----
export const getBillingSettings = () => axios.get('/admin/billing-settings').then(r => r.data.data);
export const updateBillingSettings = (payload) => axios.put('/admin/billing-settings', payload).then(r => r.data.data);

// ---- Expiry Discount Rules (Module 10) — fully admin-managed, not hard-coded ----
export const getExpiryDiscountRules = () => axios.get('/admin/expiry-discount-rules').then(r => r.data.data);
export const createExpiryDiscountRule = (payload) => axios.post('/admin/expiry-discount-rules', payload).then(r => r.data.data);
export const updateExpiryDiscountRule = (id, payload) => axios.put(`/admin/expiry-discount-rules/${id}`, payload).then(r => r.data.data);
export const activateExpiryDiscountRule = (id) => axios.patch(`/admin/expiry-discount-rules/${id}/activate`).then(r => r.data.data);
export const deactivateExpiryDiscountRule = (id) => axios.patch(`/admin/expiry-discount-rules/${id}/deactivate`).then(r => r.data.data);
export const deleteExpiryDiscountRule = (id) => axios.delete(`/admin/expiry-discount-rules/${id}`).then(r => r.data.data);

export const getActivityLogs = () => axios.get('/admin/activity-logs').then(r => r.data.data);
export const getOrgCashierLeaderboard = () => axios.get('/admin/reports/cashier-leaderboard').then(r => r.data.data);
