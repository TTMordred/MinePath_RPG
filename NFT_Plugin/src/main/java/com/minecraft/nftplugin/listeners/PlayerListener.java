package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerListener implements Listener {

    private final NFTPlugin plugin;
    private final Map<UUID, List<ItemStack>> nftItemsToRestore = new HashMap<>();

    public PlayerListener(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Send a welcome message to players when they join
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Check if the player has a registered wallet
        if (!plugin.getDatabaseManager().hasWallet(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§eWelcome! You need to register your Solana wallet to earn NFTs.");
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§eUse §6/registerwallet <SOL_ADDRESS>§e to register your wallet.");
        }
    }

    /**
     * Prevent NFT items from being dropped on death
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // First, save all NFT items from player's inventory
        List<ItemStack> nftItems = new ArrayList<>();

        // Check main inventory
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && plugin.getItemManager().isNftItem(item)) {
                nftItems.add(item.clone());
                plugin.getLogger().info("Found NFT item in inventory: " + item.getType().name());
            }
        }

        // Check armor slots
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && plugin.getItemManager().isNftItem(item)) {
                nftItems.add(item.clone());
                plugin.getLogger().info("Found NFT item in armor: " + item.getType().name());
            }
        }

        // Check offhand
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        if (offhandItem != null && plugin.getItemManager().isNftItem(offhandItem)) {
            nftItems.add(offhandItem.clone());
            plugin.getLogger().info("Found NFT item in offhand: " + offhandItem.getType().name());
        }

        // Now remove all NFT items from drops
        List<ItemStack> drops = event.getDrops();
        for (int i = drops.size() - 1; i >= 0; i--) {
            ItemStack item = drops.get(i);
            if (item != null && plugin.getItemManager().isNftItem(item)) {
                drops.remove(i);
                plugin.getLogger().info("Removed NFT item from drops: " + item.getType().name());
            }
        }

        // Store NFT items to restore on respawn
        if (!nftItems.isEmpty()) {
            nftItemsToRestore.put(player.getUniqueId(), nftItems);
            plugin.getLogger().info("Stored " + nftItems.size() + " NFT items to restore for player " + player.getName());
        }

        // Log the number of items that will drop
        plugin.getLogger().info(player.getName() + " will drop " + drops.size() + " non-NFT items");
    }

    /**
     * Restore NFT items when the player respawns
     */
    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // Check if the player has NFT items to restore
        if (nftItemsToRestore.containsKey(playerUuid)) {
            List<ItemStack> nftItems = nftItemsToRestore.get(playerUuid);

            // Schedule task to restore NFT items after a short delay
            // This ensures the items are added after the respawn process is complete
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                // Restore NFT items
                for (ItemStack item : nftItems) {
                    java.util.HashMap<Integer, ItemStack> leftover = player.getInventory().addItem(item);

                    // If inventory is full, drop the item at player's feet
                    if (!leftover.isEmpty()) {
                        for (ItemStack leftoverItem : leftover.values()) {
                            player.getWorld().dropItemNaturally(player.getLocation(), leftoverItem);
                        }
                    }
                }

                // Remove the player from the map
                nftItemsToRestore.remove(playerUuid);

                // Send message
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aYour NFT items have been restored.");

                plugin.getLogger().info("Restored " + nftItems.size() + " NFT items for player " + player.getName());
            }, 5L); // 5 tick delay (1/4 second)
        }
    }
}
