package com.minecraft.nftplugin.storage;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * A simple storage system for NFT inventories
 */
public class SimpleNFTInventory {

    private final NFTPlugin plugin;
    private final File dataFolder;
    private final Map<UUID, Map<Integer, ItemStack>> inventories = new HashMap<>();
    private final Map<UUID, FileConfiguration> configs = new HashMap<>();

    public SimpleNFTInventory(NFTPlugin plugin) {
        this.plugin = plugin;
        this.dataFolder = new File(plugin.getDataFolder(), "nft_inventories");
        if (!dataFolder.exists()) {
            dataFolder.mkdirs();
        }
        
        plugin.getLogger().info("SimpleNFTInventory initialized at " + dataFolder.getAbsolutePath());
    }

    /**
     * Get the file for a player's inventory
     * @param uuid The player's UUID
     * @return The file
     */
    private File getPlayerFile(UUID uuid) {
        return new File(dataFolder, uuid.toString() + ".yml");
    }

    /**
     * Load a player's inventory
     * @param player The player
     * @return The inventory as a map of slot to item
     */
    public Map<Integer, ItemStack> loadInventory(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Check if already loaded
        if (inventories.containsKey(uuid)) {
            plugin.getLogger().info("Returning cached inventory for " + player.getName());
            return inventories.get(uuid);
        }
        
        // Create new inventory
        Map<Integer, ItemStack> inventory = new HashMap<>();
        
        // Load from file if exists
        File file = getPlayerFile(uuid);
        if (file.exists()) {
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);
            configs.put(uuid, config);
            
            if (config.contains("inventory")) {
                for (String key : config.getConfigurationSection("inventory").getKeys(false)) {
                    try {
                        int slot = Integer.parseInt(key);
                        ItemStack item = config.getItemStack("inventory." + key);
                        if (item != null) {
                            inventory.put(slot, item);
                            plugin.getLogger().info("Loaded item in slot " + slot + " for " + player.getName());
                        }
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid slot number in inventory file: " + key);
                    }
                }
            }
            
            plugin.getLogger().info("Loaded " + inventory.size() + " items from file for " + player.getName());
        } else {
            // Create new config
            FileConfiguration config = new YamlConfiguration();
            configs.put(uuid, config);
            plugin.getLogger().info("Created new inventory for " + player.getName());
        }
        
        // Cache and return
        inventories.put(uuid, inventory);
        return inventory;
    }
    
    /**
     * Save a player's inventory
     * @param player The player
     */
    public void saveInventory(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Check if loaded
        if (!inventories.containsKey(uuid) || !configs.containsKey(uuid)) {
            plugin.getLogger().warning("Cannot save inventory for " + player.getName() + " - not loaded");
            return;
        }
        
        Map<Integer, ItemStack> inventory = inventories.get(uuid);
        FileConfiguration config = configs.get(uuid);
        
        // Clear existing inventory
        config.set("inventory", null);
        
        // Save items
        for (Map.Entry<Integer, ItemStack> entry : inventory.entrySet()) {
            config.set("inventory." + entry.getKey(), entry.getValue());
        }
        
        // Save to file
        try {
            config.save(getPlayerFile(uuid));
            plugin.getLogger().info("Saved " + inventory.size() + " items to file for " + player.getName());
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save inventory for " + player.getName() + ": " + e.getMessage());
        }
    }
    
    /**
     * Set an item in a player's inventory
     * @param player The player
     * @param slot The slot
     * @param item The item
     */
    public void setItem(Player player, int slot, ItemStack item) {
        UUID uuid = player.getUniqueId();
        
        // Load inventory if not loaded
        if (!inventories.containsKey(uuid)) {
            loadInventory(player);
        }
        
        // Set item
        Map<Integer, ItemStack> inventory = inventories.get(uuid);
        if (item == null) {
            inventory.remove(slot);
            plugin.getLogger().info("Removed item from slot " + slot + " for " + player.getName());
        } else {
            inventory.put(slot, item.clone());
            plugin.getLogger().info("Set item in slot " + slot + " for " + player.getName());
        }
    }
    
    /**
     * Get an item from a player's inventory
     * @param player The player
     * @param slot The slot
     * @return The item, or null if not found
     */
    public ItemStack getItem(Player player, int slot) {
        UUID uuid = player.getUniqueId();
        
        // Load inventory if not loaded
        if (!inventories.containsKey(uuid)) {
            loadInventory(player);
        }
        
        // Get item
        Map<Integer, ItemStack> inventory = inventories.get(uuid);
        return inventory.get(slot);
    }
    
    /**
     * Clear a player's inventory
     * @param player The player
     */
    public void clearInventory(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Load inventory if not loaded
        if (!inventories.containsKey(uuid)) {
            loadInventory(player);
        }
        
        // Clear inventory
        Map<Integer, ItemStack> inventory = inventories.get(uuid);
        inventory.clear();
        plugin.getLogger().info("Cleared inventory for " + player.getName());
    }
    
    /**
     * Unload a player's inventory
     * @param player The player
     */
    public void unloadInventory(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Save first
        if (inventories.containsKey(uuid)) {
            saveInventory(player);
        }
        
        // Remove from cache
        inventories.remove(uuid);
        configs.remove(uuid);
        plugin.getLogger().info("Unloaded inventory for " + player.getName());
    }
}
