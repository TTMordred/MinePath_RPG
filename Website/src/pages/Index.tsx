
import React, { lazy, Suspense } from 'react';
import Navbar from '@/components/Navbar';
import Hero from '@/components/Hero';
import WhatIs from '@/components/WhatIs';
import JoinCTA from '@/components/JoinCTA';
import ScrollToTop from '@/components/ScrollToTop';

// Lazy-load components that are further down the page for better initial load performance
const FeaturesSection = lazy(() => import('@/components/FeaturesSection'));
const GameModes = lazy(() => import('@/components/GameModes'));
const GameRewards = lazy(() => import('@/components/GameRewards'));
const Tokenomics = lazy(() => import('@/components/Tokenomics'));
const NFTRaritySection = lazy(() => import('@/components/NFTRaritySection'));
const Roadmap = lazy(() => import('@/components/Roadmap'));
const HowToPlay = lazy(() => import('@/components/HowToPlay'));
const NFTShowcase = lazy(() => import('@/components/NFTShowcase'));
const Newsletter = lazy(() => import('@/components/Newsletter'));
const NFTDropMechanics = lazy(() => import('@/components/NFTDropMechanics'));
const Footer = lazy(() => import('@/components/Footer'));
const TestimonialSection = lazy(() => import('@/components/TestimonialSection'));
const Web3Economy = lazy(() => import('@/components/Web3Economy'));
const CommunitySection = lazy(() => import('@/components/CommunitySection'));
const ServerStatus = lazy(() => import('@/components/ServerStatus'));

// Simple loading component for Suspense fallback
const SectionLoader = () => (
  <div className="w-full py-16 flex justify-center items-center">
    <div className="minecraft-loading"></div>
  </div>
);

const Index = () => {
  return (
    <div className="min-h-screen flex flex-col bg-minecraft-black minecraft-dirt-bg">
      <Navbar />
      <main className="flex-grow">
        {/* Critical path components loaded eagerly */}
        <Hero />
        <JoinCTA />
        <WhatIs />
        
        {/* Less critical components loaded lazily */}
        <Suspense fallback={<SectionLoader />}>
          <FeaturesSection />
          <HowToPlay />
          <Web3Economy />
          <GameModes />
          <GameRewards />
          <NFTDropMechanics />
          <Tokenomics />
          <CommunitySection />
          <TestimonialSection />
          <NFTShowcase />
          <Roadmap />
          <ServerStatus />
          <Newsletter />
        </Suspense>
      </main>
      
      <Suspense fallback={<div className="h-40"></div>}>
        <Footer />
      </Suspense>
      
      <ScrollToTop />
    </div>
  );
};

export default Index;
