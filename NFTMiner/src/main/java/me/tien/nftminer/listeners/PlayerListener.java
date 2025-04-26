package me.tien.nftminer.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.tien.nftminer.upgrade.UpgradeManager;

public class PlayerListener implements Listener {
    private final UpgradeManager upgradeManager;

    public PlayerListener(UpgradeManager upgradeManager) {
        this.upgradeManager = upgradeManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // Tải dữ liệu nâng cấp của người chơi
        upgradeManager.loadPlayerData(player);

        // Áp dụng tất cả hiệu ứng nâng cấp cho người chơi
        upgradeManager.applyAllEffects(player);

        // Gửi thông báo cho người chơi về trạng thái nâng cấp
        player.sendMessage("§a§lChào mừng trở lại! Các nâng cấp của bạn đã được áp dụng.");
    }
}