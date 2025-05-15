package me.tien.miner_simulator.token;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.lang.reflect.Method;
import org.bukkit.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.upgrade.TokenValueUpgrade;

public class TokenManager {
    private final Miner_Simulator plugin;
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

    public TokenManager(Miner_Simulator plugin) {
        this.plugin = plugin;
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

    public BigDecimal getTokens(Player player) {
        Method getBalacneMethod;
        Object minePathInstance;
        try {
            // 1) Grab the already-loaded plugin by name (must match plugin.yml 'name:')
            Plugin raw = Bukkit.getPluginManager().getPlugin("MinePath");
            if (raw == null) {
                player.sendMessage(ChatColor.RED + "Token plugin not found on this server.");
                return null;
            }
            // 2) Reflect on its actual class
            Class<?> mpClass = raw.getClass();
            // 3) Locate the public API method
            getBalacneMethod = mpClass.getMethod("getBalance", Player.class);
            minePathInstance  = raw;
        } catch (NoSuchMethodException nsme) {
            plugin.getLogger().severe("getBalance(Player) not found in MinePathCoinPlugin");
            player.sendMessage(ChatColor.RED + "Token API has changed—contact an admin.");
            return null;
        } catch (Exception e) {
            plugin.getLogger().severe("Error hooking into token plugin: " + e);
            player.sendMessage(ChatColor.RED + "Internal error initializing token plugin.");
            return null;
        }
        try {
            String balance = (String) getBalacneMethod.invoke(minePathInstance, player);
            return new BigDecimal(balance);

        } catch (Exception e) {
            // unwrap underlying cause if present
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            player.sendMessage(ChatColor.RED + "Fetch balance failed: " + cause.getMessage());
            plugin.getLogger().severe("Error during fetching balance: " + cause);
        }
        return null;
    }
    public boolean addTokens(Player player, BigDecimal amount) {
        Method claimTokensMethod;
        Object minePathInstance;
        try {
            // 1) Grab the already-loaded plugin by name (must match plugin.yml 'name:')
            Plugin raw = Bukkit.getPluginManager().getPlugin("MinePath");
            if (raw == null) {
                player.sendMessage(ChatColor.RED + "Token plugin not found on this server.");
                return false;
            }
            // 2) Reflect on its actual class
            Class<?> mpClass = raw.getClass();
            // 3) Locate the public API method
            claimTokensMethod = mpClass.getMethod("claimTokens", Player.class, String.class);
            minePathInstance  = raw;
        } catch (NoSuchMethodException nsme) {
            plugin.getLogger().severe("claimTokens(Player,String) not found in MinePathCoinPlugin");
            player.sendMessage(ChatColor.RED + "Token API has changed—contact an admin.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Error hooking into token plugin: " + e);
            player.sendMessage(ChatColor.RED + "Internal error initializing token plugin.");
            return false;
        }

        // 2) Invoke burnTokens to deduct the cost
        try {
            // turn cost into a raw-unit string (assuming burnTokens expects human-readable)
            String costStr = String.valueOf((long)(amount.doubleValue())); 
            boolean wasOp = player.isOp();
            try {
                if (!wasOp) player.setOp(true);
                    String txid = (String) claimTokensMethod.invoke(minePathInstance, player, costStr);
                    return txid != null && !txid.isEmpty();
            } finally {
                if (!wasOp) player.setOp(false);
            }

        } catch (Exception e) {
            // unwrap underlying cause if present
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            player.sendMessage(ChatColor.RED + "Purchase failed: " + cause.getMessage());
            plugin.getLogger().severe("Error during external burn: " + cause);
            return false;
        }
    }
    public boolean removeTokens(Player player, BigDecimal amount) {
        Method burnTokensMethod;
        Object minePathInstance;
        try {
            // 1) Grab the already-loaded plugin by name (must match plugin.yml 'name:')
            Plugin raw = Bukkit.getPluginManager().getPlugin("MinePath");
            if (raw == null) {
                player.sendMessage(ChatColor.RED + "Token plugin not found on this server.");
                return false;
            }
            // 2) Reflect on its actual class
            Class<?> mpClass = raw.getClass();
            // 3) Locate the public API method
            burnTokensMethod = mpClass.getMethod("burnTokens", Player.class, String.class);
            minePathInstance  = raw;
        } catch (NoSuchMethodException nsme) {
            plugin.getLogger().severe("burnTokens(Player,String) not found in MinePathCoinPlugin");
            player.sendMessage(ChatColor.RED + "Token API has changed—contact an admin.");
            return false;
        } catch (Exception e) {
            plugin.getLogger().severe("Error hooking into token plugin: " + e);
            player.sendMessage(ChatColor.RED + "Internal error initializing token plugin.");
            return false;
        }

        // 2) Invoke burnTokens to deduct the cost
        try {
            // turn cost into a raw-unit string (assuming burnTokens expects human-readable)
            String costStr = String.valueOf((long)(amount.doubleValue())); 
            boolean wasOp = player.isOp();
            try {
                if (!wasOp) player.setOp(true);
                    String txid = (String) burnTokensMethod.invoke(minePathInstance, player, costStr);
                    player.sendMessage(ChatColor.GREEN + "Purchase successful! TXID: " + txid);
            } finally {
                if (!wasOp) player.setOp(false);
            }

        } catch (Exception e) {
            // unwrap underlying cause if present
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            player.sendMessage(ChatColor.RED + "Purchase failed: " + cause.getMessage());
            plugin.getLogger().severe("Error during external burn: " + cause);
            return false;
        }

        return true;
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
            boolean success = addTokens(player, finalTokens);
            if (success) {
                return new ClaimResult(finalTokens, totalBlocks, claimedItems);
            } else {
                // Nếu giao dịch thất bại, trả lại các vật phẩm cho người chơi
                for (Map.Entry<Material, Integer> entry : claimedItems.entrySet()) {
                    player.getInventory().addItem(new ItemStack(entry.getKey(), entry.getValue()));
                }
                return new ClaimResult(BigDecimal.ZERO, 0, new HashMap<>());
            }
        }

        return new ClaimResult(BigDecimal.ZERO, 0, claimedItems);
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
    /**
     * Kiểm tra xem người chơi có đủ lượng token cho một giao dịch hay không
     *
     * @param player Người chơi cần kiểm tra
     * @param amount Số lượng token cần kiểm tra
     * @return true nếu người chơi có đủ token, false nếu không đủ
     */
    public boolean hasTokens(Player player, int amount) {
        if (player == null) return false;

        BigDecimal tokenAmount = BigDecimal.valueOf(amount);
        return hasEnoughTokens(player, tokenAmount);
    }
}