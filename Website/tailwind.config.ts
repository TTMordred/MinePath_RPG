import type { Config } from "tailwindcss";

export default {
	darkMode: ["class"],
	content: [
		"./pages/**/*.{ts,tsx}",
		"./components/**/*.{ts,tsx}",
		"./app/**/*.{ts,tsx}",
		"./src/**/*.{ts,tsx}",
	],
	prefix: "",
	theme: {
		container: {
			center: true,
			padding: '2rem',
			screens: {
				'2xl': '1400px'
			}
		},
		extend: {
			colors: {
				border: 'hsl(var(--border))',
				input: 'hsl(var(--input))',
				ring: 'hsl(var(--ring))',
				background: 'hsl(var(--background))',
				foreground: 'hsl(var(--foreground))',
				primary: {
					DEFAULT: 'hsl(var(--primary))',
					foreground: 'hsl(var(--primary-foreground))'
				},
				secondary: {
					DEFAULT: 'hsl(var(--secondary))',
					foreground: 'hsl(var(--secondary-foreground))'
				},
				destructive: {
					DEFAULT: 'hsl(var(--destructive))',
					foreground: 'hsl(var(--destructive-foreground))'
				},
				muted: {
					DEFAULT: 'hsl(var(--muted))',
					foreground: 'hsl(var(--muted-foreground))'
				},
				accent: {
					DEFAULT: 'hsl(var(--accent))',
					foreground: 'hsl(var(--accent-foreground))'
				},
				popover: {
					DEFAULT: 'hsl(var(--popover))',
					foreground: 'hsl(var(--popover-foreground))'
				},
				card: {
					DEFAULT: 'hsl(var(--card))',
					foreground: 'hsl(var(--card-foreground))'
				},
				sidebar: {
					DEFAULT: 'hsl(var(--sidebar-background))',
					foreground: 'hsl(var(--sidebar-foreground))',
					primary: 'hsl(var(--sidebar-primary))',
					'primary-foreground': 'hsl(var(--sidebar-primary-foreground))',
					accent: 'hsl(var(--sidebar-accent))',
					'accent-foreground': 'hsl(var(--sidebar-accent-foreground))',
					border: 'hsl(var(--sidebar-border))',
					ring: 'hsl(var(--sidebar-ring))'
				},
				// Custom theme colors from the image
				theme: {
					dark: '#1A1F2C',       // Dark background
					darker: '#141221',      // Darker sections
					purple: '#9b87f5',      // Primary purple color
					purpleDark: '#7E69AB',  // Darker purple for hover
					blue: '#1EAEDB',        // Blue accent color
					lightGray: '#D6BCFA',   // Light text/border color
					gray: '#2A2F3C',        // Border/separator color
				},
				// Minecraft/Solana themed colors
				minecraft: {
					green: '#5aa918',
					dirt: '#8b5a2b',
					stone: '#7f7f7f',
					blue: '#215aff',
					black: '#25252a',
					planks: '#bc9862',
					grass: '#5d923c',
					door: '#a67847',
					water: '#2f5fd6',
					lava: '#db6a0c',
					coal: '#252525',
					iron: '#b0b0b0',
					gold: '#f8bd17',
					diamond: '#5feaea',
					emerald: '#4adc65',
				},
				solana: {
					purple: '#9945ff',
					green: '#14f195',
					blue: '#00c2ff',
					black: '#141221',
				},
				rarity: {
					common: '#b0b0b0',
					uncommon: '#55ff55',
					rare: '#5555ff',
					epic: '#aa00aa',
					legendary: '#ffaa00',
				}
			},
			fontFamily: {
				minecraft: ['"Minecraft"', 'monospace'],
				poppins: ['Poppins', 'sans-serif'],
			},
			borderRadius: {
				lg: 'var(--radius)',
				md: 'calc(var(--radius) - 2px)',
				sm: 'calc(var(--radius) - 4px)'
			},
			keyframes: {
				'accordion-down': {
					from: {
						height: '0'
					},
					to: {
						height: 'var(--radix-accordion-content-height)'
					}
				},
				'accordion-up': {
					from: {
						height: 'var(--radix-accordion-content-height)'
					},
					to: {
						height: '0'
					}
				},
				'float': {
					'0%, 100%': {
						transform: 'translateY(0)'
					},
					'50%': {
						transform: 'translateY(-10px)'
					}
				},
				'pulse-glow': {
					'0%, 100%': {
						opacity: '1',
						boxShadow: '0 0 15px rgba(155, 135, 245, 0.8)'
					},
					'50%': {
						opacity: '0.7',
						boxShadow: '0 0 25px rgba(155, 135, 245, 0.4)'
					}
				},
				'shimmer': {
					'0%': {
						backgroundPosition: '-200% 0',
					},
					'100%': {
						backgroundPosition: '200% 0',
					},
				},
				'rotate-glow': {
					'0%': {
						transform: 'rotate(0deg)',
						filter: 'drop-shadow(0 0 5px rgba(155, 135, 245, 0.7))'
					},
					'50%': {
						filter: 'drop-shadow(0 0 15px rgba(155, 135, 245, 0.9))'
					},
					'100%': {
						transform: 'rotate(360deg)',
						filter: 'drop-shadow(0 0 5px rgba(155, 135, 245, 0.7))'
					}
				},
				'fade-in-up': {
					'0%': {
						opacity: '0',
						transform: 'translateY(20px)'
					},
					'100%': {
						opacity: '1',
						transform: 'translateY(0)'
					}
				},
				'modal-appear': {
					'0%': {
						opacity: '0',
						transform: 'scale(0.95)'
					},
					'100%': {
						opacity: '1',
						transform: 'scale(1)'
					}
				}
			},
			animation: {
				'accordion-down': 'accordion-down 0.2s ease-out',
				'accordion-up': 'accordion-up 0.2s ease-out',
				'float': 'float 3s ease-in-out infinite',
				'float-subtle': 'float-subtle 5s ease-in-out infinite',
				'rotate-slow': 'rotate-slow 30s linear infinite',
				'pulse-glow': 'pulse-glow 2s ease-in-out infinite',
				'pulse-slow': 'pulse-glow 4s ease-in-out infinite',
				'shimmer': 'shimmer 2s linear infinite',
				'rotate-glow': 'rotate-glow 10s linear infinite',
				'fade-in-up': 'fade-in-up 0.5s ease-out',
				'modal-appear': 'modal-appear 0.3s ease-out'
			},
			backgroundImage: {
				'minecraft-gradient': 'linear-gradient(to right, #5aa918, #215aff)',
				'modern-gradient': 'linear-gradient(to right, #9b87f5, #1EAEDB)',
					'solana-gradient': 'linear-gradient(to right, #9945ff, #00c2ff)',
				'shimmer-gradient': 'linear-gradient(90deg, rgba(255,255,255,0) 0%, rgba(255,255,255,0.2) 50%, rgba(255,255,255,0) 100%)',
				'glow-gradient': 'radial-gradient(circle, rgba(155,135,245,0.15) 0%, rgba(30,174,219,0.05) 70%, rgba(0,0,0,0) 100%)',
			},
			dropShadow: {
				'glow-sm': '0 0 3px rgba(155, 135, 245, 0.5)',
				'glow-md': '0 0 6px rgba(155, 135, 245, 0.5)',
				'glow-lg': '0 0 12px rgba(155, 135, 245, 0.5)',
				'text': '0 2px 4px rgba(0, 0, 0, 0.3)',
			}
		}
	},
	plugins: [require("tailwindcss-animate")],
} satisfies Config;
