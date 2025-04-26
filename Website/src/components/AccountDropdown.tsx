
import React, { useState, useEffect } from "react";
import { ChevronDown, LogOut, UserRound, Wallet as WalletIcon, Settings } from "lucide-react";
import AccountSettingsModal from "./AccountSettingsModal";

// Enhanced glassmorphism and shadow for dropdown
const dropdownBg = "bg-gradient-to-br from-[#252b3b]/95 via-[#151a22]/95 to-[#151a22]/95";

type AccountDropdownProps = {
  accountName: string;
  onLogout: () => void;
};

const AccountDropdown: React.FC<AccountDropdownProps> = ({ accountName, onLogout }) => {
  const [open, setOpen] = useState(false);
  const [phantomConnected, setPhantomConnected] = useState(false);
  const [phantomAccount, setPhantomAccount] = useState<string | null>(null);
  const [settingsOpen, setSettingsOpen] = useState(false);

  // Detect Phantom connection status only (no connect/disconnect—just check)
  useEffect(() => {
    if (typeof window !== "undefined" && (window as any).solana?.isPhantom) {
      (window as any).solana.connect({ onlyIfTrusted: true }).then(({ publicKey }: any) => {
        setPhantomConnected(true);
        setPhantomAccount(publicKey?.toString() || null);
      }).catch(() => {
        setPhantomConnected(false);
        setPhantomAccount(null);
      });
    }
  }, []);

  return (
    <>
      <div className="relative">
        <button
          onClick={() => setOpen((v) => !v)}
          className="flex items-center space-x-2 font-minecraft text-sm text-white bg-black/40 px-4 py-2 rounded-lg hover:bg-black/60 transition shadow-glass-card border border-cyan-400/20"
        >
          <UserRound className="h-5 w-5 mr-1 text-cyan-400 drop-shadow-glow" />
          <span>{accountName}</span>
          <ChevronDown className={`h-4 w-4 ml-1 transition-transform ${open ? 'rotate-180' : ''}`} />
        </button>
        {open && (
          <div
            className={`absolute right-0 mt-2 min-w-[260px] ${dropdownBg} backdrop-blur-lg border border-cyan-400/30 rounded-2xl shadow-2xl z-50 animate-scale-in`}
            style={{
              boxShadow: "0 8px 40px 0 rgba(0,200,255,0.13), 0 0 0 2px #146C74 inset"
            }}
            onClick={() => setOpen(false)}
          >
            <div
              className="px-5 py-6 font-minecraft flex flex-col gap-5 text-white"
              onClick={e => e.stopPropagation()}
            >
              {/* Account Name Row */}
              <div className="flex items-center gap-2 text-cyan-300 text-base">
                <UserRound className="h-4 w-4 text-cyan-400" />
                <span className="glow-effect">{accountName}</span>
              </div>
              {/* Wallet status */}
              <div className="flex items-center gap-2 bg-black/30 px-3 py-2 rounded-lg border border-cyan-400/15">
                <WalletIcon className="h-4 w-4 text-cyan-400" />
                {typeof window !== "undefined" && (window as any).solana?.isPhantom ? (
                  <>
                    {phantomConnected ? (
                      <span className="text-green-400 font-minecraft glow-effect">
                        Wallet Connected
                        <span className="block text-xs text-white/80 truncate">{phantomAccount}</span>
                      </span>
                    ) : (
                      <span className="text-yellow-400 font-minecraft">Wallet Not Connected</span>
                    )}
                  </>
                ) : (
                  <span className="text-yellow-400 font-minecraft">Phantom not installed</span>
                )}
              </div>
              {/* Account Settings */}
              <button
                className="w-full flex items-center minecraft-3d-btn bg-cyan-400/25 border-cyan-400/60 text-cyan-300 font-minecraft text-sm shadow-lg hover:bg-cyan-400/40 transition-all glow-effect"
                onClick={(e) => {
                  e.stopPropagation();
                  setOpen(false);
                  setSettingsOpen(true);
                }}
              >
                <Settings className="h-4 w-4 mr-2" /> Account Settings
              </button>
              {/* Logout */}
              <button
                className="w-full flex items-center minecraft-3d-btn bg-red-400/20 border-red-400/60 text-red-300 font-minecraft text-sm mt-1 shadow-lg hover:bg-red-400/40 transition-all"
                onClick={(e) => {
                  e.stopPropagation();
                  setOpen(false);
                  onLogout();
                }}
              >
                <LogOut className="h-4 w-4 mr-2" /> Logout
              </button>
            </div>
          </div>
        )}
      </div>
      <AccountSettingsModal 
        open={settingsOpen}
        onOpenChange={setSettingsOpen}
        accountName={accountName}
      />
    </>
  );
};

export default AccountDropdown;
