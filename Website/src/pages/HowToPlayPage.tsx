
import React from 'react';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';
import { Button } from '@/components/ui/button';
import { ArrowLeft } from 'lucide-react';
import { Link } from 'react-router-dom';

const HowToPlayPage = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-grow">
        <section className="relative py-24 min-h-[400px] flex items-center">
          <div className="absolute inset-0 bg-gradient-to-br from-solana-purple/20 via-transparent to-solana-green/20 z-0"></div>
          <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
            <div className="max-w-3xl mx-auto text-center">
              <div className="inline-block p-1.5 rounded-full bg-gradient-to-r from-solana-purple/20 via-solana-blue/20 to-solana-green/20 mb-4">
                <div className="px-4 py-1 rounded-full bg-card/60 backdrop-blur-sm text-sm font-medium text-solana-purple">
                  COMING SOON
                </div>
              </div>
              
              <h1 className="font-minecraft text-5xl md:text-6xl mb-6">
                <span className="bg-clip-text text-transparent bg-gradient-to-r from-solana-purple to-solana-green drop-shadow-[0_1.2px_1.2px_rgba(0,0,0,0.8)]">
                  HOW TO PLAY
                </span>
              </h1>
              
              <p className="text-xl text-muted-foreground mb-8 max-w-2xl mx-auto">
                Our complete guide is coming soon! Stay tuned for the release where you can browse, discover, and learn about all available MinePath NFTs.
              </p>
              
              <Link to="/">
                <Button 
                  variant="outline" 
                  size="lg"
                  className="border-solana-purple text-solana-purple hover:bg-solana-purple/10 group"
                >
                  <ArrowLeft className="mr-2 h-4 w-4 transition-transform group-hover:-translate-x-1" /> 
                  Back to Home
                </Button>
              </Link>
            </div>
          </div>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default HowToPlayPage;
