import { useState, useEffect, useContext } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ToastContext } from '../App.jsx';
import { getTransactions, addIncome, addExpense, deleteTransaction, getBudgets } from '../api.js';
import PageHeader from '../components/PageHeader.jsx';
import Modal from '../components/Modal.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';

const CATEGORY_COLORS = {
  Salary: 'bg-green/10 text-green',
  Freelance: 'bg-[#06b6d4]/10 text-[#06b6d4]',
  Rent: 'bg-[#8b5cf6]/10 text-[#8b5cf6]',
  Housing: 'bg-[#8b5cf6]/10 text-[#8b5cf6]',
  Groceries: 'bg-[#f59e0b]/10 text-[#f59e0b]',
  Food: 'bg-[#f59e0b]/10 text-[#f59e0b]',
  Netflix: 'bg-red/10 text-red',
  Entertainment: 'bg-red/10 text-red',
  Gym: 'bg-[#14b8a6]/10 text-[#14b8a6]',
  Health: 'bg-[#14b8a6]/10 text-[#14b8a6]',
  Default: 'bg-[#f3f4f6] text-[#6b7280]'
};

export default function TransactionsPage() {
  const showToast = useContext(ToastContext);
  const [searchParams] = useSearchParams();
  
  const [transactions, setTransactions] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [loading, setLoading] = useState(true);

  // Filters
  const [filterType, setFilterType] = useState('All');
  const [filterCategory, setFilterCategory] = useState(searchParams.get('category') || 'All');
  const [filterMonth, setFilterMonth] = useState('');
  const [search, setSearch] = useState('');

  // Modals
  const [showIncomeModal, setShowIncomeModal] = useState(false);
  const [showExpenseModal, setShowExpenseModal] = useState(false);
  const [form, setForm] = useState({ description: '', amount: '', date: '', category: '', source: '', merchant: '' });

  const openSalaryModal = () => {
    setForm({ 
      description: 'Monthly Salary', 
      amount: '', 
      date: new Date().toISOString().split('T')[0], 
      category: 'Salary', 
      source: 'Work', 
      merchant: '' 
    });
    setShowIncomeModal(true);
  };

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    try {
      const [txRes, budRes] = await Promise.all([getTransactions(), getBudgets()]);
      setTransactions(txRes?.transactions ?? []);
      setBudgets(budRes?.budgets ?? []);
    } catch { showToast('Failed to load data', 'error'); }
    setLoading(false);
  };

  const fetchTransactions = async () => {
    try {
      const res = await getTransactions();
      setTransactions(res?.transactions ?? []);
    } catch { showToast('Failed to load transactions', 'error'); }
  };

  const handleAdd = async (type) => {
    if (!form.category) { showToast('Please select a category', 'warning'); return; }
    try {
      if (type === 'income') {
        await addIncome(form);
        setShowIncomeModal(false);
      } else {
        await addExpense(form);
        setShowExpenseModal(false);
      }
      showToast(`${type === 'income' ? 'Income' : 'Expense'} added!`);
      setForm({ description: '', amount: '', date: '', category: '', source: '', merchant: '' });
      fetchTransactions();
    } catch { showToast('Failed to add transaction', 'error'); }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this transaction?')) return;
    try {
      await deleteTransaction(id);
      showToast('Transaction deleted');
      fetchTransactions();
    } catch { showToast('Failed to delete', 'error'); }
  };

  const categories = [...new Set(transactions.map(t => t.category).filter(Boolean))];

  const filtered = transactions.filter(t => {
    if (filterType !== 'All' && t.type !== filterType) return false;
    if (filterCategory !== 'All' && t.category !== filterCategory) return false;
    if (filterMonth && !t.date.startsWith(filterMonth)) return false;
    if (search && !t.description.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const getCategoryColor = (cat) => CATEGORY_COLORS[cat] || CATEGORY_COLORS.Default;

  const formFields = (
    <div className="space-y-4">
      <div>
        <label className="block text-[13px] font-medium text-navy mb-1.5">Description</label>
        <input value={form.description} onChange={e => setForm({ ...form, description: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="e.g. Groceries" />
      </div>
      <div>
        <label className="block text-[13px] font-medium text-navy mb-1.5">Amount ($)</label>
        <input type="number" step="0.01" value={form.amount} onChange={e => setForm({ ...form, amount: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="0.00" />
      </div>
      <div>
        <label className="block text-[13px] font-medium text-navy mb-1.5">Date</label>
        <input type="date" value={form.date} onChange={e => setForm({ ...form, date: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" />
      </div>
      <div>
        <label className="block text-[13px] font-medium text-navy mb-1.5">Category</label>
        {budgets.length > 0 ? (
          <select value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]">
            <option value="" disabled>Select a category</option>
            {budgets.map(b => <option key={b.category} value={b.category}>{b.category}</option>)}
          </select>
        ) : (
          <input value={form.category} onChange={e => setForm({ ...form, category: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="e.g. Food, Housing, Salary" />
        )}
      </div>
    </div>
  );

  if (loading) return (
    <div className="space-y-6">
      <PageHeader title="Transactions" />
      <LoadingSkeleton count={5} />
    </div>
  );

  return (
    <div className="space-y-6">
      <PageHeader title="Transactions" subtitle={`${filtered.length} entries found`} />

      <div className="flex flex-col lg:flex-row gap-4 justify-between items-start lg:items-center bg-white p-4 rounded-[16px] border border-border shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
        <div className="flex flex-wrap items-center gap-3">
          <div className="flex bg-[#f3f4f6] p-1 rounded-[10px]">
            {['All', 'INCOME', 'EXPENSE'].map(type => (
              <button key={type} onClick={() => setFilterType(type)} className={`px-4 py-1.5 rounded-md text-[13px] font-medium transition-all ${filterType === type ? 'bg-white text-navy shadow-sm' : 'text-[#6b7280] hover:text-navy'}`}>
                {type === 'All' ? 'All' : type.charAt(0) + type.slice(1).toLowerCase()}
              </button>
            ))}
          </div>
          <select value={filterCategory} onChange={e => setFilterCategory(e.target.value)} className="px-3 py-2 rounded-[10px] bg-white border border-border text-[13px] text-navy focus:outline-none focus:border-purple focus:ring-1 focus:ring-purple transition-all">
            <option value="All">All Categories</option>
            {categories.map(c => <option key={c} value={c}>{c}</option>)}
          </select>
          <input type="month" value={filterMonth} onChange={e => setFilterMonth(e.target.value)} className="px-3 py-2 rounded-[10px] bg-white border border-border text-[13px] text-navy focus:outline-none focus:border-purple focus:ring-1 focus:ring-purple transition-all max-w-[150px]" />
          <div className="relative">
            <svg className="w-4 h-4 text-[#9ca3af] absolute left-3 top-1/2 -translate-y-1/2" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" /></svg>
            <input type="text" placeholder="Search..." value={search} onChange={e => setSearch(e.target.value)} className="pl-9 pr-4 py-2 rounded-[10px] bg-white border border-border text-[13px] text-navy placeholder-[#9ca3af] focus:outline-none focus:border-purple focus:ring-1 focus:ring-purple transition-all w-full sm:w-[200px]" />
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button onClick={openSalaryModal} className="px-4 py-2 rounded-[10px] bg-green text-white text-[13px] font-bold hover:bg-opacity-90 transition-all active:scale-[0.98] shadow-sm flex items-center gap-2">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>
            + Add Salary
          </button>
          <button onClick={() => { setForm({ description: '', amount: '', date: '', category: '', source: '', merchant: '' }); setShowIncomeModal(true); }} className="px-4 py-2 rounded-[10px] border border-green text-green text-[13px] font-semibold hover:bg-green/10 transition-all active:scale-[0.98]">
            + Add Income
          </button>
          <button onClick={() => { setForm({ description: '', amount: '', date: '', category: '', source: '', merchant: '' }); setShowExpenseModal(true); }} className="px-4 py-2 rounded-[10px] border border-red text-red text-[13px] font-semibold hover:bg-red/10 transition-all active:scale-[0.98]">
            + Add Expense
          </button>
        </div>
      </div>

      <div className="bg-white rounded-[16px] border border-border overflow-hidden shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse min-w-[700px]">
            <thead>
              <tr className="border-b border-border bg-[#fafafa]">
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Date</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Description</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Merchant/Source</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Category</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Type</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider text-right">Amount</th>
                <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider text-center w-20"></th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filtered.map(t => {
                const isSalary = t.category?.toLowerCase() === 'salary';
                return (
                <tr key={t.id} className={`transition-colors group ${isSalary ? 'bg-green/5 hover:bg-green/10' : 'hover:bg-hover'}`}>
                  <td className="px-6 py-4 text-[14px] text-[#6b7280] whitespace-nowrap">{t.date}</td>
                  <td className="px-6 py-4 text-[14px] font-bold text-navy truncate max-w-[150px]">{t.description}</td>
                  <td className="px-6 py-4 text-[13px] text-navy font-medium italic opacity-80 whitespace-nowrap">
                    {t.merchant || t.source || '-'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className={`inline-flex items-center px-2.5 py-1 rounded-md text-[11px] font-bold uppercase tracking-wide ${getCategoryColor(t.category)}`}>
                      {t.category}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span className="text-[13px] font-medium text-navy">
                      {t.type === 'INCOME' ? 'Income' : 'Expense'}
                    </span>
                  </td>
                  <td className={`px-6 py-4 text-[14px] font-bold text-right whitespace-nowrap ${t.type === 'INCOME' ? 'text-green' : 'text-navy'}`}>
                    {t.type === 'INCOME' ? '+' : '-'}${t.amount.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}
                  </td>
                  <td className="px-6 py-4 text-center">
                    <button onClick={() => handleDelete(t.id)} className="text-[#9ca3af] hover:text-red transition-all p-1.5 rounded-md hover:bg-red/10 cursor-pointer" title="Delete">
                      <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
                    </button>
                  </td>
                </tr>
              )})}
              {filtered.length === 0 && (
                <tr>
                  <td colSpan="6" className="px-6 py-12 text-center text-[#9ca3af] text-[14px]">
                    <svg className="w-12 h-12 mx-auto mb-3 text-[#d1D5db]" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" /></svg>
                    No transactions found matching your criteria.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      <Modal isOpen={showIncomeModal} onClose={() => setShowIncomeModal(false)} title="Add Income">
        {formFields}
        <div className="mt-4">
          <label className="block text-[13px] font-medium text-navy mb-1.5">Source</label>
          <input value={form.source} onChange={e => setForm({ ...form, source: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="e.g. Employer" />
        </div>
        <button onClick={() => handleAdd('income')} className="w-full mt-6 py-3 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 transition-all active:scale-[0.98]">Save Income</button>
      </Modal>

      <Modal isOpen={showExpenseModal} onClose={() => setShowExpenseModal(false)} title="Add Expense">
        {formFields}
        <div className="mt-4">
          <label className="block text-[13px] font-medium text-navy mb-1.5">Merchant / Payee</label>
          <input value={form.merchant} onChange={e => setForm({ ...form, merchant: e.target.value })} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="e.g. Amazon, Starbucks" />
        </div>
        <button onClick={() => handleAdd('expense')} className="w-full mt-6 py-3 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 transition-all active:scale-[0.98]">Save Expense</button>
      </Modal>
    </div>
  );
}
