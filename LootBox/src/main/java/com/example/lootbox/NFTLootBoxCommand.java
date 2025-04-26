package com.example.lootbox;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Command to purchase NFT lootboxes
 */
public class NFTLootBoxCommand implements CommandExecutor {
    private final LootBoxPlugin plugin;
    private final NFTLootBoxUtil nftLootBoxUtil;

    /**
     * Constructor
     * @param plugin The LootBoxPlugin instance
     * @param nftLootBoxUtil The NFTLootBoxUtil instance
     */
    public NFTLootBoxCommand(LootBoxPlugin plugin, NFTLootBoxUtil nftLootBoxUtil) {
        this.plugin = plugin;
        this.nftLootBoxUtil = nftLootBoxUtil;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        if (args.length != 2) {
            player.sendMessage(ChatColor.RED + "Usage: /nftlootbox <type> <amount>");
            player.sendMessage(ChatColor.YELLOW + "Available types: basic_nft, premium_nft, ultimate_nft");
            return true;
        }

        String type = args[0].toLowerCase();
        int amount;

        try {
            amount = Integer.parseInt(args[1]);
            if (amount <= 0) {
                player.sendMessage(ChatColor.RED + "Amount must be a positive number.");
                return true;
            }
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "Amount must be a number.");
            return true;
        }

        // Check if the type is valid
        if (!type.equals("basic_nft") && !type.equals("premium_nft") && !type.equals("ultimate_nft")) {
            player.sendMessage(ChatColor.RED + "Invalid lootbox type. Available types: basic_nft, premium_nft, ultimate_nft");
            return true;
        }

        // Get the price from the config
        double price = plugin.getConfig().getDouble("prices." + type, -1);
        if (price < 0) {
            player.sendMessage(ChatColor.RED + "Unknown box type or price not configured.");
            return true;
        }

        double cost = price * amount;
        String uuid = player.getUniqueId().toString();
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
            player.sendMessage(ChatColor.RED + "Database unavailable—try again later.");
            return true;
        }

        try {
            // Check if the player has enough balance
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
                player.sendMessage(ChatColor.RED + "Account not found.");
                rs.close();
                select.close();
                return true;
            }
            rs.close();
            select.close();

            if (balance < cost) {
                player.sendMessage(ChatColor.RED +
                    String.format("You only have %.2f tokens, but need %.2f.", balance, cost)
                );
                return true;
            }

            // Deduct the cost from the player's balance
            PreparedStatement update = conn.prepareStatement(
                "UPDATE balance SET balance = balance - ? WHERE uuid = ?"
            );
            update.setDouble(1, cost);
            update.setString(2, uuid);
            int rows = update.executeUpdate();
            update.close();

            if (rows == 0) {
                player.sendMessage(ChatColor.RED + "Failed to deduct tokens. Try again later.");
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("SQL error during purchase: " + e);
            player.sendMessage(ChatColor.RED + "Database error: see console.");
            return true;
        }

        // Create the NFT lootbox item
        ItemStack box = nftLootBoxUtil.createNFTLootbox(type, amount);

        // Add the lootbox to the player's inventory
        player.getInventory().addItem(box);
        player.sendMessage(ChatColor.GREEN + "You bought " + amount + " " + type.replace("_", " ") + " lootbox(es).");

        return true;
    }
}
