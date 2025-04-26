package me.tien.nftminer.scoreboard;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.integration.MinePathIntegration;
import me.tien.nftminer.upgrade.InventoryUpgrade;
import me.tien.nftminer.upgrade.SpeedUpgrade;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.math.BigDecimal;

public class CustomScoreboardManager {
    private final NFTMiner plugin;
    private final MinePathIntegration minePathIntegration;

    public CustomScoreboardManager(NFTMiner plugin, MinePathIntegration minePathIntegration) {
        this.plugin = plugin;
        this.minePathIntegration = minePathIntegration;
    }

    public void updateScoreboard(Player player) {
        org.bukkit.scoreboard.ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        if (scoreboardManager == null)
            return;
    
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();
        Objective objective = scoreboard.registerNewObjective("NFTMiner", "dummy", ChatColor.GOLD + "NFT Miner");
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);
    
        // Lấy số dư từ MinePathIntegration
        BigDecimal balance = minePathIntegration.getBalance(player.getUniqueId());
    
        // Thêm thông tin số dư vào Scoreboard
        Score balanceScore = objective.getScore(ChatColor.GREEN + "Số dư: " + ChatColor.WHITE + balance + " token");
        balanceScore.setScore(1);
    
        // Gán Scoreboard cho người chơi
        player.setScoreboard(scoreboard);
    }
}