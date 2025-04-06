package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.WalletLogin;
import com.nftlogin.walletlogin.utils.WalletValidator;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Optional;
import java.util.UUID;

public class WalletInfoCommand implements CommandExecutor {

    private final WalletLogin plugin;

    public WalletInfoCommand(WalletLogin plugin) {
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
        String blockchainType = WalletValidator.getBlockchainType(wallet);
        
        String message = plugin.getConfig().getString("messages.wallet-info", 
                "Your connected wallet is: %wallet%")
                .replace("%wallet%", wallet);
        
        player.sendMessage(plugin.formatMessage(message));
        player.sendMessage(plugin.formatMessage("&aWallet type: &6" + blockchainType));

        return true;
    }
}
