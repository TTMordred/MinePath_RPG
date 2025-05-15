package com.solanacoin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.p2p.solanaj.core.PublicKey;

import com.solanacoin.MinePathCoinPlugin.TELLRAWCOLOR;

import java.io.BufferedReader;
import java.io.InputStreamReader;


public class AuthorizeCommand implements CommandExecutor {
    private final MinePathCoinPlugin plugin;
    public AuthorizeCommand(MinePathCoinPlugin plugin) { this.plugin = plugin; }
  
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) return false;
        Player p = (Player) sender;
        String userPubkey = plugin.fetchWalletAddress(p.getUniqueId().toString());
        String userATA    = plugin.getAssociatedTokenAddress(
            new PublicKey(userPubkey), plugin.tokenMintAddress).toBase58();
        String sessionPub = plugin.db.getSessionPublicKey(p.getUniqueId().toString());
        if(sessionPub != null){
            plugin.sendCopyableTextToPlayer(p,"You have already authorized. Here your sessionID: "+ sessionPub, sessionPub, TELLRAWCOLOR.green);
            return true;
        }
        try {
            URL url = new URL(plugin.linkweb+"/session/new");
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setRequestProperty("Content-Type", "application/json");
            String body = String.format(
            "{\"userPubkey\":\"%s\",\"userATA\":\"%s\"}", userPubkey, userATA
            );
            con.getOutputStream().write(body.getBytes(StandardCharsets.UTF_8));
    
            if (con.getResponseCode() == 200) {
            String resp = new BufferedReader(
                new InputStreamReader(con.getInputStream())
            ).lines().collect(Collectors.joining());
            JSONParser parser = new JSONParser();
            JSONObject json = (JSONObject) parser.parse(resp);
            String sessionId = (String) json.get("sessionId");
            plugin.getServer().getConsoleSender().sendMessage(
                ChatColor.GREEN + "Session ID: " + sessionId
            );
            
    
            // Store sessionId in the database
            plugin.db.addSession(p.getUniqueId().toString(), sessionId);
            // Gửi link để user ký (có thể qua chat)
            String url_send = String.format(
                  plugin.linkweb+"/approve?sessionId=%s&userATA=%s",
            sessionId,
                URLEncoder.encode(userATA, "UTF-8")
                );
            plugin.sendURLToPlayer(p,"[Click here to Authorize transfer]", url_send, TELLRAWCOLOR.green
            );
            }else{
                p.sendMessage(ChatColor.RED + "Server return HTTP " + con.getResponseCode());
                // Read and display the error message
                String err = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
                p.sendMessage(ChatColor.RED + err);
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + err);
            }
        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + "Error creating session: " + e.getMessage());
        }
        return true;
    }
  }
  
