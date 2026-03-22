import { useState, useContext } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { AuthContext, ToastContext } from '../App.jsx';
import { registerUser, loginUser } from '../api.js';

export default function RegisterPage() {
  const [name, setName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useContext(AuthContext);
  const showToast = useContext(ToastContext);
  const navigate = useNavigate();

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    try {
      await registerUser(name, email, password);
      // Auto-login after successful registration
      const res = await loginUser(email, password);
      if (res.data && res.data.user) {
        login(res.data.user);
        showToast('Account created! Welcome!', 'success');
        navigate('/dashboard');
      } else {
        showToast('Registered! Please sign in.', 'success');
        navigate('/login');
      }
    } catch (err) {
      showToast(err.response?.data?.message || 'Registration failed', 'error');
    }
    setLoading(false);
  };

  return (
    <div className="min-h-[100dvh] bg-bg-page flex items-center justify-center p-4 page-transition font-sans">
      <div className="w-full max-w-[400px]">
        <div className="text-center mb-8">
          <div className="inline-flex items-center justify-center w-14 h-14 rounded-[14px] bg-white shadow-sm border border-border mb-4">
            <svg className="w-8 h-8 text-navy" viewBox="0 0 24 24" fill="currentColor">
              <path d="M4 4h4v4H4zM10 4h10v4H10zM4 10h4v4H4zM10 10h10v4H10zM4 16h4v4H4zM10 16h10v4H10z" />
            </svg>
          </div>
          <h1 className="text-[28px] font-bold text-navy tracking-tight">Create an account</h1>
          <p className="text-[#6b7280] text-[14px] mt-1">Start tracking your finances today</p>
        </div>

        <div className="bg-white rounded-[20px] border border-border p-8 shadow-[0_8px_30px_rgb(0,0,0,0.04)]">
          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="block text-[13px] font-medium text-navy mb-1.5">Full name</label>
              <input
                type="text"
                value={name}
                onChange={(e) => setName(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]"
                placeholder="John Doe"
              />
            </div>
            <div>
              <label className="block text-[13px] font-medium text-navy mb-1.5">Email address</label>
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]"
                placeholder="you@example.com"
              />
            </div>
            <div>
              <label className="block text-[13px] font-medium text-navy mb-1.5">Password</label>
              <input
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                required
                className="w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]"
                placeholder="••••••••"
              />
            </div>
            <button
              type="submit"
              disabled={loading}
              className="w-full py-3 rounded-[10px] bg-navy text-white text-[14px] font-semibold hover:bg-opacity-90 active:scale-[0.98] transition-all disabled:opacity-50 mt-2 shadow-sm"
            >
              {loading ? 'Creating account...' : 'Sign Up'}
            </button>
          </form>
          <div className="mt-6 text-center text-[14px] text-[#6b7280]">
            Already have an account?{' '}
            <Link to="/login" className="text-purple hover:text-navy font-semibold transition-colors">Sign in</Link>
          </div>
        </div>
      </div>
    </div>
  );
}
