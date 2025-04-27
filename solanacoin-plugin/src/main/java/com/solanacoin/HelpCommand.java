package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class HelpCommand implements CommandExecutor {
    private final MinePathCoinPlugin plugin;

    public HelpCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.hasPermission(sender, "minepath.admin")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Available MinePath Commands (Admin):");
            plugin.sendUsage(sender, "/MINEPATH:admin [balance | add | delete | subtract | set | destroydb | reload]");
            plugin.sendUsage(sender, "/MINEPATH:balance");
            plugin.sendUsage(sender, "/MINEPATH:db [connect | disconnect | status]");
            plugin.sendUsage(sender, "/MINEPATH:export <amount> confirm");
            plugin.sendUsage(sender, "/MINEPATH:lbh");
            plugin.sendUsage(sender, "/MINEPATH:send <player> <amount> confirm");
            // plugin.sendUsage(sender, "/MINEPATH:exportpath <pathAmount> [confirm]");
        } else {
            sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Available MinePath Commands:");
            plugin.sendUsage(sender, "/MINEPATH:balance");
            plugin.sendUsage(sender, "/MINEPATH:export <amount> confirm");
            plugin.sendUsage(sender, "/MINEPATH:lbh");
            plugin.sendUsage(sender, "/MINEPATH:send <player> <amount> confirm");
            // plugin.sendUsage(sender, "/MINEPATH:exportpath <pathAmount> [confirm]");
        }
        return true;
    }
}