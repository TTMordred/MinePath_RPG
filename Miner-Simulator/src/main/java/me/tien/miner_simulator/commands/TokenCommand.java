package me.tien.miner_simulator.commands;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.math.BigDecimal;

public class TokenCommand implements CommandExecutor {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;

    public TokenCommand(Miner_Simulator plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        BigDecimal tokens = tokenManager.getTokens(player);
        player.sendMessage("§a§l===== TOKEN INFORMATION =====");
        player.sendMessage("§aCurrent tokens: §e" + tokens);
        player.sendMessage("§a§l===== BLOCK VALUES =====");
        player.sendMessage("§aCobblestone: §e" + tokenManager.getBlockValue(Material.COBBLESTONE) + " tokens");
        player.sendMessage("§aIron Block: §e" + tokenManager.getBlockValue(Material.IRON_BLOCK) + " tokens");
        player.sendMessage("§aGold Block: §e" + tokenManager.getBlockValue(Material.GOLD_BLOCK) + " tokens");
        player.sendMessage("§aDiamond Block: §e" + tokenManager.getBlockValue(Material.DIAMOND_BLOCK) + " tokens");
        player.sendMessage("§a§l=========================");
        player.sendMessage("§aType §e/claim §ato convert blocks to tokens");
        player.sendMessage("§aType §e/shop §ato open the upgrade shop");

        return true;
    }
}