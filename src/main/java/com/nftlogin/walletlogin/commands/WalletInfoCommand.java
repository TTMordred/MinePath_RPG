package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.utils.WalletValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class WalletInfoCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public WalletInfoCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
                return true;
            }

            Player player = (Player) sender;
            return displayWalletInfo(player);
        } catch (Exception e) {
            plugin.getLogger().severe("Error executing wallet info command: " + e.getMessage());
            sender.sendMessage(plugin.formatMessage("&cAn error occurred while executing the command."));
            return false;
        }
    }

    /**
     * Display wallet information for a player.
     *
     * @param player The player
     * @return true if the information was displayed successfully, false otherwise
     */
    private boolean displayWalletInfo(Player player) {
        try {
            UUID playerUuid = player.getUniqueId();

            // Check if player is logged in
            if (!plugin.getSessionManager().hasSession(playerUuid) ||
                    !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
                String message = plugin.getConfig().getString("messages.not-logged-in",
                        "You must be logged in to use this command!");
                player.sendMessage(plugin.formatMessage(message));
                return true;
            }

            // Get player's wallet address
            Optional<String> walletAddress = plugin.getDatabaseManager().getWalletAddress(playerUuid);

            if (!walletAddress.isPresent()) {
                String message = plugin.getConfig().getString("messages.not-connected",
                        "You don't have a wallet connected.");
                player.sendMessage(plugin.formatMessage(message));
                return true;
            }

            // Display wallet information
            String wallet = walletAddress.get();

            // Get wallet type
            Optional<String> walletTypeOpt = plugin.getDatabaseManager().getWalletType(playerUuid);
            String walletType = walletTypeOpt.orElse(WalletValidator.getWalletType(wallet));

            // Check if wallet is verified
            boolean isVerified = plugin.getDatabaseManager().isWalletVerified(playerUuid);

            String message = plugin.getConfig().getString("messages.wallet-info",
                    "Your connected Solana wallet is: %wallet%")
                    .replace("%wallet%", wallet);

            player.sendMessage(plugin.formatMessage(message));
            player.sendMessage(plugin.formatMessage("&aWallet type: &6" + walletType));
            player.sendMessage(plugin.formatMessage("&aVerification status: " +
                    (isVerified ? "&aVerified" : "&cNot Verified")));

            // If not verified, remind the player to verify
            if (!isVerified) {
                String verifyMessage = plugin.getConfig().getString("messages.wallet-verification-required",
                        "You need to verify your wallet ownership. Please check the website or use /verifycode <code>");
                player.sendMessage(plugin.formatMessage(verifyMessage));
            }

            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Error displaying wallet info: " + e.getMessage());
            player.sendMessage(plugin.formatMessage("&cAn error occurred while retrieving wallet information."));
            return false;
        }
    }
}
