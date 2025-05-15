package me.tien.miner_simulator.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.tien.miner_simulator.upgrade.UpgradeManager;

public class PlayerListener implements Listener {
    private final UpgradeManager upgradeManager;

    public PlayerListener(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Load player's upgrade data
        upgradeManager.loadPlayerData(player);

        // Apply all upgrade effects to the player
        upgradeManager.applyAllEffects(player);

        // Send message to player about upgrade status
        player.sendMessage("§a§lWelcome back! Your upgrades have been applied.");
    }
}