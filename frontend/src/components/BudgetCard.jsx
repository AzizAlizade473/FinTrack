import { Link } from 'react-router-dom';

export default function BudgetCard({ budget }) {
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
    <Link to={`/transactions?category=${encodeURIComponent(budget.category)}`} className={`block rounded-[16px] p-6 border transition-all duration-200 relative ${cardContainerClass} cursor-pointer`}>
      {exceeded && (
        <span className="absolute top-4 right-4 bg-red/10 text-red text-[11px] font-bold px-2 py-0.5 rounded-md uppercase tracking-wider">
          Exceeded
        </span>
      )}
      <div className="flex items-center gap-3 mb-6">
        <div className={`p-2 rounded-[10px] ${exceeded ? 'bg-red/10 text-red' : 'bg-purple/10 text-purple'}`}>
          <svg className="w-5 h-5" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 11H5m14 0a2 2 0 012 2v6a2 2 0 01-2 2H5a2 2 0 01-2-2v-6a2 2 0 012-2m14 0V9a2 2 0 00-2-2M5 11V9a2 2 0 012-2m0 0V5a2 2 0 012-2h6a2 2 0 012 2v2M7 7h10" /></svg>
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
