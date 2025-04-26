package com.example.lootbox;

import net.milkbowl.vault.economy.EconomyResponse;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class LootBoxCommand implements CommandExecutor {
    private final LootBoxPlugin plugin;
    public LootBoxCommand(LootBoxPlugin plugin) { this.plugin = plugin; }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player)) return false;
        Player p = (Player) s;
        if (args.length != 2) {
            p.sendMessage(ChatColor.RED + "Usage: /lootbox <type> <amount>");
            return true;
        }
        String type = args[0].toLowerCase();
        int amount;
        try { amount = Integer.parseInt(args[1]); }
        catch (NumberFormatException e) {
            p.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }

        double price = plugin.getConfig().getDouble("prices." + type, -1);
        if (price < 0) {
            p.sendMessage(ChatColor.RED + "Unknown box type.");
            return true;
        }
        double cost = price * amount;
        String uuid = p.getUniqueId().toString();
        Connection conn = plugin.getConnection();
        boolean dbBad = false;
        try {
            if (conn == null || conn.isClosed()) {
                dbBad = true;
            }
        } catch (SQLException e) {
            dbBad = true;
            plugin.getLogger().severe("DB check failed: " + e.getMessage());
        }
        
        if (dbBad) {
            p.sendMessage(ChatColor.RED + "Database unavailable—try again later.");
            return true;
        }
        try{
            PreparedStatement select = conn.prepareStatement(
                "SELECT balance FROM balance WHERE uuid = ?"
            );
            select.setString(1, uuid);
            ResultSet rs = select.executeQuery();
            double balance;
            if (rs.next()) {
                balance = rs.getDouble("balance");
            } else {
                // No row for this user
                p.sendMessage(ChatColor.RED + "Account not found.");
                rs.close();
                select.close();
                return true;
            }
            rs.close();
            select.close();
        
            if (balance < cost) {
                p.sendMessage(ChatColor.RED + 
                    String.format("You only have %.2f tokens, but need %.2f.", balance, cost)
                );
                return true;
            }
        
        
            PreparedStatement update = conn.prepareStatement(
                "UPDATE balance SET balance = balance - ? WHERE uuid = ?"
            );
            update.setDouble(1, cost);
            update.setString(2, uuid);
            int rows = update.executeUpdate();
            update.close();
            if (rows == 0) {
                p.sendMessage(ChatColor.RED + "Failed to deduct tokens. Try again later.");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error during purchase: " + e);
            p.sendMessage(ChatColor.RED + "Database error: see console.");
            return true;
        }
        ItemStack box = new ItemStack(org.bukkit.Material.CHEST, amount);
        ItemMeta meta = box.getItemMeta();
        meta.setDisplayName(ChatColor.GOLD + "Lootbox: " + type);
        meta.getPersistentDataContainer().set(
            plugin.getKey("lootbox-type"),
            PersistentDataType.STRING,
            type
        );
        box.setItemMeta(meta);

        p.getInventory().addItem(box);
        p.sendMessage(ChatColor.GREEN + "You bought " + amount + " " + type + " lootbox(es).");
        return true;
    }
}
