import React from 'react';
import { Card, CardContent } from '@/components/ui/card';
import { Badge } from '@/components/ui/badge';
import { Button } from '@/components/ui/button';
import { EyeIcon, Sparkles } from 'lucide-react';
import { motion } from 'framer-motion';

const FEATURED_NFTS = [
  {
    id: 1,
    name: "Dragon's Breath",
    image: "/images/dragons_breath.png",
    rarity: "legendary",
    description: "Unleashes a powerful flame attack that damages all enemies in front of you.",
    attributes: [
      { trait: "Damage", value: "+150%" },
      { trait: "Area Effect", value: "5 blocks" },
      { trait: "Cooldown", value: "60s" }
    ]
  },
  {
    id: 2,
    name: "Diamond Defender",
    image: "/images/diamond_defender.png",
    rarity: "rare",
    description: "Creates a temporary shield that absorbs damage from attacks.",
    attributes: [
      { trait: "Defense", value: "+40%" },
      { trait: "Duration", value: "15s" },
      { trait: "Cooldown", value: "45s" }
    ]
  },
  {
    id: 3,
    name: "Void Walker",
    image: "/images/void_walker.png",
    rarity: "epic",
    description: "Grants temporary invisibility and increased movement speed.",
    attributes: [
      { trait: "Speed", value: "+60%" },
      { trait: "Duration", value: "10s" },
      { trait: "Cooldown", value: "90s" }
    ]
  }
];

const NFTShowcase = () => {
  return (
    <section className="relative py-16 md:py-24 overflow-hidden">
      {/* Background with overlay */}
      <div className="absolute inset-0 z-0">
        <div 
          className="absolute inset-0 bg-cover bg-center" 
          style={{ 
            backgroundImage: "url('/public/lovable-uploads/5c79ef56-9243-4973-b744-f05067bf5ead.png')",
            filter: "brightness(0.4)",
            backgroundSize: "cover"
          }}
        ></div>
        <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/80"></div>
        
        {/* Animated overlay pattern */}
        <div className="absolute inset-0 bg-[url('/images/bg-icemountain.png')] bg-repeat "></div>
        
        {/* Geometric patterns along left edge */}
        <div className="absolute left-0 inset-y-0 w-16 opacity-20">
          <div className="h-full w-full bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat-y"></div>
        </div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="text-center mb-16">
          <div className="inline-block p-1.5 rounded-full bg-gradient-to-r from-cyan-500/20 to-blue-500/20 mb-5">
            <div className="px-4 py-1 rounded-full bg-black/60 backdrop-blur-sm text-sm font-medium text-cyan-400">
              FEATURED NFTs
            </div>
          </div>
          
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true, margin: "-100px" }}
            className="font-minecraft text-4xl md:text-5xl mb-6 text-white"
          >
            <span className="bg-clip-text ">
              POWER UP YOUR GAMEPLAY
            </span>
          </motion.h2>
          
          <motion.p
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true, margin: "-100px" }}
            className="text-lg text-white/80 max-w-2xl mx-auto"
          >
            Discover these powerful NFTs that can be earned in-game through mining, combat, and PvP. 
            Drop chances range from Common (1%) to Legendary (0.01%) based on rarity.
          </motion.p>
        </div>
        
        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 md:gap-8 mb-8 md:mb-16">
          {FEATURED_NFTS.map((nft, index) => (
            <motion.div
              key={nft.id}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 * index }}
              viewport={{ once: true, margin: "-100px" }}
            >
              <NFTCard nft={nft} />
            </motion.div>
          ))}
        </div>
        
        <div className="text-center">
          <button className="play-now-btn relative px-8 py-3 bg-black text-white font-minecraft tracking-wider hover:scale-105 transition-all duration-300 overflow-hidden group border border-cyan-400/50">
            <span className="relative z-10 flex items-center justify-center">
              View All NFTs <EyeIcon className="ml-2 h-4 w-4" />
            </span>
            <span className="absolute inset-0 bg-gradient-to-r from-cyan-400 to-blue-500 opacity-0 group-hover:opacity-100 transition-opacity duration-300"></span>
          </button>
        </div>
      </div>
    </section>
  );
};

const NFTCard = ({ nft }: { nft: typeof FEATURED_NFTS[0] }) => {
  return (
    <div className="group">
      <div className="glass-card overflow-hidden transition-all duration-300 group-hover:translate-y-[-4px]">
        <div className="relative h-48 md:h-64 overflow-hidden bg-black/30">
          <img 
            src={nft.image} 
            alt={nft.name}
            className="object-cover w-full h-full transition-transform duration-500 group-hover:scale-110 pixelated"
          />
          
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div className="absolute bottom-0 left-0 right-0 p-4 transform translate-y-full group-hover:translate-y-0 transition-transform duration-300">
            <div className="space-y-1">
              {nft.attributes.map((attr, index) => (
                <div key={index} className="flex justify-between text-xs">
                  <span className="text-gray-300">{attr.trait}:</span>
                  <span className="text-cyan-400 font-bold">{attr.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
        
        <div className="p-4 bg-black/70 backdrop-blur-md border-t border-cyan-400/20">
          <h3 className="font-minecraft text-lg md:text-xl mb-2 text-gradient">{nft.name}</h3>
          <p className="text-sm text-white/80 mb-4">{nft.description}</p>
          
          <div className="flex justify-between items-center">
            <button className="text-sm bg-black px-3 py-1 border border-cyan-400/30 text-cyan-400 hover:bg-cyan-400/10 transition-colors duration-300">
              View Details
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default NFTShowcase;
