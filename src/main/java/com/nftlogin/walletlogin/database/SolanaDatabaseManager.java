package com.nftlogin.walletlogin.database;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.solana.SolanaClient;
import com.nftlogin.walletlogin.solana.models.MinecraftAccount;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Solana implementation of the DatabaseManager interface.
 */
public class SolanaDatabaseManager implements DatabaseManager {

    private final SolanaLogin plugin;
    private final SolanaClient solanaClient;
    private final SQLDatabaseManager sqlManager;

    /**
     * Creates a new SolanaDatabaseManager instance.
     *
     * @param plugin The SolanaLogin plugin instance
     * @param solanaClient The Solana client
     */
    public SolanaDatabaseManager(SolanaLogin plugin, SolanaClient solanaClient) {
        this.plugin = plugin;
        this.solanaClient = solanaClient;

        // Create a SQL manager for non-wallet data
        this.sqlManager = new SQLDatabaseManager(plugin);
    }

    @Override
    public void connect() throws SQLException {
        // Connect to SQL database for non-wallet data
        sqlManager.connect();

        // No connection needed for Solana
        plugin.getLogger().info("Connected to Solana blockchain via RPC");
    }

    @Override
    public void createTables() throws SQLException {
        // Create SQL tables for non-wallet data
        sqlManager.createTables();

        // No tables needed for Solana
        plugin.getLogger().info("Solana storage initialized");
    }

    @Override
    public boolean isPlayerRegistered(UUID uuid) {
        // Player registration is still handled by SQL
        return sqlManager.isPlayerRegistered(uuid);
    }

    @Override
    public boolean registerPlayer(Player player, String password) {
        // Player registration is still handled by SQL
        return sqlManager.registerPlayer(player, password);
    }

    @Override
    public boolean authenticatePlayer(UUID uuid, String password) {
        // Player authentication is still handled by SQL
        return sqlManager.authenticatePlayer(uuid, password);
    }

    @Override
    public boolean updatePassword(UUID uuid, String newPassword) {
        // Password management is still handled by SQL
        return sqlManager.updatePassword(uuid, newPassword);
    }

    @Override
    public void updateLastLogin(UUID uuid, String ip) {
        // Login tracking is still handled by SQL
        sqlManager.updateLastLogin(uuid, ip);
    }

    @Override
    public void saveSession(UUID uuid, String ip) {
        // Session management is still handled by SQL
        sqlManager.saveSession(uuid, ip);
    }

    @Override
    public void removeSession(UUID uuid) {
        // Session management is still handled by SQL
        sqlManager.removeSession(uuid);
    }

    @Override
    public void savePlayer(Player player) {
        // Player data is still handled by SQL
        sqlManager.savePlayer(player);
    }

    @Override
    public boolean connectWallet(UUID uuid, String walletAddress, String walletType) {
        // Connect wallet using Solana
        try {
            // First save player data to SQL to ensure the player exists
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                sqlManager.savePlayer(player);
            }

            // Then connect wallet using Solana
            return solanaClient.connectWallet(uuid, walletAddress, walletType);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting wallet via Solana", e);
            return false;
        }
    }

    @Override
    public boolean disconnectWallet(UUID uuid) {
        // Disconnect wallet using Solana
        try {
            return solanaClient.disconnectWallet(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error disconnecting wallet via Solana", e);
            return false;
        }
    }

    @Override
    public Optional<String> getWalletAddress(UUID uuid) {
        // Get wallet address from Solana
        try {
            return solanaClient.getWalletAddress(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting wallet address via Solana", e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<String> getWalletType(UUID uuid) {
        // Get wallet type from Solana
        try {
            return solanaClient.getWalletType(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting wallet type via Solana", e);
            return Optional.empty();
        }
    }

    @Override
    public boolean hasWalletConnected(UUID uuid) {
        // Check if wallet is connected using Solana
        try {
            return solanaClient.hasWalletConnected(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking wallet connection via Solana", e);
            return false;
        }
    }

    @Override
    public boolean setWalletVerified(UUID uuid, boolean verified) {
        // Set wallet verification status using Solana
        try {
            return solanaClient.setWalletVerified(uuid, verified);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting wallet verification via Solana", e);
            return false;
        }
    }

    @Override
    public boolean isWalletVerified(UUID uuid) {
        // Check wallet verification status using Solana
        try {
            return solanaClient.isWalletVerified(uuid);
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking wallet verification via Solana", e);
            return false;
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        // Close SQL connection for non-wallet data
        sqlManager.closeConnection();

        // No connection to close for Solana
        plugin.getLogger().info("Closed Solana connection");
    }
}
