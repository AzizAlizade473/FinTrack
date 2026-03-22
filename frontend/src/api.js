import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL;
console.log("API BASE URL:", BASE_URL);

const API = axios.create({
  baseURL: `${BASE_URL}/api`,
  headers: { 'Content-Type': 'application/json' },
});

// ============ AUTH ============
export const registerUser = (name, email, password) =>
  API.post('/auth/register', { name, email, password });

export const loginUser = (email, password) =>
  API.post('/auth/login', { email, password });

// ============ TRANSACTIONS ============
export const getTransactions = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/api/transactions`);
    return res.data;
  } catch (err) {
    console.error("getTransactions error:", err);
    return [];
  }
};

export const addIncome = (data) => API.post('/transactions/income', data);

export const addExpense = (data) => API.post('/transactions/expense', data);

export const deleteTransaction = (id) => API.delete(`/transactions/${id}`);

// ============ BUDGETS ============
export const getBudgets = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/api/budgets`);
    return res.data;
  } catch (err) {
    console.error("getBudgets error:", err);
    return [];
  }
};

export const setBudget = (category, limit) =>
  API.post('/budgets', { category, limit });

// ============ REPORTS ============
export const getMonthlyReport = (month) =>
  API.get(`/reports/monthly?month=${month}`);

export const getCategoryReport = () => API.get('/reports/category');

// ============ BALANCE & ALERTS ============
export const getBalance = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/api/balance`);
    return res.data;
  } catch (err) {
    console.error("getBalance error:", err);
    return { balance: 0 };
  }
};

export const getAlerts = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/api/alerts`);
    return res.data;
  } catch (err) {
    console.error("getAlerts error:", err);
    return [];
  }
};

export default API;
