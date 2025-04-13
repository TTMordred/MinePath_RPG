package com.nftlogin.walletlogin;

import com.nftlogin.walletlogin.commands.*;
import com.nftlogin.walletlogin.database.DatabaseManager;
import com.nftlogin.walletlogin.database.SQLDatabaseManager;
import com.nftlogin.walletlogin.listeners.PlayerLoginListener;
import com.nftlogin.walletlogin.session.SessionManager;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;
import java.util.logging.Level;

public final class SolanaLogin extends JavaPlugin {

    private DatabaseManager databaseManager;
    private SessionManager sessionManager;

    @Override
    public void onEnable() {
        // Save default config if it doesn't exist
        saveDefaultConfig();

        // Initialize session manager
        sessionManager = new SessionManager(this);

        // Initialize database
        initDatabase();

        // Register event listeners
        getServer().getPluginManager().registerEvents(new PlayerLoginListener(this), this);

        // Register commands
        getCommand("register").setExecutor(new RegisterCommand(this));
        getCommand("login").setExecutor(new LoginCommand(this));
        getCommand("changepassword").setExecutor(new ChangePasswordCommand(this));
        getCommand("logout").setExecutor(new LogoutCommand(this));
        getCommand("connectwallet").setExecutor(new ConnectWalletCommand(this));
        getCommand("disconnectwallet").setExecutor(new DisconnectWalletCommand(this));
        getCommand("walletinfo").setExecutor(new WalletInfoCommand(this));
        getCommand("solanalogin").setExecutor(new AdminCommand(this));

        getLogger().info("SolanaLogin plugin has been enabled!");
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

        getLogger().info("SolanaLogin plugin has been disabled!");
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        getLogger().info("Configuration reloaded.");
    }

    private void initDatabase() {
        try {
            // Storage mode is currently fixed to SQL

            // For now, we'll just use SQL storage mode
            // In the future, we can implement Solana and hybrid storage modes
            databaseManager = new SQLDatabaseManager(this);
            getLogger().info("Using SQL storage mode");

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

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    public String formatMessage(String message) {
        String prefix = getConfig().getString("messages.prefix", "&8[&6SolanaLogin&8] &r");
        return ChatColor.translateAlternateColorCodes('&', prefix + message);
    }

    /**
     * Get the web server URL from the config.
     *
     * @return The web server URL
     */
    public String getWebServerUrl() {
        return getConfig().getString("web-server.url", "http://localhost:3000");
    }
}
