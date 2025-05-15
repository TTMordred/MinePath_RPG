package me.tien.miner_simulator.upgrade;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;

public class TokenValueUpgrade implements Upgrade {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<Integer, BigDecimal> upgradeCosts = new HashMap<>();
    private final Map<Integer, Double> multipliers = new HashMap<>();
    private final int maxLevel = 5;
    private final UpgradeDataManager dataManager;

    public TokenValueUpgrade(Miner_Simulator plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.dataManager = new UpgradeDataManager(plugin, "token-value");
        loadConfig();
    }

    @Override
    public void loadConfig() {
        FileConfiguration config = plugin.getConfig();
        String path = "token-value-upgrade";

        if (!config.isConfigurationSection(path)) {
            config.set(path + ".max-level", maxLevel);
            config.set(path + ".costs.1", 100);
            config.set(path + ".costs.2", 250);
            config.set(path + ".costs.3", 500);
            config.set(path + ".costs.4", 1000);
            config.set(path + ".costs.5", 2000);
            config.set(path + ".multipliers.1", 1.2);
            config.set(path + ".multipliers.2", 1.5);
            config.set(path + ".multipliers.3", 2.0);
            config.set(path + ".multipliers.4", 2.5);
            config.set(path + ".multipliers.5", 3.0);
            plugin.saveConfig();
        }

        for (String key : config.getConfigurationSection(path + ".costs").getKeys(false)) {
            int level = Integer.parseInt(key);
            BigDecimal cost = BigDecimal.valueOf(config.getDouble(path + ".costs." + key));
            upgradeCosts.put(level, cost);
        }

        for (String key : config.getConfigurationSection(path + ".multipliers").getKeys(false)) {
            int level = Integer.parseInt(key);
            double multiplier = config.getDouble(path + ".multipliers." + key);
            multipliers.put(level, multiplier);
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
        dataManager.setPlayerLevel(id, level);
    }

    @Override
    public void setLevel(UUID uuid, int level) {
        playerLevels.put(uuid, level);
        dataManager.setPlayerLevel(uuid, level);
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
            player.sendMessage("§c§lNotice: §r§cYou have reached the maximum level!");
            return false;
        }

        BigDecimal cost = upgradeCosts.get(currentLevel + 1);
        if (cost == null || !tokenManager.removeTokens(player, cost)) {
            player.sendMessage("§c§lNotice: §r§cYou don't have enough tokens to upgrade!");
            return false;
        }

        setLevel(player, currentLevel + 1);

        // Display new token value after upgrade
        double newMultiplier = getValueMultiplier(player);
        player.sendMessage(
                "§a§lNotice: §r§aUpgrade successful! Your token value has increased to x" + newMultiplier);

        return true;
    }

    @Override
    public String getType() {
        return "token-value";
    }

    @Override
    public int getEffectLevel(int level) {
        double multiplier = multipliers.getOrDefault(level, 1.0);
        return (int) ((multiplier - 1.0) * 100);
    }

    public double getValueMultiplier(Player player) {
        int level = getLevel(player);
        return multipliers.getOrDefault(level, 1.0);
    }

    @Override
    public void applyEffect(Player player) {
        // Không có hiệu ứng trực tiếp
    }

    public BigDecimal calculateTokenValue(Player player, BigDecimal baseTokens) {
        int level = getLevel(player);
        double multiplier = multipliers.getOrDefault(level, 1.0);
        return baseTokens.multiply(BigDecimal.valueOf(multiplier));
    }
}