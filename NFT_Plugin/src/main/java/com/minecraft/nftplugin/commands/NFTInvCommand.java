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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

        // First, ensure the inventory is compacted to prevent duplication
        compactPlayerInventory(player);

        // Find the first non-full page or create a new one
        int page = findFirstAvailablePage(player);

        // Store the current page for this player
        playerPages.put(player.getName(), page);

        // Open the inventory for the determined page
        openNFTInventory(player, page);
        return true;
    }

    /**
     * Find the first page that has available slots or return the first page
     * @param player The player
     * @return The page number to open
     */
    private int findFirstAvailablePage(Player player) {
        // Load player's inventory
        Map<Integer, ItemStack> allItems = storage.loadInventory(player);

        if (allItems.isEmpty()) {
            return 1; // If no items, return first page
        }

        // Find the highest page with items
        int highestPage = 0;

        for (int itemIndex : allItems.keySet()) {
            int pageOfItem = itemIndex / ITEMS_PER_PAGE + 1;
            if (pageOfItem > highestPage) {
                highestPage = pageOfItem;
            }
        }

        // Count items per page
        Map<Integer, Integer> itemsPerPage = new HashMap<>();

        for (int itemIndex : allItems.keySet()) {
            int pageOfItem = itemIndex / ITEMS_PER_PAGE + 1;
            itemsPerPage.put(pageOfItem, itemsPerPage.getOrDefault(pageOfItem, 0) + 1);
        }

        // Check each page starting from 1 up to the highest page
        for (int page = 1; page <= highestPage; page++) {
            int itemCount = itemsPerPage.getOrDefault(page, 0);

            // If this page has space, return it
            if (itemCount < ITEMS_PER_PAGE) {
                return page;
            }
        }

        // If all existing pages are full, return the first page
        // This will force the player to fill pages in order
        return 1;
    }

    /**
     * Check for and remove duplicate NFTs from a player's inventory
     * @param player The player
     */
    private void compactPlayerInventory(Player player) {
        // Load the player's inventory
        Map<Integer, ItemStack> items = storage.loadInventory(player);

        // Check for duplicate items (same NFT ID in multiple slots)
        Map<String, Integer> nftIdToSlot = new HashMap<>();
        Set<Integer> slotsToRemove = new HashSet<>();

        for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
            ItemStack item = entry.getValue();
            int slot = entry.getKey();

            if (item != null && plugin.getItemManager().isNftItem(item)) {
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    String nftId = container.get(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING);

                    if (nftId != null) {
                        if (nftIdToSlot.containsKey(nftId)) {
                            // Found a duplicate NFT
                            slotsToRemove.add(slot);
                            plugin.getLogger().warning("Found duplicate NFT " + nftId + " in slot " + slot +
                                " (already in slot " + nftIdToSlot.get(nftId) + ")");
                        } else {
                            nftIdToSlot.put(nftId, slot);
                        }
                    }
                }
            }
        }

        // Remove duplicate items
        if (!slotsToRemove.isEmpty()) {
            for (int slot : slotsToRemove) {
                items.remove(slot);
            }

            // Clear the storage
            storage.clearInventory(player);

            // Save the cleaned items
            for (Map.Entry<Integer, ItemStack> entry : items.entrySet()) {
                storage.setItem(player, entry.getKey(), entry.getValue());
            }

            // Save to file
            storage.saveInventory(player);
            plugin.getLogger().info("Removed " + slotsToRemove.size() + " duplicate NFTs from inventory for " + player.getName());
        }
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

        // Count items by page to calculate total pages
        Map<Integer, Integer> itemsPerPage = new HashMap<>();
        for (int itemIndex : allItems.keySet()) {
            int pageOfItem = itemIndex / ITEMS_PER_PAGE + 1;
            itemsPerPage.put(pageOfItem, itemsPerPage.getOrDefault(pageOfItem, 0) + 1);
        }

        // Add items for this page
        int slot = 0;

        // Find items for this specific page
        for (Map.Entry<Integer, ItemStack> entry : allItems.entrySet()) {
            int itemIndex = entry.getKey();
            int itemPage = itemIndex / ITEMS_PER_PAGE + 1;

            // Only add items from the current page
            if (itemPage == page) {
                // Calculate the slot within this page
                int pageSlot = itemIndex % ITEMS_PER_PAGE;

                // Add item to inventory
                inventory.setItem(pageSlot, entry.getValue());
                slot++;
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
        // Check if the current page is full (has all 45 slots filled)
        boolean isPageFull = true;
        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null || item.getType() == Material.AIR) {
                isPageFull = false;
                break;
            }
        }

        // Previous page button
        ItemStack prevButton = new ItemStack(Material.ARROW);
        ItemMeta meta = prevButton.getItemMeta();
        if (meta != null) {
            if (currentPage > 1) {
                // Active previous button
                meta.setDisplayName(ChatColor.YELLOW + "Previous Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + (currentPage - 1));
                meta.setLore(lore);
            } else {
                // Disabled previous button
                meta.setDisplayName(ChatColor.GRAY + "Previous Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "You are on the first page");
                meta.setLore(lore);
            }
            prevButton.setItemMeta(meta);
        }
        inventory.setItem(PREV_PAGE_SLOT, prevButton);

        // Current page indicator
        ItemStack pageIndicator = new ItemStack(Material.PAPER);
        meta = pageIndicator.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GREEN + "Page " + currentPage);
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Total items: " + totalItems);
            meta.setLore(lore);
            pageIndicator.setItemMeta(meta);
        }
        inventory.setItem(CURRENT_PAGE_SLOT, pageIndicator);

        // Next page button - only active if current page is full
        ItemStack nextButton = new ItemStack(Material.ARROW);
        meta = nextButton.getItemMeta();
        if (meta != null) {
            if (isPageFull) {
                // Active next button
                meta.setDisplayName(ChatColor.YELLOW + "Next Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + (currentPage + 1));
                meta.setLore(lore);
            } else {
                // Disabled next button
                meta.setDisplayName(ChatColor.GRAY + "Next Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Fill this page before going to the next");
                meta.setLore(lore);
            }
            nextButton.setItemMeta(meta);
        }
        inventory.setItem(NEXT_PAGE_SLOT, nextButton);
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

                    // Get the clicked item to verify it's a navigation button
                    ItemStack clickedItem = event.getCurrentItem();
                    if (clickedItem == null || clickedItem.getType() != Material.ARROW) {
                        return;
                    }

                    // Get current page from the title
                    int currentPage = 1;
                    try {
                        // Extract page number from title (format: "NFT Inventory - Page X")
                        String pageStr = title.substring(title.lastIndexOf("Page") + 5).trim();
                        currentPage = Integer.parseInt(pageStr);
                    } catch (Exception e) {
                        plugin.getLogger().warning("Could not parse page number from title: " + title);
                    }

                    // Check if the current page is full (has all 45 slots filled)
                    boolean isPageFull = true;
                    for (int i = 0; i < ITEMS_PER_PAGE; i++) {
                        ItemStack item = event.getInventory().getItem(i);
                        if (item == null || item.getType() == Material.AIR) {
                            isPageFull = false;
                            break;
                        }
                    }

                    // Calculate new page based on which button was clicked
                    final int newPage;
                    if (slot == PREV_PAGE_SLOT && currentPage > 1) {
                        // Previous page button - go back one page
                        newPage = currentPage - 1;
                    } else if (slot == NEXT_PAGE_SLOT && isPageFull) {
                        // Next page button - go forward one page only if current page is full
                        newPage = currentPage + 1;
                    } else {
                        // No change
                        newPage = currentPage;

                        // If trying to go to next page but it's not full, show a message
                        if (slot == NEXT_PAGE_SLOT && !isPageFull) {
                            player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                                ChatColor.YELLOW + "You need to fill this page before going to the next one.");
                        }
                    }

                    // Only change page if it's different and valid
                    if (newPage != currentPage && newPage >= 1) {
                        // Update player's current page
                        playerPages.put(player.getName(), newPage);

                        // Save the current inventory first to ensure items aren't lost
                        saveInventoryContents(player, event.getInventory(), currentPage);

                        // Open new page
                        player.closeInventory(); // Close current inventory
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            openNFTInventory(player, newPage); // Open new page
                        }, 2L); // Slight delay to ensure inventory is closed and saved first
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
     * Save the contents of an inventory to the player's storage
     * @param player The player
     * @param inventory The inventory to save
     * @param currentPage The current page number
     */
    private void saveInventoryContents(Player player, Inventory inventory, int currentPage) {
        // Calculate start index for this page
        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;

        // Load existing items
        Map<Integer, ItemStack> existingItems = storage.loadInventory(player);

        // Create a map to track which slots in the current page have items
        boolean[] slotHasItem = new boolean[ITEMS_PER_PAGE];

        // First, mark which slots in the current page have items in the inventory
        for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
            ItemStack item = inventory.getItem(slot);
            if (item != null && item.getType() != Material.AIR && plugin.getItemManager().isNftItem(item)) {
                slotHasItem[slot] = true;

                // Add or update the item in the global inventory
                int globalIndex = startIndex + slot;
                existingItems.put(globalIndex, item.clone());

                // Log for debugging
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    PersistentDataContainer container = meta.getPersistentDataContainer();
                    String nftId = container.get(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING);
                    plugin.getLogger().info("Saved NFT " + nftId + " to global index " + globalIndex);
                }
            }
        }

        // Now remove items from slots that are empty in the current page
        for (int slot = 0; slot < ITEMS_PER_PAGE; slot++) {
            if (!slotHasItem[slot]) {
                int globalIndex = startIndex + slot;
                if (existingItems.containsKey(globalIndex)) {
                    existingItems.remove(globalIndex);
                    plugin.getLogger().info("Removed item from global index " + globalIndex + " (empty slot)");
                }
            }
        }

        // Save all items back to storage - DO NOT CLEAR FIRST to prevent data loss
        for (Map.Entry<Integer, ItemStack> entry : existingItems.entrySet()) {
            storage.setItem(player, entry.getKey(), entry.getValue());
        }

        // Save to file
        storage.saveInventory(player);
        plugin.getLogger().info("Saved inventory contents for " + player.getName() + " with " + existingItems.size() + " items");
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

            // Get current page from the title
            int currentPage = 1;
            try {
                // Extract page number from title (format: "NFT Inventory - Page X")
                String pageStr = title.substring(title.lastIndexOf("Page") + 5).trim();
                currentPage = Integer.parseInt(pageStr);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not parse page number from title: " + title);
            }

            // Save the inventory contents
            saveInventoryContents(player, inventory, currentPage);
        }
    }
}
