package me.tien.nftminer.listeners;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.service.MintNFTService;
import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.integration.NFTPluginIntegration;
import me.tien.nftminer.world.VoidMine;
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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class MiningListener implements Listener {

    private final NFTMiner plugin;
    private final Random random = new Random();
    private final NFTPluginIntegration nftIntegration;
    private final MintNFTService mintNFTService;

    private final Map<String, List<String>> nftsByRarity = new HashMap<>();
    private final Map<String, Double> rarityDropRates = new HashMap<>();
    private final Map<String, ChatColor> rarityColors = new HashMap<>();
    private final String[] rarityOrder = { "legendary", "epic", "rare", "uncommon", "common" };

    private double baseDropChance = 0.05;
    private int cooldownSeconds = 3;
    private final Map<UUID, Long> lastDropTime = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> nftRates = new HashMap<>();


    public MiningListener(NFTMiner plugin) {
        this.plugin = plugin;
        this.nftIntegration = plugin.getNFTIntegration();
        this.mintNFTService = nftIntegration.getMintNFTService(); // Lấy từ NFTPlugin qua

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
        plugin.getLogger().info("[DEBUG] Đào block " + block.getType() + " tại " + block.getLocation() + " bởi " + player.getName());
        plugin.getLogger().info("[TEST] BlockBreakEvent by: " + player.getName() + ", block: " + block.getType());
        VoidMine voidMine = plugin.getVoidMine();
        if (voidMine != null && voidMine.isMineWorld(block.getWorld().getName())) {
            VoidMine.PlayerMine playerMine = voidMine.getMineByWorldName(block.getWorld().getName());
            if (playerMine != null && playerMine.isInMiningBox(block.getLocation())) {
                if (isMineableMaterial(block.getType())) {
                    handleNFTDrop(player);
                }
            }
        }
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

        double rollDrop = random.nextDouble();
        plugin.getLogger().info("[TEST] Rolled baseDropChance: " + rollDrop + " vs " + baseDropChance);

        if (rollDrop > baseDropChance) {
            plugin.getLogger().info("[TEST] Roll failed – no NFT this time.");
            return;
        }

        // chọn rarity
        double rarityRoll = random.nextDouble();
        plugin.getLogger().info("[TEST] Rolled rarity chance: " + rarityRoll);

        String selectedRarity = null;
        double cumulativeChance = 0.0;
        for (String rarity : rarityOrder) {
            cumulativeChance += rarityDropRates.getOrDefault(rarity, 0.0) / 100.0; // vì config là %, chuyển về 0.x
            if (rarityRoll <= cumulativeChance) {
                selectedRarity = rarity;
                break;
            }
        }

        if (selectedRarity == null) {
            plugin.getLogger().warning("[TEST] Không tìm thấy rarity phù hợp.");
            return;
        }

        plugin.getLogger().info("[TEST] Selected rarity: " + selectedRarity);

        List<String> nftList = nftsByRarity.get(selectedRarity);
        if (nftList == null || nftList.isEmpty()) {
            plugin.getLogger().warning("[TEST] Không có NFT nào cho rarity: " + selectedRarity);
            return;
        }

        String selectedNFT = nftList.get(random.nextInt(nftList.size()));
        plugin.getLogger().info("[TEST] Selected NFT ID: " + selectedNFT);

        // Gọi mint
        if (mintNFTService != null) {
            plugin.getLogger().info("[TEST] Gọi mintNFTToPlayer cho: " + player.getName());
            mintNFTService.mintNFTToPlayer(player, selectedNFT);
        } else {
            plugin.getLogger().severe("[TEST] mintNFTService null! Không thể gọi.");
        }

        lastDropTime.put(player.getUniqueId(), now);
        player.sendMessage(ChatColor.GREEN + "Bạn đã đào được " +
                rarityColors.getOrDefault(selectedRarity, ChatColor.WHITE) + selectedNFT +
                ChatColor.GREEN + " [" + selectedRarity.toUpperCase() + "]!");
    }

    private void loadConfig() {
        File configFile = new File(plugin.getDataFolder(), "nft_config.yml");

        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
                FileConfiguration defaultConfig = YamlConfiguration.loadConfiguration(configFile);
                defaultConfig.set("drop-settings.base-drop-chance", 0.05);
                defaultConfig.set("drop-settings.cooldown-seconds", 3);
                ConfigurationSection raritySection = defaultConfig.createSection("rarity-drop-rates");
                raritySection.set("common", 5.0);
                raritySection.set("uncommon", 2.0);
                raritySection.set("rare", 1.0);
                raritySection.set("epic", 0.5);
                raritySection.set("legendary", 0.1);
                defaultConfig.save(configFile);

                plugin.getLogger().info("[NFTMiner] Đã tạo file nft_config.yml mặc định.");
            } catch (IOException e) {
                plugin.getLogger().severe("[NFTMiner] Không thể tạo file cấu hình nft_config.yml");
                e.printStackTrace();
            }
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        baseDropChance = config.getDouble("drop-settings.base-drop-chance", 0.05);
        cooldownSeconds = config.getInt("drop-settings.cooldown-seconds", 3);

        plugin.getLogger().info("[NFTMiner] base-drop-chance = " + baseDropChance);
        plugin.getLogger().info("[NFTMiner] cooldown-seconds = " + cooldownSeconds + "s");

        rarityDropRates.clear();
        ConfigurationSection raritySection = config.getConfigurationSection("rarity-drop-rates");

        if (raritySection != null) {
            for (String rarity : raritySection.getKeys(false)) {
                double chance = raritySection.getDouble(rarity);
                rarityDropRates.put(rarity.toLowerCase(), chance);

                plugin.getLogger().info("[NFTMiner] rarity " + rarity.toUpperCase() + " = " + chance + "%");
            }
        } else {
            plugin.getLogger().warning("[NFTMiner] Không tìm thấy phần 'rarity-drop-rates' trong config. Dùng mặc định.");
        }
    }
    private void loadRates() {
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        ConfigurationSection tiersSection = config.getConfigurationSection("tiers");
        if (tiersSection == null) {
            plugin.getLogger().severe("[NFTMiner] Không tìm thấy phần 'tiers' trong config.yml");
            return;
        }

        for (String tier : tiersSection.getKeys(false)) {
            loadNFTRates(tier, tiersSection);
        }
    }
    private void loadNFTRates(String tier, ConfigurationSection section) {
        ConfigurationSection tierSection = section.getConfigurationSection(tier);
        if (tierSection == null) {
            plugin.getLogger().severe("[NFTMiner] Không tìm thấy tier " + tier + " trong config.yml");
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
        String[] rarities = {"common", "uncommon", "rare", "epic", "legendary"};
        for (String rarity : rarities) {
            nftsByRarity.put(rarity, new ArrayList<>());
        }

        try {
            if (nftIntegration == null) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin chưa kết nối. Không thể load NFT metadata.");
                return;
            }

            File metadataFolder = new File(nftIntegration.getNFTPluginInstance().getDataFolder(), "metadata");
            if (!metadataFolder.exists() || !metadataFolder.isDirectory()) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy thư mục metadata của NFTPlugin.");
                return;
            }

            File[] files = metadataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files == null || files.length == 0) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy file metadata JSON.");
                return;
            }

            Gson gson = new Gson();

            for (File file : files) {
                try (FileReader reader = new FileReader(file)) {
                    JsonObject json = gson.fromJson(reader, JsonObject.class);

                    String nftId = file.getName().replace(".json", "");
                    String rarity = "common"; // mặc định

                    if (json.has("attributes")) {
                        JsonArray attributes = json.getAsJsonArray("attributes");
                        for (JsonElement element : attributes) {
                            JsonObject attribute = element.getAsJsonObject();
                            if ("Rarity".equalsIgnoreCase(attribute.get("trait_type").getAsString())) {
                                rarity = attribute.get("value").getAsString().toLowerCase();
                                break;
                            }
                        }
                    }

                    if (!nftsByRarity.containsKey(rarity)) {
                        plugin.getLogger().warning("[NFTMiner] Rarity không xác định: " + rarity + ". Gán vào common.");
                        rarity = "common";
                    }

                    nftsByRarity.get(rarity).add(nftId);
                    plugin.getLogger().info("[NFTMiner] Loaded NFT: " + nftId + " (rarity: " + rarity + ")");
                } catch (Exception e) {
                    plugin.getLogger().warning("[NFTMiner] Lỗi khi đọc metadata file: " + file.getName() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Lỗi khi load NFT metadata:", e);
        }
    }


    public void reload() {
        rarityDropRates.clear();
        nftsByRarity.clear();
        lastDropTime.clear();
        loadConfig();
        loadNFTsByRarity();
    }
}
