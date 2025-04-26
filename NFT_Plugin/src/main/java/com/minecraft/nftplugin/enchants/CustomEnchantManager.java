package com.minecraft.nftplugin.enchants;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages custom enchantments for NFT items
 */
public class CustomEnchantManager {

    private final NFTPlugin plugin;
    private final NamespacedKey explosionKey;
    private final NamespacedKey laserKey;

    // Patterns to extract enchantment levels from lore
    private final Pattern explosionPattern = Pattern.compile("§b- Explosion ([IVX]+):");
    private final Pattern laserPattern = Pattern.compile("§b- Laser ([IVX]+):");

    // Maximum enchantment levels
    private static final int MAX_EXPLOSION_LEVEL = 5;
    private static final int MAX_LASER_LEVEL = 5;

    public CustomEnchantManager(NFTPlugin plugin) {
        this.plugin = plugin;
        this.explosionKey = new NamespacedKey(plugin, "explosion_level");
        this.laserKey = new NamespacedKey(plugin, "laser_level");
    }

    /**
     * Apply maximum level enchantments to a newly created NFT item
     * @param item The NFT item
     * @return The enchanted item
     */
    public ItemStack applyMaxEnchantments(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();
        boolean modified = false;

        // First check if this is an NFT item by checking if it has the NFT key
        if (plugin.getItemManager().isNftItem(item)) {


            // Check for enchantments in the lore
            if (meta.hasLore()) {
                List<String> lore = meta.getLore();

                // Check for Explosion enchantment
                for (String line : lore) {
                    Matcher explosionMatcher = explosionPattern.matcher(line);
                    if (explosionMatcher.find()) {
                        String levelStr = explosionMatcher.group(1);
                        int level = romanToArabic(levelStr);
                        container.set(explosionKey, PersistentDataType.INTEGER, level);

                        modified = true;
                    }

                    // Check for Laser enchantment
                    Matcher laserMatcher = laserPattern.matcher(line);
                    if (laserMatcher.find()) {
                        String levelStr = laserMatcher.group(1);
                        int level = romanToArabic(levelStr);
                        container.set(laserKey, PersistentDataType.INTEGER, level);

                        modified = true;
                    }


                }
            }

            // If we modified the item, update it
            if (modified) {
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    /**
     * Check if an item has the Explosion enchantment
     * @param item The item to check
     * @return The level of the enchantment, or 0 if not present
     */
    public int getExplosionLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();

        // First check persistent data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer level = container.get(explosionKey, PersistentDataType.INTEGER);
        if (level != null) {
            return level;
        }

        // If not found, check lore
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                Matcher matcher = explosionPattern.matcher(line);
                if (matcher.find()) {
                    String levelStr = matcher.group(1);
                    return romanToArabic(levelStr);
                }
            }
        }

        return 0;
    }

    /**
     * Check if an item has the Laser enchantment
     * @param item The item to check
     * @return The level of the enchantment, or 0 if not present
     */
    public int getLaserLevel(ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return 0;
        }

        ItemMeta meta = item.getItemMeta();

        // First check persistent data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        Integer level = container.get(laserKey, PersistentDataType.INTEGER);
        if (level != null) {
            return level;
        }

        // If not found, check lore
        if (meta.hasLore()) {
            List<String> lore = meta.getLore();
            for (String line : lore) {
                Matcher matcher = laserPattern.matcher(line);
                if (matcher.find()) {
                    String levelStr = matcher.group(1);
                    return romanToArabic(levelStr);
                }
            }
        }

        return 0;
    }



    /**
     * Get the size of the explosion area based on level
     * @param level The enchantment level
     * @return The size of the area
     */
    public int getExplosionSize(int level) {
        switch (level) {
            case 1: return 3; // 3x3
            case 2: return 4; // 4x4
            case 3: return 5; // 5x5
            case 4: return 6; // 6x6
            case 5: return 7; // 7x7
            default: return Math.min(2 + level, 7); // Limit to 7x7
        }
    }

    /**
     * Get the depth of the laser based on level
     * @param level The enchantment level
     * @return The depth
     */
    public int getLaserDepth(int level) {
        switch (level) {
            case 1: return 2;
            case 2: return 3;
            case 3: return 4;
            case 4: return 5;
            case 5: return 6;
            default: return Math.min(1 + level, 6); // Limit to 6 blocks
        }
    }

    /**
     * Convert a Roman numeral to an Arabic number
     * @param roman The Roman numeral
     * @return The Arabic number
     */
    private int romanToArabic(String roman) {
        switch (roman.toUpperCase()) {
            case "I": return 1;
            case "II": return 2;
            case "III": return 3;
            case "IV": return 4;
            case "V": return 5;
            default: return 1;
        }
    }

    /**
     * Apply the Explosion enchantment effect
     * @param player The player
     * @param block The block that was broken
     * @param level The enchantment level
     */
    public void applyExplosionEffect(Player player, Block block, int level) {
        if (level <= 0) {
            return;
        }

        int size = getExplosionSize(level);
        int radius = size / 2;

        // Get the facing direction of the player
        org.bukkit.util.Vector direction = player.getLocation().getDirection().normalize();

        // Determine which axis the player is primarily facing
        double absX = Math.abs(direction.getX());
        double absY = Math.abs(direction.getY());
        double absZ = Math.abs(direction.getZ());

        // Get the center of the explosion
        int centerX = block.getX();
        int centerY = block.getY();
        int centerZ = block.getZ();

        // Check if player is looking mostly up or down (Y axis)
        if (absY > absX && absY > absZ) {
            // Player is looking up or down
            // Break blocks in a horizontal pattern (X and Z)
            for (int x = centerX - radius; x <= centerX + radius; x++) {
                for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                    // Skip the original block as it's already broken
                    if (x == centerX && z == centerZ) {
                        continue;
                    }

                    Block targetBlock = block.getWorld().getBlockAt(x, centerY, z);

                    // Only break blocks that can be broken by a pickaxe
                    if (isBreakableByPickaxe(targetBlock.getType())) {
                        targetBlock.breakNaturally(player.getInventory().getItemInMainHand());
                    }
                }
            }
        } else {
            // Player is looking horizontally (X or Z axis)
            // Break blocks in a vertical pattern (Y axis) and along the other horizontal axis
            // Determine the primary horizontal axis
            boolean primaryX = absX > absZ;

            for (int y = centerY - radius; y <= centerY + radius; y++) {
                if (primaryX) {
                    // Player is primarily looking along X axis, so break in Y-Z plane
                    for (int z = centerZ - radius; z <= centerZ + radius; z++) {
                        // Skip the original block as it's already broken
                        if (y == centerY && z == centerZ) {
                            continue;
                        }

                        Block targetBlock = block.getWorld().getBlockAt(centerX, y, z);

                        // Only break blocks that can be broken by a pickaxe
                        if (isBreakableByPickaxe(targetBlock.getType())) {
                            targetBlock.breakNaturally(player.getInventory().getItemInMainHand());
                        }
                    }
                } else {
                    // Player is primarily looking along Z axis, so break in X-Y plane
                    for (int x = centerX - radius; x <= centerX + radius; x++) {
                        // Skip the original block as it's already broken
                        if (x == centerX && y == centerY) {
                            continue;
                        }

                        Block targetBlock = block.getWorld().getBlockAt(x, y, centerZ);

                        // Only break blocks that can be broken by a pickaxe
                        if (isBreakableByPickaxe(targetBlock.getType())) {
                            targetBlock.breakNaturally(player.getInventory().getItemInMainHand());
                        }
                    }
                }
            }
        }
    }

    /**
     * Apply the Laser enchantment effect
     * @param player The player
     * @param block The block that was broken
     * @param level The enchantment level
     */
    public void applyLaserEffect(Player player, Block block, int level) {
        if (level <= 0) {
            return;
        }

        int depth = getLaserDepth(level);

        // Get the direction the player is facing
        org.bukkit.util.Vector direction = player.getLocation().getDirection().normalize();

        // Break blocks in a line
        for (int i = 1; i < depth; i++) {
            int x = block.getX() + (int) Math.round(direction.getX() * i);
            int y = block.getY() + (int) Math.round(direction.getY() * i);
            int z = block.getZ() + (int) Math.round(direction.getZ() * i);

            Block targetBlock = block.getWorld().getBlockAt(x, y, z);

            // Only break blocks that can be broken by a pickaxe
            if (isBreakableByPickaxe(targetBlock.getType())) {
                targetBlock.breakNaturally(player.getInventory().getItemInMainHand());
            }
        }
    }



    /**
     * Check if a material can be broken by a pickaxe
     * @param material The material to check
     * @return True if the material can be broken by a pickaxe
     */
    private boolean isBreakableByPickaxe(Material material) {
        return material.name().contains("STONE") ||
               material.name().contains("ORE") ||
               material.name().contains("BRICK") ||
               material.name().contains("CONCRETE") ||
               material.name().contains("TERRACOTTA") ||
               material == Material.OBSIDIAN ||
               material == Material.NETHERRACK ||
               material == Material.END_STONE ||
               material == Material.ANCIENT_DEBRIS;
    }
}
