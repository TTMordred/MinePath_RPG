package com.minecraft.nftplugin.achievements;

import com.minecraft.nftplugin.NFTPlugin;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Manager for all achievements
 */
public class AchievementManager {

    private final NFTPlugin plugin;
    private final Map<String, Achievement> achievements;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public AchievementManager(NFTPlugin plugin) {
        this.plugin = plugin;
        this.achievements = new HashMap<>();
    }

    /**
     * Register an achievement
     * @param achievement The achievement to register
     */
    public void registerAchievement(Achievement achievement) {
        achievements.put(achievement.getKey(), achievement);
        achievement.initialize();
    }

    /**
     * Get an achievement by key
     * @param key The achievement key
     * @return The achievement, or empty if not found
     */
    public Optional<Achievement> getAchievement(String key) {
        return Optional.ofNullable(achievements.get(key));
    }

    /**
     * Get all registered achievements
     * @return A set of all achievement keys
     */
    public Set<String> getAchievementKeys() {
        return achievements.keySet();
    }

    /**
     * Update progress for an achievement
     * @param player The player
     * @param key The achievement key
     * @param progress The new progress
     * @return True if the progress was updated, false otherwise
     */
    public boolean updateProgress(Player player, String key, int progress) {
        Optional<Achievement> achievement = getAchievement(key);
        return achievement.isPresent() && achievement.get().updateProgress(player, progress);
    }

    /**
     * Increment progress for an achievement
     * @param player The player
     * @param key The achievement key
     * @param amount The amount to increment
     * @return True if the progress was updated, false otherwise
     */
    public boolean incrementProgress(Player player, String key, int amount) {
        Optional<Achievement> achievement = getAchievement(key);
        if (achievement.isPresent()) {
            int currentProgress = achievement.get().getCurrentProgress(player);
            return achievement.get().updateProgress(player, currentProgress + amount);
        }
        return false;
    }

    /**
     * Check if a player has completed an achievement
     * @param player The player
     * @param key The achievement key
     * @return True if the player has completed the achievement, false otherwise
     */
    public boolean isAchievementCompleted(Player player, String key) {
        Optional<Achievement> achievement = getAchievement(key);
        return achievement.isPresent() && achievement.get().isCompleted(player);
    }

    /**
     * Initialize all achievements
     */
    public void initializeAchievements() {
        // Register default achievements
        registerDefaultAchievements();

        plugin.getLogger().info("Initialized " + achievements.size() + " achievements");
    }

    /**
     * Register default achievements
     */
    private void registerDefaultAchievements() {
        plugin.getLogger().info("=== REGISTERING ACHIEVEMENTS ===");

        // First register achievements from config
        registerAchievementsFromConfig();

        // Then register achievements from metadata files
        registerAchievementsFromMetadataFiles();

        // Log all registered achievements
        plugin.getLogger().info("Total registered achievement keys: " + String.join(", ", getAchievementKeys()));
        plugin.getLogger().info("=== FINISHED REGISTERING ACHIEVEMENTS ===");
    }

    /**
     * Register achievements from config.yml
     */
    private void registerAchievementsFromConfig() {
        plugin.getLogger().info("--- Registering achievements from config.yml ---");
        plugin.getLogger().info("Config file path: " + plugin.getDataFolder().getAbsolutePath() + "/config.yml");

        // Get achievement keys from config
        if (plugin.getConfig().contains("achievements")) {
            plugin.getLogger().info("Found achievements section in config");

            // Log all achievement keys
            plugin.getLogger().info("Achievement keys in config: " +
                    String.join(", ", plugin.getConfig().getConfigurationSection("achievements").getKeys(false)));

            for (String key : plugin.getConfig().getConfigurationSection("achievements").getKeys(false)) {
                plugin.getLogger().info("Processing achievement from config: " + key);

                // Check if achievement is enabled
                boolean enabled = plugin.getConfigManager().isAchievementEnabled(key);
                plugin.getLogger().info("Achievement " + key + " enabled: " + enabled);

                if (!enabled) {
                    plugin.getLogger().info("Achievement " + key + " is disabled in config, skipping");
                    continue;
                }

                // Get achievement type
                String type = plugin.getConfig().getString("achievements." + key + ".type", "");
                plugin.getLogger().info("Achievement " + key + " type: " + type);

                // Create achievement based on type
                if (type.equals("named_item")) {
                    // Get material and item name from config
                    String materialName = plugin.getConfig().getString("achievements." + key + ".material", "BLAZE_ROD");
                    String itemName = plugin.getConfig().getString("achievements." + key + ".item_name", "Unknown Item");

                    plugin.getLogger().info("Achievement " + key + " material: " + materialName);
                    plugin.getLogger().info("Achievement " + key + " item name: " + itemName);

                    try {
                        Material material = Material.valueOf(materialName);

                        // Create and register achievement for any named item
                        plugin.getLogger().info("Creating NamedItemAchievement for " + key + " with material " + material + " and item name " + itemName);
                        registerAchievement(new NamedItemAchievement(plugin, key, material, itemName));
                        plugin.getLogger().info("Successfully registered named item achievement from config: " + key);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material in config for achievement " + key + ": " + materialName);
                    }
                } else {
                    plugin.getLogger().warning("Unknown achievement type for " + key + ": " + type);
                }
            }
        } else {
            plugin.getLogger().info("No achievements found in config.yml");
        }
    }

    /**
     * Register achievements from metadata files in the metadata directory
     */
    private void registerAchievementsFromMetadataFiles() {
        plugin.getLogger().info("--- Registering achievements from metadata files ---");

        // Get metadata directory
        File metadataDir = new File(plugin.getDataFolder(), "metadata");
        if (!metadataDir.exists()) {
            metadataDir.mkdirs();
            plugin.getLogger().info("Created metadata directory: " + metadataDir.getAbsolutePath());
            return;
        }

        // Get all JSON files in the metadata directory
        File[] metadataFiles = metadataDir.listFiles((dir, name) -> name.toLowerCase().endsWith(".json"));
        if (metadataFiles == null || metadataFiles.length == 0) {
            plugin.getLogger().info("No metadata files found in: " + metadataDir.getAbsolutePath());
            return;
        }

        plugin.getLogger().info("Found " + metadataFiles.length + " metadata files");

        // Process each metadata file
        for (File file : metadataFiles) {
            String fileName = file.getName();
            String achievementKey = fileName.substring(0, fileName.lastIndexOf('.'));

            // Skip if achievement is already registered
            if (achievements.containsKey(achievementKey)) {
                plugin.getLogger().info("Achievement " + achievementKey + " already registered from config, skipping metadata file");
                continue;
            }

            plugin.getLogger().info("Processing metadata file: " + fileName + " for achievement key: " + achievementKey);

            try {
                // Read metadata file
                Gson gson = new Gson();
                Reader reader = new FileReader(file);
                JsonObject metadata = gson.fromJson(reader, JsonObject.class);
                reader.close();

                // Extract information from metadata
                if (metadata.has("quest") && metadata.getAsJsonObject("quest").has("type")) {
                    String questType = metadata.getAsJsonObject("quest").get("type").getAsString();

                    if (questType.equals("HOLD_NAMED_ITEM_INSTANT")) {
                        // Get target material and name
                        String targetMaterial = metadata.getAsJsonObject("quest").get("target").getAsString();
                        String targetName = metadata.getAsJsonObject("quest").get("target_name").getAsString();

                        plugin.getLogger().info("Metadata file contains named item achievement: " +
                                "material=" + targetMaterial + ", name=" + targetName);

                        try {
                            Material material = Material.valueOf(targetMaterial);

                            // Create and register achievement
                            registerAchievement(new NamedItemAchievement(plugin, achievementKey, material, targetName));
                            plugin.getLogger().info("Successfully registered named item achievement from metadata: " + achievementKey);
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Invalid material in metadata for achievement " + achievementKey + ": " + targetMaterial);
                        }
                    } else {
                        plugin.getLogger().info("Unsupported quest type in metadata: " + questType);
                    }
                } else {
                    plugin.getLogger().info("Metadata file does not contain quest information, skipping");
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Error processing metadata file " + fileName + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}
