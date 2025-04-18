package com.minecraft.nftplugin.commands;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Command to mint an NFT directly to a specified player
 * Only admins can use this command
 */
public class MintNFTCommand implements CommandExecutor {

    private final NFTPlugin plugin;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public MintNFTCommand(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender is a player
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;

        // Check if player has permission
        if (!player.hasPermission("nftplugin.admin")) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        // Check if enough arguments
        if (args.length < 2) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Usage: /mintnft <username> <metadata_key>");
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                    "Example: /mintnft Steve diamond_sword");
            return true;
        }

        final String targetUsername = args[0];
        final String metadataKey = args[1];

        // Find target player
        Player targetPlayer = Bukkit.getPlayer(targetUsername);
        if (targetPlayer == null) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Player " + targetUsername + " is not online.");
            return true;
        }

        // Check if target player has a wallet
        Optional<String> walletAddressOpt = plugin.getSolanaLoginIntegration().getWalletAddress(targetPlayer.getUniqueId());
        if (!walletAddressOpt.isPresent()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Player " + targetPlayer.getName() + " doesn't have a registered Solana wallet.");
            return true;
        }

        // Check if metadata file exists
        File metadataFile = new File(plugin.getDataFolder(), "metadata/" + metadataKey + ".json");
        if (!metadataFile.exists()) {
            player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.RED +
                    "Metadata file not found: " + metadataKey + ".json");
            return true;
        }

        // Inform player
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + 
                "Minting NFT for " + targetPlayer.getName() + " using metadata: " + metadataKey + "...");
        targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW + 
                "Admin " + player.getName() + " is minting an NFT for you...");

        // Get NFT metadata
        String nftName = plugin.getConfigManager().getNftName(metadataKey);
        String nftDescription = plugin.getConfigManager().getNftDescription(metadataKey);
        String nftImageUrl = plugin.getConfigManager().getNftImageUrl(metadataKey);

        // Mint NFT
        CompletableFuture<String> future = plugin.getSolanaService().mintNft(targetPlayer, metadataKey);

        // Handle result
        future.thenAccept(transactionId -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Send success message
                String successMessage = plugin.getConfigManager().getMessage("nft_minted")
                        .replace("%tx_id%", transactionId);
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN + 
                        "Successfully minted NFT for " + targetPlayer.getName() + "!");
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + successMessage);
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") + successMessage);

                // Create NFT item using the same method as achievements
                ItemStack nftItem = createNftItemFromMetadata(transactionId, metadataKey);

                // Give item to player
                HashMap<Integer, ItemStack> leftover = targetPlayer.getInventory().addItem(nftItem);

                if (leftover.isEmpty()) {
                    targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            ChatColor.GREEN + "You received an NFT item for '" + metadataKey + "'!");
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            ChatColor.GREEN + "NFT item given to " + targetPlayer.getName() + ".");
                } else {
                    // Drop the item at player's feet if inventory is full
                    targetPlayer.getWorld().dropItem(targetPlayer.getLocation(), nftItem);
                    targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            ChatColor.YELLOW + "Your inventory is full. NFT item dropped at your feet.");
                    player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                            ChatColor.YELLOW + targetPlayer.getName() + "'s inventory was full. NFT item dropped at their location.");
                }

                // Log the mint
                plugin.getLogger().info("Admin " + player.getName() + " minted NFT for player " + targetPlayer.getName());
                plugin.getLogger().info("Transaction ID: " + transactionId);
                plugin.getLogger().info("Metadata key: " + metadataKey);
            });
        }).exceptionally(ex -> {
            // Handle exception
            Bukkit.getScheduler().runTask(plugin, () -> {
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.RED + "Error minting NFT: " + ex.getMessage());
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.RED + "Error minting your NFT. Please contact an administrator.");
                plugin.getLogger().severe("Error minting NFT: " + ex.getMessage());
                ex.printStackTrace();
            });
            return null;
        });

        return true;
    }

    /**
     * Create an NFT item from metadata file
     * @param transactionId The transaction ID
     * @param achievementKey The achievement key
     * @return The NFT item
     */
    private ItemStack createNftItemFromMetadata(String transactionId, String achievementKey) {
        try {
            // Get metadata file path
            String metadataPath = "metadata/" + achievementKey + ".json";
            File metadataFile = new File(plugin.getDataFolder(), metadataPath);
            
            if (!metadataFile.exists()) {
                plugin.getLogger().warning("Metadata file not found: " + metadataPath);
                // Fallback to ItemManager
                return plugin.getItemManager().createNftItem(transactionId, achievementKey);
            }
            
            // Parse metadata file
            Gson gson = new Gson();
            Reader reader = new FileReader(metadataFile);
            JsonObject metadata = gson.fromJson(reader, JsonObject.class);
            reader.close();
            
            // Check if metadata has reward section
            JsonObject reward = null;
            
            // First check for reward at top level
            if (metadata.has("reward")) {
                reward = metadata.getAsJsonObject("reward");
            } 
            // Then check in quest section
            else if (metadata.has("quest") && metadata.getAsJsonObject("quest").has("reward")) {
                reward = metadata.getAsJsonObject("quest").getAsJsonObject("reward");
            }
            
            if (reward == null) {
                plugin.getLogger().warning("Metadata file does not have reward section: " + metadataPath);
                // Fallback to ItemManager
                return plugin.getItemManager().createNftItem(transactionId, achievementKey);
            }
            
            // Create item
            return createItemFromReward(reward, transactionId, achievementKey);
        } catch (Exception e) {
            plugin.getLogger().severe("Error creating NFT item from metadata: " + e.getMessage());
            e.printStackTrace();
            // Fallback to ItemManager
            return plugin.getItemManager().createNftItem(transactionId, achievementKey);
        }
    }
    
    /**
     * Create an item from reward JSON
     * @param reward The reward JSON object
     * @param transactionId The transaction ID
     * @param achievementKey The achievement key
     * @return The item
     */
    private ItemStack createItemFromReward(JsonObject reward, String transactionId, String achievementKey) {
        // Get material
        String materialName = reward.has("item") ? reward.get("item").getAsString() : "PAPER";
        Material material = Material.valueOf(materialName);
        
        // Create item
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        if (meta != null) {
            // Set name
            if (reward.has("name")) {
                String name = reward.get("name").getAsString();
                meta.setDisplayName(name); // Already includes color codes
            }
            
            // Set lore
            if (reward.has("lore") && reward.get("lore").isJsonArray()) {
                List<String> lore = new ArrayList<>();
                JsonArray loreArray = reward.getAsJsonArray("lore");
                
                for (JsonElement element : loreArray) {
                    lore.add(element.getAsString());
                }
                
                // Add transaction ID to lore
                lore.add("");
                lore.add(ChatColor.GRAY + "Transaction: " + ChatColor.WHITE + transactionId);
                lore.add(ChatColor.BLUE + "" + ChatColor.UNDERLINE + 
                        "https://explorer.solana.com/address/" + transactionId + "?cluster=devnet");
                
                meta.setLore(lore);
            }
            
            // Set enchantments
            if (reward.has("enchantments") && reward.get("enchantments").isJsonArray()) {
                JsonArray enchantments = reward.getAsJsonArray("enchantments");
                
                for (JsonElement element : enchantments) {
                    String enchantmentStr = element.getAsString();
                    String[] parts = enchantmentStr.split(":");
                    
                    if (parts.length == 2) {
                        try {
                            String enchantName = parts[0];
                            int level = Integer.parseInt(parts[1]);
                            
                            // Try to get enchantment
                            Enchantment enchantment = null;
                            try {
                                enchantment = Enchantment.getByKey(NamespacedKey.minecraft(enchantName.toLowerCase()));
                            } catch (Exception ex) {
                                // Try to find enchantment by name (case insensitive)
                                for (Enchantment ench : Enchantment.values()) {
                                    if (ench.getKey().getKey().equalsIgnoreCase(enchantName) ||
                                        ench.getKey().toString().equalsIgnoreCase(enchantName)) {
                                        enchantment = ench;
                                        break;
                                    }
                                }
                            }
                            
                            if (enchantment != null) {
                                meta.addEnchant(enchantment, level, true);
                            }
                        } catch (Exception e) {
                            plugin.getLogger().warning("Error adding enchantment: " + enchantmentStr);
                        }
                    }
                }
            }
            
            // Set unbreakable
            if (reward.has("unbreakable")) {
                meta.setUnbreakable(reward.get("unbreakable").getAsBoolean());
            }
            
            // Set custom model data
            if (reward.has("custom_model_data")) {
                meta.setCustomModelData(reward.get("custom_model_data").getAsInt());
            }
            
            // Add item flags
            if (reward.has("glowing") && reward.get("glowing").getAsBoolean()) {
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }
            
            // Add other item flags
            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_UNBREAKABLE);
            
            // Add NFT data
            PersistentDataContainer container = meta.getPersistentDataContainer();
            NamespacedKey nftKey = new NamespacedKey(plugin, "nft");
            NamespacedKey nftIdKey = new NamespacedKey(plugin, "nft_id");
            NamespacedKey achievementKeyNS = new NamespacedKey(plugin, "achievement_key");
            
            container.set(nftKey, PersistentDataType.BYTE, (byte) 1);
            container.set(nftIdKey, PersistentDataType.STRING, transactionId);
            container.set(achievementKeyNS, PersistentDataType.STRING, achievementKey);
            
            // Apply meta to item
            item.setItemMeta(meta);
        }
        
        return item;
    }
}
