package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.p2p.solanaj.rpc.RpcException;



public class LbhCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public LbhCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.lbh")
        ) {
            try {
                sender.sendMessage(plugin.chatPrefix+ChatColor.GOLD + "Latest Blockhash " + plugin.rpcClient.getApi().getLatestBlockhash());
            } catch (RpcException e) {
                e.printStackTrace();
            }
        } else {
            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
