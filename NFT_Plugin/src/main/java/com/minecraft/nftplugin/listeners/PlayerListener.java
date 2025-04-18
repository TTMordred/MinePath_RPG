package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
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
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        List<ItemStack> drops = event.getDrops();
        List<ItemStack> nftItems = new ArrayList<>();
        
        // Find and remove NFT items from drops
        for (int i = drops.size() - 1; i >= 0; i--) {
            ItemStack item = drops.get(i);
            if (plugin.getItemManager().isNftItem(item)) {
                nftItems.add(item.clone());
                drops.remove(i);
            }
        }
        
        // Store NFT items to restore on respawn
        if (!nftItems.isEmpty()) {
            nftItemsToRestore.put(player.getUniqueId(), nftItems);
        }
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
            
            // Restore NFT items
            for (ItemStack item : nftItems) {
                player.getInventory().addItem(item);
            }
            
            // Remove the player from the map
            nftItemsToRestore.remove(playerUuid);
            
            // Send message
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§aYour NFT items have been restored.");
        }
    }
}
