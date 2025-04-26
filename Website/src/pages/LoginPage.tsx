import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Sparkles } from "lucide-react";

const DEMO_CREDENTIALS = { account: "Dangduy", password: "1234" };

const LoginPage: React.FC = () => {
  const [form, setForm] = useState({ account: "", password: "" });
  const [error, setError] = useState("");
  const navigate = useNavigate();

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setForm({ ...form, [e.target.name]: e.target.value });
    setError("");
  };

  const handleLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (
      form.account === DEMO_CREDENTIALS.account &&
      form.password === DEMO_CREDENTIALS.password
    ) {
      localStorage.setItem("mp_account", form.account);
      setError("");
      navigate("/", { replace: true });
      window.dispatchEvent(new Event("storage"));
    } else {
      setError("Invalid credentials");
    }
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center relative overflow-hidden" style={{ 
      background: 'linear-gradient(180deg, rgba(13,14,22,1) 0%, rgba(21,26,49,1) 100%)',
      backgroundSize: 'cover',
      backgroundAttachment: 'fixed' 
    }}>
      {/* Background elements */}
      <div className="absolute inset-0 z-0">
        <div className="absolute top-0 left-0 w-full h-full opacity-10 bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat"></div>
        <div className="absolute top-0 left-0 w-full h-full" style={{ 
          background: 'radial-gradient(circle, rgba(10, 21, 77, 0.3) 0%, rgba(13, 14, 22, 0) 70%)'
        }}></div>
        
        {/* Minecraft particles */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(20)].map((_, i) => (
            <div
              key={i}
              className="absolute pixelated w-2 h-2 bg-white opacity-30"
              style={{
                top: `${Math.random() * 100}%`,
                left: `${Math.random() * 100}%`,
                animation: `float ${5 + Math.random() * 5}s ease-in-out infinite ${Math.random() * 5}s`
              }}
            />
          ))}
        </div>
        
        {/* Floating blocks */}
        <div className="absolute inset-0 overflow-hidden">
          {[...Array(5)].map((_, i) => (
            <div
              key={`block-${i}`}
              className="absolute pixelated w-8 h-8"
              style={{
                top: `${Math.random() * 100}%`,
                left: `${Math.random() * 100}%`,
                backgroundImage: `url('/images/${['dirt', 'stone', 'diamond', 'gold'][Math.floor(Math.random() * 4)]}_block.png')`,
                backgroundSize: 'cover',
                transform: 'rotate(10deg)',
                imageRendering: 'pixelated',
                animation: `float ${7 + Math.random() * 7}s ease-in-out infinite ${Math.random() * 7}s, rotate ${15 + Math.random() * 10}s linear infinite ${Math.random() * 10}s`
              }}
            />
          ))}
        </div>
      </div>

      <div className="w-full max-w-md glass-card relative rounded-xl shadow-2xl border-4 border-cyan-400/30 p-10 text-center flex flex-col items-center animate-fade-in z-10" style={{
        boxShadow: "0 4px 40px 0 rgba(9,200,255,0.15), 0 0 0 2px #146C74 inset",
        background: "linear-gradient(120deg, rgba(0,195,255,0.18) 10%, rgba(191,175,255,0.09) 100%), rgba(0,0,0,0.65)",
        backdropFilter: "blur(10px)"
      }}>
        <Sparkles className="mx-auto mb-4 h-10 w-10 text-cyan-400 drop-shadow-glow animate-pulse-glow" />
        <h2 className="font-minecraft text-2xl text-cyan-400 mb-2 glow-effect">Login to MinePath</h2>
        <form className="space-y-4 w-full" onSubmit={handleLogin} autoComplete="off">
          <Input
            type="text"
            name="account"
            className="bg-black/60 text-cyan-200 border-cyan-400/40 font-minecraft"
            placeholder="Account Name"
            value={form.account}
            onChange={handleInputChange}
            required
            autoFocus
            autoComplete="off"
          />
          <Input
            type="password"
            name="password"
            className="bg-black/60 text-cyan-200 border-cyan-400/40 font-minecraft"
            placeholder="Account Password"
            value={form.password}
            onChange={handleInputChange}
            required
            autoComplete="off"
          />
          {error && (
            <div className="text-red-500 font-minecraft text-sm glow-effect animate-pulse">{error}</div>
          )}
          <Button
            type="submit"
            className="w-full minecraft-3d-btn bg-cyan-400/30 text-white border-cyan-400/50 font-minecraft text-lg py-2 rounded-lg glow-effect hover:bg-cyan-400/60 animate-float"
            style={{
              background: "linear-gradient(120deg, #13b0ff88 0%, #007aaf66 100%)"
            }}
          >
            Login
          </Button>
        </form>
      </div>
    </div>
  );
};

export default LoginPage;
