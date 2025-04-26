package me.tien.nftminer.token;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.integration.MinePathIntegration;
import me.tien.nftminer.upgrade.TokenValueUpgrade;

public class TokenManager {
    private final NFTMiner plugin;
    private final MinePathIntegration minePathIntegration;
    private TokenValueUpgrade tokenValueUpgrade;
    private final Map<Material, BigDecimal> blockValues = new HashMap<>();
    private final List<Material> supportedMaterials = Arrays.asList(
            Material.COBBLESTONE,
            Material.RAW_IRON,
            Material.RAW_GOLD,
            Material.DIAMOND,
            Material.STONE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE);
    private final Map<UUID, BigDecimal> fallbackTokens = new HashMap<>();

    public TokenManager(NFTMiner plugin) {
        this.plugin = plugin;
        this.minePathIntegration = new MinePathIntegration(plugin);

        if (!minePathIntegration.isMinePathAvailable()) {
            plugin.getLogger().warning("MinePath không được tìm thấy! Token sẽ được lưu trữ tạm thời.");
        } else {
            plugin.getLogger().info("Đã kết nối với MinePath thành công. Sử dụng cột balance làm token.");
        }

        loadBlockValues();
    }

    private void loadBlockValues() {
        FileConfiguration config = plugin.getConfig();

        // Kiểm tra nếu phần "block-values" chưa tồn tại trong file config
        if (!config.isConfigurationSection("block-values")) {
            // Thiết lập giá trị mặc định cho các block
            config.set("block-values.COBBLESTONE", 0.005);
            config.set("block-values.RAW_IRON", 0.08);
            config.set("block-values.RAW_GOLD", 0.4);
            config.set("block-values.DIAMOND", 0.9);
            config.set("block-values.STONE", 0.01);
            config.set("block-values.IRON_ORE", 0.1);
            config.set("block-values.GOLD_ORE", 0.5);
            config.set("block-values.DIAMOND_ORE", 0.1);
            plugin.saveConfig();
        }

        // Xóa giá trị cũ và tải giá trị mới từ file config
        blockValues.clear();
        ConfigurationSection section = config.getConfigurationSection("block-values");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double value = section.getDouble(key, 0.0);
                    if (value > 0) {
                        blockValues.put(material, BigDecimal.valueOf(value));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Material không hợp lệ trong config: " + key);
                }
            }
        }

        plugin.getLogger().info("Đã tải " + blockValues.size() + " giá trị block từ config");
    }

    public BigDecimal getTokens(UUID uuid) {
        if (minePathIntegration.isMinePathAvailable()) {
            return minePathIntegration.getBalance(uuid);
        }
        return fallbackTokens.getOrDefault(uuid, BigDecimal.ZERO);
    }

    public BigDecimal getTokens(Player player) {
        return getTokens(player.getUniqueId());
    }

    public boolean addTokens(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return false;
        if (minePathIntegration.isMinePathAvailable()) {
            return minePathIntegration.addBalance(uuid, amount);
        }
        BigDecimal current = getTokens(uuid);
        fallbackTokens.put(uuid, current.add(amount));
        return true;
    }

    public boolean addTokens(Player player, BigDecimal amount) {
        return addTokens(player.getUniqueId(), amount);
    }

    public boolean removeTokens(UUID uuid, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0)
            return false;
        BigDecimal current = getTokens(uuid);
        if (current.compareTo(amount) < 0)
            return false;
        if (minePathIntegration.isMinePathAvailable()) {
            return minePathIntegration.subtractBalance(uuid, amount);
        }
        fallbackTokens.put(uuid, current.subtract(amount));
        return true;
    }

    public boolean removeTokens(Player player, BigDecimal amount) {
        return removeTokens(player.getUniqueId(), amount);
    }

    public boolean hasTokenValue(Material material) {
        return blockValues.containsKey(material);
    }

    public BigDecimal getBlockValue(Material material) {
        return blockValues.getOrDefault(material, BigDecimal.ZERO);
    }

    public boolean hasEnoughTokens(Player player, BigDecimal amount) {
        BigDecimal currentTokens = getTokens(player);
        return currentTokens.compareTo(amount) >= 0;
    }

    public boolean hasEnoughTokens(Player player, int amount) {
        return hasEnoughTokens(player, new BigDecimal(amount));
    }

    public ClaimResult claimBlocks(Player player) {
        BigDecimal baseTokens = BigDecimal.ZERO;
        int totalBlocks = 0;
        Map<Material, Integer> claimedItems = new HashMap<>();

        for (ItemStack item : player.getInventory().getContents()) {
            if (item == null)
                continue;
            Material mat = item.getType();
            if (!hasTokenValue(mat))
                continue;

            int amount = item.getAmount();
            BigDecimal value = getBlockValue(mat).multiply(BigDecimal.valueOf(amount));
            baseTokens = baseTokens.add(value);
            totalBlocks += amount;
            claimedItems.put(mat, claimedItems.getOrDefault(mat, 0) + amount);
            player.getInventory().remove(item);
        }

        BigDecimal finalTokens = tokenValueUpgrade.calculateTokenValue(player, baseTokens);

        if (finalTokens.compareTo(BigDecimal.ZERO) > 0) {
            addTokens(player, finalTokens);
        }

        return new ClaimResult(finalTokens, totalBlocks, claimedItems);
    }

    public List<Material> getSupportedBlocks() {
        return new ArrayList<>(blockValues.keySet());
    }

    public static class ClaimResult {
        private final BigDecimal tokensEarned;
        private final int blocksConverted;
        private final Map<Material, Integer> claimedItems;

        public ClaimResult(BigDecimal tokensEarned, int blocksConverted, Map<Material, Integer> claimedItems) {
            this.tokensEarned = tokensEarned;
            this.blocksConverted = blocksConverted;
            this.claimedItems = claimedItems;
        }

        public BigDecimal getTokensEarned() {
            return tokensEarned;
        }

        public int getBlocksConverted() {
            return blocksConverted;
        }

        public Map<Material, Integer> getClaimedItems() {
            return claimedItems;
        }
    }

    public void setTokenValueUpgrade(TokenValueUpgrade tvu) {
        this.tokenValueUpgrade = tvu;
    }
}