import axios from 'axios';

const BASE_URL = import.meta.env.VITE_API_URL;
console.log("API BASE URL:", BASE_URL);

const API = axios.create({
  baseURL: `${BASE_URL}/api`,
  headers: { 'Content-Type': 'application/json' },
});

// ============ AUTH ============
export const registerUser = async (name, email, password) => {
  try {
    const res = await API.post('/auth/register', { name, email, password });
    if (res.status === 200 || res.status === 201) {
      return await loginUser(email, password);
    }
    return res;
  } catch (err) {
    console.error("registerUser error:", err);
    throw err;
  }
};

export const loginUser = async (email, password) => {
  try {
    const res = await API.post('/auth/login', { email, password });
    if (res.data && res.data.user) {
      localStorage.setItem("user", JSON.stringify(res.data.user));
    }
    return res;
  } catch (err) {
    console.error("loginUser error:", err);
    throw err;
  }
};

// ============ TRANSACTIONS ============
export const getTransactions = async () => {
  try {
    const res = await API.get('/transactions');
    return res.data;
  } catch (err) {
    console.error("getTransactions error:", err);
    return { transactions: [] };
  }
};

export const addIncome = async (data) => {
  try {
    return await API.post('/transactions/income', data);
  } catch (err) {
    console.error("addIncome error:", err);
    throw err;
  }
};

export const addExpense = async (data) => {
  try {
    return await API.post('/transactions/expense', data);
  } catch (err) {
    console.error("addExpense error:", err);
    throw err;
  }
};

export const deleteTransaction = async (id) => {
  try {
    return await API.delete(`/transactions/${id}`);
  } catch (err) {
    console.error("deleteTransaction error:", err);
    throw err;
  }
};

export const exportCSV = async () => {
  try {
    const response = await fetch(`${BASE_URL}/api/transactions/export`);
    if (!response.ok) throw new Error('Export failed');
    const blob = await response.blob();
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'transactions.csv';
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
  } catch (err) {
    console.error("exportCSV error:", err);
    throw err;
  }
};

// ============ BUDGETS ============
export const getBudgets = async () => {
  try {
    const res = await API.get('/budgets');
    return res.data;
  } catch (err) {
    console.error("getBudgets error:", err);
    return { budgets: [] };
  }
};

export const setBudget = async (category, limit) => {
  try {
    return await API.post('/budgets', { category, limit });
  } catch (err) {
    console.error("setBudget error:", err);
    throw err;
  }
};

// ============ REPORTS ============
export const getMonthlyReport = async (month) => {
  try {
    const res = await API.get(`/reports/monthly?month=${month}`);
    return res.data;
  } catch (err) {
    console.error("getMonthlyReport error:", err);
    return { income: 0, expense: 0, balance: 0 };
  }
};

export const getCategoryReport = async () => {
  try {
    const res = await API.get('/reports/category');
    return res.data;
  } catch (err) {
    console.error("getCategoryReport error:", err);
    return { categories: [] };
  }
};

// ============ BALANCE & ALERTS ============
export const getBalance = async () => {
  try {
    const res = await API.get('/balance');
    return res.data;
  } catch (err) {
    console.error("getBalance error:", err);
    return { balance: 0 };
  }
};

export const getAlerts = async () => {
  try {
    const res = await API.get('/alerts');
    return res.data;
  } catch (err) {
    console.error("getAlerts error:", err);
    return { alerts: [] };
  }
};

export const getInsights = async () => {
  try {
    const res = await API.get('/insights');
    return res.data;
  } catch (err) {
    console.error("getInsights error:", err);
    return { insights: [] };
  }
};

export default API;
