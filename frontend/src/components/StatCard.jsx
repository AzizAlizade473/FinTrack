import { useState, useEffect } from 'react';

function useCountUp(end, duration = 1000) {
  const [count, setCount] = useState(0);

  useEffect(() => {
    let startTimestamp = null;
    const step = (timestamp) => {
      if (!startTimestamp) startTimestamp = timestamp;
      const progress = Math.min((timestamp - startTimestamp) / duration, 1);
      // easeOutExpo
      const easeProgress = progress === 1 ? 1 : 1 - Math.pow(2, -10 * progress);
      setCount(easeProgress * end);
      if (progress < 1) {
        window.requestAnimationFrame(step);
      }
    };
    window.requestAnimationFrame(step);
  }, [end, duration]);

  return count;
}

export default function StatCard({ title, value, type, isCurrency = true, badgePrefix = '', onClick }) {
  const isBudget = type === 'budgets';
  const displayValue = useCountUp(value);
  
  let bgClass = "bg-[#f3f4f6] text-[#6b7280]";
  let icon = null;

  if (type === 'balance') {
    bgClass = "bg-[#e0e7ff] text-navy";
    icon = <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>;
  } else if (type === 'income') {
    bgClass = "bg-[#dcfce7] text-green";
    icon = <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" /></svg>;
  } else if (type === 'expense') {
    bgClass = "bg-[#fee2e2] text-red";
    icon = <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 17h8m0 0v-8m0 8l-8-8-4 4-6-6" /></svg>;
  } else if (type === 'budgets') {
    bgClass = "bg-[#ede9fe] text-purple";
    icon = <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" /></svg>;
  }

  return (
    <div 
        className={`bg-bg-card rounded-[16px] border border-border p-6 shadow-[0_1px_3px_rgba(0,0,0,0.06)] hover:-translate-y-1 hover:shadow-md transition-all duration-200 ease-in-out ${onClick ? 'cursor-pointer' : ''}`}
        onClick={onClick}
    >
      <div className="flex items-center justify-between mb-4">
        <span className="text-[12px] font-semibold text-[#6b7280] uppercase tracking-wider">{title}</span>
        <div className={`p-2 rounded-[10px] ${bgClass}`}>
          {icon}
        </div>
      </div>
      <h3 className="text-[28px] font-bold text-navy">
        {badgePrefix}{isCurrency ? '$' : ''}{displayValue.toLocaleString('en-US', { minimumFractionDigits: isCurrency ? 2 : 0, maximumFractionDigits: isCurrency ? 2 : 0 })}
      </h3>
    </div>
  );
}
