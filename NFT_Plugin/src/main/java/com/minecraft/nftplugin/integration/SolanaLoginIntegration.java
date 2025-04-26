package com.minecraft.nftplugin.integration;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Integration with the SolanaLogin plugin
 */
public class SolanaLoginIntegration {

    private final NFTPlugin plugin;
    private boolean solanaLoginAvailable;
    private Object solanaLoginPlugin;
    private Object databaseManager;

    // Cached reflection methods
    private Method getWalletAddressMethod;
    private Method hasWalletConnectedMethod;
    private Method isWalletVerifiedMethod;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public SolanaLoginIntegration(NFTPlugin plugin) {
        this.plugin = plugin;
        this.solanaLoginAvailable = false;

        // Initialize integration
        initialize();
    }

    /**
     * Initialize the integration with SolanaLogin
     */
    private void initialize() {
        try {
            // Check if SolanaLogin is available
            Plugin solanaLogin = Bukkit.getPluginManager().getPlugin("SolanaLogin");
            if (solanaLogin == null || !solanaLogin.isEnabled()) {
                plugin.getLogger().warning("SolanaLogin plugin not found or not enabled. Some features will be disabled.");
                return;
            }

            // Get SolanaLogin plugin instance
            solanaLoginPlugin = solanaLogin;

            // Get DatabaseManager using reflection
            Method getDatabaseManagerMethod = solanaLoginPlugin.getClass().getMethod("getDatabaseManager");
            databaseManager = getDatabaseManagerMethod.invoke(solanaLoginPlugin);

            // Cache reflection methods for better performance
            Class<?> databaseManagerClass = databaseManager.getClass();
            getWalletAddressMethod = databaseManagerClass.getMethod("getWalletAddress", UUID.class);
            hasWalletConnectedMethod = databaseManagerClass.getMethod("hasWalletConnected", UUID.class);
            isWalletVerifiedMethod = databaseManagerClass.getMethod("isWalletVerified", UUID.class);

            // Integration successful
            solanaLoginAvailable = true;
            plugin.getLogger().info("Successfully integrated with SolanaLogin plugin.");
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to integrate with SolanaLogin plugin", e);
        }
    }

    /**
     * Check if SolanaLogin is available
     * @return True if SolanaLogin is available, false otherwise
     */
    public boolean isSolanaLoginAvailable() {
        return solanaLoginAvailable;
    }

    /**
     * Get a player's wallet address from SolanaLogin
     * @param playerUuid The player's UUID
     * @return The wallet address, or empty if not found
     */
    public Optional<String> getWalletAddress(UUID playerUuid) {
        if (!solanaLoginAvailable) {
            plugin.getLogger().warning("SolanaLogin is not available. Cannot get wallet address.");
            return Optional.empty();
        }

        try {
            Object result = getWalletAddressMethod.invoke(databaseManager, playerUuid);
            if (result instanceof Optional) {
                @SuppressWarnings("unchecked")
                Optional<String> walletAddress = (Optional<String>) result;

                if (walletAddress.isPresent()) {
                    plugin.getLogger().info("Retrieved wallet address from SolanaLogin: " + walletAddress.get() +
                            " for player UUID: " + playerUuid);
                } else {
                    plugin.getLogger().warning("No wallet address found in SolanaLogin for player UUID: " + playerUuid);
                }

                return walletAddress;
            } else {
                plugin.getLogger().warning("Unexpected result type from SolanaLogin: " +
                        (result != null ? result.getClass().getName() : "null"));
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to get wallet address from SolanaLogin", e);
        }

        return Optional.empty();
    }

    /**
     * Check if a player has a wallet connected in SolanaLogin
     * @param playerUuid The player's UUID
     * @return True if the player has a wallet connected, false otherwise
     */
    public boolean hasWalletConnected(UUID playerUuid) {
        if (!solanaLoginAvailable) {
            return false;
        }

        try {
            Object result = hasWalletConnectedMethod.invoke(databaseManager, playerUuid);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check if player has wallet connected in SolanaLogin", e);
        }

        return false;
    }

    /**
     * Check if a player's wallet is verified in SolanaLogin
     * @param playerUuid The player's UUID
     * @return True if the player's wallet is verified, false otherwise
     */
    public boolean isWalletVerified(UUID playerUuid) {
        if (!solanaLoginAvailable) {
            return false;
        }

        try {
            Object result = isWalletVerifiedMethod.invoke(databaseManager, playerUuid);
            if (result instanceof Boolean) {
                return (Boolean) result;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Failed to check if player's wallet is verified in SolanaLogin", e);
        }

        return false;
    }

    /**
     * Get a player's wallet address from SolanaLogin
     * @param player The player
     * @return The wallet address, or empty if not found
     */
    public Optional<String> getWalletAddress(Player player) {
        return getWalletAddress(player.getUniqueId());
    }

    /**
     * Check if a player has a wallet connected in SolanaLogin
     * @param player The player
     * @return True if the player has a wallet connected, false otherwise
     */
    public boolean hasWalletConnected(Player player) {
        return hasWalletConnected(player.getUniqueId());
    }

    /**
     * Check if a player's wallet is verified in SolanaLogin
     * @param player The player
     * @return True if the player's wallet is verified, false otherwise
     */
    public boolean isWalletVerified(Player player) {
        return isWalletVerified(player.getUniqueId());
    }
}
