package com.solanacoin;



import static com.solanacoin.MinePathCoinPlugin.NUMBER_FORMAT;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.core.*;
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
        )   {
                if (sender instanceof Player sPlayer) {
                    if(plugin.db.playerExistsInWalletTable(sPlayer.getUniqueId())){
                        String wallet = plugin.fetchWalletAddress(sPlayer.getUniqueId().toString());
                        PublicKey Key;
                        try {
                            Key = new PublicKey(wallet);
                        } catch (IllegalArgumentException e) {
                            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You has an invalid wallet address. Please link a valid wallet.");
                            return true;
                        }
                        PublicKey ata = plugin.getAssociatedTokenAddress(Key, plugin.tokenMintAddress);
                        TokenAmountInfo balanceInfo = null;
                        try {
                            balanceInfo = plugin.rpcClient.getApi().getTokenAccountBalance(ata, null);
                        } catch (RpcException e) {
                            e.printStackTrace();
                        }
                        if(balanceInfo == null) {
                            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You does not have ATA created.");
                            return true;
                        }
                        sender.sendMessage(plugin.chatPrefix+"Your balance is " + balanceInfo.getUiAmountString() + " " + plugin.currencySymbol);
                    }else{
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You have not linked wallet.");
                        return true;
                    }
                }
            } else {
                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
            }
        return true;
    }
    public String getBalance(Player player) throws Exception {
        // 1) check linked
        if (!plugin.db.playerExistsInWalletTable(player.getUniqueId())) {
            throw new IllegalStateException("You have not linked a wallet.");
        }

        // 2) fetch and validate their on-chain wallet
        String wallet = plugin.fetchWalletAddress(player.getUniqueId().toString());
        PublicKey walletKey;
        try {
            walletKey = new PublicKey(wallet);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Invalid wallet address. Please link a valid one.");
        }

        // 3) derive ATA and fetch balance
        PublicKey ata = plugin.getAssociatedTokenAddress(walletKey, plugin.tokenMintAddress);
        TokenAmountInfo balanceInfo = plugin.rpcClient
            .getApi()
            .getTokenAccountBalance(ata, null);
        if (balanceInfo == null) {
            throw new IllegalStateException("No associated token account found. Please create one.");
        }

        // 4) format and return
        // e.g. "1234.567890123"
        return balanceInfo.getUiAmountString();
    }
}
