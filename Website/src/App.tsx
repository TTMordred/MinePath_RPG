
import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route, useNavigate, useLocation } from "react-router-dom";
import { useEffect } from "react";
import Index from "@/pages/Index";
import NotFound from "@/pages/NotFound";
import NFTCatalog from "@/pages/NFTCatalog";
import NFTDetail from "@/pages/NFTDetail";
import HowToPlayPage from "@/pages/HowToPlayPage";
import StorePage from "@/pages/StorePage";
import LoginPage from "@/pages/LoginPage";
import ScrollToTop from "@/components/ScrollToTop";

const toastStyles = {
  background: 'url("/images/bg-planks.png")',
  color: 'white',
  border: '4px solid #8b5a2b',
  fontFamily: '"minecraftregular", monospace',
  boxShadow: '0 4px 0 rgba(0,0,0,0.3)',
  imageRendering: 'pixelated' as 'pixelated',
  letterSpacing: '0.5px',
  textShadow: '1px 1px 0px rgba(0,0,0,0.8)'
};

const queryClient = new QueryClient();

// Handle 404s with a custom component that checks if the path matches our routes
const RouteHandler = () => {
  const navigate = useNavigate();
  const location = useLocation();
  
  // On first render or location change, check if we're on a valid route
  useEffect(() => {
    // This is to handle direct links and refreshes properly
    if (location.pathname.startsWith('/nfts/')) {
      const id = location.pathname.split('/').pop();
      if (id) {
        console.log(`Handling direct navigation to NFT ID: ${id}`);
      }
    }
  }, [location]);
  
  return null;
};

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <div className="minecraft-font">
        <Toaster />
        <Sonner toastOptions={{
          style: toastStyles,
        }} />
        
        <BrowserRouter>
          <ScrollToTop />
          <RouteHandler />
          <Routes>
            <Route path="/" element={<Index />} />
            <Route path="/nfts" element={<NFTCatalog />} />
            <Route path="/nfts/:id" element={<NFTDetail />} />
            <Route path="/how-to-play" element={<HowToPlayPage />} />
            <Route path="/store" element={<StorePage />} />
            <Route path="/login" element={<LoginPage />} />
            <Route path="*" element={<NotFound />} />
          </Routes>
        </BrowserRouter>
      </div>
    </TooltipProvider>
  </QueryClientProvider>
);

export default App;
