package com.minecraft.nftplugin.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Command to mint an NFT directly to a specified player
 * Only admins can use this command
 */
public class MintNFTCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    // Cache for metadata JSON objects to avoid repeated file reads
    private final Map<String, JsonObject> metadataCache = new ConcurrentHashMap<>();

    // Cache for reward objects to avoid repeated parsing
    private final Map<String, JsonObject> rewardCache = new ConcurrentHashMap<>();

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public MintNFTCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.hasPermission("nftplugin.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check if enough arguments
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Usage: /mintnft <username> <metadata_key>");
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                    "Example: /mintnft Steve diamond_sword");
            return true;
        }

        final String targetUsername = args[0];
        final String metadataKey = args[1];

        // Find target player
        Player targetPlayer = Bukkit.getPlayer(targetUsername);
        if (targetPlayer == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Player " + targetUsername + " is not online.");
            return true;
        }

        // Check if target player has a wallet
        Optional<String> walletAddressOpt = plugin.getSolanaLoginIntegration().getWalletAddress(targetPlayer.getUniqueId());
        if (!walletAddressOpt.isPresent()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Player " + targetPlayer.getName() + " doesn't have a registered Solana wallet.");
            return true;
        }

        // Check if metadata file exists
        File metadataFile = new File(plugin.getDataFolder(), "metadata/" + metadataKey + ".json");
        if (!metadataFile.exists()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Metadata file not found: " + metadataKey + ".json");
            return true;
        }

        // Only inform admin, not the target player
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                "Minting NFT for " + targetPlayer.getName() + " using metadata: " + metadataKey + "...");

        // Check if we have a metadata URI for this NFT (only log to console, not to player)
        String metadataUri = plugin.getConfigManager().getNftMetadataUri(metadataKey);
        if (metadataUri != null && !metadataUri.isEmpty()) {
            plugin.getLogger().info("Using metadata URI for " + metadataKey + ": " + metadataUri);
        } else {
            plugin.getLogger().info("No metadata URI found for " + metadataKey + ", using local metadata file");
        }

        // Verify NFT metadata exists (but don't store the values to avoid unnecessary operations)
        plugin.getConfigManager().getNftName(metadataKey);
        plugin.getConfigManager().getNftDescription(metadataKey);
        plugin.getConfigManager().getNftImageUrl(metadataKey);

        // Mint NFT
        CompletableFuture<String> future = plugin.getSolanaService().mintNft(targetPlayer, metadataKey);

        // Handle result
        future.thenAccept(transactionId -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Send success message only to admin
                String successMessage = plugin.getConfigManager().getMessage("nft_minted")
                        .replace("%tx_id%", transactionId);
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN +
                        "Successfully minted NFT for " + targetPlayer.getName() + "!");
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + successMessage);

                // Create NFT item using the same method as achievements
                ItemStack nftItem = createNftItemFromMetadata(transactionId, metadataKey);

                // Add NFT to player's NFT inventory
                addNftToPlayerInventory(targetPlayer, nftItem);

                // Send success messages
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.GREEN + "You received an NFT item for '" + metadataKey + "'! Check your /nftinv");
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.GREEN + "NFT item added to " + targetPlayer.getName() + "'s NFT inventory.");

                // Log the mint
                plugin.getLogger().info("Admin " + player.getName() + " minted NFT for player " + targetPlayer.getName());
                plugin.getLogger().info("Transaction ID: " + transactionId);
                plugin.getLogger().info("Metadata key: " + metadataKey);
            });
        }).exceptionally(ex -> {
            // Handle exception
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.RED + "Error minting NFT: " + ex.getMessage());
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.RED + "Error minting your NFT. Please contact an administrator.");
                plugin.getLogger().severe("Error minting NFT: " + ex.getMessage());
                ex.printStackTrace();
            });
            return null;
        });

        return true;
    }

    /**
     * Create an NFT item from metadata file (optimized with caching)
     * @param transactionId The transaction ID
     * @param achievementKey The achievement key
     * @return The NFT item
     */
    private ItemStack createNftItemFromMetadata(String transactionId, String achievementKey) {
        try {
            // Check if we already have the reward in cache
            JsonObject reward = rewardCache.get(achievementKey);

            if (reward == null) {
                // Get metadata file path
                String metadataPath = "metadata/" + achievementKey + ".json";
                File metadataFile = new File(plugin.getDataFolder(), metadataPath);

                if (!metadataFile.exists()) {
                    plugin.getLogger().warning("Metadata file not found: " + metadataPath);
                    // Fallback to ItemManager
                    return plugin.getItemManager().createNftItem(transactionId, achievementKey);
                }

                // Check if we have the metadata in cache
                JsonObject metadata = metadataCache.get(achievementKey);

                if (metadata == null) {
                    // Parse metadata file
                    Gson gson = new Gson();
                    try (Reader reader = new FileReader(metadataFile)) {
                        metadata = gson.fromJson(reader, JsonObject.class);
                        // Store in cache for future use
                        metadataCache.put(achievementKey, metadata);
                    }
                }

                // Extract reward section
                // First check for reward at top level
                if (metadata.has("reward")) {
                    reward = metadata.getAsJsonObject("reward");
                }
                // Then check in quest section
                else if (metadata.has("quest") && metadata.getAsJsonObject("quest").has("reward")) {
                    reward = metadata.getAsJsonObject("quest").getAsJsonObject("reward");
                }

                if (reward == null) {
                    plugin.getLogger().warning("Metadata file does not have reward section: " + metadataPath);
                    // Fallback to ItemManager
                    return plugin.getItemManager().createNftItem(transactionId, achievementKey);
                }

                // Store reward in cache for future use
                rewardCache.put(achievementKey, reward);
            }

            // Create item
            return createItemFromReward(reward, transactionId, achievementKey);
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating NFT item from metadata: " + e.getMessage());
            // Fallback to ItemManager without stack trace for speed
            return plugin.getItemManager().createNftItem(transactionId, achievementKey);
        }
    }

    /**
     * Add an NFT to a player's NFT inventory (optimized)
     * @param player The player
     * @param nftItem The NFT item
     */
    private void addNftToPlayerInventory(Player player, ItemStack nftItem) {
        // Find the first available slot in the NFT inventory
        Map<Integer, ItemStack> nftInventory = plugin.getSimpleNFTInventory().loadInventory(player);

        // Find the first empty slot (optimized)
        final int slot;
        // Check the first 100 slots to find an empty one quickly
        int emptySlot = 0;
        for (int i = 0; i < 100; i++) {
            if (!nftInventory.containsKey(i)) {
                emptySlot = i;
                break;
            }
        }
        slot = emptySlot; // Make it effectively final

        // Add the NFT to the inventory
        plugin.getSimpleNFTInventory().setItem(player, slot, nftItem);

        // Save inventory asynchronously to avoid blocking the main thread
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.getSimpleNFTInventory().saveInventory(player);
            plugin.getLogger().info("Added NFT to " + player.getName() + "'s NFT inventory at slot " + slot);
        });
    }

    /**
     * Create an item from reward JSON (optimized)
     * @param reward The reward JSON object
     * @param transactionId The transaction ID
     * @param achievementKey The achievement key
     * @return The item
     */
    private ItemStack createItemFromReward(JsonObject reward, String transactionId, String achievementKey) {
        try {
            // Get material (with fallback)
            Material material;
            try {
                String materialName = reward.has("item") ? reward.get("item").getAsString() : "DIAMOND_PICKAXE";
                material = Material.valueOf(materialName);
            } catch (Exception e) {
                // Fallback to a safe material
                material = Material.DIAMOND_PICKAXE;
            }

            // Create item
            ItemStack item = new ItemStack(material);
            ItemMeta meta = item.getItemMeta();

            if (meta != null) {
                // Set name (with fallback)
                if (reward.has("name")) {
                    try {
                        String name = reward.get("name").getAsString();
                        meta.setDisplayName(name); // Already includes color codes
                    } catch (Exception e) {
                        meta.setDisplayName(ChatColor.GOLD + "NFT " + ChatColor.AQUA + achievementKey);
                    }
                } else {
                    meta.setDisplayName(ChatColor.GOLD + "NFT " + ChatColor.AQUA + achievementKey);
                }

                // Set lore (optimized)
                List<String> lore = new ArrayList<>();

                // Add basic lore if not present in reward
                if (!reward.has("lore") || !reward.get("lore").isJsonArray()) {
                    lore.add(ChatColor.GRAY + "A special NFT item");
                    lore.add(ChatColor.GRAY + "Achievement: " + ChatColor.WHITE + achievementKey);
                } else {
                    // Add lore from reward
                    try {
                        JsonArray loreArray = reward.getAsJsonArray("lore");
                        for (JsonElement element : loreArray) {
                            lore.add(element.getAsString());
                        }
                    } catch (Exception e) {
                        lore.add(ChatColor.GRAY + "A special NFT item");
                        lore.add(ChatColor.GRAY + "Achievement: " + ChatColor.WHITE + achievementKey);
                    }
                }

                // Add transaction ID to lore
                lore.add("");
                lore.add(ChatColor.GRAY + "Transaction: " + ChatColor.WHITE + transactionId);

                // Set the lore
                meta.setLore(lore);

                // Set enchantments (optimized)
                if (reward.has("enchantments") && reward.get("enchantments").isJsonArray()) {
                    try {
                        JsonArray enchantments = reward.getAsJsonArray("enchantments");
                        for (JsonElement element : enchantments) {
                            String enchantmentStr = element.getAsString();
                            String[] parts = enchantmentStr.split(":");

                            if (parts.length == 2) {
                                try {
                                    String enchantName = parts[0];
                                    int level = Integer.parseInt(parts[1]);

                                    // Try to get enchantment by key directly
                                    Enchantment enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                                    if (enchantment != null) {
                                        meta.addEnchant(enchantment, level, true);
                                    }
                                } catch (Exception ignored) {
                                    // Skip this enchantment if it fails
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        // If enchantments fail, add some default ones
                        meta.addEnchant(Enchantment.DURABILITY, 10, true);
                        meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
                    }
                } else {
                    // Add default enchantments
                    meta.addEnchant(Enchantment.DURABILITY, 10, true);
                    meta.addEnchant(Enchantment.DIG_SPEED, 5, true);
                }

                // Set unbreakable
                meta.setUnbreakable(true);

                // Add item flags
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

                // Set custom model data if present in the reward or config
                if (reward.has("custom_model_data")) {
                    try {
                        int customModelData = reward.get("custom_model_data").getAsInt();
                        meta.setCustomModelData(customModelData);
                        plugin.getLogger().info("Applied custom model data " + customModelData + " to NFT " + achievementKey + " from metadata");
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to apply custom model data from metadata for NFT " + achievementKey + ": " + e.getMessage());
                        plugin.getLogger().warning("Reward JSON: " + reward.toString());
                    }
                } else {
                    // Try to get custom model data from config
                    int configCustomModelData = plugin.getConfigManager().getNftItemCustomModelData(achievementKey);
                    if (configCustomModelData != -1) {
                        meta.setCustomModelData(configCustomModelData);
                        plugin.getLogger().info("Applied custom model data " + configCustomModelData + " to NFT " + achievementKey + " from config");
                    } else {
                        // Use default custom model data
                        int defaultCustomModelData = plugin.getConfigManager().getNftItemCustomModelData();
                        meta.setCustomModelData(defaultCustomModelData);
                        plugin.getLogger().info("Applied default custom model data " + defaultCustomModelData + " to NFT " + achievementKey);
                    }
                }

                // Force custom model data for explosion_pickaxe_5
                if (achievementKey.equals("explosion_pickaxe_5")) {
                    meta.setCustomModelData(7405);
                    plugin.getLogger().info("Forced custom model data 7405 for explosion_pickaxe_5");
                }

                // Add NFT data
                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey nftKey = new NamespacedKey(plugin, "nft");
                NamespacedKey nftIdKey = new NamespacedKey(plugin, "nft_id");
                NamespacedKey achievementKeyNS = new NamespacedKey(plugin, "achievement_key");

                container.set(nftKey, PersistentDataType.BYTE, (byte) 1);
                container.set(nftIdKey, PersistentDataType.STRING, transactionId);
                container.set(achievementKeyNS, PersistentDataType.STRING, achievementKey);

                // Apply meta to item
                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            // If anything fails, create a simple fallback item
            ItemStack item = new ItemStack(Material.DIAMOND_PICKAXE);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "NFT " + ChatColor.AQUA + achievementKey);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "A special NFT item");
                lore.add(ChatColor.GRAY + "Transaction: " + ChatColor.WHITE + transactionId);
                meta.setLore(lore);
                meta.setUnbreakable(true);
                meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

                // Set custom model data from config or default
                int configCustomModelData = plugin.getConfigManager().getNftItemCustomModelData(achievementKey);
                if (configCustomModelData != -1) {
                    meta.setCustomModelData(configCustomModelData);
                    plugin.getLogger().info("Applied custom model data " + configCustomModelData + " to fallback NFT " + achievementKey + " from config");
                } else {
                    // Use default custom model data
                    int defaultCustomModelData = plugin.getConfigManager().getNftItemCustomModelData();
                    meta.setCustomModelData(defaultCustomModelData);
                    plugin.getLogger().info("Applied default custom model data " + defaultCustomModelData + " to fallback NFT " + achievementKey);
                }

                // Add NFT data
                PersistentDataContainer container = meta.getPersistentDataContainer();
                NamespacedKey nftKey = new NamespacedKey(plugin, "nft");
                NamespacedKey nftIdKey = new NamespacedKey(plugin, "nft_id");
                NamespacedKey achievementKeyNS = new NamespacedKey(plugin, "achievement_key");

                container.set(nftKey, PersistentDataType.BYTE, (byte) 1);
                container.set(nftIdKey, PersistentDataType.STRING, transactionId);
                container.set(achievementKeyNS, PersistentDataType.STRING, achievementKey);

                item.setItemMeta(meta);
            }
            return item;
        }
    }
}
