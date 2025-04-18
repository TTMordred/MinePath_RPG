package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;

public class InventoryListener implements Listener {
    
    private final NFTPlugin plugin;
    
    public InventoryListener(NFTPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Prevent players from dropping NFT items
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        ItemStack item = event.getItemDrop().getItemStack();
        
        if (plugin.getItemManager().isNftItem(item)) {
            event.setCancelled(true);
            Player player = event.getPlayer();
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou cannot drop this NFT item!");
        }
    }
    
    /**
     * Prevent players from moving NFT items to other inventories
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        // Check if the clicked inventory is not the player's inventory
        if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
            // Check if the cursor item is an NFT item
            ItemStack cursorItem = event.getCursor();
            if (plugin.getItemManager().isNftItem(cursorItem)) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou cannot move this NFT item to another inventory!");
                return;
            }
        }
        
        // Check if the current item is an NFT item
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem != null && plugin.getItemManager().isNftItem(currentItem)) {
            // Check if the player is trying to move the item to another inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // Allow moving within the player's inventory
                if (event.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
                    event.setCancelled(true);
                    Player player = (Player) event.getWhoClicked();
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou cannot move this NFT item to another inventory!");
                }
            }
        }
    }
    
    /**
     * Prevent players from dragging NFT items to other inventories
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        ItemStack item = event.getOldCursor();
        
        if (plugin.getItemManager().isNftItem(item)) {
            // Check if any of the slots are in the top inventory
            boolean draggedToTop = event.getRawSlots().stream()
                    .anyMatch(slot -> slot < event.getView().getTopInventory().getSize());
            
            if (draggedToTop && event.getView().getTopInventory().getType() != InventoryType.CRAFTING) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou cannot move this NFT item to another inventory!");
            }
        }
    }
    
    /**
     * Prevent hoppers and other inventory movers from moving NFT items
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryMoveItem(InventoryMoveItemEvent event) {
        ItemStack item = event.getItem();
        
        if (plugin.getItemManager().isNftItem(item)) {
            event.setCancelled(true);
        }
    }
}
