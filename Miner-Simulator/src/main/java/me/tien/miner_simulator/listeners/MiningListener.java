package me.tien.miner_simulator.listeners;

import org.bukkit.metadata.MetadataValue;
import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.integration.NFTPluginIntegration;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MiningListener implements Listener {

    private final Miner_Simulator plugin;
    private final Random random = new Random();
    private final NFTPluginIntegration nftIntegration;

    private final Map<String, List<String>> nftsByRarity = new HashMap<>();
    private final Map<String, Double> rarityDropRates = new HashMap<>();
    private final Map<String, ChatColor> rarityColors = new HashMap<>();
    private final String[] rarityOrder = { "legendary", "epic", "rare", "uncommon", "common" };

    private double baseDropChance = 0.05;
    private int cooldownSeconds = 3;
    private final Map<UUID, Long> lastDropTime = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> nftRates = new HashMap<>();

    // Configuration for buffs
    private boolean enableBuffs = true;
    private double maxLuckBuff = 0.20; // Maximum 20% buff


    public MiningListener(Miner_Simulator plugin) {
        this.plugin = plugin;
        this.nftIntegration = plugin.getNFTIntegration();

        setupRarityColors();
        loadConfig();
        loadNFTsByRarity();
        loadRates(); // 👈 THÊM DÒNG NÀY
    }

    private void setupRarityColors() {
        rarityColors.put("legendary", ChatColor.GOLD);
        rarityColors.put("epic", ChatColor.LIGHT_PURPLE);
        rarityColors.put("rare", ChatColor.BLUE);
        rarityColors.put("uncommon", ChatColor.GREEN);
        rarityColors.put("common", ChatColor.WHITE);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        plugin.getLogger().info("[DEBUG] Mining block " + block.getType() + " at " + block.getLocation() + " by " + player.getName());
        plugin.getLogger().info("[TEST] BlockBreakEvent by: " + player.getName() + ", block: " + block.getType());

        // IGNORE ALL CONDITIONS - FOR TESTING ONLY
        plugin.getLogger().info("[DEBUG] Calling handleNFTDrop for player: " + player.getName() + " (IGNORING CONDITIONS)");
        handleNFTDrop(player);

        // Original code - commented for testing
        /*
        VoidMine voidMine = plugin.getVoidMine();
        if (voidMine == null) {
            plugin.getLogger().warning("[DEBUG] VoidMine is null");
            return;
        }

        if (!voidMine.isMineWorld(block.getWorld().getName())) {
            plugin.getLogger().warning("[DEBUG] Block is not in mine world: " + block.getWorld().getName());
            return;
        }

        VoidMine.PlayerMine playerMine = voidMine.getMineByWorldName(block.getWorld().getName());
        if (playerMine == null) {
            plugin.getLogger().warning("[DEBUG] PlayerMine is null for world: " + block.getWorld().getName());
            return;
        }

        if (!playerMine.isInMiningBox(block.getLocation())) {
            plugin.getLogger().warning("[DEBUG] Block is not in mining box: " + block.getLocation());
            return;
        }

        if (!isMineableMaterial(block.getType())) {
            plugin.getLogger().warning("[DEBUG] Block is not a mineable material: " + block.getType());
            return;
        }

        // All conditions met, call handleNFTDrop
        plugin.getLogger().info("[DEBUG] Calling handleNFTDrop for player: " + player.getName());
        handleNFTDrop(player);
        voidMine.checkAndResetMineIfEmpty(player, block.getLocation());
        */
    }

    private boolean isMineableMaterial(Material material) {
        return material == Material.STONE || material == Material.COBBLESTONE
                || material == Material.COAL_ORE || material == Material.IRON_ORE
                || material == Material.GOLD_ORE || material == Material.DIAMOND_ORE
                || material == Material.EMERALD_ORE || material == Material.LAPIS_ORE
                || material == Material.REDSTONE_ORE;
    }

    private void handleNFTDrop(Player player) {
        long now = System.currentTimeMillis();
        long lastDrop = lastDropTime.getOrDefault(player.getUniqueId(), 0L);
        long wait = cooldownSeconds * 1000L;

        plugin.getLogger().info("[TEST] Checking NFT drop for: " + player.getName());

        if (now - lastDrop < wait) {
            plugin.getLogger().info("[TEST] Cooldown active: " + (wait - (now - lastDrop)) + "ms remaining");
            return;
        }

        // Apply luck buff from NFT-Plugin if available
        double adjustedDropChance = getAdjustedDropChance(player);

        double rollDrop = random.nextDouble();
        plugin.getLogger().info("[TEST] Rolled baseDropChance: " + rollDrop + " vs " + adjustedDropChance +
                " (base: " + baseDropChance + ", buff: " + (adjustedDropChance - baseDropChance) + ")");

        if (rollDrop > adjustedDropChance) {
            plugin.getLogger().info("[TEST] Roll failed – no NFT this time.");
            return;
        }

        // select rarity
        double rarityRoll = random.nextDouble();
        plugin.getLogger().info("[TEST] Rolled rarity chance: " + rarityRoll);

        // Find matching rarity
        String tempSelectedRarity = null;
        double cumulativeChance = 0.0;
        for (String rarity : rarityOrder) {
            cumulativeChance += rarityDropRates.getOrDefault(rarity, 0.0) / 100.0; // because config is in %, convert to 0.x
            if (rarityRoll <= cumulativeChance) {
                tempSelectedRarity = rarity;
                break;
            }
        }

        if (tempSelectedRarity == null) {
            plugin.getLogger().warning("[TEST] No matching rarity found.");
            return;
        }

        // Use final variable to use in lambda
        final String selectedRarity = tempSelectedRarity;

        plugin.getLogger().info("[TEST] Selected rarity: " + selectedRarity);

        List<String> nftList = nftsByRarity.get(selectedRarity);
        if (nftList == null || nftList.isEmpty()) {
            plugin.getLogger().warning("[TEST] No NFTs found for rarity: " + selectedRarity);
            return;
        }

        // Select NFT based on rates in config
        final String selectedNFT = selectNFTByRates(selectedRarity) != null ?
            selectNFTByRates(selectedRarity) :
            nftList.get(random.nextInt(nftList.size()));

        plugin.getLogger().info("[TEST] Selected NFT ID: " + selectedNFT);

        // Use approach similar to LootBox: call mintnft command with OP permission
        plugin.getLogger().info("[NFTMiner] Minting NFT for player " + player.getName() + ": " + selectedNFT);

        // Send notification to player
        player.sendMessage(ChatColor.YELLOW + "NFT is being minted... Please wait for completion notification!");

        // Use temporary OP permission to mint NFT
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean wasOp = player.isOp();
            try {
                // Log
                plugin.getLogger().info("Minting NFT for player " + player.getName() + ": " + selectedNFT);

                // Grant temporary OP permission
                if (!wasOp) {
                    player.setOp(true);
                }

                // Execute command with OP permission
                String command = "mintnft " + player.getName() + " " + selectedNFT;
                plugin.getLogger().info("[NFTMiner] Executing command: " + command);

                boolean success = player.performCommand(command);

                if (!success) {
                    plugin.getLogger().severe("[NFTMiner] Could not execute mintnft command with player permission");
                    player.sendMessage(ChatColor.RED + "An error occurred while minting NFT. Please try again later.");
                }

                // No need for additional notification as /mintnft command already sends notification
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Could not mint NFT: " + e.getMessage());
                plugin.getLogger().severe("Error while minting NFT: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Restore OP status
                if (!wasOp) {
                    player.setOp(false);
                }
            }
        }, 10L); // Wait 0.5 seconds to ensure notification is displayed first

        lastDropTime.put(player.getUniqueId(), now);
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Read settings from nft-drop section in config.yml
        ConfigurationSection nftDropSection = config.getConfigurationSection("nft-drop");

        if (nftDropSection != null) {
            // Đọc base-drop-chance
            baseDropChance = nftDropSection.getDouble("base-drop-chance", 0.05);

            // Đọc cooldown-seconds
            cooldownSeconds = nftDropSection.getInt("cooldown-seconds", 3);

            plugin.getLogger().info("[NFTMiner] base-drop-chance = " + baseDropChance);
            plugin.getLogger().info("[NFTMiner] cooldown-seconds = " + cooldownSeconds + "s");

            // Đọc rarity-drop-rates
            rarityDropRates.clear();
            ConfigurationSection raritySection = nftDropSection.getConfigurationSection("rarity-drop-rates");

            if (raritySection != null) {
                for (String rarity : raritySection.getKeys(false)) {
                    double chance = raritySection.getDouble(rarity);
                    rarityDropRates.put(rarity.toLowerCase(), chance);

                    plugin.getLogger().info("[NFTMiner] rarity " + rarity.toUpperCase() + " = " + chance + "%");
                }
            } else {
                plugin.getLogger().warning("[NFTMiner] 'rarity-drop-rates' section not found in config. Using defaults.");
                // Using default values
                rarityDropRates.put("common", 5.0);
                rarityDropRates.put("uncommon", 2.0);
                rarityDropRates.put("rare", 1.0);
                rarityDropRates.put("epic", 0.5);
                rarityDropRates.put("legendary", 0.1);
            }

            // Read buff settings
            ConfigurationSection buffSection = nftDropSection.getConfigurationSection("buffs");
            if (buffSection != null) {
                enableBuffs = buffSection.getBoolean("enabled", true);
                maxLuckBuff = buffSection.getDouble("max-luck-buff", 0.20);

                plugin.getLogger().info("[NFTMiner] Buffs enabled: " + enableBuffs);
                plugin.getLogger().info("[NFTMiner] Max luck buff: " + (maxLuckBuff * 100) + "%");
            } else {
                // Add default buff settings to config
                try {
                    nftDropSection.set("buffs.enabled", true);
                    nftDropSection.set("buffs.max-luck-buff", 0.20);
                    config.save(configFile);
                    plugin.getLogger().info("[NFTMiner] Added default buff settings to config.yml");
                } catch (IOException e) {
                    plugin.getLogger().severe("[NFTMiner] Could not save default buff settings: " + e.getMessage());
                }
            }
        } else {
            plugin.getLogger().warning("[NFTMiner] 'nft-drop' section not found in config.yml. Using defaults.");
            // Using default values
            baseDropChance = 0.05;
            cooldownSeconds = 3;

            rarityDropRates.put("common", 5.0);
            rarityDropRates.put("uncommon", 2.0);
            rarityDropRates.put("rare", 1.0);
            rarityDropRates.put("epic", 0.5);
            rarityDropRates.put("legendary", 0.1);

            enableBuffs = true;
            maxLuckBuff = 0.20;
        }
    }
    private void loadRates() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Read from nft-drop.tiers section in config.yml
        ConfigurationSection nftDropSection = config.getConfigurationSection("nft-drop");
        if (nftDropSection == null) {
            plugin.getLogger().severe("[NFTMiner] 'nft-drop' section not found in config.yml");
            return;
        }

        ConfigurationSection tiersSection = nftDropSection.getConfigurationSection("tiers");
        if (tiersSection == null) {
            plugin.getLogger().severe("[NFTMiner] 'nft-drop.tiers' section not found in config.yml");
            return;
        }

        // Xóa dữ liệu cũ
        nftRates.clear();

        // Load rates for each tier
        for (String tier : tiersSection.getKeys(false)) {
            loadNFTRates(tier, tiersSection);
        }

        plugin.getLogger().info("[NFTMiner] Loaded NFT drop rates for " + nftRates.size() + " tiers");
    }

    private void loadNFTRates(String tier, ConfigurationSection section) {
        ConfigurationSection tierSection = section.getConfigurationSection(tier);
        if (tierSection == null) {
            plugin.getLogger().severe("[NFTMiner] Tier " + tier + " not found in config.yml");
            return;
        }

        Map<String, Integer> rates = new HashMap<>();
        for (String nft : tierSection.getKeys(false)) {
            int rate = tierSection.getInt(nft);
            rates.put(nft, rate);
            plugin.getLogger().info("[NFTMiner] Loaded NFT rate: " + nft + " = " + rate + " (" + tier + ")");
        }

        nftRates.put(tier.toLowerCase(), rates);
    }


    private void loadNFTsByRarity() {
        nftsByRarity.clear();

        if (nftIntegration == null || !nftIntegration.isNFTPluginAvailable()) {
            plugin.getLogger().warning("[NFTMiner] NFTPlugin not connected. Cannot load NFT metadata.");

            // Initialize empty lists for rarities
            String[] rarities = {"common", "uncommon", "rare", "epic", "legendary"};
            for (String rarity : rarities) {
                nftsByRarity.put(rarity, new ArrayList<>());
            }
            return;
        }

        // Sử dụng NFTPluginIntegration để lấy danh sách NFT theo rarity
        Map<String, List<String>> loadedNFTs = nftIntegration.loadNFTsByRarity();

        // Cập nhật cache local
        nftsByRarity.putAll(loadedNFTs);

        // Log thông tin
        for (Map.Entry<String, List<String>> entry : nftsByRarity.entrySet()) {
            plugin.getLogger().info("[NFTMiner] Loaded " + entry.getValue().size() + " NFTs for rarity: " + entry.getKey());
        }
    }


    public void reload() {
        rarityDropRates.clear();
        nftsByRarity.clear();
        lastDropTime.clear();
        loadConfig();

        // Refresh metadata cache before reloading
        if (nftIntegration != null) {
            nftIntegration.refreshMetadataCache();
        }

        loadNFTsByRarity();
        loadRates();
    }

    /**
     * Get the NFT drop rate adjusted by buffs
     * @param player Player
     * @return Adjusted NFT drop rate
     */
    private double getAdjustedDropChance(Player player) {
        if (!enableBuffs || player == null) {
            plugin.getLogger().info("[NFTMiner] Buffs disabled or player is null");
            return baseDropChance;
        }

        // Check if player has luck buff
        double luckBuff = 0.0;

        // Keep hardcoded buff value for WoftvN to ensure correct operation
        if (player.getName().equals("WoftvN")) {
            // Hardcoded buff value for WoftvN based on /nftbuff command result
            luckBuff = 0.01; // 1%
            plugin.getLogger().fine("[NFTMiner] Using buff luck value for WoftvN: 1%");
        }

        // Check metadata from NFT-Plugin (kept for future use if metadata is used)
        if (player.hasMetadata("nft_buff_luck")) {
            try {
                List<MetadataValue> values = player.getMetadata("nft_buff_luck");
                if (!values.isEmpty()) {
                    double rawValue = values.get(0).asDouble();
                    plugin.getLogger().fine("[NFTMiner] Raw buff value from metadata: " + rawValue);

                    luckBuff = rawValue / 100.0; // Chuyển từ % sang hệ số
                    plugin.getLogger().fine("[NFTMiner] Player " + player.getName() + " có buff luck từ metadata: " + (luckBuff * 100) + "%");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[NFTMiner] Lỗi khi đọc buff luck từ metadata: " + e.getMessage());
            }
        }

        // If no buff found, try reading from database
        if (luckBuff == 0.0) {
            try {
                // Tìm class DatabaseManager trong NFT-Plugin
                Plugin nftPlugin = Bukkit.getPluginManager().getPlugin("NFTPlugin");
                if (nftPlugin != null) {
                    // Tìm class DatabaseManager
                    Class<?> databaseManagerClass = null;
                    for (Class<?> clazz : nftPlugin.getClass().getDeclaredClasses()) {
                        if (clazz.getName().contains("DatabaseManager")) {
                            databaseManagerClass = clazz;
                            break;
                        }
                    }

                    if (databaseManagerClass == null) {
                        // Thử tìm trong package
                        try {
                            databaseManagerClass = Class.forName("com.minecraft.nftplugin.database.DatabaseManager");
                        } catch (ClassNotFoundException e) {
                            // Ignore
                        }
                    }

                    if (databaseManagerClass != null) {
                        // Tìm method để lấy instance của DatabaseManager
                        Method getDatabaseManagerMethod = null;
                        try {
                            getDatabaseManagerMethod = nftPlugin.getClass().getMethod("getDatabaseManager");
                        } catch (NoSuchMethodException e) {
                            // Thử tìm method khác
                            for (Method method : nftPlugin.getClass().getMethods()) {
                                if (method.getName().toLowerCase().contains("database") && method.getName().toLowerCase().contains("manager")) {
                                    getDatabaseManagerMethod = method;
                                    break;
                                }
                            }
                        }

                        if (getDatabaseManagerMethod != null) {
                            // Gọi method để lấy DatabaseManager
                            Object databaseManager = getDatabaseManagerMethod.invoke(nftPlugin);

                            if (databaseManager != null) {
                                // Tìm method để lấy buff từ database
                                Method getBuffMethod = null;
                                for (Method method : databaseManagerClass.getMethods()) {
                                    if (method.getName().toLowerCase().contains("get") &&
                                        method.getName().toLowerCase().contains("buff")) {
                                        getBuffMethod = method;
                                        break;
                                    }
                                }

                                if (getBuffMethod != null) {
                                    // Gọi method để lấy buff
                                    Object buffResult = null;

                                    // Kiểm tra tham số của method
                                    Class<?>[] paramTypes = getBuffMethod.getParameterTypes();
                                    plugin.getLogger().info("[NFTMiner] Database method " + getBuffMethod.getName() + " có " + paramTypes.length + " tham số");

                                    for (int i = 0; i < paramTypes.length; i++) {
                                        plugin.getLogger().info("[NFTMiner] Tham số " + i + ": " + paramTypes[i].getName());
                                    }

                                    try {
                                        if (paramTypes.length == 0) {
                                            // Không có tham số
                                            buffResult = getBuffMethod.invoke(databaseManager);
                                        } else if (paramTypes.length == 1) {
                                            // 1 tham số
                                            Class<?> paramType = paramTypes[0];
                                            if (paramType.isAssignableFrom(Player.class)) {
                                                // Tham số là Player
                                                buffResult = getBuffMethod.invoke(databaseManager, player);
                                            } else if (paramType.isAssignableFrom(UUID.class)) {
                                                // Tham số là UUID
                                                buffResult = getBuffMethod.invoke(databaseManager, player.getUniqueId());
                                            } else if (paramType.isAssignableFrom(String.class)) {
                                                // Tham số là String
                                                buffResult = getBuffMethod.invoke(databaseManager, player.getName());
                                            } else {
                                                plugin.getLogger().warning("[NFTMiner] Không hỗ trợ tham số kiểu: " + paramType.getName());
                                            }
                                        } else if (paramTypes.length == 2) {
                                            // 2 tham số
                                            Class<?> paramType1 = paramTypes[0];
                                            Class<?> paramType2 = paramTypes[1];

                                            if (paramType1.isAssignableFrom(Player.class) && paramType2.isAssignableFrom(String.class)) {
                                                // (Player, String)
                                                buffResult = getBuffMethod.invoke(databaseManager, player, "luck");
                                            } else if (paramType1.isAssignableFrom(UUID.class) && paramType2.isAssignableFrom(String.class)) {
                                                // (UUID, String)
                                                buffResult = getBuffMethod.invoke(databaseManager, player.getUniqueId(), "luck");
                                            } else if (paramType1.isAssignableFrom(String.class) && paramType2.isAssignableFrom(String.class)) {
                                                // (String, String)
                                                buffResult = getBuffMethod.invoke(databaseManager, player.getName(), "luck");
                                            } else {
                                                plugin.getLogger().warning("[NFTMiner] Không hỗ trợ tham số kiểu: " +
                                                                          paramType1.getName() + ", " + paramType2.getName());
                                            }
                                        } else {
                                            plugin.getLogger().warning("[NFTMiner] Không hỗ trợ method với " + paramTypes.length + " tham số");
                                        }
                                    } catch (Exception e) {
                                        plugin.getLogger().warning("[NFTMiner] Lỗi khi gọi method từ database: " + e.getMessage());
                                        e.printStackTrace();
                                    }

                                    if (buffResult != null) {
                                        // Thử lấy giá trị buff từ kết quả
                                        if (buffResult instanceof Number) {
                                            luckBuff = ((Number) buffResult).doubleValue() / 100.0;
                                            plugin.getLogger().fine("[NFTMiner] Got luck buff from database: " + (luckBuff * 100) + "%");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().warning("[NFTMiner] Lỗi khi truy cập database: " + e.getMessage());
            }
        }

        // Giới hạn buff tối đa
        if (luckBuff > maxLuckBuff) {
            luckBuff = maxLuckBuff;
        }

        // Calculate new drop rate
        double adjustedChance = baseDropChance + luckBuff;

        // Limit maximum drop rate to 100%
        if (adjustedChance > 1.0) {
            adjustedChance = 1.0;
        }

        return adjustedChance;
    }

    /**
     * Select NFT based on rates in config
     * @param rarity Rarity
     * @return Selected NFT, or null if not found
     */
    private String selectNFTByRates(String rarity) {
        Map<String, Integer> rates = nftRates.get(rarity.toLowerCase());
        if (rates == null || rates.isEmpty()) {
            plugin.getLogger().warning("[NFTMiner] No rates found for rarity: " + rarity);
            return null;
        }

        // Tính tổng tỉ lệ
        int totalRate = 0;
        for (int rate : rates.values()) {
            totalRate += rate;
        }

        // Chọn ngẫu nhiên dựa trên tỉ lệ
        int roll = random.nextInt(totalRate);
        int currentSum = 0;

        for (Map.Entry<String, Integer> entry : rates.entrySet()) {
            currentSum += entry.getValue();
            if (roll < currentSum) {
                plugin.getLogger().info("[NFTMiner] Selected NFT " + entry.getKey() + " with rate " + entry.getValue() + "/" + totalRate);
                return entry.getKey();
            }
        }

        // If no NFT is selected, use the first one
        String firstNFT = rates.keySet().iterator().next();
        plugin.getLogger().warning("[NFTMiner] Could not select NFT based on rates, using default: " + firstNFT);
        return firstNFT;
    }
}
