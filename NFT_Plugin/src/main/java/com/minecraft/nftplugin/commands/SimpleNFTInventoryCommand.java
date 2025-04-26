package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.storage.SimpleNFTInventory;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

/**
 * Command to display a simple NFT inventory
 */
public class SimpleNFTInventoryCommand implements CommandExecutor, Listener {

    private final NFTPlugin plugin;
    private final SimpleNFTInventory storage;
    
    public static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "NFT Inventory";
    public static final int INVENTORY_SIZE = 54; // 6 rows of 9 slots

    public SimpleNFTInventoryCommand(NFTPlugin plugin, SimpleNFTInventory storage) {
        this.plugin = plugin;
        this.storage = storage;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        openNFTInventory(player);
        return true;
    }

    /**
     * Open the NFT inventory for a player
     * @param player The player
     */
    private void openNFTInventory(Player player) {
        // Create inventory with 54 slots (6 rows)
        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, INVENTORY_TITLE);

        // Load NFTs from storage
        Map<Integer, ItemStack> items = storage.loadInventory(player);

        // Add items to inventory
        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            int slot = entry.getKey();
            ItemStack item = entry.getValue();
            
            if (slot >= 0 && slot < INVENTORY_SIZE) {
                inventory.setItem(slot, item);
            }
        }

        // Open inventory for player
        player.openInventory(inventory);
        plugin.getLogger().info("Opened NFT inventory for " + player.getName() + " with " + items.size() + " items");
    }

    /**
     * Handle inventory click events
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if the clicked inventory is an NFT inventory
        if (title.equals(INVENTORY_TITLE)) {
            // Handle different click types
            switch (event.getAction()) {
                case PICKUP_ALL:
                case PICKUP_HALF:
                case PICKUP_ONE:
                case PICKUP_SOME:
                case PLACE_ALL:
                case PLACE_ONE:
                case PLACE_SOME:
                case SWAP_WITH_CURSOR:
                case DROP_ONE_SLOT:
                case DROP_ALL_SLOT:
                case HOTBAR_SWAP:
                    // Regular clicks - check if in NFT inventory
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                        // If clicking in the NFT inventory
                        ItemStack clickedItem = event.getCurrentItem();
                        ItemStack cursorItem = event.getCursor();
                        
                        // If taking an item out, always allow
                        if (clickedItem != null && clickedItem.getType() != Material.AIR && 
                            (cursorItem == null || cursorItem.getType() == Material.AIR)) {
                            // Taking an item out - always allow
                            return;
                        }
                        
                        // If placing an item in, check if it's an NFT
                        if (cursorItem != null && cursorItem.getType() != Material.AIR) {
                            if (!plugin.getItemManager().isNftItem(cursorItem)) {
                                // Not an NFT item, cancel the event
                                event.setCancelled(true);
                                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                                    ChatColor.RED + "Only NFT items can be placed in this inventory.");
                            }
                        }
                    }
                    break;
                    
                case MOVE_TO_OTHER_INVENTORY: // This is SHIFT+CLICK
                    // If shift-clicking from player inventory to NFT inventory
                    if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                        ItemStack clickedItem = event.getCurrentItem();
                        if (clickedItem != null && clickedItem.getType() != Material.AIR) {
                            // Only allow NFT items to be shift-clicked into NFT inventory
                            if (!plugin.getItemManager().isNftItem(clickedItem)) {
                                event.setCancelled(true);
                                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                                    ChatColor.RED + "Only NFT items can be placed in this inventory.");
                            }
                        }
                    }
                    // If shift-clicking from NFT inventory to player inventory, always allow
                    break;
                    
                default:
                    // For other actions, we'll just let them happen
                    break;
            }
            
            // Log the action for debugging
            plugin.getLogger().info("Inventory click in NFT inventory: " + event.getAction() + 
                " by " + player.getName() + ", cancelled: " + event.isCancelled());
        }
    }

    /**
     * Handle inventory close events
     */
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }

        String title = event.getView().getTitle();
        if (title.equals(INVENTORY_TITLE)) {
            // Save the inventory contents
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            plugin.getLogger().info("Saving NFT inventory for " + player.getName());

            // First clear the storage
            storage.clearInventory(player);

            // Then save all items
            for (int slot = 0; slot < INVENTORY_SIZE; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (plugin.getItemManager().isNftItem(item)) {
                        // Save the item
                        storage.setItem(player, slot, item);
                        
                        // Log for debugging
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            String nftId = container.get(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING);
                            plugin.getLogger().info("Saved NFT " + nftId + " to slot " + slot);
                        }
                    } else {
                        plugin.getLogger().warning("Found non-NFT item in slot " + slot + ": " + item.getType().name());
                    }
                }
            }

            // Save to file
            storage.saveInventory(player);
            plugin.getLogger().info("Finished saving NFT inventory for " + player.getName());
        }
    }
}
