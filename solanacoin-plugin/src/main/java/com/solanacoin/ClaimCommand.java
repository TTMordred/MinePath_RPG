package com.solanacoin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.p2p.solanaj.core.PublicKey;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class ClaimCommand implements CommandExecutor {
    private final MinePathCoinPlugin plugin;
    public ClaimCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // expect exactly 1 arg: the amount to mint/claim
        if (!(sender instanceof Player) || args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /tokenclaim <amount>");
            return true;
        }

        Player p = (Player) sender;
        String sessionId = plugin.db.getSessionPublicKey(p.getUniqueId().toString());
        if (sessionId == null) {
            p.sendMessage(ChatColor.RED + "You haven't authorized yet. Use /authorize first.");
            return true;
        }

        String amount = args[0];
        try {
            // build the POST to /session/claim
            URL url = new URL("http://localhost:3000/session/claim");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");

            // your plugin already knows the wallet → ATA mapping
            String userWallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
            PublicKey walletPubkey = new PublicKey(userWallet);
            String userATA = plugin.getAssociatedTokenAddress(
                walletPubkey,
                plugin.tokenMintAddress
            ).toBase58();

            // JSON body: sessionId, userATA, amount
            String body = String.format(
                "{\"sessionId\":\"%s\",\"userATA\":\"%s\",\"amount\":%s}",
                sessionId, userATA, amount
            );
            con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

            // read and handle response
            int code = con.getResponseCode();
            BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                  code == 200 ? con.getInputStream() : con.getErrorStream(),
                  StandardCharsets.UTF_8
                )
            );
            String resp = reader.lines().collect(Collectors.joining());
            reader.close();
            if (code == 402) {
                p.sendMessage(ChatColor.RED + "Session needs funding! Please re-authorize to add SOL.");
            }
            if (code == 200) {
                JSONObject json = (JSONObject) new JSONParser().parse(resp);
                String txid = (String) json.get("txid");
                p.sendMessage(ChatColor.GREEN + "Claim successful! TXID: " + txid);
            } else {
                p.sendMessage(ChatColor.RED + "Claim failed (HTTP " + code + "): " + resp);
            }

        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + "Error during claim: " + e.getMessage());
        }

        return true;
    }
    public String claimTokens(Player p, String amount) throws Exception {
        // 1) ensure session is authorized
        String sessionId = plugin.db.getSessionPublicKey(p.getUniqueId().toString());
        if (sessionId == null) {
            throw new IllegalStateException("Player not authorized. Use /tokenauthorize first.");
        }

        // 2) derive user ATA
        String userWallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
        PublicKey walletPubkey = new PublicKey(userWallet);
        String userATA = plugin.getAssociatedTokenAddress(
            walletPubkey,
            plugin.tokenMintAddress
        ).toBase58();

        // 3) build HTTP POST to your backend
        URL url = new URL("http://localhost:3000/session/claim");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        String body = String.format(
            "{\"sessionId\":\"%s\",\"userATA\":\"%s\",\"amount\":%s}",
            sessionId, userATA, amount
        );
        con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        // 4) read the response
        int code = con.getResponseCode();
        BufferedReader reader = new BufferedReader(
            new InputStreamReader(
                code == 200 ? con.getInputStream() : con.getErrorStream(),
                StandardCharsets.UTF_8
            )
        );
        String resp = reader.lines().collect(Collectors.joining());
        reader.close();

        // 5) handle errors

        if (code != 200) {
            if (code == 402) {
                throw new IllegalStateException("Session needs funding! Please add SOL.");
            }
            throw new RuntimeException("Claim failed (HTTP " + code + "): " + resp);
        }

        // 6) parse the txid
        JSONObject json = (JSONObject) new JSONParser().parse(resp);
        return (String) json.get("txid");
    }
}
