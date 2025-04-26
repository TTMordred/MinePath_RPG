package me.tien.nftminer.commands;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class TokenCommand implements CommandExecutor {
    private final NFTMiner plugin;
    private final TokenManager tokenManager;

    public TokenCommand(NFTMiner plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }

        Player player = (Player) sender;
        BigDecimal tokens = tokenManager.getTokens(player);
        player.sendMessage("§a§l===== THÔNG TIN TOKEN =====");
        player.sendMessage("§aSố token hiện có: §e" + tokens);
        player.sendMessage("§a§l===== GIÁ TRỊ BLOCK =====");
        player.sendMessage("§aCobblestone: §e" + tokenManager.getBlockValue(Material.COBBLESTONE) + " token");
        player.sendMessage("§aIron Block: §e" + tokenManager.getBlockValue(Material.IRON_BLOCK) + " token");
        player.sendMessage("§aGold Block: §e" + tokenManager.getBlockValue(Material.GOLD_BLOCK) + " token");
        player.sendMessage("§aDiamond Block: §e" + tokenManager.getBlockValue(Material.DIAMOND_BLOCK) + " token");
        player.sendMessage("§a§l=========================");
        player.sendMessage("§aGõ §e/claim §ađể đổi các block thành token");
        player.sendMessage("§aGõ §e/shop §ađể mở cửa hàng nâng cấp");

        return true;
    }
}