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
            p.sendMessage(ChatColor.RED + "You have already authorized. Here your sessionID: "+ sessionPub + ". Please confirm with the link above if you havent. If you already confirm then you can ignore this message.");
            plugin.sendCopyableTextToPlayer(p,null, sessionPub, null);
            return true;
        }
        try {
            URL url = new URL("http://localhost:3000/session/new");
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
            
    
            // Lưu sessionId cho player
            plugin.db.addSession(p.getUniqueId().toString(), sessionId);
            // Gửi link để user ký (có thể qua chat)
            String url_send = String.format(
                  "http://localhost:3000/approve?sessionId=%s&userATA=%s",
            sessionId,
                URLEncoder.encode(userATA, "UTF-8")
                );
            plugin.sendURLToPlayer(p,"[Click here to Authorize transfer]", url_send, TELLRAWCOLOR.green
            );
            }else{
                p.sendMessage(ChatColor.RED + "Server trả về HTTP " + con.getResponseCode());
                // Đọc và hiển thị luôn body để debug
                String err = new BufferedReader(new InputStreamReader(con.getErrorStream()))
                    .lines().collect(Collectors.joining("\n"));
                p.sendMessage(ChatColor.RED + err);
                plugin.getServer().getConsoleSender().sendMessage(ChatColor.RED + err);
            }
        } catch (Exception e) {
            p.sendMessage(ChatColor.RED + "Lỗi khi tạo session: " + e.getMessage());
        }
        return true;
    }
  }
  
