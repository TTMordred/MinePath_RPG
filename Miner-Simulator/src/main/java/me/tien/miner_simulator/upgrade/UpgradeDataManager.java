package me.tien.miner_simulator.upgrade;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import me.tien.miner_simulator.Miner_Simulator;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.logging.Level;

public class UpgradeDataManager {
    private final Miner_Simulator plugin;
    private final String upgradeType;
    private File playerDataFolder;

    public UpgradeDataManager(Miner_Simulator plugin, String upgradeType) {
        this.plugin = plugin;
        this.upgradeType = upgradeType;
        setupDataFolder();
    }

    private void setupDataFolder() {
        // Ensure players directory exists
        playerDataFolder = new File(plugin.getDataFolder(), "players");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    private File getPlayerFile(UUID playerUuid) {
        File playerFile = new File(playerDataFolder, playerUuid.toString() + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create data file for player " + playerUuid, e);
            }
        }
        return playerFile;
    }

    private FileConfiguration getPlayerConfig(UUID playerUuid) {
        File playerFile = getPlayerFile(playerUuid);
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public int getPlayerLevel(UUID playerUuid) {
        FileConfiguration config = getPlayerConfig(playerUuid);
        return config.getInt("upgrades." + upgradeType + ".level", 0);
    }

    public void setPlayerLevel(UUID playerUuid, int level) {
        File playerFile = getPlayerFile(playerUuid);
        FileConfiguration config = YamlConfiguration.loadConfiguration(playerFile);
        config.set("upgrades." + upgradeType + ".level", level);
        try {
            config.save(playerFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save data for player " + playerUuid, e);
        }
    }

    public void saveData(UUID playerUuid, int level) {
        setPlayerLevel(playerUuid, level);
    }

    public void reloadData(UUID playerUuid) {
    }
}