package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.enchants.CustomEnchantManager;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener for custom enchantment effects
 */
public class CustomEnchantListener implements Listener {

    private final NFTPlugin plugin;
    private final CustomEnchantManager enchantManager;

    public CustomEnchantListener(NFTPlugin plugin, CustomEnchantManager enchantManager) {
        this.plugin = plugin;
        this.enchantManager = enchantManager;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.isCancelled()) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Skip if player is in creative mode
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }

        // Check if the block is breakable by a pickaxe
        if (!isBreakableByPickaxe(block.getType())) {
            return;
        }

        // Get the item in the player's hand
        ItemStack item = player.getInventory().getItemInMainHand();

        // Skip if not a pickaxe
        if (!isPickaxe(item.getType())) {
            return;
        }

        // Check if the item is an NFT item
        if (!plugin.getItemManager().isNftItem(item)) {
            return;
        }

        // Get enchantment levels
        int explosionLevel = enchantManager.getExplosionLevel(item);
        int laserLevel = enchantManager.getLaserLevel(item);

        // Apply enchantment effects
        if (explosionLevel > 0) {
            enchantManager.applyExplosionEffect(player, block, explosionLevel);
        }

        if (laserLevel > 0) {
            enchantManager.applyLaserEffect(player, block, laserLevel);
        }
    }

    /**
     * Check if a material is a pickaxe
     * @param material The material to check
     * @return True if the material is a pickaxe
     */
    private boolean isPickaxe(Material material) {
        return material == Material.WOODEN_PICKAXE ||
               material == Material.STONE_PICKAXE ||
               material == Material.IRON_PICKAXE ||
               material == Material.GOLDEN_PICKAXE ||
               material == Material.DIAMOND_PICKAXE ||
               material == Material.NETHERITE_PICKAXE;
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
