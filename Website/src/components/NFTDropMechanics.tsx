import React from 'react';
import { motion } from 'framer-motion';
import { ShieldAlert, Clock, BarChart3, Award, Axe } from 'lucide-react';

const NFTDropMechanics = () => {
  const dropRarities = [
    { 
      name: "Common", 
      color: "text-rarity-common",
      playtime: "~1 hour of gameplay",
      examples: "Stone Pickaxe, Leather Armor"
    },
    { 
      name: "Uncommon", 
      color: "text-rarity-uncommon",
      playtime: "~5 hours of gameplay",
      examples: "Iron Tools, Chain Armor"
    },
    { 
      name: "Rare", 
      color: "text-rarity-rare",
      playtime: "~20 hours of gameplay",
      examples: "Diamond Tools, Special Potions"
    },
    { 
      name: "Epic", 
      color: "text-rarity-epic",
      playtime: "~45 hours of gameplay",
      examples: "Enchanted Diamond Gear, Special Weapons"
    },
    { 
      name: "Legendary", 
      color: "text-rarity-legendary",
      playtime: "75+ hours of gameplay",
      examples: "Unique Weapons, Special Abilities"
    }
  ];

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
        
        {/* Minecraft particles */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(20)].map((_, i) => (
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
        
        {/* Floating blocks */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(8)].map((_, i) => (
            <div
              key={`block-${i}`}
              className="absolute pixelated w-8 h-8"
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
          className="text-center mb-12"
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          viewport={{ once: true }}
        >
          <div className="inline-block p-1.5 rounded-none bg-gradient-to-r from-cyan-500/20 via-blue-500/20 to-cyan-400/20 mb-4 border border-cyan-400/30">
            <div className="px-4 py-1 rounded-none bg-black/60 backdrop-blur-sm text-sm font-minecraft text-cyan-400">
              NFT DROP SYSTEM
            </div>
          </div>
          
          <h2 className="font-minecraft text-4xl md:text-5xl mb-6 text-white">
            <span className="bg-clip-text ">HOW TO EARN <span className="text-blue-500">NFTs</span></span>
          </h2>
          
          <p className="text-lg text-white/80 max-w-3xl mx-auto">
            Our server features a luck-based NFT drop system that rewards your gameplay with valuable blockchain items
          </p>
        </motion.div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 md:gap-8 mb-8 md:mb-16">
          <div className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-4 md:p-6 rounded-lg">
            <h3 className="font-minecraft text-xl md:text-2xl mb-4 text-cyan-400">Drop Triggers</h3>
            
            <div className="space-y-4">
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-minecraft-green">
                  <Axe size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-minecraft-green">Mining</h4>
                  <p className="text-sm text-white/80">Mining valuable blocks like diamond, emerald, and ancient debris has a chance to drop NFTs</p>
                </div>
              </div>
              
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-cyan-400">
                  <Award size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-cyan-400">Monster Kills</h4>
                  <p className="text-sm text-white/80">Defeating hostile mobs and bosses gives you a chance to earn NFT rewards</p>
                </div>
              </div>
              
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-blue-500">
                  <ShieldAlert size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-blue-500">PvP Combat</h4>
                  <p className="text-sm text-white/80">Selected PvP battles in designated arenas can also trigger NFT drops</p>
                </div>
              </div>
            </div>
          </div>
          
          <div className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-4 md:p-6 rounded-lg">
            <h3 className="font-minecraft text-xl md:text-2xl mb-4 text-cyan-400">Safeguards & Balance</h3>
            
            <div className="space-y-4">
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-minecraft-green">
                  <Clock size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-minecraft-green">Cooldown System</h4>
                  <p className="text-sm text-white/80">After earning an NFT, a cooldown period applies before you can earn another one</p>
                </div>
              </div>
              
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-cyan-400">
                  <BarChart3 size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-cyan-400">Diminishing Returns</h4>
                  <p className="text-sm text-white/80">Repetitive actions like farming the same mob will yield fewer drops over time</p>
                </div>
              </div>
              
              <div className="flex items-start">
                <div className="mt-1 mr-4 text-blue-500">
                  <ShieldAlert size={24} />
                </div>
                <div>
                  <h4 className="font-minecraft text-lg text-blue-500">Anti-Farming</h4>
                  <p className="text-sm text-white/80">Low-value actions are excluded to prevent exploitation of the system</p>
                </div>
              </div>
            </div>
          </div>
        </div>
        
        <h3 className="font-minecraft text-xl md:text-2xl mb-6 text-center text-white">NFT Rarity Tiers</h3>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-5 gap-4 mb-8 md:mb-12">
          {dropRarities.map((rarity, index) => (
            <motion.div 
              key={index}
              className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-4 md:p-6 flex flex-col items-center text-center rounded-lg"
              initial={{ opacity: 0, scale: 0.9 }}
              whileInView={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.3, delay: index * 0.1 }}
              viewport={{ once: true }}
            >
              <h4 className={`font-minecraft text-xl mb-2 ${rarity.color}`}>{rarity.name}</h4>
              <p className="text-sm mb-2 text-white/60">Expected after:</p>
              <p className="font-minecraft text-sm mb-3 text-white/80">{rarity.playtime}</p>
              <p className="text-xs text-white/60 mt-auto">Examples: {rarity.examples}</p>
            </motion.div>
          ))}
        </div>
        
        <div className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-6 max-w-4xl mx-auto rounded-lg">
          <h3 className="font-minecraft text-xl mb-4 text-center text-cyan-400">Pro Tips for NFT Hunters</h3>
          
          <ul className="space-y-2">
            <li className="flex items-start">
              <span className="font-minecraft text-cyan-400 mr-2">▶</span>
              <span className="text-sm text-white/80">Vary your activities between mining and combat for the best chances</span>
            </li>
            <li className="flex items-start">
              <span className="font-minecraft text-cyan-400 mr-2">▶</span>
              <span className="text-sm text-white/80">Boss fights and rare ore mining have higher drop rates</span>
            </li>
            <li className="flex items-start">
              <span className="font-minecraft text-cyan-400 mr-2">▶</span>
              <span className="text-sm text-white/80">Join server events for boosted drop chances during special periods</span>
            </li>
            <li className="flex items-start">
              <span className="font-minecraft text-cyan-400 mr-2">▶</span>
              <span className="text-sm text-white/80">Legendary items are extremely rare - expect to play for 75+ hours before finding one</span>
            </li>
          </ul>
        </div>
      </div>
    </section>
  );
};

export default NFTDropMechanics;
