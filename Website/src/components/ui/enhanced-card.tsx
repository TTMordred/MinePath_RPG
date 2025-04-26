
import React from 'react';
import { cn } from '@/lib/utils';
import { Card } from '@/components/ui/card';

interface EnhancedCardProps extends React.ComponentPropsWithoutRef<typeof Card> {
  hoverEffect?: 'glow' | 'scale' | 'rotate' | 'none';
  borderGradient?: boolean;
  glassEffect?: boolean;
  minecraftStyle?: 'dirt' | 'stone' | 'planks' | 'diamond' | 'gold';
}

const EnhancedCard = React.forwardRef<
  HTMLDivElement,
  EnhancedCardProps
>(({ 
  className, 
  hoverEffect = 'none', 
  borderGradient = false,
  glassEffect = false,
  minecraftStyle,
  children, 
  ...props 
}, ref) => {
  // Minecraft background classes
  const minecraftBg = {
    dirt: "minecraft-dirt-bg",
    stone: "minecraft-stone-bg",
    planks: "minecraft-wood-bg",
    diamond: "bg-minecraft-diamond/20",
    gold: "bg-minecraft-gold/20"
  };
  
  // Minecraft border classes
  const minecraftBorder = {
    dirt: "border-minecraft-dirt",
    stone: "border-minecraft-stone",
    planks: "border-minecraft-planks",
    diamond: "border-minecraft-diamond/70",
    gold: "border-minecraft-gold/70"
  };
  
  return (
    <Card
      ref={ref}
      className={cn(
        "transition-all duration-300 overflow-hidden",
        glassEffect && "bg-background/30 backdrop-blur-lg",
        borderGradient && !minecraftStyle && "border-none p-[1px] bg-gradient-to-br from-solana-purple/50 via-solana-blue/50 to-solana-green/50",
        minecraftStyle && "border-4 rounded-none",
        minecraftStyle && minecraftBg[minecraftStyle],
        minecraftStyle && minecraftBorder[minecraftStyle],
        hoverEffect === 'glow' && "hover:shadow-[0_0_20px_rgba(155,135,245,0.3)]",
        hoverEffect === 'scale' && "hover:scale-105",
        hoverEffect === 'rotate' && "hover:rotate-1 hover:scale-[1.02]",
        className
      )}
      {...props}
    >
      <div className={cn(
        "h-full w-full",
        borderGradient && !minecraftStyle && "bg-card rounded-[inherit]",
      )}>
        {/* Minecraft style overlay - corner pixels */}
        {minecraftStyle && (
          <>
            <div className="absolute -top-1 -left-1 w-2 h-2 bg-black/10"></div>
            <div className="absolute -top-1 -right-1 w-2 h-2 bg-black/10"></div>
            <div className="absolute -bottom-1 -left-1 w-2 h-2 bg-black/10"></div>
            <div className="absolute -bottom-1 -right-1 w-2 h-2 bg-black/10"></div>
          </>
        )}
        
        {children}
      </div>
    </Card>
  );
});

EnhancedCard.displayName = "EnhancedCard";

export { EnhancedCard };
