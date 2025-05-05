package com.minecraft.nftplugin.commands;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.service.MintNFTService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Command to mint an NFT directly to a specified player
 * Only admins can use this command
 */
public class MintNFTCommand implements CommandExecutor {

    private final NFTPlugin plugin;
    private final MintNFTService mintNFTService;

    /**
     * Constructor
     * @param plugin The NFTPlugin instance
     */
    public MintNFTCommand(NFTPlugin plugin) {
        this.plugin = plugin;
        this.mintNFTService = new MintNFTService(plugin);
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

        // Only inform admin, not the target player
        player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.YELLOW +
                "Minting NFT for " + targetPlayer.getName() + " using metadata: " + metadataKey + "...");

        // Verify NFT metadata exists (but don't store the values to avoid unnecessary operations)
        plugin.getConfigManager().getNftName(metadataKey);
        plugin.getConfigManager().getNftDescription(metadataKey);
        plugin.getConfigManager().getNftImageUrl(metadataKey);

        // Mint NFT
        CompletableFuture<String> future = plugin.getSolanaService().mintNft(targetPlayer, metadataKey);

        // Handle result
        future.thenAccept(transactionId -> {
            Bukkit.getScheduler().runTask(plugin, () -> {
                // Send success message only to admin
                String successMessage = plugin.getConfigManager().getMessage("nft_minted")
                        .replace("%tx_id%", transactionId);
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + ChatColor.GREEN +
                        "Successfully minted NFT for " + targetPlayer.getName() + "!");
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") + successMessage);

                // Create NFT item using the service
                ItemStack nftItem = mintNFTService.createNftItemFromMetadata(transactionId, metadataKey);

                // Add NFT to player's NFT inventory
                mintNFTService.addNftToPlayerInventory(targetPlayer, nftItem);

                // Send success messages
                targetPlayer.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.GREEN + "You received an NFT item for '" + metadataKey + "'! Check your /nftinv");
                player.sendMessage(plugin.getConfigManager().getMessage("prefix") +
                        ChatColor.GREEN + "NFT item added to " + targetPlayer.getName() + "'s NFT inventory.");

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
}