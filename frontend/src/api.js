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
    console.error(err);
    throw err;
  }
};

export const loginUser = async (email, password) => {
  try {
    const res = await API.post('/auth/login', { email, password });
    if (res.data && res.data.user) {
      localStorage.setItem("user", JSON.stringify(res.data.user));
      window.location.href = "/dashboard";
    }
    return res;
  } catch (err) {
    console.error(err);
    throw err;
  }
};

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
export const getMonthlyReport = async (month) => {
  try {
    const res = await axios.get(`${BASE_URL}/api/reports/monthly?month=${month}`);
    return res.data;
  } catch (err) {
    return {};
  }
};

export const getCategoryReport = async () => {
  try {
    const res = await axios.get(`${BASE_URL}/api/reports/category`);
    return res.data;
  } catch (err) {
    return {};
  }
};

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
