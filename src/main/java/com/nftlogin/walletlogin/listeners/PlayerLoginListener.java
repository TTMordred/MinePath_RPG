package com.nftlogin.walletlogin.listeners;

import com.nftlogin.walletlogin.WalletLogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerLoginListener implements Listener {

    private final WalletLogin plugin;

    public PlayerLoginListener(WalletLogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();
        
        // If wallet login is required, check if the player has a wallet connected
        if (plugin.getConfig().getBoolean("settings.require-wallet-login", false)) {
            UUID playerUuid = player.getUniqueId();
            
            if (!plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                // Player doesn't have a wallet connected, but we'll let them join and notify them
                // The actual restriction will be handled in the PlayerJoinEvent
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Save player data to database
        plugin.getDatabaseManager().savePlayer(player);
        
        // Check if wallet login is required
        if (plugin.getConfig().getBoolean("settings.require-wallet-login", false)) {
            UUID playerUuid = player.getUniqueId();
            
            if (!plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                // Notify player they need to connect a wallet
                String message = plugin.getConfig().getString("messages.wallet-required", 
                        "You need to connect a wallet to play on this server. Use /connectwallet <address>");
                player.sendMessage(plugin.formatMessage(message));
                
                // Set a timer to kick the player if they don't connect a wallet
                int timeout = plugin.getConfig().getInt("settings.login-timeout", 60);
                
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        // Check if the player is still online and hasn't connected a wallet
                        if (player.isOnline() && !plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                            player.kickPlayer(plugin.formatMessage(message));
                        }
                    }
                }.runTaskLater(plugin, timeout * 20L); // Convert seconds to ticks
            }
        }
    }
}
