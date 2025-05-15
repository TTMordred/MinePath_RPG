package me.tien.miner_simulator.integration;

import com.google.gson.JsonObject;
import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.service.NFTMetadataService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.logging.Level;

public class NFTPluginIntegration {

    private final Miner_Simulator plugin;
    private Plugin nftPluginInstance;
    private NFTMetadataService metadataService;
    private boolean nftPluginAvailable = false;

    public NFTPluginIntegration(Miner_Simulator plugin) {
        this.plugin = plugin;
        this.metadataService = new NFTMetadataService(plugin);
        initialize();
    }

    private void initialize() {
        try {
            nftPluginInstance = Bukkit.getPluginManager().getPlugin("NFTPlugin");
            if (nftPluginInstance == null || !nftPluginInstance.isEnabled()) {
                plugin.getLogger().warning("[NFTMiner] NFTPlugin is not available or not enabled.");
                return;
            }

            // Check if NFTPlugin is ready
            nftPluginAvailable = true;
            plugin.getLogger().info("[NFTMiner] Successfully connected to NFTPlugin!");

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "[NFTMiner] Error when connecting to NFTPlugin:", e);
        }
    }

    public boolean isNFTPluginAvailable() {
        return nftPluginAvailable;
    }

    public Plugin getNFTPluginInstance() {
        return nftPluginInstance;
    }

    /**
     * Get metadata of a specific NFT
     * @param nftKey NFT key
     * @return Metadata as JsonObject, or null if not found
     */
    public JsonObject getNFTMetadata(String nftKey) {
        return metadataService.getMetadata(nftKey);
    }

    /**
     * Load all NFTs categorized by rarity from the NFTPlugin metadata directory
     * @return Map containing lists of NFTs by rarity
     */
    public Map<String, List<String>> loadNFTsByRarity() {
        return metadataService.getNFTsByRarity();
    }

    /**
     * Refresh metadata cache
     */
    public void refreshMetadataCache() {
        metadataService.refreshCache();
    }
}
