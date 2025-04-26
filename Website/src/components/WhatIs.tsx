import React from 'react';
import { motion } from 'framer-motion';
import { Sparkles, ArrowRight } from 'lucide-react';
import { Link } from 'react-router-dom';

const WhatIs = () => {
  return (
    <section className="relative py-16 md:py-32 overflow-hidden">
      {/* Background with overlay */}
      <div className="absolute inset-0 z-0">
        
        <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/80"></div>
        
        {/* Animated overlay pattern */}
        <div className="absolute inset-0 bg-[url('/images/bg-mountain.png')] bg-no-repeat bg-cover bg-center opacity-4"></div>
        
        {/* Geometric patterns along left edge */}
        <div className="absolute left-0 inset-y-0 w-16 opacity-20">
          <div className="h-full w-full bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat-y"></div>
        </div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-8 lg:gap-16 items-center">
          <motion.div
            initial={{ opacity: 0, x: -30 }}
            whileInView={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.6 }}
            viewport={{ once: true, margin: "-100px" }}
          >
            <div className="inline-block p-1.5 rounded-full bg-gradient-to-r from-cyan-500/20 to-blue-500/20 mb-5">
              <div className="px-4 py-1 rounded-full bg-black/60 backdrop-blur-sm text-sm font-medium text-cyan-400">
                OUR MISSION
              </div>
            </div>
            
            <h2 className="text-3xl md:text-4xl lg:text-5xl font-minecraft mb-6 text-white text-center lg:text-left">
              What is <span className="text-cyan-400">MinePath</span>?
            </h2>
            
            <p className="text-base md:text-lg leading-relaxed text-white/80 mb-6 text-center lg:text-left">
              MinePath combines the beloved gameplay of Minecraft with Solana blockchain technology, creating a unique 
              play-to-earn experience where your mining and exploration efforts are rewarded with valuable NFTs.
            </p>
            
            <div className="space-y-4 md:space-y-6 mb-8">
              <Feature title="Play & Earn" description="Mine, fight, and explore to earn NFTs that have real-world value" />
              <Feature title="Fair Distribution" description="NFTs drop based on mining valuable blocks and defeating monsters" />
              <Feature title="Community Owned" description="Governance tokens empower players to vote on future developments" />
            </div>
            
            <Link to="/how-to-play">
              <button className="flex items-center gap-2 group">
                <span className="font-minecraft text-white group-hover:text-cyan-400 transition-colors">
                  Learn more about our ecosystem
                </span>
                <ArrowRight className="h-5 w-5 text-cyan-400 group-hover:translate-x-1 transition-transform" />
              </button>
            </Link>
          </motion.div>
          
          <motion.div
            initial={{ opacity: 0, scale: 0.95 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.6, delay: 0.2 }}
            viewport={{ once: true, margin: "-100px" }}
            className="relative mt-8 lg:mt-0"
          >
            <div className="relative z-10 border border-cyan-500/30 p-1.5 bg-black/40 backdrop-blur-md">
              <img 
                src="/images/minecraft_world.png"
                alt="Minecraft world" 
                className="w-full h-auto pixelated"
              />
              
              <div className="absolute -bottom-4 -right-4 p-3 bg-black/80 border border-cyan-400/30 max-w-xs">
                <p className="text-sm text-white">
                  <span className="text-cyan-400 font-bold">The First Minecraft x Solana Project</span> with real 
                  ownership of in-game assets as NFTs
                </p>
              </div>
            </div>
            
            {/* Floating Decorative Elements */}
            <div className="absolute top-1/2 -left-16 transform -translate-y-1/2">
              <motion.img 
                src="/images/diamond.png" 
                alt="Diamond" 
                className="w-16 h-16 pixelated"
                animate={{ 
                  y: [0, -10, 0],
                  rotate: [0, 5, 0]
                }}
                transition={{ 
                  duration: 4,
                  repeat: Infinity,
                  ease: "easeInOut"
                }}
              />
            </div>
            
            <div className="absolute -bottom-8 left-1/4">
              <motion.img 
                src="/images/gold.png" 
                alt="Gold" 
                className="w-12 h-12 pixelated"
                animate={{ 
                  y: [0, -8, 0],
                  rotate: [0, -5, 0]
                }}
                transition={{ 
                  duration: 3.5,
                  repeat: Infinity,
                  ease: "easeInOut",
                  delay: 0.5
                }}
              />
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

const Feature = ({ title, description }) => (
  <div className="flex items-start">
    <div className="mr-4 mt-1">
      <div className="w-3 h-3 bg-cyan-400"></div>
    </div>
    <div>
      <h3 className="font-minecraft text-xl text-cyan-400 mb-1">{title}</h3>
      <p className="text-white/80">{description}</p>
    </div>
  </div>
);

export default WhatIs;
