# NFT-Plugin Technology Documentation

## Blockchain Integration Technologies

### Solana Blockchain Core

The NFT-Plugin leverages the Solana blockchain as its foundation for NFT creation and verification, utilizing several key technologies:

#### 1. Solana Web3.js Library (v1.77.3)

The plugin integrates with Solana through the official `@solana/web3.js` JavaScript library, which provides:

- **RPC Connection Management**: Establishes secure connections to Solana nodes
- **Transaction Construction**: Creates properly formatted Solana transactions
- **Account Management**: Handles wallet keypairs and public addresses
- **Commitment Levels**: Supports different confirmation levels for transactions

```javascript
// From mint-nft.js - Core Solana connection setup
const { Connection, Keypair, PublicKey } = require('@solana/web3.js');

// Create connection with specific commitment and timeout settings
const connection = new Connection(options.rpcUrl, {
  commitment: 'confirmed',
  confirmTransactionInitialTimeout: options.confirmationTimeout
});

// Create wallet from private key
const privateKeyBytes = bs58.decode(options.privateKey);
const wallet = Keypair.fromSecretKey(privateKeyBytes);
```

#### 2. Metaplex Foundation SDK (v0.19.4)

The plugin uses the Metaplex SDK (`@metaplex-foundation/js`) to handle NFT-specific operations:

- **Metadata Creation**: Generates and uploads NFT metadata to Arweave via Bundlr
- **NFT Minting**: Creates NFTs following the Metaplex Token Metadata standard
- **Token Transfer**: Handles secure transfer of NFTs between wallets
- **Master Edition Creation**: Supports limited edition NFTs with controlled supply

```javascript
// From mint-nft.js - Metaplex initialization and usage
const { Metaplex, keypairIdentity, bundlrStorage } = require('@metaplex-foundation/js');

// Initialize Metaplex with wallet identity and storage solution
const metaplex = Metaplex.make(connection)
  .use(keypairIdentity(wallet))
  .use(bundlrStorage({
    address: options.network === 'mainnet' ? 'https://node1.bundlr.network' : 'https://devnet.bundlr.network',
    providerUrl: options.rpcUrl,
    timeout: options.confirmationTimeout,
  }));

// Upload metadata to Arweave (via Bundlr)
const { uri } = await metaplex.nfts().uploadMetadata({
  name: options.name,
  description: options.description,
  image: options.image,
  attributes: [
    { trait_type: 'Player', value: options.player },
    { trait_type: 'Achievement', value: options.achievement },
    { trait_type: 'Date', value: new Date().toISOString() }
  ]
});

// Create the NFT using Metaplex standards
const { nft } = await metaplex.nfts().create({
  uri,
  name: options.name,
  sellerFeeBasisPoints: 0, // No royalties
  maxSupply: 1, // Unique NFT
  isMutable: false, // Cannot be changed
  creators: [{ address: wallet.publicKey, share: 100 }],
  tokenOwner: wallet.publicKey,
  tokenStandard: 0 // Non-fungible token
}, { commitment: 'confirmed' });
```

#### 3. BS58 Encoding Library (v5.0.0)

The plugin uses the `bs58` library for Base58 encoding/decoding, which is essential for Solana's cryptographic operations:

- **Private Key Handling**: Converts between binary private keys and Base58 strings
- **Address Formatting**: Ensures proper encoding of Solana addresses
- **Transaction Signing**: Facilitates the signing process with properly encoded keys

```javascript
// From mint-nft.js - BS58 usage for key handling
const bs58 = require('bs58');

// Decode a Base58 private key string into bytes
const privateKeyBytes = bs58.decode(options.privateKey);
// Create a Solana keypair from the decoded bytes
const wallet = Keypair.fromSecretKey(privateKeyBytes);
```

#### 4. Commander.js (v11.0.0)

The plugin uses Commander.js for robust command-line argument parsing in the Node.js backend:

- **Argument Parsing**: Cleanly handles command-line arguments with validation
- **Option Management**: Supports default values and type conversion
- **Help Generation**: Automatically generates help documentation

```javascript
// From mint-nft.js - Command-line argument parsing
const { program } = require('commander');

// Define command-line options with types and defaults
program
  .option('--network <network>', 'Solana network (mainnet, testnet, devnet)', 'devnet')
  .option('--rpc-url <url>', 'RPC URL', 'https://api.devnet.solana.com')
  .option('--private-key <key>', 'Private key of the server wallet (base58 encoded)')
  .option('--recipient <address>', 'Recipient wallet address')
  .option('--name <name>', 'NFT name')
  .option('--description <description>', 'NFT description')
  .option('--image <url>', 'NFT image URL')
  .option('--confirmation-timeout <ms>', 'Timeout for transaction confirmation in milliseconds', '60000')
  .parse(process.argv);
```

#### 5. Dotenv (v16.3.1)

The plugin uses Dotenv for secure environment variable management:

- **Configuration Management**: Loads sensitive configuration from .env files
- **Secret Handling**: Keeps private keys and API endpoints out of source code
- **Environment Switching**: Facilitates different configurations for development/production

```javascript
// From mint-nft.js - Environment variable loading
try {
  // Try to load .env from current directory
  if (fs.existsSync('.env')) {
    require('dotenv').config();
    console.log('Loaded .env file from current directory');
  }
  // Try to load .env from script directory
  else {
    const scriptDir = __dirname;
    const envPath = `${scriptDir}/.env`;

    if (fs.existsSync(envPath)) {
      require('dotenv').config({ path: envPath });
      console.log(`Loaded .env file from script directory: ${envPath}`);
    }
  }
} catch (error) {
  console.log(`Error loading .env file: ${error.message}`);
}

// Use environment variables as fallback
options.privateKey = options.privateKey || process.env.SOLANA_PRIVATE_KEY;
options.network = options.network || process.env.SOLANA_NETWORK || 'devnet';
options.rpcUrl = options.rpcUrl || process.env.SOLANA_RPC_URL || 'https://api.devnet.solana.com';
```

## Java-Blockchain Integration

### Java-Node.js Bridge Architecture

The NFT-Plugin implements a sophisticated bridge between Java (Minecraft) and Node.js (Solana) components:

#### 1. Process-Based Communication

The plugin uses Java's `ProcessBuilder` to execute the Node.js minting script with appropriate parameters:

- **Asynchronous Execution**: Runs blockchain operations without blocking the game thread
- **Parameter Passing**: Transfers all necessary data from Java to Node.js
- **Output Parsing**: Captures and parses the Node.js script output
- **Error Handling**: Robust error detection and reporting

```java
// From SolanaService.java - Process-based communication
ProcessBuilder pb = new ProcessBuilder(
    "node",
    nodeJsScriptFile.getAbsolutePath(),
    "--network", network,
    "--rpc-url", rpcUrl,
    "--private-key", privateKey,
    "--recipient", walletAddress,
    "--name", nftName,
    "--description", nftDescription,
    "--image", nftImageUrl,
    "--player", player.getName(),
    "--achievement", achievementKey
);

// Set working directory and redirect error stream
pb.directory(backendDir);
pb.redirectErrorStream(true);

// Start the process
Process process = pb.start();

// Read the output
BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
StringBuilder output = new StringBuilder();
String line;
while ((line = reader.readLine()) != null) {
    output.append(line).append("\n");
    plugin.getLogger().info("[Solana] " + line);
}

// Wait for the process to complete
int exitCode = process.waitFor();
```

#### 2. Asynchronous Processing

The plugin uses Bukkit's scheduler and Java's `CompletableFuture` for non-blocking operations:

- **Task Scheduling**: Offloads blockchain operations to background threads
- **Promise-Based Results**: Uses `CompletableFuture` for asynchronous result handling
- **Callback Handling**: Processes results when blockchain operations complete
- **Exception Propagation**: Properly handles and reports errors

```java
// From SolanaService.java - Asynchronous processing
public CompletableFuture<String> mintNft(Player player, String achievementKey) {
    CompletableFuture<String> future = new CompletableFuture<>();

    // Get player's wallet address
    String walletAddress = plugin.getSolanaLoginIntegration().getWalletAddress(player);
    if (walletAddress == null || walletAddress.isEmpty()) {
        future.completeExceptionally(new IllegalStateException("Player does not have a registered wallet address"));
        return future;
    }

    // Get NFT metadata
    String nftName = plugin.getConfigManager().getNftName(achievementKey);
    String nftDescription = plugin.getConfigManager().getNftDescription(achievementKey);
    String nftImageUrl = plugin.getConfigManager().getNftImageUrl(achievementKey);

    // Run the minting process asynchronously
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            // Execute Node.js process
            // ...

            // Parse the output to get the transaction ID
            String transactionId = parseTransactionId(output.toString());

            // Complete the future with the transaction ID
            future.complete(transactionId);
        } catch (Exception e) {
            future.completeExceptionally(e);
        }
    });

    return future;
}
```

### Minecraft Integration Technologies

#### 1. Bukkit/Spigot API (v1.18.2)

The plugin uses the Bukkit/Spigot API for core Minecraft integration:

- **Event System**: Listens for player actions and game events
- **Command Registration**: Registers custom commands like `/mintnft` and `/nftinv`
- **Inventory Management**: Creates and manages custom inventories
- **Item Manipulation**: Creates and modifies Minecraft items

```java
// From NFTPlugin.java - Bukkit integration
@Override
public void onEnable() {
    // Register commands
    getCommand("nftinfo").setExecutor(new NFTInfoCommand(this));
    getCommand("nftlist").setExecutor(new NFTListCommand(this));
    getCommand("resetnft").setExecutor(new ResetNFTCommand(this));
    getCommand("mintnft").setExecutor(new MintNFTCommand(this));
    getCommand("nftbuff").setExecutor(new NFTBuffCommand(this));

    // Register event listeners
    Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
    Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
    Bukkit.getPluginManager().registerEvents(new CustomEnchantListener(this, customEnchantManager), this);
    Bukkit.getPluginManager().registerEvents(new BuffListener(this), this);
}
```

#### 2. PersistentDataContainer API

The plugin uses Bukkit's PersistentDataContainer API for storing NFT data directly in items:

- **Secure Storage**: Stores NFT data in a way that can't be modified by players
- **Type Safety**: Uses typed data storage for different properties
- **Namespace Isolation**: Prevents conflicts with other plugins
- **Persistence**: Data remains with items through server restarts

```java
// From ItemManager.java - PersistentDataContainer usage
public ItemStack createNftItem(Player player, String achievementKey, String transactionId) {
    // Create the base item
    ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
    ItemMeta meta = item.getItemMeta();

    // Set display properties
    meta.setDisplayName(ChatColor.GOLD + plugin.getConfigManager().getNftName(achievementKey));

    // Store NFT data in item
    PersistentDataContainer container = meta.getPersistentDataContainer();
    NamespacedKey nftKey = new NamespacedKey(plugin, "nft");
    NamespacedKey nftIdKey = new NamespacedKey(plugin, "nft_id");
    NamespacedKey achievementKeyNS = new NamespacedKey(plugin, "achievement_key");

    container.set(nftKey, PersistentDataType.BYTE, (byte) 1);
    container.set(nftIdKey, PersistentDataType.STRING, transactionId);
    container.set(achievementKeyNS, PersistentDataType.STRING, achievementKey);

    item.setItemMeta(meta);
    return item;
}
```

#### 3. Custom Inventory System

The plugin implements a sophisticated custom inventory system for NFT storage:

- **Multi-Page Design**: Supports unlimited pages of NFT items
- **Page-Specific Indexing**: Maps inventory slots to storage indices
- **Navigation Controls**: Intuitive buttons for moving between pages
- **Persistent Storage**: Saves inventory state when closed or navigating

```java
// From NFTInvCommand.java - Custom inventory implementation
private void openNFTInventory(Player player, int page) {
    // Create inventory with title showing the page number
    String title = INVENTORY_TITLE_PREFIX + " - Page " + page;
    Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

    // Load NFTs from storage
    Map<Integer, ItemStack> allItems = storage.loadInventory(player);

    // Count items by page
    Map<Integer, Integer> itemsPerPage = new HashMap<>();
    for (int itemIndex : allItems.keySet()) {
        int pageOfItem = itemIndex / ITEMS_PER_PAGE + 1;
        itemsPerPage.put(pageOfItem, itemsPerPage.getOrDefault(pageOfItem, 0) + 1);
    }

    // Find items for this specific page
    for (Map.Entry<Integer, ItemStack> entry : allItems.entrySet()) {
        int itemIndex = entry.getKey();
        int itemPage = itemIndex / ITEMS_PER_PAGE + 1;

        // Only add items from the current page
        if (itemPage == page) {
            // Calculate the slot within this page
            int pageSlot = itemIndex % ITEMS_PER_PAGE;

            // Add item to inventory
            inventory.setItem(pageSlot, entry.getValue());
        }
    }

    // Add navigation items
    addNavigationItems(inventory, page, allItems.size());

    // Open inventory for player
    player.openInventory(inventory);
}
```

## Gameplay Enhancement Technologies

### Custom Enchantment System

The plugin implements a sophisticated custom enchantment system that extends Minecraft's native functionality:

#### 1. Event-Based Architecture

The enchantment system uses Bukkit's event system to intercept and modify game events:

- **BlockBreakEvent Handling**: Intercepts block breaking to apply special effects
- **PlayerInteractEvent Handling**: Detects right-click actions for activation
- **EntityDamageByEntityEvent Handling**: Modifies combat mechanics for combat enchantments
- **Event Priority Management**: Uses appropriate event priorities to ensure compatibility

```java
// From CustomEnchantListener.java - Event-based enchantment system
@EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
public void onBlockBreak(BlockBreakEvent event) {
    Player player = event.getPlayer();
    ItemStack item = player.getInventory().getItemInMainHand();

    // Skip if not an NFT item
    if (!plugin.getItemManager().isNftItem(item)) {
        return;
    }

    // Check for explosion enchantment
    int explosionLevel = enchantManager.getEnchantmentLevel(item, "explosion");
    if (explosionLevel > 0) {
        // Apply explosion mining effect
        enchantManager.applyExplosionEffect(player, event.getBlock(), explosionLevel);
    }

    // Check for laser enchantment
    int laserLevel = enchantManager.getEnchantmentLevel(item, "laser");
    if (laserLevel > 0) {
        // Apply laser mining effect
        enchantManager.applyLaserEffect(player, event.getBlock(), laserLevel);
    }
}
```

#### 2. Vector Mathematics

The plugin uses sophisticated vector mathematics for directional effects like laser mining:

- **Direction Vectors**: Calculates player's facing direction for targeted effects
- **Ray Tracing**: Implements ray casting for laser mining
- **Coordinate Transformations**: Handles different mining orientations
- **Distance Calculations**: Limits effects based on distance from origin

```java
// From CustomEnchantManager.java - Vector mathematics for laser mining
public void applyLaserEffect(Player player, Block startBlock, int level) {
    // Get player's looking direction
    Vector direction = player.getLocation().getDirection().normalize();

    // Calculate maximum depth based on level (2-6 blocks)
    int maxDepth = Math.min(1 + level, 6);

    // Start from the broken block
    Block currentBlock = startBlock;

    // Mine blocks in a line
    for (int i = 0; i < maxDepth; i++) {
        // Skip the first block (already broken by the event)
        if (i == 0) continue;

        // Get the next block in the line
        Block nextBlock = getNextBlock(currentBlock, direction);

        // Break the block if possible
        if (canBreakBlock(player, nextBlock)) {
            nextBlock.breakNaturally(player.getInventory().getItemInMainHand());

            // Show laser particle effect
            player.getWorld().spawnParticle(
                Particle.REDSTONE,
                nextBlock.getLocation().add(0.5, 0.5, 0.5),
                10, 0.2, 0.2, 0.2, 0.01,
                new Particle.DustOptions(Color.RED, 1.0f)
            );
        } else {
            // Stop if we hit an unbreakable block
            break;
        }

        // Move to the next block
        currentBlock = nextBlock;
    }
}

private Block getNextBlock(Block current, Vector direction) {
    // Determine which axis has the largest component
    double absX = Math.abs(direction.getX());
    double absY = Math.abs(direction.getY());
    double absZ = Math.abs(direction.getZ());

    if (absX >= absY && absX >= absZ) {
        // X-axis is dominant
        return current.getRelative(direction.getX() > 0 ? BlockFace.EAST : BlockFace.WEST);
    } else if (absY >= absX && absY >= absZ) {
        // Y-axis is dominant
        return current.getRelative(direction.getY() > 0 ? BlockFace.UP : BlockFace.DOWN);
    } else {
        // Z-axis is dominant
        return current.getRelative(direction.getZ() > 0 ? BlockFace.SOUTH : BlockFace.NORTH);
    }
}
```

### Buff System

The plugin implements a comprehensive buff system that enhances player attributes:

#### 1. Attribute Modification API

The buff system uses Bukkit's Attribute API for applying percentage-based buffs:

- **Dynamic Attribute Modification**: Modifies player attributes in real-time
- **Stacking Mechanism**: Properly handles multiple buffs of the same type
- **Operation Types**: Uses appropriate operation types for percentage-based effects
- **Attribute Targeting**: Modifies specific attributes for different buff types

```java
// From BuffManager.java - Attribute modification system
public void applyBuffsToPlayer(Player player, Map<String, Double> buffs) {
    // Apply mining speed buff
    if (buffs.containsKey("mining_speed")) {
        double value = buffs.get("mining_speed");
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);

        if (attribute != null) {
            // Remove existing modifier if present
            for (AttributeModifier modifier : attribute.getModifiers()) {
                if (modifier.getName().equals("nft_mining_speed")) {
                    attribute.removeModifier(modifier);
                }
            }

            // Add new modifier
            AttributeModifier modifier = new AttributeModifier(
                "nft_mining_speed",
                value / 100.0, // Convert percentage to multiplier
                AttributeModifier.Operation.MULTIPLY_SCALAR_1
            );
            attribute.addModifier(modifier);
        }
    }

    // Apply damage buff
    if (buffs.containsKey("damage")) {
        double value = buffs.get("damage");
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);

        if (attribute != null) {
            // Similar implementation as above
            // ...
        }
    }

    // Apply other buffs...
}
```

#### 2. Event-Based Buff Updates

The buff system uses events to keep buffs updated as players change equipment:

- **InventoryClickEvent**: Updates buffs when players move items in inventory
- **PlayerItemHeldEvent**: Updates buffs when players switch items in hotbar
- **PlayerJoinEvent**: Applies buffs when players join the server
- **PlayerQuitEvent**: Cleans up buff data when players leave

```java
// From BuffListener.java - Event-based buff updates
@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onInventoryClick(InventoryClickEvent event) {
    if (!(event.getWhoClicked() instanceof Player)) {
        return;
    }

    Player player = (Player) event.getWhoClicked();

    // Schedule a delayed task to update buffs after inventory changes are complete
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        plugin.getBuffManager().updatePlayerBuffs(player);
    }, 1L);
}

@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
public void onPlayerItemHeld(PlayerItemHeldEvent event) {
    Player player = event.getPlayer();

    // Update buffs when player switches items
    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        plugin.getBuffManager().updatePlayerBuffs(player);
    }, 1L);
}
```

### Storage Technologies

The plugin uses multiple storage technologies to ensure data persistence:

#### 1. YAML Configuration

The plugin uses YAML for configuration and metadata storage:

- **ConfigurationSection API**: Hierarchical data storage for complex structures
- **File-Based Storage**: Simple, human-readable format for easy editing
- **Default Configuration**: Provides sensible defaults with comments
- **Reload Support**: Allows configuration changes without server restarts

```java
// From ConfigManager.java - YAML configuration handling
public void loadConfig() {
    // Save default config if it doesn't exist
    plugin.saveDefaultConfig();

    // Load the config
    plugin.reloadConfig();
    FileConfiguration config = plugin.getConfig();

    // Load NFT metadata
    if (config.contains("nfts")) {
        ConfigurationSection nftsSection = config.getConfigurationSection("nfts");
        for (String key : nftsSection.getKeys(false)) {
            ConfigurationSection nftSection = nftsSection.getConfigurationSection(key);

            String name = nftSection.getString("name", "Unknown NFT");
            String description = nftSection.getString("description", "");
            String imageUrl = nftSection.getString("image_url", "");

            nftNames.put(key, name);
            nftDescriptions.put(key, description);
            nftImageUrls.put(key, imageUrl);

            // Load enchantments
            if (nftSection.contains("enchantments")) {
                ConfigurationSection enchantmentsSection = nftSection.getConfigurationSection("enchantments");
                for (String enchantKey : enchantmentsSection.getKeys(false)) {
                    int level = enchantmentsSection.getInt(enchantKey);
                    nftEnchantments.put(key + "." + enchantKey, level);
                }
            }

            // Load buffs
            if (nftSection.contains("buffs")) {
                ConfigurationSection buffsSection = nftSection.getConfigurationSection("buffs");
                for (String buffKey : buffsSection.getKeys(false)) {
                    double value = buffsSection.getDouble(buffKey);
                    nftBuffs.put(key + "." + buffKey, value);
                }
            }
        }
    }
}
```

#### 2. Custom File-Based Storage

The plugin implements a custom file-based storage system for NFT inventories:

- **JSON Serialization**: Uses Gson for efficient serialization/deserialization
- **UUID-Based Files**: Stores player data in separate files by UUID
- **Asynchronous I/O**: Performs file operations off the main thread
- **Caching Layer**: Maintains in-memory cache for performance

```java
// From SimpleNFTInventory.java - Custom file-based storage
public void saveInventory(Player player) {
    UUID uuid = player.getUniqueId();
    Map<Integer, ItemStack> inventory = playerInventories.getOrDefault(uuid, new HashMap<>());

    // Convert ItemStacks to Base64 strings for storage
    Map<Integer, String> serializedInventory = new HashMap<>();
    for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
        serializedInventory.put(entry.getKey(), itemStackToBase64(entry.getValue()));
    }

    // Save asynchronously
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        File playerFile = new File(dataFolder, uuid.toString() + ".json");
        try (FileWriter writer = new FileWriter(playerFile)) {
            gson.toJson(serializedInventory, writer);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save inventory for player " + player.getName() + ": " + e.getMessage());
        }
    });
}

public Map<Integer, ItemStack> loadInventory(Player player) {
    UUID uuid = player.getUniqueId();

    // Return from cache if available
    if (playerInventories.containsKey(uuid)) {
        return new HashMap<>(playerInventories.get(uuid));
    }

    // Load from file
    Map<Integer, ItemStack> inventory = new HashMap<>();
    File playerFile = new File(dataFolder, uuid.toString() + ".json");

    if (playerFile.exists()) {
        try (FileReader reader = new FileReader(playerFile)) {
            Type type = new TypeToken<Map<Integer, String>>(){}.getType();
            Map<Integer, String> serializedInventory = gson.fromJson(reader, type);

            for (Map.Entry<Integer, String> entry : serializedInventory.entrySet()) {
                inventory.put(entry.getKey(), itemStackFromBase64(entry.getValue()));
            }
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to load inventory for player " + player.getName() + ": " + e.getMessage());
        }
    }

    // Cache the loaded inventory
    playerInventories.put(uuid, inventory);
    return new HashMap<>(inventory);
}
```

## Development and Build Technologies

### Maven Build System

The plugin uses Maven for dependency management and building:

#### 1. Dependency Management

Maven handles all external dependencies with specific versions:

- **Spigot API**: Core Minecraft server API (v1.18.2-R0.1-SNAPSHOT)
- **HikariCP**: High-performance JDBC connection pool (v5.0.1)
- **Gson**: JSON serialization/deserialization library (v2.9.0)
- **JUnit**: Testing framework for unit tests (v5.8.2)
- **Mockito**: Mocking framework for unit tests (v4.5.1)

```xml
<!-- From pom.xml - Maven dependency management -->
<dependencies>
    <dependency>
        <groupId>org.spigotmc</groupId>
        <artifactId>spigot-api</artifactId>
        <version>1.18.2-R0.1-SNAPSHOT</version>
        <scope>provided</scope>
    </dependency>
    <dependency>
        <groupId>com.zaxxer</groupId>
        <artifactId>HikariCP</artifactId>
        <version>5.0.1</version>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
        <version>2.9.0</version>
    </dependency>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter-api</artifactId>
        <version>5.8.2</version>
        <scope>test</scope>
    </dependency>
    <dependency>
        <groupId>org.mockito</groupId>
        <artifactId>mockito-core</artifactId>
        <version>4.5.1</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

#### 2. Shade Plugin

The plugin uses Maven's Shade plugin to package dependencies into the final JAR:

- **Dependency Inclusion**: Bundles all required libraries
- **Relocation**: Prevents conflicts with other plugins using the same libraries
- **Minimization**: Excludes unnecessary classes to reduce file size
- **Manifest Generation**: Creates proper manifest with plugin information

```xml
<!-- From pom.xml - Maven Shade plugin configuration -->
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-shade-plugin</artifactId>
    <version>3.2.4</version>
    <executions>
        <execution>
            <phase>package</phase>
            <goals>
                <goal>shade</goal>
            </goals>
            <configuration>
                <createDependencyReducedPom>false</createDependencyReducedPom>
                <relocations>
                    <relocation>
                        <pattern>com.zaxxer.hikari</pattern>
                        <shadedPattern>com.minecraft.nftplugin.libs.hikari</shadedPattern>
                    </relocation>
                    <relocation>
                        <pattern>com.google.gson</pattern>
                        <shadedPattern>com.minecraft.nftplugin.libs.gson</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
        </execution>
    </executions>
</plugin>
```

### NPM Package Management

The Node.js backend uses NPM for dependency management:

#### 1. Package.json

The package.json file defines all Node.js dependencies:

- **@solana/web3.js**: Core Solana blockchain library (v1.77.3)
- **@metaplex-foundation/js**: Metaplex NFT standards library (v0.19.4)
- **bs58**: Base58 encoding/decoding (v5.0.0)
- **commander**: Command-line argument parsing (v11.0.0)
- **dotenv**: Environment variable management (v16.3.1)

```json
// From package.json - NPM dependency management
{
  "name": "solana-nft-minter",
  "version": "1.0.0",
  "description": "Backend service for minting Solana NFTs",
  "main": "mint-nft.js",
  "dependencies": {
    "@metaplex-foundation/js": "^0.19.4",
    "@solana/web3.js": "^1.77.3",
    "bs58": "^5.0.0",
    "commander": "^11.0.0",
    "dotenv": "^16.3.1"
  },
  "scripts": {
    "mint": "node mint-nft.js"
  }
}
```

## Technical Advantages

### 1. Hybrid On-Chain/Off-Chain Architecture

The plugin's hybrid architecture provides several key advantages:

- **Cost Efficiency**: Only essential data (ownership, token ID) stored on-chain, reducing transaction costs
- **Performance**: Game mechanics run locally without blockchain latency
- **Flexibility**: Game-specific properties can be updated without blockchain transactions
- **Security**: Ownership and authenticity verified on-chain when needed

### 2. Real Blockchain Integration

Unlike simulated NFT systems, this plugin uses actual blockchain technology:

- **True Ownership**: NFTs are real Solana tokens following the Metaplex standard
- **External Compatibility**: NFTs can be viewed in Solana wallets and marketplaces
- **Permanence**: NFT ownership persists even if the Minecraft server shuts down
- **Transferability**: NFTs can be traded outside the game on Solana marketplaces

### 3. Asynchronous Processing

The plugin's asynchronous design ensures smooth gameplay:

- **Non-Blocking Operations**: Blockchain operations run in background threads
- **Responsive UI**: Player interface remains responsive during minting
- **Parallel Processing**: Multiple operations can run simultaneously
- **Failure Isolation**: Errors in blockchain operations don't crash the server

### 4. Robust Error Handling

The plugin implements sophisticated error handling:

- **Retry Mechanisms**: Automatic retries with exponential backoff for failed operations
- **Graceful Degradation**: Falls back to local data when blockchain is unavailable
- **Detailed Logging**: Comprehensive logging for troubleshooting
- **User Feedback**: Clear error messages for players when operations fail

## Conclusion

The NFT-Plugin represents a sophisticated integration of blockchain technology with Minecraft gameplay. By leveraging specific technologies like Solana's Web3.js, Metaplex SDK, and Bukkit's PersistentDataContainer API, the plugin creates a seamless bridge between the blockchain and gaming worlds.

The technical implementation follows best practices in both blockchain and game development:

1. **Separation of Concerns**: Clear division between blockchain operations (Node.js) and game mechanics (Java)
2. **Asynchronous Processing**: Non-blocking design for responsive gameplay
3. **Robust Error Handling**: Graceful handling of network issues and transaction failures
4. **Efficient Storage**: Multi-layered approach with in-memory caching and persistent storage
5. **Modular Architecture**: Well-defined components with clear interfaces

This technology stack demonstrates how blockchain can be meaningfully integrated into gaming experiences, creating digital assets with both collectible value and practical utility. The plugin's implementation balances the authenticity of real blockchain NFTs with the performance requirements of an interactive game environment.

By using industry-standard libraries and following best practices in both the blockchain and gaming domains, the NFT-Plugin provides a solid foundation that can be extended with new features while maintaining reliability and performance.
