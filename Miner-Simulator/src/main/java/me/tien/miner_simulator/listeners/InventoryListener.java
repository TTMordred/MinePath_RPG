package me.tien.miner_simulator.listeners;

import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.upgrade.InventoryUpgrade;

public class InventoryListener implements Listener {

    private final Miner_Simulator plugin;
    private final InventoryUpgrade inventoryUpgrade;

    public InventoryListener(Miner_Simulator plugin, InventoryUpgrade inventoryUpgrade) {
        this.plugin = plugin;
        this.inventoryUpgrade = inventoryUpgrade;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player data when they log in
        inventoryUpgrade.loadPlayerData(player);

        // Apply inventory lock effect
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            inventoryUpgrade.applyEffect(player);
        }, 10L); // Half second delay to ensure inventory is ready
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Save data when player quits
        inventoryUpgrade.saveData();
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        
        // Reload upgrade data before applying effects to ensure correct updates
        inventoryUpgrade.loadPlayerData(player);
        
        // Apply inventory lock effect when player changes worlds
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            inventoryUpgrade.applyEffect(player);
        }, 5L);
    }
    
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        
        // Reload upgrade data before applying effects
        inventoryUpgrade.loadPlayerData(player);
        
        // Apply inventory lock effect when player respawns
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            inventoryUpgrade.applyEffect(player);
        }, 5L);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        
        // Get list of currently locked slots (updated by level)
        List<Integer> lockedSlots = inventoryUpgrade.getLockedSlots(player);

        // If clicking in player's inventory
        if (event.getClickedInventory() == player.getInventory()) {
            int clickedSlot = event.getSlot();

            // Check if slot is locked
            if (lockedSlots.contains(clickedSlot)) {
                event.setCancelled(true);
                player.sendMessage("§c§lNotice: §r§cThis slot is locked. Unlock at §f/shop");

                // If it's a barrier item, open upgrade shop
                ItemStack clickedItem = event.getCurrentItem();
                if (clickedItem != null && inventoryUpgrade.isLockBarrier(clickedItem)) {
                    // Could call shop command here
                    // player.performCommand("shop upgrade");
                }
                return;
            }
        }

        // Prevent other actions that could affect locked slots
        if (event.getAction().name().contains("PLACE") ||
                event.getAction().name().contains("HOTBAR_SWAP")) {

            int slot = event.getSlot();
            // For hotbar swap, check if hotbar slot is locked
            if (event.getAction().name().contains("HOTBAR_SWAP")) {
                int hotbarButton = event.getHotbarButton();
                if (hotbarButton != -1 && lockedSlots.contains(hotbarButton)) {
                    event.setCancelled(true);
                    player.sendMessage("§c§lNotice: §r§cCannot use locked slots.");
                    return;
                }
            }

            if (event.getClickedInventory() == player.getInventory() && lockedSlots.contains(slot)) {
                event.setCancelled(true);
                player.sendMessage("§c§lNotice: §r§cCannot place items in locked slots.");
            }
        }
        
        // Handle SHIFT-CLICK to prevent placing items in locked slots
        if (event.isShiftClick() && event.getClickedInventory() != player.getInventory()) {
            // If player shift-clicks in another inventory (like chest, furnace, etc.)
            // Check if the next slot where the item would be placed is locked
            
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && !clickedItem.getType().isAir()) {
                // Find slot where the item could be placed (how Minecraft handles shift-click)
                boolean wouldPlaceInLockedSlot = true;
                
                // Check slots in player inventory from bottom to top
                for (int i = 35; i >= 0; i--) {
                    if (lockedSlots.contains(i)) continue; // Skip locked slots
                    
                    ItemStack existingItem = player.getInventory().getItem(i);
                    if (existingItem == null) {
                        // Found an empty non-locked slot
                        wouldPlaceInLockedSlot = false;
                        break;
                    } else if (existingItem.isSimilar(clickedItem) && 
                               existingItem.getAmount() < existingItem.getMaxStackSize()) {
                        // Found a slot with a similar item that can stack more
                        wouldPlaceInLockedSlot = false;
                        break;
                    }
                }
                
                // If no suitable slot found, it means the item would be placed in a locked slot
                if (wouldPlaceInLockedSlot) {
                    event.setCancelled(true);
                    player.sendMessage("§c§lNotice: §r§cNot enough space in inventory. Unlock more at §f/shop");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        // Get list of currently locked slots (updated by level)
        List<Integer> lockedSlots = inventoryUpgrade.getLockedSlots(player);

        // Check if any affected slot is a locked slot
        for (Integer rawSlot : event.getRawSlots()) {
            if (rawSlot < player.getInventory().getSize()) { // Make sure this is a slot in player inventory
                int slot = rawSlot;
                if (lockedSlots.contains(slot)) {
                    event.setCancelled(true);
                    player.sendMessage("§c§lNotice: §r§cCannot place items in locked slots.");
                    break;
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        List<Integer> lockedSlots = inventoryUpgrade.getLockedSlots(player);
        ItemStack pickupItem = event.getItem().getItemStack();

        // Check if inventory is full (only considering unlocked slots)
        boolean canPickup = false;

        // Check if can stack with existing items
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            if (!lockedSlots.contains(i)) {
                ItemStack existingItem = player.getInventory().getItem(i);
                if (existingItem == null) {
                    canPickup = true;
                    break;
                } else if (existingItem.isSimilar(pickupItem) &&
                        existingItem.getAmount() < existingItem.getMaxStackSize()) {
                    canPickup = true;
                    break;
                }
            }
        }

        if (!canPickup) {
            event.setCancelled(true);
            player.sendMessage("§c§lNotice: §r§cNot enough space in inventory. Unlock more at §f/shop");
        }
    }
}