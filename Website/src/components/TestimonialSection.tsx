import React from 'react';
import { motion } from 'framer-motion';
import { Star, Quote } from 'lucide-react';

const testimonials = [
  {
    id: 1,
    name: "Alex_Miner",
    role: "Early Access Player",
    avatar: "/images/user-icon.png",
    quote: "MinePath revolutionized my Minecraft experience. The NFT drops while mining feel rewarding and exciting. It's like treasure hunting but with real value!",
    rating: 5,
  },
  {
    id: 2,
    name: "BlockQueen",
    role: "Guild Leader",
    avatar: "/images/user-icon.png",
    quote: "As a guild leader, MinePath offers amazing opportunities for community building. The tokenomics are well-balanced and the NFTs are genuinely useful in-game.",
    rating: 5,
  },
  {
    id: 3,
    name: "CryptoDigger",
    role: "Veteran Player",
    avatar: "/images/user-icon.png",
    quote: "I've played many blockchain games, but MinePath captures the Minecraft essence while adding meaningful Web3 elements. The server performance is also excellent.",
    rating: 4,
  }
];

const TestimonialSection = () => {
  return (
    <section className="relative py-24 overflow-hidden" style={{ 
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
        <div className="text-center mb-16">
          <div className="inline-block p-1.5 rounded-full bg-gradient-to-r from-cyan-500/20 to-blue-500/20 mb-5">
            <div className="px-4 py-1 rounded-full bg-black/60 backdrop-blur-sm text-sm font-medium text-cyan-400">
              PLAYER TESTIMONIALS
            </div>
          </div>
          
          <motion.h2
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true, margin: "-100px" }}
            className="font-minecraft text-4xl md:text-5xl mb-6 text-white"
          >
            What Our Community <span className="text-cyan-400">Says</span>
          </motion.h2>
          
          <motion.p
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true, margin: "-100px" }}
            className="text-lg text-white/80 max-w-2xl mx-auto"
          >
            Join thousands of players already experiencing the fusion of Minecraft gameplay with the earning potential of NFTs.
          </motion.p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
          {testimonials.map((testimonial, index) => (
            <motion.div
              key={testimonial.id}
              initial={{ opacity: 0, y: 30 }}
              whileInView={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.1 * index }}
              viewport={{ once: true, margin: "-100px" }}
            >
              <TestimonialCard testimonial={testimonial} />
            </motion.div>
          ))}
        </div>
      </div>
    </section>
  );
};

const TestimonialCard = ({ testimonial }) => {
  return (
    <div className="glass-card h-full flex flex-col">
      <div className="p-6">
        <div className="flex items-center mb-4">
          <div className="mr-3">
            <img
              src={testimonial.avatar}
              alt={testimonial.name}
              className="w-12 h-12 pixelated"
            />
          </div>
          <div>
            <h3 className="font-minecraft text-lg text-cyan-400">{testimonial.name}</h3>
            <p className="text-sm text-white/60">{testimonial.role}</p>
          </div>
        </div>
        
        <div className="flex mb-4">
          {[...Array(5)].map((_, i) => (
            <Star
              key={i}
              className={`h-4 w-4 ${
                i < testimonial.rating ? "text-yellow-400" : "text-gray-500"
              }`}
              fill={i < testimonial.rating ? "currentColor" : "none"}
            />
          ))}
        </div>
        
        <div className="relative">
          <Quote className="absolute -top-2 -left-2 h-6 w-6 text-cyan-400/30" />
          <p className="text-white/80 text-sm leading-relaxed pt-2 pl-4">
            "{testimonial.quote}"
          </p>
        </div>
      </div>
      
      <div className="mt-auto">
        <div className="h-1 bg-gradient-to-r from-cyan-400 to-blue-500"></div>
      </div>
    </div>
  );
};

export default TestimonialSection;
