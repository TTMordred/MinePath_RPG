package me.tien.miner_simulator.upgrade;

import java.util.UUID;
import org.bukkit.entity.Player;

public interface Upgrade {
    /**
     * Get the current upgrade level of a player based on UUID.
     *
     * @param uuid Player's UUID
     * @return Current level
     */
    int getLevel(UUID uuid);

    /**
     * Get the current upgrade level of a player.
     *
     * @param player Player
     * @return Current level
     */
    int getLevel(Player player);

    /**
     * Set the upgrade level for a player based on UUID.
     *
     * @param uuid Player's UUID
     * @param level New level
     */
    void setLevel(UUID uuid, int level);

    /**
     * Set the upgrade level for a player.
     *
     * @param player Player
     * @param level New level
     */
    void setLevel(Player player, int level);

    /**
     * Apply the upgrade effect to a player.
     *
     * @param player Player
     */
    void applyEffect(Player player);

    /**
     * Get the cost for the next upgrade level.
     *
     * @param player Player
     * @return Cost for next upgrade level
     */
    int getNextLevelCost(Player player);

    /**
     * Get the maximum upgrade level.
     *
     * @return Maximum level
     */
    int getMaxLevel();

    /**
     * Get the upgrade type (example: "InventoryUpgrade").
     *
     * @return Upgrade type
     */
    String getType();

    /**
     * Save upgrade data.
     */
    void saveData();

    /**
     * Get the effect corresponding to the level.
     *
     * @param level Level
     * @return Effect
     */
    int getEffectLevel(int level);

    /**
     * Load upgrade configuration from config file.
     */
    void loadConfig();

    /**
     * Perform an upgrade for a player.
     *
     * @param player Player
     * @return `true` if upgrade successful, `false` if failed
     */
    boolean upgrade(Player player);

    /**
     * Load player data from config file.
     *
     * @param player Player
     */
    void loadPlayerData(Player player);
}