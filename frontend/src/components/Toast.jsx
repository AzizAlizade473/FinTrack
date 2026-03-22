export default function Toast({ message, type = 'success', onClose }) {
  const isError = type === 'error';
  const isWarning = type === 'warning';
  
  let borderColor = 'border-green';
  let icon = <svg className="w-5 h-5 text-green" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" /></svg>;

  if (isError) {
    borderColor = 'border-red';
    icon = <svg className="w-5 h-5 text-red" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>;
  } else if (isWarning) {
    borderColor = 'border-[#f59e0b]';
    icon = <svg className="w-5 h-5 text-[#f59e0b]" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" /></svg>;
  }

  return (
    <div className={`fixed top-6 right-6 z-[100] bg-white border-l-4 ${borderColor} text-navy px-4 py-3 rounded-[12px] shadow-[0_8px_30px_rgb(0,0,0,0.12)] toast-enter flex items-center gap-3 w-[320px] max-w-[calc(100vw-48px)]`}>
      <div className="shrink-0 bg-[#f3f4f6] p-1.5 rounded-full">{icon}</div>
      <span className="text-[14px] font-medium flex-1">{message}</span>
      <button onClick={onClose} className="text-[#9ca3af] hover:text-navy text-xl">&times;</button>
    </div>
  );
}
