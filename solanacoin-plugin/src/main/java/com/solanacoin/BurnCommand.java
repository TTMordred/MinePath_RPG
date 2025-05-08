package com.solanacoin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.p2p.solanaj.core.*;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BurnCommand implements CommandExecutor {
    private final MinePathCoinPlugin plugin;
    public BurnCommand(MinePathCoinPlugin plugin) { this.plugin = plugin; }
  
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
      if (!(sender instanceof Player) || args.length != 1) return false;
      Player p = (Player) sender;
      String sessionId = plugin.db.getSessionPublicKey(p.getUniqueId().toString());
      if (sessionId == null) {
        p.sendMessage(ChatColor.RED + "You havent authorize. Please use command /authorize to procedd.");
        return true;
      }
  
      String amount = args[0];
      try {
        URL url = new URL("http://localhost:3000/session/burn");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");
        String userWallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
        PublicKey pubkey = new PublicKey(userWallet);
        String userATA = plugin.getAssociatedTokenAddress(pubkey, plugin.tokenMintAddress).toBase58();
        String body = String.format(
          "{\"sessionId\":\"%s\",\"userATA\":\"%s\",\"amount\":%s}",
          sessionId, userATA, amount
        );
        con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
  
        if (con.getResponseCode() == 200) {
            StringBuilder respBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(con.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                respBuilder.append(line);
                }
            }
            String resp = respBuilder.toString();

            // 4) Parse JSON để lấy txid
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(resp);
            String txid = (String) json.get("txid");

            // 5) Thông báo thành công
            sender.sendMessage(ChatColor.GREEN + "Burn success TXID: " + txid);
            return true;
        } else {
          p.sendMessage(ChatColor.RED + "Burn failed, code: " + con.getResponseCode());
          if(con.getResponseCode() == 402){
            p.sendMessage(ChatColor.RED + "Session need funding.");
            return true;
          }
        }
      } catch (Exception e) {
        p.sendMessage(ChatColor.RED + "Err Burn: " + e.getMessage());
      }
      return true;
    }
    public String burnTokens(Player p, String amount)throws Exception{
      String sessionId = plugin.db.getSessionPublicKey(p.getUniqueId().toString());
        if (sessionId == null) {
            throw new IllegalStateException("Player not authorized. Use /tokenauthorize first.");
        }

        // 2) Build their ATA
        String userWallet = plugin.fetchWalletAddress(p.getUniqueId().toString());
        PublicKey pubkey = new PublicKey(userWallet);
        String userATA = plugin.getAssociatedTokenAddress(pubkey, plugin.tokenMintAddress).toBase58();

        // 3) Prepare the HTTP POST
        URL url = new URL("http://localhost:3000/session/burn");
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        con.setRequestProperty("Content-Type", "application/json");

        // 4) Write JSON body
        String body = String.format(
            "{\"sessionId\":\"%s\",\"userATA\":\"%s\",\"amount\":%s}",
            sessionId, userATA, amount
        );
        con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));

        // 5) Read response
        int code = con.getResponseCode();
        BufferedReader reader = new BufferedReader(new InputStreamReader(
            code == 200 ? con.getInputStream() : con.getErrorStream(),
            StandardCharsets.UTF_8
        ));
        String resp = reader.lines().collect(Collectors.joining());
        reader.close();

        if (code != 200) {
          if (code == 402) {
            throw new IllegalStateException("Session needs funding! Please add SOL.");
          }
          if(code == 500){
            throw new IllegalStateException("Exceed the balance.");
          }
            // propagate the server error
          throw new RuntimeException("Burn failed (HTTP " + code + "): " + resp);
        }

        // 6) Parse JSON for txid
        JSONObject json = (JSONObject) new JSONParser().parse(resp);
        return (String) json.get("txid");
    }
  }
  
