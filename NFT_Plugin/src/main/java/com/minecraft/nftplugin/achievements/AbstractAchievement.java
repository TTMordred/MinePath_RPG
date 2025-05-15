package com.minecraft.nftplugin.achievements;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
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
import java.util.List;
import java.util.UUID;

/**
 * Abstract base class for all achievements
 */
public abstract class AbstractAchievement implements Achievement {

    protected final NFTPlugin plugin;
    protected final String key;
    protected final String name;
    protected final String description;
    protected final int requiredProgress;
    protected final String metadataFileName;
    protected final String imageUrl;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     * @param key The achievement key
     * @param name The achievement name
     * @param description The achievement description
     * @param requiredProgress The required progress
     * @param metadataFileName The metadata file name
     * @param imageUrl The image URL
     */
    public AbstractAchievement(NFTPlugin plugin, String key, String name, String description,
                              int requiredProgress, String metadataFileName, String imageUrl) {
        this.plugin = plugin;
        this.key = key;
        this.name = name;
        this.description = description;
        this.requiredProgress = requiredProgress;
        this.metadataFileName = metadataFileName;
        this.imageUrl = imageUrl;
    }

    @Override
    public String getKey() {
        return key;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public int getRequiredProgress() {
        return requiredProgress;
    }

    @Override
    public int getCurrentProgress(Player player) {
        return plugin.getDatabaseManager().getAchievementProgress(player.getUniqueId(), key);
    }

    @Override
    public boolean updateProgress(Player player, int progress) {
        UUID playerUuid = player.getUniqueId();
        int currentProgress = getCurrentProgress(player);

        // Don't update if already completed
        if (currentProgress >= requiredProgress) {
            return false;
        }

        // Update progress in database
        boolean updated = plugin.getDatabaseManager().updateAchievementProgress(playerUuid, key, progress);

        // Check if achievement is completed
        if (updated && progress >= requiredProgress) {
            onAchievementCompleted(player);
        }

        return updated;
    }

    @Override
    public boolean isCompleted(Player player) {
        return getCurrentProgress(player) >= requiredProgress;
    }

    @Override
    public String getMetadataFilePath() {
        // Check if metadataFileName already includes the "metadata/" prefix
        if (metadataFileName.startsWith("metadata/")) {
            return metadataFileName;
        } else {
            return "metadata/" + metadataFileName;
        }
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }

    /**
     * Called when a player completes the achievement
     * @param player The player
     */
    protected void onAchievementCompleted(Player player) {
        // Notify the player
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                plugin.getConfigManager().getMessage("achievement_complete").replace("%achievement%", name));

        // Mint NFT
        plugin.getSolanaService().mintNft(player, key)
                .thenAccept(transactionId -> {
                    // Send mint success message
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            plugin.getConfigManager().getMessage("nft_minted")
                                    .replace("%tx_id%", transactionId));

                    // Create and give reward item to player
                    createAndGiveRewardItem(player, transactionId);
                })
                .exceptionally(e -> {
                    plugin.getLogger().severe("Failed to mint NFT: " + e.getMessage());
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            plugin.getConfigManager().getMessage("nft_mint_failed"));
                    return null;
                });
    }

    /**
     * Create and give reward item to player
     * @param player The player
     * @param transactionId The transaction ID
     */
    private void createAndGiveRewardItem(Player player, String transactionId) {
        plugin.getLogger().info("Creating and giving reward item to player " + player.getName() + " for achievement " + key + " with transaction ID " + transactionId);

        // Run on main thread since inventory operations must be sync
        plugin.getServer().getScheduler().runTask(plugin, () -> {
            try {
                // Read metadata file to get reward information
                File metadataFile = new File(plugin.getDataFolder(), getMetadataFilePath());
                plugin.getLogger().info("Looking for metadata file: " + metadataFile.getAbsolutePath());

                if (!metadataFile.exists()) {
                    plugin.getLogger().warning("Metadata file not found: " + getMetadataFilePath());
                    // Use default item creation
                    plugin.getLogger().info("Using default item creation instead");
                    giveDefaultRewardItem(player, transactionId);
                    return;
                }

                plugin.getLogger().info("Metadata file found, parsing JSON");

                // Parse metadata file
                Gson gson = new Gson();
                Reader reader = new FileReader(metadataFile);
                JsonObject metadata = gson.fromJson(reader, JsonObject.class);
                reader.close();

                // Check if metadata has quest and reward sections
                if (!metadata.has("quest") || !metadata.getAsJsonObject("quest").has("reward")) {
                    plugin.getLogger().warning("Metadata file does not have reward section: " + getMetadataFilePath());
                    // Use default item creation
                    plugin.getLogger().info("Using default item creation instead");
                    giveDefaultRewardItem(player, transactionId);
                    return;
                }

                // Get reward section
                JsonObject reward = metadata.getAsJsonObject("quest").getAsJsonObject("reward");
                plugin.getLogger().info("Found reward section in metadata, creating item");

                // Create item
                ItemStack item = createRewardItemFromMetadata(reward, transactionId);
                plugin.getLogger().info("Created reward item: " + item.getType() +
                        " with name: " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                        item.getItemMeta().getDisplayName() : "<no name>"));

                // Give item to player
                plugin.getLogger().info("Adding item to player inventory");
                player.getInventory().addItem(item);

                // Force update inventory to make sure client sees the new item
                player.updateInventory();

                // Send message
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        plugin.getConfigManager().getMessage("item_preserved")
                                .replace("%item_name%", item.getItemMeta().getDisplayName()));

                // Play sound
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

                plugin.getLogger().info("Successfully gave reward item to player " + player.getName());

            } catch (Exception e) {
                plugin.getLogger().severe("Failed to create reward item: " + e.getMessage());
                e.printStackTrace();
                // Fallback to default item
                plugin.getLogger().info("Using default item creation due to error");
                giveDefaultRewardItem(player, transactionId);
            }
        });
    }

    /**
     * Create reward item from metadata
     * @param reward The reward JSON object
     * @param transactionId The transaction ID
     * @return The reward item
     */
    private ItemStack createRewardItemFromMetadata(JsonObject reward, String transactionId) {
        // Get item material
        String materialName = reward.has("item") ? reward.get("item").getAsString() : "PAPER";
        Material material = Material.valueOf(materialName);

        // Create item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set name
        if (reward.has("name")) {
            meta.setDisplayName(reward.get("name").getAsString());
        }

        // Set lore
        if (reward.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement loreElement : reward.getAsJsonArray("lore")) {
                lore.add(loreElement.getAsString());
            }
            // Add transaction ID to lore
            lore.add("§8NFT Transaction: §7" + transactionId);
            meta.setLore(lore);
        }

        // Set unbreakable
        if (reward.has("unbreakable") && reward.get("unbreakable").getAsBoolean()) {
            meta.setUnbreakable(true);
        }

        // Set custom model data
        if (reward.has("custom_model_data")) {
            int customModelData = reward.get("custom_model_data").getAsInt();
            meta.setCustomModelData(customModelData);
            plugin.getLogger().info("Applied custom model data " + customModelData + " to NFT " + key + " from metadata");
        } else {
            // Try to get custom model data from config
            int configCustomModelData = plugin.getConfigManager().getNftItemCustomModelData(key);
            if (configCustomModelData != -1) {
                meta.setCustomModelData(configCustomModelData);
                plugin.getLogger().info("Applied custom model data " + configCustomModelData + " to NFT " + key + " from config");
            }
        }

        // Special case for explosion_pickaxe_5
        if (key.equals("explosion_pickaxe_5")) {
            meta.setCustomModelData(7405);
            plugin.getLogger().info("Forced custom model data 7405 for explosion_pickaxe_5");
        }

        // Add item flags
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS,
                         ItemFlag.HIDE_ATTRIBUTES,
                         ItemFlag.HIDE_UNBREAKABLE);

        // Add NBT data to mark as NFT item
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey nftKey = new NamespacedKey(plugin, "nft");
        NamespacedKey nftIdKey = new NamespacedKey(plugin, "nft_id");
        NamespacedKey achievementKey = new NamespacedKey(plugin, "achievement_key");

        container.set(nftKey, PersistentDataType.BYTE, (byte) 1);
        container.set(nftIdKey, PersistentDataType.STRING, transactionId);
        container.set(achievementKey, PersistentDataType.STRING, key);

        // Apply meta to item
        item.setItemMeta(meta);

        // Add enchantments
        if (reward.has("enchantments")) {
            for (JsonElement enchantElement : reward.getAsJsonArray("enchantments")) {
                String enchantString = enchantElement.getAsString();
                String[] parts = enchantString.split(":");
                if (parts.length == 2) {
                    String enchantName = parts[0];
                    int level = Integer.parseInt(parts[1]);

                    // Use Enchantment.getByKey or try to find by name (for backward compatibility)
                    Enchantment enchantment = null;
                    try {
                        // Try to get by key first (modern method)
                        NamespacedKey enchantKey = NamespacedKey.minecraft(enchantName.toLowerCase());
                        enchantment = Enchantment.getByKey(enchantKey);
                    } catch (Exception e) {
                        // Fallback to deprecated method
                        try {
                            enchantment = Enchantment.getByName(enchantName);
                        } catch (Exception ex) {
                            plugin.getLogger().warning("Could not find enchantment: " + enchantName);
                        }
                    }

                    if (enchantment != null) {
                        item.addUnsafeEnchantment(enchantment, level);
                    } else {
                        plugin.getLogger().warning("Unknown enchantment: " + enchantName);
                    }
                }
            }
        }

        // Add glowing effect if needed
        if (reward.has("glowing") && reward.get("glowing").getAsBoolean() &&
                item.getEnchantments().isEmpty()) {
            // Add a dummy enchantment for glowing effect
            item.addUnsafeEnchantment(Enchantment.DURABILITY, 1);
        }

        return item;
    }

    /**
     * Give default reward item to player
     * @param player The player
     * @param transactionId The transaction ID
     */
    private void giveDefaultRewardItem(Player player, String transactionId) {
        plugin.getLogger().info("Creating default NFT item for player " + player.getName() + " for achievement " + key);

        // Create default NFT item
        ItemStack item = plugin.getItemManager().createNftItem(transactionId, key);
        plugin.getLogger().info("Created default NFT item: " + item.getType() +
                " with name: " + (item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                item.getItemMeta().getDisplayName() : "<no name>"));

        // Give item to player
        plugin.getLogger().info("Adding default NFT item to player inventory");
        player.getInventory().addItem(item);

        // Force update inventory to make sure client sees the new item
        player.updateInventory();

        // Send message
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                plugin.getConfigManager().getMessage("item_preserved")
                        .replace("%item_name%", item.getItemMeta().getDisplayName()));

        // Play sound
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

        plugin.getLogger().info("Successfully gave default NFT item to player " + player.getName());
    }

    /**
     * Initialize the achievement
     */
    @Override
    public void initialize() {
        // Create metadata directory if it doesn't exist
        File metadataDir = new File(plugin.getDataFolder(), "metadata");
        if (!metadataDir.exists()) {
            metadataDir.mkdirs();
        }

        // Register events
        registerEvents();

        plugin.getLogger().info("Initialized achievement: " + key);
    }
}
