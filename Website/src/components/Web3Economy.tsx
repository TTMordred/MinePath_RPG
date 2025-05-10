
import React from 'react';
import { motion } from 'framer-motion';
import { Coins, TrendingUp, Wallet, Landmark } from 'lucide-react';
import { MinecraftIcon } from '@/components/ui/minecraft-icon';
import WalletModal from '@/components/WalletModal';

const Web3Economy = () => {
  return (
    <section className="py-24 relative overflow-hidden" style={{ 
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
          className="text-center mb-16"
          initial={{ opacity: 0, y: 20 }}
          whileInView={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
          viewport={{ once: true }}
        >
          <div className="inline-block p-1.5 rounded-none bg-gradient-to-r from-cyan-500/20 via-blue-500/20 to-cyan-400/20 mb-4 border border-cyan-400/30">
            <div className="px-4 py-1.5 rounded-none bg-black/60 backdrop-blur-sm text-sm font-minecraft text-cyan-400">
              BLOCKCHAIN POWERED
            </div>
          </div>
          
          <h2 className="font-minecraft text-4xl md:text-5xl mb-6">
            <span className="bg-clip-text ">
              POWER YOUR JOURNEY <span className="text-blue-500">WITH WEB3</span> 
            </span>
          </h2>
          
          <p className="text-lg text-white/70 max-w-2xl mx-auto">
            Our integrated Web3 ecosystem gives you real ownership of in-game assets and rewards, all powered by Solana blockchain technology.
          </p>
        </motion.div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 mb-16">
          {/* $MINE & $PATH Tokens */}
          <motion.div 
            className="bg-black/40 backdrop-blur-md p-6 border border-cyan-400/30 rounded-lg"
            initial={{ opacity: 0, x: -20 }}
            whileInView={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={Coins} size="lg" variant="gold" className="mr-4" />
              <h3 className="font-minecraft text-2xl text-minecraft-gold">$MINE & $PATH TOKENS</h3>
            </div>
            <p className="mb-4 text-white/80">
              Earn $MINE through gameplay and $PATH via staking, both with real-world value on Raydium DEX.
            </p>
            
            {/* Token image showcases */}
            <div className="flex justify-center mb-4 gap-4">
              <div className="relative">
                <img src="/images/token_mine.png" alt="MINE Token" className="h-16 w-16 pixelated" />
                <div className="absolute -top-2 -right-2 bg-minecraft-gold text-xs px-1 font-minecraft">$MINE</div>
              </div>
              <div className="relative">
                <img src="/images/token_path.png" alt="PATH Token" className="h-16 w-16 pixelated" />
                <div className="absolute -top-2 -right-2 bg-solana-green text-xs px-1 font-minecraft">$PATH</div>
              </div>
            </div>
            
            <div className="grid grid-cols-2 gap-4 mb-4">
              <div className="bg-black/50 p-4 text-center border border-cyan-400/20">
                <div className="font-minecraft text-minecraft-gold text-lg">$MINE</div>
                <div className="text-sm text-white/80">Utility Token</div>
                <div className="mt-2 text-sm text-white/80">Mine, Farm to earn</div>
              </div>
              <div className="bg-black/50 p-4 text-center border border-cyan-400/20">
                <div className="font-minecraft text-solana-green text-lg">$PATH</div>
                <div className="text-sm text-white/80">Governance Token</div>
                <div className="mt-2 text-sm text-white/80">Stake $MINE to earn</div>
              </div>
            </div>
          </motion.div>
          
          {/* NFT Ecosystem */}
          <motion.div 
            className="bg-black/40 backdrop-blur-md p-6 border border-cyan-400/30 rounded-lg"
            initial={{ opacity: 0, x: 20 }}
            whileInView={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={TrendingUp} size="lg" variant="diamond" className="mr-4" />
              <h3 className="font-minecraft text-2xl text-minecraft-diamond">NFT ECOSYSTEM</h3>
            </div>
            <p className="mb-4 text-white/80">
              Own unique Tools, Pets, Weapons, and Land NFTs that can be traded for profit on Solana marketplaces.
            </p>
            
            {/* NFT item showcase */}
            <div className="flex justify-center mb-4 gap-2 overflow-x-auto py-2">
              {['diamond_pickaxe', 'golden_apple', 'netherite_helmet', 'enchanted_book', 'diamond_sword'].map((item, i) => (
                <div key={i} className="relative bg-black/50 p-2 border border-cyan-400/20">
                  <img src={`/images/${item}.png`} alt={item} className="h-12 w-12 object-contain pixelated" />
                </div>
              ))}
            </div>
            
            <div className="grid grid-cols-2 gap-4">
              <div className="bg-black/50 p-3 text-center border border-cyan-400/20">
                <div className="font-minecraft text-rarity-common text-md">Common</div>
                <div className="text-xs text-white/80">1% Drop Rate</div>
              </div>
              <div className="bg-black/50 p-3 text-center border border-cyan-400/20">
                <div className="font-minecraft text-rarity-uncommon text-md">Uncommon</div>
                <div className="text-xs text-white/80">0.5% Drop Rate</div>
              </div>
              <div className="bg-black/50 p-3 text-center border border-cyan-400/20">
                <div className="font-minecraft text-rarity-rare text-md">Rare</div>
                <div className="text-xs text-white/80">0.1% Drop Rate</div>
              </div>
              <div className="bg-black/50 p-3 text-center border border-cyan-400/20">
                <div className="font-minecraft text-rarity-legendary text-md">Legendary</div>
                <div className="text-xs text-white/80">0.01% Drop Rate</div>
              </div>
            </div>
          </motion.div>
          
          {/* DeFi Staking */}
          <motion.div 
            className="bg-black/40 backdrop-blur-md p-6 border border-cyan-400/30 rounded-lg"
            initial={{ opacity: 0, x: -20 }}
            whileInView={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={Landmark} size="lg" variant="diamond" className="mr-4" />
              <h3 className="font-minecraft text-2xl text-minecraft-emerald">DEFI STAKING</h3>
            </div>
            <p className="mb-4 text-white/80">
              Lock $MINE or NFTs to earn $PATH, with APY up to 50% in Phase 3. The longer you stake, the more you earn!
            </p>
            <div className="bg-black/50 p-4 flex justify-between items-center border border-cyan-400/20">
              <div>
                <div className="font-minecraft text-minecraft-emerald text-lg">STAKE NOW</div>
                <div className="text-sm text-white/80">APY: 25-50%</div>
              </div>
              <div className="font-minecraft text-white text-sm bg-gradient-to-r from-cyan-500 to-blue-600 p-2">
                Phase 3 Feature
              </div>
            </div>
          </motion.div>
          
          {/* Land Ownership */}
          <motion.div 
            className="bg-black/40 backdrop-blur-md p-6 border border-cyan-400/30 rounded-lg"
            initial={{ opacity: 0, x: 20 }}
            whileInView={{ opacity: 1, x: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={Wallet} size="lg" variant="iron" className="mr-4" />
              <h3 className="font-minecraft text-2xl text-minecraft-iron">LAND OWNERSHIP</h3>
            </div>
            <p className="mb-4 text-white/80">
              Purchase Land NFTs to expand your empire, boost resource generation, and rent to other players for passive income.
            </p>
            <div className="bg-black/50 p-4 flex justify-between items-center border border-cyan-400/20">
              <div>
                <div className="font-minecraft text-minecraft-iron text-lg">OWN LAND</div>
                <div className="text-sm text-white/80">16×16 to 64×64 plots</div>
              </div>
              <div className="font-minecraft text-white text-sm bg-gradient-to-r from-cyan-500 to-blue-600 p-2">
                Phase 4 Feature
              </div>
            </div>
          </motion.div>
        </div>
        
        <div className="text-center">
          <WalletModal />
        </div>
      </div>
    </section>
  );
};

export default Web3Economy;
