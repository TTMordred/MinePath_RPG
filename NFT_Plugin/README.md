# 🔷 Minecraft NFT Plugin 🔷

A comprehensive Solana NFT integration plugin for Minecraft servers. This plugin creates a seamless bridge between Minecraft gameplay and Solana blockchain technology, allowing players to own, use, and display real blockchain NFTs with special abilities in-game.

<div align="center">

![NFT Plugin Banner](https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4)

[<img src="https://img.shields.io/badge/Solana-Devnet-blue?style=for-the-badge&logo=solana" alt="Solana Devnet">](https://explorer.solana.com/?cluster=devnet)
[<img src="https://img.shields.io/badge/Minecraft-1.18.2+-green?style=for-the-badge&logo=minecraft" alt="Minecraft 1.18.2+">](https://www.minecraft.net/)
[<img src="https://img.shields.io/badge/IPFS-Pinata-orange?style=for-the-badge&logo=ipfs" alt="IPFS Pinata">](https://www.pinata.cloud/)
[<img src="https://img.shields.io/badge/Paper-Spigot-yellow?style=for-the-badge" alt="Paper/Spigot">](https://papermc.io/)

</div>

## 📋 Table of Contents

- [Features](#-features)
- [Requirements](#-requirements)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Commands](#-commands)
- [NFT Metadata](#-nft-metadata)
- [Custom Enchantments](#-custom-enchantments)
- [Buff System](#-buff-system)
- [Technical Details](#-technical-details)
- [Troubleshooting](#-troubleshooting)
- [Creating New NFTs](#-creating-new-nfts)
- [Support](#-support)

## ✨ Features

### Core Features

- **Real Blockchain NFTs**: Mint actual NFTs on the Solana blockchain that players can view on Solana Explorer
- **NFT Inventory System**: Dedicated inventory for storing and managing NFTs with unlimited pagination
- **NFT List Command**: View all your NFTs with detailed information, including blockchain links
- **Metadata Storage**: Store NFT metadata on Pinata IPFS for decentralized and permanent access
- **Admin Minting**: Server operators can mint NFTs for players with a simple command

### Gameplay Enhancements

- **Custom Enchantments**:
  - **Explosion Mining** (Levels I-IV): Mine in square patterns from 3×3 to 6×6 areas
  - **Laser Mining** (Levels I-V): Mine in straight lines up to 6 blocks deep

- **Buff System**:
  - **Lucky Charms**: Increase drop rates for rare items with percentage-based buffs (1% to 20%)
  - **Stackable Buffs**: Multiple NFTs with the same buff type stack their effects

- **Special NFT Properties**:
  - **Unbreakable Items**: NFT items never break or lose durability
  - **Death Protection**: NFT items remain in inventory after death
  - **Custom Models**: Support for custom resource packs with unique item models
  - **Glowing Effect**: NFT items have an enchantment glint for visual distinction

## ⚙️ Requirements

### Server Requirements

- **Minecraft Server**: Paper or Spigot 1.18.2 or higher
- **Java**: Java 17 or higher
- **Memory**: Minimum 4GB RAM recommended
- **Storage**: At least 500MB free space for plugin files and NFT data
- **Internet Connection**: Required for blockchain interactions

### Software Dependencies

- **Node.js**: Version 16.0.0 or higher (for Solana blockchain integration)
- **npm**: Latest version (comes with Node.js)
- **Solana CLI** (optional): For testing and debugging blockchain interactions

### Required Plugins

- **WalletLogin**: Required for connecting player accounts to Solana wallets

### Optional Plugins

- **Vault**: For economy integration (if you plan to add NFT purchases later)
- **PlaceholderAPI**: For displaying NFT information in other plugins

## 💾 Installation

### Step 1: Download the Plugin

- Download the latest `NFT-Plugin.jar` file from the [releases page](https://github.com/yourusername/NFT-Plugin/releases)
- Download the required [WalletLogin](https://github.com/yourusername/WalletLogin/releases) plugin

### Step 2: Install the Plugin

- Stop your Minecraft server if it's running
- Place both JAR files in your server's `plugins` directory
- Create a folder named `NFTPlugin` in the `plugins` directory (optional, will be created automatically on first run)

### Step 3: First Run Setup

- Start your Minecraft server
- The plugin will automatically create the following directories:
  - `plugins/NFTPlugin/` - Main plugin directory
  - `plugins/NFTPlugin/metadata/` - NFT metadata files
  - `plugins/NFTPlugin/solana-backend/` - Solana integration files
- Stop the server after the directories are created

### Step 4: Set Up Solana Backend

1. Install Node.js if you haven't already (version 16+)
2. Open a terminal/command prompt and navigate to the solana-backend directory:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   ```
3. Install the required Node.js dependencies:
   ```bash
   npm install
   ```
4. This will install all necessary Solana libraries and dependencies

### Step 5: Create a Solana Wallet

1. Install the [Solana CLI](https://docs.solana.com/cli/install-solana-cli-tools) (optional but recommended)
2. Generate a new keypair for your server:
   ```bash
   solana-keygen new --outfile server-keypair.json
   ```
3. Get the private key in base58 format:
   ```bash
   solana-keygen pubkey --keypair server-keypair.json
   ```
4. Fund your wallet with SOL (for devnet):
   ```bash
   solana airdrop 2 YOUR_PUBLIC_KEY --url https://api.devnet.solana.com
   ```

### Step 6: Configure the Plugin

1. Create a `.env` file in the `plugins/NFTPlugin/solana-backend/` directory with the following content:
   ```
   SOLANA_PRIVATE_KEY=your_private_key_here
   SOLANA_NETWORK=devnet
   SOLANA_RPC_URL=https://api.devnet.solana.com
   MINT_FEE=0.000005
   CONFIRMATION_TIMEOUT=60000
   RETRY_COUNT=5
   ```
2. Edit the `config.yml` file in the `plugins/NFTPlugin/` directory (see Configuration section below)
3. Add your NFT metadata files to the `plugins/NFTPlugin/metadata/` directory

### Step 7: Upload Metadata to IPFS

1. Create an account on [Pinata](https://www.pinata.cloud/)
2. Upload your NFT metadata files to Pinata
3. Get the CID (Content Identifier) for each file
4. Add the CIDs to your `config.yml` file (see Configuration section)

### Step 8: Final Setup

1. Start your Minecraft server
2. Verify the plugin loaded correctly by checking the console for NFT Plugin startup messages
3. Test the plugin by running the command `/nfthelp` in-game

## 🔧 Configuration

### Main Configuration (config.yml)

Below is a detailed explanation of the `config.yml` file with all available options:

```yaml
# Plugin Settings
plugin:
  prefix: "&6[NFT] &r"  # Prefix for all plugin messages
  debug: false          # Enable detailed debug logging (set to true for troubleshooting)
  language: "en"        # Language file to use (en, fr, etc.)

# Solana Blockchain Settings
solana:
  # Network Configuration
  network: "devnet"     # Solana network (options: devnet, testnet, mainnet)
  rpc_url: "https://api.devnet.solana.com"  # Solana RPC URL
  mint_fee: 0.000005    # Fee for minting NFTs in SOL (0.000005 SOL = 5000 lamports)
  confirmation_timeout: 60000  # Transaction confirmation timeout in milliseconds
  retry_count: 3        # Number of retries for failed blockchain operations

  # Metadata Configuration
  use_metadata_image_url: true  # Use image URL from metadata files for NFT display
  default_image_url: "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4"

  # Pinata IPFS Integration
  use_pinata_metadata: true  # Use complete metadata files from Pinata IPFS
  pinata_base_uri: "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/"  # Base URI for Pinata IPFS

  # Metadata CIDs for each NFT type
  # Format: nft_key: "content_identifier"
  metadata_cids:
    lucky_charm_1: "bafkreih5hvacyeu4ojl374t7s3bhdeje3xnwxkmyqz2cidsqto6k5pavhy"
    lucky_charm_2: "bafkreih5hvacyeu4ojl374t7s3bhdeje3xnwxkmyqz2cidsqto6k5pavhy"
    lucky_charm_3: "bafkreih5hvacyeu4ojl374t7s3bhdeje3xnwxkmyqz2cidsqto6k5pavhy"
    explosion_pickaxe_1: "bafkreiabcd1234567890abcdef1234567890abcdef1234567890"
    explosion_pickaxe_2: "bafkreiabcd1234567890abcdef1234567890abcdef1234567890"
    explosion_pickaxe_3: "bafkreiabcd1234567890abcdef1234567890abcdef1234567890"
    explosion_pickaxe_4: "bafkreiabcd1234567890abcdef1234567890abcdef1234567890"
    explosion_pickaxe_5: "bafkreiabcd1234567890abcdef1234567890abcdef1234567890"
    laser_pickaxe_1: "bafkreiefgh1234567890abcdef1234567890abcdef1234567890"
    laser_pickaxe_2: "bafkreiefgh1234567890abcdef1234567890abcdef1234567890"
    laser_pickaxe_3: "bafkreiefgh1234567890abcdef1234567890abcdef1234567890"
    laser_pickaxe_4: "bafkreiefgh1234567890abcdef1234567890abcdef1234567890"
    laser_pickaxe_5: "bafkreiefgh1234567890abcdef1234567890abcdef1234567890"

# NFT Inventory Settings
inventory:
  title: "NFT Inventory"  # Title of the NFT inventory GUI
  rows: 6                 # Number of rows in the inventory (max 6)
  auto_pagination: true   # Automatically create new pages when needed
  item_spacing: 1         # Spacing between items (0 = no spacing)
  background_item: "BLACK_STAINED_GLASS_PANE"  # Material for background items

# NFT List Settings
nft_list:
  items_per_page: 5       # Number of NFTs to display per page
  show_explorer_link: true  # Show Solana Explorer link
  show_image_link: true   # Show NFT image link

# NFT Item Settings
nft_item:
  unbreakable: true       # Make NFT items unbreakable
  keep_on_death: true     # Keep NFT items on death
  allow_in_chests: true   # Allow storing NFT items in chests
  allow_in_enderchest: true  # Allow storing NFT items in enderchests
  prevent_dropping: false  # Prevent players from dropping NFT items

# Enchantment Settings
enchantments:
  explosion:
    enabled: true         # Enable Explosion Mining enchantment
    max_level: 4          # Maximum level for Explosion Mining (1-4)
    cooldown: 0           # Cooldown between uses in seconds (0 = no cooldown)
    affect_ores_only: false  # Only affect ore blocks
    patterns:             # Mining patterns for each level
      1: 3                # Level 1: 3x3 area
      2: 4                # Level 2: 4x4 area
      3: 5                # Level 3: 5x5 area
      4: 6                # Level 4: 6x6 area

  laser:
    enabled: true         # Enable Laser Mining enchantment
    max_level: 5          # Maximum level for Laser Mining (1-5)
    cooldown: 0           # Cooldown between uses in seconds
    affect_ores_only: false  # Only affect ore blocks
    depths:               # Mining depths for each level
      1: 2                # Level 1: 2 blocks deep
      2: 3                # Level 2: 3 blocks deep
      3: 4                # Level 3: 4 blocks deep
      4: 5                # Level 4: 5 blocks deep
      5: 6                # Level 5: 6 blocks deep

# Buff Settings
buffs:
  luck:
    enabled: true         # Enable Luck buff
    max_value: 20         # Maximum luck buff value (percentage)
    stack_method: "ADD"   # How buffs stack: ADD (add values) or MAX (use highest)

  # Add more buff types here as needed
```

### Solana Backend Configuration (.env)

Create a `.env` file in the `plugins/NFTPlugin/solana-backend/` directory with the following content:

```
# IMPORTANT: Server Wallet Private Key (base58 encoded)
# This wallet will be used to mint NFTs and pay for transaction fees
# KEEP THIS SECURE AND NEVER SHARE IT
SOLANA_PRIVATE_KEY=your_private_key_here

# Solana Network Configuration
SOLANA_NETWORK=devnet
SOLANA_RPC_URL=https://api.devnet.solana.com

# Transaction Settings
MINT_FEE=0.000005
CONFIRMATION_TIMEOUT=60000
RETRY_COUNT=5

# Advanced Settings (optional)
# MAX_CONCURRENT_TRANSACTIONS=3
# LOG_LEVEL=info
```

### NFT Metadata Structure

Each NFT requires a metadata JSON file in the `plugins/NFTPlugin/metadata/` directory. Here's a detailed explanation of the metadata structure:

#### Basic Metadata Fields

```json
{
  "name": "NFT Name",           // Required: Name of the NFT
  "symbol": "SYMBOL",           // Required: Short symbol for the NFT
  "description": "Description", // Required: Description of the NFT
  "image": "https://...",       // Required: URL to the NFT image

  "attributes": [               // Optional: Array of trait attributes
    {
      "trait_type": "Type",     // Category of the trait
      "value": "Tool"           // Value of the trait
    },
    {
      "trait_type": "Rarity",
      "value": "Rare"
    },
    {
      "trait_type": "Enchantment",
      "value": "Explosion Mining I"
    }
  ],

  "properties": {               // Optional: Additional properties
    "files": [                  // Array of associated files
      {
        "uri": "https://...",   // URL to the file
        "type": "image/png"     // MIME type of the file
      }
    ]
  }
}
```

#### Minecraft-Specific Fields

The `quest` section contains Minecraft-specific information:

```json
"quest": {
  "reward": {                   // Information about the in-game item
    "item": "DIAMOND_PICKAXE",  // Minecraft material type
    "name": "§b§lExplosion Pickaxe I",  // Formatted item name
    "lore": [                   // Array of lore lines
      "§7A pickaxe with explosive mining capabilities",
      "§7Mines blocks in a 3x3 area",
      "§d§lEnchantments:",
      "§b- Explosion Mining I"
    ],
    "enchantments": [           // Array of enchantments
      "DURABILITY:10",          // Format: ENCHANTMENT_KEY:LEVEL
      "EXPLOSION_MINING:1"
    ],
    "unbreakable": true,        // Whether the item is unbreakable
    "glowing": true,            // Whether the item has a glowing effect
    "custom_model_data": 9001   // Custom model data for resource packs
  },
  "buff": {                     // Optional: Buff provided by this NFT
    "type": "LUCK",             // Type of buff
    "value": 1                  // Value of the buff (percentage)
  }
}
```

### Example Metadata Files

#### Lucky Charm I (Buff NFT)

```json
{
  "name": "Lucky Charm I",
  "symbol": "LUCK1",
  "description": "A magical charm that brings a small amount of luck",
  "image": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafybeieiot6gbstht27uxqqcug5l3ers5mstnrxwr3rronmcs2il5vdr5e",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Charm"
    },
    {
      "trait_type": "Rarity",
      "value": "Common"
    },
    {
      "trait_type": "Buff",
      "value": "Luck +1%"
    }
  ],
  "properties": {
    "files": [
      {
        "uri": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafybeieiot6gbstht27uxqqcug5l3ers5mstnrxwr3rronmcs2il5vdr5e",
        "type": "image/png"
      }
    ]
  },
  "quest": {
    "reward": {
      "item": "EMERALD",
      "name": "§a§lLucky Charm I",
      "lore": [
        "§7A magical charm that brings a small amount of luck",
        "§7Earned through good fortune",
        "§d§lBuff Effects:",
        "§b- Luck: §7+1% chance of better drops",
        "§7\"Fortune favors the bold\"",
        "§dRarity: §aCommon"
      ],
      "enchantments": [
        "LUCK:1"
      ],
      "unbreakable": true,
      "glowing": true,
      "custom_model_data": 8001
    },
    "buff": {
      "type": "LUCK",
      "value": 1
    }
  }
}
```

#### Explosion Pickaxe I (Tool NFT)

```json
{
  "name": "Explosion Pickaxe I",
  "symbol": "EXPICK1",
  "description": "A pickaxe enchanted with explosive mining capabilities",
  "image": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafybeieiot6gbstht27uxqqcug5l3ers5mstnrxwr3rronmcs2il5vdr5e",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Tool"
    },
    {
      "trait_type": "Rarity",
      "value": "Rare"
    },
    {
      "trait_type": "Enchantment",
      "value": "Explosion Mining I"
    }
  ],
  "properties": {
    "files": [
      {
        "uri": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafybeieiot6gbstht27uxqqcug5l3ers5mstnrxwr3rronmcs2il5vdr5e",
        "type": "image/png"
      }
    ]
  },
  "quest": {
    "reward": {
      "item": "DIAMOND_PICKAXE",
      "name": "§b§lExplosion Pickaxe I",
      "lore": [
        "§7A pickaxe enchanted with explosive mining capabilities",
        "§7Mines blocks in a 3x3 area",
        "§d§lEnchantments:",
        "§b- Explosion Mining I",
        "§b- Unbreaking X",
        "§7\"One swing, many blocks\"",
        "§dRarity: §bRare"
      ],
      "enchantments": [
        "DURABILITY:10",
        "EXPLOSION_MINING:1"
      ],
      "unbreakable": true,
      "custom_model_data": 9001
    }
  }
}
```

## 💬 Commands

### Player Commands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/nftinv` | Open your NFT inventory | `nftplugin.command.nftinv` | `/nftinv` |
| `/nftlist` | View a list of your NFTs | `nftplugin.command.nftlist` | `/nftlist` |
| `/nfthelp` | Show help information | `nftplugin.command.nfthelp` | `/nfthelp` |

### Admin Commands

| Command | Description | Permission | Example |
|---------|-------------|------------|---------|
| `/mintnft <username> <metadata_key>` | Mint an NFT for a player | `nftplugin.command.mintnft` | `/mintnft Steve lucky_charm_1` |
| `/nftbuff <player>` | View a player's NFT buffs | `nftplugin.command.nftbuff` | `/nftbuff Steve` |

### Command Details

#### `/nftinv` - NFT Inventory

Opens a graphical inventory showing all NFTs owned by the player. Features include:

- **Unlimited Pagination**: Automatically creates new pages as needed
- **Interactive Interface**: Click on NFTs to view detailed information
- **Visual Display**: Shows NFT items with their actual appearance
- **Sorting**: NFTs are sorted by acquisition date (newest first)

Usage:
```
/nftinv
```

#### `/nftlist` - NFT List

Displays a text-based list of all NFTs owned by the player. Features include:

- **Pagination**: Navigate through pages of NFTs
- **Detailed Information**: Shows NFT name, description, and acquisition date
- **Blockchain Links**: Clickable links to view the NFT on Solana Explorer
- **Image Links**: Clickable links to view the NFT image

Usage:
```
/nftlist
```

#### `/nfthelp` - Help Command

Shows a list of available commands and their usage. The information displayed depends on the player's permissions:

- **Regular Players**: See only player commands
- **Operators/Admins**: See all commands including admin commands

Usage:
```
/nfthelp
```

#### `/mintnft` - Mint NFT (Admin Only)

Mints a new NFT for a specified player. This command:

1. Creates a new NFT on the Solana blockchain
2. Adds the NFT to the player's virtual inventory
3. Gives the player an in-game item representing the NFT

Usage:
```
/mintnft <username> <metadata_key>
```

Parameters:
- `<username>`: The name of the player to receive the NFT
- `<metadata_key>`: The key of the NFT metadata file (without .json extension)

Examples:
```
/mintnft Steve lucky_charm_1
/mintnft Alex explosion_pickaxe_3
/mintnft Notch laser_pickaxe_2
```

#### `/nftbuff` - Check Buffs (Admin Only)

Shows the active buffs provided by a player's NFTs. This command displays:

- The exact numeric values of all buffs
- The total combined buff value
- The source NFTs providing each buff

Usage:
```
/nftbuff <player>
```

Parameters:
- `<player>`: The name of the player to check

Example:
```
/nftbuff Steve
```

## 📦 NFT Metadata

### Understanding NFT Metadata

NFT metadata is the information that defines what an NFT is and how it appears both on the blockchain and in-game. The plugin uses a combination of on-chain and off-chain metadata:

1. **On-Chain Metadata**: Basic information stored directly on the Solana blockchain
   - Name
   - Symbol
   - URI pointing to the full metadata

2. **Off-Chain Metadata**: Detailed information stored on IPFS via Pinata
   - Complete description
   - Image URL
   - Attributes
   - Minecraft-specific properties

### Metadata Storage Flow

1. You create a JSON metadata file in the `plugins/NFTPlugin/metadata/` directory
2. You upload this file to Pinata IPFS and get a CID
3. You add this CID to your `config.yml` file
4. When an NFT is minted, the plugin:
   - Retrieves the metadata from Pinata using the CID
   - Creates the on-chain NFT with a link to this metadata
   - Creates an in-game item based on the metadata

### Metadata Fields Explained

#### Standard Metaplex Fields

These fields follow the [Metaplex NFT Standard](https://docs.metaplex.com/programs/token-metadata/token-standard):

- **name**: The name of the NFT (displayed on marketplaces and explorers)
- **symbol**: A short symbol for the NFT (like a ticker symbol)
- **description**: A detailed description of the NFT
- **image**: URL to the image representing the NFT
- **attributes**: Array of traits that describe the NFT
- **properties**: Additional properties including associated files

#### Minecraft-Specific Fields

These fields are specific to the NFT Plugin and control how the NFT appears in-game:

- **quest.reward.item**: The Minecraft material type for the item
- **quest.reward.name**: The formatted name of the item (supports color codes)
- **quest.reward.lore**: Array of lore lines (supports color codes)
- **quest.reward.enchantments**: Array of enchantments to apply
- **quest.reward.unbreakable**: Whether the item is unbreakable
- **quest.reward.glowing**: Whether the item has a glowing effect
- **quest.reward.custom_model_data**: Custom model data for resource packs
- **quest.buff**: Information about buffs provided by this NFT

## 📖 Usage Guide

### For Players

#### Connecting Your Wallet

1. **Register Your Solana Wallet**:
   - Use `/connectwallet <wallet_address>` to link your Solana wallet
   - You'll receive a confirmation message when successful
   - This allows you to receive NFTs on the Solana blockchain
   - You can view your NFTs on Solana Explorer or in-game

2. **Verifying Your Connection**:
   - Use `/wallet` to check your connected wallet address
   - The wallet address will be displayed in chat
   - This confirms your Minecraft account is linked to your Solana wallet

#### Managing Your NFTs

1. **View Your NFT Inventory**:
   - Use `/nftinv` to open your virtual NFT inventory
   - Your NFT items will be displayed in a GUI
   - The inventory automatically creates new pages as needed
   - Click on any NFT to see detailed information
   - Use the navigation buttons at the bottom to move between pages

2. **List Your NFTs**:
   - Use `/nftlist` to see your NFTs in an interactive list
   - Each NFT entry shows:
     - NFT name and acquisition date
     - Brief description
     - Rarity and type information
   - Click on any NFT to view detailed information including:
     - Full description and attributes
     - Transaction ID on the blockchain
     - Clickable Solana Explorer link to view on blockchain
     - Clickable image link to view the NFT artwork
   - Navigate between pages using the arrow buttons

3. **Get Help**:
   - Use `/nfthelp` to see available commands and information
   - The help menu shows commands you have permission to use
   - Each command includes a brief description of its function

#### Using NFT Special Abilities

1. **Explosion Mining**:
   - **Preparation**: Equip an Explosion Mining pickaxe in your main hand
   - **Activation**: Mine any block as you normally would
   - **Effect**: All blocks in a square area around the target block will be mined simultaneously
   - **Area Size**: Depends on enchantment level:
     - Level I: 3×3 area (9 blocks total)
     - Level II: 4×4 area (16 blocks total)
     - Level III: 5×5 area (25 blocks total)
     - Level IV: 6×6 area (36 blocks total)
   - **Behavior**:
     - Works on any block the pickaxe can normally mine
     - Respects block protection (won't mine protected blocks)
     - Drops all items as if mined normally
     - No durability loss due to unbreakable property

2. **Laser Mining**:
   - **Preparation**: Equip a Laser Mining pickaxe in your main hand
   - **Activation**: Mine any block as you normally would
   - **Effect**: All blocks in a straight line behind the target block will be mined
   - **Depth**: Depends on enchantment level:
     - Level I: 2 blocks deep
     - Level II: 3 blocks deep
     - Level III: 4 blocks deep
     - Level IV: 5 blocks deep
     - Level V: 6 blocks deep
   - **Behavior**:
     - Works in the direction you're facing
     - Only mines blocks the pickaxe can normally mine
     - Stops at unbreakable blocks or protected areas
     - Drops all items as if mined normally

3. **Lucky Charm Buffs**:
   - **Activation**: Simply keep Lucky Charm items in your inventory
   - **Effect**: Increases your chance of getting better drops from blocks and mobs
   - **Stacking**: Multiple Lucky Charms stack their effects:
     - Lucky Charm I: +1% luck
     - Lucky Charm II: +2% luck
     - Lucky Charm III: +5% luck
     - Lucky Charm IV: +10% luck
     - Lucky Charm V: +20% luck
   - **Example**: Having Lucky Charm I and Lucky Charm II gives +3% total luck

### For Administrators

#### Managing NFTs

1. **Mint NFTs for Players**:
   - **Command**: `/mintnft <player> <metadata_key>`
   - **Example**: `/mintnft Steve lucky_charm_1`
   - **Process**:
     1. The plugin connects to the Solana blockchain
     2. Creates a new NFT token with metadata from Pinata
     3. Transfers the NFT to the player's connected wallet
     4. Creates an in-game item representing the NFT
     5. Adds the item to the player's inventory
   - **Requirements**:
     - Server wallet must have sufficient SOL for transaction fees
     - Player must have a connected wallet (via WalletLogin)
     - Metadata file must exist for the specified key

2. **Check Player Buffs**:
   - **Command**: `/nftbuff <player>`
   - **Example**: `/nftbuff Steve`
   - **Output**:
     - Shows all active buffs and their exact numeric values
     - Lists the source NFTs providing each buff
     - Displays the total combined buff value
   - **Usage**: Useful for debugging and balancing

#### Setting Up NFTs

1. **Create Metadata Files**:
   - Create JSON files in the `plugins/NFTPlugin/metadata/` directory
   - Name format: `<metadata_key>.json` (e.g., `lucky_charm_1.json`)
   - Follow the format shown in the Configuration section
   - Include all required fields: name, description, image, etc.
   - Test your JSON with a validator to ensure it's properly formatted

2. **Upload to Pinata IPFS**:
   - Create an account on [Pinata](https://www.pinata.cloud/)
   - Upload your metadata JSON files to Pinata
   - Get the CID (Content Identifier) for each file
   - Add CIDs to the `config.yml` file under `solana.metadata_cids`

3. **Configure the Plugin**:
   - Set up the Solana wallet for the server (see Installation section)
   - Configure metadata URIs in `config.yml`
   - Customize enchantment and buff settings
   - Restart the server to apply changes

## ⚒️ Custom Enchantments

The NFT Plugin includes two powerful custom enchantments that enhance mining capabilities:

### Explosion Mining

Explosion Mining allows players to mine multiple blocks at once in a square pattern around the target block.

#### Technical Details

- **Enchantment ID**: `EXPLOSION_MINING`
- **Maximum Level**: 4
- **Applicable Items**: Pickaxes
- **Activation**: Breaking any minable block
- **Effect Area**: Square pattern centered on the broken block

#### Level Progression

| Level | Area Size | Total Blocks | Description |
|-------|-----------|--------------|-------------|
| I     | 3×3       | 9 blocks     | Small explosion radius |
| II    | 4×4       | 16 blocks    | Medium explosion radius |
| III   | 5×5       | 25 blocks    | Large explosion radius |
| IV    | 6×6       | 36 blocks    | Massive explosion radius |

#### Implementation Details

- The enchantment respects block protection plugins and won't break protected blocks
- All blocks are broken as if the player mined them normally (drops all items)
- Experience orbs are dropped for each block that would normally give experience
- The enchantment works in all dimensions (Overworld, Nether, End)
- No durability is consumed due to the unbreakable property of NFT items

### Laser Mining

Laser Mining allows players to mine multiple blocks in a straight line extending from the target block.

#### Technical Details

- **Enchantment ID**: `LASER_MINING`
- **Maximum Level**: 5
- **Applicable Items**: Pickaxes
- **Activation**: Breaking any minable block
- **Effect Area**: Straight line in the direction the player is facing

#### Level Progression

| Level | Depth | Total Blocks | Description |
|-------|-------|--------------|-------------|
| I     | 2     | 2 blocks     | Short laser beam |
| II    | 3     | 3 blocks     | Medium laser beam |
| III   | 4     | 4 blocks     | Long laser beam |
| IV    | 5     | 5 blocks     | Extended laser beam |
| V     | 6     | 6 blocks     | Maximum laser beam |

#### Implementation Details

- The laser beam extends in the direction the player is facing
- The beam stops if it encounters an unbreakable block or protected area
- All blocks are broken as if the player mined them normally
- The enchantment is particularly useful for mining veins of ore or creating tunnels
- No durability is consumed due to the unbreakable property of NFT items

## 🔮 Buff System

The NFT Plugin includes a buff system that provides passive bonuses to players based on the NFTs they possess.

### Luck Buff

The Luck buff increases a player's chance of receiving better drops from blocks and mobs.

#### Technical Details

- **Buff ID**: `LUCK`
- **Value Range**: 1% to 20%
- **Stacking Method**: Additive (multiple buffs add their values together)
- **Activation**: Passive (active while the NFT is in the player's inventory)

#### Buff Levels

| NFT | Buff Value | Description |
|-----|------------|-------------|
| Lucky Charm I | +1% | Slight increase in luck |
| Lucky Charm II | +2% | Minor increase in luck |
| Lucky Charm III | +5% | Moderate increase in luck |
| Lucky Charm IV | +10% | Significant increase in luck |
| Lucky Charm V | +20% | Major increase in luck |

#### Implementation Details

- The luck percentage directly increases the chance of rare drops
- For example, with a +10% luck buff, a drop with a base 5% chance becomes 5.5% (5% + 10% of 5%)
- The buff applies to all random drops in the game (blocks, mobs, fishing, etc.)
- Multiple Lucky Charms stack their effects (e.g., Lucky Charm I + Lucky Charm II = +3% luck)
- The maximum combined luck buff is capped at 20% by default (configurable)

### Viewing Buffs

- **Players**: Cannot directly view their buff values
- **Administrators**: Can view any player's buffs using the `/nftbuff <player>` command
- The command shows the exact numeric values of all buffs and their sources

## 🔧 Troubleshooting

### Common Issues and Solutions

#### Solana Integration Issues

| Issue | Solution |
|-------|----------|
| **"Failed to mint NFT"** | Ensure server wallet has sufficient SOL (at least 0.05 SOL) |
| **"Failed to load bindings"** | Run `npm rebuild` in the solana-backend directory |
| **"Error: NFT metadata is required"** | Ensure metadata files have name, description, and image fields |
| **"Cannot connect to RPC URL"** | Check network connection and RPC URL in config |
| **"Transaction simulation failed"** | Verify the server wallet has enough SOL for the transaction |
| **"Invalid private key"** | Check that the private key in .env is in base58 format |

#### Plugin Issues

| Issue | Solution |
|-------|----------|
| **NFT items not showing custom models** | Ensure custom_model_data is set correctly in metadata |
| **Enchantments not working** | Check that the enchantment is enabled in config.yml |
| **NFT inventory not opening** | Verify the player has permission to use the command |
| **Explorer links not working** | Check that the Solana network is configured correctly |

#### Node.js Backend Issues

If you encounter issues with the Node.js backend:

1. **Rebuild Node.js Dependencies**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm rebuild
   ```

2. **Update Solana Dependencies**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install @metaplex-foundation/js@latest @solana/web3.js@latest
   ```

3. **Clean Install**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   rm -rf node_modules
   npm cache clean --force
   npm install
   ```

#### Testing the Solana Backend

To test the Solana backend directly:

```bash
cd plugins/NFTPlugin/solana-backend
node mint-nft.js --metadata-uri "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreih5hvacyeu4ojl374t7s3bhdeje3xnwxkmyqz2cidsqto6k5pavhy" --recipient "5EQJH2HDC2oUbvMCTnYa7vwLsY2Um7Y6LAJsZoqxZQGz" --name "Lucky Charm I" --description "Test Description" --image "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafybeieiot6gbstht27uxqqcug5l3ers5mstnrxwr3rronmcs2il5vdr5e"
```

#### Getting SOL for Testing

For testing on Solana devnet, you can get free SOL from:
- [Solana Faucet](https://solfaucet.com/)
- [QuickNode Faucet](https://faucet.quicknode.com/solana/devnet)

## 🚀 Creating New NFTs

### Step 1: Create Metadata File

Create a new JSON file in the `plugins/NFTPlugin/metadata/` directory with the following structure:

```json
{
  "name": "Your NFT Name",
  "symbol": "SYMBOL",
  "description": "Description of your NFT",
  "image": "https://your-pinata-ipfs-url.com/ipfs/your-image-cid",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Tool/Charm/Artifact"
    },
    {
      "trait_type": "Rarity",
      "value": "Common/Rare/Epic/Legendary"
    },
    {
      "trait_type": "Enchantment",
      "value": "Explosion Mining I"
    }
  ],
  "properties": {
    "files": [
      {
        "uri": "https://your-pinata-ipfs-url.com/ipfs/your-image-cid",
        "type": "image/png"
      }
    ]
  },
  "quest": {
    "reward": {
      "item": "DIAMOND_PICKAXE",
      "name": "§b§lYour NFT Name",
      "lore": [
        "§7Description line 1",
        "§7Description line 2",
        "§d§lEnchantments:",
        "§b- Enchantment description"
      ],
      "enchantments": [
        "DURABILITY:10",
        "EXPLOSION_MINING:1"
      ],
      "unbreakable": true,
      "custom_model_data": 9001
    }
  }
}
```

### Step 2: Upload to Pinata IPFS

1. Create an account on [Pinata](https://www.pinata.cloud/)
2. Upload your metadata file to Pinata
3. Get the CID (Content Identifier) of your uploaded file

### Step 3: Update Configuration

Add the CID to your `config.yml` file:

```yaml
solana:
  metadata_cids:
    your_nft_key: "your-metadata-cid-from-pinata"
```

### Step 4: Test Your NFT

Use the admin command to mint your new NFT:

```
/mintnft <player> your_nft_key
```

## 💻 Technical Details

### Solana Integration

The plugin uses the following technologies for Solana blockchain integration:

- **Metaplex SDK**: For creating and managing NFTs on Solana
- **Solana Web3.js**: For interacting with the Solana blockchain
- **Node.js Backend**: For handling blockchain operations
- **IPFS via Pinata**: For decentralized storage of metadata and images

#### Viewing NFTs on Solana Explorer

Players can view their NFTs on the Solana blockchain:

1. Use `/nftlist` or `/nftinv` and click on an NFT
2. Click the "Click to Open Explorer" button in the detailed view
3. This opens the Solana Explorer in a web browser
4. The Explorer shows all blockchain details about the NFT:
   - Owner address
   - Metadata
   - Transaction history
   - Token information

#### Viewing NFT Images

Players can view the actual NFT artwork:

1. Use `/nftlist` or `/nftinv` and click on an NFT
2. Click the "Open Image in Browser" button in the detailed view
3. This opens the NFT image stored on IPFS in a web browser

### Enchantment System

Custom enchantments are implemented using:

- **PersistentDataContainer**: For storing enchantment data on items
- **Custom Event Handlers**: For processing mining events
- **Efficient Algorithms**: For calculating affected blocks

### Buff System

The buff system provides in-game bonuses:

- **Luck Buffs**: Increase drop rates for rare items (1% to 20%)
- **Stacking System**: Multiple buffs of the same type add together
- **Passive Effects**: Active as long as the NFT is in the player's inventory
- **Admin Monitoring**: Server operators can view all active buffs with `/nftbuff`

### NFT Inventory

The virtual NFT inventory system features:

- **Custom GUI**: User-friendly interface for managing NFTs
- **Unlimited Pagination**: Automatically creates new pages as needed
- **Persistent Storage**: NFTs are saved between sessions
- **Interactive Elements**: Click on NFTs to view detailed information
- **Visual Display**: Shows NFT items with their actual appearance and properties

### Data Storage

The plugin uses several storage systems:

- **Local Files**: Metadata templates and configuration
- **Pinata IPFS**: Decentralized storage for metadata and images
- **Solana Blockchain**: Immutable record of NFT ownership
- **Plugin Database**: Links between in-game items and blockchain NFTs

## 📞 Support and Contact

If you need help with the plugin:

- **GitHub Issues**: Report bugs and request features at [GitHub Repository](https://github.com/yourusername/NFT-Plugin/issues)
- **Discord**: Join our community for support at [Discord Server](https://discord.gg/yourserver)
- **Email**: Contact us directly at support@yourwebsite.com

## 📜 License

This plugin is released under the MIT License. See the LICENSE file for details.

---

<div align="center">

### ⭐ Enjoy your NFT adventures in Minecraft! ⭐

[<img src="https://img.shields.io/badge/Solana-Devnet-blue?style=for-the-badge&logo=solana" alt="Solana Devnet">](https://explorer.solana.com/?cluster=devnet)
[<img src="https://img.shields.io/badge/Minecraft-1.18.2+-green?style=for-the-badge&logo=minecraft" alt="Minecraft 1.18.2+">](https://www.minecraft.net/)
[<img src="https://img.shields.io/badge/IPFS-Pinata-orange?style=for-the-badge&logo=ipfs" alt="IPFS Pinata">](https://www.pinata.cloud/)
[<img src="https://img.shields.io/badge/Paper-Spigot-yellow?style=for-the-badge" alt="Paper/Spigot">](https://papermc.io/)

</div>
