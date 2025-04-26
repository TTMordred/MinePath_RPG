
import React, { useState } from 'react';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import { Button } from '@/components/ui/button';
import { ArrowLeft, ChevronRight, Search, ShoppingCart } from 'lucide-react';
import { Link } from 'react-router-dom';
import { motion } from 'framer-motion';
import { MinecraftIcon } from '@/components/ui/minecraft-icon';
import { MinecraftCard } from '@/components/ui/minecraft-card';

// Sample store items
const STORE_ITEMS = [
  {
    id: 1,
    name: "Diamond Sword",
    image: "/images/diamond_sword.png",
    price: 500,
    currency: "BLOC",
    category: "weapons",
    description: "A powerful sword that deals extra damage to enemies."
  },
  {
    id: 2,
    name: "Ender Pearl",
    image: "/images/ender_pearl.png",
    price: 250,
    currency: "BLOC",
    category: "consumables",
    description: "Teleport instantly to your targeted location."
  },
  {
    id: 3,
    name: "Golden Apple",
    image: "/images/golden_apple.png",
    price: 300,
    currency: "BLOC",
    category: "consumables",
    description: "Restores health and provides temporary resistance."
  },
  {
    id: 4,
    name: "Diamond Pickaxe",
    image: "/images/diamond_pickaxe.png",
    price: 450,
    currency: "BLOC",
    category: "tools",
    description: "Mine blocks faster with increased durability."
  },
  {
    id: 5,
    name: "Enchanted Book",
    image: "/images/enchanted_book.png",
    price: 800,
    currency: "BLOC",
    category: "enchantments",
    description: "Apply special enchantments to your weapons and tools."
  },
  {
    id: 6,
    name: "Netherite Helmet",
    image: "/images/netherite_helmet.png",
    price: 1200,
    currency: "BLOC",
    category: "armor",
    description: "Superior protection from all types of damage."
  }
];

const categories = [
  "all", "weapons", "tools", "armor", "consumables", "enchantments"
];

const StorePage = () => {
  const [selectedCategory, setSelectedCategory] = useState("all");
  const [searchQuery, setSearchQuery] = useState("");
  
  const filteredItems = STORE_ITEMS.filter(item => {
    return (
      (selectedCategory === "all" || item.category === selectedCategory) &&
      (searchQuery === "" || item.name.toLowerCase().includes(searchQuery.toLowerCase()))
    );
  });

  return (
    <div className="min-h-screen flex flex-col minecraft-wood-bg">
      <Navbar />
      <main className="flex-grow py-20">
        <div className="container mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center mb-8">
            <Link to="/">
              <button className="minecraft-btn bg-minecraft-stone border-minecraft-stone/50 text-white flex items-center justify-center group">
                <ArrowLeft className="mr-2 h-4 w-4 transition-transform group-hover:-translate-x-1" /> 
                Back to Home
              </button>
            </Link>
          </div>
          
          <motion.div 
            className="minecraft-panel mb-10"
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
          >
            <div className="flex flex-col md:flex-row items-center justify-between gap-4 p-6">
              <div>
                <h1 className="font-minecraft text-3xl md:text-4xl text-black mb-2">GAME STORE</h1>
                <p className="text-sm text-black/70 font-minecraft">Purchase in-game items using $BLOC tokens</p>
              </div>
              
              <div className="flex items-center space-x-4">
                <div className="relative">
                  <input
                    type="text"
                    placeholder="Search items..."
                    className="bg-black/60 font-minecraft border-2 border-minecraft-dirt pl-10 pr-4 py-2 w-64 text-white placeholder:text-gray-400"
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                  />
                  <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 h-4 w-4" />
                </div>
                
                <div className="relative">
                  <button className="minecraft-btn-green">
                    <ShoppingCart className="h-4 w-4 mr-2" /> Cart (0)
                  </button>
                </div>
              </div>
            </div>
          </motion.div>
          
          {/* Category tabs - Minecraft style */}
          <div className="flex overflow-x-auto space-x-2 mb-8 pb-2">
            {categories.map((category) => (
              <button
                key={category}
                onClick={() => setSelectedCategory(category)}
                className={`minecraft-btn ${
                  selectedCategory === category 
                    ? 'minecraft-btn-green' 
                    : 'bg-minecraft-stone border-gray-700 text-white hover:bg-black/80'
                } whitespace-nowrap capitalize min-w-[120px] font-minecraft`}
              >
                {category}
              </button>
            ))}
          </div>
          
          {/* Store items grid */}
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 mb-10">
            {filteredItems.map((item) => (
              <motion.div 
                key={item.id}
                className="minecraft-inventory-slot group"
                initial={{ opacity: 0, scale: 0.9 }}
                animate={{ opacity: 1, scale: 1 }}
                transition={{ duration: 0.3 }}
                whileHover={{ y: -5 }}
              >
                <div className="p-4 flex flex-col items-center">
                  <div className="relative mb-4">
                    <img 
                      src={item.image} 
                      alt={item.name} 
                      className="w-16 h-16 object-contain pixelated"
                    />
                    <div className="absolute -top-3 -right-3 bg-minecraft-gold text-black text-xs font-minecraft px-2 py-1">
                      {item.price} {item.currency}
                    </div>
                  </div>
                  
                  <h3 className="font-minecraft text-lg mb-2 text-center text-white">{item.name}</h3>
                  <p className="text-xs text-gray-300 text-center mb-4 font-minecraft">{item.description}</p>
                  
                  <button className="minecraft-btn-green text-sm w-full">
                    Add to Cart
                  </button>
                </div>
              </motion.div>
            ))}
          </div>
          
          {/* Pagination - Minecraft style */}
          <div className="flex justify-center">
            <button className="minecraft-btn bg-minecraft-stone border-gray-700 text-white px-3 font-minecraft">Previous</button>
            <div className="mx-2 flex">
              {[1, 2, 3].map(page => (
                <button 
                  key={page} 
                  className={`w-10 h-10 flex items-center justify-center font-minecraft ${
                    page === 1 
                      ? 'minecraft-btn-green' 
                      : 'minecraft-btn bg-minecraft-stone border-gray-700 text-white'
                  }`}
                >
                  {page}
                </button>
              ))}
            </div>
            <button className="minecraft-btn bg-minecraft-stone border-gray-700 text-white px-3 font-minecraft">Next</button>
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default StorePage;
