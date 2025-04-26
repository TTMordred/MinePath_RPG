
package me.tien.nftminer.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.upgrade.UpgradeManager;

public class ResetUpgradeCommand implements CommandExecutor {
    private final NFTMiner plugin;
    private final UpgradeManager upgradeManager;

    public ResetUpgradeCommand(NFTMiner plugin, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cLệnh này chỉ có thể được sử dụng bởi người chơi!");
            return true;
        }

        Player player = (Player) sender;

        // Đặt lại cấp độ của tất cả các nâng cấp
        upgradeManager.getInventoryUpgrade().setLevel(player, 0);
        upgradeManager.getSpeedUpgrade().setLevel(player, 0);
        upgradeManager.getTokenValueUpgrade().setLevel(player, 0);

        // Lưu dữ liệu
        upgradeManager.saveAllData();

        // Thông báo cho người chơi
        player.sendMessage("§aTất cả cấp độ nâng cấp của bạn đã được đặt lại!");
        plugin.getLogger().info("Player " + player.getName() + " đã reset toàn bộ cấp độ nâng cấp.");

        return true;
    }
}