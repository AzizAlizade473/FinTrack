import { useState, useEffect, useContext } from 'react';
import { ToastContext } from '../App.jsx';
import { getTransactions, getBalance, getBudgets, getAlerts, getInsights } from '../api.js';
import PageHeader from '../components/PageHeader.jsx';
import StatCard from '../components/StatCard.jsx';
import BudgetCard from '../components/BudgetCard.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';
import Modal from '../components/Modal.jsx';
import { LineChart, Line, PieChart, Pie, Cell, XAxis, YAxis, Tooltip, ResponsiveContainer, Legend } from 'recharts';
import { Link } from 'react-router-dom';

const PIE_COLORS = ['#6c63ff', '#16a34a', '#dc2626', '#f59e0b', '#06b6d4', '#ec4899', '#8b5cf6', '#14b8a6'];

export default function DashboardPage() {
  let user = {};
  try {
    user = JSON.parse(localStorage.getItem("user") || "{}");
  } catch(e) {
    console.error("Invalid user JSON in localStorage");
    user = {};
  }

  const showToast = useContext(ToastContext);
  const [loading, setLoading] = useState(true);
  const [balance, setBalance] = useState(0);
  const [totalIncome, setTotalIncome] = useState(0);
  const [totalExpense, setTotalExpense] = useState(0);
  const [transactions, setTransactions] = useState([]);
  const [budgets, setBudgets] = useState([]);
  const [alerts, setAlerts] = useState([]);
  const [insights, setInsights] = useState([]);
  const [modalData, setModalData] = useState({ isOpen: false, type: '', title: '' });

  useEffect(() => {
    const load = async () => {
      try {
        const b = await getBalance();
        setBalance(b?.balance ?? 0);
        setTotalIncome(b?.totalIncome ?? 0);
        setTotalExpense(b?.totalExpense ?? 0);

        const t = await getTransactions();
        setTransactions(t?.transactions ?? []);

        const bu = await getBudgets();
        setBudgets(bu?.budgets ?? []);

        const al = await getAlerts();
        setAlerts(al?.alerts ?? []);

        const ins = await getInsights();
        setInsights(ins?.insights ?? []);
      } catch (err) {
        console.error("Dashboard fetch error:", err);
        showToast('Failed to load dashboard data', 'error');
      }
      setLoading(false);
    };
    load();
  }, []);

  const recentTx = [...transactions].reverse().slice(0, 5);

  const catMap = {};
  transactions.filter(t => t.type === 'EXPENSE').forEach(t => {
    catMap[t.category] = (catMap[t.category] || 0) + t.amount;
  });
  const pieData = Object.entries(catMap).map(([name, value]) => ({ name, value }));

  const monthMap = {};
  transactions.forEach(t => {
    if (!t.date) return;
    const m = t.date.substring(0, 7);
    if (!monthMap[m]) monthMap[m] = { month: m, Income: 0, Expense: 0 };
    if (t.type === 'INCOME') monthMap[m].Income += t.amount;
    else monthMap[m].Expense += t.amount;
  });
  const lineData = Object.values(monthMap).sort((a, b) => a.month.localeCompare(b.month));

  if (loading) return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" subtitle={`Welcome back, ${user?.name || 'User'}`} />
      <LoadingSkeleton count={4} height="h-32" />
    </div>
  );

  return (
    <div className="space-y-6">
      <PageHeader title="Dashboard" subtitle={`Welcome back, ${user?.name || 'User'}`} />

      {/* Premium Salary Banner */}
      {(() => {
        const salaryBudget = budgets.find(b => b.category.toLowerCase() === 'salary');
        if (!salaryBudget) return null;
        
        const pct = salaryBudget.limit > 0 ? Math.min((salaryBudget.spent / salaryBudget.limit) * 100, 100) : 0;
        const reached = salaryBudget.spent >= salaryBudget.limit;
        
        return (
          <div className="relative overflow-hidden bg-gradient-to-r from-[#16a34a] to-[#0d9488] rounded-[24px] p-8 text-white shadow-[0_20px_50px_rgba(22,163,74,0.15)] mb-8 group">
            <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -translate-y-1/2 translate-x-1/2 blur-3xl group-hover:bg-white/20 transition-all duration-700"></div>
            <div className="relative flex flex-col md:flex-row items-center justify-between gap-8">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-4">
                  <span className="px-3 py-1 bg-white/20 rounded-full text-[12px] font-bold tracking-wider uppercase backdrop-blur-md">
                    Monthly Salary Goal
                  </span>
                  {reached && (
                    <span className="px-3 py-1 bg-white text-[#16a34a] rounded-full text-[12px] font-bold tracking-wider uppercase shadow-sm">
                      Target Achieved
                    </span>
                  )}
                </div>
                <h2 className="text-[32px] font-extrabold leading-tight mb-2">
                  ${salaryBudget.spent.toLocaleString()} <span className="text-white/70 font-medium text-[20px]">earned</span>
                </h2>
                <p className="text-white/80 text-[15px] max-w-md">
                  You have achieved <span className="text-white font-bold">{pct.toFixed(0)}%</span> of your monthly salary target (${salaryBudget.limit.toLocaleString()}).
                </p>
              </div>
              
              <div className="w-full md:w-64 flex flex-col items-center gap-3">
                <div className="relative w-32 h-32 flex items-center justify-center">
                  <svg className="w-full h-full -rotate-90 transform" viewBox="0 0 100 100">
                    <circle className="text-white/10" strokeWidth="8" stroke="currentColor" fill="transparent" r="40" cx="50" cy="50" />
                    <circle 
                      className="text-white transition-all duration-1000 ease-out" 
                      strokeWidth="8" 
                      strokeDasharray={2 * Math.PI * 40} 
                      strokeDashoffset={(1 - pct / 100) * 2 * Math.PI * 40} 
                      strokeLinecap="round" 
                      stroke="currentColor" 
                      fill="transparent" 
                      r="40" 
                      cx="50" 
                      cy="50" 
                    />
                  </svg>
                  <span className="absolute text-[24px] font-black">{pct.toFixed(0)}%</span>
                </div>
                <Link to="/transactions" className="mt-2 px-6 py-2.5 bg-white text-[#16a34a] rounded-xl text-[14px] font-bold hover:bg-opacity-90 transition-all shadow-md active:scale-95">
                  Update Earnings
                </Link>
              </div>
            </div>
          </div>
        );
      })()}

      {insights.length > 0 && (
        <div className="bg-purple/10 border border-purple/20 rounded-[12px] p-5 flex gap-4 items-start shadow-sm">
          <div className="w-10 h-10 rounded-full bg-purple/20 flex items-center justify-center shrink-0">
            <svg className="w-5 h-5 text-purple" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
            </svg>
          </div>
          <div className="flex-1">
            <h4 className="text-[15px] font-bold text-navy mb-2">Smart Financial Insights</h4>
            <div className="space-y-2">
              {insights.map((ins, i) => (
                <p key={i} className="text-[#4b5563] text-[13.5px] font-medium leading-relaxed bg-white/50 px-3 py-1.5 rounded-[8px] border border-white/60">
                 • {ins}
                </p>
              ))}
            </div>
          </div>
        </div>
      )}

      {alerts.length > 0 && (
        <div className="bg-red/10 border border-red/20 rounded-[12px] p-4 flex gap-3 items-start">
          <svg className="w-5 h-5 text-red shrink-0 mt-0.5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>
          <div>
            <h4 className="text-[14px] font-bold text-red">Budget Alerts</h4>
            {alerts.map((a, i) => <p key={i} className="text-red/80 text-[13px] mt-1">{a}</p>)}
          </div>
        </div>
      )}

      {/* Row 1: Stat Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
        <StatCard title="Total Balance" value={balance} type="balance" onClick={() => setModalData({ isOpen: true, type: 'balance', title: 'Total Balance Details' })} />
        <StatCard title="Total Income" value={totalIncome} type="income" badgePrefix="+" onClick={() => setModalData({ isOpen: true, type: 'income', title: 'Income Details' })} />
        <StatCard title="Total Expenses" value={totalExpense} type="expense" badgePrefix="-" onClick={() => setModalData({ isOpen: true, type: 'expense', title: 'Expense Details' })} />
        <StatCard title="Active Budgets" value={budgets.length} type="budgets" isCurrency={false} onClick={() => setModalData({ isOpen: true, type: 'budgets', title: 'Active Budgets' })} />
      </div>

      {/* Row 2: Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        <div className="lg:col-span-3 bg-white rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
          <h3 className="text-[16px] font-bold text-navy mb-6">Income vs Expenses</h3>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={lineData} margin={{ top: 5, right: 20, left: -20, bottom: 0 }}>
              <XAxis dataKey="month" stroke="#9ca3af" tick={{ fill: '#6b7280', fontSize: 12 }} axisLine={false} tickLine={false} dy={10} />
              <YAxis stroke="#9ca3af" tick={{ fill: '#6b7280', fontSize: 12 }} axisLine={false} tickLine={false} tickFormatter={(val) => `$${val}`} />
              <Tooltip 
                contentStyle={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.1)', fontSize: '13px', fontWeight: '500' }}
                cursor={{ stroke: '#f3f4f6', strokeWidth: 2, strokeDasharray: '4 4' }}
              />
              <Legend iconType="circle" wrapperStyle={{ fontSize: '13px', paddingTop: '20px' }} />
              <Line type="monotone" dataKey="Income" stroke="#16a34a" strokeWidth={3} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 6 }} />
              <Line type="monotone" dataKey="Expense" stroke="#dc2626" strokeWidth={3} dot={{ r: 4, strokeWidth: 2, fill: '#fff' }} activeDot={{ r: 6 }} />
            </LineChart>
          </ResponsiveContainer>
        </div>

        <div className="lg:col-span-2 bg-white rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)] flex flex-col">
          <h3 className="text-[16px] font-bold text-navy mb-2">Spending by Category</h3>
          <div className="flex-1 min-h-[220px]">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie data={pieData} cx="50%" cy="50%" innerRadius={60} outerRadius={85} paddingAngle={2} dataKey="value" stroke="none">
                  {pieData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                </Pie>
                <Tooltip contentStyle={{ background: '#fff', border: '1px solid #e5e7eb', borderRadius: '8px', fontSize: '13px', fontWeight: '500' }} itemStyle={{ color: '#1a1a2e' }} />
              </PieChart>
            </ResponsiveContainer>
          </div>
          {pieData.length > 0 ? (
            <div className="flex flex-wrap gap-x-4 gap-y-2 justify-center mt-2">
              {pieData.slice(0, 4).map((d, i) => (
                <div key={i} className="flex items-center gap-1.5 text-[12px] font-medium text-[#6b7280]">
                  <div className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }}></div>
                  {d.name} <span className="text-navy">${d.value}</span>
                </div>
              ))}
            </div>
          ) : (
            <p className="text-center text-[13px] text-[#9ca3af] mt-auto">No expense data yet</p>
          )}
        </div>
      </div>

      {/* Row 3: Recent Tx + Budgets */}
      <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">
        {/* Recent Transactions */}
        <div className="bg-white rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)] flex flex-col">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-[16px] font-bold text-navy">Recent Transactions</h3>
            <Link to="/transactions" className="text-[13px] font-medium text-purple hover:text-navy transition-colors">View all</Link>
          </div>
          <div className="flex-1 flex flex-col gap-1">
            {recentTx.length === 0 && <p className="text-[#9ca3af] text-[14px] py-4">No transactions yet.</p>}
            {recentTx.map((t) => (
              <div key={t.id} className="flex items-center justify-between p-3 rounded-[12px] hover:bg-hover transition-colors group">
                <div className="flex items-center gap-3">
                  <div className={`w-10 h-10 rounded-full flex items-center justify-center shrink-0 ${t.type === 'INCOME' ? 'bg-green/10 text-green' : 'bg-[#f3f4f6] text-[#6b7280]'}`}>
                    {t.type === 'INCOME' 
                      ? <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" /></svg>
                      : <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" /></svg>
                    }
                  </div>
                  <div>
                    <p className="text-[14px] font-bold text-navy">{t.description}</p>
                    <div className="flex items-center gap-2 mt-0.5">
                      <span className="text-[12px] text-[#6b7280]">{t.date}</span>
                      <span className="w-1 h-1 rounded-full bg-[#cbd5e1]"></span>
                      <span className="text-[11px] px-2 py-0.5 rounded-md bg-[#f3f4f6] text-[#6b7280] font-medium">{t.category}</span>
                    </div>
                  </div>
                </div>
                <span className={`font-bold text-[14px] ${t.type === 'INCOME' ? 'text-green' : 'text-navy'}`}>
                  {t.type === 'INCOME' ? '+' : '-'}${t.amount.toFixed(2)}
                </span>
              </div>
            ))}
          </div>
        </div>

        {/* Budget Status */}
        <div className="bg-white rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-[16px] font-bold text-navy">Budget Status</h3>
            <Link to="/budgets" className="text-[13px] font-medium text-purple hover:text-navy transition-colors">Manage</Link>
          </div>
          <div className="space-y-4 pt-2">
            {budgets.length === 0 && <p className="text-[#9ca3af] text-[14px]">No active budgets.</p>}
            {budgets.slice(0, 4).map((b, i) => {
              const pct = b.limit > 0 ? Math.min((b.spent / b.limit) * 100, 100) : 0;
              let fillClass = "bg-green";
              if (pct >= 80 && pct < 100) fillClass = "bg-[#f59e0b]";
              if (b.spent > b.limit) fillClass = "bg-red";

              return (
                <div key={i} className="group">
                  <div className="flex items-center justify-between mb-1.5">
                    <span className="text-[13px] font-semibold text-navy">{b.category}</span>
                    <span className="text-[12px] font-medium text-[#6b7280]">
                      <span className={b.spent > b.limit ? "text-red" : "text-navy"}>${(b.spent || 0).toFixed(0)}</span> / ${(b.limit || 0).toFixed(0)}
                    </span>
                  </div>
                  <div className="w-full h-[6px] bg-[#f3f4f6] rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-700 w-0 ${fillClass}`} style={{ width: `${Math.min(pct, 100)}%` }} />
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </div>

      <Modal isOpen={modalData.isOpen} onClose={() => setModalData({ ...modalData, isOpen: false })} title={modalData.title}>
        {modalData.type === 'balance' && (
           <p className="text-[#6b7280]">Your current net balance is {balance >= 0 ? 'positive' : 'negative'}. It is calculated as Total Income minus Total Expenses.</p>
        )}
        {modalData.type === 'income' && (
           <div className="space-y-3 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
             {transactions.filter(t => t.type === 'INCOME').slice(0, 10).map(t => (
               <div key={t.id} className="flex justify-between items-center py-2 border-b border-[#f3f4f6] last:border-0 hover:bg-hover px-2 rounded-lg transition-colors">
                 <div>
                   <p className="text-[14px] font-bold text-navy">{t.description}</p>
                   <p className="text-[12px] text-[#9ca3af]">{t.date} • {t.source || t.category}</p>
                 </div>
                 <span className="text-green font-bold">+${t.amount.toFixed(2)}</span>
               </div>
             ))}
             {transactions.filter(t => t.type === 'INCOME').length === 0 && <p className="text-[13px] text-secondary">No income records found.</p>}
           </div>
        )}
        {modalData.type === 'expense' && (
           <div className="space-y-3 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
             {transactions.filter(t => t.type === 'EXPENSE').slice(0, 10).map(t => (
               <div key={t.id} className="flex justify-between items-center py-2 border-b border-[#f3f4f6] last:border-0 hover:bg-hover px-2 rounded-lg transition-colors">
                 <div>
                   <p className="text-[14px] font-bold text-navy">{t.description}</p>
                   <p className="text-[12px] text-[#9ca3af]">{t.date} • {t.category}</p>
                 </div>
                 <span className="text-red font-bold">-${t.amount.toFixed(2)}</span>
               </div>
             ))}
             {transactions.filter(t => t.type === 'EXPENSE').length === 0 && <p className="text-[13px] text-secondary">No expense records found.</p>}
           </div>
        )}
        {modalData.type === 'budgets' && (
           <div className="space-y-4 max-h-[300px] overflow-y-auto pr-2 custom-scrollbar">
             {budgets.map(b => (
                <div key={b.category} className="bg-[#f8fafc] rounded-lg p-3 border border-border">
                  <div className="flex justify-between mb-1">
                    <span className="text-[13px] font-bold text-navy">{b.category}</span>
                    <span className="text-[13px] text-[#6b7280]">${b.spent || 0} / ${b.limit}</span>
                  </div>
                  <div className="w-full h-[6px] bg-[#e2e8f0] rounded-full overflow-hidden">
                    <div className={`h-full rounded-full transition-all duration-700 w-0 ${b.spent > b.limit ? 'bg-red' : 'bg-purple'}`} style={{ width: `${Math.min(((b.spent || 0) / (b.limit || 1)) * 100, 100)}%` }} />
                  </div>
                </div>
             ))}
             {budgets.length === 0 && <p className="text-[13px] text-secondary">No active budgets found.</p>}
           </div>
        )}
      </Modal>
    </div>
  );
}
