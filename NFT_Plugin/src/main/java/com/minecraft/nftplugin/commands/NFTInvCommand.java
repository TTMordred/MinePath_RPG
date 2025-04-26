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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Command to display a paginated NFT inventory
 */
public class NFTInvCommand implements CommandExecutor, Listener {

    private final NFTPlugin plugin;
    private final SimpleNFTInventory storage;

    public static final String INVENTORY_TITLE_PREFIX = ChatColor.DARK_PURPLE + "NFT Inventory";
    public static final int INVENTORY_SIZE = 54; // 6 rows of 9 slots
    public static final int ITEMS_PER_PAGE = 45; // 5 rows of 9 slots (bottom row for navigation)

    // Map to track which page each player is viewing
    private final Map<String, Integer> playerPages = new HashMap<>();

    // Navigation item slots
    private static final int PREV_PAGE_SLOT = 45;
    private static final int CURRENT_PAGE_SLOT = 49;
    private static final int NEXT_PAGE_SLOT = 53;

    public NFTInvCommand(NFTPlugin plugin, SimpleNFTInventory storage) {
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
        int page = 1; // Default to first page

        // Check if a page number was specified
        if (args.length > 0) {
            try {
                page = Integer.parseInt(args[0]);
                if (page < 1) {
                    page = 1;
                }
            } catch (NumberFormatException e) {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Invalid page number. Using page 1.");
            }
        }

        // Store the current page for this player
        playerPages.put(player.getName(), page);

        // Open the inventory for the specified page
        openNFTInventory(player, page);
        return true;
    }

    /**
     * Open the NFT inventory for a player at the specified page
     * @param player The player
     * @param page The page number (1-based)
     */
    private void openNFTInventory(Player player, int page) {
        // Create inventory with title showing the page number
        String title = INVENTORY_TITLE_PREFIX + " - Page " + page;
        Inventory inventory = Bukkit.createInventory(player, INVENTORY_SIZE, title);

        // Load NFTs from storage
        Map<Integer, ItemStack> allItems = storage.loadInventory(player);

        // Calculate start index for this page
        int startIndex = (page - 1) * ITEMS_PER_PAGE;

        // Add items for this page
        int slot = 0;
        int itemsAdded = 0;
        for (Map.Entry<Integer, ItemStack> entry : allItems.entrySet()) {
            // Skip items that don't belong on this page
            if (itemsAdded < startIndex) {
                itemsAdded++;
                continue;
            }

            // Stop if we've reached the end of this page
            if (slot >= ITEMS_PER_PAGE) {
                break;
            }

            ItemStack item = entry.getValue();
            inventory.setItem(slot, item);
            slot++;
            itemsAdded++;

            // Stop if we've reached the end of all items
            if (itemsAdded >= allItems.size()) {
                break;
            }
        }

        // Add navigation items
        addNavigationItems(inventory, page, allItems.size());

        // Open inventory for player
        player.openInventory(inventory);
        plugin.getLogger().info("Opened NFT inventory page " + page + " for " + player.getName() + " with " + slot + " items");
    }

    /**
     * Add navigation items to the inventory
     * @param inventory The inventory
     * @param currentPage The current page number
     * @param totalItems The total number of items
     */
    private void addNavigationItems(Inventory inventory, int currentPage, int totalItems) {
        int totalPages = (int) Math.ceil((double) totalItems / ITEMS_PER_PAGE);
        if (totalPages < 1) totalPages = 1;

        // Previous page button (if not on first page)
        if (currentPage > 1) {
            ItemStack prevButton = new ItemStack(Material.ARROW);
            ItemMeta meta = prevButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + (currentPage - 1));
                meta.setLore(lore);
                prevButton.setItemMeta(meta);
            }
            inventory.setItem(PREV_PAGE_SLOT, prevButton);
        }

        // Current page indicator
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        ItemMeta meta = pageIndicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Page " + currentPage + " of " + totalPages);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Total items: " + totalItems);
            meta.setLore(lore);
            pageIndicator.setItemMeta(meta);
        }
        inventory.setItem(CURRENT_PAGE_SLOT, pageIndicator);

        // Next page button (if not on last page)
        if (currentPage < totalPages) {
            ItemStack nextButton = new ItemStack(Material.ARROW);
            meta = nextButton.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + "Next Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + (currentPage + 1));
                meta.setLore(lore);
                nextButton.setItemMeta(meta);
            }
            inventory.setItem(NEXT_PAGE_SLOT, nextButton);
        }
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
        if (title.startsWith(INVENTORY_TITLE_PREFIX)) {
            // Handle navigation clicks
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.CHEST) {
                int slot = event.getSlot();

                // Check if clicked on navigation buttons
                if (slot == PREV_PAGE_SLOT || slot == NEXT_PAGE_SLOT) {
                    event.setCancelled(true); // Cancel the click event

                    // Get current page
                    int currentPage = playerPages.getOrDefault(player.getName(), 1);

                    // Calculate new page
                    int newPage = currentPage;
                    if (slot == PREV_PAGE_SLOT && currentPage > 1) {
                        newPage = currentPage - 1;
                    } else if (slot == NEXT_PAGE_SLOT) {
                        newPage = currentPage + 1;
                    }

                    // Only change page if it's different
                    if (newPage != currentPage) {
                        playerPages.put(player.getName(), newPage);
                        player.closeInventory(); // Close current inventory
                        openNFTInventory(player, newPage); // Open new page
                    }
                    return;
                }

                // If clicking in the bottom row (navigation row)
                if (slot >= 45) {
                    event.setCancelled(true);
                    return;
                }

                // Regular clicks in the item area
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
            } else if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                // If shift-clicking from player inventory to NFT inventory
                if (event.isShiftClick()) {
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
        if (title.startsWith(INVENTORY_TITLE_PREFIX)) {
            // Save the inventory contents
            Player player = (Player) event.getPlayer();
            Inventory inventory = event.getInventory();

            plugin.getLogger().info("Saving NFT inventory for " + player.getName());

            // Get current page
            int currentPage = playerPages.getOrDefault(player.getName(), 1);
            int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;

            // First clear the storage for this page
            Map<Integer, ItemStack> existingItems = storage.loadInventory(player);
            for (int i = 0; i < ITEMS_PER_PAGE; i++) {
                int globalIndex = startIndex + i;
                if (existingItems.containsKey(globalIndex)) {
                    storage.setItem(player, globalIndex, null); // Remove item at this index
                }
            }

            // Then save all items from this page
            for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
                ItemStack item = inventory.getItem(slot);
                if (item != null && item.getType() != Material.AIR) {
                    if (plugin.getItemManager().isNftItem(item)) {
                        // Calculate global index for this slot
                        int globalIndex = startIndex + slot;

                        // Save the item
                        storage.setItem(player, globalIndex, item);

                        // Log for debugging
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            PersistentDataContainer container = meta.getPersistentDataContainer();
                            String nftId = container.get(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING);
                            plugin.getLogger().info("Saved NFT " + nftId + " to global index " + globalIndex);
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
