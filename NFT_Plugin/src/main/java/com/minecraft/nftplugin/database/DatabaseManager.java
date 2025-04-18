package com.minecraft.nftplugin.database;

import com.minecraft.nftplugin.NFTPlugin;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final NFTPlugin plugin;
    private HikariDataSource dataSource;

    // Table names
    private final String walletTable;
    private final String achievementTable;
    private final String nftTable;
    private final String nftStorageTable;

    public DatabaseManager(NFTPlugin plugin) {
        this.plugin = plugin;

        // Initialize table names with prefix
        String prefix = plugin.getConfigManager().getDatabaseTablePrefix();
        this.walletTable = prefix + "wallets";
        this.achievementTable = prefix + "achievements";
        this.nftTable = prefix + "nfts";
        this.nftStorageTable = prefix + "nft_storage";
    }

    /**
     * Initialize the database connection and tables
     * @return True if successful, false otherwise
     */
    public boolean initialize() {
        try {
            setupDataSource();
            createTables();
            return true;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to initialize database: " + e.getMessage());
            return false;
        }
    }

    /**
     * Set up the HikariCP data source
     * @throws SQLException If an error occurs
     */
    private void setupDataSource() throws SQLException {
        HikariConfig config = new HikariConfig();

        // Get database configuration from config
        String host = plugin.getConfigManager().getDatabaseHost();
        int port = plugin.getConfigManager().getDatabasePort();
        String database = plugin.getConfigManager().getDatabaseName();
        String username = plugin.getConfigManager().getDatabaseUsername();
        String password = plugin.getConfigManager().getDatabasePassword();

        // Configure HikariCP
        config.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // Connection pool settings
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(30000);
        config.setConnectionTimeout(10000);
        config.setMaxLifetime(1800000);

        // Additional MySQL settings
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");

        // Create the data source
        dataSource = new HikariDataSource(config);

        // Test connection
        try (Connection conn = dataSource.getConnection()) {
            if (!conn.isValid(1000)) {
                throw new SQLException("Could not establish database connection.");
            }
        }
    }

    /**
     * Create the necessary tables if they don't exist
     * @throws SQLException If an error occurs
     */
    private void createTables() throws SQLException {
        try (Connection conn = getConnection()) {
            // Create wallets table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + walletTable + " (" +
                            "uuid VARCHAR(36) PRIMARY KEY, " +
                            "player_name VARCHAR(16) NOT NULL, " +
                            "wallet_address VARCHAR(44) NOT NULL, " +
                            "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")) {
                stmt.executeUpdate();
            }

            // Create achievements table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + achievementTable + " (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "achievement_key VARCHAR(32) NOT NULL, " +
                            "progress INT NOT NULL DEFAULT 0, " +
                            "completed BOOLEAN NOT NULL DEFAULT FALSE, " +
                            "completed_at TIMESTAMP NULL, " +
                            "UNIQUE KEY unique_player_achievement (uuid, achievement_key)" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")) {
                stmt.executeUpdate();
            }

            // Create NFTs table
            try (PreparedStatement stmt = conn.prepareStatement(
                    "CREATE TABLE IF NOT EXISTS " + nftTable + " (" +
                            "id INT AUTO_INCREMENT PRIMARY KEY, " +
                            "uuid VARCHAR(36) NOT NULL, " +
                            "achievement_key VARCHAR(32) NOT NULL, " +
                            "nft_id VARCHAR(64) NOT NULL, " +
                            "mint_address VARCHAR(44) NOT NULL, " +
                            "transaction_id VARCHAR(88) NOT NULL, " +
                            "minted_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                            ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;")) {
                stmt.executeUpdate();
            }
        }
    }

    /**
     * Get a database connection from the pool
     * @return A database connection
     * @throws SQLException If an error occurs
     */
    public Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    /**
     * Get the NFT table name
     * @return The NFT table name
     */
    public String getNftTable() {
        return nftTable;
    }

    /**
     * Get the NFT storage table name
     * @return The NFT storage table name
     */
    public String getNftStorageTable() {
        return nftStorageTable;
    }

    /**
     * Close the data source
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    /**
     * Register a player's wallet
     * @param uuid The player's UUID
     * @param playerName The player's name
     * @param walletAddress The wallet address
     * @return True if successful, false otherwise
     */
    public boolean registerWallet(UUID uuid, String playerName, String walletAddress) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO " + walletTable + " (uuid, player_name, wallet_address) VALUES (?, ?, ?) " +
                             "ON DUPLICATE KEY UPDATE player_name = ?, wallet_address = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, playerName);
            stmt.setString(3, walletAddress);
            stmt.setString(4, playerName);
            stmt.setString(5, walletAddress);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to register wallet: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player has a registered wallet
     * @param uuid The player's UUID
     * @return True if the player has a registered wallet, false otherwise
     */
    public boolean hasWallet(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM " + walletTable + " WHERE uuid = ?")) {

            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to check if player has wallet: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a player's wallet address
     * @param uuid The player's UUID
     * @return The wallet address, or null if not found
     */
    public String getWalletAddress(UUID uuid) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT wallet_address FROM " + walletTable + " WHERE uuid = ?")) {

            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("wallet_address");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get wallet address: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get a player's achievement progress
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return The progress, or 0 if not found
     */
    public int getAchievementProgress(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT progress FROM " + achievementTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("progress");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get achievement progress: " + e.getMessage());
        }

        return 0;
    }

    /**
     * Update a player's achievement progress
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @param progress The new progress
     * @param required The required progress to complete the achievement
     * @return True if successful, false otherwise
     */
    public boolean updateAchievementProgress(UUID uuid, String achievementKey, int progress, int required) {
        boolean completed = progress >= required;

        return updateAchievementProgress(uuid, achievementKey, progress, completed);
    }

    /**
     * Update a player's achievement progress
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @param progress The new progress
     * @return True if successful, false otherwise
     */
    public boolean updateAchievementProgress(UUID uuid, String achievementKey, int progress) {
        // Get the required progress from config
        int required = plugin.getConfigManager().getRequiredBlocks(achievementKey);
        return updateAchievementProgress(uuid, achievementKey, progress, required);
    }

    /**
     * Update a player's achievement progress
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @param progress The new progress
     * @param completed Whether the achievement is completed
     * @return True if successful, false otherwise
     */
    private boolean updateAchievementProgress(UUID uuid, String achievementKey, int progress, boolean completed) {

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO " + achievementTable + " (uuid, achievement_key, progress, completed, completed_at) " +
                             "VALUES (?, ?, ?, ?, " + (completed ? "CURRENT_TIMESTAMP" : "NULL") + ") " +
                             "ON DUPLICATE KEY UPDATE progress = ?, completed = ?, " +
                             "completed_at = " + (completed ? "COALESCE(completed_at, CURRENT_TIMESTAMP)" : "completed_at"))) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);
            stmt.setInt(3, progress);
            stmt.setBoolean(4, completed);
            stmt.setInt(5, progress);
            stmt.setBoolean(6, completed);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to update achievement progress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player has completed an achievement
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return True if the player has completed the achievement, false otherwise
     */
    public boolean hasCompletedAchievement(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT completed FROM " + achievementTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("completed");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to check if player has completed achievement: " + e.getMessage());
        }

        return false;
    }

    /**
     * Record a minted NFT
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @param nftId The NFT ID
     * @param mintAddress The mint address
     * @param transactionId The transaction ID
     * @return True if successful, false otherwise
     */
    public boolean recordNft(UUID uuid, String achievementKey, String nftId, String mintAddress, String transactionId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO " + nftTable + " (uuid, achievement_key, nft_id, mint_address, transaction_id) " +
                             "VALUES (?, ?, ?, ?, ?)")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);
            stmt.setString(3, nftId);
            stmt.setString(4, mintAddress);
            stmt.setString(5, transactionId);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to record NFT: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if a player has an NFT for an achievement
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return True if the player has an NFT for the achievement, false otherwise
     */
    public boolean hasNft(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT 1 FROM " + nftTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to check if player has NFT: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get a player's NFT ID for an achievement
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return The NFT ID, or null if not found
     */
    public String getNftId(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT nft_id FROM " + nftTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("nft_id");
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get NFT ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Get a player's UUID from their name
     * @param playerName The player's name
     * @return The UUID, or null if not found
     */
    public UUID getUUIDFromName(String playerName) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT uuid FROM " + walletTable + " WHERE player_name = ?")) {

            stmt.setString(1, playerName);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return UUID.fromString(rs.getString("uuid"));
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get UUID from name: " + e.getMessage());
        }

        return null;
    }

    /**
     * Reset a player's NFT for an achievement
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return True if successful, false otherwise
     */
    public boolean resetNFT(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + nftTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to reset NFT: " + e.getMessage());
            return false;
        }
    }

    /**
     * Reset a player's achievement progress
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @return True if successful, false otherwise
     */
    public boolean resetAchievementProgress(UUID uuid, String achievementKey) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "DELETE FROM " + achievementTable + " WHERE uuid = ? AND achievement_key = ?")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, achievementKey);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to reset achievement progress: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get all NFTs owned by a player
     * @param uuid The player's UUID
     * @return A list of NFTData objects
     */
    public List<NFTData> getPlayerNFTs(UUID uuid) {
        List<NFTData> nfts = new ArrayList<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM " + nftTable + " WHERE uuid = ? ORDER BY minted_at DESC")) {

            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NFTData nft = new NFTData(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("achievement_key"),
                            rs.getString("nft_id"),
                            rs.getString("mint_address"),
                            rs.getString("transaction_id"),
                            rs.getTimestamp("minted_at")
                    );
                    nfts.add(nft);
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get player NFTs: " + e.getMessage());
        }

        return nfts;
    }

    /**
     * Get an NFT by its NFT ID
     * @param nftId The NFT ID
     * @return The NFTData object, or null if not found
     */
    public NFTData getNFTByNftId(String nftId) {
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT * FROM " + nftTable + " WHERE nft_id = ?")) {

            stmt.setString(1, nftId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new NFTData(
                            rs.getInt("id"),
                            UUID.fromString(rs.getString("uuid")),
                            rs.getString("achievement_key"),
                            rs.getString("nft_id"),
                            rs.getString("mint_address"),
                            rs.getString("transaction_id"),
                            rs.getTimestamp("minted_at")
                    );
                }
            }
        } catch (SQLException e) {
            plugin.log(Level.SEVERE, "Failed to get NFT by ID: " + e.getMessage());
        }

        return null;
    }
}
