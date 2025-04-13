package com.nftlogin.walletlogin.database;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.utils.PasswordUtils;

import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * SQL implementation of the DatabaseManager interface.
 */
public class SQLDatabaseManager implements DatabaseManager {

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

    public SQLDatabaseManager(SolanaLogin plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("database.host", "localhost");
        this.port = plugin.getConfig().getInt("database.port", 3306);
        this.database = plugin.getConfig().getString("database.database", "minecraft");
        this.username = plugin.getConfig().getString("database.username", "root");
        this.password = plugin.getConfig().getString("database.password", DEFAULT_PASSWORD);
        this.tablePrefix = plugin.getConfig().getString("database.table-prefix", "walletlogin_");
    }

    @Override
    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false&allowPublicKeyRetrieval=true";
        connection = DriverManager.getConnection(url, username, password);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public void removeSession(UUID uuid) {
        String sql = DELETE_FROM + tablePrefix + "sessions WHERE uuid = ?";

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, uuid.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error removing session", e);
        }
    }

    @Override
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

    @Override
    public boolean connectWallet(UUID uuid, String walletAddress, String walletType) {
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

    @Override
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

    @Override
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

    @Override
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

    @Override
    public boolean hasWalletConnected(UUID uuid) {
        return getWalletAddress(uuid).isPresent();
    }

    @Override
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

    @Override
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

    @Override
    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
