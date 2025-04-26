
import React from 'react';
import { Copy, ArrowRight } from 'lucide-react';
import { useToast } from '@/hooks/use-toast';
import ServerStatus from './ServerStatus';
import { MinecraftProgress } from './ui/minecraft-progress';
import { motion } from 'framer-motion';
import { useIsMobile } from '@/hooks/use-mobile';

const JoinCTA = () => {
  const { toast } = useToast();
  const isMobile = useIsMobile();
  
  const copyServerAddress = () => {
    navigator.clipboard.writeText('play.minepath.com');
    toast({
      title: "Server address copied!",
      description: "Ready to paste in your Minecraft client"
    });
  };
  
  return (
    <section className="relative py-12 md:py-16 overflow-hidden" style={{ 
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
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.6 }}
          viewport={{ once: true, margin: "-100px" }}
          className="bg-black/60 backdrop-blur-md border border-blue-400/20 p-6 md:p-8 lg:p-10 shadow-lg"
        >
          <div className="flex flex-col lg:flex-row items-center justify-between gap-8">
            <div className="w-full lg:max-w-2xl">
              <h2 className="font-minecraft text-2xl sm:text-3xl md:text-4xl mb-4 text-white text-center lg:text-left">
                <span className="text-cyan-400">JOIN THE ADVENTURE</span> TODAY
              </h2>
              
              <p className="text-base md:text-lg text-white/90 mb-6 font-minecraft leading-relaxed text-center lg:text-left">
                Ready to earn NFTs while playing Minecraft? Our luck-based drop system rewards mining valuable blocks and monster kills with NFTs ranging from Common to Legendary!
              </p>

              <div className="mb-6">
                <p className="text-sm text-white mb-2 text-center lg:text-left">Server Population:</p>
                <div className="w-full h-3 bg-black/50 border border-blue-300/30 overflow-hidden">
                  <div className="h-full bg-gradient-to-r from-cyan-400 to-blue-500 w-[72%]"></div>
                </div>
                <div className="flex justify-between text-xs mt-1 text-white/70">
                  <span>0</span>
                  <span>500</span>
                  <span>1000 players</span>
                </div>
              </div>
              
              <div className="flex flex-col sm:flex-row gap-4 justify-center lg:justify-start">
                <div className="relative w-full sm:w-auto">
                  <div className="flex items-center bg-black/60 border border-blue-400/30 px-4 py-2 w-full">
                    <span className="font-minecraft text-white">play.minepath.com</span>
                    <button 
                      onClick={copyServerAddress}
                      className="ml-3 p-1 hover:text-cyan-400 transition-colors ml-auto"
                      aria-label="Copy server address"
                    >
                      <Copy size={16} />
                    </button>
                  </div>
                </div>
                
                <button className="play-now-btn relative px-6 py-2 bg-white text-black font-minecraft tracking-wider hover:scale-105 transition-all duration-300 overflow-hidden group inline-flex items-center justify-center w-full sm:w-auto">
                  <span className="relative z-10 flex items-center">
                    Join Server Now <ArrowRight className="ml-2 h-4 w-4" />
                  </span>
                  <span className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-blue-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></span>
                </button>
              </div>
            </div>
            
            <div className="w-full lg:w-auto mt-6 lg:mt-0 flex justify-center">
              <motion.img
                initial={{ y: 10 }}
                animate={{ y: -10 }}
                transition={{
                  duration: 2,
                  repeat: Infinity,
                  repeatType: "reverse",
                  ease: "easeInOut"
                }}
                src="/images/minecraft_character.png" 
                alt="Minecraft Character with NFT" 
                className="pixelated h-56 sm:h-64 md:h-72 lg:h-96 w-auto object-contain mx-auto"
              />
            </div>
          </div>
        </motion.div>
      </div>
    </section>
  );
};

export default JoinCTA;
