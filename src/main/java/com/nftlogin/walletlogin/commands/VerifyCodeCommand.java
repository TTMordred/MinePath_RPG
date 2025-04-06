package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class VerifyCodeCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public VerifyCodeCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        // Check if the command has the correct number of arguments
        if (args.length != 1) {
            player.sendMessage(plugin.formatMessage("&cUsage: /verifycode <code>"));
            return false;
        }

        String code = args[0];
        return verifyWalletCode(player, code);
    }

    /**
     * Verify a wallet verification code.
     *
     * @param player The player
     * @param code The verification code
     * @return true if the verification was successful, false otherwise
     */
    private boolean verifyWalletCode(Player player, String code) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(playerUuid) ||
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in",
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if player has a wallet connected
        Optional<String> walletAddress = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (!walletAddress.isPresent()) {
            String message = plugin.getConfig().getString("messages.not-connected",
                    "You don't have a wallet connected.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if wallet is already verified
        if (plugin.getDatabaseManager().isWalletVerified(playerUuid)) {
            player.sendMessage(plugin.formatMessage("&aYour wallet is already verified!"));
            return true;
        }

        // Verify the code
        boolean success = plugin.getSessionManager().verifyCode(playerUuid, code);
        if (success) {
            // Mark wallet as verified
            plugin.getDatabaseManager().setWalletVerified(playerUuid, true);

            // Update session
            plugin.getSessionManager().getSession(playerUuid).setWalletVerified(true);

            String message = plugin.getConfig().getString("messages.wallet-verification-success",
                    "Your wallet has been successfully verified!");
            player.sendMessage(plugin.formatMessage(message));

            // Log the verification
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s verified their wallet", player.getName()));
            }
        } else {
            String message = plugin.getConfig().getString("messages.wallet-verification-fail",
                    "Wallet verification failed. Please try again.");
            player.sendMessage(plugin.formatMessage(message));
        }

        return true;
    }
}
