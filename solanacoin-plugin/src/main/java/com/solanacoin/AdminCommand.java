package com.solanacoin;

import org.p2p.solanaj.core.*;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;

public class AdminCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public AdminCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.admin")
        ) {
            if (args.length >= 1) {
                switch(args[0]) {
                    case "balance":
                        if (args.length == 1) {
                            try {
                                if (!(sender instanceof Player pSender)) {
                                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Export not available to non-players.");
                                    return true;
                                }
                                String wallet = plugin.fetchWalletAddress(pSender.getUniqueId().toString());
                                PublicKey Key;
                                try {
                                    Key = new PublicKey(wallet);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Your wallet address is invalid. Please link a valid wallet.");
                                    return true;
                                }
                                PublicKey ata = plugin.getAssociatedTokenAddress(Key, plugin.tokenMintAddress);
                                TokenAmountInfo balanceInfo = plugin.rpcClient.getApi().getTokenAccountBalance(ata, null);
                                sender.sendMessage(plugin.chatPrefix+"Your Balance is " + balanceInfo.getUiAmountString() + " " + plugin.currencySymbol);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (args.length == 2) {
                            OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[1]);
                            if (p == null) {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Player " + args[1] + " not found.");
                                break;
                            }
                            if(plugin.db.playerExistsInWalletTable(p.getUniqueId())){
                                String wallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
                                PublicKey Key;
                                try {
                                    Key = new PublicKey(wallet);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Player " + args[1] + " has an invalid wallet address. Please link a valid wallet.");
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
                                    sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Player " + args[1] + " does not have ATA created.");
                                    break;
                                }
                                sender.sendMessage(plugin.chatPrefix+"" + args[1] + "'s balance is " + balanceInfo.getUiAmountString() + " " + plugin.currencySymbol);
                            }else{
                                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Player " + args[1] + " not linked wallet.");
                                break;
                            }
                            
                        } else {
                            sender.sendMessage(plugin.chatPrefix+"/MINEPATH:admin balance (player)");
                        }
                        break;
                    case "add":
                        if (args.length == 3) {
                            OfflinePlayer p = plugin.getServer().getOfflinePlayer(args[1]);

                            if (!plugin.db.playerExistsInWalletTable(p.getUniqueId())) {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Player " + args[1] + " not found in db.");
                                break;
                            }

                            double amount = Double.parseDouble(args[2]);
                            if (amount > 0) {
                                String wallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
                                PublicKey toKey;
                                try {
                                    toKey = new PublicKey(wallet);
                                } catch (IllegalArgumentException e) {
                                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Player " + args[1] + " has an invalid wallet address. Please link a valid wallet.");
                                    return true;
                                }
                                PublicKey toTokenPublicKey = plugin.fetchAssociatedTokenAccount(toKey, plugin.tokenMintAddress);
                                Transaction tx = new Transaction();
                                if (toTokenPublicKey == null) {
                                    // Create the associated token account if it doesn't exist
                                    plugin.getLogger().info("Starting creating token account for " + toKey.toBase58());
                                    
                                    plugin.addCreateAtaInstruction_MINE(toKey, tx);
                                    toTokenPublicKey = plugin.getAssociatedTokenAddress(toKey, plugin.tokenMintAddress);
                                }
                                plugin.getLogger().info("To Key: " + toKey.toBase58() + " Token Account: " + toTokenPublicKey.toBase58());
                                
                                try {
                                    plugin.getLogger().info("TransferAmount (lamports) = " +
                                    (long)(amount * Math.pow(10, plugin.tokenDecimals)));
                                    // Use standard SPL token transfer instruction
                                    TransactionInstruction instruction = new TransactionInstruction(
                                        new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), // Standard Token Program
                                        List.of(
                                            new AccountMeta(plugin.tokenMintAddress, false, true),  // from token account (writable)
                                            new AccountMeta(toTokenPublicKey, false, true),    // to token account (writable)
                                            new AccountMeta(plugin.signer.getPublicKey(), true, false)              // owner/signer
                                        ),
                                        plugin.createInstructionData(amount,(byte) 7)
                                    );

                                    tx.addInstruction(instruction);
                                    
                                    // IMPORTANT: Explicitly fetch the latest blockhash using getLatestBlockhash 
                                    // (instead of getRecentBlockhash, which is deprecated on Devnet)
                                    String latestBlockhash = plugin.rpcClient.getApi().getLatestBlockhash().getValue().getBlockhash();
                                    tx.setRecentBlockHash(latestBlockhash);
                                    plugin.getLogger().info("Latest Blockhash set: " + latestBlockhash);
                                    
                                    // Send the transaction using the built-in API
                                    List<Account> signers = new ArrayList<>();
                                    signers.add(plugin.signer);
                                    
                                    String signature = plugin.rpcClient.getApi().sendTransaction(tx, signers, latestBlockhash);
                                    plugin.getLogger().info("Transaction sent with signature: " + signature);
                                    sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Transaction sent!");
                                }catch (RpcException e) {
                                    // Handle Solana RPC errors
                                    plugin.getLogger().severe("RPC Error: " + e.getMessage());
                                    plugin.getLogger().info("Using RPC endpoint: " + plugin.rpcClient.getEndpoint());
                                    // If the error indicates that the token account doesn't exist, notify the user accordingly
                                    if (e.getMessage().contains("account not found") || 
                                        e.getMessage().contains("invalid account owner") || 
                                        e.getMessage().contains("could not find account")) {                       
                                    } else {
                                        // Generic error message
                                        sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error while sending transaction. Please try again or contact admin.");
                                    }
                                    return true;
                                }
                                sender.sendMessage(plugin.chatPrefix+"Added " + amount + plugin.currencySymbol+ " to " + p.getName() + ".");
                                if (p.isOnline()) {
                                    plugin.getServer().getPlayer(p.getUniqueId()).sendMessage(plugin.chatPrefix+ChatColor.GOLD + "An admin sent you " + amount + " " + plugin.currencySymbol + "!");
                                }
                            } else {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Amount must be greater than zero " + plugin.currencySymbol + ".");
                            }
                        } else {
                            sender.sendMessage(plugin.chatPrefix+"/MINEPATH:admin add <player> <amount>");
                        }
                        break;
                    case "destroydb":
                        if (args.length == 2) {
                            if (args[1].equalsIgnoreCase("confirm")) {
                                plugin.db.deleteBalanceTable();
                                sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Database destroyed.");
                            }
                        } else {
                            sender.sendMessage(plugin.chatPrefix+ChatColor.YELLOW + "WARNING this will destroy all data in the database, please confirm.");
                            sender.sendMessage(plugin.chatPrefix+"/MINEPATH:admin destroydb confirm");
                        }
                        break;
                    case "help":
                        sender.sendMessage(plugin.chatPrefix+"/MINEPATH:admin [balance|add|destroydb|reload]");
                        break;
                    case "reload":
                        plugin.reloadConfig();
                        sender.sendMessage(plugin.chatPrefix+"Config Reloaded");
                        break;
                }
            } else {
                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:admin [balance|add|destroydb|reload]");
            }
        } else {
            sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You are not permitted to use that command.");
        }
        return true;
    }
}
