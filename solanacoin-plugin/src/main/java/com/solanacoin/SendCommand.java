package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public SendCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.send")
        ) {
            if (args.length >= 2) {
                if (sender instanceof Player pSender) {
                    double senderBalance = plugin.db.getBalanceOfPlayer(pSender.getUniqueId());

                    OfflinePlayer pReceiver = plugin.getServer().getOfflinePlayer(args[0]);

                    if (!plugin.db.playerExistsInBalanceTable(pReceiver.getUniqueId())) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + " Player " + args[0] + " not found in db.");
                        return true;
                    }

                    if (pSender.getUniqueId().equals(pReceiver.getUniqueId())) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You cannot send to yourself!");
                        return true;
                    }

                    double amount = 0;

                    try {
                        amount = Double.parseDouble(args[1]);
                    } catch (NullPointerException | NumberFormatException nullPointerException) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED+"Failed to parse <amount>. Please make sure your sending a valid number.");
                        return true;
                    }
                    if (amount > 0) {
                        if (senderBalance >= amount) {
                            if (args.length == 2) {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.AQUA + "Please confirm you want to send " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:send <player> <amount> confirm");
                                return true;
                            }
                            if (args[2].equalsIgnoreCase("confirm")) {
                                boolean removedBalance = plugin.db.addBalanceToPlayer(pSender.getUniqueId(), -amount);
                                if (removedBalance) {
                                    boolean addedBalance = plugin.db.addBalanceToPlayer(pReceiver.getUniqueId(), amount);
                                    if (addedBalance) {
                                        sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Sent " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                                        if (pReceiver.isOnline()) {
                                            plugin.getServer().getPlayer(pReceiver.getUniqueId()).sendMessage(plugin.chatPrefix+ChatColor.GOLD + "" +pSender.getName() + " sent you " + amount + " " + plugin.currencySymbol + "!");
                                        }
                                    } else {
                                        plugin.db.addBalanceToPlayer(pSender.getUniqueId(), amount);
                                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Failed to send " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                                    }
                                }
                            } else {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.AQUA + "Please confirm you want to send " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:send <player> <amount> confirm");
                            }

                        } else {
                            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You do not have enough " + plugin.currencySymbol + " to send.");
                        }
                    } else {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Amount must be greater than zero " + plugin.currencySymbol +  ".");
                    }
                    return true;
                } else {
                    OfflinePlayer pReceiver = plugin.getServer().getOfflinePlayer(args[0]);

                    if (!plugin.db.playerExistsInBalanceTable(pReceiver.getUniqueId())) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Player " + args[0] + " not found in db.");
                        return true;
                    }
                    double amount = 0;

                    try {
                        amount = Double.parseDouble(args[1]);
                    } catch (NullPointerException | NumberFormatException nullPointerException) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED+"Failed to parse <amount>. Please make sure your sending a valid number.");
                        return true;
                    }

                    if (amount > 0) {
                        if (args.length == 2) {
                            sender.sendMessage(plugin.chatPrefix+"Please confirm you want to send " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                            return true;
                        }
                        if (args[2].equalsIgnoreCase("confirm")) {
                            boolean addedBalance = plugin.db.addBalanceToPlayer(pReceiver.getUniqueId(), amount);
                            if (addedBalance) {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Sent " + ChatColor.YELLOW + amount + " " + ChatColor.GREEN + plugin.currencySymbol + " to " + ChatColor.GOLD + pReceiver.getName());
                                if (pReceiver.isOnline()) {
                                    plugin.getServer().getPlayer(pReceiver.getUniqueId()).sendMessage(plugin.chatPrefix+ChatColor.GOLD + "Server" + ChatColor.GREEN + " sent you " + ChatColor.YELLOW + amount + " " + ChatColor.GREEN + plugin.currencySymbol + "!");
                                }
                            } else {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Failed to send " + ChatColor.YELLOW + amount + " " + ChatColor.RED + plugin.currencySymbol + " to " + ChatColor.GOLD + pReceiver.getName());
                            }
                        } else {
                            sender.sendMessage(plugin.chatPrefix+ChatColor.AQUA + "Please confirm you want to send " + ChatColor.YELLOW +amount + " " + ChatColor.AQUA + plugin.currencySymbol + " to " + ChatColor.GOLD + pReceiver.getName());
                        }

                    } else {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Amount must be greater than " + ChatColor.YELLOW + "0" + ChatColor.RED + plugin.currencySymbol + ".");
                        return true;
                    }
                }
            } else {
                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:send <player> <amount> confirm");
            }
        } else {
            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
