package com.minecraft.nftplugin;

import com.minecraft.nftplugin.achievements.AchievementManager;
import com.minecraft.nftplugin.commands.MintNFTCommand;
import com.minecraft.nftplugin.commands.NFTInfoCommand;

import com.minecraft.nftplugin.commands.NFTListCommand;
import com.minecraft.nftplugin.commands.ResetNFTCommand;
import com.minecraft.nftplugin.database.DatabaseManager;

import com.minecraft.nftplugin.integration.SolanaLoginIntegration;
import com.minecraft.nftplugin.listeners.BlockBreakListener;
import com.minecraft.nftplugin.listeners.InventoryListener;
import com.minecraft.nftplugin.listeners.PlayerListener;
import com.minecraft.nftplugin.metadata.MetadataManager;
import com.minecraft.nftplugin.solana.SolanaService;
import com.minecraft.nftplugin.utils.ConfigManager;
import com.minecraft.nftplugin.utils.ItemManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.logging.Level;

public class NFTPlugin extends JavaPlugin {

    private static NFTPlugin instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private SolanaService solanaService;
    private ItemManager itemManager;
    private SolanaLoginIntegration solanaLoginIntegration;
    private AchievementManager achievementManager;
    private MetadataManager metadataManager;


    @Override
    public void onEnable() {
        // Set instance
        instance = this;

        // Initialize config
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // Initialize database
        databaseManager = new DatabaseManager(this);
        if (!databaseManager.initialize()) {
            getLogger().severe("Failed to initialize database. Disabling plugin.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        // Initialize SolanaLogin integration
        solanaLoginIntegration = new SolanaLoginIntegration(this);
        if (!solanaLoginIntegration.isSolanaLoginAvailable()) {
            getLogger().warning("SolanaLogin plugin not found. Players will need to register their wallets through SolanaLogin to use NFT features.");
        }

        // Extract solana-backend files
        extractSolanaBackendFiles();

        // Initialize Solana service
        solanaService = new SolanaService(this);

        // Initialize item manager
        itemManager = new ItemManager(this);

        // Initialize metadata manager
        metadataManager = new MetadataManager(this);



        // Initialize achievement manager
        achievementManager = new AchievementManager(this);
        achievementManager.initializeAchievements();

        // Register commands
        getCommand("nftinfo").setExecutor(new NFTInfoCommand(this));
        getCommand("nftlist").setExecutor(new NFTListCommand(this));

        getCommand("resetnft").setExecutor(new ResetNFTCommand(this));
        getCommand("mintnft").setExecutor(new MintNFTCommand(this));

        // Register event listeners
        Bukkit.getPluginManager().registerEvents(new BlockBreakListener(this), this);
        Bukkit.getPluginManager().registerEvents(new InventoryListener(this), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);

        getLogger().info("NFTPlugin has been enabled!");
    }

    @Override
    public void onDisable() {
        // Close database connections
        if (databaseManager != null) {
            databaseManager.close();
        }

        getLogger().info("NFTPlugin has been disabled!");
    }

    /**
     * Get the plugin instance
     * @return The plugin instance
     */
    public static NFTPlugin getInstance() {
        return instance;
    }

    /**
     * Get the config manager
     * @return The config manager
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Get the database manager
     * @return The database manager
     */
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Get the Solana service
     * @return The Solana service
     */
    public SolanaService getSolanaService() {
        return solanaService;
    }

    /**
     * Get the item manager
     * @return The item manager
     */
    public ItemManager getItemManager() {
        return itemManager;
    }

    /**
     * Get the SolanaLogin integration
     * @return The SolanaLogin integration
     */
    public SolanaLoginIntegration getSolanaLoginIntegration() {
        return solanaLoginIntegration;
    }

    /**
     * Get the achievement manager
     * @return The achievement manager
     */
    public AchievementManager getAchievementManager() {
        return achievementManager;
    }

    /**
     * Get the metadata manager
     * @return The metadata manager
     */
    public MetadataManager getMetadataManager() {
        return metadataManager;
    }



    /**
     * Log a message with the specified level
     * @param level The log level
     * @param message The message to log
     */
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    /**
     * Extract solana-backend files from the plugin JAR to the plugin data folder
     */
    private void extractSolanaBackendFiles() {
        File backendDir = new File(getDataFolder(), "solana-backend");
        if (!backendDir.exists()) {
            backendDir.mkdirs();
            getLogger().info("Created solana-backend directory");
        }

        // List of files to extract
        String[] files = {
            "mint-nft.js",
            "package.json",
            "README.md",
            ".env.example",
            "setup-backend.sh",
            "setup-backend.bat"
        };

        for (String fileName : files) {
            File file = new File(backendDir, fileName);
            if (!file.exists()) {
                try {
                    saveResource("solana-backend/" + fileName, false);
                    getLogger().info("Extracted " + fileName + " to solana-backend directory");
                } catch (Exception e) {
                    getLogger().warning("Failed to extract " + fileName + ": " + e.getMessage());
                }
            }
        }

        getLogger().info("Solana backend files extracted to: " + backendDir.getAbsolutePath());
    }
}
