import React from 'react';
import { motion } from 'framer-motion';
import { Twitter } from 'lucide-react';
import { MinecraftIcon } from '@/components/ui/minecraft-icon';

type TeamMember = {
  id: number;
  name: string;
  role: string;
  twitter: string;
  image: string;
  description: string;
}

const TEAM_MEMBERS: TeamMember[] = [
  {
    id: 1,
    name: "TTMordred",
    role: "Team Leader",
    twitter: "https://twitter.com/TTMordred",
    image: "/images/TTMordred.png",
    description: "Leads project coordination and feature planning for MinePath."
  },
  {
    id: 2,
    name: "Solaze",
    role: "CTO & Smart Contract Developer",
    twitter: "https://x.com/Solazezz",
    image: "/images/solaze.png",
    description: "Builds and audits smart contracts on Solana for our core prototype."
  },
  {
    id: 3,
    name: "Duynamiq",
    role: "Creative Director",
    twitter: "https://x.com/duynamiq",
    image: "/images/duynamiq.png",
    description: "Designs pixel art assets and UI mockups to define MinePath’s style."
  },
  {
    id: 4,
    name: "HngPhn",
    role: " Tokenomics Advisor",
    twitter: "https://x.com/HngPhan0612",
    image: "/images/HngPhn.png",
    description: "Designs the dual-token economy and staking mechanics for MinePath."
  },
  {
    id: 5,
    name: "tienz",
    role: "Plugins Manager",
    twitter: "https://x.com/PhanTin45497753",
    image: "/images/tienz.png",
    description: "Manages the development of plugins and mods for MinePath’s ecosystem."
  }
];


const TeamMembers = () => {
  return (
    <section className="relative py-16 md:py-24 overflow-hidden">
      {/* Background with overlay */}
      <div className="absolute inset-0 z-0">
        <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/80"></div>
        <div className="absolute inset-0 bg-[url('/images/bg-sunset.png')] bg-no-repeat bg-cover bg-center opacity-40"></div>
        <div className="absolute left-0 inset-y-0 w-16 opacity-20">
          <div className="h-full w-full bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat-y"></div>
        </div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="text-center mb-12 md:mb-16">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="inline-block p-1.5 rounded-none bg-gradient-to-r from-blue-600/20 to-purple-600/20 mb-5"
          >
            <div className="px-4 py-1.5 font-minecraft text-solana-purple text-sm border-b border-solana-purple/30">
              OUR TEAM
            </div>
          </motion.div>

          <motion.h2 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
            className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white"
          >
            <span className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white">
              MEET THE <span className="text-blue-500">BUILDERS</span>
            </span>
          </motion.h2>

          <motion.p 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
            className="text-base lg:text-lg text-white/80 max-w-3xl mx-auto"
          >
            Our team of experienced developers, designers, and blockchain experts are building the future of play-to-earn Minecraft gaming.
          </motion.p>
        </div>
        
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-5 gap-6 mb-12">
          {TEAM_MEMBERS.map((member, index) => (
            <TeamMemberCard 
              key={member.id} 
              member={member} 
              index={index} 
            />
          ))}
        </div>
      </div>
    </section>
  );
};

const TeamMemberCard = ({ member, index }: { member: TeamMember, index: number }) => {
  return (
    <motion.div
      initial={{ opacity: 0, y: 20 }}
      whileInView={{ opacity: 1, y: 0 }}
      transition={{ duration: 0.4, delay: index * 0.1 }}
      viewport={{ once: true }}
      className="bg-black/40 backdrop-blur-sm border border-solana-purple/30 overflow-hidden group flex flex-col h-full"
    >
      <div className="relative h-48 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-b from-solana-purple/20 via-transparent to-black/80 z-10"></div>
        <img 
          src={member.image} 
          alt={member.name} 
          className="w-full h-full object-cover pixelated" 
        />
      </div>
      
      <div className="p-4 text-center flex flex-col flex-grow">
        <h3 className="font-minecraft text-xl text-white mb-1">{member.name}</h3>
        <div className="text-sm text-solana-purple mb-3">{member.role}</div>
        <p className="text-sm text-white/70 mb-4 flex-grow">{member.description}</p>
        
        <div className="mt-auto">
          <a 
            href={member.twitter}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-flex items-center justify-center text-sm font-minecraft px-3 py-2 text-white bg-gradient-to-r from-solana-blue/20 to-solana-purple/20 border border-solana-purple/30 hover:border-solana-purple/60 transition-all duration-300"
          >
            <MinecraftIcon icon={Twitter} size="sm" variant="diamond" className="mr-2" />
            Follow on X
          </a>
        </div>
      </div>
    </motion.div>
  );
};

export default TeamMembers;
