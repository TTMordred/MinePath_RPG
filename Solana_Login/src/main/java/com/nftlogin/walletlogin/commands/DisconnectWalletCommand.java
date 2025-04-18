package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class DisconnectWalletCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public DisconnectWalletCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return false;
        }

        Player player = (Player) sender;
        disconnectWallet(player);
        return true;
    }

    /**
     * Disconnect a player's wallet.
     *
     * @param player The player
     */
    private void disconnectWallet(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(playerUuid) ||
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in",
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return;
        }

        // Check if player has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (!existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.not-connected",
                    "You don't have a wallet connected.");
            player.sendMessage(plugin.formatMessage(message));
            return;
        }

        // Disconnect the wallet
        boolean success = plugin.getDatabaseManager().disconnectWallet(playerUuid);
        if (success) {
            // Note: removeVerificationCode is deprecated but kept for backward compatibility

            String message = plugin.getConfig().getString("messages.wallet-disconnected",
                    "Your Solana wallet has been disconnected.");
            player.sendMessage(plugin.formatMessage(message));

            // Log the wallet disconnection
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s disconnected their wallet", player.getName()));
            }

            // If wallet login is required, warn the player they'll need to reconnect
            handleRequiredWalletLogin(player, playerUuid);
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to disconnect your wallet. Please try again later."));
        }
    }

    /**
     * Handle the case where wallet login is required.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     */
    private void handleRequiredWalletLogin(Player player, UUID playerUuid) {
        if (plugin.getConfig().getBoolean("settings.require-wallet-login", false)) {
            String requiredMessage = plugin.getConfig().getString("messages.wallet-required",
                    "You need to connect a Solana wallet to play on this server. Use /connectwallet <address>");
            player.sendMessage(plugin.formatMessage(requiredMessage));

            // Set a timer to kick the player if they don't reconnect
            int timeout = plugin.getConfig().getInt("settings.login-timeout", 60);
            player.sendMessage(plugin.formatMessage(String.format("&cYou have %d seconds to connect a wallet or you will be kicked.", timeout)));

            plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
                // Check if the player is still online and hasn't connected a wallet
                if (player.isOnline() && !plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
                    // Using the modern kick API with Component
                    player.kick(Component.text(plugin.formatMessage(requiredMessage)));
                }
            }, timeout * 20L); // Convert seconds to ticks
        }
    }
}
