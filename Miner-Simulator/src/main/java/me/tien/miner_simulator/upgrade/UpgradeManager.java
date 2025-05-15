package me.tien.miner_simulator.upgrade;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class UpgradeManager {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final Map<String, Upgrade> upgrades = new HashMap<>();

    // Store references directly to avoid casting and key mismatches
    private TokenValueUpgrade tokenValueUpgrade;
    private SpeedUpgrade speedUpgrade;
    private InventoryUpgrade inventoryUpgrade;

    public UpgradeManager(Miner_Simulator plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        // Initialize upgrades
        plugin.getLogger().info("[UpgradeManager] Loading upgrade types...");
        // Register all upgrades
        registerUpgrades();
        plugin.getLogger().info("[UpgradeManager] Successfully loaded " + upgrades.size() + " upgrade types.");
    }

    /**
     * Register all upgrade types
     */
    private void registerUpgrades() {
        // Speed upgrade
        speedUpgrade = new SpeedUpgrade(plugin, tokenManager);
        upgrades.put(speedUpgrade.getType(), speedUpgrade);

        // Inventory upgrade
        inventoryUpgrade = new InventoryUpgrade(plugin, tokenManager);
        upgrades.put(inventoryUpgrade.getType(), inventoryUpgrade);

        // Token value upgrade
        tokenValueUpgrade = new TokenValueUpgrade(plugin, tokenManager);
        upgrades.put(tokenValueUpgrade.getType(), tokenValueUpgrade);
    }

    /**
     * Get upgrade by type
     * 
     * @param type Upgrade type
     * @return The upgrade instance
     */
    public Upgrade getUpgrade(String type) {
        return upgrades.get(type);
    }

    /**
     * Get the speed upgrade
     * 
     * @return The speed upgrade instance
     */
    public SpeedUpgrade getSpeedUpgrade() {
        return speedUpgrade;
    }

    /**
     * Get the inventory upgrade
     * 
     * @return The inventory upgrade instance
     */
    public InventoryUpgrade getInventoryUpgrade() {
        return inventoryUpgrade;
    }

    /**
     * Get the token value upgrade
     * 
     * @return The token value upgrade instance
     */
    public TokenValueUpgrade getTokenValueUpgrade() {
        return tokenValueUpgrade;
    }

    /**
     * Apply all upgrade effects to player
     * 
     * @param player Player to apply effects to
     */
    public void applyAllEffects(Player player) {
        for (Upgrade upgrade : upgrades.values()) {
            upgrade.applyEffect(player);
        }
    }
    
    /**
     * Save data for all upgrades
     */
    public void saveAllData() {
        for (Upgrade upgrade : upgrades.values()) {
            upgrade.saveData();
        }
        plugin.getLogger().info("[UpgradeManager] Saved upgrade data for all players.");
    }

    /**
     * Load data for a player for all upgrades
     * 
     * @param player Player to load data for
     */
    public void loadPlayerData(Player player) {
        for (Upgrade upgrade : upgrades.values()) {
            upgrade.loadPlayerData(player);
        }
        plugin.getLogger().info("[UpgradeManager] Loaded upgrade data for player: " + player.getName());
    }
}