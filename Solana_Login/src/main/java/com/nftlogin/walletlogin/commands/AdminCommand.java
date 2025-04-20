package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Command for admin management of the SolanaLogin plugin.
 */
public class AdminCommand implements CommandExecutor, TabCompleter {

    // Command constants
    private static final String CMD_HELP = "help";
    private static final String CMD_RELOAD = "reload";
    private static final String CMD_INFO = "info";
    private static final String CMD_STATS = "stats";
    private static final String CMD_LIST = "list";
    private static final String CMD_FORCE_LOGIN = "forcelogin";
    private static final String CMD_FORCE_LOGOUT = "forcelogout";
    private static final String CMD_RESET_PASSWORD = "resetpassword";
    private static final String CMD_FORCE_WALLET = "forcewallet";
    private static final String CMD_DISCONNECT_WALLET = "disconnectwallet";
    private static final String CMD_PURGE = "purge";

    // Config constants
    private static final String CONFIG_REQUIRE_LOGIN = "settings.require-login";
    private static final String CONFIG_REQUIRE_WALLET = "settings.require-wallet-login";

    // Message constants
    private static final String MSG_PLAYER_NOT_REGISTERED = "&cPlayer is not registered.";
    private static final String MSG_PLAYER_NOT_FOUND = "&cPlayer not found in database.";
    private static final String MSG_TOTAL_PREFIX = "&7Total: ";

    // Filter constants
    private static final String FILTER_ALL = "all";
    private static final String FILTER_ONLINE = "online";
    private static final String FILTER_REGISTERED = "registered";
    private static final String FILTER_UNREGISTERED = "unregistered";
    private static final String FILTER_WALLET = "wallet";
    private static final String FILTER_NO_WALLET = "nowallet";

    private final SolanaLogin plugin;

    public AdminCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if sender has admin permission
        if (!sender.hasPermission("solanalogin.admin")) {
            String message = plugin.getConfig().getString("messages.no-permission",
                    "&cYou don't have permission to use this command.");
            sender.sendMessage(plugin.formatMessage(message));
            return true; // Command handled, even though permission denied
        }

        if (args.length == 0) {
            showAdminHelp(sender);
            return true; // Command handled successfully
        }

        String subCommand = args[0].toLowerCase();
        boolean commandHandled = true; // Default to true, set to false if command not recognized

        switch (subCommand) {
            case CMD_HELP:
                showAdminHelp(sender);
                break;
            case CMD_RELOAD:
                reloadPlugin(sender);
                break;
            case CMD_INFO:
                showPluginInfo(sender);
                break;
            case CMD_STATS:
                showStats(sender);
                break;
            case CMD_LIST:
                listPlayers(sender, args);
                break;
            case CMD_FORCE_LOGIN:
                forceLogin(sender, args);
                break;
            case CMD_FORCE_LOGOUT:
                forceLogout(sender, args);
                break;
            case CMD_RESET_PASSWORD:
                resetPassword(sender, args);
                break;
            case CMD_FORCE_WALLET:
                forceWalletConnection(sender, args);
                break;
            case CMD_DISCONNECT_WALLET:
                forceWalletDisconnection(sender, args);
                break;
            case CMD_PURGE:
                purgeInactiveAccounts(sender, args);
                break;
            default:
                sender.sendMessage(plugin.formatMessage("&cUnknown subcommand. Use /solanalogin help for a list of commands."));
                commandHandled = false; // Command not recognized
                break;
        }

        return commandHandled; // Return true if command was handled, false otherwise
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("solanalogin.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            List<String> subCommands = Arrays.asList(CMD_HELP, CMD_RELOAD, CMD_INFO, CMD_STATS, CMD_LIST,
                    CMD_FORCE_LOGIN, CMD_FORCE_LOGOUT, CMD_RESET_PASSWORD, CMD_FORCE_WALLET, CMD_DISCONNECT_WALLET, CMD_PURGE);

            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals(CMD_LIST)) {
                return Arrays.asList(FILTER_ALL, FILTER_ONLINE, FILTER_REGISTERED, FILTER_UNREGISTERED, FILTER_WALLET, FILTER_NO_WALLET)
                        .stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals(CMD_FORCE_LOGIN) || subCommand.equals(CMD_FORCE_LOGOUT) ||
                    subCommand.equals(CMD_RESET_PASSWORD) || subCommand.equals(CMD_FORCE_WALLET) ||
                    subCommand.equals(CMD_DISCONNECT_WALLET)) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            } else if (subCommand.equals(CMD_PURGE)) {
                return Arrays.asList("30", "60", "90", "180", "365")
                        .stream()
                        .filter(s -> s.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
        }

        return Collections.emptyList();
    }

    /**
     * Shows the admin help menu.
     *
     * @param sender The command sender
     */
    private void showAdminHelp(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Admin Commands ==="));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin help &7- Show this help menu"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin reload &7- Reload the plugin configuration"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin info &7- Show plugin information"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin stats &7- Show plugin statistics"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin list [all|online|registered|unregistered|wallet|nowallet] &7- List players"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin forcelogin <player> &7- Force a player to be logged in"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin forcelogout <player> &7- Force a player to be logged out"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin resetpassword <player> <newpassword> &7- Reset a player's password"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin forcewallet <player> <address> &7- Force connect a wallet to a player"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin disconnectwallet <player> &7- Force disconnect a player's wallet"));
        sender.sendMessage(plugin.formatMessage("&e/solanalogin purge <days> &7- Purge inactive accounts older than specified days"));
        sender.sendMessage(plugin.formatMessage("&6==================================="));
    }

    /**
     * Reloads the plugin configuration.
     *
     * @param sender The command sender
     */
    private void reloadPlugin(CommandSender sender) {
        plugin.reloadConfig();
        sender.sendMessage(plugin.formatMessage("&aPlugin configuration reloaded successfully."));
    }

    /**
     * Shows plugin information.
     *
     * @param sender The command sender
     */
    private void showPluginInfo(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Plugin Info ==="));
        sender.sendMessage(plugin.formatMessage("&eVersion: &7" + plugin.getDescription().getVersion()));
        sender.sendMessage(plugin.formatMessage("&eAuthors: &7" + String.join(", ", plugin.getDescription().getAuthors())));
        sender.sendMessage(plugin.formatMessage("&eWebsite: &7" + plugin.getDescription().getWebsite()));
        sender.sendMessage(plugin.formatMessage("&eDescription: &7" + plugin.getDescription().getDescription()));

        // Configuration info
        sender.sendMessage(plugin.formatMessage("&eRequire Login: &7" + plugin.getConfig().getBoolean(CONFIG_REQUIRE_LOGIN, true)));
        sender.sendMessage(plugin.formatMessage("&eRequire Wallet: &7" + plugin.getConfig().getBoolean(CONFIG_REQUIRE_WALLET, true)));
        sender.sendMessage(plugin.formatMessage("&eLogin Timeout: &7" + plugin.getConfig().getInt("settings.login-timeout", 300) + " seconds"));
        sender.sendMessage(plugin.formatMessage("&eSession Timeout: &7" + plugin.getConfig().getInt("settings.session-timeout", 1440) + " minutes"));
        sender.sendMessage(plugin.formatMessage("&eWeb Server URL: &7" + plugin.getConfig().getString("web-server.url", "https://minepath-api.vercel.app")));
        sender.sendMessage(plugin.formatMessage("&6=============================="));
    }

    /**
     * Shows plugin statistics.
     *
     * @param sender The command sender
     */
    private void showStats(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Statistics ==="));

        // Database stats
        int totalPlayers = plugin.getDatabaseManager().getTotalPlayers();
        int registeredPlayers = plugin.getDatabaseManager().getRegisteredPlayers();
        int walletConnections = plugin.getDatabaseManager().getWalletConnections();

        sender.sendMessage(plugin.formatMessage("&eTotal Players: &7" + totalPlayers));
        sender.sendMessage(plugin.formatMessage("&eRegistered Players: &7" + registeredPlayers));
        sender.sendMessage(plugin.formatMessage("&eWallet Connections: &7" + walletConnections));

        // Online stats
        int onlinePlayers = Bukkit.getOnlinePlayers().size();
        int authenticatedPlayers = 0;
        int walletConnectedPlayers = 0;

        for (Player player : Bukkit.getOnlinePlayers()) {
            UUID uuid = player.getUniqueId();
            if (plugin.getSessionManager().hasSession(uuid) &&
                    plugin.getSessionManager().getSession(uuid).isAuthenticated()) {
                authenticatedPlayers++;
            }
            if (plugin.getDatabaseManager().hasWalletConnected(uuid)) {
                walletConnectedPlayers++;
            }
        }

        sender.sendMessage(plugin.formatMessage("&eOnline Players: &7" + onlinePlayers));
        sender.sendMessage(plugin.formatMessage("&eAuthenticated Players: &7" + authenticatedPlayers));
        sender.sendMessage(plugin.formatMessage("&ePlayers with Wallets: &7" + walletConnectedPlayers));
        sender.sendMessage(plugin.formatMessage("&6============================"));
    }

    /**
     * Lists players based on the specified filter.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void listPlayers(CommandSender sender, String[] args) {
        String filter = args.length > 1 ? args[1].toLowerCase() : "all";

        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Player List (" + filter + ") ==="));

        List<String> players = getPlayerList(filter);

        if (players.isEmpty()) {
            sender.sendMessage(plugin.formatMessage(getEmptyMessage(filter)));
        } else {
            sender.sendMessage(plugin.formatMessage(MSG_TOTAL_PREFIX + players.size() + getTotalSuffix(filter)));
            sender.sendMessage(plugin.formatMessage("&7" + String.join(", ", players)));
        }

        sender.sendMessage(plugin.formatMessage("&6======================================="));
    }

    /**
     * Gets a list of players based on the specified filter.
     *
     * @param filter The filter to apply
     * @return A list of player names, or an empty list if the filter is invalid
     */
    private List<String> getPlayerList(String filter) {
        switch (filter) {
            case FILTER_ALL:
                return plugin.getDatabaseManager().getAllPlayers();
            case FILTER_ONLINE:
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList());
            case FILTER_REGISTERED:
                return plugin.getDatabaseManager().getRegisteredPlayerNames();
            case FILTER_UNREGISTERED:
                return plugin.getDatabaseManager().getUnregisteredPlayerNames();
            case FILTER_WALLET:
                return plugin.getDatabaseManager().getPlayersWithWallet();
            case FILTER_NO_WALLET:
                return plugin.getDatabaseManager().getPlayersWithoutWallet();
            default:
                return Collections.emptyList();
        }
    }

    /**
     * Gets the message to display when the player list is empty.
     *
     * @param filter The filter that was applied
     * @return The empty message
     */
    private String getEmptyMessage(String filter) {
        switch (filter) {
            case FILTER_ALL:
                return "&7No players found.";
            case FILTER_ONLINE:
                return "&7No online players.";
            case FILTER_REGISTERED:
                return "&7No registered players.";
            case FILTER_UNREGISTERED:
                return "&7No unregistered players.";
            case FILTER_WALLET:
                return "&7No players with wallets.";
            case FILTER_NO_WALLET:
                return "&7No players without wallets.";
            default:
                return "&7No players found.";
        }
    }

    /**
     * Gets the suffix to display after the total count.
     *
     * @param filter The filter that was applied
     * @return The total suffix
     */
    private String getTotalSuffix(String filter) {
        switch (filter) {
            case FILTER_ALL:
                return " players";
            case FILTER_ONLINE:
                return " online players";
            case FILTER_REGISTERED:
                return " registered players";
            case FILTER_UNREGISTERED:
                return " unregistered players";
            case FILTER_WALLET:
                return " players with wallets";
            case FILTER_NO_WALLET:
                return " players without wallets";
            default:
                return " players";
        }
    }

    /**
     * Forces a player to be logged in.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void forceLogin(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin forcelogin <player>"));
            return;
        }

        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(plugin.formatMessage("&cPlayer not found or not online."));
            return;
        }

        UUID playerUuid = player.getUniqueId();

        // Check if player is registered
        if (!plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_REGISTERED));
            return;
        }

        // Check if player is already logged in
        if (plugin.getSessionManager().hasSession(playerUuid) &&
                plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            sender.sendMessage(plugin.formatMessage("&cPlayer is already logged in."));
            return;
        }

        // Force login
        if (!plugin.getSessionManager().hasSession(playerUuid)) {
            plugin.getSessionManager().createSession(player);
        }
        plugin.getSessionManager().getSession(playerUuid).setAuthenticated(true);

        // Update last login
        String ip = player.getAddress().getAddress().getHostAddress();
        plugin.getDatabaseManager().updateLastLogin(playerUuid, ip);
        plugin.getDatabaseManager().saveSession(playerUuid, ip);

        player.sendMessage(plugin.formatMessage("&aYou have been forcefully logged in by an admin."));
        sender.sendMessage(plugin.formatMessage("&aPlayer " + playerName + " has been forcefully logged in."));

        // Log the action
        if (plugin.getLogger().isLoggable(Level.INFO)) {
            plugin.getLogger().log(Level.INFO, String.format("Admin %s forced login for player %s",
                    sender.getName(), playerName));
        }
    }

    /**
     * Forces a player to be logged out.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void forceLogout(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin forcelogout <player>"));
            return;
        }

        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);

        if (player == null) {
            sender.sendMessage(plugin.formatMessage("&cPlayer not found or not online."));
            return;
        }

        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(playerUuid) ||
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            sender.sendMessage(plugin.formatMessage("&cPlayer is not logged in."));
            return;
        }

        // Force logout
        plugin.getSessionManager().removeSession(playerUuid);
        plugin.getDatabaseManager().removeSession(playerUuid);

        player.sendMessage(plugin.formatMessage("&cYou have been forcefully logged out by an admin."));
        sender.sendMessage(plugin.formatMessage("&aPlayer " + playerName + " has been forcefully logged out."));

        // Log the action
        if (plugin.getLogger().isLoggable(Level.INFO)) {
            plugin.getLogger().log(Level.INFO, String.format("Admin %s forced logout for player %s",
                    sender.getName(), playerName));
        }

        // If login is required, kick the player
        if (plugin.getConfig().getBoolean(CONFIG_REQUIRE_LOGIN, true)) {
            player.kick(net.kyori.adventure.text.Component.text(
                    ChatColor.translateAlternateColorCodes('&',
                    "&cYou have been logged out by an admin. Please reconnect and login again.")));
        }
    }

    /**
     * Resets a player's password.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void resetPassword(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin resetpassword <player> <newpassword>"));
            return;
        }

        String playerName = args[1];
        String newPassword = args[2];

        // Check password length
        int minLength = plugin.getConfig().getInt("auth.min-password-length", 6);
        int maxLength = plugin.getConfig().getInt("auth.max-password-length", 32);

        if (newPassword.length() < minLength) {
            sender.sendMessage(plugin.formatMessage("&cPassword is too short! Minimum length: " + minLength));
            return;
        }

        if (newPassword.length() > maxLength) {
            sender.sendMessage(plugin.formatMessage("&cPassword is too long! Maximum length: " + maxLength));
            return;
        }

        // Get player UUID
        UUID playerUuid = plugin.getDatabaseManager().getPlayerUUID(playerName);

        if (playerUuid == null) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_FOUND));
            return;
        }

        // Check if player is registered
        if (!plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_REGISTERED));
            return;
        }

        // Reset password
        boolean success = plugin.getDatabaseManager().updatePassword(playerUuid, newPassword);

        if (success) {
            sender.sendMessage(plugin.formatMessage("&aPassword for player " + playerName + " has been reset."));

            // Notify player if online
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(plugin.formatMessage("&aYour password has been reset by an admin."));

                // Force logout
                plugin.getSessionManager().removeSession(playerUuid);
                plugin.getDatabaseManager().removeSession(playerUuid);

                // If login is required, kick the player
                if (plugin.getConfig().getBoolean(CONFIG_REQUIRE_LOGIN, true)) {
                    player.kick(net.kyori.adventure.text.Component.text(
                            ChatColor.translateAlternateColorCodes('&',
                            "&aYour password has been reset by an admin. Please reconnect and login with your new password.")));
                }
            }

            // Log the action
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().log(Level.INFO, String.format("Admin %s reset password for player %s",
                        sender.getName(), playerName));
            }
        } else {
            sender.sendMessage(plugin.formatMessage("&cFailed to reset password. Please try again."));
        }
    }

    /**
     * Forces a wallet connection for a player.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void forceWalletConnection(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin forcewallet <player> <address>"));
            return;
        }

        String playerName = args[1];
        String walletAddress = args[2];

        // Validate wallet address if enabled
        if (plugin.getConfig().getBoolean("settings.wallet-validation", true) &&
                !walletAddress.matches("^[1-9A-HJ-NP-Za-km-z]{32,44}$")) {
            sender.sendMessage(plugin.formatMessage("&cInvalid Solana wallet address format."));
            return;
        }

        // Get player UUID
        UUID playerUuid = plugin.getDatabaseManager().getPlayerUUID(playerName);

        if (playerUuid == null) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_FOUND));
            return;
        }

        // Check if player is registered
        if (!plugin.getDatabaseManager().isPlayerRegistered(playerUuid)) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_REGISTERED));
            return;
        }

        // Check if wallet is already in use by another player
        if (plugin.getDatabaseManager().isWalletAddressInUse(walletAddress, playerUuid)) {
            sender.sendMessage(plugin.formatMessage("&cThis wallet address is already connected to another player."));
            return;
        }

        // Force wallet connection
        boolean success = plugin.getDatabaseManager().connectWallet(playerUuid, walletAddress, "admin");

        if (success) {
            // Mark wallet as verified
            plugin.getDatabaseManager().setWalletVerified(playerUuid, true);

            // Update session if player is online
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline() &&
                    plugin.getSessionManager().hasSession(playerUuid)) {
                plugin.getSessionManager().getSession(playerUuid).setWalletVerified(true);
                player.sendMessage(plugin.formatMessage("&aA Solana wallet has been connected to your account by an admin."));
            }

            sender.sendMessage(plugin.formatMessage("&aWallet has been connected to player " + playerName + "."));

            // Log the action
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().log(Level.INFO, String.format("Admin %s connected wallet %s to player %s",
                        sender.getName(), walletAddress, playerName));
            }
        } else {
            sender.sendMessage(plugin.formatMessage("&cFailed to connect wallet. Please try again."));
        }
    }

    /**
     * Forces a wallet disconnection for a player.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void forceWalletDisconnection(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin disconnectwallet <player>"));
            return;
        }

        String playerName = args[1];

        // Get player UUID
        UUID playerUuid = plugin.getDatabaseManager().getPlayerUUID(playerName);

        if (playerUuid == null) {
            sender.sendMessage(plugin.formatMessage(MSG_PLAYER_NOT_FOUND));
            return;
        }

        // Check if player has a wallet connected
        if (!plugin.getDatabaseManager().hasWalletConnected(playerUuid)) {
            sender.sendMessage(plugin.formatMessage("&cPlayer doesn't have a wallet connected."));
            return;
        }

        // Force wallet disconnection
        boolean success = plugin.getDatabaseManager().disconnectWallet(playerUuid);

        if (success) {
            // Update session if player is online
            Player player = Bukkit.getPlayer(playerUuid);
            if (player != null && player.isOnline() &&
                    plugin.getSessionManager().hasSession(playerUuid)) {
                plugin.getSessionManager().getSession(playerUuid).setWalletVerified(false);
                player.sendMessage(plugin.formatMessage("&cYour Solana wallet has been disconnected by an admin."));

                // If wallet login is required, kick the player
                if (plugin.getConfig().getBoolean(CONFIG_REQUIRE_WALLET, true)) {
                    player.kick(net.kyori.adventure.text.Component.text(
                            ChatColor.translateAlternateColorCodes('&',
                            "&cYour wallet has been disconnected by an admin. Please reconnect and connect a wallet.")));
                }
            }

            sender.sendMessage(plugin.formatMessage("&aWallet has been disconnected from player " + playerName + "."));

            // Log the action
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().log(Level.INFO, String.format("Admin %s disconnected wallet from player %s",
                        sender.getName(), playerName));
            }
        } else {
            sender.sendMessage(plugin.formatMessage("&cFailed to disconnect wallet. Please try again."));
        }
    }

    /**
     * Purges inactive accounts older than the specified number of days.
     *
     * @param sender The command sender
     * @param args The command arguments
     */
    private void purgeInactiveAccounts(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.formatMessage("&cUsage: /solanalogin purge <days>"));
            return;
        }

        int days;
        try {
            days = Integer.parseInt(args[1]);
            if (days <= 0) {
                sender.sendMessage(plugin.formatMessage("&cDays must be a positive number."));
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.formatMessage("&cInvalid number format. Please enter a valid number of days."));
            return;
        }

        // Confirm purge
        sender.sendMessage(plugin.formatMessage("&eThis will permanently delete all accounts that have not logged in for " + days + " days."));
        sender.sendMessage(plugin.formatMessage("&eTo confirm, use: &c/solanalogin purge " + days + " confirm"));

        if (args.length >= 3 && args[2].equalsIgnoreCase("confirm")) {
            // Perform purge
            int purgedAccounts = plugin.getDatabaseManager().purgeInactiveAccounts(days);

            if (purgedAccounts >= 0) {
                sender.sendMessage(plugin.formatMessage("&aPurged " + purgedAccounts + " inactive accounts."));

                // Log the action
                if (plugin.getLogger().isLoggable(Level.INFO)) {
                    plugin.getLogger().log(Level.INFO, String.format("Admin %s purged %d inactive accounts older than %d days",
                            sender.getName(), purgedAccounts, days));
                }
            } else {
                sender.sendMessage(plugin.formatMessage("&cFailed to purge inactive accounts. Check the console for errors."));
            }
        }
    }
}
