package com.solanacoin;


import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;

import static com.solanacoin.MinePathCoinPlugin.NUMBER_FORMAT;

import java.math.BigDecimal;

public class ServerBalanceCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public ServerBalanceCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (plugin.hasPermission(sender, "minepath.serverbalance")) {
            try {
                TokenAmountInfo tokenInfo = plugin.rpcClient.getApi().getTokenAccountBalance(plugin.associatedTokenAddress);
                BigDecimal serverBalance = new BigDecimal(tokenInfo.getUiAmountString());

                String balanceMsg = plugin.chatPrefix + ChatColor.AQUA + "Server Balance is " +
                        ChatColor.YELLOW + NUMBER_FORMAT.format(serverBalance) + " " +
                        ChatColor.AQUA + plugin.currencySymbol;

                if (sender instanceof Player pSender) {
                    if (plugin.shouldRateLimit(pSender)) {
                        sender.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Command failed, rate limited.");
                        return true;
                    } else {
                        sender.sendMessage(balanceMsg);
                        boolean updated = this.plugin.db.setLastRequestTimestamp(pSender.getUniqueId(), plugin.getNow().toString());
                        if (!updated) {
                            plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix + "Failed to update last timestamp for " + pSender.getUniqueId());
                        }
                    }
                } else {
                    sender.sendMessage(balanceMsg);
                }
            } catch (Exception e) {
                sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Failed to fetch server balance.");
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
