package me.tien.miner_simulator;

import me.tien.miner_simulator.commands.*;
import me.tien.miner_simulator.listeners.MiningListener;
import me.tien.miner_simulator.world.MineProtectionListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import me.tien.miner_simulator.gui.ShopGUI;
import me.tien.miner_simulator.integration.NFTPluginIntegration;
import me.tien.miner_simulator.listeners.InventoryListener;
import me.tien.miner_simulator.listeners.PlayerListener;
import me.tien.miner_simulator.listeners.ShopListener;
import me.tien.miner_simulator.token.TokenManager;
import me.tien.miner_simulator.upgrade.InventoryUpgrade;
import me.tien.miner_simulator.upgrade.SpeedUpgrade;
import me.tien.miner_simulator.upgrade.TokenValueUpgrade;
import me.tien.miner_simulator.upgrade.UpgradeManager;
import me.tien.miner_simulator.world.VoidMine;

public class Miner_Simulator extends JavaPlugin {
    private VoidMine voidMine;
    private NFTPluginIntegration nftIntegration;
    private TokenManager tokenManager;
    private UpgradeManager upgradeManager;
    private InventoryUpgrade inventoryUpgrade;
    private ShopGUI shopGUI;
    private SpeedUpgrade speedUpgrade;
    private TokenValueUpgrade tokenValueUpgrade;
    private MineProtectionListener mineProtectionListener;

    @Override
    public void onEnable() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            // Initialize NFTPlugin integration
            nftIntegration = new NFTPluginIntegration(this);

            // Initialize VoidMine
            voidMine = new VoidMine(this);

            // Initialize MineProtectionListener
            mineProtectionListener = voidMine.getProtectionListener();
            getLogger().info("MineProtectionListener has been initialized from VoidMine");

            // 1) First create a temporary TokenManager (without upgradeManager)
            tokenManager = new TokenManager(this);
            getLogger().info("TokenManager initialized successfully");
            // 2) Create UpgradeManager, pass in TokenManager
            upgradeManager = new UpgradeManager(this, tokenManager);
            getLogger().info("UpgradeManager initialized successfully");
            // 3) Now TokenManager can access tokenValueUpgrade
            tokenManager.setTokenValueUpgrade(upgradeManager.getTokenValueUpgrade());
            tokenValueUpgrade = upgradeManager.getTokenValueUpgrade();
            // Initialize GUI
            shopGUI = new ShopGUI(this, tokenManager, upgradeManager);
            inventoryUpgrade = new InventoryUpgrade(this, tokenManager);
            speedUpgrade = upgradeManager.getSpeedUpgrade();
            // Save default config
            saveDefaultConfig();
            // Register commands
            getCommand("claim").setExecutor(new ClaimCommand(this, tokenManager, tokenValueUpgrade));
            getCommand("token").setExecutor(new TokenCommand(this, tokenManager));
            getCommand("shop").setExecutor(new ShopCommand(this, shopGUI));
            getCommand("miningbox").setExecutor(new MiningBoxCommand(this, voidMine));
            getCommand("resetmine").setExecutor(new ResetMineCommand(voidMine));
            getCommand("resetupgrades").setExecutor(new ResetUpgradeCommand(this, upgradeManager));
            getCommand("help").setExecutor(new HelpCommand(this));
            // Register listeners
            getServer().getPluginManager().registerEvents(new ShopListener(shopGUI), this);
            getServer().getPluginManager().registerEvents(new PlayerListener(upgradeManager), this);
            getServer().getPluginManager().registerEvents(new MiningListener(this), this);
            getServer().getPluginManager().registerEvents(new InventoryListener(this, inventoryUpgrade), this);

            // Ensure MineProtectionListener is registered with highest priority
            getServer().getPluginManager().registerEvents(mineProtectionListener, this);
            getLogger().info("MineProtectionListener registered with highest priority");

            if (nftIntegration.isNFTPluginAvailable()) {
                getLogger().info("NFTPlugin found - NFT features have been activated!");
            } else {
                getLogger().warning("NFTPlugin not found - NFT features are disabled!");
            }
        }, 40L);
    } // wait 1 second (20 ticks) after server load

    /**
     * Get NFT Integration
     */
    public NFTPluginIntegration getNFTIntegration() {
        return nftIntegration;
    }

    /**
     * Get VoidMine
     */
    public VoidMine getVoidMine() {
        return voidMine;
    }

    /**
     * Get MineProtectionListener
     */
    public MineProtectionListener getMineProtectionListener() {
        return mineProtectionListener;
    }

    /** Add this getter: */
    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }
}