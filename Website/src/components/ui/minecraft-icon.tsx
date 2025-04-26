import React from 'react';
import { cn } from '@/lib/utils';
import { LucideIcon } from 'lucide-react';

interface MinecraftIconProps extends React.HTMLAttributes<HTMLDivElement> {
  icon: LucideIcon;
  size?: 'sm' | 'md' | 'lg';
  variant?: 'grass' | 'diamond' | 'gold' | 'iron' | 'stone';
}

const MinecraftIcon = ({
  icon: Icon,
  size = 'md',
  variant = 'grass',
  className,
  ...props
}: MinecraftIconProps) => {
  // Size classes
  const sizeClasses = {
    sm: "w-8 h-8 p-1.5",
    md: "w-10 h-10 p-2",
    lg: "w-12 h-12 p-2.5"
  };
  
  // Color classes based on variant
  const colorClasses = {
    grass: "text-minecraft-green",
    diamond: "text-minecraft-diamond",
    gold: "text-minecraft-gold",
    iron: "text-minecraft-iron",
    stone: "text-minecraft-stone"
  };
  
  // Border color classes based on variant
  const borderClasses = {
    grass: "border-minecraft-green/50",
    diamond: "border-minecraft-diamond/50",
    gold: "border-minecraft-gold/50",
    iron: "border-minecraft-iron/50",
    stone: "border-minecraft-stone/50"
  };
  
  return (
    <div 
      className={cn(
        "relative bg-black/40 border-2 flex items-center justify-center",
        sizeClasses[size],
        borderClasses[variant],
        className
      )}
      {...props}
    >
      {/* Corner pixels for Minecraft style */}
      <div className="absolute -top-0.5 -left-0.5 w-1 h-1 bg-white/20"></div>
      <div className="absolute -bottom-0.5 -right-0.5 w-1 h-1 bg-black/30"></div>
      
      <Icon className={cn("w-full h-full", colorClasses[variant])} />
    </div>
  );
};

export { MinecraftIcon };
