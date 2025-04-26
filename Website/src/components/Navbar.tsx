import React, { useState, useEffect } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { Menu, X, User } from 'lucide-react';
import { motion, AnimatePresence } from 'framer-motion';
import AccountDropdown from "./AccountDropdown";

const DEMO_CREDENTIALS = { account: "Dangduy", password: "1234" };

const Navbar = () => {
  const [isOpen, setIsOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  // Login state: Only track if logged in
  const [isLoggedIn, setIsLoggedIn] = useState(() =>
    Boolean(localStorage.getItem("mp_account"))
  );
  const [accountName, setAccountName] = useState(
    localStorage.getItem("mp_account") || ""
  );

  // Listen for storage changes (in case of login/logout from another tab)
  useEffect(() => {
    const syncAccount = () => {
      const account = localStorage.getItem("mp_account") || "";
      setAccountName(account);
      setIsLoggedIn(Boolean(account));
    };
    window.addEventListener("storage", syncAccount);
    return () => window.removeEventListener("storage", syncAccount);
  }, []);

  useEffect(() => {
    const handleScroll = () => {
      setScrolled(window.scrollY > 10);
    };
    window.addEventListener('scroll', handleScroll);
    return () => window.removeEventListener('scroll', handleScroll);
  }, []);

  // Logging out (helper)
  const handleLogout = () => {
    localStorage.removeItem("mp_account");
    setAccountName("");
    setIsLoggedIn(false);
    window.dispatchEvent(new Event("storage"));
  };

  return (
    <nav className={`fixed top-0 w-full z-50 transition-all duration-500 ${scrolled ? 'bg-black/70 backdrop-blur-md py-3 shadow-lg' : 'bg-transparent py-5'}`}>
      <div className="container mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between">
          <div className="flex items-center">
            <Link to="/" className="flex items-center group">
              <motion.img 
                src="/images/minecraft-logo.png" 
                alt="MinePath" 
                className="h-9 w-auto mr-2 pixelated" 
                whileHover={{ scale: 1.05 }}
                transition={{ type: "spring", stiffness: 400, damping: 10 }}
              />
              <motion.div
                initial={{ opacity: 1 }}
                whileHover={{ scale: 1.05 }}
                transition={{ type: "spring", stiffness: 400, damping: 10 }}
                className="flex items-center"
              >
                <span className="font-minecraft text-2xl">
                  <span className="text-minecraft-green">Mine</span>
                  <span className="text-solana-purple">Path</span>
                </span>
                <motion.div 
                  className="h-0.5 w-0 bg-gradient-to-r from-solana-blue to-solana-green group-hover:w-full transition-all duration-300"
                ></motion.div>
              </motion.div>
            </Link>
            <div className="hidden lg:block ml-16">
              <div className="flex items-center space-x-8">
                <NavLink to="/" currentPath={location.pathname}>HOME</NavLink>
              </div>
            </div>
          </div>

          {/* LOGIN/ACCOUNT AREA - Desktop */}
          <div className="hidden lg:flex items-center space-x-4">
            {!isLoggedIn ? (
              <button
                className="bg-cyan-400/20 border border-cyan-400/30 text-cyan-400 font-minecraft text-sm px-4 py-2 rounded-md hover:bg-cyan-400/50 transition flex items-center"
                onClick={() => navigate("/login")}
              >
                <User className="h-4 w-4 mr-2" /> Login
              </button>
            ) : (
              <AccountDropdown accountName={accountName} onLogout={handleLogout} />
            )}
          </div>
          
          {/* MOBILE MENU ICON */}
          <div className="lg:hidden">
            <button
              onClick={() => setIsOpen(!isOpen)}
              className="inline-flex items-center justify-center p-2 text-foreground/90 hover:text-white transition-colors"
            >
              {isOpen ? <X className="h-6 w-6" /> : <Menu className="h-6 w-6" />}
            </button>
          </div>
        </div>
      </div>
      
      {/* Mobile menu with animation */}
      <AnimatePresence>
        {isOpen && (
          <motion.div 
            className="lg:hidden bg-black/95 backdrop-blur-md border-t border-solana-purple/20"
            initial={{ opacity: 0, height: 0 }}
            animate={{ opacity: 1, height: "auto" }}
            exit={{ opacity: 0, height: 0 }}
            transition={{ duration: 0.3 }}
          >
            <div className="px-2 pt-2 pb-3 space-y-1">
              {/* Account area - mobile */}
              {!isLoggedIn ? (
                <button
                  className="w-full mb-4 bg-cyan-400/20 border border-cyan-400/30 text-cyan-400 font-minecraft text-base px-4 py-2 rounded-md hover:bg-cyan-400/50 transition flex items-center justify-center"
                  onClick={() => {
                    setIsOpen(false);
                    navigate("/login");
                  }}
                >
                  <User className="h-5 w-5 mr-2" /> Login
                </button>
              ) : (
                <div className="py-3">
                  <AccountDropdown accountName={accountName} onLogout={handleLogout} />
                </div>
              )}

              <MobileNavLink to="/" currentPath={location.pathname} onClick={() => setIsOpen(false)}>Home</MobileNavLink>
            </div>
          </motion.div>
        )}
      </AnimatePresence>
    </nav>
  );
};

// Helper components for cleaner navigation link styling
const NavLink = ({ to, currentPath, children }) => {
  const isActive = currentPath === to;
  
  return (
    <Link 
      to={to} 
      className={`font-minecraft text-sm relative group ${
        isActive 
          ? 'text-solana-blue' 
          : 'text-white hover:text-solana-blue transition-colors'
      }`}
    >
      <span>{children}</span>
      <div className={`absolute bottom-0 left-0 h-0.5 ${isActive ? 'w-full bg-gradient-to-r from-solana-blue to-solana-green' : 'w-0 bg-solana-blue group-hover:w-full'} transition-all duration-300`}></div>
    </Link>
  );
};

const MobileNavLink = ({ to, currentPath, onClick, children }) => {
  const isActive = currentPath === to;
  
  return (
    <Link 
      to={to} 
      className={`block px-3 py-2 text-base font-minecraft ${
        isActive 
          ? 'text-solana-blue bg-black/50' 
          : 'text-white hover:text-solana-blue transition-colors'
      }`}
      onClick={onClick}
    >
      <span>{children}</span>
    </Link>
  );
};

export default Navbar;

