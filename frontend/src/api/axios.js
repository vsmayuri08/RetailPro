import axios from 'axios';

const axiosInstance = axios.create({
  baseURL: '/api'
});

axiosInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('retail_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
}, (error) => Promise.reject(error));

axiosInstance.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('retail_token');
      localStorage.removeItem('retail_user');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default axiosInstance;
