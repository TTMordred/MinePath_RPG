package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class AdminCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public AdminCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has permission
        if (!sender.hasPermission("solanalogin.admin")) {
            sender.sendMessage(plugin.formatMessage("&cYou don't have permission to use this command."));
            return true;
        }

        // Check if the command has arguments
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        // Process the subcommand
        return processSubCommand(sender, subCommand);
    }

    /**
     * Process the subcommand.
     *
     * @param sender The command sender
     * @param subCommand The subcommand to process
     * @return true if the command was processed successfully
     */
    private boolean processSubCommand(CommandSender sender, String subCommand) {
        switch (subCommand) {
            case "reload":
                plugin.reloadConfig();
                sender.sendMessage(plugin.formatMessage("&aConfiguration reloaded!"));
                break;

            case "info":
                showPluginInfo(sender);
                break;

            default:
                showHelp(sender);
                break;
        }

        return true;
    }

    private void showHelp(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Admin Commands ==="));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin reload &7- Reload the configuration"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin info &7- Show plugin information"));
    }

    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Plugin Info ==="));
        sender.sendMessage(plugin.formatMessage("&eVersion: &7" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.formatMessage("&eAuthors: &7" + String.join(", ", plugin.getDescription().getAuthors())));

        // Database info
        sender.sendMessage(plugin.formatMessage("&eDatabase: &7" +
                plugin.getConfig().getString("database.host", "localhost") + ":" +
                plugin.getConfig().getInt("database.port", 3306) + "/" +
                plugin.getConfig().getString("database.database", "minecraft")));

        // Settings info
        sender.sendMessage(plugin.formatMessage("&eRequire Login: &7" +
                plugin.getConfig().getBoolean("settings.require-login", true)));
        sender.sendMessage(plugin.formatMessage("&eRequire Wallet: &7" +
                plugin.getConfig().getBoolean("settings.require-wallet-login", false)));
        sender.sendMessage(plugin.formatMessage("&eSolana Network: &7" +
                plugin.getConfig().getString("solana.network", "mainnet")));

        // Player stats
        int onlinePlayers = plugin.getServer().getOnlinePlayers().size();
        int authenticatedPlayers = 0;
        int walletConnectedPlayers = 0;

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (plugin.getSessionManager().hasSession(uuid) &&
                    plugin.getSessionManager().getSession(uuid).isAuthenticated()) {
                authenticatedPlayers++;
            }
            if (plugin.getDatabaseManager().hasWalletConnected(uuid)) {
                walletConnectedPlayers++;
            }
        }

        sender.sendMessage(plugin.formatMessage("&ePlayers Online: &7" + onlinePlayers));
        sender.sendMessage(plugin.formatMessage("&ePlayers Authenticated: &7" + authenticatedPlayers));
        sender.sendMessage(plugin.formatMessage("&ePlayers with Wallets: &7" + walletConnectedPlayers));
    }
}
