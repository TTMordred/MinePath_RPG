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

        // Check if the player is requesting admin help
        if (args.length > 0 && args[0].equalsIgnoreCase("admin")) {
            // Only show admin commands if the player has admin permission
            if (player.hasPermission("nftplugin.admin")) {
                showAdminHelp(player);
            } else {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "You don't have permission to view admin commands.");
            }
            return true;
        }

        // Show regular user commands
        showUserHelp(player);
        return true;
    }

    /**
     * Display help information for regular users
     * @param player The player to show help to
     */
    private void showUserHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.YELLOW + "✨ NFT Plugin Help ✨" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "Player Commands:" + ChatColor.GOLD + "                           ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/nftinfo" + ChatColor.WHITE + " - View NFT item information       " + ChatColor.GOLD + "║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/nftlist" + ChatColor.WHITE + " - Browse all your NFTs            " + ChatColor.GOLD + "║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/nftinv [page]" + ChatColor.WHITE + " - Open NFT inventory        " + ChatColor.GOLD + "║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/nftbuff" + ChatColor.WHITE + " - View your active NFT buffs      " + ChatColor.GOLD + "║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.AQUA + "/nfthelp" + ChatColor.WHITE + " - Show this help message          " + ChatColor.GOLD + "║");

        // If the player has admin permission, show a hint about admin commands
        if (player.hasPermission("nftplugin.admin")) {
            player.sendMessage(ChatColor.GOLD + "║                                              ║");
            player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "You have admin access!" + ChatColor.GOLD + "                     ║");
            player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "Use " + ChatColor.AQUA + "/nfthelp admin" + ChatColor.YELLOW + " for admin commands.    " + ChatColor.GOLD + "║");
        }

        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════════════╝");
        player.sendMessage("");
    }

    /**
     * Display help information for administrators
     * @param player The admin player to show help to
     */
    private void showAdminHelp(Player player) {
        player.sendMessage("");
        player.sendMessage(ChatColor.GOLD + "╔══════════ " + ChatColor.RED + "⚙ NFT Plugin Admin Help ⚙" + ChatColor.GOLD + " ══════════╗");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.RED + "Admin Commands:" + ChatColor.GOLD + "                           ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.RED + "/resetnft <player> [achievement]" + ChatColor.GOLD + "         ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "  Reset a player's NFT progress" + ChatColor.GOLD + "            ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.RED + "/mintnft <player> <metadata_key>" + ChatColor.GOLD + "         ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "  Mint an NFT for a player" + ChatColor.GOLD + "                 ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.RED + "/nftbuff <player>" + ChatColor.GOLD + "                        ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "  View a player's active NFT buffs" + ChatColor.GOLD + "         ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.RED + "/test" + ChatColor.GOLD + "                                     ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.WHITE + "  Test command for debugging" + ChatColor.GOLD + "               ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "║  " + ChatColor.YELLOW + "Use " + ChatColor.AQUA + "/nfthelp" + ChatColor.YELLOW + " for player commands." + ChatColor.GOLD + "        ║");
        player.sendMessage(ChatColor.GOLD + "║                                              ║");
        player.sendMessage(ChatColor.GOLD + "╚══════════════════════════════════════════════╝");
        player.sendMessage("");
    }
}
