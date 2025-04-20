package com.nftlogin.walletlogin.listeners;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.*;
import io.papermc.paper.event.player.AsyncChatEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Listener that restricts player actions when not authenticated or without a wallet connected.
 */
public class PlayerRestrictionListener implements Listener {

    private final SolanaLogin plugin;
    private final Set<String> allowedCommands;

    public PlayerRestrictionListener(SolanaLogin plugin) {
        this.plugin = plugin;

        // Initialize allowed commands (commands that can be used without authentication)
        this.allowedCommands = new HashSet<>(Arrays.asList(
                "/login",
                "/register",
                "/connectwallet"
        ));
    }

    /**
     * Checks if a player is fully authenticated (logged in and has wallet connected if required).
     *
     * @param playerUuid The player's UUID
     * @return true if the player is fully authenticated, false otherwise
     */
    private boolean isPlayerFullyAuthenticated(UUID playerUuid) {
        // Check if player is logged in
        boolean isLoggedIn = plugin.getSessionManager().hasSession(playerUuid) &&
                plugin.getSessionManager().getSession(playerUuid).isAuthenticated();

        // If wallet login is not required, just check if player is logged in
        if (!plugin.getConfig().getBoolean("settings.require-wallet-login", true)) {
            return isLoggedIn;
        }

        // If wallet login is required, check if player has a wallet connected
        return isLoggedIn && plugin.getDatabaseManager().hasWalletConnected(playerUuid);
    }

    /**
     * Sends a message to the player if they are not authenticated.
     *
     * @param player The player
     */
    private void sendAuthenticationMessage(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        boolean isLoggedIn = plugin.getSessionManager().hasSession(playerUuid) &&
                plugin.getSessionManager().getSession(playerUuid).isAuthenticated();

        if (!isLoggedIn) {
            // Player is not logged in
            if (!plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
                // Player is not registered
                String message = plugin.getConfig().getString("messages.register-required",
                        "Please register with /register <password> <confirmPassword>");
                player.sendMessage(plugin.formatMessage(message));
            } else {
                // Player is registered but not logged in
                String message = plugin.getConfig().getString("messages.login-required",
                        "Please login with /login <password>");
                player.sendMessage(plugin.formatMessage(message));
            }
        } else if (plugin.getConfig().getBoolean("settings.require-wallet-login", true) &&
                !plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
            // Player is logged in but doesn't have a wallet connected
            String message = plugin.getConfig().getString("messages.wallet-required",
                    "You need to connect a Solana wallet to play on this server. Use /connectwallet");
            player.sendMessage(plugin.formatMessage(message));
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerMove(PlayerMoveEvent event) {
        // Allow small head movements (looking around) but prevent actual movement
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    /**
     * Handle chat events using the new Paper API
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    // Legacy chat event handler removed in version 1.3 - Paper API AsyncChatEvent is now used

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        String command = event.getMessage().split(" ")[0].toLowerCase();

        // Allow certain commands even when not authenticated
        if (allowedCommands.contains(command)) {
            return;
        }

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerEditBook(PlayerEditBookEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemDamage(PlayerItemDamageEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();

        // Check if player is fully authenticated
        if (!isPlayerFullyAuthenticated(player.getUniqueId())) {
            event.setCancelled(true);
            sendAuthenticationMessage(player);
        }
    }
}
