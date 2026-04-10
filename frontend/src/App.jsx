import { BrowserRouter, Routes, Route, Navigate, useLocation } from 'react-router-dom';
import { useState, useContext, createContext, useEffect } from 'react';
import Sidebar from './components/Sidebar.jsx';
import Toast from './components/Toast.jsx';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import DashboardPage from './pages/DashboardPage.jsx';
import TransactionsPage from './pages/TransactionsPage.jsx';
import BudgetsPage from './pages/BudgetsPage.jsx';
import ReportsPage from './pages/ReportsPage.jsx';
import SettingsPage from './pages/SettingsPage.jsx';

export const AuthContext = createContext(null);
export const ToastContext = createContext(null);

function ProtectedRoute({ children }) {
  const user = (() => {
    try { return JSON.parse(localStorage.getItem('user')); }
    catch { return null; }
  })();
  if (!user) return <Navigate to="/login" replace />;
  return children;
}

// Wrapper to trigger page transition animation on route change
function PageWrapper({ children }) {
  const location = useLocation();
  return (
    <div key={location.pathname} className="page-transition w-full max-w-7xl mx-auto">
      {children}
    </div>
  );
}

function AppLayout({ children }) {
  const [sidebarOpen, setSidebarOpen] = useState(false);
  const { user, logout } = useContext(AuthContext);

  return (
    <div className="flex min-h-screen bg-bg-page font-sans text-navy">
      <Sidebar open={sidebarOpen} onToggle={() => setSidebarOpen(!sidebarOpen)} />
      
      {/* Mobile sidebar overlay backdrop */}
      {sidebarOpen && (
        <div 
          className="fixed inset-0 bg-black/20 z-30 md:hidden"
          onClick={() => setSidebarOpen(false)}
        />
      )}

      <div className="flex-1 flex flex-col md:ml-[240px] min-w-0 transition-all duration-300">
        <header className="flex justify-between items-center bg-white/50 backdrop-blur-sm border-b border-border sticky top-0 z-20 px-6 py-4">
          <div className="flex items-center">
            {/* Mobile hamburger button */}
            <button 
              className="md:hidden p-2 -ml-2 mr-2 text-secondary rounded-lg hover:bg-hover active:scale-95 transition-all"
              onClick={() => setSidebarOpen(true)}
            >
              <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </button>
            <h2 className="text-xl font-bold text-navy hidden md:block">FinTrack</h2>
          </div>

          <div className="relative">
            <button 
              onClick={() => document.getElementById('profile-dropdown').classList.toggle('hidden')}
              className="w-10 h-10 rounded-full bg-purple/10 border border-purple/20 flex items-center justify-center cursor-pointer hover:bg-purple/20 transition-colors"
            >
              <span className="font-bold text-purple">{user?.name ? user.name.charAt(0).toUpperCase() : 'U'}</span>
            </button>
            
            <div id="profile-dropdown" className="hidden absolute right-0 mt-2 w-48 bg-white rounded-[12px] shadow-lg border border-border overflow-hidden z-50">
              <div className="px-4 py-3 border-b border-border bg-[#f8fafc]">
                 <p className="text-[14px] font-bold text-navy truncate">{user?.name}</p>
                 <p className="text-[12px] text-secondary truncate">{user?.email}</p>
              </div>
              <button 
                onClick={logout}
                className="w-full text-left px-4 py-2.5 text-[14px] font-medium text-red hover:bg-red/5 transition-colors flex items-center gap-2"
              >
                <svg className="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" /></svg>
                Log Out
              </button>
            </div>
          </div>
        </header>

        <main className="flex-1 p-6 lg:p-10 overflow-auto">
          <PageWrapper>
            {children}
          </PageWrapper>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  const [user, setUser] = useState(() => {
    try { return JSON.parse(localStorage.getItem('user')); }
    catch { return null; }
  });
  const [toast, setToast] = useState(null);

  const showToast = (message, type = 'success') => {
    setToast({ message, type });
    setTimeout(() => setToast(null), 3000);
  };

  const login = (userData) => {
    localStorage.setItem('user', JSON.stringify(userData));
    setUser(userData);
  };

  const logout = () => {
    localStorage.removeItem('user');
    setUser(null);
  };

  return (
    <AuthContext.Provider value={{ user, login, logout }}>
      <ToastContext.Provider value={showToast}>
        <BrowserRouter>
          <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />
            <Route path="/dashboard" element={<ProtectedRoute><AppLayout><DashboardPage /></AppLayout></ProtectedRoute>} />
            <Route path="/transactions" element={<ProtectedRoute><AppLayout><TransactionsPage /></AppLayout></ProtectedRoute>} />
            <Route path="/budgets" element={<ProtectedRoute><AppLayout><BudgetsPage /></AppLayout></ProtectedRoute>} />
            <Route path="/reports" element={<ProtectedRoute><AppLayout><ReportsPage /></AppLayout></ProtectedRoute>} />
            <Route path="/settings" element={<ProtectedRoute><AppLayout><SettingsPage /></AppLayout></ProtectedRoute>} />
            <Route path="*" element={<Navigate to={user ? "/dashboard" : "/login"} replace />} />
          </Routes>
          {toast && <Toast message={toast.message} type={toast.type} onClose={() => setToast(null)} />}
        </BrowserRouter>
      </ToastContext.Provider>
    </AuthContext.Provider>
  );
}
