import React from 'react';

export const Pickaxe = ({ className, size = 24, ...props }: React.SVGProps<SVGSVGElement> & { size?: number }) => {
  return (
    <svg 
      xmlns="http://www.w3.org/2000/svg" 
      width={size} 
      height={size} 
      viewBox="0 0 24 24" 
      fill="none" 
      stroke="currentColor" 
      strokeWidth="2" 
      strokeLinecap="round" 
      strokeLinejoin="round" 
      className={className}
      {...props}
    >
      <path d="M14 10L3.5 20.5" />
      <path d="M16 3l-4.5 11 5.5 5.5 11-4.5V3h-12z" />
    </svg>
  );
};
