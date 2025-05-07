package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to display help information about NFT plugin commands
 */
public class NFTHelpCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    public NFTHelpCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has OP permissions
        if (player.isOp() || player.hasPermission("nftplugin.admin")) {
            // Show both user and admin commands for OP players
            showFullHelp(player);
        } else {
            // Show only user commands for non-OP players
            showUserHelp(player);
        }

        return true;
    }

    /**
     * Display help information for regular users
     * @param player The player to show help to
     */
    private void showUserHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✦ NFT Plugin Help ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Player Commands:");
        player.sendMessage(ChatColor.AQUA + "/nftinfo" + ChatColor.WHITE + " - View NFT item information");
        player.sendMessage(ChatColor.AQUA + "/nftlist" + ChatColor.WHITE + " - Browse all your NFTs");
        player.sendMessage(ChatColor.AQUA + "/nftinv" + ChatColor.WHITE + " - Open NFT inventory with unlimited pages");
        player.sendMessage(ChatColor.AQUA + "/nftbuff" + ChatColor.WHITE + " - View your active NFT buffs");
        player.sendMessage(ChatColor.AQUA + "/nfthelp" + ChatColor.WHITE + " - Show this help message");
    }

    /**
     * Display complete help information for OP/admin players
     * @param player The admin player to show help to
     */
    private void showFullHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "✦ NFT Plugin Help ✦");
        player.sendMessage("");
        player.sendMessage(ChatColor.YELLOW + "Player Commands:");
        player.sendMessage(ChatColor.AQUA + "/nftinfo" + ChatColor.WHITE + " - View NFT item information");
        player.sendMessage(ChatColor.AQUA + "/nftlist" + ChatColor.WHITE + " - Browse all your NFTs");
        player.sendMessage(ChatColor.AQUA + "/nftinv" + ChatColor.WHITE + " - Open NFT inventory with unlimited pages");
        player.sendMessage(ChatColor.AQUA + "/nftbuff" + ChatColor.WHITE + " - View your active NFT buffs");
        player.sendMessage(ChatColor.AQUA + "/nfthelp" + ChatColor.WHITE + " - Show this help message");
        player.sendMessage("");
        player.sendMessage(ChatColor.RED + "Admin Commands:");
        player.sendMessage(ChatColor.RED + "/resetnft <player> [achievement]");
        player.sendMessage(ChatColor.WHITE + "  Reset a player's NFT progress");
        player.sendMessage(ChatColor.RED + "/mintnft <player> <metadata_key>");
        player.sendMessage(ChatColor.WHITE + "  Mint an NFT for a player");
        player.sendMessage(ChatColor.RED + "/nftbuff <player>");
        player.sendMessage(ChatColor.WHITE + "  View a player's active NFT buffs");
        player.sendMessage(ChatColor.RED + "/test");
        player.sendMessage(ChatColor.WHITE + "  Test command for debugging");
    }
}
