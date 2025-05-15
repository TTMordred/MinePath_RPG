
import React from 'react';
import { motion } from 'framer-motion';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, AreaChart, Area } from 'recharts';
import { Users, Gem, ChartLine } from 'lucide-react';
import { MinecraftIcon } from '@/components/ui/minecraft-icon';
import { cn } from '@/lib/utils';

// Player growth data
const playerGrowthData = [
  { month: 'Jan', players: 1500 },
  { month: 'Feb', players: 3000 },
  { month: 'Mar', players: 4500 },
  { month: 'Apr', players: 6200 },
  { month: 'May', players: 8700 },
  { month: 'Jun', players: 12000 },
];

// NFT distribution data
const nftDistributionData = [
  { name: 'Common', value: 60, color: '#b0b0b0' },  // silver
  { name: 'Uncommon', value: 25, color: '#55ff55' }, // green
  { name: 'Rare', value: 10, color: '#5555ff' },    // blue
  { name: 'Epic', value: 3, color: '#aa00aa' },     // purple
  { name: 'Legendary', value: 2, color: '#ffaa00' }, // gold
];

// Weekly rewards data
const weeklyRewardsData = [
  { day: 'Mon', tokens: 2500 },
  { day: 'Tue', tokens: 3200 },
  { day: 'Wed', tokens: 4100 },
  { day: 'Thu', tokens: 3800 },
  { day: 'Fri', tokens: 5200 },
  { day: 'Sat', tokens: 6400 },
  { day: 'Sun', tokens: 7200 },
];

// Custom tooltip for charts
const CustomTooltip = ({ active, payload, label, valuePrefix, valueSuffix }: any) => {
  if (active && payload && payload.length) {
    return (
      <div className="minecraft-tooltip">
        <p className="font-minecraft text-sm mb-1">{label}</p>
        <p className="text-cyan-400 text-sm">{`${valuePrefix || ''}${payload[0].value}${valueSuffix || ''}`}</p>
      </div>
    );
  }
  return null;
};

const StatsSection = () => {
  // Animation variants for charts
  const chartVariants = {
    hidden: { opacity: 0, y: 20 },
    visible: { opacity: 1, y: 0, transition: { duration: 0.5 } }
  };

  return (
    <section className="relative py-16 md:py-24 overflow-hidden">
      {/* Background with overlay */}
      <div className="absolute inset-0 z-0">
        <div className="absolute inset-0 bg-gradient-to-b from-black/30 via-transparent to-black/80"></div>
        <div className="absolute inset-0 bg-[url('/images/bg-mountain.png')] bg-no-repeat bg-cover bg-center opacity-30"></div>
        <div className="absolute left-0 inset-y-0 w-16 opacity-20">
          <div className="h-full w-full bg-[url('/public/lovable-uploads/571ce867-0253-4784-ba20-b363e73c1463.png')] bg-repeat-y"></div>
        </div>
      </div>
      
      <div className="container mx-auto px-4 sm:px-6 lg:px-8 relative z-10">
        <div className="text-center mb-12">
          <motion.div
            initial={{ opacity: 0, scale: 0.9 }}
            whileInView={{ opacity: 1, scale: 1 }}
            transition={{ duration: 0.5 }}
            viewport={{ once: true }}
            className="inline-block p-1.5 rounded-none bg-gradient-to-r from-blue-600/20 to-purple-600/20 mb-5"
          >
            <div className="px-4 py-1.5 font-minecraft text-cyan-400 text-sm border-b border-cyan-400/30">
              SERVER STATS
            </div>
          </motion.div>

          <motion.h2 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.2 }}
            viewport={{ once: true }}
            className="font-minecraft text-3xl md:text-4xl lg:text-5xl mb-6 text-white"
          >
            <span className="bg-clip-text">
              OUR GROWTH BY THE NUMBERS
            </span>
          </motion.h2>

          <motion.p 
            initial={{ opacity: 0 }}
            whileInView={{ opacity: 1 }}
            transition={{ duration: 0.5, delay: 0.3 }}
            viewport={{ once: true }}
            className="text-base lg:text-lg text-white/80 max-w-3xl mx-auto"
          >
            Track our server's growth, NFT distributions, and token rewards in real-time with these statistics.
          </motion.p>
        </div>
        
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-12">
          {/* Player Growth Chart */}
          <motion.div
            variants={chartVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-5 h-80"
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={Users} variant="grass" className="mr-3" />
              <h3 className="font-minecraft text-lg text-white">Player Growth</h3>
            </div>
            <ResponsiveContainer width="100%" height="85%">
              <AreaChart data={playerGrowthData} margin={{ top: 5, right: 20, bottom: 25, left: 0 }}>
                <defs>
                  <linearGradient id="playerGrowth" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#5aa918" stopOpacity={0.8}/>
                    <stop offset="95%" stopColor="#5aa918" stopOpacity={0.1}/>
                  </linearGradient>
                </defs>
                <XAxis 
                  dataKey="month" 
                  stroke="#ffffff60"
                  fontSize={12}
                  tickLine={false}
                />
                <YAxis 
                  stroke="#ffffff60"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(value) => value.toLocaleString()}
                />
                <Tooltip content={<CustomTooltip valueSuffix=" players" />} />
                <Area 
                  type="monotone" 
                  dataKey="players" 
                  stroke="#5aa918" 
                  fillOpacity={1} 
                  fill="url(#playerGrowth)" 
                  strokeWidth={2}
                />
              </AreaChart>
            </ResponsiveContainer>
          </motion.div>
          
          {/* NFT Distribution Chart */}
          <motion.div
            variants={chartVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            transition={{ delay: 0.2 }}
            className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-5 h-80"
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={Gem} variant="diamond" className="mr-3" />
              <h3 className="font-minecraft text-lg text-white">NFT Distribution</h3>
            </div>
            <ResponsiveContainer width="100%" height="85%">
              <PieChart>
                <Pie
                  data={nftDistributionData}
                  cx="50%"
                  cy="50%"
                  innerRadius={60}
                  outerRadius={90}
                  paddingAngle={2}
                  dataKey="value"
                  label={(entry) => `${entry.name}: ${entry.value}%`}
                >
                  {nftDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} stroke="#000" strokeWidth={1} />
                  ))}
                </Pie>
                <Tooltip 
                  content={<CustomTooltip valueSuffix="%" />}
                  formatter={(value) => [`${value}%`]} 
                />
              </PieChart>
            </ResponsiveContainer>
          </motion.div>
          
          {/* Weekly Rewards Chart */}
          <motion.div
            variants={chartVariants}
            initial="hidden"
            whileInView="visible"
            viewport={{ once: true }}
            transition={{ delay: 0.4 }}
            className="bg-black/40 backdrop-blur-sm border border-cyan-400/30 p-5 h-80"
          >
            <div className="flex items-center mb-4">
              <MinecraftIcon icon={ChartLine} variant="gold" className="mr-3" />
              <h3 className="font-minecraft text-lg text-white">Weekly $MINE Rewards</h3>
            </div>
            <ResponsiveContainer width="100%" height="85%">
              <BarChart data={weeklyRewardsData} margin={{ top: 5, right: 20, bottom: 25, left: 0 }}>
                <XAxis 
                  dataKey="day" 
                  stroke="#ffffff60" 
                  fontSize={12}
                  tickLine={false}
                />
                <YAxis 
                  stroke="#ffffff60"
                  fontSize={12}
                  tickLine={false}
                  axisLine={false}
                  tickFormatter={(value) => value.toLocaleString()}
                />
                <Tooltip content={<CustomTooltip valuePrefix="$MINE " />} />
                <Bar dataKey="tokens" fill="#f8bd17" radius={[4, 4, 0, 0]}>
                  {weeklyRewardsData.map((entry, index) => (
                    <Cell 
                      key={`cell-${index}`}
                      fill={cn(
                        index === weeklyRewardsData.length - 1 ? '#ffcc33' : '#f8bd17',
                        'drop-shadow-glow'
                      )}
                    />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          </motion.div>
        </div>
        
        <div className="text-center">
          <motion.div
            initial={{ opacity: 0, y: 10 }}
            whileInView={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.6 }}
            viewport={{ once: true }}
          >
            <div className="minecraft-diamond-btn inline-block">
              Real-time Dashboard Coming Soon
            </div>
          </motion.div>
        </div>
      </div>
    </section>
  );
};

export default StatsSection;
