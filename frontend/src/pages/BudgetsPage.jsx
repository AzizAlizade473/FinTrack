import { useState, useEffect, useContext } from 'react';
import { ToastContext } from '../App.jsx';
import { getBudgets, setBudget, deleteBudget, getAlerts } from '../api.js';
import PageHeader from '../components/PageHeader.jsx';
import BudgetCard from '../components/BudgetCard.jsx';
import Modal from '../components/Modal.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';

export default function BudgetsPage() {
  const showToast = useContext(ToastContext);
  const [budgets, setBudgetsState] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [loading, setLoading] = useState(true);
  
  const [showModal, setShowModal] = useState(false);
  const [category, setCategory] = useState('');
  const [limit, setLimit] = useState('');

  useEffect(() => { fetchData(); }, []);

  const fetchData = async () => {
    try {
      const budData = await getBudgets();
      setBudgetsState(budData?.budgets ?? []);
      const alertData = await getAlerts();
      setAlerts(alertData?.alerts ?? []);
    } catch (err) {
      console.error("BudgetsPage fetch error:", err);
      showToast('Failed to load budgets', 'error');
    }
    setLoading(false);
  };

  const handleSetBudget = async () => {
    if (!category || !limit) { showToast('Fill in all fields', 'warning'); return; }
    try {
      await setBudget(category, parseFloat(limit));
      showToast('Budget updated successfully!', 'success');
      setShowModal(false);
      setCategory('');
      setLimit('');
      fetchData();
    } catch { showToast('Failed to set budget', 'error'); }
  };

  const handleDeleteBudget = async (catToDelete) => {
    if (!confirm(`Delete budget for ${catToDelete}?`)) return;
    try {
      await deleteBudget(catToDelete);
      showToast('Budget deleted successfully!', 'success');
      fetchData();
    } catch { showToast('Failed to delete budget', 'error'); }
  };

  if (loading) return (
    <div className="space-y-6">
      <PageHeader title="Budgets" />
      <LoadingSkeleton count={3} height="h-32" />
    </div>
  );

  return (
    <div className="space-y-6 relative">
      <div className="flex items-center justify-between mb-8">
        <div>
          <h1 className="text-[28px] font-bold text-navy">Budgets</h1>
          <p className="text-[14px] text-[#6b7280] mt-1">Manage and track your monthly spending limits</p>
        </div>
        <button onClick={() => setShowModal(true)} className="hidden sm:block px-5 py-2.5 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 active:scale-[0.98] transition-all shadow-sm">
          + Set Budget
        </button>
      </div>

      {alerts.length > 0 && (
        <div className="bg-[#fff5f5] border border-red/30 rounded-[12px] p-4 flex gap-3 items-start shadow-sm">
          <div className="p-1 rounded-full bg-red text-white mt-0.5">
            <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          </div>
          <div>
            <h4 className="text-[15px] font-bold text-red">Action Required: Budgets Exceeded</h4>
            <ul className="list-disc pl-5 mt-2 space-y-1">
              {alerts.map((a, i) => <li key={i} className="text-red/90 text-[13px]">{a}</li>)}
            </ul>
          </div>
        </div>
      )}

      {/* Mobile action button */}
      <button onClick={() => setShowModal(true)} className="sm:hidden w-full px-5 py-3 mb-4 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 active:scale-[0.98] transition-all shadow-sm">
        + Set Budget
      </button>

      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6">
        {budgets.map((b, i) => <BudgetCard key={i} budget={b} onDelete={handleDeleteBudget} />)}
      </div>

      {budgets.length === 0 && (
        <div className="text-center py-20 px-4">
          <div className="w-16 h-16 bg-white rounded-2xl shadow-sm border border-border flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-[#9ca3af]" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 6v6m0 0v6m0-6h6m-6 0H6" /></svg>
          </div>
          <h3 className="text-[18px] font-bold text-navy mb-1">No budgets created yet</h3>
          <p className="text-[#6b7280] text-[14px] max-w-sm mx-auto">Set up limits for categories like Food or Housing to stay on top of your spending.</p>
        </div>
      )}

      <Modal isOpen={showModal} onClose={() => setShowModal(false)} title="Set Budget">
        <div className="space-y-4">
          <div>
            <label className="block text-[13px] font-medium text-navy mb-1.5">Category</label>
            <input value={category} onChange={e => setCategory(e.target.value)} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="e.g. Housing" />
          </div>
          <div>
            <label className="block text-[13px] font-medium text-navy mb-1.5">Monthly Limit ($)</label>
            <input type="number" step="0.01" value={limit} onChange={e => setLimit(e.target.value)} className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]" placeholder="0.00" />
          </div>
        </div>
        <button onClick={handleSetBudget} className="w-full mt-6 py-3 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 transition-all active:scale-[0.98]">Save Budget Limit</button>
      </Modal>
    </div>
  );
}
