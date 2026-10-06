import axios from './axios';

export const askRetailProAi = (question) =>
  axios.post('/ai/ask', { question }).then((r) => r.data.data);

export const getInventoryInsights = () =>
  axios.get('/ai/inventory-insights').then((r) => r.data.data);
