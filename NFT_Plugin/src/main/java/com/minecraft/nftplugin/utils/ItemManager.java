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
                    String enchantKey = entry.getKey();
                    int level = entry.getValue();

                    // Check for custom enchantments
                    if (enchantKey.equals("EXPLOSION")) {
                        // Add to lore for CustomEnchantManager to process


                        // Make sure the Special Enchantments section exists in lore
                        boolean hasSpecialSection = false;
                        for (String line : lore) {
                            if (line.contains("§d§lSpecial Enchantments:")) {
                                hasSpecialSection = true;
                                break;
                            }
                        }

                        if (!hasSpecialSection) {
                            lore.add("§d§lSpecial Enchantments:");
                        }

                        // Add the enchantment to lore if not already present
                        boolean hasEnchant = false;
                        for (String line : lore) {
                            if (line.contains("§b- Explosion")) {
                                hasEnchant = true;
                                break;
                            }
                        }

                        if (!hasEnchant) {
                            String romanLevel = arabicToRoman(level);
                            int size = level + 2; // New formula for size (level 1 = 3 blocks)
                            lore.add("§b- Explosion " + romanLevel + ": §7Break blocks in a " + size + "x" + size + " area (vertical plane when mining horizontally, horizontal plane when mining vertically)");
                        }

                        continue;
                    } else if (enchantKey.equals("LASER")) {
                        // Add to lore for CustomEnchantManager to process


                        // Make sure the Special Enchantments section exists in lore
                        boolean hasSpecialSection = false;
                        for (String line : lore) {
                            if (line.contains("§d§lSpecial Enchantments:")) {
                                hasSpecialSection = true;
                                break;
                            }
                        }

                        if (!hasSpecialSection) {
                            lore.add("§d§lSpecial Enchantments:");
                        }

                        // Add the enchantment to lore if not already present
                        boolean hasEnchant = false;
                        for (String line : lore) {
                            if (line.contains("§b- Laser")) {
                                hasEnchant = true;
                                break;
                            }
                        }

                        if (!hasEnchant) {
                            String romanLevel = arabicToRoman(level);
                            int depth = level + 1; // Simple formula for depth
                            lore.add("§b- Laser " + romanLevel + ": §7Break blocks up to " + depth + " blocks deep");
                        }

                        continue;

                    }

                    // Handle standard Minecraft enchantments
                    try {
                        // Try to get the enchantment by name
                        Enchantment enchantment = null;

                        // Check for common enchantments
                        if (enchantKey.equals("EFFICIENCY")) {
                            enchantment = Enchantment.DIG_SPEED;
                        } else if (enchantKey.equals("FORTUNE")) {
                            enchantment = Enchantment.LOOT_BONUS_BLOCKS;
                        } else if (enchantKey.equals("DURABILITY")) {
                            enchantment = Enchantment.DURABILITY;
                        } else if (enchantKey.equals("MENDING")) {
                            enchantment = Enchantment.MENDING;
                        } else if (enchantKey.equals("SILK_TOUCH")) {
                            enchantment = Enchantment.SILK_TOUCH;
                        } else {
                            // Try to get by name (deprecated but still works)
                            enchantment = Enchantment.getByName(enchantKey);
                        }

                        if (enchantment != null) {
                            meta.addEnchant(enchantment, level, true);
                        } else {
                            plugin.getLogger().warning("Invalid enchantment: " + enchantKey);
                        }
                    } catch (Exception e) {
                        plugin.getLogger().warning("Failed to add enchantment: " + enchantKey + " - " + e.getMessage());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to add enchantment: " + e.getMessage());
                }
            }

            // Update lore
            meta.setLore(lore);

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

            // Apply custom enchantments if this is a special pickaxe
            if (plugin.getCustomEnchantManager() != null) {
                item = plugin.getCustomEnchantManager().applyMaxEnchantments(item);
            }
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

    /**
     * Convert an Arabic number to a Roman numeral
     * @param arabic The Arabic number
     * @return The Roman numeral
     */
    private String arabicToRoman(int arabic) {
        switch (arabic) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            default: return "I";
        }
    }
}
