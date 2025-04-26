package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * A simple test command
 */
public class TestCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    public TestCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + "Test command works!");
        plugin.getLogger().info("Test command executed by " + player.getName());
        return true;
    }
}
