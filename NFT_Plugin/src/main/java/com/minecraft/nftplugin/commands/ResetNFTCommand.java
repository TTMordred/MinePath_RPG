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
        // Check admin permission
        if (!sender.hasPermission("nftplugin.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        // Check for update-dependencies command
        if (args.length > 0 && args[0].equalsIgnoreCase("update-dependencies")) {
            boolean clean = args.length > 1 && args[1].equalsIgnoreCase("--clean");
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + "Updating dependencies" +
                    (clean ? " (clean install)" : "") + "...");

            // Run dependency update in a separate thread
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    plugin.getSolanaService().updateDependencies(clean);
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + "Dependencies updated successfully!");
                } catch (Exception e) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Error updating dependencies: " + e.getMessage());
                }
            });

            return true;
        }

        // Check parameter count
        if (args.length < 1) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Usage: /resetnft <player|update-dependencies> [achievement_key|--clean]");
            return true;
        }

        // Get player name
        String playerName = args[0];
        Player targetPlayer = Bukkit.getPlayer(playerName);
        UUID playerUUID = null;

        if (targetPlayer != null) {
            playerUUID = targetPlayer.getUniqueId();
        } else {
            // Find UUID from player name (if player is offline)
            try {
                playerUUID = plugin.getDatabaseManager().getUUIDFromName(playerName);
                if (playerUUID == null) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Player not found: " + playerName);
                    return true;
                }
            } catch (Exception e) {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Error finding player UUID: " + e.getMessage());
                return true;
            }
        }

        // Determine achievement key
        String achievementKey = args.length > 1 ? args[1] : "wood_chopper";

        // Reset NFT and progress
        try {
            boolean resetNFT = plugin.getDatabaseManager().resetNFT(playerUUID, achievementKey);
            boolean resetProgress = plugin.getDatabaseManager().resetAchievementProgress(playerUUID, achievementKey);

            if (resetNFT || resetProgress) {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + "Reset NFT and progress for " +
                        playerName + " for achievement " + achievementKey);

                // Notify player if they are online
                if (targetPlayer != null) {
                    targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                            "Your NFT and achievement progress for " + achievementKey + " has been reset by an admin.");
                }
            } else {
                sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                        "No NFT or progress found to reset for " + playerName);
            }

            return true;
        } catch (Exception e) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "Error resetting NFT: " + e.getMessage());
            plugin.getLogger().severe("Error resetting NFT: " + e.getMessage());
            e.printStackTrace();
            return true;
        }
    }
}
