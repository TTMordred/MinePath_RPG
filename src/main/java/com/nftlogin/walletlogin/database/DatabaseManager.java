package com.nftlogin.walletlogin.database;

import com.nftlogin.walletlogin.WalletLogin;
import org.bukkit.entity.Player;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {

    private final WalletLogin plugin;
    private Connection connection;
    private final String host;
    private final int port;
    private final String database;
    private final String username;
    private final String password;
    private final String tablePrefix;

    public DatabaseManager(WalletLogin plugin) {
        this.plugin = plugin;
        this.host = plugin.getConfig().getString("database.host", "localhost");
        this.port = plugin.getConfig().getInt("database.port", 3306);
        this.database = plugin.getConfig().getString("database.database", "minecraft");
        this.username = plugin.getConfig().getString("database.username", "root");
        this.password = plugin.getConfig().getString("database.password", "password");
        this.tablePrefix = plugin.getConfig().getString("database.table-prefix", "walletlogin_");
    }

    public void connect() throws SQLException {
        String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=false";
        connection = DriverManager.getConnection(url, username, password);
    }

    public void createTables() throws SQLException {
        String playersTable = "CREATE TABLE IF NOT EXISTS " + tablePrefix + "players (" +
                "uuid VARCHAR(36) PRIMARY KEY, " +
                "username VARCHAR(16) NOT NULL, " +
                "wallet_address VARCHAR(255), " +
                "connected_at TIMESTAMP NULL, " +
                "last_login TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP" +
                ")";
        
        try (Statement statement = connection.createStatement()) {
            statement.execute(playersTable);
        }
    }

    public void savePlayer(Player player) {
        String sql = "INSERT INTO " + tablePrefix + "players (uuid, username) VALUES (?, ?) " +
                "ON DUPLICATE KEY UPDATE username = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, player.getUniqueId().toString());
            statement.setString(2, player.getName());
            statement.setString(3, player.getName());
            statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error saving player data", e);
        }
    }

    public boolean connectWallet(UUID playerUuid, String walletAddress) {
        String sql = "UPDATE " + tablePrefix + "players SET wallet_address = ?, connected_at = CURRENT_TIMESTAMP " +
                "WHERE uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, walletAddress);
            statement.setString(2, playerUuid.toString());
            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error connecting wallet", e);
            return false;
        }
    }

    public boolean disconnectWallet(UUID playerUuid) {
        String sql = "UPDATE " + tablePrefix + "players SET wallet_address = NULL, connected_at = NULL " +
                "WHERE uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
            int updated = statement.executeUpdate();
            return updated > 0;
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Error disconnecting wallet", e);
            return false;
        }
    }

    public Optional<String> getWalletAddress(UUID playerUuid) {
        String sql = "SELECT wallet_address FROM " + tablePrefix + "players WHERE uuid = ?";
        
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, playerUuid.toString());
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

    public boolean hasWalletConnected(UUID playerUuid) {
        return getWalletAddress(playerUuid).isPresent();
    }

    public void closeConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }
}
