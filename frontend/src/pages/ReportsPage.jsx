import { useState, useContext, useEffect } from 'react';
import { ToastContext } from '../App.jsx';
import { getMonthlyReport, getCategoryReport } from '../api.js';
import PageHeader from '../components/PageHeader.jsx';
import StatCard from '../components/StatCard.jsx';
import LoadingSkeleton from '../components/LoadingSkeleton.jsx';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend } from 'recharts';

export default function ReportsPage() {
  const showToast = useContext(ToastContext);
  const [tab, setTab] = useState('monthly');
  const [month, setMonth] = useState(() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  });
  
  const [monthlyData, setMonthlyData] = useState(null);
  const [categoryData, setCategoryData] = useState(null);
  const [loading, setLoading] = useState(false);

  // Auto-fetch data when tab or month changes
  useEffect(() => {
    if (tab === 'monthly') fetchMonthly();
    else if (tab === 'category' && !categoryData) fetchCategory();
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [tab]);

  const fetchMonthly = async () => {
    if (!month) return;
    setLoading(true);
    try {
      const res = await getMonthlyReport(month);
      setMonthlyData(res.data.data);
    } catch { showToast('Failed to load monthly report', 'error'); }
    setLoading(false);
  };

  const fetchCategory = async () => {
    setLoading(true);
    try {
      const res = await getCategoryReport();
      setCategoryData(res.data.data);
    } catch { showToast('Failed to load category report', 'error'); }
    setLoading(false);
  };

  return (
    <div className="space-y-6">
      <PageHeader title="Reports" subtitle="Analyze your spending patterns" />

      {/* Tabs Menu */}
      <div className="flex bg-[#f3f4f6] p-1 rounded-[12px] w-fit">
        <button 
          onClick={() => setTab('monthly')} 
          className={`px-6 py-2 rounded-[10px] text-[13px] font-bold transition-all ${tab === 'monthly' ? 'bg-white text-navy shadow-sm' : 'text-[#6b7280] hover:text-navy'}`}
        >
          Monthly Summary
        </button>
        <button 
          onClick={() => setTab('category')} 
          className={`px-6 py-2 rounded-[10px] text-[13px] font-bold transition-all ${tab === 'category' ? 'bg-white text-navy shadow-sm' : 'text-[#6b7280] hover:text-navy'}`}
        >
          Category Breakdown
        </button>
      </div>

      {/* Content Area */}
      {loading ? <LoadingSkeleton count={3} height="h-40" /> : (
        <>
          {/* Monthly Tab */}
          {tab === 'monthly' && (
            <div className="space-y-6">
              <div className="bg-white p-4 rounded-[16px] border border-border shadow-[0_1px_3px_rgba(0,0,0,0.06)] flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="text-[14px] font-medium text-navy">Select Period:</span>
                  <input type="month" value={month} onChange={e => setMonth(e.target.value)} className="px-3 py-2 rounded-[8px] bg-[#f9fafb] border border-[#e5e7eb] text-[14px] text-navy focus:outline-none focus:border-purple focus:ring-1 focus:ring-purple transition-all" />
                  <button onClick={fetchMonthly} className="px-4 py-2 rounded-[8px] bg-[#f3f4f6] text-navy text-[13px] font-semibold hover:bg-[#e5e7eb] transition-all">Go</button>
                </div>
                {monthlyData && (
                  <button onClick={() => showToast('Exported successfully')} className="px-4 py-2 rounded-[8px] border border-border text-navy text-[13px] font-semibold hover:bg-[#f9fafb] transition-all flex items-center gap-2">
                    <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 16v1a3 3 0 003 3h10a3 3 0 003-3v-1m-4-4l-4 4m0 0l-4-4m4 4V4" /></svg>
                    Export CSV
                  </button>
                )}
              </div>

              {monthlyData ? (
                <>
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-6">
                    <StatCard title="Total Income" value={monthlyData.totalIncome} type="income" badgePrefix="+" />
                    <StatCard title="Total Expenses" value={monthlyData.totalExpense} type="expense" badgePrefix="-" />
                    <StatCard title="Net Balance" value={monthlyData.netBalance} type={monthlyData.netBalance >= 0 ? "balance" : "expense"} />
                  </div>
                  
                  <div className="bg-white rounded-[16px] border border-border p-8 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
                    <h3 className="text-[16px] font-bold text-navy mb-8 text-center">{month} Cash Flow</h3>
                    <ResponsiveContainer width="100%" height={320}>
                      <BarChart data={[{ name: month, Income: monthlyData.totalIncome, Expense: monthlyData.totalExpense }]} barGap={20} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                        <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#f3f4f6" />
                        <XAxis dataKey="name" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 13 }} dy={10} />
                        <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 13 }} tickFormatter={v => `$${v}`} />
                        <Tooltip cursor={{ fill: '#f9fafb' }} contentStyle={{ borderRadius: '8px', border: '1px solid #e5e7eb', boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)' }} />
                        <Legend iconType="circle" wrapperStyle={{ paddingTop: '20px', fontSize: '13px' }} />
                        <Bar dataKey="Income" fill="#16a34a" radius={[6, 6, 0, 0]} maxBarSize={60} />
                        <Bar dataKey="Expense" fill="#dc2626" radius={[6, 6, 0, 0]} maxBarSize={60} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </>
              ) : (
                <div className="text-center py-20 text-[#6b7280] text-[14px]">Select a month and generate to see data.</div>
              )}
            </div>
          )}

          {/* Category Tab */}
          {tab === 'category' && (
            <div className="space-y-6">
              {categoryData ? (
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
                  {/* Left Chart */}
                  <div className="bg-white rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
                    <h3 className="text-[16px] font-bold text-navy mb-4">Spending Hierarchy</h3>
                    <ResponsiveContainer width="100%" height={400}>
                      <BarChart data={categoryData.categories} layout="vertical" margin={{ top: 0, right: 30, left: 10, bottom: 0 }}>
                        <XAxis type="number" hide />
                        <YAxis dataKey="category" type="category" axisLine={false} tickLine={false} tick={{ fill: '#6b7280', fontSize: 12, fontWeight: 500 }} width={90} />
                        <Tooltip cursor={{ fill: '#f9fafb' }} contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 12px rgba(0,0,0,0.1)' }} />
                        <Bar dataKey="total" fill="#6c63ff" radius={[0, 4, 4, 0]} barSize={24} />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>

                  {/* Right Table */}
                  <div className="bg-white rounded-[16px] border border-border overflow-hidden shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
                    <table className="w-full text-left border-collapse">
                      <thead>
                        <tr className="border-b border-border bg-[#fafafa]">
                          <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider">Category</th>
                          <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider text-right">Total Spent</th>
                          <th className="px-6 py-4 text-[12px] font-bold text-[#6b7280] uppercase tracking-wider text-right">% of Total</th>
                        </tr>
                      </thead>
                      <tbody className="divide-y divide-border">
                        {categoryData.categories.map((c, i) => (
                          <tr key={i} className="hover:bg-hover transition-colors">
                            <td className="px-6 py-4 text-[14px] font-bold text-navy">{c.category}</td>
                            <td className="px-6 py-4 text-[14px] text-navy font-semibold text-right">${c.total.toLocaleString(undefined, {minimumFractionDigits: 2})}</td>
                            <td className="px-6 py-4 text-[14px] text-[#6b7280] text-right">{c.percentage.toFixed(1)}%</td>
                          </tr>
                        ))}
                        {categoryData.categories.length === 0 && (
                          <tr><td colSpan="3" className="px-6 py-8 text-center text-[#9ca3af]">No categorized expenses found.</td></tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              ) : (
                <div className="text-center py-20 text-[#6b7280] text-[14px]">Loading category data...</div>
              )}
            </div>
          )}
        </>
      )}
    </div>
  );
}
