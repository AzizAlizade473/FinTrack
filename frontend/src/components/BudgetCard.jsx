import { Link } from 'react-router-dom';

const getCategoryIcon = (category) => {
  const catL = category.toLowerCase();
  if (catL.includes('food') || catL.includes('grocer') || catL.includes('eat')) {
    return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" /></svg>;
  } else if (catL.includes('hous') || catL.includes('rent') || catL.includes('mortgage')) {
    return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" /></svg>;
  } else if (catL.includes('transport') || catL.includes('car') || catL.includes('gas') || catL.includes('uber')) {
    return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>;
  } else if (catL.includes('health') || catL.includes('medical') || catL.includes('fitness')) {
    return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" /></svg>;
  } else if (catL.includes('entertainment') || catL.includes('fun') || catL.includes('netflix')) {
    return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M14.752 11.168l-3.197-2.132A1 1 0 0010 9.87v4.263a1 1 0 001.555.832l3.197-2.132a1 1 0 000-1.664z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12a9 9 0 11-18 0 9 9 0 0118 0z" /></svg>;
  }
  return <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>;
};

export default function BudgetCard({ budget, onDelete }) {
  const pct = budget.limit > 0 ? Math.min((budget.spent / budget.limit) * 100, 100) : 0;
  const exceeded = budget.exceeded;
  
  // Progress bar colors based on limits
  let fillClass = "bg-green";
  if (pct >= 80 && pct < 100) fillClass = "bg-[#f59e0b]";
  if (exceeded) fillClass = "bg-red";

  const cardContainerClass = exceeded 
    ? "bg-[#fff5f5] border-red/30 shadow-[0_4px_12px_rgba(220,38,38,0.1)]" 
    : "bg-bg-card border-border hover:-translate-y-1 shadow-[0_1px_3px_rgba(0,0,0,0.06)] hover:shadow-md";

  return (
    <Link to={`/transactions?category=${encodeURIComponent(budget.category)}`} className={`group block rounded-[16px] p-6 border transition-all duration-200 relative ${cardContainerClass} cursor-pointer`}>
      {exceeded && (
        <span className="absolute top-4 right-12 bg-red/10 text-red text-[11px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider">
          Exceeded
        </span>
      )}
      <button 
        className="absolute top-3 right-3 text-[#9ca3af] hover:text-red opacity-0 group-hover:opacity-100 transition-all p-1.5 rounded-md hover:bg-red/10 cursor-pointer" 
        onClick={(e) => { e.preventDefault(); e.stopPropagation(); if (onDelete) onDelete(budget.category); }}
        title="Delete Budget"
      >
        <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" /></svg>
      </button>
      
      <div className="flex items-center gap-3 mb-6">
        <div className={`p-2 rounded-[10px] ${exceeded ? 'bg-red/10 text-red' : 'bg-purple/10 text-purple'}`}>
          {getCategoryIcon(budget.category)}
        </div>
        <h3 className="text-[16px] font-bold text-navy">{budget.category}</h3>
      </div>
      
      <div className="w-full h-2 bg-[#f3f4f6] rounded-full overflow-hidden mb-2 relative">
        <div
          className={`h-full rounded-full transition-all duration-700 ease-out delay-100 w-0 ${fillClass}`}
          style={{ width: `${Math.min(pct, 100)}%` }}
        />
      </div>
      
      <div className="flex items-center justify-between text-[13px] mt-3">
        <span className="text-[#6b7280]">
          <span className={`font-semibold ${exceeded ? 'text-red' : 'text-navy'}`}>${budget.spent.toFixed(2)}</span> spent of ${budget.limit.toFixed(2)}
        </span>
        <span className={`font-semibold ${exceeded ? 'text-red' : (pct >= 80 ? 'text-[#f59e0b]' : 'text-green')}`}>
          {pct.toFixed(0)}%
        </span>
      </div>
    </Link>
  );
}
