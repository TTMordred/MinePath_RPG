package me.tien.nftminer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.world.VoidMine;

public class MiningBoxCommand implements CommandExecutor {
    private final NFTMiner plugin;

    public MiningBoxCommand(NFTMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ có thể được sử dụng bởi người chơi!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nftminer.miningbox")) {
            player.sendMessage(ChatColor.RED + "Bạn không có quyền sử dụng lệnh này!");
            return true;
        }

        // Lấy khu đào của người chơi và teleport đến đó
        VoidMine voidMine = plugin.getVoidMine();
        if (voidMine != null) {
            VoidMine.PlayerMine playerMine = voidMine.getPlayerMine(player);
            if (playerMine != null) {
                playerMine.teleportPlayer(player);
            } else {
                player.sendMessage(ChatColor.RED + "Không thể tìm thấy khu đào của bạn!");
            }
        } else {
            player.sendMessage(ChatColor.RED + "Hệ thống khu đào hiện không khả dụng!");
        }

        return true;
    }
}