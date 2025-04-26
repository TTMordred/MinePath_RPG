# ✨ Minecraft NFT Plugin ✨

A comprehensive Solana NFT integration plugin for Minecraft, allowing players to receive, store, and use NFTs with special abilities in-game. This plugin creates a seamless bridge between Minecraft gameplay and Solana blockchain technology, enhancing player experience with unique collectible items.

<div align="center">

![NFT Plugin Banner](https://i.imgur.com/placeholder.png)

</div>

## ✅ Features

- **Solana Blockchain Integration**: Mint NFTs directly on the Solana blockchain when players complete achievements
- **Achievement System**: Earn NFTs by finding and holding special named items in-game
- **Virtual NFT Inventory**: Store and manage your NFTs with a paginated inventory system
- **Custom Enchantments**:
  - **Explosion Mining** (Levels I-V): Mine in 3x3 to 7x7 areas
  - **Laser Mining** (Levels I-V): Mine up to 6 blocks deep
- **Buff System**: Gain special abilities and bonuses from your NFTs
  - **Lucky Charms**: Increase drop rates for rare items
- **SolanaLogin Integration**: Securely link your Solana wallet to your Minecraft account
- **NFT Lootbox System**: Purchase and open lootboxes with different rarity tiers
- **Admin Commands**: Comprehensive tools for server administrators
- **Interactive UI**: View NFT details with clickable Solana Explorer links
- **Database Storage**: Secure MySQL/MariaDB integration for persistent data

## ⚙️ Requirements

- Minecraft Paper/Spigot 1.18+
- Java 17 or higher
- Node.js 16+ (for Solana backend)
- SolanaLogin Plugin (optional but recommended)
- MySQL/MariaDB database
- Vault (for Lootbox economy integration)

## 💾 Installation

1. Download the plugin JAR file
2. Place the JAR file in your Minecraft server's `plugins` directory
3. Start the server to generate configuration files
4. Configure the plugin in `plugins/NFTPlugin/config.yml`
5. Set up the database connection in the config file
6. Set up the Solana backend:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install
   ```
7. Configure the Solana backend in `.env` file (see below)
8. Restart the server

## 🔧 Configuration

### Main Configuration (config.yml)

```yaml
# Database Configuration
database:
  host: localhost
  port: 3306
  database: minecraft
  username: root
  password: your_password
  table-prefix: nftplugin_

# Achievement Settings
achievements:
  # Great Light - Blaze Rod
  anh_sang_vi_dai:
    enabled: true
    type: named_item
    material: BLAZE_ROD
    item_name: "Great Light"

  # Ancient Scroll - Paper item
  ancient_scroll:
    enabled: true
    type: named_item
    material: PAPER
    item_name: "Ancient Scroll"

  # Diamond Sword - Diamond Sword item
  diamond_sword:
    enabled: true
    type: named_item
    material: DIAMOND_SWORD
    item_name: "Sword of Power"

# Solana Settings
solana:
  network: "devnet"
  rpc_url: "https://api.devnet.solana.com"
  server_wallet_private_key: "" # DO NOT FILL THIS IN THE CONFIG FILE! Use environment variable instead
  mint_fee: 0.000005
```

### Solana Backend Configuration (.env)

Create a `.env` file in the `plugins/NFTPlugin/solana-backend/` directory:

```
# Server wallet private key (base58 format)
SOLANA_PRIVATE_KEY=your_private_key_here

# Solana network (devnet, testnet, mainnet)
SOLANA_NETWORK=devnet

# Solana RPC URL
SOLANA_RPC_URL=https://api.devnet.solana.com

# NFT minting fee (SOL)
MINT_FEE=0.000005

# Transaction confirmation timeout (milliseconds)
CONFIRMATION_TIMEOUT=60000

# Number of retry attempts on error
RETRY_COUNT=5
```

### Metadata Files

Create JSON files in the `plugins/NFTPlugin/metadata/` directory for each achievement:

**anh_sang_vi_dai.json**:
```json
{
  "name": "Great Light Staff",
  "description": "A mystical staff containing the power of the great light",
  "image": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Weapon"
    },
    {
      "trait_type": "Rarity",
      "value": "Legendary"
    },
    {
      "trait_type": "Enchantment",
      "value": "Explosion Mining III"
    }
  ],
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "BLAZE_ROD",
    "target_name": "Great Light",
    "duration": 0,
    "description": "Hold a Blaze Rod named 'Great Light'"
  }
}
```

**ancient_scroll.json**:
```json
{
  "name": "Ancient Scroll",
  "description": "A mysterious scroll containing ancient knowledge",
  "image": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Artifact"
    },
    {
      "trait_type": "Rarity",
      "value": "Epic"
    },
    {
      "trait_type": "Buff",
      "value": "Lucky Charm +5%"
    }
  ],
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "PAPER",
    "target_name": "Ancient Scroll",
    "duration": 0,
    "description": "Hold a Paper item named 'Ancient Scroll'"
  }
}
```

## 💬 Commands

### Player Commands
- `/nftinfo` - Display information about the NFT item currently held in hand
- `/nftlist` - View a list of all your NFTs with pagination
- `/nftinv [page]` - Open your virtual NFT inventory to store and manage NFTs
- `/nftbuff` - View your active NFT buffs and their values

### Admin Commands
- `/resetnft <player> [achievement_key]` - Reset a player's achievement and NFT progress
- `/mintnft <username> <metadata_key>` - Manually mint an NFT for a player
- `/nftbuff [player]` - View a player's active NFT buffs and their values
- `/test` - Test command for debugging purposes

### Lootbox Commands
- `/nftlootbox <type> <amount>` - Purchase NFT lootboxes (requires Vault)
  - Types: basic_nft, premium_nft, ultimate_nft

## 📖 Usage Guide

### For Players

1. **Register Your Solana Wallet**:
   - Install the SolanaLogin plugin on your server
   - Use `/connectwallet <wallet_address>` to link your Solana wallet
   - This allows you to receive NFTs on the Solana blockchain

2. **Earn NFTs Through Achievements**:
   - Find and hold special named items to trigger achievements
   - For example, hold a Blaze Rod named "Great Light" to earn the Great Light NFT
   - When an achievement is completed, an NFT will be minted to your wallet

3. **Manage Your NFT Inventory**:
   - Use `/nftinv` to open your virtual NFT inventory
   - Store, organize, and manage your NFT items
   - Navigate through pages with the pagination buttons

4. **Use NFT Special Abilities**:
   - **Explosion Mining**: Mine blocks in a square area (3x3 to 7x7)
   - **Laser Mining**: Mine blocks in a straight line (up to 6 blocks deep)
   - **Lucky Charm**: Increase your chance of finding rare items

5. **View NFT Information**:
   - Hold an NFT item and use `/nftinfo` to see detailed information
   - Use `/nftlist` to browse all your NFTs with pagination
   - Click on NFTs in the list to view detailed information

6. **Purchase Lootboxes**:
   - Use `/nftlootbox <type> <amount>` to purchase lootboxes
   - Open lootboxes to receive random NFTs of different rarities

### For Administrators

1. **Mint NFTs for Players**:
   - Use `/mintnft <player> <metadata_key>` to mint an NFT for a player
   - The NFT will be added to the player's wallet and virtual inventory

2. **Monitor Player Buffs**:
   - Use `/nftbuff <player>` to view a player's active NFT buffs
   - This helps with balancing and troubleshooting

3. **Reset Player Progress**:
   - Use `/resetnft <player> [achievement_key]` to reset progress
   - Reset a specific achievement or all achievements

## 🎮 NFT Lootbox System

The plugin includes a comprehensive lootbox system with different tiers:

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

## 🔧 Troubleshooting

### Solana Integration Issues

If you encounter issues with Solana integration:

1. **Check Server Wallet Balance**:
   - Ensure the server wallet has sufficient SOL (at least 0.05 SOL)
   - Add SOL to the server wallet from the Solana Faucet: [Solana Faucet](https://solfaucet.com/)

2. **Verify RPC URL**:
   - Make sure the RPC URL in the `.env` file is correct and accessible
   - Try alternative RPC URLs if needed: `https://devnet.genesysgo.net/`

3. **Update Solana Dependencies**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install @metaplex-foundation/js@latest @solana/web3.js@latest
   ```

4. **Troubleshoot Node.js Backend**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   rm -rf node_modules
   npm cache clean --force
   npm install
   ```

### Testing Solana Backend

To test the Solana backend directly:

```bash
cd plugins/NFTPlugin/solana-backend
node mint-nft.js \
  --network devnet \
  --rpc-url https://api.devnet.solana.com \
  --private-key your_private_key \
  --recipient recipient_wallet_address \
  --name "Test NFT" \
  --description "This is a test NFT" \
  --image "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4" \
  --player "TestPlayer" \
  --achievement "test_achievement"
```

### Database Issues

If you encounter database-related issues:

1. **Check Database Connection**:
   - Verify that the database credentials in `config.yml` are correct
   - Make sure the MySQL/MariaDB server is running and accessible

2. **Table Creation Issues**:
   - The plugin automatically creates necessary tables
   - If tables are not created, check database user permissions

3. **Data Persistence Problems**:
   - If data is not being saved, check database connection settings
   - Verify that the database user has INSERT/UPDATE/DELETE privileges

## ✨ Adding New NFTs

To add a new NFT to your server:

1. **Create a Metadata File**:
   - Create a new JSON file in the `plugins/NFTPlugin/metadata/` directory
   - Name the file according to the format `<achievement_key>.json`
   - Include all necessary fields: name, description, image, attributes, and quest

2. **Add Achievement to Config**:
   - Add a new entry in the `achievements` section of `config.yml`
   - Specify the type, material, and item name for the achievement

3. **Restart the Server**:
   - The plugin will automatically detect and load the new NFT metadata
   - No need to recompile the plugin

### Custom Enchantments and Buffs

To add custom enchantments or buffs to your NFTs:

1. **Add Enchantment Attributes**:
   - Include attributes with `trait_type: "Enchantment"` in the metadata file
   - Format: `"value": "Explosion Mining III"` or `"value": "Laser Mining II"`

2. **Add Buff Attributes**:
   - Include attributes with `trait_type: "Buff"` in the metadata file
   - Format: `"value": "Lucky Charm +5%"`

## 📚 Technical Details

- **Database Structure**: The plugin uses MySQL/MariaDB with tables for wallets, achievements, NFTs, and inventory
- **Solana Integration**: Uses Node.js backend with Metaplex and Solana Web3.js libraries
- **Custom Enchantments**: Implemented using PersistentDataContainer for efficient storage
- **Buff System**: Provides in-game bonuses based on NFT attributes
- **Inventory System**: Paginated virtual inventory for storing and managing NFTs

## 📬 Support and Contact

If you encounter any issues or have questions about the plugin:

- Check the troubleshooting section above
- Review the plugin configuration
- Contact the developer for support

---

<div align="center">

### ⭐ Enjoy using the Minecraft NFT Plugin! ⭐

</div>
