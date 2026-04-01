import { useEffect, useState } from 'react';

export default function Modal({ isOpen, onClose, title, children }) {
  const [shouldRender, setShouldRender] = useState(false);

  useEffect(() => {
    if (isOpen) setShouldRender(true);
  }, [isOpen]);

  const onAnimationEnd = () => {
    if (!isOpen) setShouldRender(false);
  };

  if (!shouldRender && !isOpen) return null;

  return (
    <div 
      className={`fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm 
        ${isOpen ? 'modal-overlay-enter' : 'opacity-0 transition-opacity duration-200 pointer-events-none'}`} 
      onClick={onClose}
      onAnimationEnd={onAnimationEnd}
      onTransitionEnd={onAnimationEnd}
    >
      <div
        className={`bg-white rounded-[20px] shadow-2xl w-full max-w-[480px] p-8 m-4
        ${isOpen ? 'modal-content-enter' : 'scale-95 opacity-0 transition-all duration-200'}`}
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-8">
          <h2 className="text-[20px] font-bold text-navy">{title}</h2>
          <button 
            onClick={onClose} 
            className="text-[#9ca3af] hover:text-navy hover:bg-[#f3f4f6] w-8 h-8 rounded-full flex items-center justify-center transition-colors"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
          </button>
        </div>
        {children}
      </div>
    </div>
  );
}
