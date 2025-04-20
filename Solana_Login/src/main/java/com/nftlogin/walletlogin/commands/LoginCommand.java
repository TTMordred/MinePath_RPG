package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class LoginCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public LoginCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        // Check if the player is asking for help
        if (args.length == 1 && args[0].equalsIgnoreCase("help")) {
            showHelpMenu(sender);
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        // Check if player is already logged in
        if (plugin.getSessionManager().hasSession(player.getUniqueId()) &&
                plugin.getSessionManager().getSession(player.getUniqueId()).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.already-logged-in",
                    "You are already logged in!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if player is registered
        if (!plugin.getDatabaseManager().isPlayerRegistered(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.register-required",
                    "Please register with /register <password> <confirmPassword>");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if the command has the correct number of arguments
        if (args.length != 1) {
            player.sendMessage(plugin.formatMessage("&cUsage: /login <password> or /login help"));
            return false;
        }

        // Check if player has exceeded login attempts
        if (plugin.getSessionManager().hasExceededLoginAttempts(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.login-attempts-exceeded",
                    "Too many failed login attempts. Please try again later.");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        String password = args[0];

        // Authenticate the player
        boolean success = plugin.getDatabaseManager().authenticatePlayer(player.getUniqueId(), password);

        if (success) {
            // Reset login attempts
            plugin.getSessionManager().resetLoginAttempts(player.getUniqueId());

            // Create or update session
            if (!plugin.getSessionManager().hasSession(player.getUniqueId())) {
                plugin.getSessionManager().createSession(player);
            }
            plugin.getSessionManager().getSession(player.getUniqueId()).setAuthenticated(true);

            // Update last login
            String ip = player.getAddress().getAddress().getHostAddress();
            plugin.getDatabaseManager().updateLastLogin(player.getUniqueId(), ip);
            plugin.getDatabaseManager().saveSession(player.getUniqueId(), ip);

            String message = plugin.getConfig().getString("messages.login-success",
                    "You have successfully logged in!");
            player.sendMessage(plugin.formatMessage(message));

            // Notify player that session will expire when they leave the server
            player.sendMessage(plugin.formatMessage("&7Note: You will need to login again when you rejoin the server."));

            // Log login
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s logged in from IP: %s", player.getName(), ip));
            }
        } else {
            // Record failed login attempt
            int attemptsLeft = plugin.getSessionManager().recordFailedLoginAttempt(player.getUniqueId());

            String message = plugin.getConfig().getString("messages.login-fail",
                    "Incorrect password! Attempts remaining: %attempts%")
                    .replace("%attempts%", String.valueOf(attemptsLeft));
            player.sendMessage(plugin.formatMessage(message));

            // Log failed login attempt
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Failed login attempt for player %s from IP: %s (Attempts left: %d)",
                        player.getName(), player.getAddress().getAddress().getHostAddress(), attemptsLeft));
            }
        }

        return true;
    }

    /**
     * Shows the help menu with all available commands.
     *
     * @param sender The command sender
     */
    private void showHelpMenu(CommandSender sender) {
        sender.sendMessage(plugin.formatMessage("&6=== SolanaLogin Help Menu ==="));
        sender.sendMessage(plugin.formatMessage("&7User Commands:&r"));
        sender.sendMessage(plugin.formatMessage("&e/register <password> <confirmPassword> &7- Register an account"));
        sender.sendMessage(plugin.formatMessage("&e/login <password> &7- Login to your account"));
        sender.sendMessage(plugin.formatMessage("&e/changepassword <oldPassword> <newPassword> <confirmNewPassword> &7- Change your password"));
        sender.sendMessage(plugin.formatMessage("&e/logout &7- Logout from your account"));
        sender.sendMessage(plugin.formatMessage("&e/connectwallet &7- Connect your Solana wallet"));
        sender.sendMessage(plugin.formatMessage("&e/walletinfo &7- View your Solana wallet information"));

        // Only show admin commands to players with permission
        if (sender.isOp() || sender.hasPermission("solanalogin.admin")) {
            sender.sendMessage(plugin.formatMessage("&7Admin Commands:&r"));
            sender.sendMessage(plugin.formatMessage("&e/disconnectwallet &7- Disconnect a Solana wallet (admin only)"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin help &7- Show all admin commands"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin reload &7- Reload the plugin configuration"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin info &7- Show plugin information"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin stats &7- Show plugin statistics"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin list [filter] &7- List players with filters"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin forcelogin <player> &7- Force a player to be logged in"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin forcelogout <player> &7- Force a player to be logged out"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin resetpassword <player> <newpassword> &7- Reset a player's password"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin forcewallet <player> <address> &7- Force connect a wallet"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin disconnectwallet <player> &7- Force disconnect a wallet"));
            sender.sendMessage(plugin.formatMessage("&e/solanalogin purge <days> &7- Purge inactive accounts"));
        }

        sender.sendMessage(plugin.formatMessage("&6==============================="));
    }
}
