import React, { useState, useEffect } from 'react';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Filter, ChevronDown, ChevronUp, Search } from 'lucide-react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { MinecraftCard } from '@/components/ui/minecraft-card';

// NFT Item type definition
type NFTItem = {
  id: string;
  name: string;
  image: string;
  rarity: 'common' | 'uncommon' | 'rare' | 'epic' | 'legendary' | 'mythic';
  category: 'weapon' | 'armor' | 'tool' | 'pet' | 'cosmetic' | 'resource';
  description: string;
  attributes: {
    trait: string;
    value: string;
  }[];
};

const NFT_ITEMS: NFTItem[] = [
  // Explosion Pickaxes I–V (levels 1–5)
  {
    id: 'exp1',
    name: "Explosion Pickaxe I",
    image: "/images/explosion-1.png",
    rarity: "uncommon",
    category: "tool",
    description: "Mine blocks faster and with higher chance of rare drops.",
    attributes: [
      { trait: "Efficiency", value: "III" },
      { trait: "Fortune", value: "I" },
      { trait: "Durability", value: "II" },
      { trait: "Explosion", value: "I" }
    ]
  },
  {
    id: 'exp2',
    name: "Explosion Pickaxe II",
    image: "/images/explosion-2.png",
    rarity: "rare",
    category: "tool",
    description: "Enhanced explosion core boosts mining speed and drop rate.",
    attributes: [
      { trait: "Efficiency", value: "IV" },
      { trait: "Fortune", value: "II" },
      { trait: "Durability", value: "III" },
      { trait: "Explosion", value: "II" }
    ]
  },
  {
    id: 'exp3',
    name: "Explosion Pickaxe III",
    image: "/images/explosion-3.png",
    rarity: "epic",
    category: "tool",
    description: "High-tier explosion technology for maximum block yield.",
    attributes: [
      { trait: "Efficiency", value: "IV" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Explosion", value: "III" }
    ]
  },
  {
    id: 'exp4',
    name: "Explosion Pickaxe IV",
    image: "/images/explosion-4.png",
    rarity: "legendary",
    category: "tool",
    description: "Advanced explosion pickaxe with superior mining performance.",
    attributes: [
      { trait: "Efficiency", value: "V" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Mending", value: "I" },
      { trait: "Explosion", value: "IV" }
    ]
  },
  {
    id: 'exp5',
    name: "Explosion Pickaxe V",
    image: "/images/explosion-5.png",
    rarity: "mythic",
    category: "tool",
    description: "Ultimate explosion pickaxe for unparalleled mining efficiency.",
    attributes: [
      { trait: "Efficiency", value: "V" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Mending", value: "I" },
      { trait: "Silk Touch", value: "I" },
      { trait: "Explosion", value: "V" }
    ]
  },

  // Laser Pickaxes I–V (levels 1–5)
  {
    id: 'laser1',
    name: "Laser Pickaxe I",
    image: "/images/laser-1.png",
    rarity: "uncommon",
    category: "tool",
    description: "Basic laser module that slices through blocks with ease.",
    attributes: [
      { trait: "Efficiency", value: "III" },
      { trait: "Fortune", value: "I" },
      { trait: "Durability", value: "II" },
      { trait: "Laser", value: "I" }
    ]
  },
  {
    id: 'laser2',
    name: "Laser Pickaxe II",
    image: "/images/laser-2.png",
    rarity: "rare",
    category: "tool",
    description: "Upgraded laser lens for increased block penetration.",
    attributes: [
      { trait: "Efficiency", value: "IV" },
      { trait: "Fortune", value: "II" },
      { trait: "Durability", value: "III" },
      { trait: "Laser", value: "II" }
    ]
  },
  {
    id: 'laser3',
    name: "Laser Pickaxe III",
    image: "/images/laser-3.png",
    rarity: "epic",
    category: "tool",
    description: "High-intensity laser for rapid block breaking.",
    attributes: [
      { trait: "Efficiency", value: "IV" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Laser", value: "III" }
    ]
  },
  {
    id: 'laser4',
    name: "Laser Pickaxe IV",
    image: "/images/laser-4.png",
    rarity: "legendary",
    category: "tool",
    description: "Advanced laser emitter for pinpoint accuracy.",
    attributes: [
      { trait: "Efficiency", value: "V" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Mending", value: "I" },
      { trait: "Laser", value: "IV" }
    ]
  },
  {
    id: 'laser5',
    name: "Laser Pickaxe V",
    image: "/images/laser-5.png",
    rarity: "mythic",
    category: "tool",
    description: "Cutting-edge laser technology for the ultimate mining tool.",
    attributes: [
      { trait: "Efficiency", value: "V" },
      { trait: "Fortune", value: "III" },
      { trait: "Durability", value: "III" },
      { trait: "Mending", value: "I" },
      { trait: "Silk Touch", value: "I" },
      { trait: "Laser", value: "V" }
    ]
  },

  // Lucky Charms (levels 1, 2, 5, 10, 20)
  {
    id: 'charm1',
    name: "Lucky Charm I",
    image: "/images/charm-1.png",
    rarity: "common",
    category: "resource",
    description: "A magical charm that brings a small amount of luck.",
    attributes: [
      { trait: "Luck", value: "+1%" }
    ]
  },
  {
    id: 'charm2',
    name: "Lucky Charm II",
    image: "/images/charm-2.png",
    rarity: "uncommon",
    category: "resource",
    description: "A magical charm that brings a moderate amount of luck.",
    attributes: [
      { trait: "Luck", value: "+2%" }
    ]
  },
  {
    id: 'charm5',
    name: "Lucky Charm V",
    image: "/images/charm-5.png",
    rarity: "rare",
    category: "resource",
    description: "A magical charm that brings a significant amount of luck.",
    attributes: [
      { trait: "Luck", value: "+5%" }
    ]
  },
  {
    id: 'charm10',
    name: "Lucky Charm X",
    image: "/images/charm-10.png",
    rarity: "epic",
    category: "resource",
    description: "A magical charm that brings a substantial amount of luck.",
    attributes: [
      { trait: "Luck", value: "+10%" }
    ]
  },
  {
    id: 'charm20',
    name: "Lucky Charm XX",
    image: "/images/charm-20.png",
    rarity: "legendary",
    category: "resource",
    description: "A magical charm that brings an extraordinary amount of luck.",
    attributes: [
      { trait: "Luck", value: "+20%" }
    ]
  }
];

// Filter categories
const CATEGORIES = [
  { value: 'all', label: 'All Items' },
  { value: 'weapon', label: 'Weapons' },
  { value: 'armor', label: 'Armor' },
  { value: 'tool', label: 'Tools' },
  { value: 'pet', label: 'Pets' },
  { value: 'cosmetic', label: 'Cosmetics' },
  { value: 'resource', label: 'Resources' },
];

// Rarity filters
const RARITIES = [
  { value: 'all', label: 'All Rarities' },
  { value: 'common', label: 'Common', color: 'text-rarity-common' },
  { value: 'uncommon', label: 'Uncommon', color: 'text-rarity-uncommon' },
  { value: 'rare', label: 'Rare', color: 'text-rarity-rare' },
  { value: 'epic', label: 'Epic', color: 'text-rarity-epic' },
  { value: 'legendary', label: 'Legendary', color: 'text-rarity-legendary' },
  { value: 'mythic', label: 'Mythic', color: 'text-purple-600' },
];

const NFTCatalog = () => {
  const [activeCategory, setActiveCategory] = useState('all');
  const [activeRarity, setActiveRarity] = useState('all');
  const [searchTerm, setSearchTerm] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  
  useEffect(() => {
    window.scrollTo(0, 0);
  }, []);

  // Filter NFTs based on selected criteria
  const filteredNFTs = NFT_ITEMS.filter(nft => {
    const matchesCategory = activeCategory === 'all' || nft.category === activeCategory;
    const matchesRarity = activeRarity === 'all' || nft.rarity === activeRarity;
    const matchesSearch = nft.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
                          nft.description.toLowerCase().includes(searchTerm.toLowerCase());
    
    return matchesCategory && matchesRarity && matchesSearch;
  });

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Navbar />
      
      <main className="flex-grow">
        {/* Hero section */}
        <section className="relative py-16 md:py-24 overflow-hidden">
          {/* Background with overlay */}
          <div className="absolute inset-0 z-0">
            <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/80"></div>
            <div className="absolute inset-0 bg-[url('/images/bg-icemountain.png')] bg-no-repeat bg-cover bg-center opacity-40"></div>
            <div className="absolute left-0 inset-y-0 w-16 opacity-20">
              <div className="h-full w-full bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat-y"></div>
            </div>
          </div>

          <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div className="max-w-3xl mx-auto text-center mb-12">
              <motion.div
                initial={{ opacity: 0, y: 20 }}
                animate={{ opacity: 1, y: 0 }}
                transition={{ duration: 0.5 }}
                className="inline-block p-1.5 rounded-none bg-gradient-to-r from-cyan-500/20 to-blue-500/20 mb-4"
              >
                <div className="px-4 py-1.5 rounded-none bg-black/60 backdrop-blur-sm text-sm font-minecraft text-cyan-400">
                  DISCOVER NFTs
                </div>
              </motion.div>
              
              <motion.h1 
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.2 }}
                className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white"
              >
                <span className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white">
                  NFT COLLECTION
                </span>
              </motion.h1>
              
              <motion.p
                initial={{ opacity: 0 }}
                animate={{ opacity: 1 }}
                transition={{ duration: 0.5, delay: 0.3 }}
                className="text-base lg:text-lg text-white/80 max-w-2xl mx-auto mb-8"
              >
                Browse our complete collection of in-game NFTs. Find weapons, armor, tools, and more to enhance your gameplay experience.
              </motion.p>
              
              <Link to="/">
                <Button 
                  variant="outline" 
                  size="lg"
                  className="border-solana-purple bg-solana-purple/10 text-solana-purple hover:bg-solana-purple/20 group"
                >
                  <ArrowLeft className="mr-2 h-4 w-4 transition-transform group-hover:-translate-x-1" /> 
                  Back to Home
                </Button>
              </Link>
            </div>
            
            {/* Search and filter */}
            <div className="mb-10">
              <div className="flex flex-col md:flex-row gap-4 mb-4">
                <div className="relative flex-grow">
                  <input
                    type="text"
                    placeholder="Search NFTs..."
                    value={searchTerm}
                    onChange={(e) => setSearchTerm(e.target.value)}
                    className="w-full py-3 px-4 pl-10 bg-black/50 backdrop-blur-sm border border-cyan-400/30 text-white placeholder:text-white/50 font-minecraft"
                  />
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-white/70 h-5 w-5" />
                </div>
                
                <button 
                  onClick={() => setShowFilters(!showFilters)}
                  className="md:w-auto w-full py-3 px-5 bg-black/50 backdrop-blur-sm border border-cyan-400/30 text-white flex items-center justify-center font-minecraft hover:bg-black/70 transition-colors"
                >
                  <Filter className="mr-2 h-5 w-5" />
                  Filters
                  {showFilters ? 
                    <ChevronUp className="ml-2 h-4 w-4" /> : 
                    <ChevronDown className="ml-2 h-4 w-4" />
                  }
                </button>
              </div>
              
              {/* Expandable filters */}
              {showFilters && (
                <motion.div 
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: "auto" }}
                  exit={{ opacity: 0, height: 0 }}
                  className="bg-black/50 backdrop-blur-sm border border-cyan-400/30 p-4 mb-4"
                >
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    <div>
                      <h3 className="font-minecraft text-white mb-2 text-sm">Category</h3>
                      <div className="flex flex-wrap gap-2">
                        {CATEGORIES.map(category => (
                          <button
                            key={category.value}
                            onClick={() => setActiveCategory(category.value)}
                            className={`px-3 py-1 text-xs font-minecraft ${
                              activeCategory === category.value
                                ? 'bg-cyan-500 text-black'
                                : 'bg-black/60 text-white hover:bg-black/80'
                            } transition-colors`}
                          >
                            {category.label}
                          </button>
                        ))}
                      </div>
                    </div>
                    
                    <div>
                      <h3 className="font-minecraft text-white mb-2 text-sm">Rarity</h3>
                      <div className="flex flex-wrap gap-2">
                        {RARITIES.map(rarity => (
                          <button
                            key={rarity.value}
                            onClick={() => setActiveRarity(rarity.value)}
                            className={`px-3 py-1 text-xs font-minecraft ${
                              activeRarity === rarity.value
                                ? rarity.value === 'all'
                                  ? 'bg-white text-black'
                                  : `bg-black/60 ${rarity.color}`
                                : 'bg-black/60 text-white hover:bg-black/80'
                            } transition-colors ${
                              activeRarity === rarity.value && rarity.value !== 'all'
                                ? 'border border-current'
                                : ''
                            }`}
                          >
                            {rarity.label}
                          </button>
                        ))}
                      </div>
                    </div>
                  </div>
                </motion.div>
              )}
            </div>
            
            {/* NFT Grid */}
            {filteredNFTs.length > 0 ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6 mb-16">
                {filteredNFTs.map((nft, index) => (
                  <NFTCard key={nft.id} nft={nft} index={index} />
                ))}
              </div>
            ) : (
              <div className="text-center py-16">
                <div className="font-minecraft text-2xl text-white/80 mb-4">No NFTs Found</div>
                <p className="text-white/60 max-w-lg mx-auto">
                  Try adjusting your search criteria or filters to see more results.
                </p>
              </div>
            )}
          </div>
        </section>
      </main>
      
      <Footer />
    </div>
  );
};

// NFT Card component
const NFTCard = ({ nft, index }: { nft: NFTItem; index: number }) => {
  const getRarityColor = (rarity: string) => {
    switch (rarity) {
      case 'common': return 'border-rarity-common';
      case 'uncommon': return 'border-rarity-uncommon';
      case 'rare': return 'border-rarity-rare';
      case 'epic': return 'border-rarity-epic';
      case 'legendary': return 'border-rarity-legendary';
      case 'mythic': return 'border-purple-600';
      default: return '';
    }
  };
  
  const getRarityTextColor = (rarity: string) => {
    switch (rarity) {
      case 'common': return 'text-rarity-common';
      case 'uncommon': return 'text-rarity-uncommon';
      case 'rare': return 'text-rarity-rare';
      case 'epic': return 'text-rarity-epic';
      case 'legendary': return 'text-rarity-legendary';
      case 'mythic': return 'text-purple-600';
      default: return '';
    }
  };

  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      animate={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.1 }}
      className="group h-full"
    >
      <MinecraftCard 
        variant="gradient" 
        className={`overflow-hidden transition-all duration-300 group-hover:translate-y-[-4px] h-full ${getRarityColor(nft.rarity)}`}
      >
        <div className="relative h-48 md:h-64 overflow-hidden bg-black/30">
          <img 
            src={nft.image} 
            alt={nft.name}
            className="object-contain w-full h-full transition-transform duration-500 group-hover:scale-110 pixelated p-4"
          />
          
          <div className="absolute inset-0 bg-gradient-to-t from-black/80 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300"></div>
          
          <div className="absolute top-2 right-2">
            <span className={`text-xs font-minecraft px-2 py-1 ${getRarityTextColor(nft.rarity)} bg-black/70 uppercase`}>
              {nft.rarity}
            </span>
          </div>
          
          <div className="absolute bottom-0 left-0 right-0 p-4 transform translate-y-full group-hover:translate-y-0 transition-transform duration-300">
            <div className="space-y-1">
              {nft.attributes.map((attr, index) => (
                <div key={index} className="flex justify-between text-xs">
                  <span className="text-gray-300">{attr.trait}:</span>
                  <span className={`${getRarityTextColor(nft.rarity)}`}>{attr.value}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
        
        <div className="p-4 bg-black/70 backdrop-blur-md">
          <h3 className={`font-minecraft text-lg mb-2 ${getRarityTextColor(nft.rarity)}`}>{nft.name}</h3>
          <p className="text-sm text-white/80 mb-4 h-12 overflow-hidden">{nft.description}</p>
          
          <div className="flex justify-between items-center">
            <span className="text-xs text-white/60 uppercase">{nft.category}</span>
            <Link to={`/nfts/${nft.id}`}>
              <button className={`text-sm bg-black/80 px-3 py-1 border ${getRarityColor(nft.rarity)} ${getRarityTextColor(nft.rarity)} hover:bg-black transition-colors duration-300`}>
                View Details
              </button>
            </Link>
          </div>
        </div>
      </MinecraftCard>
    </motion.div>
  );
};

export default NFTCatalog;
