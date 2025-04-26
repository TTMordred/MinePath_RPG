package me.tien.nftminer.integration;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.service.MintNFTService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;

public class NFTPluginIntegration {

    private final JavaPlugin plugin;
    private Plugin nftPluginInstance;
    private MintNFTService mintNFTService;
    private boolean nftPluginAvailable = false;

    public NFTPluginIntegration(JavaPlugin plugin) {
        this.plugin = plugin;
        initialize();
    }

    private void initialize() {
        try {
            nftPluginInstance = Bukkit.getPluginManager().getPlugin("NFTPlugin");
            if (nftPluginInstance == null || !nftPluginInstance.isEnabled()) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin không khả dụng hoặc chưa bật.");
                return;
            }

            connectMintNFTService();
            nftPluginAvailable = (mintNFTService != null);

            if (nftPluginAvailable) {
                plugin.getLogger().info("[NFTMiner] Đã kết nối thành công với NFTPlugin!");
            } else {
                plugin.getLogger().warning("[NFTMiner] Không thể kết nối MintNFTService từ NFTPlugin.");
            }

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Lỗi khi kết nối NFTPlugin:", e);
        }
    }

    private void connectMintNFTService() {
        try {
            Method getMintNFTServiceMethod = nftPluginInstance.getClass().getMethod("getMintNFTService");
            Object service = getMintNFTServiceMethod.invoke(nftPluginInstance);

            if (service instanceof MintNFTService) {
                mintNFTService = (MintNFTService) service;
                plugin.getLogger().info("[NFTMiner] MintNFTService đã được lấy thành công từ NFTPlugin.");
            }
        } catch (Exception e) {
            plugin.getLogger().severe("[NFTMiner] Không thể lấy MintNFTService: " + e.getMessage());
        }
    }

    public boolean isNFTPluginAvailable() {
        return nftPluginAvailable;
    }

    public MintNFTService getMintNFTService() {
        return mintNFTService;
    }
    public Plugin getNFTPluginInstance() {
        return nftPluginInstance;
    }

    /**
     * Load tất cả NFT phân loại theo rarity từ thư mục metadata của NFTPlugin
     */
    public Map<String, List<String>> loadNFTsByRarity() {
        Map<String, List<String>> nftsByRarity = new HashMap<>();

        String[] rarities = {"common", "uncommon", "rare", "epic", "legendary"};
        for (String rarity : rarities) {
            nftsByRarity.put(rarity, new ArrayList<>());
        }

        try {
            if (nftPluginInstance == null) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin chưa kết nối. Không thể load NFT metadata.");
                return nftsByRarity;
            }

            File metadataFolder = new File(nftPluginInstance.getDataFolder(), "metadata");
            if (!metadataFolder.exists() || !metadataFolder.isDirectory()) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy thư mục metadata trong NFTPlugin.");
                return nftsByRarity;
            }

            File[] files = metadataFolder.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
            if (files == null || files.length == 0) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy file metadata JSON.");
                return nftsByRarity;
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

                    // Nếu rarity không đúng danh sách thì mặc định common
                    if (!nftsByRarity.containsKey(rarity)) {
                        plugin.getLogger().warning("[NFTMiner] Rarity không xác định: " + rarity + ". Gán vào common.");
                        rarity = "common";
                    }

                    nftsByRarity.get(rarity).add(nftId);

                    plugin.getLogger().info("[NFTMiner] Loaded NFT " + nftId + " (" + rarity + ")");
                } catch (Exception e) {
                    plugin.getLogger().warning("[NFTMiner] Lỗi khi đọc file metadata: " + file.getName() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Lỗi khi load NFT metadata:", e);
        }

        return nftsByRarity;
    }
}
