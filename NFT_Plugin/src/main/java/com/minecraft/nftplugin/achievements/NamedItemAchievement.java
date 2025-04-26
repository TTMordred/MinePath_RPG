package com.minecraft.nftplugin.achievements;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.util.UUID;

/**
 * Achievement for holding a specific named item
 * This class handles all types of named items, including paper items
 */
public class NamedItemAchievement extends AbstractAchievement implements Listener {

    private static final int REQUIRED_PROGRESS = 1; // Only need to hold once

    private final Material targetMaterial;
    private final String targetItemName;

    /**
     * Constructor with default values
     * @param plugin The NFTPlugin instance
     * @deprecated Use the constructor with parameters instead
     */
    @Deprecated
    public NamedItemAchievement(NFTPlugin plugin) {
        // This constructor should not be used directly anymore
        // Instead, AchievementManager should create instances with parameters from config
        this(plugin,
             "great_light",
             Material.valueOf(plugin.getConfig().getString("achievements.great_light.material", "BLAZE_ROD")),
             plugin.getConfig().getString("achievements.great_light.item_name", "Great Light"));

        plugin.getLogger().warning("Using deprecated constructor for NamedItemAchievement. This should not happen in production.");
        plugin.getLogger().warning("Please update your code to use the constructor with parameters.");
    }

    /**
     * Constructor with custom values
     * @param plugin The NFTPlugin instance
     * @param key The achievement key
     * @param material The target material
     * @param itemName The target item name
     */
    public NamedItemAchievement(NFTPlugin plugin, String key, Material material, String itemName) {
        // Super constructor must be the first statement
        super(plugin, key,
              plugin.getConfigManager().getNftName(key),
              plugin.getConfigManager().getNftDescription(key),
              REQUIRED_PROGRESS,
              getMetadataFilePath(plugin, key),
              plugin.getConfigManager().getNftImageUrl(key));

        // Set instance variables
        this.targetMaterial = material;
        this.targetItemName = itemName;

        // Log information
        plugin.getLogger().info("Created NamedItemAchievement: " + key);
        plugin.getLogger().info("  - Material: " + material);
        plugin.getLogger().info("  - Target item name: " + itemName);
        plugin.getLogger().info("  - Metadata file: " + getMetadataFilePath(plugin, key));
    }

    /**
     * Get the metadata file path for an achievement
     * @param plugin The NFTPlugin instance
     * @param key The achievement key
     * @return The metadata file path
     */
    private static String getMetadataFilePath(NFTPlugin plugin, String key) {
        // Always use the standard naming convention: <key>.json
        // The "metadata/" prefix will be added by AbstractAchievement.getMetadataFilePath()
        String metadataFilePath = key + ".json";

        // Check if the file exists
        File metadataFile = new File(plugin.getDataFolder(), "metadata/" + metadataFilePath);
        if (!metadataFile.exists()) {
            plugin.getLogger().warning("Metadata file not found: metadata/" + metadataFilePath);
            plugin.getLogger().warning("Please create this file in the plugin directory");
        } else {
            plugin.getLogger().info("Using metadata file: metadata/" + metadataFilePath);
        }

        return metadataFilePath;
    }

    @Override
    public void registerEvents() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        checkPlayerHoldingItem(player);
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> checkPlayerHoldingItem(player), 1L);
    }

    private void checkPlayerHoldingItem(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack item = player.getInventory().getItemInMainHand();

        // Debug info about the item
        plugin.getLogger().info("Checking item for player " + player.getName() + ": " +
                (item == null ? "null" : item.getType() + ", has meta: " + (item.hasItemMeta() ? "yes" : "no")));

        if (isTargetItem(item)) {
            // Player is holding the target item
            plugin.getLogger().info("Player " + player.getName() + " is holding the named item: " + targetItemName);

            // Check if player has already completed this achievement
            boolean hasCompleted = plugin.getDatabaseManager().hasCompletedAchievement(uuid, key);
            plugin.getLogger().info("Player " + player.getName() + " has completed achievement " + key + ": " + hasCompleted);

            if (!hasCompleted) {
                // Complete the achievement immediately
                plugin.getLogger().info("Updating progress for player " + player.getName() + " for achievement " + key);
                boolean updated = updateProgress(player, REQUIRED_PROGRESS);
                plugin.getLogger().info("Progress updated: " + updated);

                // Send message
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.GREEN + "You have found the " + ChatColor.GOLD + targetItemName +
                        ChatColor.GREEN + "! You have earned the " + ChatColor.AQUA + getName() +
                        ChatColor.GREEN + " NFT!");

                // Tell the player we're minting an NFT
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.YELLOW + "Minting NFT... Your " + ChatColor.GOLD + targetItemName +
                        ChatColor.YELLOW + " will be replaced with an NFT item.");

                // Remove the original item
                int slot = player.getInventory().getHeldItemSlot();
                player.getInventory().setItem(slot, null);

                // Mint the NFT - this will create and give the new NFT item
                onAchievementCompleted(player);
            }
        }
    }

    /**
     * Check if an item is an NFT item
     * @param meta The item meta
     * @return True if the item is an NFT item, false otherwise
     */
    private boolean isNftItem(ItemMeta meta) {
        if (meta == null) {
            return false;
        }

        // Check for NFT tag in persistent data container
        PersistentDataContainer container = meta.getPersistentDataContainer();
        NamespacedKey nftKey = new NamespacedKey(plugin, "nft");

        return container.has(nftKey, PersistentDataType.BYTE);
    }

    /**
     * Check if an item is a target item for this achievement
     * @param item The item
     * @return True if the item is a target item, false otherwise
     */
    private boolean isTargetItem(ItemStack item) {
        // Check if item is null
        if (item == null) {
            plugin.getLogger().info("[" + key + "] Item is null, not a match");
            return false;
        }

        // Check material
        if (item.getType() != targetMaterial) {
            plugin.getLogger().info("[" + key + "] Item material " + item.getType() + " does not match target material " + targetMaterial);
            return false;
        }

        // Check item meta
        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            plugin.getLogger().info("[" + key + "] Item meta is null");
            return false;
        }

        // Check if the item is already an NFT item - if so, don't allow it to trigger again
        if (isNftItem(meta)) {
            plugin.getLogger().info("[" + key + "] Item is already an NFT item, ignoring");
            return false;
        }

        // Check display name
        if (!meta.hasDisplayName()) {
            plugin.getLogger().info("[" + key + "] Item does not have a display name");
            return false;
        }

        // Log the actual display name for debugging
        String rawDisplayName = meta.getDisplayName();
        String displayName = ChatColor.stripColor(rawDisplayName);

        plugin.getLogger().info("=== ITEM NAME CHECK FOR " + key + " ===");
        plugin.getLogger().info("Item display name (raw): '" + rawDisplayName + "'");
        plugin.getLogger().info("Item display name (stripped): '" + displayName + "'");
        plugin.getLogger().info("Target name from config: '" + targetItemName + "'");

        // Try different comparison methods
        boolean exactMatch = displayName.equals(targetItemName);
        boolean looseMatch = displayName.trim().equalsIgnoreCase(targetItemName.trim());
        boolean containsMatch = displayName.toLowerCase().contains(targetItemName.toLowerCase());
        boolean reverseContainsMatch = targetItemName.toLowerCase().contains(displayName.toLowerCase());

        plugin.getLogger().info("Exact match: " + exactMatch);
        plugin.getLogger().info("Case-insensitive match: " + looseMatch);
        plugin.getLogger().info("Item contains target: " + containsMatch);
        plugin.getLogger().info("Target contains item: " + reverseContainsMatch);

        // Return true if any match works
        boolean result = exactMatch || looseMatch || containsMatch || reverseContainsMatch;
        plugin.getLogger().info("Final match result: " + result);
        plugin.getLogger().info("=== END ITEM NAME CHECK ===\n");
        return result;
    }
}
