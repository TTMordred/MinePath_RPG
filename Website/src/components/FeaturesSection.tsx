
import React from 'react';
import { motion } from 'framer-motion';
import { Sword, Shield, Coins, Award, Share2, Sparkles, LucideIcon } from 'lucide-react';
import { MinecraftIcon } from '@/components/ui/minecraft-icon';
import { EnhancedButton } from '@/components/ui/enhanced-button';
import { useIsMobile } from '@/hooks/use-mobile';

const features = [
  {
    title: "Mint In-Game Items",
    description: "Transform your hard-earned Minecraft items into tradable NFTs on the Solana blockchain.",
    icon: Sword,
    blockType: "diamond",
    variant: "diamond",
    image: "/images/feature_mint.png"
  },
  {
    title: "Secure Ownership",
    description: "True ownership of your items, secured by blockchain technology that can't be hacked or duplicated.",
    icon: Shield,
    blockType: "iron",
    variant: "stone",
    image: "/images/feature_secure.png"
  },
  {
    title: "Trade & Sell",
    description: "Trade your NFTs with other players or sell them on our marketplace for real cryptocurrency.",
    icon: Coins,
    blockType: "gold",
    variant: "gold",
    image: "/images/feature_trade.png"
  },
  {
    title: "Collect Rarities",
    description: "Discover and collect rare items with different tiers of scarcity – from common to legendary.",
    icon: Award,
    blockType: "emerald",
    variant: "grass",
    image: "/images/feature_collect.png"
  },
  {
    title: "Cross-Server Usage",
    description: "Use your NFT items across multiple compatible Minecraft servers in our network.",
    icon: Share2,
    blockType: "lapis",
    variant: "diamond",
    image: "/images/feature_servers.png"
  },
  {
    title: "Special Abilities",
    description: "Legendary NFTs grant special abilities and perks that regular Minecraft items don't have.",
    icon: Sparkles,
    blockType: "redstone",
    variant: "stone",
    image: "/images/feature_abilities.png"
  },
];

const container = {
  hidden: { opacity: 0 },
  show: {
    opacity: 1,
    transition: {
      staggerChildren: 0.1
    }
  }
};

const item = {
  hidden: { opacity: 0, y: 20 },
  show: { opacity: 1, y: 0 }
};

const FeaturesSection = () => {
  const isMobile = useIsMobile();

  return (
    <section className="py-16 md:py-24 relative overflow-hidden" style={{ 
      background: 'linear-gradient(180deg, rgba(13,14,22,1) 0%, rgba(21,26,49,1) 100%)',
      backgroundSize: 'cover',
      backgroundAttachment: 'fixed' 
    }}>
      {/* Background elements */}
      <div className="absolute inset-0 z-0">
        <div className="absolute top-0 left-0 w-full h-full opacity-10 bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat"></div>
        <div className="absolute top-0 left-0 w-full h-full" style={{ 
          background: 'radial-gradient(circle, rgba(10, 21, 77, 0.3) 0%, rgba(13, 14, 22, 0) 70%)'
        }}></div>
        
        {/* Minecraft particles - reduced for mobile */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(isMobile ? 10 : 20)].map((_, i) => (
            <div
              key={i}
              className="absolute pixelated w-2 h-2 bg-white opacity-30"
              style={{
                top: `${Math.random() * 100}%`,
                left: `${Math.random() * 100}%`,
                animation: `float ${5 + Math.random() * 5}s ease-in-out infinite ${Math.random() * 5}s`
              }}
            />
          ))}
        </div>
        
        {/* Floating blocks - reduced for mobile */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(isMobile ? 4 : 8)].map((_, i) => (
            <div
              key={`block-${i}`}
              className="absolute pixelated w-6 md:w-8 h-6 md:h-8"
              style={{
                top: `${Math.random() * 100}%`,
                left: `${Math.random() * 100}%`,
                backgroundImage: `url('/images/${['dirt', 'stone', 'diamond', 'gold'][Math.floor(Math.random() * 4)]}_block.png')`,
                backgroundSize: 'cover',
                transform: 'rotate(10deg)',
                imageRendering: 'pixelated',
                animation: `float ${7 + Math.random() * 7}s ease-in-out infinite ${Math.random() * 7}s, rotate ${15 + Math.random() * 10}s linear infinite ${Math.random() * 10}s`
              }}
            />
          ))}
        </div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <motion.div 
          className="text-center mb-10 md:mb-16"
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          viewport={{ once: true, margin: "-100px" }}
        >
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            viewport={{ once: true }}
            className="inline-block p-1.5 rounded-md backdrop-blur-sm bg-gradient-to-r from-blue-600/20 to-purple-600/20 mb-5"
          >
            <div className="px-4 py-1.5 font-minecraft text-cyan-400 text-sm border-b border-cyan-400/30">
              SERVER FEATURES
            </div>
          </motion.div>
          
          <motion.h2 
            className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
          >
            <span className="bg-clip-text ">
              PLAY, <span className="text-cyan-400">EARN</span>, <span className="text-blue-500">EVOLVE</span>
            </span>
          </motion.h2>
          
          <motion.p 
            className="text-base md:text-lg text-white/80 max-w-2xl mx-auto font-minecraft tracking-wide"
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
          >
            Our Minecraft server integrates Solana NFTs for a revolutionary gaming experience, 
            allowing you to truly own your virtual assets and trade them on the blockchain.
          </motion.p>
        </motion.div>
        
        <motion.div 
          className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4 md:gap-6"
          variants={container}
          initial="hidden"
          whileInView="show"
          viewport={{ once: true, margin: "-50px" }}
        >
          {features.map((feature, index) => (
            <FeatureCard key={index} feature={feature} index={index} />
          ))}
        </motion.div>
        
        <div className="mt-12 md:mt-16 text-center">
          <button className="play-now-btn relative px-6 md:px-8 py-2 md:py-3 bg-white text-black font-minecraft tracking-wider hover:scale-105 transition-all duration-300 overflow-hidden group">
            <span className="relative z-10">JOIN OUR SERVER</span>
            <span className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-blue-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></span>
          </button>
        </div>
      </div>
    </section>
  );
};

interface FeatureCardProps {
  feature: {
    title: string;
    description: string;
    icon: LucideIcon;
    blockType: string;
    variant: string;
    image: string;
  };
  index: number;
}

const FeatureCard = ({ feature, index }: FeatureCardProps) => {
  return (
    <motion.div 
      className="h-full"
      variants={item}
      transition={{ duration: 0.5, delay: index * 0.05 }}
    >
      <div className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg h-full relative overflow-hidden">
        {/* Dark overlay for better readability */}
        <div className="absolute inset-0 bg-black/20"></div>
        
        <div className="p-4 md:p-6 flex flex-col h-full relative">
          {/* Pixelated overlay texture */}
          <div className="absolute inset-0 opacity-10" style={{ 
            backgroundImage: 'url("/images/pixel_pattern.png")',
            backgroundSize: '4px 4px',
            imageRendering: 'pixelated'
          }}></div>
          
          {/* Feature Image */}
          <div className="relative w-full aspect-[16/9] overflow-hidden rounded-lg">
            <img 
              src={feature.image} 
              alt={feature.title} 
              className="pixelated w-full h-full object-cover"
              onError={(e) => {
                (e.target as HTMLImageElement).src = '/images/placeholder_feature.png'
              }}
            />
          </div>
          
          {/* Icon */}
          <div className="relative z-10 mb-4 md:mb-6 mt-4">
            <div className="w-10 h-10 md:w-12 md:h-12 bg-black/40 border border-cyan-400/30 backdrop-blur-sm rounded-lg flex items-center justify-center">
              <feature.icon className="h-5 w-5 md:h-6 md:w-6 text-cyan-400" />
            </div>
          </div>
          
          <h3 className="font-minecraft text-lg md:text-xl mb-2 md:mb-3 text-cyan-400 relative z-10">
            {feature.title}
          </h3>
          
          <p className="text-xs md:text-sm text-white/80 font-minecraft tracking-wide relative z-10 leading-relaxed">
            {feature.description}
          </p>
        </div>
      </div>
    </motion.div>
  );
};

export default FeaturesSection;
