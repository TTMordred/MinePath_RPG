package com.solanacoin;



import static com.solanacoin.MinePathCoinPlugin.NUMBER_FORMAT;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;


public class BalanceCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public BalanceCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.balance")
        ) {
            if (args.length == 0) {
                if (sender instanceof Player) {
                    Player sPlayer = (Player) sender;
                    sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Your balance is " + ChatColor.YELLOW + NUMBER_FORMAT.format(plugin.db.getBalanceOfPlayer(sPlayer.getUniqueId())) + " " + ChatColor.GREEN + plugin.currencySymbol);
                } else {
                    try {
                        TokenAmountInfo balanceInfo = plugin.rpcClient.getApi().getTokenAccountBalance(plugin.associatedTokenAddress, null);
                        sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Server Balance is " + ChatColor.YELLOW + NUMBER_FORMAT.format(balanceInfo.getUiAmountString()) + " " + ChatColor.GREEN + plugin.currencySymbol);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else {
                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:balance");
            }
        } else {
            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
