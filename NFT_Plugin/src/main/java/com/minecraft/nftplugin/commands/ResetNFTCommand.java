package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ResetNFTCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    public ResetNFTCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Kiểm tra quyền admin
        if (!sender.hasPermission("nftplugin.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Bạn không có quyền sử dụng lệnh này!");
            return true;
        }

        // Kiểm tra lệnh update-dependencies
        if (args.length > 0 && args[0].equalsIgnoreCase("update-dependencies")) {
            boolean clean = args.length > 1 && args[1].equalsIgnoreCase("--clean");
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + "Đang cập nhật dependencies" +
                    (clean ? " (clean install)" : "") + "...");

            // Chạy cập nhật dependencies trong một thread riêng
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    plugin.getSolanaService().updateDependencies(clean);
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + "Đã cập nhật dependencies thành công!");
                } catch (Exception e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Lỗi khi cập nhật dependencies: " + e.getMessage());
                }
            });

            return true;
        }

        // Kiểm tra số lượng tham số
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Sử dụng: /resetnft <player|update-dependencies> [achievement_key|--clean]");
            return true;
        }

        // Lấy tên người chơi
        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID playerUUID = null;

        if (targetPlayer != null) {
            playerUUID = targetPlayer.getUniqueId();
        } else {
            // Tìm UUID từ tên người chơi (nếu người chơi offline)
            try {
                playerUUID = plugin.getDatabaseManager().getUUIDFromName(playerName);
                if (playerUUID == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Không tìm thấy người chơi: " + playerName);
                    return true;
                }
            } catch (Exception e) {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Lỗi khi tìm UUID của người chơi: " + e.getMessage());
                return true;
            }
        }

        // Xác định achievement key
        String achievementKey = args.length > 1 ? args[1] : "wood_chopper";

        // Reset NFT và tiến trình
        try {
            boolean resetNFT = plugin.getDatabaseManager().resetNFT(playerUUID, achievementKey);
            boolean resetProgress = plugin.getDatabaseManager().resetAchievementProgress(playerUUID, achievementKey);

            if (resetNFT || resetProgress) {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + "Đã reset NFT và tiến trình của " +
                        playerName + " cho thành tựu " + achievementKey);

                // Thông báo cho người chơi nếu họ đang online
                if (targetPlayer != null) {
                    targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                            "NFT và tiến trình thành tựu " + achievementKey + " của bạn đã được reset bởi admin.");
                }
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                        "Không tìm thấy NFT hoặc tiến trình nào để reset cho " + playerName);
            }

            return true;
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Lỗi khi reset NFT: " + e.getMessage());
            plugin.getLogger().severe("Error resetting NFT: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
}
