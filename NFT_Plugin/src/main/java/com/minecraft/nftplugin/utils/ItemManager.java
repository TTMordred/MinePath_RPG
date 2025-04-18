package com.minecraft.nftplugin.utils;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemManager {

    private final NFTPlugin plugin;
    private final NamespacedKey nftKey;
    private final NamespacedKey nftIdKey;
    private final NamespacedKey achievementKey;

    public ItemManager(NFTPlugin plugin) {
        this.plugin = plugin;
        this.nftKey = new NamespacedKey(plugin, "nft");
        this.nftIdKey = new NamespacedKey(plugin, "nft_id");
        this.achievementKey = new NamespacedKey(plugin, "achievement_key");
    }

    /**
     * Create an NFT item
     * @param nftId The NFT ID
     * @param achievementType The achievement type
     * @return The NFT item
     */
    public ItemStack createNftItem(String nftId, String achievementType) {
        // Get item properties from config
        Material material = plugin.getConfigManager().getNftItemMaterial();
        String name = plugin.getConfigManager().getNftItemName();
        List<String> lore = new ArrayList<>(plugin.getConfigManager().getNftItemLore());
        Map<String, Integer> enchantments = plugin.getConfigManager().getNftItemEnchantments();
        boolean unbreakable = plugin.getConfigManager().isNftItemUnbreakable();
        int customModelData = plugin.getConfigManager().getNftItemCustomModelData();

        // Create the item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set name and lore
            meta.setDisplayName(name);

            // Replace placeholders in lore
            List<String> processedLore = new ArrayList<>();
            for (String line : lore) {
                processedLore.add(line.replace("%nft_id%", nftId));
            }
            meta.setLore(processedLore);

            // Set unbreakable
            meta.setUnbreakable(unbreakable);

            // Add enchantments
            for (Map.Entry<String, Integer> entry : enchantments.entrySet()) {
                try {
                    Enchantment enchantment = Enchantment.getByName(entry.getKey());
                    if (enchantment != null) {
                        meta.addEnchant(enchantment, entry.getValue(), true);
                    } else {
                        plugin.getLogger().warning("Invalid enchantment: " + entry.getKey());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to add enchantment: " + e.getMessage());
                }
            }

            // Set custom model data
            meta.setCustomModelData(customModelData);

            // Add item flags
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);

            // Add NBT data
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(nftKey, PersistentDataType.BYTE, (byte) 1); // 1 = true
            container.set(nftIdKey, PersistentDataType.STRING, nftId);
            container.set(achievementKey, PersistentDataType.STRING, achievementType);

            // Apply meta to item
            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Check if an item is an NFT item
     * @param item The item to check
     * @return True if the item is an NFT item, false otherwise
     */
    public boolean isNftItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return false;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.has(nftKey, PersistentDataType.BYTE) &&
                container.getOrDefault(nftKey, PersistentDataType.BYTE, (byte) 0) == (byte) 1;
    }

    /**
     * Get the NFT ID from an item
     * @param item The item
     * @return The NFT ID, or null if not found
     */
    public String getNftId(ItemStack item) {
        if (!isNftItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(nftIdKey, PersistentDataType.STRING, null);
    }

    /**
     * Get the achievement key from an item
     * @param item The item
     * @return The achievement key, or null if not found
     */
    public String getAchievementKey(ItemStack item) {
        if (!isNftItem(item)) {
            return null;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null) {
            return null;
        }

        PersistentDataContainer container = meta.getPersistentDataContainer();
        return container.getOrDefault(achievementKey, PersistentDataType.STRING, null);
    }

    /**
     * Get the NFT key
     * @return The NFT key
     */
    public NamespacedKey getNftKey() {
        return nftKey;
    }

    /**
     * Get the NFT ID key
     * @return The NFT ID key
     */
    public NamespacedKey getNftIdKey() {
        return nftIdKey;
    }

    /**
     * Get the achievement key
     * @return The achievement key
     */
    public NamespacedKey getAchievementNamespacedKey() {
        return achievementKey;
    }
}
