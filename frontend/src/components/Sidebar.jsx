import { NavLink } from 'react-router-dom';
import { useContext } from 'react';
import { AuthContext } from '../App.jsx';

const links = [
  { 
    to: '/dashboard', label: 'Dashboard', 
    icon: <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2V6zM14 6a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2V6zM4 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2H6a2 2 0 01-2-2v-2zM14 16a2 2 0 012-2h2a2 2 0 012 2v2a2 2 0 01-2 2h-2a2 2 0 01-2-2v-2z" /></svg>
  },
  { 
    to: '/transactions', label: 'Transactions', 
    icon: <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7h12m0 0l-4-4m4 4l-4 4m0 6H4m0 0l4 4m-4-4l4-4" /></svg>
  },
  { 
    to: '/budgets', label: 'Budgets', 
    icon: <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z" /></svg>
  },
  { 
    to: '/reports', label: 'Reports', 
    icon: <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" /></svg>
  },
  { 
    to: '/settings', label: 'Settings', 
    icon: <svg className="w-[18px] h-[18px]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10.325 4.317c.426-1.756 2.924-1.756 3.35 0a1.724 1.724 0 002.573 1.066c1.543-.94 3.31.826 2.37 2.37a1.724 1.724 0 001.065 2.572c1.756.426 1.756 2.924 0 3.35a1.724 1.724 0 00-1.066 2.573c.94 1.543-.826 3.31-2.37 2.37a1.724 1.724 0 00-2.572 1.065c-.426 1.756-2.924 1.756-3.35 0a1.724 1.724 0 00-2.573-1.066c-1.543.94-3.31-.826-2.37-2.37a1.724 1.724 0 00-1.065-2.572c-1.756-.426-1.756-2.924 0-3.35a1.724 1.724 0 001.066-2.573c-.94-1.543.826-3.31 2.37-2.37.996.608 2.296.07 2.572-1.065z" /><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" /></svg>
  },
];

export default function Sidebar({ open, onToggle }) {
  const { user, logout } = useContext(AuthContext);
  const initials = user?.name ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0,2) : 'U';

  return (
    <aside
      className={`fixed top-0 left-0 h-full bg-bg-sidebar border-r border-border z-40 flex flex-col transition-transform duration-300 ease-in-out w-[240px] shadow-sm
      ${open ? 'translate-x-0' : '-translate-x-full md:translate-x-0'}`}
    >
      <div className="flex items-center gap-3 px-6 py-8">
        <svg className="w-7 h-7 text-navy" viewBox="0 0 24 24" fill="currentColor">
          <path d="M4 4h4v4H4zM10 4h10v4H10zM4 10h4v4H4zM10 10h10v4H10zM4 16h4v4H4zM10 16h10v4H10z" />
        </svg>
        <div className="flex flex-col">
          <h1 className="text-[18px] font-bold text-navy leading-tight">FinTrack</h1>
          <span className="text-[11px] text-[#9ca3af] font-medium tracking-wide uppercase mt-0.5">Personal Finance</span>
        </div>
      </div>

      <nav className="flex-1 py-2 flex flex-col gap-1.5 px-4 w-full h-[calc(100vh-200px)] overflow-y-auto">
        {links.map((link) => (
          <NavLink
            key={link.to}
            to={link.to}
            onClick={() => { if(window.innerWidth < 768) onToggle(); }}
            className={({ isActive }) =>
              `group relative flex items-center gap-3 px-3 h-10 rounded-[10px] transition-all duration-150 text-[14px] font-medium
              ${isActive
                ? 'bg-[#f3f4f6] text-navy font-semibold'
                : 'text-[#6b7280] hover:bg-[#f9fafb] hover:text-navy'
              }`
            }
          >
            {({ isActive }) => (
              <>
                {isActive && <div className="absolute left-0 top-1/2 -translate-y-1/2 w-[3px] h-6 bg-purple rounded-r-md"></div>}
                <span className={`transition-colors ${isActive ? "text-navy" : "text-[#6b7280] group-hover:text-navy"}`}>
                  {link.icon}
                </span>
                <span>{link.label}</span>
              </>
            )}
          </NavLink>
        ))}
      </nav>

      <div className="p-4 border-t border-border mt-auto w-full">
        <div className="flex items-center justify-between">
          <div className="flex items-center gap-3 min-w-0 pr-2">
            <div className="w-9 h-9 rounded-full bg-navy flex items-center justify-center text-white font-semibold text-xs shrink-0 shadow-sm">
              {initials}
            </div>
            <div className="min-w-0 flex-1">
              <p className="text-[14px] font-bold text-navy truncate leading-tight">{user?.name}</p>
              <p className="text-[12px] text-[#9ca3af] truncate">{user?.email}</p>
            </div>
          </div>
          <button 
            onClick={logout}
            className="p-1.5 text-[#9ca3af] hover:text-red hover:bg-red/10 rounded-md transition-colors shrink-0"
            title="Logout"
          >
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
          </button>
        </div>
      </div>
    </aside>
  );
}
