import { useContext } from 'react';
import { AuthContext } from '../App.jsx';

export default function PageHeader({ title, subtitle }) {
  const { user } = useContext(AuthContext);
  const initials = user?.name ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0,2) : 'U';

  return (
    <div className="flex items-center justify-between mb-8">
      <div>
        <h1 className="text-[28px] font-bold text-navy">{title}</h1>
        {subtitle && <p className="text-[14px] text-[#6b7280] mt-1">{subtitle}</p>}
      </div>
      <div className="flex items-center gap-3">
        <button className="w-10 h-10 rounded-full border border-border flex items-center justify-center text-[#6b7280] hover:bg-hover hover:text-navy transition-colors">
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9" /></svg>
        </button>
        <button className="w-10 h-10 rounded-full bg-navy flex items-center justify-center text-white font-semibold text-sm shadow-sm opacity-90 hover:opacity-100 transition-opacity">
          {initials}
        </button>
      </div>
    </div>
  );
}
