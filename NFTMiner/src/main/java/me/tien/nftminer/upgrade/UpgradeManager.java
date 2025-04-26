package me.tien.nftminer.upgrade;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;

public class UpgradeManager {
    private final NFTMiner plugin;
    private final TokenManager tokenManager;
    private final Map<String, Upgrade> upgrades = new HashMap<>();

    // Store references directly to avoid casting and key mismatches
    private TokenValueUpgrade tokenValueUpgrade;
    private SpeedUpgrade speedUpgrade;
    private InventoryUpgrade inventoryUpgrade;

    public UpgradeManager(NFTMiner plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.tokenValueUpgrade = new TokenValueUpgrade(plugin, tokenManager);
        // Initialize upgrades
        this.inventoryUpgrade = new InventoryUpgrade(plugin, tokenManager);
        plugin.getLogger().info("[UpgradeManager] All upgrades loaded successfully");
        // Initialize other upgrades...
        // Register all upgrades
        registerUpgrades();
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
        return upgrades.values().stream()
                .filter(upgrade -> upgrade instanceof InventoryUpgrade)
                .map(upgrade -> (InventoryUpgrade) upgrade)
                .findFirst()
                .orElse(null);
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
    }

    public void loadPlayerData(Player player) {
        inventoryUpgrade.loadPlayerData(player);
        // Load data for other upgrades...
    }
    
}