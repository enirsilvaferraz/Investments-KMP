/**
 * @license
 * SPDX-License-Identifier: Apache-2.0
 */

import { ReactNode } from 'react';
import { motion } from 'motion/react';

interface MD3CardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: ReactNode;
  variant?: 'elevated' | 'filled' | 'outlined' | 'error' | 'tertiary';
  className?: string;
}

export default function MD3Card({ 
  title, 
  value, 
  subtitle, 
  icon, 
  variant = 'elevated',
  className = '' 
}: MD3CardProps) {
  
  // Material 3 Card themes
  const variantStyles = {
    elevated: 'bg-white shadow-md hover:shadow-lg hover:translate-y-[-2px] border border-[#ebedf2] text-[#1a1c1e]',
    filled: 'bg-[#f1f3f8] hover:bg-[#ebedf2] border border-[#dfe2e7] text-[#1a1c1e]',
    outlined: 'bg-white border-2 border-[#c3c7cf] hover:border-[#73777f] text-[#1a1c1e]',
    error: 'bg-[#ffdad6] text-[#410002] border border-[#ffb4ab] hover:bg-[#ffecea]',
    tertiary: 'bg-[#f2daff] text-[#251431] border border-[#e8c3fc] hover:bg-[#f6e6ff]'
  };

  return (
    <motion.div
      whileHover={{ scale: 1.02 }}
      transition={{ type: 'spring', stiffness: 400, damping: 25 }}
      className={`p-4 rounded-[24px] transition-all duration-200 ${variantStyles[variant]} ${className}`}
    >
      <div className="flex items-start justify-between">
        <p className="text-xs font-display font-medium text-[#535f70] select-none block uppercase tracking-wide">
          {title}
        </p>
        {icon && <div className="text-[#535f70]/80">{icon}</div>}
      </div>
      
      <div className="mt-2.5">
        <h4 className="text-xl font-mono font-bold tracking-tight text-inherit">
          {value}
        </h4>
        {subtitle && (
          <p className="text-[11px] font-sans mt-0.5 text-[#43474e] flex items-center gap-1">
            {subtitle}
          </p>
        )}
      </div>
    </motion.div>
  );
}
