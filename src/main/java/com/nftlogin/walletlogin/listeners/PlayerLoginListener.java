package com.nftlogin.walletlogin.listeners;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

public class PlayerLoginListener implements Listener {

    private final SolanaLogin plugin;
    private static final String LOGIN_TIMEOUT_CONFIG = "settings.login-timeout";
    private static final int DEFAULT_LOGIN_TIMEOUT = 60;

    public PlayerLoginListener(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerLogin(PlayerLoginEvent event) {
        Player player = event.getPlayer();

        // Save player data to database
        plugin.getDatabaseManager().savePlayer(player);

        // If login is required and player is not registered, we'll let them join and notify them
        // The actual restriction will be handled in the PlayerJoinEvent
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // Check if player is registered
        boolean isRegistered = plugin.getDatabaseManager().isPlayerRegistered(playerUuid);

        // Handle authentication if required
        if (plugin.getConfig().getBoolean("settings.require-login", true)) {
            handleAuthentication(player, playerUuid, isRegistered);
        }

        // Handle wallet connection if required
        if (isPlayerAuthenticated(playerUuid) &&
                plugin.getConfig().getBoolean("settings.require-wallet-login", false)) {
            handleWalletConnection(player, playerUuid);
        }
    }

    /**
     * Handles player authentication (registration and login).
     *
     * @param player The player
     * @param playerUuid The player's UUID
     * @param isRegistered Whether the player is registered
     */
    private void handleAuthentication(Player player, UUID playerUuid, boolean isRegistered) {
        if (!isRegistered) {
            handleRegistration(player, playerUuid);
        } else if (!isPlayerAuthenticated(playerUuid)) {
            handleLogin(player, playerUuid);
        }
    }

    /**
     * Checks if a player is authenticated.
     *
     * @param playerUuid The player's UUID
     * @return true if the player is authenticated, false otherwise
     */
    private boolean isPlayerAuthenticated(UUID playerUuid) {
        return plugin.getSessionManager().hasSession(playerUuid) &&
                plugin.getSessionManager().getSession(playerUuid).isAuthenticated();
    }

    /**
     * Handles player registration.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     */
    private void handleRegistration(Player player, UUID playerUuid) {
        // Notify player they need to register
        String message = plugin.getConfig().getString("messages.register-required",
                "Please register with /register <password> <confirmPassword>");
        player.sendMessage(plugin.formatMessage(message));

        // Set a timer to kick the player if they don't register
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player is still online and hasn't registered
                if (player.isOnline() && !plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
                    player.kickPlayer(plugin.formatMessage(message));
                }
            }
        }.runTaskLater(plugin, timeout * 20L); // Convert seconds to ticks
    }

    /**
     * Handles player login.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     */
    private void handleLogin(Player player, UUID playerUuid) {
        // Player is registered but not logged in
        String message = plugin.getConfig().getString("messages.login-required",
                "Please login with /login <password>");
        player.sendMessage(plugin.formatMessage(message));

        // Set a timer to kick the player if they don't login
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player is still online and hasn't logged in
                if (player.isOnline() && !isPlayerAuthenticated(playerUuid)) {
                    String timeoutMessage = plugin.getConfig().getString("messages.login-timeout",
                            "You took too long to login. Please reconnect.");
                    player.kickPlayer(plugin.formatMessage(timeoutMessage));
                }
            }
        }.runTaskLater(plugin, timeout * 20L); // Convert seconds to ticks
    }

    /**
     * Handles wallet connection and verification.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     */
    private void handleWalletConnection(Player player, UUID playerUuid) {
        if (!plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
            promptWalletConnection(player, playerUuid);
        } else if (!plugin.getDatabaseManager().isWalletVerified(playerUuid)) {
            // Notify player they need to verify their wallet
            String message = plugin.getConfig().getString("messages.wallet-verification-pending",
                    "Your wallet verification is pending. Please complete the verification process.");
            player.sendMessage(plugin.formatMessage(message));
        }
    }

    /**
     * Prompts the player to connect a wallet and sets a timeout.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     */
    private void promptWalletConnection(Player player, UUID playerUuid) {
        // Notify player they need to connect a wallet
        String message = plugin.getConfig().getString("messages.wallet-required",
                "You need to connect a Solana wallet to play on this server. Use /connectwallet <address>");
        player.sendMessage(plugin.formatMessage(message));

        // Set a timer to kick the player if they don't connect a wallet
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);

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

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // If player has a session but is not authenticated, remove the session
        if (plugin.getSessionManager().hasSession(playerUuid) &&
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            plugin.getSessionManager().removeSession(playerUuid);
        }
    }
}
