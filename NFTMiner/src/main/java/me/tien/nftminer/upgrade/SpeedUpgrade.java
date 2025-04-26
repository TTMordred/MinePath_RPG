package me.tien.nftminer.upgrade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;

public class SpeedUpgrade implements Upgrade {
    private final NFTMiner plugin;
    private final TokenManager tokenManager;
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<Integer, BigDecimal> upgradeCosts = new HashMap<>();
    private final Map<Integer, Integer> upgradeEffects = new HashMap<>();
    private final int maxLevel = 5;

    public SpeedUpgrade(NFTMiner plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
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
        int level = plugin.getConfig().getInt("player-data." + id + ".speed-level", 0);
        playerLevels.put(id, level);
    }

    @Override
    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
            plugin.getConfig().set("player-data." + entry.getKey() + ".speed-level", entry.getValue());
        }
        plugin.saveConfig();
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
        plugin.getScoreboardManager().updateScoreboard(player);
        saveData();
        applyEffect(player); // Áp dụng hiệu ứng ngay sau khi cấp độ thay đổi
    }

    @Override
    public void setLevel(UUID uuid, int level) {
        playerLevels.put(uuid, level);
        saveData();
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
            return false;
        }

        BigDecimal cost = upgradeCosts.get(currentLevel + 1);
        if (cost == null || !tokenManager.removeTokens(player, cost)) {
            return false;
        }

        setLevel(player, currentLevel + 1);
        return true;
    }

    @Override
    public String getType() {
        return "speed";
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
            // Áp dụng hiệu ứng Haste
            player.addPotionEffect(
                    new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, effectLevel - 1, true, false));
            plugin.getLogger().info("Đã áp dụng hiệu ứng Haste cấp " + effectLevel + " cho " + player.getName());
        } else {
            // Xóa hiệu ứng nếu không có cấp độ
            player.removePotionEffect(PotionEffectType.FAST_DIGGING);
            plugin.getLogger().info("Đã xóa hiệu ứng Haste cho " + player.getName());
        }
    }
}