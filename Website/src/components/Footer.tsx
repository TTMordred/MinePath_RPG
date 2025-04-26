
import React from 'react';
import { Link } from 'react-router-dom';
import { Github, Twitter, Zap, ArrowRight, MessageSquare, Globe, Heart } from 'lucide-react';
import { motion } from 'framer-motion';
import { useIsMobile } from '@/hooks/use-mobile';

const FooterLink = ({ href, text }: { href: string; text: string }) => (
  <li className="mb-2.5">
    <Link to={href} className="text-white hover:text-cyan-400 transition-colors text-sm font-minecraft relative group">
      {text}
      <span className="absolute -bottom-1 left-0 h-0.5 w-0 bg-cyan-400 transition-all duration-300 group-hover:w-full"></span>
    </Link>
  </li>
);

const Footer = () => {
  const isMobile = useIsMobile();

  return (
    <footer className="relative border-t-4 border-cyan-400/30 pt-12 md:pt-16 pb-6 overflow-hidden" style={{ 
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
          {[...Array(isMobile ? 5 : 10)].map((_, i) => (
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
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-8 md:gap-10">
          <motion.div 
            className="col-span-1 md:col-span-1"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
          >
            <Link to="/" className="block mb-6 group">
              <div className="flex items-center">
                <img src="/images/minecraft-logo.png" alt="MinePath" className="h-8 w-auto mb-2 pixelated group-hover:scale-110 transition-transform duration-300" />
                <h3 className="font-minecraft text-xl text-cyan-400 ml-2">
                  MinePath
                </h3>
              </div>
            </Link>
            <p className="text-sm text-white/80 mb-6 font-minecraft bg-black/30 backdrop-blur-sm border border-cyan-400/20 p-3">
              Building the future of Minecraft gameplay with Solana blockchain integration.
            </p>
            <div className="flex space-x-4">
              <a 
                href="https://x.com/MinePath_RPG" 
                target="_blank" 
                rel="noopener noreferrer"
                className="w-10 h-10 bg-black/40 backdrop-blur-sm border border-cyan-400/30 text-white flex items-center justify-center hover:bg-cyan-500/20 transition-colors"
              >
                <Twitter size={16} className="text-cyan-400" />
              </a>
              <a 
                href="https://discord.gg/HDc5QYXz" 
                target="_blank" 
                rel="noopener noreferrer"
                className="w-10 h-10 bg-black/40 backdrop-blur-sm border border-cyan-400/30 text-white flex items-center justify-center hover:bg-cyan-500/20 transition-colors"
              >
                <MessageSquare size={16} className="text-cyan-400" />
              </a>
              <a 
                href="https://github.com" 
                target="_blank" 
                rel="noopener noreferrer"
                className="w-10 h-10 bg-black/40 backdrop-blur-sm border border-cyan-400/30 text-white flex items-center justify-center hover:bg-cyan-500/20 transition-colors"
              >
                <Github size={16} className="text-cyan-400" />
              </a>
            </div>
          </motion.div>
          
          <motion.div 
            className="col-span-1 md:col-span-1"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.1 }}
            viewport={{ once: true }}
          >
            <h4 className="font-minecraft text-lg mb-4 text-cyan-400 border-b border-cyan-400/30 pb-2">QUICK LINKS</h4>
            <ul>
              <FooterLink href="/" text="Home" />
              <FooterLink href="/nfts" text="NFT Catalog" />
              <FooterLink href="/how-to-play" text="How to Play" />
              <FooterLink href="/store" text="Store" />
            </ul>
          </motion.div>
          
          <motion.div 
            className="col-span-1 md:col-span-1"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
          >
            <h4 className="font-minecraft text-lg mb-4 text-cyan-400 border-b border-cyan-400/30 pb-2">RESOURCES</h4>
            <ul>
              <FooterLink href="/wiki" text="Wiki" />
              <FooterLink href="/gallery" text="NFT Gallery" />
              <FooterLink href="/roadmap" text="Roadmap" />
              <FooterLink href="/support" text="Support" />
            </ul>
          </motion.div>
          
          <motion.div 
            className="col-span-1 md:col-span-1"
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
          >
            <h4 className="font-minecraft text-lg mb-4 text-cyan-400 border-b border-cyan-400/30 pb-2">PLAY NOW</h4>
            <div className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-4 md:p-5">
              <p className="text-sm text-white/80 mb-3 font-minecraft">
                Connect to our Minecraft server 
              </p>
              <div className="bg-black/50 border border-cyan-400/20 px-3 py-2 mb-3 flex items-center justify-between">
                <span className="font-minecraft text-sm text-cyan-400">play.minepath.com</span>
                <Zap className="h-4 w-4 text-cyan-400" />
              </div>
              <button className="w-full bg-gradient-to-r from-cyan-500 to-blue-600 hover:from-cyan-400 hover:to-blue-500 transition-colors text-white py-2 px-4 flex items-center justify-center text-sm font-minecraft">
                Join Discord <ArrowRight className="ml-2 h-4 w-4" />
              </button>
            </div>
          </motion.div>
        </div>
        
        <div className="border-t border-cyan-400/30 mt-8 md:mt-10 pt-6 flex flex-col md:flex-row justify-between items-center">
          <p className="text-xs text-white/60 font-minecraft flex items-center">
            <Heart size={12} className="text-cyan-400 mr-1" /> © 2025 MinePath. All rights reserved.
          </p>
          <div className="mt-4 md:mt-0 flex gap-6">
            <a href="/privacy" className="text-xs text-white/60 hover:text-cyan-400 transition-colors font-minecraft">
              Privacy Policy
            </a>
            <a href="/terms" className="text-xs text-white/60 hover:text-cyan-400 transition-colors font-minecraft">
              Terms of Service
            </a>
          </div>
        </div>
      </div>
      
      {/* Decorative elements */}
      <div className="absolute bottom-0 left-0 w-full h-1 bg-gradient-to-r from-cyan-500 to-blue-600"></div>
    </footer>
  );
};

export default Footer;
