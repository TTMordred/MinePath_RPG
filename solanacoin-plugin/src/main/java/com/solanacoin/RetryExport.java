package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.p2p.solanaj.core.Transaction;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.ConfirmedTransaction;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;
import java.math.BigDecimal;

public class RetryExport implements Runnable {

    MinePathCoinPlugin plugin;
    Transaction tx;
    String signature;
    Player sender;
    double amount;
    int maxRetry;
    int retryTimeoutInMinutes;
    int confirmTimeoutInMinutes;
    String recentBlockhash;
    Thread thread;


    public RetryExport(MinePathCoinPlugin plugin, Player sender, double amount, Transaction tx, String signature) {
        this(plugin, sender, amount, tx, signature, 1, 60, 2);
    }

    public RetryExport(MinePathCoinPlugin plugin, Player sender, double amount, Transaction tx, String signature, int maxRetry, int retryTimeoutInMinutes, int confirmTimeoutInMinutes) {
        this.plugin = plugin;
        this.tx = tx;
        this.sender = sender;
        this.amount = amount;
        this.signature = signature;
        this.maxRetry = maxRetry;
        this.retryTimeoutInMinutes = retryTimeoutInMinutes;
        this.confirmTimeoutInMinutes = confirmTimeoutInMinutes;
        try {
            String recentBlockhash = plugin.rpcClient.getApi().getLatestBlockhash().getValue().getBlockhash();
            tx.setRecentBlockHash(recentBlockhash);
            this.recentBlockhash = recentBlockhash;
        } catch (RpcException e) {
            e.printStackTrace();
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Failed to fetch blockhash.");
        }
    
        this.thread = (new Thread(this));
        this.thread.start();
    }

    @Override
    public void run() {
        boolean flag = true;
        int retryAttempt = 1;
        int confirmCheckCount = 1;
        int errorCount = 1;
        while(flag){
            if (errorCount > 25) {
                flag = false;
            }
            if (retryAttempt > this.maxRetry) {
                if (confirmCheckCount > this.confirmTimeoutInMinutes) {
                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Transaction failed to confirm after " + (confirmCheckCount-1) + " minutes. " + this.amount + " of " + plugin.currencySymbol + " will be returned once the blockhash is invalidated.");
                    try {
                        Thread.sleep(60 * 1000 * 5);
                        if(!this.plugin.rpcClient.getApi().isBlockhashValid(this.recentBlockhash)) {
                            ConfirmedTransaction confirmedTX = this.plugin.rpcClient.getApi().getTransaction(this.signature);
                            if (confirmedTX == null) {
                                this.sender.sendMessage(plugin.chatPrefix+ChatColor.YELLOW + "Your transaction was never confirmed (dropped). Solana could be experiencing high load.");
                                boolean returned = this.plugin.db.addBalanceToPlayer(sender.getUniqueId(), this.amount);
                                if (returned) {
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "" + this.amount + " " + plugin.currencySymbol + " has been returned to you.");
                                } else {
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "There was an error in returning " + this.amount + " " + plugin.currencySymbol + " to you. Please contact server admin.");
                                }
                            } else {
                                if (confirmedTX.getMeta().getErr() != null) {
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.YELLOW + "Your transaction was confirmed, but it failed.");
                                    boolean returned = this.plugin.db.addBalanceToPlayer(sender.getUniqueId(), this.amount);
                                    if (returned) {
                                        this.sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "" + this.amount + " " + plugin.currencySymbol + " has been returned to you.");
                                    } else {
                                        this.sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "There was an error in returning " + this.amount + " " + plugin.currencySymbol + " to you. Please contact server admin.");
                                    }
                                } else {
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Your transaction was confirmed, Solana could be experiencing high load.");
                                }
                            }
                            flag = false;
                        }
                        confirmCheckCount += 5;
                    } catch (InterruptedException | RpcException e) {
                        errorCount++;
                        e.printStackTrace();
                    }
                } else {
                    try {
                        Thread.sleep(60 * 1000);
                    } catch (InterruptedException e) {
                        errorCount++;
                        e.printStackTrace();
                    }
                    confirmCheckCount++;
                }
            } else {
                try {
                    Thread.sleep(this.retryTimeoutInMinutes * 1000L);
                    ConfirmedTransaction confirmedTX = this.plugin.rpcClient.getApi().getTransaction(this.signature);
                    if (confirmedTX != null) {
                        confirmCheckCount = 1;
                        if (confirmedTX.getMeta().getErr() != null) {
                            try {
                                TokenAmountInfo tokenInfo = plugin.rpcClient.getApi().getTokenAccountBalance(plugin.associatedTokenAddress);
                                BigDecimal serverBalance = new BigDecimal(tokenInfo.getUiAmountString());                                
                                if (serverBalance.subtract(new BigDecimal(amount)).doubleValue() < 0) {
                                    sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "Your transaction failed, and the server no longer has enough supply of " + plugin.currencySymbol + ".");
                                    flag = false;
                                } else {
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.YELLOW + "Transaction failed, retrying.");
                                    String signature = this.plugin.rpcClient.getApi().sendTransaction(this.tx, this.plugin.signer);
                                    this.signature = signature;
                                    this.sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Resent Transaction!");
                                    this.plugin.sendURLToPlayer(this.sender, "Check the Transaction Status", "https://solscan.io/tx/"+signature, MinePathCoinPlugin.TELLRAWCOLOR.yellow);
                                    retryAttempt++;
                                }
                            } catch (Exception e) {
                                errorCount++;
                                e.printStackTrace();
                            }
                        } else {
                            this.sender.sendMessage(plugin.chatPrefix+ChatColor.GREEN + "Transaction success!");
                            flag = false;
                        }
                    } else {
                        this.sender.sendMessage(plugin.chatPrefix+ChatColor.GRAY + "("+confirmCheckCount+ ((confirmCheckCount>1) ? " minutes" : " minute") +") Transaction Status: Unconfirmed.");
                        confirmCheckCount++;
                    }
                } catch (InterruptedException | RpcException e) {
                    errorCount++;
                    e.printStackTrace();
                }
            }
        }
    }
}
