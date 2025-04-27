package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;



public class DBCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public DBCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.db")
        ) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("connect")) {
                    if (!this.plugin.db.isConnected()) {
                        plugin.loadSQL();
                        sender.sendMessage(plugin.chatPrefix+"DB is " + (plugin.db.isConnected() ? "connected." : "disconnected."));
                    } else {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "DB is already connected.");
                    }

                } else if (args[0].equalsIgnoreCase("disconnect")) {
                    if (plugin.db.isConnected()) {
                        plugin.db.disconnect();
                        sender.sendMessage(plugin.chatPrefix+"DB is " + (plugin.db.isConnected() ? "connected." : "disconnected."));
                    } else {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "DB is not connected.");
                    }
                } else if (args[0].equalsIgnoreCase("status")) {
                    sender.sendMessage(plugin.chatPrefix+"DB is " + (plugin.db.isConnected() ? "connected." : "disconnected."));
                }
            } else {
                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:db [connect|disconnect|status]");
            }
        } else {
            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
