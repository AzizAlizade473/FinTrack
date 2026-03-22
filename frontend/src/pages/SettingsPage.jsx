import { useState, useContext } from 'react';
import { AuthContext, ToastContext } from '../App.jsx';
import PageHeader from '../components/PageHeader.jsx';

export default function SettingsPage() {
  const { user, login, logout } = useContext(AuthContext);
  const showToast = useContext(ToastContext);
  
  const [name, setName] = useState(user?.name || '');
  const [email, setEmail] = useState(user?.email || '');
  
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [notifications, setNotifications] = useState(true);
  const [currency, setCurrency] = useState('USD');

  const handleSaveProfile = () => {
    const updated = { ...user, name, email };
    login(updated);
    showToast('Profile updated successfully!', 'success');
  };

  const handleChangePassword = () => {
    if (!currentPassword || !newPassword || !confirmPassword) {
      showToast('Fill in all password fields', 'warning'); return;
    }
    if (newPassword !== confirmPassword) {
      showToast('Passwords do not match', 'error'); return;
    }
    showToast('Password changed successfully!', 'success');
    setCurrentPassword(''); setNewPassword(''); setConfirmPassword('');
  };

  const handleDeleteAccount = () => {
    if (confirm('Are you sure you want to delete your account? This cannot be undone.')) {
      logout();
      showToast('Account deleted', 'warning');
    }
  };

  // Password strength logic
  const getStrength = (pw) => {
    let strength = 0;
    if (pw.length > 5) strength += 1;
    if (pw.length > 8) strength += 1;
    if (/[A-Z]/.test(pw)) strength += 1;
    if (/[0-9]/.test(pw)) strength += 1;
    return strength;
  };
  const strengthVal = getStrength(newPassword);
  
  const inputClass = "w-full px-4 py-3 rounded-[10px] bg-white border border-border text-navy placeholder-[#9ca3af] focus:border-purple focus:ring-2 focus:ring-purple/20 focus:outline-none transition-all text-[14px]";

  const initials = user?.name ? user.name.split(' ').map(n => n[0]).join('').toUpperCase().substring(0,2) : 'U';

  return (
    <div className="space-y-6">
      <PageHeader title="Settings" subtitle="Manage your account preferences" />

      <div className="flex flex-col lg:flex-row gap-8">
        
        {/* Left Navigator (Desktop only) */}
        <div className="hidden lg:flex w-64 flex-col gap-1 shrink-0">
          {['Profile', 'Security', 'Preferences', 'Danger Zone'].map((item, idx) => (
             <a href={`#section-${idx}`} key={idx} className="px-4 py-3 rounded-[10px] text-[14px] font-bold text-[#6b7280] hover:bg-[#f9fafb] hover:text-navy transition-colors">
               {item}
             </a>
          ))}
        </div>

        {/* Content Panel */}
        <div className="flex-1 space-y-8 max-w-3xl">
          
          {/* Profile Section */}
          <section id="section-0" className="bg-white rounded-[16px] border border-border p-8 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
            <h2 className="text-[18px] font-bold text-navy mb-6">Profile</h2>
            <div className="flex items-center gap-6 mb-8">
              <div className="w-16 h-16 rounded-full bg-navy flex items-center justify-center text-white text-[24px] font-bold shadow-sm">
                {initials}
              </div>
              <div>
                <button className="px-4 py-2 rounded-[8px] border border-border text-[13px] font-semibold text-navy hover:bg-[#f9fafb] transition-all mb-1">Upload new avatar</button>
                <p className="text-[12px] text-[#6b7280]">JPG, GIF or PNG. Max size of 800K</p>
              </div>
            </div>
            <div className="space-y-4">
              <div>
                <label className="block text-[13px] font-medium text-navy mb-1.5">Full name</label>
                <input value={name} onChange={e => setName(e.target.value)} className={inputClass} />
              </div>
              <div>
                <label className="block text-[13px] font-medium text-navy mb-1.5">Email address</label>
                <input type="email" value={email} onChange={e => setEmail(e.target.value)} className={inputClass} />
              </div>
              <div className="pt-2">
                <button onClick={handleSaveProfile} className="px-6 py-2.5 rounded-[10px] bg-navy text-white text-[13px] font-semibold hover:bg-opacity-90 active:scale-[0.98] transition-all">Save Changes</button>
              </div>
            </div>
          </section>

          {/* Security Section */}
          <section id="section-1" className="bg-white rounded-[16px] border border-border p-8 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
            <h2 className="text-[18px] font-bold text-navy mb-6">Security</h2>
            <div className="space-y-4 max-w-md">
              <div>
                <label className="block text-[13px] font-medium text-navy mb-1.5">Current Password</label>
                <input type="password" value={currentPassword} onChange={e => setCurrentPassword(e.target.value)} className={inputClass} />
              </div>
              <div>
                <label className="block text-[13px] font-medium text-navy mb-1.5">New Password</label>
                <input type="password" value={newPassword} onChange={e => setNewPassword(e.target.value)} className={inputClass} />
                
                {/* Strength Meter */}
                {newPassword.length > 0 && (
                  <div className="mt-2 flex gap-1 h-1.5">
                    {[1,2,3,4].map(i => (
                      <div key={i} className={`flex-1 rounded-full transition-colors duration-300 ${i <= strengthVal ? (strengthVal < 3 ? 'bg-[#f59e0b]' : 'bg-green') : 'bg-[#e5e7eb]'}`} />
                    ))}
                  </div>
                )}
              </div>
              <div>
                <label className="block text-[13px] font-medium text-navy mb-1.5">Confirm New Password</label>
                <input type="password" value={confirmPassword} onChange={e => setConfirmPassword(e.target.value)} className={inputClass} />
              </div>
              <div className="pt-2">
                <button onClick={handleChangePassword} className="px-6 py-2.5 rounded-[10px] bg-white border border-border text-navy text-[13px] font-semibold hover:bg-[#f9fafb] active:scale-[0.98] transition-all">Update Password</button>
              </div>
            </div>
          </section>

          {/* Preferences Section */}
          <section id="section-2" className="bg-white rounded-[16px] border border-border p-8 shadow-[0_1px_3px_rgba(0,0,0,0.06)]">
            <h2 className="text-[18px] font-bold text-navy mb-6">Preferences</h2>
            
            <div className="flex items-center justify-between py-4 border-b border-border">
              <div>
                <h4 className="text-[14px] font-bold text-navy">Email Notifications</h4>
                <p className="text-[12px] text-[#6b7280]">Receive budget alerts and weekly reports</p>
              </div>
              <button 
                onClick={() => setNotifications(!notifications)}
                className={`relative w-11 h-6 rounded-full transition-colors ${notifications ? 'bg-green' : 'bg-[#d1d5db]'}`}
              >
                <span className={`absolute top-1 left-1 bg-white w-4 h-4 rounded-full transition-transform ${notifications ? 'translate-x-5' : ''}`} />
              </button>
            </div>

            <div className="flex items-center justify-between py-4 border-b border-border">
              <div>
                <h4 className="text-[14px] font-bold text-navy">Currency Preference</h4>
                <p className="text-[12px] text-[#6b7280]">Setting display currency</p>
              </div>
              <select value={currency} onChange={e => setCurrency(e.target.value)} className="px-3 py-2 rounded-[8px] bg-[#f9fafb] border border-[#e5e7eb] text-[14px] text-navy focus:outline-none focus:border-purple focus:ring-1 focus:ring-purple transition-all">
                <option value="USD">USD ($)</option>
                <option value="EUR">EUR (€)</option>
                <option value="GBP">GBP (£)</option>
                <option value="AZN">AZN (₼)</option>
              </select>
            </div>

            <div className="flex items-center justify-between py-4">
              <div>
                <h4 className="text-[14px] font-bold text-navy">Date Format</h4>
                <p className="text-[12px] text-[#6b7280]">How dates are displayed</p>
              </div>
              <p className="text-[14px] font-medium text-navy">YYYY-MM-DD</p>
            </div>
          </section>

          {/* Danger Zone */}
          <section id="section-3" className="bg-white rounded-[16px] border border-red/30 p-8">
            <h2 className="text-[18px] font-bold text-red mb-2">Danger Zone</h2>
            <p className="text-[13px] text-[#6b7280] mb-6">Permanently remove your account and all of your data from the FinTrack platform. This action is not reversible.</p>
            <button onClick={handleDeleteAccount} className="px-6 py-2.5 rounded-[10px] bg-[#fff5f5] text-red border border-red/20 text-[13px] font-semibold hover:bg-red hover:text-white transition-colors">Delete Account</button>
          </section>
        </div>
      </div>
    </div>
  );
}
