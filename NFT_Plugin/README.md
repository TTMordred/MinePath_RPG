# ✨ Minecraft NFT Plugin ✨

A Solana NFT integration plugin for Minecraft, allowing players to receive NFTs when they achieve specific in-game accomplishments. This plugin creates a seamless bridge between Minecraft gameplay and Solana blockchain technology.

## ✅ Features

- **Mint NFTs on Solana DevNet** when players achieve in-game accomplishments
- **SolanaLogin Integration** to link Solana wallets with Minecraft accounts
- **Flexible Achievement System** based on holding specially named items
- **Easy Configuration** through config.yml and JSON metadata files
- **Add New Achievements** without recompiling the plugin
- **Enhanced NFT Display** with compact and user-friendly information
- **NFT List Command** with pagination for browsing all your NFTs
- **Interactive UI** with clickable buttons for Solana Explorer and image links
- **Visual Enhancements** for better user experience

## ⚙️ Requirements

- Minecraft Paper 1.18.2
- Java 17 or higher
- Node.js 16 or higher (for Solana backend)
- SolanaLogin Plugin (for wallet linking)
- MySQL/MariaDB (for data storage)

## 💾 Installation

1. Download the latest JAR file from [<kbd>Download Latest Release</kbd>](https://github.com/Woft257/nft-plugin/releases)
2. Place the JAR file in your Minecraft server's `plugins` directory
3. Start the server to generate configuration files
4. Configure the plugin in `plugins/NFTPlugin/config.yml`
5. Set up the Solana backend:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install
   ```
6. Configure the Solana backend in `plugins/NFTPlugin/solana-backend/.env`
7. Restart the server

## 🔧 Configuration

### config.yml

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
  great_light:
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

# Solana Settings
solana:
  network: "devnet"
  rpc_url: "https://api.devnet.solana.com"
  server_wallet_private_key: "" # Do not fill this in here! Use the SOLANA_PRIVATE_KEY environment variable
  mint_fee: 0.000005
```

### Solana Backend Configuration (.env)

Create a `.env` file in the `plugins/NFTPlugin/solana-backend/` directory with the following content:

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

**great_light.json**:
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

- `/nftinfo` - Display information about the NFT item currently held in hand
- `/nftlist` - View a list of all your NFTs with pagination
- `/resetnft <player>` - Reset a player's achievement and NFT progress (Admin only)
- `/mintnft <player> <achievement_key>` - Manually mint an NFT for a player (Admin only)

## 📖 Usage Guide

1. **Register Solana Wallet**:
   - Players need to register their Solana wallet using the SolanaLogin plugin
   - Use the command `/connectwallet <wallet_address>` from the SolanaLogin plugin

2. **Achieve Accomplishments**:
   - Players need to find and hold specially named items
   - When holding the item, the plugin will automatically mint an NFT and send it to the player's Solana wallet
   - The original item will be removed and replaced with an in-game NFT item

3. **View NFT Information**:
   - Hold the NFT item and use the `/nftinfo` command
   - Detailed information about the NFT will be displayed, including clickable links to Solana Explorer
   - The information is displayed in a compact and user-friendly format

4. **Browse Your NFT Collection**:
   - Use the `/nftlist` command to open an inventory with all your NFTs
   - Navigate through pages using the arrow buttons if you have many NFTs
   - Click on any NFT to view detailed information about it

## 🔧 Troubleshooting

### "Signature is not valid" Error

If you encounter a "Signature is not valid" error when minting NFTs:

1. **Check Server Wallet Balance**:
   - Ensure the server wallet has sufficient SOL (at least 0.05 SOL)
   - Add SOL to the server wallet from the Solana Faucet: [<kbd>Get SOL from Faucet</kbd>](https://solfaucet.com/)

2. **Try Alternative RPC URL**:
   - Change the RPC_URL in the `.env` file to `https://devnet.genesysgo.net/`

3. **Update Metaplex**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   npm install @metaplex-foundation/js@latest
   ```

4. **Clear Cache and Reinstall**:
   ```bash
   cd plugins/NFTPlugin/solana-backend
   rm -rf node_modules
   npm cache clean --force
   npm install
   ```

### Backend Testing Command

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

## ✨ Adding New Achievements

To add a new achievement:

1. **Create a Metadata File**:
   - Create a new JSON file in the `plugins/NFTPlugin/metadata/` directory
   - Name the file according to the format `<achievement_key>.json`

2. **Update config.yml**:
   - Add a new entry in the `achievements` section of the `config.yml` file

Example for adding a "Diamond Sword" achievement:

**diamond_sword.json**:
```json
{
  "name": "Diamond Sword of Power",
  "description": "A legendary diamond sword with immense power",
  "image": "https://cyan-perfect-clam-972.mypinata.cloud/ipfs/bafkreifri6u3f3ww7u6v2gkkcfsol2ijqbno5qmc77n5h57hytebvtr6n4",
  "attributes": [
    {
      "trait_type": "Type",
      "value": "Weapon"
    },
    {
      "trait_type": "Rarity",
      "value": "Legendary"
    }
  ],
  "quest": {
    "type": "HOLD_NAMED_ITEM_INSTANT",
    "target": "DIAMOND_SWORD",
    "target_name": "Sword of Power",
    "duration": 0,
    "description": "Hold a Diamond Sword named 'Sword of Power'"
  }
}
```

**Update config.yml**:
```yaml
achievements:
  # Existing achievements...

  # Diamond Sword
  diamond_sword:
    enabled: true
    type: named_item
    material: DIAMOND_SWORD
    item_name: "Sword of Power"
```

## 🔐 License

This plugin is released under the MIT License.

## 📬 Contact

If you have any questions or encounter issues, please create an issue on GitHub or contact via email: your.email@example.com

---

<div align="center">

### ⭐ Enjoy using the Minecraft NFT Plugin! ⭐

[<kbd>Report an Issue</kbd>](https://github.com/Woft257/nft-plugin/issues/new) &nbsp;&nbsp;&nbsp; [<kbd>Request a Feature</kbd>](https://github.com/Woft257/nft-plugin/issues/new?labels=enhancement)

</div>
