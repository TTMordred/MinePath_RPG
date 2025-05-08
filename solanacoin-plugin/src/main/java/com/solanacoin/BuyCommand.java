package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.solanacoin.MinePathCoinPlugin.TELLRAWCOLOR;

import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;
import org.p2p.solanaj.core.PublicKey;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.stream.Collectors;
import java.io.IOException;

public class BuyCommand implements CommandExecutor {

    private final MinePathCoinPlugin plugin;
    private static final String BUY_URL = "http://localhost:3000/buy";
    private static final double RATE = 200.0; // 200 coins per item
    public BuyCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Requests a “buy” ticket from your TS backend.
     * Payload: { senderATA, amount, mintAddress, decimals }
     * Returns the ticket UUID or null on failure.
     */
    private String requestBuyTicket(String senderAta, double amount, String mintAddress, int decimals, int item_amount) {
        String payload = String.format(
            "{\"senderATA\":\"%s\",\"amount\":\"%f\",\"mintAddress\":\"%s\",\"decimals\":\"%d\",\"item_amount\":\"%d\"}",
            senderAta, amount, mintAddress, decimals, item_amount
        );
        plugin.getLogger().info("requestBuyTicket payload: " + payload);

        try {
            URL url = new URL(BUY_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type","application/json");

            try (OutputStream os = conn.getOutputStream()) {
                os.write(payload.getBytes(StandardCharsets.UTF_8));
            }

            int code = conn.getResponseCode();
            plugin.getLogger().info("requestBuyTicket HTTP " + code);
            if (code != HttpURLConnection.HTTP_OK) {
                plugin.getLogger().warning("  Non-OK response");
                return null;
            }

            String body;
            try (BufferedReader in = new BufferedReader(
                   new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
                body = in.lines().collect(Collectors.joining());
            }
            plugin.getLogger().info("response body: " + body);

            Matcher m = Pattern.compile("\"ticket\":\"([^\"]+)\"").matcher(body);
            if (m.find()) return m.group(1);

            plugin.getLogger().warning("  No ticket in JSON");
            return null;

        } catch (IOException e) {
            plugin.getLogger().severe("requestBuyTicket error: " + e.getMessage());
            return null;
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!plugin.hasPermission(sender, "minepath.buy")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You are not permitted to use that command.");
            return true;
        }

        if (!(sender instanceof Player pSender)) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Only players can buy item!");
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage(plugin.chatPrefix + "/MINEPATH:buy <amount> confirm");
            return true;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                "Invalid amount. Usage: /MINEPATH:buy <amount> confirm");
            return true;
        }
        if (amount <= 0) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                "Amount must be greater than zero.");
            return true;
        }

        // if no "confirm" flag yet, prompt user
        if (args.length == 1 || !args[1].equalsIgnoreCase("confirm")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.AQUA +
                "Please confirm purchase of " + amount + " item for " +
                 amount * RATE + plugin.currencySymbol + ":");
            sender.sendMessage(plugin.chatPrefix + "/MINEPATH:buy " + amount + " confirm");
            return true;
        }

        // On "confirm", build & send the ticket link
        try {
            // fetch the user's ATA & balance just like SendCommand
            String wal = plugin.fetchWalletAddress(pSender.getUniqueId().toString());
            PublicKey userWallet = new PublicKey(wal);
            PublicKey userATA = plugin.getAssociatedTokenAddress(userWallet, plugin.tokenMintAddress);
            TokenAmountInfo bal = plugin.rpcClient.getApi()
                .getTokenAccountBalance(userATA, null);
            if (bal.getUiAmount() < amount * RATE) {
                sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                    "You do not have enough " + plugin.currencySymbol + ".");
                return true;
            }

            String ticket = requestBuyTicket(
                userATA.toBase58(),
                amount * RATE,
                plugin.tokenMintAddress.toBase58(),
                plugin.tokenDecimals,
                amount
            );
            if (ticket == null) {
                sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                    "Failed to request purchase ticket. Try again later.");
                return true;
            }

            String link = BUY_URL + "?ticket=" +
                URLEncoder.encode(ticket, StandardCharsets.UTF_8);
            plugin.sendURLToPlayer(pSender,
                "[Click here to complete your purchase]", link,
                TELLRAWCOLOR.green
            );

        } catch (RpcException e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                "Error checking balance: " + e.getMessage());
        }

        return true;
    }
}
