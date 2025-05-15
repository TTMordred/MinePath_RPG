package me.tien.miner_simulator.upgrade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;

public class SpeedUpgrade implements Upgrade {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<Integer, BigDecimal> upgradeCosts = new HashMap<>();
    private final Map<Integer, Integer> upgradeEffects = new HashMap<>();
    private final int maxLevel = 5;
    private final UpgradeDataManager dataManager;

    public SpeedUpgrade(Miner_Simulator plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.dataManager = new UpgradeDataManager(plugin, "speed");
        loadConfig();
    }

    @Override
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        if (!config.isConfigurationSection("speed-upgrade")) {
            config.set("speed-upgrade.max-level", maxLevel);
            config.set("speed-upgrade.costs.1", 0);
            config.set("speed-upgrade.costs.2", 150);
            config.set("speed-upgrade.costs.3", 300);
            config.set("speed-upgrade.costs.4", 500);
            config.set("speed-upgrade.costs.5", 1000);
            config.set("speed-upgrade.effects.1", 1);
            config.set("speed-upgrade.effects.2", 2);
            config.set("speed-upgrade.effects.3", 3);
            config.set("speed-upgrade.effects.4", 4);
            config.set("speed-upgrade.effects.5", 5);
            plugin.saveConfig();
        }

        for (String key : config.getConfigurationSection("speed-upgrade.costs").getKeys(false)) {
            int level = Integer.parseInt(key);
            BigDecimal cost = BigDecimal.valueOf(config.getDouble("speed-upgrade.costs." + key));
            upgradeCosts.put(level, cost);
        }

        for (String key : config.getConfigurationSection("speed-upgrade.effects").getKeys(false)) {
            int level = Integer.parseInt(key);
            int effect = config.getInt("speed-upgrade.effects." + key);
            upgradeEffects.put(level, effect);
        }
    }

    @Override
    public void loadPlayerData(Player player) {
        UUID id = player.getUniqueId();
        int level = dataManager.getPlayerLevel(id);
        playerLevels.put(id, level);
    }

    @Override
    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
            dataManager.setPlayerLevel(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public int getLevel(Player player) {
        return playerLevels.getOrDefault(player.getUniqueId(), 0);
    }

    @Override
    public int getLevel(UUID uuid) {
        return playerLevels.getOrDefault(uuid, 0);
    }

    @Override
    public void setLevel(Player player, int level) {
        UUID id = player.getUniqueId();
        playerLevels.put(id, level);
        dataManager.setPlayerLevel(id, level); // Save to player file
        applyEffect(player); // Apply effect immediately after level change
    }

    @Override
    public void setLevel(UUID uuid, int level) {
        playerLevels.put(uuid, level);
        dataManager.setPlayerLevel(uuid, level); // Save to player file
    }

    @Override
    public int getNextLevelCost(Player player) {
        int currentLevel = getLevel(player);
        int nextLevel = currentLevel + 1;
        BigDecimal cost = upgradeCosts.get(nextLevel);
        return cost != null ? cost.intValue() : -1;
    }

    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public boolean upgrade(Player player) {
        int currentLevel = getLevel(player);
        if (currentLevel >= maxLevel) {
            player.sendMessage("§c§lNotice: §r§cYou've reached the maximum level!");
            return false;
        }

        BigDecimal cost = upgradeCosts.get(currentLevel + 1);
        if (cost == null || !tokenManager.removeTokens(player, cost)) {
            player.sendMessage("§c§lNotice: §r§cYou don't have enough tokens to upgrade. Required: " + cost.intValue()
                    + " tokens.");
            return false;
        }

        setLevel(player, currentLevel + 1);

        // Success notification
        int newLevel = currentLevel + 1;
        int newEffect = getEffectLevel(newLevel);
        player.sendMessage("§a§lSuccess: §r§aSpeed upgrade increased to level " + newLevel + "!");
        player.sendMessage("§e§lEffect: §r§eMining speed +" + newEffect + " level");

        // Sound and particle effects
        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);

        return true;
    }

    @Override
    public String getType() {
        return "haste";
    }

    @Override
    public int getEffectLevel(int level) {
        return upgradeEffects.getOrDefault(level, 0);
    }

    @Override
    public void applyEffect(Player player) {
        int level = getLevel(player);
        int effectLevel = getEffectLevel(level);

        if (effectLevel > 0) {
            // Apply Haste effect
            player.addPotionEffect(
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, effectLevel - 1, true, false));
            plugin.getLogger().info("Applied Haste effect level " + effectLevel + " to " + player.getName());
        } else {
            // Remove effect if no level
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            plugin.getLogger().info("Removed Haste effect from " + player.getName());
        }
    }
}