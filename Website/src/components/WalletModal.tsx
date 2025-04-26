import React, { useState } from 'react';
import { X, ChevronDown, ChevronUp, Wallet, ExternalLink } from 'lucide-react';
import { Dialog, DialogContent, DialogTrigger } from '@/components/ui/dialog';
import { Button } from '@/components/ui/button';
import { useToast } from '@/hooks/use-toast';

// Mock wallet detection function
const detectWallet = (type: string) => {
  return type === 'Phantom'; // Mock Phantom as detected
};

type WalletOption = {
  id: string;
  name: string;
  icon: React.ReactNode;
  detected?: boolean;
};

const WalletModal = () => {
  const [view, setView] = useState<'intro' | 'options'>('intro');
  const [showAllOptions, setShowAllOptions] = useState(false);
  const { toast } = useToast();

  const walletOptions: WalletOption[] = [
    { 
      id: 'phantom', 
      name: 'Phantom',
      detected: detectWallet('Phantom'),
      icon: <div className="bg-purple-600 rounded-full p-1.5 w-8 h-8 flex items-center justify-center">
              <img src="/lovable-uploads/fb07d11d-3a8b-4117-af9c-10ef663a1935.png" alt="Phantom" className="w-5 h-5" />
            </div>
    },
    { 
      id: 'torus', 
      name: 'Torus',
      icon: <div className="bg-blue-500 rounded-full p-1.5 w-8 h-8 flex items-center justify-center text-white font-bold">T</div>
    },
    { 
      id: 'ledger', 
      name: 'Ledger',
      icon: <div className="bg-white rounded-full p-1 w-8 h-8 flex items-center justify-center">
              <div className="grid grid-cols-2 gap-0.5 w-6 h-6">
                <div className="bg-black"></div>
                <div className="bg-black"></div>
                <div className="bg-black"></div>
                <div className="bg-black"></div>
              </div>
            </div>
    },
    { 
      id: 'sollet', 
      name: 'Sollet',
      icon: <div className="bg-green-400 rounded-full p-1.5 w-8 h-8 flex items-center justify-center">
              <div className="w-5 h-2.5 bg-white rounded-sm"></div>
            </div>
    },
    { 
      id: 'slope', 
      name: 'Slope',
      icon: <div className="bg-blue-400 rounded-full p-1.5 w-8 h-8 flex items-center justify-center">
              <div className="rotate-45 bg-white w-3.5 h-3.5"></div>
            </div>
    },
    { 
      id: 'solflare', 
      name: 'Solflare',
      icon: <div className="bg-orange-500 rounded-full p-1.5 w-8 h-8 flex items-center justify-center">
              <div className="text-white text-xs">*</div>
            </div>
    },
    { 
      id: 'sollet-extension', 
      name: 'Sollet (Extension)',
      icon: <div className="bg-blue-500 rounded-full p-1.5 w-8 h-8 flex items-center justify-center text-white font-bold">S</div>
    }
  ];

  // Limit displayed options unless "show all" is clicked
  const displayedOptions = showAllOptions 
    ? walletOptions 
    : walletOptions.slice(0, 4);

  const handleWalletSelect = (walletId: string) => {
    toast({
      title: "Wallet Connection",
      description: `Connecting to ${walletId}...`
    });
    // Here you would typically implement actual wallet connection logic
  };

  const handleGetStarted = () => {
    setView('options');
  };

  return (
    <Dialog>
      <DialogTrigger asChild>
        {/* <Button className="minecraft-btn-purple py-3 px-6 text-base font-minecraft flex items-center bg-gradient-to-r from-solana-purple to-solana-blue hover:bg-solana-purple/90 text-white shadow-md hover:shadow-lg transition-shadow duration-300">
          Connect Wallet <Wallet className="ml-2 h-5 w-5" />
        </Button> */}
      </DialogTrigger>
      <DialogContent className="bg-[#1A1F2C] border border-solana-purple/20 text-white p-0 w-full max-w-md rounded-xl shadow-xl">
        <div className="relative p-6">
          <button 
            className="absolute right-4 top-4 rounded-full hover:bg-solana-purple/20 p-1.5 transition-colors"
            onClick={() => setView('intro')}
          >
            <X size={20} />
          </button>

          {view === 'intro' ? (
            <div className="flex flex-col items-center text-center">
              <h2 className="text-2xl font-bold mb-8 mt-4">You'll need a wallet on Solana to continue</h2>
              
              <div className="w-24 h-24 rounded-full bg-gradient-to-r from-solana-purple/30 to-solana-green/30 flex items-center justify-center mb-8 animate-pulse-glow">
                <div className="w-16 h-16 rounded-full bg-gradient-to-r from-solana-purple/50 to-solana-green/50 flex items-center justify-center">
                  <Wallet size={32} className="text-[#00C2FF]" />
                </div>
              </div>
              
              <Button 
                onClick={handleGetStarted}
                className="w-full py-6 bg-gradient-to-r from-solana-purple to-solana-blue hover:from-solana-purple/90 hover:to-solana-blue/90 rounded-md text-white font-medium text-lg shadow-lg shadow-solana-purple/20"
              >
                Get started
              </Button>
              
              <button 
                onClick={() => setView('options')}
                className="mt-6 flex items-center text-gray-300 hover:text-white transition-colors"
              >
                Already have a wallet? View options <ChevronDown size={16} className="ml-1" />
              </button>

              <div className="mt-8 pt-6 border-t border-gray-700/50 text-sm text-gray-400">
                <p className="flex items-center justify-center">
                  New to blockchain? <a href="https://solana.com/learn" target="_blank" rel="noopener noreferrer" className="text-solana-blue ml-2 flex items-center">Learn more <ExternalLink size={14} className="ml-1" /></a>
                </p>
              </div>
            </div>
          ) : (
            <div>
              <h2 className="text-2xl font-bold mb-8 mt-4">Connect a wallet on Solana to continue</h2>
              
              <div className="space-y-3">
                {displayedOptions.map((wallet) => (
                  <button
                    key={wallet.id}
                    className="flex items-center justify-between w-full p-3 hover:bg-white/5 rounded-md transition-colors"
                    onClick={() => handleWalletSelect(wallet.id)}
                  >
                    <div className="flex items-center">
                      {wallet.icon}
                      <span className="ml-3 text-lg">{wallet.name}</span>
                    </div>
                    {wallet.detected && (
                      <span className="text-solana-green text-sm flex items-center">
                        Detected
                      </span>
                    )}
                  </button>
                ))}
              </div>
              
              {!showAllOptions && walletOptions.length > 4 && (
                <button 
                  onClick={() => setShowAllOptions(true)}
                  className="mt-4 flex items-center justify-center w-full text-gray-300 hover:text-white py-2 hover:bg-white/5 rounded-md transition-colors"
                >
                  More options <ChevronDown size={16} className="ml-1" />
                </button>
              )}
              
              {showAllOptions && (
                <button 
                  onClick={() => setShowAllOptions(false)}
                  className="mt-4 flex items-center justify-center w-full text-gray-300 hover:text-white py-2 hover:bg-white/5 rounded-md transition-colors"
                >
                  Less options <ChevronUp size={16} className="ml-1" />
                </button>
              )}

              <div className="mt-6 pt-4 border-t border-gray-700/50">
                <p className="text-sm text-center text-gray-400">
                  By connecting your wallet, you agree to our<br />
                  <span className="text-solana-blue cursor-pointer">Terms of Service</span> and <span className="text-solana-blue cursor-pointer">Privacy Policy</span>
                </p>
              </div>
            </div>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
};

export default WalletModal;
