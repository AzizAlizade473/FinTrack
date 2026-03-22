import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8080/api',
  headers: { 'Content-Type': 'application/json' },
});

// ============ AUTH ============
export const registerUser = (name, email, password) =>
  API.post('/auth/register', { name, email, password });

export const loginUser = (email, password) =>
  API.post('/auth/login', { email, password });

// ============ TRANSACTIONS ============
export const getTransactions = () => API.get('/transactions');

export const addIncome = (data) => API.post('/transactions/income', data);

export const addExpense = (data) => API.post('/transactions/expense', data);

export const deleteTransaction = (id) => API.delete(`/transactions/${id}`);

// ============ BUDGETS ============
export const getBudgets = () => API.get('/budgets');

export const setBudget = (category, limit) =>
  API.post('/budgets', { category, limit });

// ============ REPORTS ============
export const getMonthlyReport = (month) =>
  API.get(`/reports/monthly?month=${month}`);

export const getCategoryReport = () => API.get('/reports/category');

// ============ BALANCE & ALERTS ============
export const getBalance = () => API.get('/balance');

export const getAlerts = () => API.get('/alerts');

export default API;
