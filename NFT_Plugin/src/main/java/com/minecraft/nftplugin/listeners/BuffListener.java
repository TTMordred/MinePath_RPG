package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for buff-related events
 */
public class BuffListener implements Listener {

    private final NFTPlugin plugin;

    public BuffListener(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Update buffs when a player joins the server
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getBuffManager().updatePlayerBuffs(player);
    }

    /**
     * Clear buffs when a player leaves the server
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        plugin.getBuffManager().updatePlayerBuffs(player);
    }

    /**
     * Update buffs when a player closes their inventory
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getPlayer() instanceof Player) {
            Player player = (Player) event.getPlayer();
            plugin.getBuffManager().updatePlayerBuffs(player);
        }
    }

    /**
     * Update buffs when a player picks up an item
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();
            plugin.getBuffManager().updatePlayerBuffs(player);
        }
    }

    /**
     * Update buffs when a player drops an item
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        plugin.getBuffManager().updatePlayerBuffs(player);
    }
}
