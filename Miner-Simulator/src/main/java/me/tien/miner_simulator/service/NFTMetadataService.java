package me.tien.miner_simulator.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.tien.miner_simulator.Miner_Simulator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * Service for handling NFT metadata
 * This service caches metadata from NFT-Plugin to reduce file system access
 */
public class NFTMetadataService {

    private final Miner_Simulator plugin;
    private final Map<String, Map<String, List<String>>> nftsByRarityCache = new ConcurrentHashMap<>();
    private final Map<String, JsonObject> metadataCache = new ConcurrentHashMap<>();
    private long lastCacheUpdate = 0;
    private static final long CACHE_TTL = 60000; // 1 minute cache TTL

    public NFTMetadataService(Miner_Simulator plugin) {
        this.plugin = plugin;
    }

    /**
     * Get all NFTs grouped by rarity
     * @return Map of rarity to list of NFT keys
     */
    public Map<String, List<String>> getNFTsByRarity() {
        refreshCacheIfNeeded();

        // Return a copy of the cached data for the current server
        String serverKey = getServerKey();
        if (nftsByRarityCache.containsKey(serverKey)) {
            return new HashMap<>(nftsByRarityCache.get(serverKey));
        }

        // If no cache exists, load it now
        Map<String, List<String>> nftsByRarity = loadNFTsByRarity();
        nftsByRarityCache.put(serverKey, nftsByRarity);
        return new HashMap<>(nftsByRarity);
    }

    /**
     * Get metadata for a specific NFT
     * @param nftKey The NFT key
     * @return The metadata as a JsonObject, or null if not found
     */
    public JsonObject getMetadata(String nftKey) {
        refreshCacheIfNeeded();

        String cacheKey = getServerKey() + ":" + nftKey;
        if (metadataCache.containsKey(cacheKey)) {
            return metadataCache.get(cacheKey);
        }

        // If not in cache, load it
        JsonObject metadata = loadMetadata(nftKey);
        if (metadata != null) {
            metadataCache.put(cacheKey, metadata);
        }
        return metadata;
    }

    /**
     * Force refresh of the cache
     */
    public void refreshCache() {
        nftsByRarityCache.clear();
        metadataCache.clear();
        lastCacheUpdate = System.currentTimeMillis();

        // Pre-load NFTs by rarity
        getNFTsByRarity();
    }

    private void refreshCacheIfNeeded() {
        long now = System.currentTimeMillis();
        if (now - lastCacheUpdate > CACHE_TTL) {
            refreshCache();
        }
    }

    private String getServerKey() {
        // Use server name or IP as a key to support multiple servers
        return Bukkit.getServer().getName();
    }

    private Map<String, List<String>> loadNFTsByRarity() {
        Map<String, List<String>> nftsByRarity = new HashMap<>();
        String[] rarities = {"common", "uncommon", "rare", "epic", "legendary"};
        for (String rarity : rarities) {
            nftsByRarity.put(rarity, new ArrayList<>());
        }

        try {
            Plugin nftPlugin = Bukkit.getPluginManager().getPlugin("NFTPlugin");
            if (nftPlugin == null || !nftPlugin.isEnabled()) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin không khả dụng. Không thể load NFT metadata.");
                return nftsByRarity;
            }

            // Thử tìm thư mục metadata ở nhiều vị trí khác nhau
            File metadataFolder = findMetadataFolder(nftPlugin);
            if (metadataFolder == null) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy thư mục metadata trong NFTPlugin.");
                return nftsByRarity;
            }

            plugin.getLogger().info("[NFTMiner] Tìm thấy thư mục metadata tại: " + metadataFolder.getAbsolutePath());

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
                    String rarity = findRarityFromMetadata(json);

                    if (!nftsByRarity.containsKey(rarity)) {
                        plugin.getLogger().warning("[NFTMiner] Rarity không xác định: " + rarity + ". Gán vào common.");
                        rarity = "common";
                    }

                    nftsByRarity.get(rarity).add(nftId);

                    // Also cache the metadata
                    String cacheKey = getServerKey() + ":" + nftId;
                    metadataCache.put(cacheKey, json);

                    plugin.getLogger().info("[NFTMiner] Loaded NFT: " + nftId + " (rarity: " + rarity + ")");
                } catch (Exception e) {
                    plugin.getLogger().warning("[NFTMiner] Lỗi khi đọc metadata file: " + file.getName() + " - " + e.getMessage());
                }
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Lỗi khi load NFT metadata:", e);
        }

        return nftsByRarity;
    }

    /**
     * Tìm thư mục metadata trong NFTPlugin
     * Thử nhiều vị trí khác nhau
     */
    private File findMetadataFolder(Plugin nftPlugin) {
        // Danh sách các vị trí có thể chứa metadata
        String[] possiblePaths = {
            "metadata",
            "data/metadata",
            "nft/metadata",
            "assets/metadata",
            "resources/metadata"
        };

        for (String path : possiblePaths) {
            File folder = new File(nftPlugin.getDataFolder(), path);
            if (folder.exists() && folder.isDirectory()) {
                return folder;
            }
        }

        // Thử tìm trong thư mục gốc của plugin
        File[] files = nftPlugin.getDataFolder().listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && (
                    file.getName().equalsIgnoreCase("metadata") ||
                    file.getName().equalsIgnoreCase("nft") ||
                    file.getName().equalsIgnoreCase("assets")
                )) {
                    // Kiểm tra xem thư mục này có chứa file JSON không
                    File[] jsonFiles = file.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
                    if (jsonFiles != null && jsonFiles.length > 0) {
                        return file;
                    }

                    // Kiểm tra các thư mục con
                    File[] subDirs = file.listFiles(File::isDirectory);
                    if (subDirs != null) {
                        for (File subDir : subDirs) {
                            if (subDir.getName().equalsIgnoreCase("metadata")) {
                                File[] subJsonFiles = subDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
                                if (subJsonFiles != null && subJsonFiles.length > 0) {
                                    return subDir;
                                }
                            }
                        }
                    }
                }
            }
        }

        return null;
    }

    /**
     * Tìm rarity từ metadata
     * Thử nhiều cấu trúc metadata khác nhau
     */
    private String findRarityFromMetadata(JsonObject json) {
        // Mặc định là common
        String rarity = "common";

        // Cấu trúc 1: attributes array với trait_type và value
        if (json.has("attributes") && json.get("attributes").isJsonArray()) {
            JsonArray attributes = json.getAsJsonArray("attributes");
            for (JsonElement element : attributes) {
                if (element.isJsonObject()) {
                    JsonObject attribute = element.getAsJsonObject();
                    if (attribute.has("trait_type") && attribute.has("value")) {
                        String traitType = attribute.get("trait_type").getAsString();
                        if ("Rarity".equalsIgnoreCase(traitType) || "rarity".equalsIgnoreCase(traitType)) {
                            rarity = attribute.get("value").getAsString().toLowerCase();
                            return rarity;
                        }
                    }
                }
            }
        }

        // Cấu trúc 2: properties.rarity
        if (json.has("properties") && json.get("properties").isJsonObject()) {
            JsonObject properties = json.get("properties").getAsJsonObject();
            if (properties.has("rarity")) {
                rarity = properties.get("rarity").getAsString().toLowerCase();
                return rarity;
            }
        }

        // Cấu trúc 3: rarity trực tiếp
        if (json.has("rarity")) {
            rarity = json.get("rarity").getAsString().toLowerCase();
            return rarity;
        }

        // Cấu trúc 4: metadata.rarity
        if (json.has("metadata") && json.get("metadata").isJsonObject()) {
            JsonObject metadata = json.get("metadata").getAsJsonObject();
            if (metadata.has("rarity")) {
                rarity = metadata.get("rarity").getAsString().toLowerCase();
                return rarity;
            }
        }

        return rarity;
    }

    private JsonObject loadMetadata(String nftKey) {
        try {
            Plugin nftPlugin = Bukkit.getPluginManager().getPlugin("NFTPlugin");
            if (nftPlugin == null || !nftPlugin.isEnabled()) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin không khả dụng. Không thể load NFT metadata.");
                return null;
            }

            // Tìm thư mục metadata
            File metadataFolder = findMetadataFolder(nftPlugin);
            if (metadataFolder == null) {
                plugin.getLogger().warning("[NFTMiner] Không tìm thấy thư mục metadata trong NFTPlugin.");
                return null;
            }

            // Tìm file metadata
            File metadataFile = new File(metadataFolder, nftKey + ".json");
            if (!metadataFile.exists() || !metadataFile.isFile()) {
                // Thử tìm file với tên khác
                File[] possibleFiles = metadataFolder.listFiles((dir, name) ->
                    name.toLowerCase().startsWith(nftKey.toLowerCase()) && name.toLowerCase().endsWith(".json"));

                if (possibleFiles != null && possibleFiles.length > 0) {
                    metadataFile = possibleFiles[0];
                    plugin.getLogger().info("[NFTMiner] Tìm thấy file metadata tương tự: " + metadataFile.getName());
                } else {
                    plugin.getLogger().warning("[NFTMiner] Không tìm thấy file metadata: " + nftKey + ".json");
                    return null;
                }
            }

            Gson gson = new Gson();
            try (FileReader reader = new FileReader(metadataFile)) {
                JsonObject json = gson.fromJson(reader, JsonObject.class);

                // Cache metadata
                String cacheKey = getServerKey() + ":" + nftKey;
                metadataCache.put(cacheKey, json);

                return json;
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Lỗi khi load metadata cho " + nftKey + ":", e);
            return null;
        }
    }
}
