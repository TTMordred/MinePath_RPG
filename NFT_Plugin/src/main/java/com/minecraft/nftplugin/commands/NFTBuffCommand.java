package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.buffs.BuffType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Command to display NFT buff information
 */
public class NFTBuffCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    public NFTBuffCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // If no arguments, show the sender's buffs (if they're a player)
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Usage: /nftbuff <player>");
                return true;
            }

            Player player = (Player) sender;

            // Update and show the player's buffs
            plugin.getBuffManager().updatePlayerBuffs(player);
            showPlayerBuffs(player);

            return true;
        }

        // If there are arguments, check if the sender has admin permission
        if (!sender.hasPermission("nftplugin.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "You don't have permission to check other players' buffs.");
            return true;
        }

        // Get the target player
        String targetName = args[0];
        Player targetPlayer = plugin.getServer().getPlayer(targetName);

        if (targetPlayer == null) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Player not found: " + targetName);
            return true;
        }

        // Update and show the target player's buffs in compact format
        plugin.getBuffManager().updatePlayerBuffs(targetPlayer);
        showPlayerBuffsCompact(sender, targetPlayer);

        return true;
    }

    /**
     * Show buffs for a player
     * @param player The player
     */
    private void showPlayerBuffs(Player player) {
        Map<BuffType, Integer> playerBuffs = plugin.getBuffManager().getAllPlayerBuffs()
                .getOrDefault(player.getUniqueId(), new HashMap<>());

        player.sendMessage(ChatColor.GOLD + "===== Your NFT Buffs =====");

        if (playerBuffs.isEmpty()) {
            player.sendMessage(ChatColor.YELLOW + "You don't have any active buffs.");
            return;
        }

        for (Map.Entry<BuffType, Integer> entry : playerBuffs.entrySet()) {
            BuffType buffType = entry.getKey();
            int value = entry.getValue();

            player.sendMessage(ChatColor.AQUA + buffType.getDisplayName() + ": " + ChatColor.WHITE + "+" + value + "%");
        }
    }

    /**
     * Show buffs for a player in compact format (for admins)
     * @param sender The command sender
     * @param targetPlayer The target player
     */
    private void showPlayerBuffsCompact(CommandSender sender, Player targetPlayer) {
        // Force update player buffs to ensure we have the latest data
        plugin.getBuffManager().updatePlayerBuffs(targetPlayer);

        Map<BuffType, Integer> playerBuffs = plugin.getBuffManager().getAllPlayerBuffs()
                .getOrDefault(targetPlayer.getUniqueId(), new HashMap<>());

        // Debug: Log the player's buffs
        plugin.getLogger().info("Player " + targetPlayer.getName() + " has buffs: " + playerBuffs);

        // Build compact format string with just the numbers
        StringBuilder compactFormat = new StringBuilder();

        // Add values for each buff type
        for (BuffType buffType : BuffType.values()) {
            int value = playerBuffs.getOrDefault(buffType, 0);
            plugin.getLogger().info("Buff " + buffType.name() + " = " + value);
            if (value > 0) {
                compactFormat.append(value).append(" ");
            }
        }

        // If no buffs, show 0
        if (compactFormat.length() == 0) {
            compactFormat.append("0");
        }

        // Send the compact format
        sender.sendMessage(ChatColor.GOLD + "Buffs for " + targetPlayer.getName() + ": " +
                           ChatColor.WHITE + compactFormat.toString().trim());
    }
}
