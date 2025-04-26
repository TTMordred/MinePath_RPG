
import React from 'react';
import { motion } from 'framer-motion';
import { Users, Trophy, CalendarDays, MessageSquare, Twitter } from 'lucide-react';
import { Link } from 'react-router-dom';
import { useIsMobile } from '@/hooks/use-mobile';

const CommunitySection = () => {
  const isMobile = useIsMobile();
  
  return (
    <section className="py-16 md:py-24 relative overflow-hidden" style={{ 
      background: 'linear-gradient(180deg, rgba(13,14,22,1) 0%, rgba(21,26,49,1) 100%)',
      backgroundSize: 'cover',
      backgroundAttachment: 'fixed' 
    }}>
      <div className="absolute inset-0 z-0">
        <div className="absolute top-0 left-0 w-full h-full opacity-10 bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat"></div>
        <div className="absolute top-0 left-0 w-full h-full" style={{ 
          background: 'radial-gradient(circle, rgba(10, 21, 77, 0.3) 0%, rgba(13, 14, 22, 0) 70%)'
        }}></div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <motion.div 
          className="text-center mb-10 md:mb-16"
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          viewport={{ once: true }}
        >
          <div className="inline-block p-1.5 rounded-md backdrop-blur-sm bg-gradient-to-r from-blue-600/20 to-purple-600/20 mb-5">
            <div className="px-4 py-1.5 font-minecraft text-cyan-400 text-sm border-b border-cyan-400/30">
              JOIN US
            </div>
          </div>
          
          <h2 className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6">
            <span className="bg-clip-text ">
              JOIN THE <span className="text-blue-500">MINEPATH COMMUNITY</span>
            </span>
          </h2>
          
          <p className="text-base lg:text-lg text-white/80 max-w-2xl mx-auto">
            Connect with thousands of players across the world, form guilds, and participate in exclusive events.
          </p>
        </motion.div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6 md:gap-8 mb-16">
          {/* Global Reach */}
          <motion.div 
            className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg p-4 md:p-6"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <Users className="h-6 w-6 md:h-8 md:w-8 text-cyan-400 mr-3 md:mr-4" />
              <h3 className="font-minecraft text-xl md:text-2xl text-cyan-400">GLOBAL REACH</h3>
            </div>
            <p className="text-sm md:text-base text-white/80 mb-6">
              Connect with thousands of players across Cross-Server networks and compete in real-time leaderboards.
            </p>
            <div className="grid grid-cols-3 gap-2 md:gap-4 text-center">
              <div className="bg-black/50 p-2 border border-cyan-400/20 rounded-md">
                <div className="font-minecraft text-cyan-400 text-lg md:text-xl">1000+</div>
                <div className="text-xs text-white/70">Daily Players</div>
              </div>
              <div className="bg-black/50 p-2 border border-cyan-400/20 rounded-md">
                <div className="font-minecraft text-cyan-400 text-lg md:text-xl">50+</div>
                <div className="text-xs text-white/70">Countries</div>
              </div>
              <div className="bg-black/50 p-2 border border-cyan-400/20 rounded-md">
                <div className="font-minecraft text-cyan-400 text-lg md:text-xl">5</div>
                <div className="text-xs text-white/70">Game Modes</div>
              </div>
            </div>
          </motion.div>
          
          {/* Guilds & Parties */}
          <motion.div 
            className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg p-4 md:p-6"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <Trophy className="h-6 w-6 md:h-8 md:w-8 text-cyan-400 mr-3 md:mr-4" />
              <h3 className="font-minecraft text-xl md:text-2xl text-cyan-400">GUILDS & PARTIES</h3>
            </div>
            <p className="text-sm md:text-base text-white/80 mb-6">
              Form alliances, tackle boss battles, and dominate leaderboards together. Guilds earn bonus rewards!
            </p>
            <div className="bg-black/50 p-3 md:p-4 border border-cyan-400/20 rounded-md">
              <div className="font-minecraft text-cyan-400 mb-2">TOP GUILDS</div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm md:text-base text-white/80">Diamond Miners</span>
                <span className="font-minecraft text-sm md:text-base text-yellow-400">1250 $FARM</span>
              </div>
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm md:text-base text-white/80">Emerald Knights</span>
                <span className="font-minecraft text-sm md:text-base text-yellow-400">980 $FARM</span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-sm md:text-base text-white/80">Redstone Wizards</span>
                <span className="font-minecraft text-sm md:text-base text-yellow-400">840 $FARM</span>
              </div>
            </div>
          </motion.div>
          
          {/* Limited Events */}
          <motion.div 
            className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg p-4 md:p-6"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <CalendarDays className="h-6 w-6 md:h-8 md:w-8 text-cyan-400 mr-3 md:mr-4" />
              <h3 className="font-minecraft text-xl md:text-2xl text-cyan-400">LIMITED EVENTS</h3>
            </div>
            <p className="text-sm md:text-base text-white/80 mb-6">
              Participate in exclusive events like Crystal Rush or Boss Raid Festival for rare rewards and unique NFTs.
            </p>
            <div className="bg-black/50 p-3 md:p-4 border border-cyan-400/20 rounded-md">
              <div className="flex justify-between items-center mb-4">
                <div>
                  <div className="font-minecraft text-cyan-400">Server Opening</div>
                  <div className="text-xs text-white/70">Starting in 3 days</div>
                </div>
                <div className="bg-gradient-to-r from-blue-500/20 to-cyan-500/20 p-1.5 text-xs sm:text-sm text-white/80 border border-cyan-400/20 rounded-md">
                  GREAT REWARDS
                </div>
              </div>
              <button className="w-full bg-gradient-to-r from-cyan-500 to-blue-500 text-white py-2 px-4 rounded-md text-sm font-minecraft">
                Set Reminder
              </button>
            </div>
          </motion.div>
          
          {/* Social Links */}
          <motion.div 
            className="bg-black/30 backdrop-blur-sm border border-cyan-400/30 rounded-lg p-4 md:p-6"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <MessageSquare className="h-6 w-6 md:h-8 md:w-8 text-cyan-400 mr-3 md:mr-4" />
              <h3 className="font-minecraft text-xl md:text-2xl text-cyan-400">JOIN OUR CHANNELS</h3>
            </div>
            <p className="text-sm md:text-base text-white/80 mb-6">
              Follow us for the latest news, updates, and exclusive giveaways. New followers receive 1 $FARM!
            </p>
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <a 
                href="https://discord.gg/HDc5QYXz" 
                target="_blank" 
                rel="noopener noreferrer"
                className="bg-indigo-600 hover:bg-indigo-700 flex items-center justify-center py-2 md:py-3 rounded-md text-white font-minecraft transition-colors"
              >
                <MessageSquare className="mr-2 h-4 w-4" />
                Join Discord
              </a>
              <a 
                href="https://x.com/MinePath_RPG" 
                target="_blank" 
                rel="noopener noreferrer"
                className="bg-blue-500 hover:bg-blue-600 flex items-center justify-center py-2 md:py-3 rounded-md text-white font-minecraft transition-colors"
              >
                <Twitter className="mr-2 h-4 w-4" />
                Follow on X
              </a>
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

export default CommunitySection;
