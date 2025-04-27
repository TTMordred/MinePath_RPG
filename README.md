Server Drive: https://www.mediafire.com/file/1mzqvzvhssn0sk0/MinePath_RPG.rar/file
# MyPaperServer - Minecraft Server with Blockchain Integration

## Overview

MyPaperServer is a Minecraft server built on [Paper](https://papermc.io/) 1.18.2 that integrates blockchain technology, allowing players to earn and use cryptocurrency tokens while playing Minecraft. The server features NFT mining, token rewards, and various economy-based plugins.

## Features

- **Blockchain Integration**: Connect your Minecraft experience with [Solana blockchain](https://solana.com/)
- **NFT Mining System**: Mine blocks to earn tokens through the NFTMiner plugin
- **Token Economy**: Earn MINECRAFT tokens that can be exported to real blockchain wallets
- **Upgradable Mining**: Improve your mining speed and inventory capacity with tokens
- **Multiple Plugins**: Various plugins to enhance gameplay and server management

## Server Specifications

- **Minecraft Version**: [1.18.2](https://www.minecraft.net/en-us)
- **Server Software**: [Paper](https://papermc.io/downloads/paper) (paper-1.18.2-388.jar)
- **Memory Allocation**: 2GB minimum, 4GB maximum

## Plugins

### Core Blockchain Plugins
- **NFTPlugin**: Core plugin for NFT functionality and [Solana](https://solana.com/) blockchain integration
- **MinePath**: Manages player token balances and blockchain transactions
- **NFTMiner**: Allows players to mine blocks for token rewards
- **Synex**: Additional cryptocurrency exchange functionality

### Other Plugins
- **PicoJobs**: Job system for players to earn in-game rewards

### Links to Official Plugin Documentation
- [Paper Documentation](https://docs.papermc.io/)
- [PlaceholderAPI](https://github.com/PlaceholderAPI/PlaceholderAPI) - Required for some plugins

## Configuration

### Server Start
The server starts with the following command (from start.bat):
```
"E:\server\microsoft-jdk-17.0.14-windows-x64\jdk-17.0.14+7\bin\java.exe" -Xms2G -Xmx4G -jar paper-1.18.2-388.jar nogui
```

### Mining System
The NFTMiner plugin allows players to earn tokens by mining different blocks:
- Cobblestone: 1 token
- Raw Iron: 2 tokens
- Stone: 3 tokens
- Iron Ore: 4 tokens
- Raw Gold: 5 tokens
- Gold Ore: 10 tokens
- Diamond: 10 tokens
- Diamond Ore: 20 tokens

### Upgrades
Players can spend tokens to upgrade:
- **Speed Upgrade**: Up to 5 levels, providing Haste effects
- **Inventory Upgrade**: Up to 3 levels, increasing storage capacity

## Database Configuration
The server uses both MySQL and SQLite databases for different plugins:
- **MySQL**: Used for MinePath and other blockchain data
- **SQLite**: Used by some plugins for local data storage

## Getting Started

1. Start the server using the start.bat file
2. Connect to the server using [Minecraft Java Edition 1.18.2](https://www.minecraft.net/en-us/download)
3. Begin mining to earn tokens
4. Use earned tokens to upgrade your mining abilities or export to your [Solana wallet](https://docs.solana.com/wallet-guide)

### Server Connection

- **Server Address**: `your-server-ip:25565` (replace with your actual server IP)
- **Minecraft Version**: 1.18.2
- **Server Type**: Java Edition

## Requirements

- [Java 17](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html) (JDK 17.0.14 or later)
- [Minecraft Java Edition 1.18.2](https://www.minecraft.net/en-us/download)
- [MySQL database](https://www.mysql.com/downloads/) (for full functionality)
- [MinePathCoinPlugin](https://github.com/HungPhan-0612/Minecraft-Solana-Coin-Plugin/blob/master/README.md)
- [Solana_Login](https://github.com/TTMordred/MinePath_RPG/blob/main/Solana_Login/README.md)
- [NFTPlugin](https://github.com/Woft257/NFT-Plugin/blob/main/README.md)
- [LootBox](https://github.com/Woft257/LootBox/blob/main/README.md)
- [NFTMiner](https://github.com/TTMordred/MinePath_RPG/blob/main/NFTMiner/README.md)
- Open the folder plugins/MinePath create database just like file balance.sql


## Support

For issues or questions about the server, please contact the server administrator.

## Useful Links

- [Solana Blockchain](https://solana.com/)
- [Solana Developer Resources](https://solana.com/developers)
- [Minecraft Wiki](https://minecraft.fandom.com/wiki/Minecraft_Wiki)
- [Paper Project](https://papermc.io/)
- [MySQL Documentation](https://dev.mysql.com/doc/)

---

*Note: This server integrates with the Solana blockchain. Please ensure you understand [blockchain technology](https://solana.com/learn) and cryptocurrency before using the token export features.*
