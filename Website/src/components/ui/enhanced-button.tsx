
import React from 'react';
import { cn } from '@/lib/utils';
import { Button, ButtonProps } from '@/components/ui/button';

interface EnhancedButtonProps extends ButtonProps {
  glowing?: boolean;
  hoverable?: boolean;
  minecraftStyle?: 'grass' | 'diamond' | 'gold' | 'stone';
}

const EnhancedButton = React.forwardRef<HTMLButtonElement, EnhancedButtonProps>(
  ({ className, variant, glowing = false, hoverable = false, minecraftStyle, children, ...props }, ref) => {
    // Minecraft style classes
    const minecraftClasses = {
      grass: "bg-minecraft-green border-b-4 border-minecraft-green/70 text-white hover:bg-minecraft-green/90",
      diamond: "bg-minecraft-diamond border-b-4 border-minecraft-diamond/70 text-white hover:bg-minecraft-diamond/90", 
      gold: "bg-minecraft-gold border-b-4 border-minecraft-gold/70 text-white hover:bg-minecraft-gold/90",
      stone: "bg-minecraft-stone border-b-4 border-minecraft-stone/70 text-white hover:bg-minecraft-stone/90"
    };
    
    return (
      <Button
        ref={ref}
        variant={variant}
        className={cn(
          "relative overflow-hidden transition-all duration-300 font-minecraft",
          glowing && "shadow-[0_0_15px_rgba(155,135,245,0.4)] hover:shadow-[0_0_20px_rgba(155,135,245,0.6)]",
          hoverable && "hover:scale-105",
          minecraftStyle && minecraftClasses[minecraftStyle],
          minecraftStyle && "border-2 border-t-0 border-l-0 border-r-4 py-2 px-4 rounded-none",
          variant === 'default' && "bg-minecraft-green hover:bg-minecraft-green/90",
          className
        )}
        {...props}
      >
        {hoverable && (
          <span className="absolute inset-0 bg-gradient-to-r from-white/0 via-white/10 to-white/0 bg-[length:500%_100%] animate-shimmer pointer-events-none"></span>
        )}
        {children}
        
        {/* Pixelated border overlay for Minecraft effect */}
        {minecraftStyle && (
          <span className="absolute inset-0 border-2 border-t-0 border-l-0 border-black/10 pointer-events-none"></span>
        )}
      </Button>
    );
  }
);

EnhancedButton.displayName = "EnhancedButton";

export { EnhancedButton };
