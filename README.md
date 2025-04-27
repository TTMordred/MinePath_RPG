# MinePath RPG - Minecraft Server with Solana Blockchain Integration

Server Download: [MinePath_RPG.rar](https://www.mediafire.com/file/1mzqvzvhssn0sk0/MinePath_RPG.rar/file)

## Overview

MinePath RPG is a Minecraft server built on [Paper](https://papermc.io/) 1.18.2 that integrates Solana blockchain technology, allowing players to earn and use cryptocurrency tokens while playing Minecraft. The server features NFT mining, token rewards, and various economy-based plugins.

## Features

- **Solana Blockchain Integration**: Connect your Minecraft experience with [Solana blockchain](https://solana.com/)
- **NFT Mining System**: Mine blocks to earn tokens through the NFTMiner plugin
- **Token Economy**: Earn MINECRAFT tokens that can be exported to real blockchain wallets
- **NFT Lootboxes**: Purchase and open lootboxes to receive NFTs with special abilities
- **Wallet Authentication**: Login with your Solana wallet using the SolanaLogin plugin
- **Upgradable Mining**: Improve your mining speed and inventory capacity with tokens
- **Web Interface**: Manage your wallet connection and NFTs through a web interface

## Server Specifications

- **Minecraft Version**: [1.18.2](https://www.minecraft.net/en-us)
- **Server Software**: [Paper](https://papermc.io/downloads/paper) (paper-1.18.2-388.jar)
- **Memory Allocation**: 2GB minimum, 4GB recommended

## Complete Installation Guide

### Prerequisites

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) (JDK 17.0.14 or later)
- [Minecraft Java Edition 1.18.2](https://www.minecraft.net/en-us/download)
- [MySQL database](https://www.mysql.com/downloads/) (for blockchain data storage)
- [Node.js](https://nodejs.org/) (version 14 or higher, for web server components)
- [Phantom Wallet](https://phantom.app/) (browser extension or mobile app)

### Step 1: Server Setup

1. Download the server files from [MinePath_RPG.rar](https://www.mediafire.com/file/1mzqvzvhssn0sk0/MinePath_RPG.rar/file)
2. Extract the RAR file to your desired location
3. Edit the `start.bat` file to point to your Java installation:
   ```
   "PATH_TO_YOUR_JAVA\bin\java.exe" -Xms2G -Xmx4G -jar paper-1.18.2-388.jar nogui
   ```
4. Start the server once to generate necessary files, then stop it

### Step 2: Database Setup

1. Install MySQL if not already installed
2. Create a new database for MinePath:
   ```sql
   CREATE DATABASE minepath;
   CREATE USER 'minepath'@'localhost' IDENTIFIED BY 'your_password';
   GRANT ALL PRIVILEGES ON minepath.* TO 'minepath'@'localhost';
   FLUSH PRIVILEGES;
   ```
3. Import the database schema:
   ```
   mysql -u minepath -p minepath < plugins/MinePath/balance.sql
   ```

### Step 3: Plugin Installation

#### NFTPlugin

1. Place `NFTPlugin.jar` in the `plugins` folder
2. Start the server to generate configuration files
3. Configure in `plugins/NFTPlugin/config.yml`:
   ```yaml
   database:
     host: localhost
     port: 3306
     database: minepath
     username: minepath
     password: your_password
     table-prefix: nftplugin_
   ```
4. Set up the Solana backend:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install
   # Configure .env file with your Solana wallet details
   ```

#### SolanaLogin Plugin

1. Place `SolanaLogin.jar` in the `plugins` folder
2. Start the server to generate configuration files
3. Configure in `plugins/SolanaLogin/config.yml`:
   ```yaml
   database:
     host: localhost
     port: 3306
     database: minepath
     username: minepath
     password: your_password
     table-prefix: walletlogin_

   web-server:
     enabled: true
     url: "http://localhost:3000"  # Change to your server's URL
     port: 3000
   ```
4. Set up the web server:
   ```bash
   cd plugins/SolanaLogin/web-server
   # or use the separate web-server directory if available
   npm install
   npm start
   ```

#### NFTMiner Plugin

1. Place `NFTMiner.jar` in the `plugins` folder
2. Start the server to generate configuration files
3. Configure in `plugins/NFTMiner/config.yml` as needed

#### LootBox Plugin

1. Place `LootBox.jar` in the `plugins` folder
2. Start the server to generate configuration files
3. Configure in `plugins/LootBox/config.yml`:
   ```yaml
   database:
     url: jdbc:mysql://localhost:3306/minepath
     user: minepath
     password: your_password
   ```

### Step 4: Web Interface Setup (Optional)

If you want to deploy the web interface for better user experience:

1. Navigate to the `Website` directory
2. Install dependencies:
   ```bash
   npm install --legacy-peer-deps
   ```
3. Build the website:
   ```bash
   npm run build
   ```
4. Deploy the built files to your web server or use a service like Vercel

### Step 5: Final Configuration

1. Make sure all plugins are properly configured
2. Ensure the web server is running and accessible
3. Update the server IP in all configuration files
4. Restart the server to apply all changes

## Plugin Details

### NFTPlugin

Core plugin for NFT functionality and Solana blockchain integration.

**Features:**
- Mint NFTs on the Solana blockchain
- Virtual NFT inventory system
- Custom enchantments (Explosion Mining, Laser Mining)
- Buff system (Lucky Charms)
- Admin commands for NFT management

**Commands:**
- `/nftinfo` - Display information about NFT items
- `/nftlist` - View all your NFTs
- `/mintnft <player> <metadata_key>` - Mint an NFT for a player (admin)
- `/nftbuff` - View active NFT buffs

### SolanaLogin

Allows players to authenticate using Solana wallets.

**Features:**
- Wallet authentication through blockchain signatures
- QR code login for mobile wallet users
- Secure session management
- Integration with Solana devnet

**Commands:**
- `/connectwallet` - Connect your Solana wallet
- `/walletinfo` - Display wallet information

### NFTMiner

Allows players to mine blocks for token rewards.

**Features:**
- Token rewards for mining different blocks
- Upgradable mining abilities
- Token balance management
- Custom scoreboard display

**Commands:**
- `/token` - Check your token balance
- `/shop` - Open the upgrade shop
- `/claim` - Claim mining rewards

### LootBox

Provides NFT lootboxes with different rarity tiers.

**Features:**
- Three lootbox tiers: Basic, Premium, and Ultimate
- Different drop rates for each tier
- Integration with NFTPlugin for minting
- Visual lootbox opening interface

**Commands:**
- `/nftlootbox <type> <amount>` - Purchase NFT lootboxes

## Mining System

The NFTMiner plugin allows players to earn tokens by mining different blocks:

- Cobblestone: 1 token
- Raw Iron: 2 tokens
- Stone: 3 tokens
- Iron Ore: 4 tokens
- Raw Gold: 5 tokens
- Gold Ore: 10 tokens
- Diamond: 10 tokens
- Diamond Ore: 20 tokens

## Upgrades

Players can spend tokens to upgrade:

- **Speed Upgrade**: Up to 5 levels, providing Haste effects
- **Inventory Upgrade**: Up to 3 levels, increasing storage capacity

## NFT Lootbox System

### Lootbox Types and Prices
- **Basic NFT Lootbox**: 500 currency units
- **Premium NFT Lootbox**: 1500 currency units
- **Ultimate NFT Lootbox**: 3000 currency units

### Rarity Tiers and Drop Rates

#### Basic Lootbox
- Common: 80%
- Rare: 15%
- Epic: 4%
- Legendary: 0.9%
- Mythic: 0.1%

#### Premium Lootbox
- Common: 50%
- Rare: 35%
- Epic: 10%
- Legendary: 4%
- Mythic: 1%

#### Ultimate Lootbox
- Common: 30%
- Rare: 40%
- Epic: 20%
- Legendary: 8%
- Mythic: 2%

## Troubleshooting

### Common Issues

1. **Database Connection Errors**:
   - Verify MySQL is running
   - Check database credentials in all plugin config files
   - Ensure the database schema is properly imported

2. **Web Server Connection Issues**:
   - Make sure Node.js is installed
   - Check that the web server is running on the correct port
   - Verify firewall settings allow connections to the web server port

3. **Plugin Compatibility Issues**:
   - Ensure all plugins are using the correct version for Minecraft 1.18.2
   - Check for conflicts in the server logs

### Getting Help

For issues or questions about the server, please contact the server administrator or refer to the individual plugin documentation.

## Useful Links

- [Solana Blockchain](https://solana.com/)
- [Solana Developer Resources](https://solana.com/developers)
- [Phantom Wallet](https://phantom.app/)
- [Minecraft Wiki](https://minecraft.fandom.com/wiki/Minecraft_Wiki)
- [Paper Project](https://papermc.io/)
- [MySQL Documentation](https://dev.mysql.com/doc/)
- [Node.js Documentation](https://nodejs.org/en/docs/)

---

*Note: This server integrates with the Solana blockchain. Please ensure you understand [blockchain technology](https://solana.com/learn) and cryptocurrency before using the token export features.*
