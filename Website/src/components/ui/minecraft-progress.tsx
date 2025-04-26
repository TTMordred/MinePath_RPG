
import React from 'react';
import { cn } from '@/lib/utils';

interface MinecraftProgressProps extends React.HTMLAttributes<HTMLDivElement> {
  value: number;
  max?: number;
  variant?: 'green' | 'blue' | 'red' | 'gold';
  height?: 'sm' | 'md' | 'lg';
  showValue?: boolean;
  animated?: boolean;
}

const MinecraftProgress = React.forwardRef<HTMLDivElement, MinecraftProgressProps>(
  ({ className, value, max = 100, variant = 'green', height = 'md', showValue = false, animated = false, ...props }, ref) => {
    // Calculate percentage
    const percentage = Math.min(100, Math.max(0, (value / max) * 100));
    
    // Height classes
    const heightClasses = {
      sm: "h-2",
      md: "h-4",
      lg: "h-6"
    };
    
    // Color classes for the progress bar
    const colorClasses = {
      green: "bg-minecraft-green",
      blue: "bg-minecraft-blue",
      red: "bg-red-500",
      gold: "bg-minecraft-gold"
    };
    
    return (
      <div 
        ref={ref}
        className={cn(
          "relative w-full bg-black/70 border-2 border-minecraft-dirt overflow-hidden",
          heightClasses[height],
          className
        )}
        {...props}
      >
        <div 
          className={cn(
            "h-full transition-all duration-300",
            colorClasses[variant],
            animated && "shimmer"
          )}
          style={{ width: `${percentage}%` }}
        >
          {/* Pixel pattern overlay */}
          <div className="absolute inset-0 opacity-20 pointer-events-none" 
            style={{
              backgroundImage: 'url("/images/pixel_pattern.png")',
              backgroundSize: '4px 4px',
              imageRendering: 'pixelated'
            }}
          />
        </div>
        
        {showValue && (
          <div className="absolute inset-0 flex items-center justify-center text-xs font-minecraft text-white">
            {value}/{max}
          </div>
        )}
        
        {/* Corner pixels for Minecraft style */}
        <div className="absolute top-0 left-0 w-1 h-1 bg-white/20"></div>
        <div className="absolute top-0 right-0 w-1 h-1 bg-white/20"></div>
        <div className="absolute bottom-0 left-0 w-1 h-1 bg-black/40"></div>
        <div className="absolute bottom-0 right-0 w-1 h-1 bg-black/40"></div>
      </div>
    );
  }
);

MinecraftProgress.displayName = "MinecraftProgress";

export { MinecraftProgress };
