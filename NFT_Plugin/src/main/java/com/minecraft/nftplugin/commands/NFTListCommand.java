package com.minecraft.nftplugin.commands;

import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.database.NFTData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Command to display a list of NFTs owned by a player
 */
public class NFTListCommand implements CommandExecutor, Listener {

    private final NFTPlugin plugin;
    private final Map<String, Integer> playerPages = new HashMap<>();
    private final Map<UUID, List<NFTData>> playerNFTs = new HashMap<>();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private static final int ITEMS_PER_PAGE = 45; // 9x5 grid, leaving bottom row for navigation
    private static final String INVENTORY_TITLE = ChatColor.DARK_PURPLE + "✨ Your NFT Collection ✨";

    // Navigation item types
    private static final Material PREV_PAGE_MATERIAL = Material.SPECTRAL_ARROW;
    private static final Material NEXT_PAGE_MATERIAL = Material.SPECTRAL_ARROW;
    private static final Material INFO_MATERIAL = Material.BOOK;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public NFTListCommand(NFTPlugin plugin) {
        this.plugin = plugin;
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Get player's NFTs from database
        List<NFTData> nfts = plugin.getDatabaseManager().getPlayerNFTs(player.getUniqueId());

        if (nfts.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + "You don't have any NFTs yet.");
            return true;
        }

        // Store NFTs for this player
        playerNFTs.put(player.getUniqueId(), nfts);

        // Set current page to 0 (first page)
        playerPages.put(player.getName(), 0);

        // Open the first page of the inventory
        openInventoryPage(player, 0);

        return true;
    }

    /**
     * Open a specific page of the NFT inventory for a player
     * @param player The player
     * @param page The page number (0-based)
     */
    private void openInventoryPage(Player player, int page) {
        List<NFTData> nfts = playerNFTs.get(player.getUniqueId());
        if (nfts == null || nfts.isEmpty()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + "You don't have any NFTs yet.");
            return;
        }

        // Calculate total pages
        int totalPages = (int) Math.ceil((double) nfts.size() / ITEMS_PER_PAGE);

        // Validate page number
        if (page < 0) {
            page = 0;
        } else if (page >= totalPages) {
            page = totalPages - 1;
        }

        // Update player's current page
        playerPages.put(player.getName(), page);

        // Create inventory with 54 slots (6 rows)
        Inventory inventory = Bukkit.createInventory(player, 54, INVENTORY_TITLE + " - Page " + (page + 1) + "/" + totalPages);

        // Calculate start and end indices for this page
        int startIndex = page * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, nfts.size());

        // Add NFT items to inventory
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            NFTData nft = nfts.get(i);

            // Create NFT item
            ItemStack item = createNFTItem(nft);

            // Add to inventory
            inventory.setItem(slot, item);
            slot++;
        }

        // Add navigation buttons in the bottom row
        addNavigationButtons(inventory, page, totalPages);

        // Open inventory for player
        player.openInventory(inventory);
    }

    /**
     * Create an item representing an NFT
     * @param nft The NFT data
     * @return The item stack
     */
    private ItemStack createNFTItem(NFTData nft) {
        // Get achievement details
        String achievementKey = nft.getAchievementKey();
        String achievementName = getFormattedAchievementName(achievementKey);
        String description = plugin.getConfigManager().getNftDescription(achievementKey);

        // Try to get material from metadata
        Material material = getMaterialFromMetadata(achievementKey);
        if (material == null) {
            // Fallback to default material
            material = plugin.getConfigManager().getNftItemMaterial();
        }

        // Create item with enchanted effect for better visibility
        ItemStack item = new ItemStack(material);
        item.addUnsafeEnchantment(org.bukkit.enchantments.Enchantment.DURABILITY, 1);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            // Set name with NFT tag
            meta.setDisplayName(ChatColor.GOLD + achievementName + ChatColor.GREEN + " ✨");

            // Hide enchantments
            meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);

            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.AQUA + "✦ NFT Item ✦");
            lore.add("");

            // Add achievement info - most important information
            lore.add(ChatColor.GOLD + achievementName);

            // Add minimal description if available
            if (description != null && !description.isEmpty()) {
                // Limit description length and add ellipsis if too long
                if (description.length() > 30) {
                    description = description.substring(0, 27) + "...";
                }
                lore.add(ChatColor.GRAY + description);
            }

            // Add date received in compact format
            if (nft.getMintedAt() != null) {
                // Use shorter date format
                SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM/yy");
                lore.add(ChatColor.GRAY + "Received: " + ChatColor.WHITE + shortDateFormat.format(nft.getMintedAt()));
            }

            // Store NFT ID in persistent data but don't show it in lore

            // Add view instructions with better formatting
            lore.add("");
            lore.add(ChatColor.YELLOW + "→ Click for details");

            meta.setLore(lore);

            // Store NFT ID in item's persistent data container
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING, nft.getNftId());

            item.setItemMeta(meta);
        }

        return item;
    }

    /**
     * Get the material for an NFT from its metadata
     * @param achievementKey The achievement key
     * @return The material, or null if not found
     */
    private Material getMaterialFromMetadata(String achievementKey) {
        // Try to get material from metadata
        JsonObject metadata = plugin.getMetadataManager().getMetadata(achievementKey);
        if (metadata != null) {
            // Check if metadata has reward section with item type
            if (metadata.has("quest") && metadata.getAsJsonObject("quest").has("reward") &&
                metadata.getAsJsonObject("quest").getAsJsonObject("reward").has("item")) {

                String materialName = metadata.getAsJsonObject("quest")
                        .getAsJsonObject("reward")
                        .get("item").getAsString();

                try {
                    return Material.valueOf(materialName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material name in metadata: " + materialName);
                }
            }

            // Check if metadata has target material (for HOLD_NAMED_ITEM_INSTANT achievements)
            if (metadata.has("quest") && metadata.getAsJsonObject("quest").has("target")) {
                String materialName = metadata.getAsJsonObject("quest").get("target").getAsString();
                try {
                    return Material.valueOf(materialName);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid target material in metadata: " + materialName);
                }
            }
        }

        return null;
    }

    /**
     * Add navigation buttons to the inventory
     * @param inventory The inventory
     * @param currentPage The current page number
     * @param totalPages The total number of pages
     */
    private void addNavigationButtons(Inventory inventory, int currentPage, int totalPages) {
        // Fill bottom row with glass panes for better UI
        ItemStack glassPaneBorder = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta glassMeta = glassPaneBorder.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPaneBorder.setItemMeta(glassMeta);
        }

        // Add glass panes to bottom row
        for (int i = 45; i < 54; i++) {
            if (i != 45 && i != 49 && i != 53) { // Skip button positions
                inventory.setItem(i, glassPaneBorder);
            }
        }

        // Previous page button (slot 45)
        if (currentPage > 0) {
            ItemStack prevButton = new ItemStack(PREV_PAGE_MATERIAL);
            ItemMeta prevMeta = prevButton.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.GREEN + "◀ Previous Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + currentPage);
                lore.add(ChatColor.YELLOW + "« Page " + currentPage + " »");
                prevMeta.setLore(lore);
                prevButton.setItemMeta(prevMeta);
            }
            inventory.setItem(45, prevButton);
        } else {
            // Disabled previous button
            ItemStack disabledButton = new ItemStack(Material.ARROW);
            ItemMeta disabledMeta = disabledButton.getItemMeta();
            if (disabledMeta != null) {
                disabledMeta.setDisplayName(ChatColor.GRAY + "Previous Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "You are on the first page");
                disabledMeta.setLore(lore);
                disabledButton.setItemMeta(disabledMeta);
            }
            inventory.setItem(45, disabledButton);
        }

        // Info button (slot 49)
        ItemStack infoButton = new ItemStack(INFO_MATERIAL);
        ItemMeta infoMeta = infoButton.getItemMeta();
        if (infoMeta != null) {
            infoMeta.setDisplayName(ChatColor.AQUA + "ℹ Collection Information");
            List<String> lore = new ArrayList<>();
            lore.add(ChatColor.GRAY + "Current Page: " + ChatColor.WHITE + (currentPage + 1));
            lore.add(ChatColor.GRAY + "Total Pages: " + ChatColor.WHITE + totalPages);

            // Get total NFTs count from the owner of the inventory
            int totalNFTs = 0;
            if (inventory.getHolder() instanceof Player) {
                Player owner = (Player) inventory.getHolder();
                List<NFTData> nfts = playerNFTs.get(owner.getUniqueId());
                if (nfts != null) {
                    totalNFTs = nfts.size();
                }
            } else if (!inventory.getViewers().isEmpty()) {
                // Fallback to first viewer if holder is not a player
                UUID viewerUUID = inventory.getViewers().get(0).getUniqueId();
                List<NFTData> nfts = playerNFTs.get(viewerUUID);
                if (nfts != null) {
                    totalNFTs = nfts.size();
                }
            }

            lore.add(ChatColor.GRAY + "Total NFTs: " + ChatColor.GOLD + totalNFTs);
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click on an NFT to view details");
            infoMeta.setLore(lore);
            infoButton.setItemMeta(infoMeta);
        }
        inventory.setItem(49, infoButton);

        // Next page button (slot 53)
        if (currentPage < totalPages - 1) {
            ItemStack nextButton = new ItemStack(NEXT_PAGE_MATERIAL);
            ItemMeta nextMeta = nextButton.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.GREEN + "Next Page ▶");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Click to go to page " + (currentPage + 2));
                lore.add(ChatColor.YELLOW + "« Page " + (currentPage + 2) + " »");
                nextMeta.setLore(lore);
                nextButton.setItemMeta(nextMeta);
            }
            inventory.setItem(53, nextButton);
        } else {
            // Disabled next button
            ItemStack disabledButton = new ItemStack(Material.ARROW);
            ItemMeta disabledMeta = disabledButton.getItemMeta();
            if (disabledMeta != null) {
                disabledMeta.setDisplayName(ChatColor.GRAY + "Next Page");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "You are on the last page");
                disabledMeta.setLore(lore);
                disabledButton.setItemMeta(disabledMeta);
            }
            inventory.setItem(53, disabledButton);
        }
    }

    /**
     * Handle inventory click events
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();

        // Check if the clicked inventory is an NFT list inventory
        if (title.startsWith(INVENTORY_TITLE)) {
            event.setCancelled(true); // Prevent taking items

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType() == Material.AIR) {
                return;
            }

            // Get current page
            Integer currentPage = playerPages.get(player.getName());
            if (currentPage == null) {
                currentPage = 0;
            }

            // Handle navigation buttons
            if (event.getSlot() == 45 && clickedItem.getType() == PREV_PAGE_MATERIAL) {
                // Previous page button
                openInventoryPage(player, currentPage - 1);
                return;
            } else if (event.getSlot() == 53 && clickedItem.getType() == NEXT_PAGE_MATERIAL) {
                // Next page button
                openInventoryPage(player, currentPage + 1);
                return;
            } else if (event.getSlot() == 49 && clickedItem.getType() == INFO_MATERIAL) {
                // Info button - do nothing
                return;
            }

            // Handle click on NFT item
            ItemMeta meta = clickedItem.getItemMeta();
            if (meta != null) {
                // Get NFT ID from persistent data container
                PersistentDataContainer container = meta.getPersistentDataContainer();
                String nftId = container.get(plugin.getItemManager().getNftIdKey(), PersistentDataType.STRING);

                if (nftId != null) {
                    // Display detailed NFT information
                    player.closeInventory();
                    displayNFTDetails(player, nftId);
                } else {
                    // Since we no longer store NFT ID in lore, just inform the player
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Could not retrieve NFT information from this item.");
                    player.closeInventory();
                }
            }
        }
    }

    /**
     * Display detailed information about an NFT
     * @param player The player
     * @param nftId The NFT ID
     */
    private void displayNFTDetails(Player player, String nftId) {
        // Get NFT data from database
        NFTData nft = plugin.getDatabaseManager().getNFTByNftId(nftId);

        if (nft == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "NFT information not found.");
            return;
        }

        // Get achievement details
        String achievementName = getFormattedAchievementName(nft.getAchievementKey());
        String description = plugin.getConfigManager().getNftDescription(nft.getAchievementKey());

        // Display detailed NFT information with improved formatting
        player.sendMessage("§8§m-----------------------------------------------------");
        player.sendMessage("§e§lNFT Details: §r§6" + achievementName);
        player.sendMessage("§8§m-----------------------------------------------------");

        // Item name with NFT tag
        player.sendMessage("§7Item: §b" + achievementName + " §a<NFT Item>");

        // Description with better formatting
        if (description != null && !description.isEmpty()) {
            player.sendMessage("§7Description: §f" + description);
        }

        // Transaction ID with extremely shortened display
        String shortNftId = nft.getNftId();
        if (shortNftId.length() > 12) {
            shortNftId = shortNftId.substring(0, 4) + "..." + shortNftId.substring(shortNftId.length() - 4);
        }
        player.sendMessage("§7NFT ID: §f" + shortNftId);

        // Transaction ID with extremely shortened display
        String transactionId = nft.getTransactionId();
        if (transactionId != null && transactionId.length() > 12) {
            transactionId = transactionId.substring(0, 4) + "..." + transactionId.substring(transactionId.length() - 4);
        }
        player.sendMessage("§7TX ID: §f" + (transactionId != null ? transactionId : "N/A"));

        // Achievement with colored text
        player.sendMessage("§7Achievement: §6" + achievementName);

        // Network with colored text
        player.sendMessage("§7Network: §3Solana DevNet");

        // Display date received with better formatting
        if (nft.getMintedAt() != null) {
            player.sendMessage("§7Date Received: §f" + dateFormat.format(nft.getMintedAt()));
        }

        player.sendMessage("§8§m-----------------------------------------------------");

        // Display Solana Explorer link with only the functional button
        String explorerUrl;
        if (nft.getMintAddress() != null && !nft.getMintAddress().isEmpty()) {
            // Use mint address if available (preferred)
            explorerUrl = plugin.getConfigManager().getSolanaExplorerAddressUrl(nft.getMintAddress());
        } else if (nft.getTransactionId() != null && !nft.getTransactionId().isEmpty()) {
            // Fallback to transaction ID
            explorerUrl = plugin.getConfigManager().getSolanaExplorerTransactionUrl(nft.getTransactionId());
        } else {
            // Last resort, use NFT ID (though this may not work correctly)
            explorerUrl = plugin.getConfigManager().getSolanaExplorerAddressUrl(nft.getNftId());
        }

        player.sendMessage("§7View on Solana Explorer: ");

        // Use Spigot's JSON message API to create clickable links
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
            "tellraw " + player.getName() + " {\"text\":\"§7[§a§lClick to Open Explorer§7]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + explorerUrl + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§7Click to open Solana Explorer\"}}"
        );

        // Display detail link with only the functional button
        // Get detail link from config manager (falls back to image URL if not configured)
        String detailLink = plugin.getConfigManager().getNftDetailLink(nft.getAchievementKey());
        if (detailLink != null && !detailLink.isEmpty()) {
            player.sendMessage("§7Detail: ");
            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                "tellraw " + player.getName() + " {\"text\":\"§7[§a§lOpen Detail in Browser§7]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + detailLink + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§7Click to view NFT details\"}}"
            );
        }

        player.sendMessage("§8§m-----------------------------------------------------");
    }

    /**
     * Format achievement key to a readable name
     * @param achievementKey The achievement key
     * @return The formatted achievement name
     */
    private String getFormattedAchievementName(String achievementKey) {
        if (achievementKey == null) {
            return "Unknown";
        }

        // Replace underscores with spaces and capitalize each word
        String[] words = achievementKey.split("_");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1).toLowerCase())
                      .append(" ");
            }
        }

        return result.toString().trim();
    }
}
