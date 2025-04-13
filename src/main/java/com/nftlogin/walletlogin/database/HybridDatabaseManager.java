package com.nftlogin.walletlogin.database;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.solana.SolanaClient;
import com.nftlogin.walletlogin.solana.SolanaConfig;
import org.bukkit.entity.Player;

import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Hybrid implementation of the DatabaseManager interface that combines SQL and Solana storage.
 */
public class HybridDatabaseManager implements DatabaseManager {

    private final SolanaLogin plugin;
    private final SQLDatabaseManager sqlManager;
    private final SolanaClient solanaClient;
    private final SolanaConfig solanaConfig;

    private static final String WALLET_LINKS = "wallet-links";

    /**
     * Creates a new HybridDatabaseManager instance.
     *
     * @param plugin The SolanaLogin plugin instance
     * @param sqlManager The SQL database manager
     * @param solanaClient The Solana client
     */
    public HybridDatabaseManager(SolanaLogin plugin, SQLDatabaseManager sqlManager, SolanaClient solanaClient) {
        this.plugin = plugin;
        this.sqlManager = sqlManager;
        this.solanaClient = solanaClient;
        this.solanaConfig = new SolanaConfig(plugin);
    }

    @Override
    public void connect() throws SQLException {
        // Connect to SQL database
        sqlManager.connect();

        // No connection needed for Solana
        plugin.getLogger().info("Connected to Solana blockchain via RPC");
    }

    @Override
    public void createTables() throws SQLException {
        // Create SQL tables
        sqlManager.createTables();

        // No tables needed for Solana
        plugin.getLogger().info("Hybrid storage initialized (SQL + Solana)");
    }

    @Override
    public boolean isPlayerRegistered(UUID uuid) {
        // Player registration is always handled by SQL
        return sqlManager.isPlayerRegistered(uuid);
    }

    @Override
    public boolean registerPlayer(Player player, String password) {
        // Player registration is always handled by SQL
        return sqlManager.registerPlayer(player, password);
    }

    @Override
    public boolean authenticatePlayer(UUID uuid, String password) {
        // Player authentication is always handled by SQL
        return sqlManager.authenticatePlayer(uuid, password);
    }

    @Override
    public boolean updatePassword(UUID uuid, String newPassword) {
        // Password management is always handled by SQL
        return sqlManager.updatePassword(uuid, newPassword);
    }

    @Override
    public void updateLastLogin(UUID uuid, String ip) {
        // Login tracking is always handled by SQL
        sqlManager.updateLastLogin(uuid, ip);
    }

    @Override
    public void saveSession(UUID uuid, String ip) {
        // Session management is always handled by SQL
        sqlManager.saveSession(uuid, ip);
    }

    @Override
    public void removeSession(UUID uuid) {
        // Session management is always handled by SQL
        sqlManager.removeSession(uuid);
    }

    @Override
    public void savePlayer(Player player) {
        // Player data is always handled by SQL
        sqlManager.savePlayer(player);
    }

    @Override
    public boolean connectWallet(UUID uuid, String walletAddress, String walletType) {
        // First save player data to SQL to ensure the player exists
        Player player = plugin.getServer().getPlayer(uuid);
        if (player != null) {
            sqlManager.savePlayer(player);
        }

        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Connect wallet using Solana
                boolean solanaSuccess = solanaClient.connectWallet(uuid, walletAddress, walletType);

                // If Solana fails, fall back to SQL
                if (!solanaSuccess) {
                    plugin.getLogger().warning("Failed to connect wallet via Solana, falling back to SQL");
                    return sqlManager.connectWallet(uuid, walletAddress, walletType);
                }

                return solanaSuccess;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error connecting wallet via Solana, falling back to SQL", e);
                return sqlManager.connectWallet(uuid, walletAddress, walletType);
            }
        } else {
            // Store wallet link in SQL
            return sqlManager.connectWallet(uuid, walletAddress, walletType);
        }
    }

    @Override
    public boolean disconnectWallet(UUID uuid) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Disconnect wallet using Solana
                boolean solanaSuccess = solanaClient.disconnectWallet(uuid);

                // If Solana fails, fall back to SQL
                if (!solanaSuccess) {
                    plugin.getLogger().warning("Failed to disconnect wallet via Solana, falling back to SQL");
                    return sqlManager.disconnectWallet(uuid);
                }

                return solanaSuccess;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error disconnecting wallet via Solana, falling back to SQL", e);
                return sqlManager.disconnectWallet(uuid);
            }
        } else {
            // Disconnect wallet from SQL
            return sqlManager.disconnectWallet(uuid);
        }
    }

    @Override
    public Optional<String> getWalletAddress(UUID uuid) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Get wallet address from Solana
                Optional<String> solanaAddress = solanaClient.getWalletAddress(uuid);

                // If Solana returns a result, use it
                if (solanaAddress.isPresent()) {
                    return solanaAddress;
                }

                // Otherwise, fall back to SQL
                plugin.getLogger().fine("No wallet address found on Solana, checking SQL");
                return sqlManager.getWalletAddress(uuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting wallet address via Solana, falling back to SQL", e);
                return sqlManager.getWalletAddress(uuid);
            }
        } else {
            // Get wallet address from SQL
            return sqlManager.getWalletAddress(uuid);
        }
    }

    @Override
    public Optional<String> getWalletType(UUID uuid) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Get wallet type from Solana
                Optional<String> solanaType = solanaClient.getWalletType(uuid);

                // If Solana returns a result, use it
                if (solanaType.isPresent()) {
                    return solanaType;
                }

                // Otherwise, fall back to SQL
                plugin.getLogger().fine("No wallet type found on Solana, checking SQL");
                return sqlManager.getWalletType(uuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error getting wallet type via Solana, falling back to SQL", e);
                return sqlManager.getWalletType(uuid);
            }
        } else {
            // Get wallet type from SQL
            return sqlManager.getWalletType(uuid);
        }
    }

    @Override
    public boolean hasWalletConnected(UUID uuid) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Check if wallet is connected using Solana
                boolean solanaConnected = solanaClient.hasWalletConnected(uuid);

                // If connected on Solana, return true
                if (solanaConnected) {
                    return true;
                }

                // Otherwise, check SQL
                plugin.getLogger().fine("No wallet connection found on Solana, checking SQL");
                return sqlManager.hasWalletConnected(uuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking wallet connection via Solana, falling back to SQL", e);
                return sqlManager.hasWalletConnected(uuid);
            }
        } else {
            // Check if wallet is connected using SQL
            return sqlManager.hasWalletConnected(uuid);
        }
    }

    @Override
    public boolean setWalletVerified(UUID uuid, boolean verified) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Set wallet verification status using Solana
                boolean solanaSuccess = solanaClient.setWalletVerified(uuid, verified);

                // If Solana fails, fall back to SQL
                if (!solanaSuccess) {
                    plugin.getLogger().warning("Failed to set wallet verification via Solana, falling back to SQL");
                    return sqlManager.setWalletVerified(uuid, verified);
                }

                return solanaSuccess;
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error setting wallet verification via Solana, falling back to SQL", e);
                return sqlManager.setWalletVerified(uuid, verified);
            }
        } else {
            // Set wallet verification status using SQL
            return sqlManager.setWalletVerified(uuid, verified);
        }
    }

    @Override
    public boolean isWalletVerified(UUID uuid) {
        // Check if wallet links should be stored on Solana
        if (solanaConfig.shouldStoreOnSolana(WALLET_LINKS)) {
            try {
                // Check wallet verification status using Solana
                boolean solanaVerified = solanaClient.isWalletVerified(uuid);

                // If verified on Solana, return true
                if (solanaVerified) {
                    return true;
                }

                // Otherwise, check SQL
                plugin.getLogger().fine("No wallet verification found on Solana, checking SQL");
                return sqlManager.isWalletVerified(uuid);
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Error checking wallet verification via Solana, falling back to SQL", e);
                return sqlManager.isWalletVerified(uuid);
            }
        } else {
            // Check wallet verification status using SQL
            return sqlManager.isWalletVerified(uuid);
        }
    }

    @Override
    public void closeConnection() throws SQLException {
        // Close SQL connection
        sqlManager.closeConnection();

        // No connection to close for Solana
        plugin.getLogger().info("Closed hybrid storage connections");
    }
}
