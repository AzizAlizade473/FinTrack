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
  const user = JSON.parse(localStorage.getItem('user'));
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
        <main className="flex-1 p-6 lg:p-10 overflow-auto">
          {/* Mobile hamburger button */}
          <button 
            className="md:hidden p-2 -ml-2 mb-4 text-secondary rounded-lg hover:bg-hover active:scale-95 transition-all"
            onClick={() => setSidebarOpen(true)}
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
            </svg>
          </button>
          
          <PageWrapper>
            {children}
          </PageWrapper>
        </main>
      </div>
    </div>
  );
}

export default function App() {
  const [user, setUser] = useState(() => JSON.parse(localStorage.getItem('user')));
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
