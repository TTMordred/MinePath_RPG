
import React from "react";
import { User, Wallet } from "lucide-react";

type Props = {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  accountName: string;
};

const AccountSettingsModal: React.FC<Props> = ({ open, onOpenChange, accountName }) => {
  // Check Phantom wallet connection status
  const phantomConnected = typeof window !== "undefined" && (window as any).solana?.isPhantom;
  const phantomAccount = phantomConnected ? (window as any).solana.publicKey?.toString() : null;

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70">
      <div
        className="bg-gradient-to-br from-[#232a37]/85 via-[#0b1120]/90 to-[#232a37]/85 border-4 border-cyan-400/25 rounded-2xl shadow-2xl max-w-sm w-full p-10 relative glass-card animate-scale-in"
        style={{
          boxShadow: "0 0 32px 4px #7E69AB55, 0 0 0 2px #146C74 inset",
          backdropFilter: "blur(18px)"
        }}
      >
        <h2 className="font-minecraft text-2xl text-cyan-400 mb-4 glow-effect">Account Information</h2>
        
        {/* Account Name */}
        <div className="mb-6">
          <div className="flex items-center mb-2">
            <User className="h-5 w-5 mr-2 text-cyan-400" />
            <label className="font-minecraft text-white/80">Account Name</label>
          </div>
          <div className="bg-black/50 rounded-lg p-3 font-minecraft text-white">
            {accountName}
          </div>
        </div>

        {/* Wallet Connection Status */}
        <div>
          <div className="flex items-center mb-2">
            <Wallet className="h-5 w-5 mr-2 text-cyan-400" />
            <label className="font-minecraft text-white/80">Wallet Connection</label>
          </div>
          <div 
            className={`bg-black/50 rounded-lg p-3 font-minecraft ${
              phantomConnected ? 'text-green-400' : 'text-red-400'
            }`}
          >
            {phantomConnected 
              ? `Connected (${phantomAccount ? phantomAccount.slice(0, 10) + '...' : 'Wallet Connected'})` 
              : 'Not Connected'}
          </div>
        </div>

        {/* Close Button */}
        <div className="flex justify-end mt-6">
          <button
            className="text-gray-400 font-minecraft hover:text-white transition"
            onClick={() => onOpenChange(false)}
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default AccountSettingsModal;
