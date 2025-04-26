import { Toaster } from "@/components/ui/toaster";
import { Toaster as Sonner } from "@/components/ui/sonner";
import { TooltipProvider } from "@/components/ui/tooltip";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { BrowserRouter, Routes, Route } from "react-router-dom";
import Index from "@/pages/Index";
import NotFound from "@/pages/NotFound";
import NFTCatalog from "@/pages/NFTCatalog";
import HowToPlayPage from "@/pages/HowToPlayPage";
import StorePage from "@/pages/StorePage";
import LoginPage from "@/pages/LoginPage";

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

const App = () => (
  <QueryClientProvider client={queryClient}>
    <TooltipProvider>
      <div className="minecraft-font">
        <Toaster />
        <Sonner toastOptions={{
          style: toastStyles,
        }} />
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<Index />} />
            <Route path="/nfts" element={<NFTCatalog />} />
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
