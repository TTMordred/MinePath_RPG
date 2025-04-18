package com.minecraft.nftplugin.achievements;

import org.bukkit.entity.Player;

/**
 * Interface for all achievements
 */
public interface Achievement {
    
    /**
     * Get the achievement key
     * @return The achievement key
     */
    String getKey();
    
    /**
     * Get the achievement name
     * @return The achievement name
     */
    String getName();
    
    /**
     * Get the achievement description
     * @return The achievement description
     */
    String getDescription();
    
    /**
     * Get the required progress to complete the achievement
     * @return The required progress
     */
    int getRequiredProgress();
    
    /**
     * Get the current progress for a player
     * @param player The player
     * @return The current progress
     */
    int getCurrentProgress(Player player);
    
    /**
     * Update the progress for a player
     * @param player The player
     * @param progress The new progress
     * @return True if the progress was updated, false otherwise
     */
    boolean updateProgress(Player player, int progress);
    
    /**
     * Check if a player has completed the achievement
     * @param player The player
     * @return True if the player has completed the achievement, false otherwise
     */
    boolean isCompleted(Player player);
    
    /**
     * Get the NFT metadata file path
     * @return The NFT metadata file path
     */
    String getMetadataFilePath();
    
    /**
     * Get the NFT image URL
     * @return The NFT image URL
     */
    String getImageUrl();
    
    /**
     * Register event listeners for this achievement
     */
    void registerEvents();
    
    /**
     * Initialize the achievement
     */
    void initialize();
}
