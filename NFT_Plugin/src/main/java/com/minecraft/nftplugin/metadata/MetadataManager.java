package com.minecraft.nftplugin.metadata;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Manager for NFT metadata
 */
public class MetadataManager {

    private final NFTPlugin plugin;
    private final Gson gson;
    private final Map<String, JsonObject> metadataCache;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public MetadataManager(NFTPlugin plugin) {
        this.plugin = plugin;
        this.gson = new Gson();
        this.metadataCache = new HashMap<>();

        // Initialize metadata
        loadAllMetadata();
    }

    /**
     * Load all metadata files
     */
    private void loadAllMetadata() {
        // Load from resources
        loadMetadataFromResources();

        // Load from data folder
        loadMetadataFromDataFolder();

        plugin.getLogger().info("Loaded " + metadataCache.size() + " metadata files");
    }

    /**
     * Load metadata files from resources
     */
    private void loadMetadataFromResources() {
        String[] metadataFiles = {
            "divine_axe.json",
            "diamond_master.json",
            "mountain_climber.json",
            "zombie_hunter.json",
            "master_fisher.json"
        };

        for (String fileName : metadataFiles) {
            try {
                InputStream inputStream = plugin.getResource("metadata/" + fileName);
                if (inputStream != null) {
                    try (Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8)) {
                        JsonObject metadata = gson.fromJson(reader, JsonObject.class);
                        String key = fileName.replace(".json", "");
                        metadataCache.put(key, metadata);
                        plugin.getLogger().info("Loaded metadata from resources: " + fileName);
                    }
                }
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load metadata from resources: " + fileName, e);
            }
        }
    }

    /**
     * Load metadata files from data folder
     */
    private void loadMetadataFromDataFolder() {
        File metadataDir = new File(plugin.getDataFolder(), "metadata");
        if (!metadataDir.exists()) {
            metadataDir.mkdirs();
            return;
        }

        File[] files = metadataDir.listFiles((dir, name) -> name.endsWith(".json"));
        if (files == null) {
            return;
        }

        for (File file : files) {
            try (FileReader reader = new FileReader(file)) {
                JsonObject metadata = gson.fromJson(reader, JsonObject.class);
                String key = file.getName().replace(".json", "");
                metadataCache.put(key, metadata);
                plugin.getLogger().info("Loaded metadata from data folder: " + file.getName());
            } catch (IOException e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load metadata from data folder: " + file.getName(), e);
            }
        }
    }

    /**
     * Get metadata for an achievement
     * @param achievementKey The achievement key
     * @return The metadata, or null if not found
     */
    public JsonObject getMetadata(String achievementKey) {
        return metadataCache.get(achievementKey);
    }

    /**
     * Get the NFT name from metadata
     * @param achievementKey The achievement key
     * @return The NFT name, or a default name if not found
     */
    public String getNftName(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("name")) {
            return metadata.get("name").getAsString();
        }
        return "NFT #" + achievementKey;
    }

    /**
     * Get the NFT description from metadata
     * @param achievementKey The achievement key
     * @return The NFT description, or a default description if not found
     */
    public String getNftDescription(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("description")) {
            return metadata.get("description").getAsString();
        }
        return "An NFT earned by completing the " + achievementKey + " achievement";
    }

    /**
     * Get the NFT image URL from metadata
     * @param achievementKey The achievement key
     * @return The NFT image URL, or a default URL if not found
     */
    public String getNftImageUrl(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("image")) {
            return metadata.get("image").getAsString();
        }
        return "https://example.com/default.png";
    }

    /**
     * Get the quest type from metadata
     * @param achievementKey The achievement key
     * @return The quest type, or null if not found
     */
    public String getQuestType(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("type")) {
            return metadata.getAsJsonObject("quest").get("type").getAsString();
        }
        return null;
    }

    /**
     * Get the quest target from metadata
     * @param achievementKey The achievement key
     * @return The quest target, or null if not found
     */
    public String getQuestTarget(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("target")) {
            return metadata.getAsJsonObject("quest").get("target").getAsString();
        }
        return null;
    }

    /**
     * Get the quest amount from metadata
     * @param achievementKey The achievement key
     * @return The quest amount, or 0 if not found
     */
    public int getQuestAmount(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("amount")) {
            return metadata.getAsJsonObject("quest").get("amount").getAsInt();
        }
        return 0;
    }

    /**
     * Get the quest duration from metadata
     * @param achievementKey The achievement key
     * @return The quest duration, or 0 if not found
     */
    public int getQuestDuration(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("duration")) {
            return metadata.getAsJsonObject("quest").get("duration").getAsInt();
        }
        return 0;
    }

    /**
     * Get the quest target Y coordinate from metadata
     * @param achievementKey The achievement key
     * @return The quest target Y coordinate, or 0 if not found
     */
    public int getQuestTargetY(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("target_y")) {
            return metadata.getAsJsonObject("quest").get("target_y").getAsInt();
        }
        return 0;
    }

    /**
     * Get the quest description from metadata
     * @param achievementKey The achievement key
     * @return The quest description, or a default description if not found
     */
    public String getQuestDescription(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata != null && metadata.has("quest") && metadata.getAsJsonObject("quest").has("description")) {
            return metadata.getAsJsonObject("quest").get("description").getAsString();
        }
        return "Complete the " + achievementKey + " achievement";
    }

    /**
     * Create a reward item from metadata
     * @param achievementKey The achievement key
     * @return The reward item, or null if not found
     */
    public ItemStack createRewardItem(String achievementKey) {
        JsonObject metadata = getMetadata(achievementKey);
        if (metadata == null || !metadata.has("quest") || !metadata.getAsJsonObject("quest").has("reward")) {
            return null;
        }

        JsonObject reward = metadata.getAsJsonObject("quest").getAsJsonObject("reward");

        // Get item type
        String itemType = reward.has("item") ? reward.get("item").getAsString() : "DIAMOND";
        Material material = Material.getMaterial(itemType);
        if (material == null) {
            material = Material.DIAMOND;
        }

        // Create item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();

        // Set name
        if (reward.has("name")) {
            meta.setDisplayName(reward.get("name").getAsString());
        }

        // Set lore
        if (reward.has("lore")) {
            List<String> lore = new ArrayList<>();
            for (JsonElement element : reward.getAsJsonArray("lore")) {
                lore.add(element.getAsString());
            }
            meta.setLore(lore);
        }

        // Set unbreakable
        if (reward.has("unbreakable") && reward.get("unbreakable").getAsBoolean()) {
            meta.setUnbreakable(true);
        }

        // Apply meta
        item.setItemMeta(meta);

        // Add enchantments
        if (reward.has("enchantments")) {
            for (JsonElement element : reward.getAsJsonArray("enchantments")) {
                String[] parts = element.getAsString().split(":");
                if (parts.length == 2) {
                    String enchantName = parts[0];
                    int level = Integer.parseInt(parts[1]);

                    Enchantment enchantment = getEnchantmentByName(enchantName);
                    if (enchantment != null) {
                        item.addUnsafeEnchantment(enchantment, level);
                    }
                }
            }
        }

        return item;
    }

    /**
     * Get an enchantment by name
     * @param name The enchantment name
     * @return The enchantment, or null if not found
     */
    private Enchantment getEnchantmentByName(String name) {
        switch (name.toUpperCase()) {
            case "DURABILITY":
            case "UNBREAKING":
                return Enchantment.DURABILITY;
            case "EFFICIENCY":
                return Enchantment.DIG_SPEED;
            case "FORTUNE":
                return Enchantment.LOOT_BONUS_BLOCKS;
            case "DAMAGE_UNDEAD":
            case "SMITE":
                return Enchantment.DAMAGE_UNDEAD;
            case "FIRE_ASPECT":
                return Enchantment.FIRE_ASPECT;
            case "LUCK_OF_THE_SEA":
                return Enchantment.LUCK;
            case "LURE":
                return Enchantment.LURE;
            case "PROTECTION_FALL":
            case "FEATHER_FALLING":
                return Enchantment.PROTECTION_FALL;
            default:
                return null;
        }
    }
}
