package com.minecraft.nftplugin.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Service class for handling NFT minting operations
 */
public class MintNFTService {

    private final NFTPlugin plugin;

    // Cache for metadata JSON objects to avoid repeated file reads
    private final Map<String, JsonObject> metadataCache = new ConcurrentHashMap<>();

    // Cache for reward objects to avoid repeated parsing
    private final Map<String, JsonObject> rewardCache = new ConcurrentHashMap<>();

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public MintNFTService(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Create an NFT item from metadata file (optimized with caching)
     * @param transactionId The transaction ID
     * @param achievementKey The achievement key
     * @return The NFT item
     */
    public ItemStack createNftItemFromMetadata(String transactionId, String achievementKey) {
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
    public void addNftToPlayerInventory(Player player, ItemStack nftItem) {
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
    public ItemStack createItemFromReward(JsonObject reward, String transactionId, String achievementKey) {
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

                // Make item glow
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

                // Set unbreakable
                meta.setUnbreakable(true);

                // Set the meta
                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating item from reward: " + e.getMessage());
            // Fallback to a default item
            ItemStack fallbackItem = new ItemStack(Material.PAPER);
            ItemMeta meta = fallbackItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.GOLD + "NFT " + ChatColor.AQUA + achievementKey);
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "A special NFT item");
                lore.add(ChatColor.GRAY + "Achievement: " + ChatColor.WHITE + achievementKey);
                lore.add("");
                lore.add(ChatColor.GRAY + "Transaction: " + ChatColor.WHITE + transactionId);
                meta.setLore(lore);
                fallbackItem.setItemMeta(meta);
            }
            return fallbackItem;
        }
    }
    /**
     * Mint an NFT directly to a player
     * @param player The player
     * @param nftId The NFT ID to mint
     * @param achievementKey The achievement key (optional)
     */
    public void mintNFTToPlayer(Player player, String nftId, String achievementKey) {
        // Tạo NFT item từ metadata
        ItemStack nftItem = createNftItemFromMetadata(nftId, achievementKey);

        if (nftItem != null) {
            // Thêm NFT vào inventory của người chơi
            addNftToPlayerInventory(player, nftItem);

            // Thông báo cho người chơi
            player.sendMessage(ChatColor.GREEN + "Bạn đã nhận được một NFT mới!");
        } else {
            plugin.log(Level.WARNING, "Failed to create NFT item for ID: " + nftId);
        }
    }
}