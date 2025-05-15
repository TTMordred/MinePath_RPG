import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import { Button } from '@/components/ui/button';
import { ArrowLeft, Star, Shield } from 'lucide-react';
import { Pickaxe } from '@/components/ui/icons/Pickaxe';
import { motion } from 'framer-motion';
import { MinecraftCard } from '@/components/ui/minecraft-card';
import { toast } from 'sonner';

// NFT Item type definition
type NFTItem = {
  id: string;
  name: string;
  symbol?: string;
  image: string;
  rarity: 'common' | 'uncommon' | 'rare' | 'epic' | 'legendary' | 'mythic';
  category: 'weapon' | 'armor' | 'tool' | 'pet' | 'cosmetic' | 'resource';
  description: string;
  achievement?: string;
  specialEnchantments?: {
    name: string;
    description: string;
  }[];
  flavorText?: string;
  miningLevel?: string;
  attributes: {
    trait: string;
    value: string;
  }[];
};

// Import the same NFT items from the catalog with updated information
const NFT_ITEMS: NFTItem[] = [
  // Explosion Pickaxes I–V (levels 1–5)
  {
    id: 'exp1',
    name: "Explosion Pickaxe I",
    symbol: "EXPICK1",
    image: "/images/explosion-1.png", // Updated image path
    rarity: "uncommon",
    category: "tool",
    description: "A magical pickaxe with explosion enchantment level I",
    achievement: "Novice Explosion Miner",
    specialEnchantments: [
      { name: "Explosion I", description: "Break blocks in a 3x3 area (vertical plane when mining horizontally, horizontal plane when mining vertically)" }
    ],
    flavorText: "The ground trembles slightly",
    miningLevel: "Uncommon",
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
    symbol: "EXPICK2",
    image: "/images/explosion-2.png", // Updated image path
    rarity: "rare",
    category: "tool",
    description: "A magical pickaxe with explosion enchantment level II",
    achievement: "Skilled Explosion Miner",
    specialEnchantments: [
      { name: "Explosion II", description: "Break blocks in a 4x4 area (vertical plane when mining horizontally, horizontal plane when mining vertically)" }
    ],
    flavorText: "The earth shakes beneath your feet",
    miningLevel: "Rare",
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
    symbol: "EXPICK3",
    image: "/images/explosion-3.png", // Updated image path
    rarity: "epic",
    category: "tool",
    description: "A magical pickaxe with explosion enchantment level III",
    achievement: "Expert Explosion Miner",
    specialEnchantments: [
      { name: "Explosion III", description: "Break blocks in a 5x5 area (vertical plane when mining horizontally, horizontal plane when mining vertically)" }
    ],
    flavorText: "Mountains crumble at your touch",
    miningLevel: "Epic",
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
    symbol: "EXPICK4",
    image: "/images/explosion-4.png", // Updated image path
    rarity: "legendary",
    category: "tool",
    description: "A magical pickaxe with explosion enchantment level IV",
    achievement: "Master Explosion Miner",
    specialEnchantments: [
      { name: "Explosion IV", description: "Break blocks in a 6x6 area (vertical plane when mining horizontally, horizontal plane when mining vertically)" }
    ],
    flavorText: "Your mining power rivals TNT",
    miningLevel: "Legendary",
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
    symbol: "EXPICK5",
    image: "/images/explosion-5.png", // Updated image path
    rarity: "mythic",
    category: "tool",
    description: "A magical pickaxe with ultimate explosion enchantment level V",
    achievement: "Legendary Explosion Miner",
    specialEnchantments: [
      { name: "Explosion V", description: "Break blocks in a 7x7 area (vertical plane when mining horizontally, horizontal plane when mining vertically)" }
    ],
    flavorText: "The earth trembles before this pickaxe",
    miningLevel: "Mythic",
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
    symbol: "LASPICK1",
    image: "/images/laser-1.png", // Keep existing image path
    rarity: "uncommon",
    category: "tool",
    description: "A magical pickaxe with laser enchantment level I",
    achievement: "Novice Laser Miner",
    specialEnchantments: [
      { name: "Laser I", description: "Break blocks up to 2 blocks deep" }
    ],
    flavorText: "Your pickaxe emits a faint beam",
    miningLevel: "Uncommon",
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
    symbol: "LASPICK2",
    image: "/images/laser-2.png", // Keep existing image path
    rarity: "rare",
    category: "tool",
    description: "A magical pickaxe with laser enchantment level II",
    achievement: "Skilled Laser Miner",
    specialEnchantments: [
      { name: "Laser II", description: "Break blocks up to 3 blocks deep" }
    ],
    flavorText: "Your pickaxe cuts through stone with precision",
    miningLevel: "Rare",
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
    symbol: "LASPICK3",
    image: "/images/laser-3.png", // Keep existing image path
    rarity: "epic",
    category: "tool",
    description: "A magical pickaxe with laser enchantment level III",
    achievement: "Expert Laser Miner",
    specialEnchantments: [
      { name: "Laser III", description: "Break blocks up to 4 blocks deep" }
    ],
    flavorText: "Your laser beam penetrates deep into the earth",
    miningLevel: "Epic",
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
    symbol: "LASPICK4",
    image: "/images/laser-4.png", // Keep existing image path
    rarity: "legendary",
    category: "tool",
    description: "A magical pickaxe with laser enchantment level IV",
    achievement: "Master Laser Miner",
    specialEnchantments: [
      { name: "Laser IV", description: "Break blocks up to 5 blocks deep" }
    ],
    flavorText: "Your laser beam can pierce through mountains",
    miningLevel: "Legendary",
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
    symbol: "LASPICK5",
    image: "/images/laser-5.png", // Keep existing image path
    rarity: "mythic",
    category: "tool",
    description: "A magical pickaxe with laser enchantment level V",
    achievement: "Legendary Laser Miner",
    specialEnchantments: [
      { name: "Laser V", description: "Break blocks up to 6 blocks deep" }
    ],
    flavorText: "Cut through stone like butter",
    miningLevel: "Mythic",
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
    symbol: "LUCK1",
    image: "/images/charm-1.png", // Keep existing image path
    rarity: "common",
    category: "resource",
    description: "A magical charm that brings a small amount of luck",
    specialEnchantments: [
      { name: "Luck", description: "+1% chance of better drops" }
    ],
    flavorText: "Fortune favors the bold",
    attributes: [
      { trait: "Luck", value: "+1%" }
    ]
  },
  {
    id: 'charm2',
    name: "Lucky Charm II",
    symbol: "LUCK2",
    image: "/images/charm-2.png", // Keep existing image path
    rarity: "uncommon",
    category: "resource",
    description: "A magical charm that brings a moderate amount of luck",
    specialEnchantments: [
      { name: "Luck", description: "+2% chance of better drops" }
    ],
    flavorText: "Fortune smiles upon you",
    attributes: [
      { trait: "Luck", value: "+2%" }
    ]
  },
  {
    id: 'charm5',
    name: "Lucky Charm V",
    symbol: "LUCK5",
    image: "/images/charm-5.png", // Keep existing image path
    rarity: "rare",
    category: "resource",
    description: "A magical charm that brings a significant amount of luck",
    specialEnchantments: [
      { name: "Luck", description: "+5% chance of better drops" }
    ],
    flavorText: "Lady Luck is on your side",
    attributes: [
      { trait: "Luck", value: "+5%" }
    ]
  },
  {
    id: 'charm10',
    name: "Lucky Charm X",
    symbol: "LUCK10",
    image: "/images/charm-10.png", // Keep existing image path
    rarity: "epic",
    category: "resource",
    description: "A magical charm that brings a substantial amount of luck",
    specialEnchantments: [
      { name: "Luck", description: "+10% chance of better drops" }
    ],
    flavorText: "Luck flows through your veins",
    attributes: [
      { trait: "Luck", value: "+10%" }
    ]
  },
  {
    id: 'charm20',
    name: "Lucky Charm XX",
    symbol: "LUCK20",
    image: "/images/charm-20.png", // Keep existing image path
    rarity: "legendary",
    category: "resource",
    description: "A magical charm that brings an extraordinary amount of luck",
    specialEnchantments: [
      { name: "Luck", description: "+20% chance of better drops" }
    ],
    flavorText: "You are the embodiment of luck itself",
    attributes: [
      { trait: "Luck", value: "+20%" }
    ]
  }
];

// Helper function to get rarity color
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

// Helper function to get rarity text color
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

// Helper function to get category icon
const getCategoryIcon = (category: string) => {
  switch (category) {
    case 'tool': return <Pickaxe className="h-5 w-5" />;
    case 'weapon': return <Shield className="h-5 w-5" />;
    case 'resource': return <Star className="h-5 w-5" />;
    default: return <Shield className="h-5 w-5" />;
  }
};

const NFTDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [nft, setNft] = useState<NFTItem | null>(null);
  const [relatedNfts, setRelatedNfts] = useState<NFTItem[]>([]);
  const navigate = useNavigate();
  
  useEffect(() => {
    // Find the NFT with the matching ID
    const foundNft = NFT_ITEMS.find(item => item.id === id);
    
    if (foundNft) {
      setNft(foundNft);
      
      // Find related NFTs (same category, different ID)
      const related = NFT_ITEMS.filter(
        item => item.category === foundNft.category && item.id !== id
      ).slice(0, 4); // Limit to 4 related items
      
      setRelatedNfts(related);
    } else if (id) {
      // If ID exists but NFT not found, show error
      toast.error('NFT not found', {
        description: `Could not find NFT with ID: ${id}`,
      });
      
      // Redirect to the catalog after a short delay
      setTimeout(() => {
        navigate('/nfts');
      }, 3000);
    }
    
    // Scroll to top when NFT changes
    window.scrollTo(0, 0);
  }, [id, navigate]);

  // If no NFT is found, show a loading state while the potential redirect is happening
  if (!nft) {
    return (
      <div className="min-h-screen flex flex-col bg-background">
        <Navbar />
        <main className="flex-grow flex items-center justify-center">
          <div className="text-center p-8">
            <h1 className="font-minecraft text-2xl mb-4 text-white">
              {id ? 'NFT Not Found' : 'Loading...'}
            </h1>
            <p className="mb-6 text-white/80">
              {id ? 'The NFT you\'re looking for doesn\'t exist.' : 'Fetching NFT details...'}
            </p>
            <Link to="/nfts">
              <Button variant="default" className="font-minecraft">
                <ArrowLeft className="mr-2 h-4 w-4" /> Back to Catalog
              </Button>
            </Link>
          </div>
        </main>
        <Footer />
      </div>
    );
  }

  return (
    <div className="min-h-screen flex flex-col bg-background">
      <Navbar />
      
      <main className="flex-grow">
        {/* NFT Detail Section */}
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
            <div className="mb-8">
              <Link to="/nfts" className="inline-flex items-center font-minecraft text-white/80 hover:text-white transition-colors">
                <ArrowLeft className="mr-2 h-4 w-4" /> Back to NFT Catalog
              </Link>
            </div>
            
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
              {/* NFT Image */}
              <motion.div
                initial={{ opacity: 0, x: -20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5 }}
                className="relative"
              >
                <div className={`aspect-square bg-black/60 backdrop-blur-sm border-4 ${getRarityColor(nft.rarity)} p-8 relative overflow-hidden`}>
                  <div className="absolute inset-0 bg-gradient-to-t from-black/60 via-transparent to-transparent"></div>
                  
                  {/* Symbol and ID display */}
                  {nft.symbol && (
                    <div className="absolute top-4 left-4 z-10">
                      <div className="px-3 py-1 bg-black/70 font-minecraft text-white/80 text-sm">
                        {nft.symbol}
                      </div>
                    </div>
                  )}
                  
                  {/* Rarity badge */}
                  <div className="absolute top-4 right-4 z-10">
                    <div className={`${getRarityTextColor(nft.rarity)} px-3 py-1 bg-black/70 uppercase font-minecraft text-sm`}>
                      {nft.rarity}
                    </div>
                  </div>
                  
                  {/* Pixelated grid overlay */}
                  <div className="absolute inset-0 opacity-10" style={{ 
                    backgroundImage: 'url("/images/pixel_pattern.png")',
                    backgroundSize: '4px 4px',
                    imageRendering: 'pixelated'
                  }}></div>
                  
                  {/* Glowing effect for legendary and epic items */}
                  {(nft.rarity === 'legendary' || nft.rarity === 'epic' || nft.rarity === 'mythic') && (
                    <div className="absolute inset-0 animate-pulse-slow opacity-30" style={{
                      background: `radial-gradient(circle, ${
                        nft.rarity === 'legendary' ? 'rgba(255, 215, 0, 0.3)' : 
                        nft.rarity === 'mythic' ? 'rgba(186, 85, 211, 0.3)' : 
                        'rgba(163, 53, 238, 0.3)'
                      } 0%, transparent 70%)`
                    }}></div>
                  )}
                  
                  <img 
                    src={nft.image} 
                    alt={nft.name}
                    className="pixelated object-contain w-full h-full relative z-10"
                  />
                  
                  {/* Particles for high rarity items */}
                  {(nft.rarity === 'legendary' || nft.rarity === 'epic' || nft.rarity === 'mythic') && (
                    <div className="absolute inset-0 overflow-hidden">
                      {[...Array(15)].map((_, i) => (
                        <div
                          key={i}
                          className="absolute pixelated w-1.5 h-1.5 bg-white opacity-70"
                          style={{
                            top: `${Math.random() * 100}%`,
                            left: `${Math.random() * 100}%`,
                            animation: `float ${3 + Math.random() * 4}s ease-in-out infinite ${Math.random() * 5}s`
                          }}
                        />
                      ))}
                    </div>
                  )}
                </div>
              </motion.div>
              
              {/* NFT Info */}
              <motion.div
                initial={{ opacity: 0, x: 20 }}
                animate={{ opacity: 1, x: 0 }}
                transition={{ duration: 0.5, delay: 0.2 }}
              >
                <div className="inline-block p-1.5 bg-black/70 border border-cyan-400/30 mb-4">
                  <div className="px-4 py-1 font-minecraft text-cyan-400 text-sm flex items-center">
                    {getCategoryIcon(nft.category)}
                    <span className="ml-2">{nft.category.toUpperCase()}</span>
                  </div>
                </div>
                
                <h1 className={`font-minecraft text-3xl lg:text-4xl mb-4 ${getRarityTextColor(nft.rarity)}`}>
                  {nft.name}
                </h1>
                
                <p className="text-lg text-white/80 mb-8 font-minecraft">
                  {nft.description}
                </p>
                
                {/* Special Enchantments Section */}
                {nft.specialEnchantments && nft.specialEnchantments.length > 0 && (
                  <div className="mb-8 bg-black/50 backdrop-blur-sm border border-cyan-400/30 p-4">
                    <h3 className="font-minecraft text-pink-400 text-lg mb-3">Special Enchantments</h3>
                    <div className="space-y-3">
                      {nft.specialEnchantments.map((enchant, idx) => (
                        <div key={idx} className="ml-2">
                          <span className={`font-minecraft ${getRarityTextColor(nft.rarity)}`}>{enchant.name}:</span>
                          <span className="text-white/80 ml-2">{enchant.description}</span>
                        </div>
                      ))}
                    </div>
                    {nft.flavorText && (
                      <div className="mt-4 text-gray-400 italic">"{nft.flavorText}"</div>
                    )}
                  </div>
                )}
                
                {/* Attributes Section */}
                <div className="mb-8">
                  <h3 className="font-minecraft text-xl mb-4 text-white">Enchantments</h3>
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                    {nft.attributes.map((attr, index) => (
                      <div 
                        key={index}
                        className="bg-black/50 backdrop-blur-sm border border-cyan-400/30 p-4"
                      >
                        <div className="text-white/70 text-sm mb-1">{attr.trait}</div>
                        <div className={`text-lg font-minecraft ${getRarityTextColor(nft.rarity)}`}>
                          {attr.value}
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
                
                {/* Achievement Section */}
                {nft.achievement && (
                  <div className="mb-8 p-4 bg-black/50 backdrop-blur-sm border border-cyan-400/30">
                    <h4 className="font-minecraft text-sm text-cyan-400 mb-2">ACHIEVEMENT</h4>
                    <div className="text-white font-minecraft">{nft.achievement}</div>
                  </div>
                )}
              </motion.div>
            </div>
            
            {/* Related NFTs */}
            {relatedNfts.length > 0 && (
              <div className="mt-16">
                <h2 className="font-minecraft text-2xl mb-8 text-white">Related Items</h2>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-6">
                  {relatedNfts.map((related, index) => (
                    <motion.div
                      key={related.id}
                      initial={{ opacity: 0, y: 20 }}
                      animate={{ opacity: 1, y: 0 }}
                      transition={{ duration: 0.4, delay: index * 0.1 }}
                      className="group"
                    >
                      <Link to={`/nfts/${related.id}`}>
                        <MinecraftCard 
                          variant="gradient" 
                          className={`overflow-hidden transition-all duration-300 group-hover:translate-y-[-4px] ${getRarityColor(related.rarity)}`}
                        >
                          <div className="relative h-40 overflow-hidden bg-black/30">
                            <img 
                              src={related.image} 
                              alt={related.name}
                              className="object-contain w-full h-full transition-transform duration-500 group-hover:scale-110 pixelated p-4"
                            />
                            
                            <div className="absolute top-2 right-2">
                              <span className={`text-xs font-minecraft px-2 py-1 ${getRarityTextColor(related.rarity)} bg-black/70 uppercase`}>
                                {related.rarity}
                              </span>
                            </div>
                          </div>
                          
                          <div className="p-3 bg-black/70 backdrop-blur-md">
                            <h3 className={`font-minecraft text-sm mb-1 ${getRarityTextColor(related.rarity)}`}>
                              {related.name}
                            </h3>
                            <span className="text-xs text-white/60 uppercase">{related.category}</span>
                          </div>
                        </MinecraftCard>
                      </Link>
                    </motion.div>
                  ))}
                </div>
              </div>
            )}
          </div>
        </section>
      </main>
      
      <Footer />
    </div>
  );
};

export default NFTDetail;
