package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.commands.SimpleNFTInventoryCommand;
import com.minecraft.nftplugin.commands.NFTInvCommand;
import org.bukkit.Material;
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
     * Allow players to move NFT items only within their inventory or to NFT inventory
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack cursorItem = event.getCursor();
        ItemStack currentItem = event.getCurrentItem();
        String inventoryTitle = event.getView().getTitle();
        boolean isNftInventory = inventoryTitle.equals(SimpleNFTInventoryCommand.INVENTORY_TITLE) || inventoryTitle.startsWith(NFTInvCommand.INVENTORY_TITLE_PREFIX);

        // Handle shift-click separately
        if (event.isShiftClick()) {
            handleShiftClick(event, player, isNftInventory);
            return;
        }

        // Check if the cursor item is an NFT item
        if (cursorItem != null && plugin.getItemManager().isNftItem(cursorItem)) {
            // Allow placing into NFT inventory
            if (isNftInventory) {
                // This is allowed - placing NFT in NFT inventory
                return;
            }

            // Allow moving within player inventory
            if (event.getClickedInventory() != null &&
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // This is allowed - moving within player inventory
                return;
            }

            // Prevent placing into any other inventory
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou can only place NFT items in your inventory or NFT inventory!");
            return;
        }

        // Check if the current item is an NFT item
        if (currentItem != null && plugin.getItemManager().isNftItem(currentItem)) {
            // Allow taking from NFT inventory
            if (isNftInventory || inventoryTitle.startsWith(NFTInvCommand.INVENTORY_TITLE_PREFIX)) {
                // This is allowed - taking NFT from NFT inventory
                return;
            }

            // Allow moving within player inventory
            if (event.getClickedInventory() != null &&
                event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // This is allowed - moving within player inventory
                return;
            }

            // Prevent all other movements
            event.setCancelled(true);
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou can only place NFT items in your inventory or NFT inventory!");
        }
    }

    /**
     * Handle shift-click events specifically
     */
    private void handleShiftClick(InventoryClickEvent event, Player player, boolean isNftInventory) {
        ItemStack currentItem = event.getCurrentItem();
        if (currentItem == null || currentItem.getType() == Material.AIR) {
            return; // Nothing to shift-click
        }

        // If it's an NFT item
        if (plugin.getItemManager().isNftItem(currentItem)) {
            // If clicking in player inventory
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // Only allow shift-clicking to NFT inventory
                if (!isNftInventory) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou can only place NFT items in your inventory or NFT inventory!");
                    plugin.getLogger().info("Blocked shift-click of NFT from player inventory to non-NFT inventory");
                }
            }
            // If clicking in any other inventory
            else if (event.getClickedInventory() != null && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                // If not in NFT inventory, block the shift-click
                if (!isNftInventory) {
                    event.setCancelled(true);
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou can only place NFT items in your inventory or NFT inventory!");
                    plugin.getLogger().info("Blocked shift-click of NFT from non-NFT inventory");
                }
            }
        }
    }

    /**
     * Allow players to drag NFT items only to NFT inventory
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        ItemStack item = event.getOldCursor();
        String inventoryTitle = event.getView().getTitle();
        boolean isNftInventory = inventoryTitle.equals(SimpleNFTInventoryCommand.INVENTORY_TITLE) || inventoryTitle.startsWith(NFTInvCommand.INVENTORY_TITLE_PREFIX);

        if (plugin.getItemManager().isNftItem(item)) {
            // Check if any of the slots are in the top inventory
            boolean draggedToTop = event.getRawSlots().stream()
                    .anyMatch(slot -> slot < event.getView().getTopInventory().getSize());

            if (draggedToTop && !isNftInventory) {
                event.setCancelled(true);
                Player player = (Player) event.getWhoClicked();
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou can only place NFT items in your inventory or NFT inventory!");
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
            // Prevent all automated movement of NFT items
            event.setCancelled(true);
        }
    }
}
