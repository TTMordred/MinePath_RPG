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
            player.sendMessage(plugin.formatMessage("&cUsage: /login <password>"));
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
}
