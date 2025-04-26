
import React, { useEffect } from 'react';
import { useLocation, Link } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Home } from 'lucide-react';
import Navbar from '@/components/Navbar';
import Footer from '@/components/Footer';

const NotFound = () => {
  const location = useLocation();

  useEffect(() => {
    console.error(
      "404 Error: User attempted to access non-existent route:",
      location.pathname
    );
  }, [location.pathname]);

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-grow flex items-center justify-center py-16">
        <div className="text-center px-4">
          <h1 className="font-minecraft text-6xl mb-4 text-solana-purple">404</h1>
          <div className="mb-8">
            <img 
              src="/images/creeper_face.png" 
              alt="Creeper face" 
              className="w-32 h-32 mx-auto" 
            />
          </div>
          <p className="text-xl mb-6">Oops! Looks like a Creeper blew up this page.</p>
          <p className="text-muted-foreground mb-8">
            The page you're looking for has been destroyed or never existed.
          </p>
          <Button asChild size="lg">
            <Link to="/" className="flex items-center">
              <Home className="mr-2 h-5 w-5" /> Return to Home
            </Link>
          </Button>
        </div>
      </main>
      <Footer />
    </div>
  );
};

export default NotFound;
