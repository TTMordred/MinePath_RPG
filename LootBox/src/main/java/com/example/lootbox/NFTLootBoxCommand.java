package com.example.lootbox;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.checkerframework.checker.units.qual.min;



/**
 * Command to purchase NFT lootboxes
 */
public class NFTLootBoxCommand implements CommandExecutor {
    private final LootBoxPlugin plugin;
    private final NFTLootBoxUtil nftLootBoxUtil; // Replace with actual URL

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
        Method burnTokensMethod;
        Object minePathInstance;
        try {
            // 1) Grab the already-loaded plugin by name (must match plugin.yml 'name:')
            Plugin raw = Bukkit.getPluginManager().getPlugin("MinePath");
            if (raw == null) {
                player.sendMessage(ChatColor.RED + "Token plugin not found on this server.");
                return true;
            }
            // 2) Reflect on its actual class
            Class<?> mpClass = raw.getClass();
            // 3) Locate the public API method
            burnTokensMethod = mpClass.getMethod("burnTokens", Player.class, String.class);
            minePathInstance  = raw;
        } catch (NoSuchMethodException nsme) {
            plugin.getLogger().severe("burnTokens(Player,String) not found in MinePathCoinPlugin");
            player.sendMessage(ChatColor.RED + "Token API has changed—contact an admin.");
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error hooking into token plugin: " + e);
            player.sendMessage(ChatColor.RED + "Internal error initializing token plugin.");
            return true;
        }

        // 2) Invoke burnTokens to deduct the cost
        try {
            // turn cost into a raw-unit string (assuming burnTokens expects human-readable)
            String costStr = String.valueOf((long)(cost)); 
            String txid = (String) burnTokensMethod.invoke(minePathInstance, player, costStr);

            // 3) On success, give the lootbox
            ItemStack box = nftLootBoxUtil.createNFTLootbox(type, amount);
            player.getInventory().addItem(box);
            player.sendMessage(ChatColor.GREEN + "Purchase successful! TXID: " + txid);
        } catch (Exception e) {
            // unwrap underlying cause if present
            Throwable cause = e.getCause() != null ? e.getCause() : e;
            player.sendMessage(ChatColor.RED + "Purchase failed: " + cause.getMessage());
            plugin.getLogger().severe("Error during external burn: " + cause);
        }

        return true;
    }
}
