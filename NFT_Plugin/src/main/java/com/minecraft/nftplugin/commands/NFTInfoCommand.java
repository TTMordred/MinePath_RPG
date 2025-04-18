package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Optional;

public class NFTInfoCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    public NFTInfoCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cThis command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if the player has permission
        if (!player.hasPermission("nftplugin.nftinfo")) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + "§cYou don't have permission to use this command.");
            return true;
        }

        // Check if the player has a registered wallet in SolanaLogin
        if (!plugin.getSolanaLoginIntegration().hasWalletConnected(player.getUniqueId())) {
            player.sendMessage(plugin.getConfigManager().getMessage("no_wallet"));
            return true;
        }

        // Get the player's wallet address from SolanaLogin
        Optional<String> walletAddressOpt = plugin.getSolanaLoginIntegration().getWalletAddress(player.getUniqueId());
        String walletAddress = walletAddressOpt.orElse("Unknown");

        // Display wallet information with improved formatting
        player.sendMessage("§8§m-----------------------------------------------------");
        player.sendMessage("§6§l✨ Your NFT Information ✨");
        player.sendMessage("§8§m-----------------------------------------------------");

        // Format wallet address for extremely compact display
        String displayWallet = walletAddress;
        if (walletAddress.length() > 12) {
            displayWallet = walletAddress.substring(0, 4) + "..." + walletAddress.substring(walletAddress.length() - 4);
        }

        player.sendMessage("§7Wallet: §b" + displayWallet);
        player.sendMessage("§7View wallet on Solana Explorer: ");

        // Add clickable link to view wallet on Solana Explorer
        String walletExplorerUrl = "https://explorer.solana.com/address/" + walletAddress + "?cluster=devnet";
        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
            "tellraw " + player.getName() + " {\"text\":\"§7[§a§lView Wallet on Explorer§7]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + walletExplorerUrl + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§7Click to view your wallet on Solana Explorer\"}}"
        );

        // Check if player is holding an item
        if (player.getInventory().getItemInMainHand() != null && !player.getInventory().getItemInMainHand().getType().isAir()) {
            // Check if the item is an NFT item
            if (plugin.getItemManager().isNftItem(player.getInventory().getItemInMainHand())) {
                // Get the NFT ID from the item
                String nftId = plugin.getItemManager().getNftId(player.getInventory().getItemInMainHand());
                String achievementKey = plugin.getItemManager().getAchievementKey(player.getInventory().getItemInMainHand());

                if (nftId != null && achievementKey != null) {
                    // Get achievement name and details
                    String achievementName = getFormattedAchievementName(achievementKey);
                    String description = plugin.getConfigManager().getNftDescription(achievementKey);
                    String imageUrl = plugin.getConfigManager().getNftImageUrl(achievementKey);

                    // Get item details
                    ItemStack item = player.getInventory().getItemInMainHand();
                    String itemName = item.hasItemMeta() && item.getItemMeta().hasDisplayName() ?
                                     item.getItemMeta().getDisplayName() : item.getType().toString();

                    // Display detailed NFT information with improved formatting
                    player.sendMessage("§8§m-----------------------------------------------------");
                    player.sendMessage("§e§lNFT Details: §r§6" + achievementName);
                    player.sendMessage("§8§m-----------------------------------------------------");

                    // Item name with NFT tag
                    player.sendMessage("§7Item: §b" + itemName + " §a<NFT Item>");

                    // Description with better formatting
                    if (description != null && !description.isEmpty()) {
                        player.sendMessage("§7Description: §f" + description);
                    }

                    // Transaction ID with extremely shortened display
                    String shortNftId = nftId;
                    if (nftId.length() > 12) {
                        shortNftId = nftId.substring(0, 4) + "..." + nftId.substring(nftId.length() - 4);
                    }
                    player.sendMessage("§7NFT ID: §f" + shortNftId);

                    // Achievement with colored text
                    player.sendMessage("§7Achievement: §6" + achievementName);

                    // Network with colored text
                    player.sendMessage("§7Network: §3Solana DevNet");

                    // Display enchantments if any with better formatting
                    if (!item.getEnchantments().isEmpty()) {
                        player.sendMessage("§7Enchantments: §d" + formatEnchantments(item));
                    }

                    player.sendMessage("§8§m-----------------------------------------------------");

                    // Display Solana Explorer link with only the functional button
                    String explorerUrl = "https://explorer.solana.com/address/" + nftId + "?cluster=devnet";
                    player.sendMessage("§7View on Solana Explorer: ");

                    // Use Spigot's JSON message API to create clickable links
                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                        "tellraw " + player.getName() + " {\"text\":\"§7[§a§lClick to Open Explorer§7]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + explorerUrl + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§7Click to open Solana Explorer\"}}"
                    );

                    // Display image link with only the functional button
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        player.sendMessage("§7Image: ");
                        plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(),
                            "tellraw " + player.getName() + " {\"text\":\"§7[§a§lOpen Image in Browser§7]\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + imageUrl + "\"},\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"§7Click to view NFT image\"}}"
                        );
                    }
                }
            } else {
                // Improved message when not holding an NFT item
                player.sendMessage("§8§m-----------------------------------------------------");
                player.sendMessage("§c§l❌ Not an NFT Item");
                player.sendMessage("§8§m-----------------------------------------------------");
                player.sendMessage("§7You are not holding an NFT item.");
                player.sendMessage("§7Hold an NFT item in your main hand to see its information.");
                player.sendMessage("§7Use §e/nft get §7to obtain NFT items.");
            }
        } else {
            // Improved message when not holding any item
            player.sendMessage("§8§m-----------------------------------------------------");
            player.sendMessage("§c§l❌ No Item Detected");
            player.sendMessage("§8§m-----------------------------------------------------");
            player.sendMessage("§7You are not holding any item in your main hand.");
            player.sendMessage("§7Hold an NFT item to see its information.");
            player.sendMessage("§7Use §e/nft get §7to obtain NFT items.");
        }

        player.sendMessage("§8§m-----------------------------------------------------");

        return true;
    }

    /**
     * Format achievement key to a readable name
     * @param achievementKey The achievement key
     * @return The formatted achievement name
     */
    private String getFormattedAchievementName(String achievementKey) {
        if (achievementKey == null) {
            return "Unknown";
        }

        switch (achievementKey) {
            case "anh_sang_vi_dai":
                return "Great Light";
            case "ancient_scroll":
                return "Ancient Scroll";
            case "diamond_sword":
                return "Diamond Sword of Power";
            default:
                // Convert snake_case to Title Case
                String[] words = achievementKey.split("_");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        result.append(Character.toUpperCase(word.charAt(0)))
                              .append(word.substring(1).toLowerCase())
                              .append(" ");
                    }
                }
                return result.toString().trim();
        }
    }

    /**
     * Format enchantments to a readable string
     * @param item The item with enchantments
     * @return The formatted enchantments string
     */
    private String formatEnchantments(ItemStack item) {
        if (item == null || item.getEnchantments().isEmpty()) {
            return "None";
        }

        StringBuilder result = new StringBuilder();
        Map<Enchantment, Integer> enchantments = item.getEnchantments();

        int i = 0;
        for (Map.Entry<Enchantment, Integer> entry : enchantments.entrySet()) {
            Enchantment enchantment = entry.getKey();
            int level = entry.getValue();

            // Get enchantment name
            String name = enchantment.getKey().getKey();
            name = name.replace("_", " ");

            // Capitalize first letter of each word
            String[] words = name.split(" ");
            StringBuilder enchName = new StringBuilder();
            for (String word : words) {
                if (!word.isEmpty()) {
                    enchName.append(Character.toUpperCase(word.charAt(0)))
                           .append(word.substring(1).toLowerCase())
                           .append(" ");
                }
            }

            // Add to result
            result.append(enchName.toString().trim())
                  .append(" ")
                  .append(formatRomanNumeral(level));

            // Add comma if not last
            if (i < enchantments.size() - 1) {
                result.append(", ");
            }

            i++;
        }

        return result.toString();
    }

    /**
     * Convert a number to Roman numeral
     * @param number The number to convert
     * @return The Roman numeral
     */
    private String formatRomanNumeral(int number) {
        if (number <= 0) {
            return "";
        }

        switch (number) {
            case 1: return "I";
            case 2: return "II";
            case 3: return "III";
            case 4: return "IV";
            case 5: return "V";
            case 6: return "VI";
            case 7: return "VII";
            case 8: return "VIII";
            case 9: return "IX";
            case 10: return "X";
            default: return String.valueOf(number);
        }
    }
}
