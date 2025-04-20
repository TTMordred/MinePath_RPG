package com.nftlogin.walletlogin.listeners;

import com.nftlogin.walletlogin.SolanaLogin;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;
import java.util.logging.Level;

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

        // Always handle wallet connection if player is authenticated
        // This ensures wallet connection is enforced from the beginning
        if (isPlayerAuthenticated(playerUuid)) {
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

        // Get timeout in minutes and notify player
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);
        int minutes = timeout / 60;
        player.sendMessage(plugin.formatMessage(String.format("&cYou have %d minutes to register or you will be kicked.", minutes)));

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player is still online and hasn't registered
                if (player.isOnline() && !plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
                    // Using the modern kick API with Component
                    player.kick(Component.text(plugin.formatMessage(message)));
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

        // Get timeout in minutes and notify player
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);
        int minutes = timeout / 60;
        player.sendMessage(plugin.formatMessage(String.format("&cYou have %d minutes to login or you will be kicked.", minutes)));

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player is still online and hasn't logged in
                if (player.isOnline() && !isPlayerAuthenticated(playerUuid)) {
                    String timeoutMessage = plugin.getConfig().getString("messages.login-timeout",
                            "You took too long to login. Please reconnect.");
                    // Using the modern kick API with Component
                    player.kick(Component.text(plugin.formatMessage(timeoutMessage)));
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
        // Only proceed if wallet login is required or player already has a wallet connected
        if (plugin.getConfig().getBoolean("settings.require-wallet-login", true) ||
                plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {

            if (!plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                promptWalletConnection(player, playerUuid);
            } else if (!plugin.getDatabaseManager().isWalletVerified(playerUuid)) {
                // Notify player they need to verify their wallet
                String message = plugin.getConfig().getString("messages.wallet-verification-pending",
                        "Your wallet verification is pending. Please complete the verification process.");
                player.sendMessage(plugin.formatMessage(message));
            }
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
                "You need to connect a Solana wallet to play on this server. Use /connectwallet");
        player.sendMessage(plugin.formatMessage(message));

        // Get timeout in minutes and notify player
        int timeout = plugin.getConfig().getInt(LOGIN_TIMEOUT_CONFIG, DEFAULT_LOGIN_TIMEOUT);
        int minutes = timeout / 60;
        player.sendMessage(plugin.formatMessage(String.format("&cYou have %d minutes to connect a wallet or you will be kicked.", minutes)));

        // Set a timer to kick the player if they don't connect a wallet

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check if the player is still online and hasn't connected a wallet
                if (player.isOnline() && !plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                    // Using the modern kick API with Component
                    player.kick(Component.text(plugin.formatMessage(message)));
                }
            }
        }.runTaskLater(plugin, timeout * 20L); // Convert seconds to ticks
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID playerUuid = player.getUniqueId();

        // Always remove the session when player quits to ensure they need to login again
        if (plugin.getSessionManager().hasSession(playerUuid)) {
            plugin.getSessionManager().removeSession(playerUuid);
            plugin.getDatabaseManager().removeSession(playerUuid);

            // Log the session removal
            if (plugin.getLogger().isLoggable(Level.FINE)) {
                plugin.getLogger().fine(String.format("Session removed for player %s on quit", player.getName()));
            }
        }
    }
}
