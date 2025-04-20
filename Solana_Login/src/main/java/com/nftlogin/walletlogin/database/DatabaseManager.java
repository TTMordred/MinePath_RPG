package com.nftlogin.walletlogin.database;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.utils.PasswordUtils;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    // SQL constants
    private static final String CREATE_TABLE_IF_NOT_EXISTS = "CREATE TABLE IF NOT EXISTS ";
    private static final String INSERT_INTO = "INSERT INTO ";
    private static final String UPDATE = "UPDATE ";
    private static final String DELETE_FROM = "DELETE FROM ";
    private static final String SELECT = "SELECT ";
    private static final String WALLETS_WHERE_UUID = "wallets WHERE uuid = ?";
    private static final String PLAYERS_WHERE_UUID = "players WHERE uuid = ?";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String PASSWORD_COLUMN = "password";

    private final SolanaLogin plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    public DatabaseManager(SolanaLogin plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("database.host", "localhost");
        this.port = plugin.getConfig().getInt("database.port", 3306);
        this.database = plugin.getConfig().getString("database.database", "minecraft");
        this.username = plugin.getConfig().getString("database.username", "root");
        this.password = plugin.getConfig().getString("database.password", DEFAULT_PASSWORD);
        this.tablePrefix = plugin.getConfig().getString("database.table-prefix", "walletlogin_");
    }

    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
        connection = DriverManager.getConnection(url, username, password);
    }

    public void createTables() throws SQLException {
        // Players table
        String playersTable = CREATE_TABLE_IF_NOT_EXISTS + tablePrefix + "players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "password VARCHAR(255), " +
                "ip VARCHAR(45), " +
                "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        // Wallets table
        String walletsTable = CREATE_TABLE_IF_NOT_EXISTS + tablePrefix + "wallets (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "wallet_address VARCHAR(255) NOT NULL, " +
                "wallet_type VARCHAR(50), " +
                "verified BOOLEAN DEFAULT FALSE, " +
                "connected_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (uuid) REFERENCES " + tablePrefix + "players(uuid) ON DELETE CASCADE" +
                ")";

        // Sessions table
        String sessionsTable = CREATE_TABLE_IF_NOT_EXISTS + tablePrefix + "sessions (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "ip VARCHAR(45) NOT NULL, " +
                "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (uuid) REFERENCES " + tablePrefix + "players(uuid) ON DELETE CASCADE" +
                ")";

        try (Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
            statement.execute(walletsTable);
            statement.execute(sessionsTable);
        }
    }

    /**
     * Checks if a player is registered.
     *
     * @param uuid The player's UUID
     * @return true if the player is registered, false otherwise
     */
    public boolean isPlayerRegistered(UUID uuid) {
        String sql = SELECT + "password FROM " + tablePrefix + PLAYERS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString(PASSWORD_COLUMN);
                return storedPassword != null && !storedPassword.isEmpty();
            }

            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if player is registered", e);
            return false;
        }
    }

    /**
     * Registers a new player.
     *
     * @param player The player
     * @param password The password
     * @return true if registration was successful, false otherwise
     */
    public boolean registerPlayer(Player player, String password) {
        String sql = INSERT_INTO + tablePrefix + "players (uuid, username, password, ip) VALUES (?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE username = ?, password = ?, ip = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String hashedPassword = PasswordUtils.hashPassword(password);

            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, hashedPassword);
            statement.setString(4, player.getAddress().getAddress().getHostAddress());
            statement.setString(5, player.getName());
            statement.setString(6, hashedPassword);
            statement.setString(7, player.getAddress().getAddress().getHostAddress());

            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error registering player", e);
            return false;
        }
    }

    /**
     * Authenticates a player.
     *
     * @param uuid The player's UUID
     * @param password The password
     * @return true if authentication was successful, false otherwise
     */
    public boolean authenticatePlayer(UUID uuid, String password) {
        String sql = SELECT + "password FROM " + tablePrefix + PLAYERS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedPassword = resultSet.getString(PASSWORD_COLUMN);
                return PasswordUtils.verifyPassword(password, storedPassword);
            }

            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error authenticating player", e);
            return false;
        }
    }

    /**
     * Updates a player's password.
     *
     * @param uuid The player's UUID
     * @param newPassword The new password
     * @return true if the update was successful, false otherwise
     */
    public boolean updatePassword(UUID uuid, String newPassword) {
        String sql = UPDATE + tablePrefix + "players SET password = ? WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            String hashedPassword = PasswordUtils.hashPassword(newPassword);

            statement.setString(1, hashedPassword);
            statement.setString(2, uuid.toString());

            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating password", e);
            return false;
        }
    }

    /**
     * Updates a player's last login time.
     *
     * @param uuid The player's UUID
     * @param ip The player's IP address
     */
    public void updateLastLogin(UUID uuid, String ip) {
        String sql = UPDATE + tablePrefix + "players SET last_login = CURRENT_TIMESTAMP, ip = ? WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, ip);
            statement.setString(2, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error updating last login", e);
        }
    }

    /**
     * Saves a player's session.
     *
     * @param uuid The player's UUID
     * @param ip The player's IP address
     */
    public void saveSession(UUID uuid, String ip) {
        String sql = INSERT_INTO + tablePrefix + "sessions (uuid, ip) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE ip = ?, last_login = CURRENT_TIMESTAMP";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, ip);
            statement.setString(3, ip);
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving session", e);
        }
    }

    /**
     * Removes a player's session.
     *
     * @param uuid The player's UUID
     */
    public void removeSession(UUID uuid) {
        String sql = DELETE_FROM + tablePrefix + "sessions WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing session", e);
        }
    }

    public void savePlayer(Player player) {
        String sql = INSERT_INTO + tablePrefix + "players (uuid, username, ip) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE username = ?, ip = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, player.getAddress().getAddress().getHostAddress());
            statement.setString(4, player.getName());
            statement.setString(5, player.getAddress().getAddress().getHostAddress());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player data", e);
        }
    }

    /**
     * Checks if a wallet address is already in use by another player.
     *
     * @param walletAddress The wallet address to check
     * @param excludeUuid UUID to exclude from the check (can be null)
     * @return true if the wallet address is already in use, false otherwise
     */
    public boolean isWalletAddressInUse(String walletAddress, UUID excludeUuid) {
        String sql = SELECT + "uuid FROM " + tablePrefix + "wallets WHERE wallet_address = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, walletAddress);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                String uuidStr = resultSet.getString("uuid");
                // If we're excluding a UUID and this is it, continue checking
                if (excludeUuid != null && uuidStr.equals(excludeUuid.toString())) {
                    continue;
                }
                // If we found any other UUID using this wallet, return true
                return true;
            }

            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking if wallet address is in use", e);
            return false;
        }
    }

    /**
     * Gets the player UUID associated with a wallet address.
     *
     * @param walletAddress The wallet address
     * @return The player UUID, or empty if no player has this wallet address
     */
    public Optional<UUID> getPlayerByWalletAddress(String walletAddress) {
        String sql = SELECT + "uuid FROM " + tablePrefix + "wallets WHERE wallet_address = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, walletAddress);
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String uuidStr = resultSet.getString("uuid");
                return Optional.of(UUID.fromString(uuidStr));
            }

            return Optional.empty();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player by wallet address", e);
            return Optional.empty();
        }
    }

    /**
     * Connects a wallet to a player's account.
     *
     * @param uuid The player's UUID
     * @param walletAddress The wallet address
     * @param walletType The wallet type
     * @return true if the connection was successful, false otherwise
     */
    public boolean connectWallet(UUID uuid, String walletAddress, String walletType) {
        // First check if this wallet is already in use by another player
        if (isWalletAddressInUse(walletAddress, uuid)) {
            plugin.getLogger().info("Wallet address " + walletAddress + " is already in use by another player");
            return false;
        }

        String sql = INSERT_INTO + tablePrefix + "wallets (uuid, wallet_address, wallet_type) VALUES (?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE wallet_address = ?, wallet_type = ?, connected_at = CURRENT_TIMESTAMP";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.setString(2, walletAddress);
            statement.setString(3, walletType);
            statement.setString(4, walletAddress);
            statement.setString(5, walletType);

            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting wallet", e);
            return false;
        }
    }

    /**
     * Disconnects a wallet from a player's account.
     *
     * @param uuid The player's UUID
     * @return true if the disconnection was successful, false otherwise
     */
    public boolean disconnectWallet(UUID uuid) {
        String sql = DELETE_FROM + tablePrefix + WALLETS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());

            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error disconnecting wallet", e);
            return false;
        }
    }

    /**
     * Gets a player's wallet address.
     *
     * @param uuid The player's UUID
     * @return The wallet address, or empty if the player doesn't have a wallet connected
     */
    public Optional<String> getWalletAddress(UUID uuid) {
        String sql = SELECT + "wallet_address FROM " + tablePrefix + WALLETS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String walletAddress = resultSet.getString("wallet_address");
                return Optional.ofNullable(walletAddress);
            }

            return Optional.empty();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting wallet address", e);
            return Optional.empty();
        }
    }

    /**
     * Gets a player's wallet type.
     *
     * @param uuid The player's UUID
     * @return The wallet type, or empty if the player doesn't have a wallet connected
     */
    public Optional<String> getWalletType(UUID uuid) {
        String sql = SELECT + "wallet_type FROM " + tablePrefix + WALLETS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String walletType = resultSet.getString("wallet_type");
                return Optional.ofNullable(walletType);
            }

            return Optional.empty();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting wallet type", e);
            return Optional.empty();
        }
    }

    /**
     * Checks if a player has a wallet connected.
     *
     * @param uuid The player's UUID
     * @return true if the player has a wallet connected, false otherwise
     */
    public boolean hasWalletConnected(UUID uuid) {
        return getWalletAddress(uuid).isPresent();
    }

    /**
     * Sets a wallet as verified.
     *
     * @param uuid The player's UUID
     * @param verified Whether the wallet is verified
     * @return true if the update was successful, false otherwise
     */
    public boolean setWalletVerified(UUID uuid, boolean verified) {
        String sql = UPDATE + tablePrefix + "wallets SET verified = ? WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBoolean(1, verified);
            statement.setString(2, uuid.toString());

            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error setting wallet verification", e);
            return false;
        }
    }

    /**
     * Checks if a player's wallet is verified.
     *
     * @param uuid The player's UUID
     * @return true if the wallet is verified, false otherwise
     */
    public boolean isWalletVerified(UUID uuid) {
        String sql = SELECT + "verified FROM " + tablePrefix + WALLETS_WHERE_UUID;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                return resultSet.getBoolean("verified");
            }

            return false;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error checking wallet verification", e);
            return false;
        }
    }

    /**
     * Gets the total number of players in the database.
     *
     * @return The total number of players
     */
    public int getTotalPlayers() {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "players";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting total players", e);
            return 0;
        }
    }

    /**
     * Gets the number of registered players (with passwords).
     *
     * @return The number of registered players
     */
    public int getRegisteredPlayers() {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "players WHERE password IS NOT NULL";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting registered players", e);
            return 0;
        }
    }

    /**
     * Gets the number of wallet connections.
     *
     * @return The number of wallet connections
     */
    public int getWalletConnections() {
        String sql = "SELECT COUNT(*) FROM " + tablePrefix + "wallets";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting wallet connections", e);
            return 0;
        }
    }

    /**
     * Gets a list of all player names in the database.
     *
     * @return A list of all player names
     */
    public List<String> getAllPlayers() {
        String sql = "SELECT username FROM " + tablePrefix + "players ORDER BY username";
        List<String> players = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("username"));
            }
            return players;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting all players", e);
            return players;
        }
    }

    /**
     * Gets a list of registered player names.
     *
     * @return A list of registered player names
     */
    public List<String> getRegisteredPlayerNames() {
        String sql = "SELECT username FROM " + tablePrefix + "players WHERE password IS NOT NULL ORDER BY username";
        List<String> players = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("username"));
            }
            return players;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting registered players", e);
            return players;
        }
    }

    /**
     * Gets a list of unregistered player names.
     *
     * @return A list of unregistered player names
     */
    public List<String> getUnregisteredPlayerNames() {
        String sql = "SELECT username FROM " + tablePrefix + "players WHERE password IS NULL ORDER BY username";
        List<String> players = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("username"));
            }
            return players;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting unregistered players", e);
            return players;
        }
    }

    /**
     * Gets a list of player names with connected wallets.
     *
     * @return A list of player names with connected wallets
     */
    public List<String> getPlayersWithWallet() {
        String sql = "SELECT p.username FROM " + tablePrefix + "players p JOIN " + tablePrefix + "wallets w ON p.uuid = w.uuid ORDER BY p.username";
        List<String> players = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("username"));
            }
            return players;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting players with wallet", e);
            return players;
        }
    }

    /**
     * Gets a list of player names without connected wallets.
     *
     * @return A list of player names without connected wallets
     */
    public List<String> getPlayersWithoutWallet() {
        String sql = "SELECT p.username FROM " + tablePrefix + "players p LEFT JOIN " + tablePrefix + "wallets w ON p.uuid = w.uuid WHERE w.uuid IS NULL ORDER BY p.username";
        List<String> players = new ArrayList<>();

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                players.add(resultSet.getString("username"));
            }
            return players;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting players without wallet", e);
            return players;
        }
    }

    /**
     * Gets a player's UUID by their username.
     *
     * @param username The player's username
     * @return The player's UUID, or null if not found
     */
    public UUID getPlayerUUID(String username) {
        String sql = "SELECT uuid FROM " + tablePrefix + "players WHERE username = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return UUID.fromString(resultSet.getString("uuid"));
            }
            return null;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error getting player UUID", e);
            return null;
        }
    }

    /**
     * Purges inactive accounts older than the specified number of days.
     *
     * @param days The number of days
     * @return The number of purged accounts, or -1 if an error occurred
     */
    public int purgeInactiveAccounts(int days) {
        String sql = DELETE_FROM + tablePrefix + "players WHERE last_login < DATE_SUB(NOW(), INTERVAL ? DAY)";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, days);
            return statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error purging inactive accounts", e);
            return -1;
        }
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
