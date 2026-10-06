import axios from './axios';

// ---- Dashboard ----
export const getManagerStats = () => axios.get('/manager/dashboard/stats').then(r => r.data.data);

// ---- Products & Inventory ----
export const getProducts = () => axios.get('/manager/products').then(r => r.data.data);
export const getProductAlerts = () => axios.get('/manager/products/alerts').then(r => r.data.data);
export const createProduct = (payload) => axios.post('/manager/products', payload).then(r => r.data.data);
export const updateProduct = (id, payload) => axios.put(`/manager/products/${id}`, payload).then(r => r.data.data);
export const activateProduct = (id) => axios.patch(`/manager/products/${id}/activate`).then(r => r.data.data);
export const deactivateProduct = (id) => axios.patch(`/manager/products/${id}/deactivate`).then(r => r.data.data);
export const adjustStock = (id, payload) => axios.patch(`/manager/products/${id}/stock`, payload).then(r => r.data.data);
export const browseBranchCatalog = (branchId, query) =>
  axios.get(`/manager/products/branch/${branchId}`, { params: query ? { query } : {} }).then(r => r.data.data);

// ---- Employees ----
export const getEmployees = () => axios.get('/manager/employees').then(r => r.data.data);
export const createEmployee = (payload) => axios.post('/manager/employees', payload).then(r => r.data.data);
export const activateEmployee = (id) => axios.patch(`/manager/employees/${id}/activate`).then(r => r.data.data);
export const deactivateEmployee = (id) => axios.patch(`/manager/employees/${id}/deactivate`).then(r => r.data.data);

// ---- Other branches (for stock transfer requests) ----
export const getOtherBranches = () => axios.get('/manager/branches').then(r => r.data.data);

// ---- Stock Transfers ----
export const getIncomingTransfers = () => axios.get('/manager/stock-transfers/incoming').then(r => r.data.data);
export const getOutgoingTransfers = () => axios.get('/manager/stock-transfers/outgoing').then(r => r.data.data);
export const requestStockTransfer = (payload) => axios.post('/manager/stock-transfers', payload).then(r => r.data.data);
export const approveTransfer = (id, decisionNote) =>
  axios.patch(`/manager/stock-transfers/${id}/approve`, decisionNote ? { decisionNote } : {}).then(r => r.data.data);
export const rejectTransfer = (id, decisionNote) =>
  axios.patch(`/manager/stock-transfers/${id}/reject`, decisionNote ? { decisionNote } : {}).then(r => r.data.data);

// ---- Customers ----
export const getCustomers = (query) => axios.get('/manager/customers', { params: query ? { query } : {} }).then(r => r.data.data);
export const registerCustomer = (payload) => axios.post('/manager/customers', payload).then(r => r.data.data);
export const updateCustomer = (id, payload) => axios.put(`/manager/customers/${id}`, payload).then(r => r.data.data);
export const adjustLoyaltyPoints = (id, delta, reason) =>
  axios.patch(`/manager/customers/${id}/loyalty-points`, { delta, reason }).then(r => r.data.data);

// ---- Staff (Employee HR records) — own branch only, Cashier/Inventory Staff ----
export const getStaff = () => axios.get('/manager/staff').then(r => r.data.data);
export const createStaff = (payload) => axios.post('/manager/staff', payload).then(r => r.data.data);
export const updateStaff = (id, payload) => axios.put(`/manager/staff/${id}`, payload).then(r => r.data.data);
export const activateStaff = (id) => axios.patch(`/manager/staff/${id}/activate`).then(r => r.data.data);
export const deactivateStaff = (id) => axios.patch(`/manager/staff/${id}/deactivate`).then(r => r.data.data);
export const generateStaffPayroll = (id, payload) => axios.post(`/manager/staff/${id}/payroll`, payload).then(r => r.data.data);
export const getStaffPayrollHistory = (id) => axios.get(`/manager/staff/${id}/payroll`).then(r => r.data.data);

// ---- Customer purchase history (now backed by real Sale data) ----
export const getCustomerPurchases = (id) => axios.get(`/manager/customers/${id}/purchases`).then(r => r.data.data);

// ---- Inventory Batches (Module 5) — receiving stock and stock history ----
export const receiveStock = (productId, payload) => axios.post(`/manager/products/${productId}/receive-stock`, payload).then(r => r.data.data);
export const getStockHistory = (productId) => axios.get(`/manager/products/${productId}/stock-history`).then(r => r.data.data);
export const getManagerSuppliers = () => axios.get('/manager/suppliers').then(r => r.data.data);

// ---- Categories (read-only, for the product form's category picker) ----
export const getManagerCategories = () => axios.get('/manager/categories').then(r => r.data.data);

// ---- ABC Analysis (novel addition — ranks products by revenue contribution) ----
export const getAbcAnalysis = (startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  return axios.get('/manager/reports/abc-analysis', { params }).then(r => r.data.data);
};

// ---- Demand Forecast (novel addition — moving-average demand vs current stock) ----
export const getDemandForecast = (startDate, endDate, forecastDays) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  if (forecastDays) params.forecastDays = forecastDays;
  return axios.get('/manager/reports/demand-forecast', { params }).then(r => r.data.data);
};

// ---- Market Basket (novel addition — products frequently bought together) ----
export const getMarketBasket = (startDate, endDate, minSupport) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  if (minSupport) params.minSupport = minSupport;
  return axios.get('/manager/reports/market-basket', { params }).then(r => r.data.data);
};

// ---- RFM Segmentation (novel addition — recency, frequency, monetary buckets) ----
export const getRfmSegmentation = (startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  return axios.get('/manager/reports/rfm-segmentation', { params }).then(r => r.data.data);
};

// ---- Reorder Suggestions (novel addition — velocity qty + supplier from batches) ----
export const getReorderSuggestions = (startDate, endDate) => {
  const params = {};
  if (startDate) params.startDate = startDate;
  if (endDate) params.endDate = endDate;
  return axios.get('/manager/reports/reorder-suggestions', { params }).then(r => r.data.data);
};

export const getCashierLeaderboard = () =>
  axios.get('/manager/reports/cashier-leaderboard').then(r => r.data.data);

export const getActivityLogs = () => axios.get('/manager/activity-logs').then(r => r.data.data);

export const getSalesTrend = () => axios.get('/manager/reports/sales-trend').then(r => r.data.data);

export const createPurchaseOrdersFromReorder = () =>
  axios.post('/manager/purchase-orders/from-reorder').then(r => r.data);
export const getPurchaseOrders = () => axios.get('/manager/purchase-orders').then(r => r.data.data);
export const getPurchaseOrder = (id) => axios.get(`/manager/purchase-orders/${id}`).then(r => r.data.data);
export const submitPurchaseOrder = (id) => axios.patch(`/manager/purchase-orders/${id}/submit`).then(r => r.data.data);
export const approvePurchaseOrder = (id) => axios.patch(`/manager/purchase-orders/${id}/approve`).then(r => r.data.data);
export const cancelPurchaseOrder = (id) => axios.patch(`/manager/purchase-orders/${id}/cancel`).then(r => r.data.data);
export const receivePurchaseOrder = (id) => axios.patch(`/manager/purchase-orders/${id}/receive`).then(r => r.data.data);
