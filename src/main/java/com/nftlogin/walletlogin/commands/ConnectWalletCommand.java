package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.utils.PasswordUtils;
import com.nftlogin.walletlogin.utils.WalletValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class ConnectWalletCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public ConnectWalletCommand(SolanaLogin plugin) {
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
            player.sendMessage(plugin.formatMessage("&cUsage: /connectwallet <wallet_address>"));
            return false;
        }

        String walletAddress = args[0];
        return connectWallet(player, walletAddress);
    }

    /**
     * Connect a wallet to a player's account.
     *
     * @param player The player
     * @param walletAddress The wallet address to connect
     * @return true if the wallet was connected successfully, false otherwise
     */
    private boolean connectWallet(Player player, String walletAddress) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(playerUuid) ||
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in",
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if player already has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.already-connected",
                    "You already have a wallet connected. Use /disconnectwallet first.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Validate wallet address if validation is enabled
        if (plugin.getConfig().getBoolean("settings.wallet-validation", true) &&
                !WalletValidator.isValidWalletAddress(walletAddress)) {
            String message = plugin.getConfig().getString("messages.invalid-wallet",
                    "The wallet address you provided is not a valid Solana address.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Get wallet type
        String walletType = WalletValidator.getWalletType(walletAddress);

        // Connect the wallet
        boolean success = plugin.getDatabaseManager().connectWallet(playerUuid, walletAddress, walletType);
        if (success) {
            // Generate verification code
            String verificationCode = PasswordUtils.generateVerificationCode(6);
            plugin.getSessionManager().storeVerificationCode(playerUuid, verificationCode);

            String message = plugin.getConfig().getString("messages.wallet-connected",
                    "Your Solana wallet has been successfully connected!");
            player.sendMessage(plugin.formatMessage(message));

            // Send verification instructions
            player.sendMessage(plugin.formatMessage("&eTo verify your wallet ownership, use the code: &6" + verificationCode));
            player.sendMessage(plugin.formatMessage("&eYou can verify through the website or use /verifycode <code>"));

            // Log the wallet connection
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s connected a %s wallet: %s",
                        player.getName(), walletType, walletAddress));
            }
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to connect your wallet. Please try again later."));
        }

        return true;
    }
}
