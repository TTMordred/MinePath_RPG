package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.WalletLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class DisconnectWalletCommand implements CommandExecutor {

    private final WalletLogin plugin;

    public DisconnectWalletCommand(WalletLogin plugin) {
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

        // Check if player has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (!existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.not-connected", 
                    "You don't have a wallet connected.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Disconnect the wallet
        boolean success = plugin.getDatabaseManager().disconnectWallet(playerUuid);
        if (success) {
            String message = plugin.getConfig().getString("messages.wallet-disconnected", 
                    "Your wallet has been disconnected.");
            player.sendMessage(plugin.formatMessage(message));
            
            // Log the wallet disconnection
            plugin.getLogger().info("Player " + player.getName() + " disconnected their wallet.");
            
            // If wallet login is required, warn the player they'll need to reconnect
            if (plugin.getConfig().getBoolean("settings.require-wallet-login", false)) {
                String requiredMessage = plugin.getConfig().getString("messages.wallet-required", 
                        "You need to connect a wallet to play on this server. Use /connectwallet <address>");
                player.sendMessage(plugin.formatMessage(requiredMessage));
                
                // Set a timer to kick the player if they don't reconnect
                int timeout = plugin.getConfig().getInt("settings.login-timeout", 60);
                player.sendMessage(plugin.formatMessage("&cYou have " + timeout + 
                        " seconds to connect a wallet or you will be kicked."));
                
                plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                    // Check if the player is still online and hasn't connected a wallet
                    if (player.isOnline() && !plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                        player.kickPlayer(plugin.formatMessage(requiredMessage));
                    }
                }, timeout * 20L); // Convert seconds to ticks
            }
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to disconnect your wallet. Please try again later."));
        }

        return true;
    }
}
