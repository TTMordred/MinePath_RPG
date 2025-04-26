package me.tien.nftminer.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.world.VoidMine;

public class ResetMineCommand implements CommandExecutor {
    private final NFTMiner plugin;

    public ResetMineCommand(NFTMiner plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Lệnh này chỉ có thể được sử dụng bởi người chơi!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("nftminer.resetmine")) {
            player.sendMessage(ChatColor.RED + "Bạn không có quyền sử dụng lệnh này!");
            return true;
        }

        // Reset mine của người chơi
        player.sendMessage(ChatColor.YELLOW + "Đang tạo lại khu đào của bạn...");
        VoidMine voidMine = plugin.getVoidMine();
        voidMine.resetPlayerMine(player);
        return true;
    }
}