package com.nftlogin.walletlogin;

import com.nftlogin.walletlogin.commands.ConnectWalletCommand;
import com.nftlogin.walletlogin.commands.DisconnectWalletCommand;
import com.nftlogin.walletlogin.commands.WalletInfoCommand;
import com.nftlogin.walletlogin.database.DatabaseManager;
import com.nftlogin.walletlogin.listeners.PlayerLoginListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class WalletLogin extends JavaPlugin {

    private DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();
        
        // Initialize database
        initDatabase();
        
        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);
        
        // Register commands
        getCommand("connectwallet").setExecutor(new ConnectWalletCommand(this));
        getCommand("disconnectwallet").setExecutor(new DisconnectWalletCommand(this));
        getCommand("walletinfo").setExecutor(new WalletInfoCommand(this));
        
        getLogger().info("WalletLogin plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close database connection
        if (databaseManager != null) {
            try {
                databaseManager.closeConnection();
                getLogger().info("Database connection closed.");
            } catch (SQLException e) {
                getLogger().log(Level.SEVERE, "Error closing database connection", e);
            }
        }
        
        getLogger().info("WalletLogin plugin has been disabled!");
    }
    
    private void initDatabase() {
        try {
            databaseManager = new DatabaseManager(this);
            databaseManager.connect();
            databaseManager.createTables();
            getLogger().info("Database connection established successfully.");
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Failed to initialize database", e);
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
    
    public String formatMessage(String message) {
        String prefix = getConfig().getString("messages.prefix", "&8[&6WalletLogin&8] &r");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
}
