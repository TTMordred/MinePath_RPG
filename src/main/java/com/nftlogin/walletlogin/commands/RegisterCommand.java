package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class RegisterCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public RegisterCommand(SolanaLogin plugin) {
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
        if (plugin.getSessionManager().hasSession(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.already-logged-in",
                    "You are already logged in!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if player is already registered
        if (plugin.getDatabaseManager().isPlayerRegistered(player.getUniqueId())) {
            String message = plugin.getConfig().getString("messages.already-registered",
                    "You are already registered!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check if the command has the correct number of arguments
        if (args.length != 2) {
            player.sendMessage(plugin.formatMessage("&cUsage: /register <password> <confirmPassword>"));
            return false;
        }

        String password = args[0];
        String confirmPassword = args[1];

        // Check if passwords match
        if (!password.equals(confirmPassword)) {
            String message = plugin.getConfig().getString("messages.register-password-mismatch",
                    "Passwords do not match!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check password length
        int minLength = plugin.getConfig().getInt("auth.min-password-length", 6);
        int maxLength = plugin.getConfig().getInt("auth.max-password-length", 32);

        if (password.length() < minLength) {
            String message = plugin.getConfig().getString("messages.register-password-too-short",
                    "Password is too short! Minimum length: %length%")
                    .replace("%length%", String.valueOf(minLength));
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        if (password.length() > maxLength) {
            String message = plugin.getConfig().getString("messages.register-password-too-long",
                    "Password is too long! Maximum length: %length%")
                    .replace("%length%", String.valueOf(maxLength));
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Check IP registration limit
        String ip = player.getAddress().getAddress().getHostAddress();
        int ipLimit = plugin.getConfig().getInt("settings.register-ip-limit", 3);

        if (plugin.getSessionManager().getIpRegistrations(ip) >= ipLimit) {
            String message = plugin.getConfig().getString("messages.register-ip-limit",
                    "You have reached the maximum number of accounts for your IP!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Register the player
        boolean success = plugin.getDatabaseManager().registerPlayer(player, password);

        if (success) {
            // Record IP registration
            plugin.getSessionManager().recordIpRegistration(ip);

            // Create session
            plugin.getSessionManager().createSession(player);
            plugin.getSessionManager().getSession(player.getUniqueId()).setAuthenticated(true);

            // Update last login
            plugin.getDatabaseManager().updateLastLogin(player.getUniqueId(), ip);

            String message = plugin.getConfig().getString("messages.register-success",
                    "You have successfully registered! Please login with /login <password>");
            player.sendMessage(plugin.formatMessage(message));

            // Log registration
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s registered from IP: %s", player.getName(), ip));
            }
        } else {
            String message = plugin.getConfig().getString("messages.register-fail",
                    "Registration failed. Please try again.");
            player.sendMessage(plugin.formatMessage(message));
        }

        return true;
    }
}
