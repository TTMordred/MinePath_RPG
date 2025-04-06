package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.WalletLogin;
import com.nftlogin.walletlogin.utils.WalletValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class ConnectWalletCommand implements CommandExecutor {

    private final WalletLogin plugin;

    public ConnectWalletCommand(WalletLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        UUID playerUuid = player.getUniqueId();

        // Check if player already has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.already-connected", 
                    "You already have a wallet connected. Use /disconnectwallet first.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if the command has the correct number of arguments
        if (args.length != 1) {
            player.sendMessage(plugin.formatMessage("&cUsage: /connectwallet <wallet_address>"));
            return false;
        }

        String walletAddress = args[0];

        // Validate wallet address if validation is enabled
        if (plugin.getConfig().getBoolean("settings.wallet-validation", true)) {
            if (!WalletValidator.isValidWalletAddress(walletAddress)) {
                String message = plugin.getConfig().getString("messages.invalid-wallet", 
                        "The wallet address you provided is invalid.");
                player.sendMessage(plugin.formatMessage(message));
                return true;
            }
        }

        // Connect the wallet
        boolean success = plugin.getDatabaseManager().connectWallet(playerUuid, walletAddress);
        if (success) {
            String message = plugin.getConfig().getString("messages.wallet-connected", 
                    "Your wallet has been successfully connected!");
            player.sendMessage(plugin.formatMessage(message));
            
            // Log the wallet connection
            String blockchainType = WalletValidator.getBlockchainType(walletAddress);
            plugin.getLogger().info("Player " + player.getName() + " connected a " + 
                    blockchainType + " wallet: " + walletAddress);
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to connect your wallet. Please try again later."));
        }

        return true;
    }
}
