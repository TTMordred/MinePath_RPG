package me.tien.nftminer.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.scoreboard.CustomScoreboardManager;

public class PlayerJoinListener implements Listener {
    private final NFTMiner plugin;
    private final CustomScoreboardManager scoreboardManager;

    public PlayerJoinListener(NFTMiner plugin, CustomScoreboardManager scoreboardManager) {
        this.plugin = plugin;
        this.scoreboardManager = scoreboardManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        // Cập nhật Scoreboard cho người chơi khi họ tham gia
        scoreboardManager.updateScoreboard(event.getPlayer());
    }
}