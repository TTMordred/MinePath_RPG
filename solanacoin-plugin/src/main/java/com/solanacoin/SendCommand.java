package com.solanacoin;


import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;

import com.solanacoin.MinePathCoinPlugin.TELLRAWCOLOR;

import org.p2p.solanaj.core.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.net.HttpURLConnection;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class SendCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;
    private static final String SEND_TOKEN_URL = "http://localhost:3000/confirm";  // ← point this at your TS endpoint

    public SendCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }


    private String requestTicket(
    String senderAta,
    String receiverWallet,
    double amount,
    String mintAddress,
    int decimals
) {
    String payload = String.format(
      "{\"senderATA\":\"%s\",\"receiverWallet\":\"%s\",\"amount\":%f,"
    + "\"mintAddress\":\"%s\",\"decimals\":%d}",
      senderAta, receiverWallet, amount, mintAddress, decimals
    );
    plugin.getLogger().info("📨 requestTicket payload: " + payload);

    try {
        URL url = new URL("http://localhost:3000/confirm");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload.getBytes(StandardCharsets.UTF_8));
        }

        int code = conn.getResponseCode();
        plugin.getLogger().info("requestTicket HTTP " + code);

        if (code != HttpURLConnection.HTTP_OK) {
            plugin.getLogger().warning("  Non-OK response");
            return null;
        }

        // Read response body: {"success":true,"ticket":"UUID…"}
        String body;
        try (BufferedReader in = new BufferedReader(
               new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            body = in.lines().collect(Collectors.joining());
        }
        plugin.getLogger().info("response body: " + body);

        // Extract the ticket with a simple regex
        Matcher m = Pattern.compile("\"ticket\":\"([^\"]+)\"").matcher(body);
        if (m.find()) return m.group(1);
        else {
          plugin.getLogger().warning("  No ticket in JSON");
          return null;
        }

    } catch (IOException e) {
        plugin.getLogger().severe("RequestTicket error: " + e.getMessage());
        return null;
    }
}
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (
                plugin.hasPermission(sender, "minepath.send")
        ) {
            if (args.length >= 2) {
                if (sender instanceof Player pSender) {
                    if(!plugin.db.playerExistsInWalletTable(pSender.getUniqueId())) {
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You have not linked a wallet. Please link a wallet first.");
                        return true;
                    }
                    // Get sender wallet
                    String wal = plugin.fetchWalletAddress(pSender.getUniqueId().toString());
                    PublicKey UserWallet = new PublicKey(wal);
                    // Get sender ATA
                    PublicKey UserATA = plugin.getAssociatedTokenAddress(UserWallet, plugin.tokenMintAddress);
                    TokenAmountInfo balance_info; 
                    try{
                        balance_info = plugin.rpcClient.getApi().getTokenAccountBalance(UserATA, null);
                    }catch(RpcException e){
                        sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You don't have an associated token account. Please create one first.");
                        return true;
                    }
                    // Get sender balance
                    Double balance = balance_info.getUiAmount();
                    
                    OfflinePlayer pReceiver = plugin.getServer().getOfflinePlayer(args[0]);

                    if (!plugin.db.playerExistsInWalletTable(pReceiver.getUniqueId())) {
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
                        if (balance >= amount) {
                            if (args.length == 2) {
                                sender.sendMessage(plugin.chatPrefix+ChatColor.AQUA + "Please confirm you want to send " + amount + " " + plugin.currencySymbol + " to " + pReceiver.getName());
                                sender.sendMessage(plugin.chatPrefix+"/MINEPATH:send <player> <amount> confirm");
                                return true;
                            }
                            if (args[2].equalsIgnoreCase("confirm")) {

                                String toWalletAddress = plugin.fetchWalletAddress(pReceiver.getUniqueId().toString());
                                PublicKey toWallet = new PublicKey(toWalletAddress);
                                String senderAtaStr   = UserATA.toBase58();
                                String receiverWalletStr = toWallet.toBase58();
                                String ticket = requestTicket(senderAtaStr, receiverWalletStr, amount, plugin.tokenMintAddress.toBase58(), plugin.tokenDecimals);
                                if (ticket == null) {
                                sender.sendMessage(plugin.chatPrefix
                                    + ChatColor.RED + "Failed to request confirmation ticket.");
                                return true;
                                }
                                String url = SEND_TOKEN_URL+"?ticket="
                                        + URLEncoder.encode(ticket, StandardCharsets.UTF_8);
                                plugin.sendURLToPlayer((Player) sender,"[Click here to confirm transfer]", url, TELLRAWCOLOR.green
                                );
                                return true;
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
                }else {
                    sender.sendMessage(plugin.chatPrefix+ChatColor.RED + "You must be a player to use this command.");
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
