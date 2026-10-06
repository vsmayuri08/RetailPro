import axios from './axios';

// ---- Products (search/lookup, own branch) ----
export const searchProducts = (query) => axios.get('/cashier/products', { params: query ? { query } : {} }).then(r => r.data.data);
export const lookupProductByCode = (code) => axios.get('/cashier/products/lookup', { params: { code } }).then(r => r.data.data);

// ---- Customers (select or register) ----
export const searchCustomers = (query) => axios.get('/cashier/customers', { params: query ? { query } : {} }).then(r => r.data.data);
export const registerCustomer = (payload) => axios.post('/cashier/customers', payload).then(r => r.data.data);

// ---- Sales / Billing ----
export const checkout = (payload) => axios.post('/cashier/sales', payload).then(r => r.data.data);
export const getRecentSales = () => axios.get('/cashier/sales').then(r => r.data.data);
export const getSale = (id) => axios.get(`/cashier/sales/${id}`).then(r => r.data.data);
export const getBillingPreviewSettings = () => axios.get('/cashier/billing-settings').then(r => r.data.data);

// ---- Sales returns ----
export const processSaleReturn = (saleId, payload) =>
  axios.post(`/cashier/sales/${saleId}/returns`, payload).then(r => r.data.data);
export const getSaleReturns = (saleId) =>
  axios.get(`/cashier/sales/${saleId}/returns`).then(r => r.data.data);
export const getRecentReturns = () => axios.get('/cashier/returns').then(r => r.data.data);

export const downloadInvoicePdf = async (saleId, invoiceNumber) => {
  const response = await axios.get(`/cashier/sales/${saleId}/invoice.pdf`, { responseType: 'blob' });
  const url = window.URL.createObjectURL(response.data);
  const link = document.createElement('a');
  link.href = url;
  link.download = `${invoiceNumber || 'invoice'}.pdf`;
  link.click();
  window.URL.revokeObjectURL(url);
};

export const getCashierLeaderboard = () => axios.get('/cashier/leaderboard').then(r => r.data.data);
