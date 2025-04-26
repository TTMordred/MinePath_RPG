import React from "react";
import { cn } from "@/lib/utils";

interface MinecraftCardProps extends React.HTMLAttributes<HTMLDivElement> {
  variant?: "stone" | "diamond" | "gold" | "iron" | "planks" | "purple" | "gradient";
  bordered?: boolean;
  elevated?: boolean;
  children: React.ReactNode;
}

export const MinecraftCard = ({
  variant = "stone",
  bordered = false,
  elevated = false,
  className,
  children,
  ...props
}: MinecraftCardProps) => {
  // Define variant styles
  const variantClasses = {
    stone: "bg-black/60 backdrop-blur-sm",
    diamond: "bg-black/60 backdrop-blur-sm border-minecraft-diamond/40",
    gold: "bg-black/60 backdrop-blur-sm border-minecraft-gold/40",
    iron: "bg-black/60 backdrop-blur-sm border-minecraft-iron/40",
    planks: "bg-black/60 backdrop-blur-sm border-minecraft-planks/40",
    purple: "bg-black/60 backdrop-blur-sm border-solana-purple/40",
    gradient: "bg-black/60 backdrop-blur-sm border-0"
  };

  // Define border styles
  const borderClass = bordered 
    ? variant === "gradient" 
      ? "border border-gradient-to-r from-solana-blue via-solana-purple to-solana-green" 
      : "border" 
    : "";

  // Define elevation styles
  const elevationClass = elevated ? "shadow-xl" : "";

  return (
    <div 
      className={cn(
        variantClasses[variant],
        borderClass,
        elevationClass,
        className
      )}
      {...props}
    >
      {variant === "gradient" && (
        <div className="absolute inset-0 bg-gradient-to-br from-solana-blue/10 via-solana-purple/10 to-solana-green/10 pointer-events-none"></div>
      )}
      {children}
    </div>
  );
};
