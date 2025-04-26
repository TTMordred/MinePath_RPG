
import React from 'react';
import { Button } from '@/components/ui/button';
import { ArrowRight } from 'lucide-react';
import { motion } from 'framer-motion';
import { useIsMobile } from '@/hooks/use-mobile';

const STEPS = [
  {
    number: '01',
    title: 'Join the Server',
    description: 'Connect to our Minecraft server using the IP address play.minepath.com',
    icon: '/images/icons/minecraft-server.png'
  },
  {
    number: '02',
    title: 'Link Your Wallet',
    description: 'Connect your Solana wallet to your Minecraft account using our simple in-game command',
    icon: '/images/icons/minecraft-wallet.png'
  },
  {
    number: '03',
    title: 'Play & Earn',
    description: 'Mine valuable blocks and defeat mobs to earn NFT drops based on rarity (1% chance for Common items)',
    icon: '/images/icons/minecraft-pickaxe.png'
  },
  {
    number: '04',
    title: 'Use Your NFTs',
    description: 'Equip your NFTs in-game to gain special powers and abilities that enhance your gameplay',
    icon: '/images/icons/minecraft-sword.png'
  },
  {
    number: '05',
    title: 'Trade & Collect',
    description: 'Build your collection from Common to Legendary items and trade them on the Solana blockchain',
    icon: '/images/icons/minecraft-diamond.png'
  }
];

const HowToPlay = () => {
  const isMobile = useIsMobile();
  
  return (
    <section className="py-16 md:py-24 relative overflow-hidden" style={{ 
      background: 'linear-gradient(180deg, rgba(21,26,49,1) 0%, rgba(13,14,22,1) 100%)',
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
        <div className="text-center mb-10 md:mb-16">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            viewport={{ once: true }}
            className="inline-block p-1.5 rounded-md backdrop-blur-sm bg-gradient-to-r from-blue-600/20 to-purple-600/20 mb-5"
          >
            <div className="px-4 py-1.5 font-minecraft text-cyan-400 text-sm border-b border-cyan-400/30">
              GETTING STARTED
            </div>
          </motion.div>

          <motion.h2 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
            className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white"
          >
            <span className="bg-clip-text">
              HOW TO PLAY
            </span>
          </motion.h2>

          <motion.p 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
            className="text-base lg:text-lg text-white/80 max-w-3xl mx-auto"
          >
            Join our Minecraft server and start earning NFTs that have real value. Follow these simple steps to get started:
          </motion.p>
        </div>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 md:gap-6 mb-12 md:mb-16">
          {STEPS.map((step, index) => (
            <motion.div 
              key={index}
              className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg p-4 md:p-6 relative"
              initial={{ opacity: 0, y: 20 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: index * 0.1 }}
              viewport={{ once: true }}
            >
              <div className="absolute -top-4 -left-4 w-8 h-8 bg-gradient-to-br from-cyan-400 to-blue-500 flex items-center justify-center font-minecraft text-black rounded-md">
                {step.number}
              </div>
              
              <div className="flex justify-center mb-4">
                <img src={step.icon} alt={step.title} className="h-12 w-12 md:h-16 md:w-16 pixelated" />
              </div>
              
              <h3 className="font-minecraft text-base md:text-lg mb-2 text-center text-cyan-400">
                {step.title}
              </h3>
              
              <p className="text-xs md:text-sm text-center text-white/80">
                {step.description}
              </p>
            </motion.div>
          ))}
        </div>
        
        <div className="text-center">
          <motion.button 
            className="play-now-btn relative px-6 md:px-8 py-2 md:py-3 bg-white text-black font-minecraft tracking-wider hover:scale-105 transition-all duration-300 overflow-hidden group inline-flex items-center"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.6 }}
            viewport={{ once: true }}
          >
            <span className="relative z-10 flex items-center">
              Get Started Now <ArrowRight className="ml-2 h-5 w-5" />
            </span>
            <span className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-blue-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></span>
          </motion.button>
        </div>
      </div>
    </section>
  );
};

export default HowToPlay;
