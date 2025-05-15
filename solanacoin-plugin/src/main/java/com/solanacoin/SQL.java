package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;


import java.sql.*;
import java.util.UUID;

public class SQL {
    MinePathCoinPlugin plugin;
    public Connection connection;
    boolean isSQLite = false;
    public SQL(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    public void connectSQL(String type, String host, String port, String database, String username, String password, boolean useSSL) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:"+type+"://"+host+":"+port+"/"+database+"?useSSL="+(useSSL ? "true" : "false")+"&allowPublicKeyRetrieval=true", username, password);
    }

    public void connectSQLite(String location) throws SQLException {
        this.isSQLite = true;
        this.connection = DriverManager.getConnection("jdbc:sqlite:"+location);
    }

    public boolean isConnected() {
        if (this.connection != null) {
            try {
                return !connection.isClosed();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void disconnect() {
        if (this.isConnected()) {
            try {
                connection.close();
                plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.YELLOW + "Database disconnected.");
            } catch (SQLException e) {
                e.printStackTrace();
                plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "Database disconnect failed.");
            }
        }
    }

    public void setupBalanceTable() {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + plugin.getConfig().getString("dbTable")
                    + " (NAME VARCHAR(100),UUID VARCHAR(100),BALANCE DOUBLE(25,2),LASTREQUESTTIMESTAMP VARCHAR(100),PRIMARY KEY (UUID))"
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void setupSessionTable(){
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("CREATE TABLE IF NOT EXISTS session (UUID VARCHAR(100), SESSION_PUBLICKEY VARCHAR(100), PRIMARY KEY (UUID))"
            );
            ps.executeUpdate();
            String sql =     "CREATE TABLE IF NOT EXISTS session_secret (" +
            "SESSION_PUBLICKEY VARCHAR(100) PRIMARY KEY, " +
            "SECRET_B64 TEXT NOT NULL, " +
            "CREATED_AT TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP" +
            ");";
            PreparedStatement ps2 = this.connection.prepareStatement(sql);
            ps2.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    
    public boolean addPlayerToBalanceTable(OfflinePlayer player) {
        return addPlayerToBalanceTable(player.getName(), player.getUniqueId());
    }

    public boolean addPlayerToBalanceTable(Player player) {
        return addPlayerToBalanceTable(player.getName(), player.getUniqueId());
    }

    public boolean addPlayerToBalanceTable(String playerName, UUID playerUUID) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        try {
            if (!this.playerExistsInBalanceTable(playerUUID)) {
                PreparedStatement ps = this.connection.prepareStatement("INSERT " + (this.isSQLite ? "OR" : "") + " IGNORE INTO "+ plugin.getConfig().getString("dbTable")
                        + " (NAME, UUID, BALANCE, LASTREQUESTTIMESTAMP) VALUES (?, ?, "+plugin.startingBalance+", ?)"
                );
                ps.setString(1, playerName);
                ps.setString(2, playerUUID.toString());
                ps.setString(3, plugin.getNow().toString());
                ps.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }



    public boolean playerExistsInBalanceTable(UUID uuid) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM " + plugin.getConfig().getString("dbTable") + " WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addBalanceToPlayer(UUID uuid, double amount) {
        return this.setBalanceOfPlayer(uuid, getBalanceOfPlayer(uuid) + amount);
    }

    public boolean setBalanceOfPlayer(UUID uuid, double amount) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("UPDATE " + plugin.getConfig().getString("dbTable") + " SET BALANCE=? WHERE UUID=?");
            ps.setDouble(1, amount);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public double getBalanceOfPlayer(UUID uuid) {
        double balance = 0;
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return balance;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT BALANCE FROM " + plugin.getConfig().getString("dbTable") + " WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                balance = results.getDouble("BALANCE");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return balance;
    }

    public void deleteBalanceTable() {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement(
                    this.isSQLite ? ("DELETE FROM " + plugin.getConfig().getString("dbTable")) :
                    ("TRUNCATE " + plugin.getConfig().getString("dbTable"))
            );
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removePlayerFromBalanceTable(UUID uuid) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("DELETE FROM " + plugin.getConfig().getString("dbTable") + " WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getLastRequestTimestamp(UUID uuid) {
        String lastTimestamp = "";
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return "";
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT LASTREQUESTTIMESTAMP FROM " + plugin.getConfig().getString("dbTable") + " WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                lastTimestamp = results.getString("LASTREQUESTTIMESTAMP");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lastTimestamp;
    }

    public boolean setLastRequestTimestamp(UUID uuid, String timestamp) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("UPDATE " + plugin.getConfig().getString("dbTable") + " SET LASTREQUESTTIMESTAMP=? WHERE UUID=?");
            ps.setString(1, timestamp);
            ps.setString(2, uuid.toString());
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    public String fetchWalletAddress (String uuid) {
        String walletAddress = null;
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return null;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT wallet_address FROM walletlogin_wallets WHERE uuid = ?");
            ps.setString(1, uuid);
            ResultSet results = ps.executeQuery();
            if (results.next()) {
                walletAddress = results.getString("wallet_address");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return walletAddress;
    }
    public boolean playerExistsInWalletTable(UUID uuid) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        try {
            PreparedStatement ps = this.connection.prepareStatement("SELECT * FROM walletlogin_wallets  WHERE UUID=?");
            ps.setString(1, uuid.toString());
            ResultSet results = ps.executeQuery();
            return results.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
    public boolean addSession(String uuid, String sessionPublicKey) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        String sql = ""
          + "INSERT INTO session(UUID, SESSION_PUBLICKEY) "
          + "VALUES ( ?, ?) ";

        try  {
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.setString(2, sessionPublicKey);
            ps.executeUpdate();
            return true;
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String getSessionPublicKey(String uuid){
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return null;
        }
        String sql = "SELECT SESSION_PUBLICKEY FROM session WHERE UUID = ?;";
        try{
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, uuid);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("SESSION_PUBLICKEY");
                }
                return null;
            }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean addPlayerToSessionTable(String uuid) {
        if (!this.isConnected()) {
            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix+ChatColor.RED + "SQL not connected, check config.");
            return false;
        }
        String sql = ""
        + "INSERT INTO session(UUID) "
        + "VALUES (?) "
        + "ON CONFLICT (UUID) DO NOTHING;";
        try {
            PreparedStatement ps = this.connection.prepareStatement(sql);
            ps.setString(1, uuid);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to add player UUID to session DB: " + e.getMessage());
        }
        return false;
    }
}
