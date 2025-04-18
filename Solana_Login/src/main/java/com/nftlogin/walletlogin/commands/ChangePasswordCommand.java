package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChangePasswordCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public ChangePasswordCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;
        
        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(player.getUniqueId()) || 
                !plugin.getSessionManager().getSession(player.getUniqueId()).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in", 
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }
        
        // Check if the command has the correct number of arguments
        if (args.length != 3) {
            player.sendMessage(plugin.formatMessage("&cUsage: /changepassword <oldPassword> <newPassword> <confirmNewPassword>"));
            return false;
        }
        
        String oldPassword = args[0];
        String newPassword = args[1];
        String confirmNewPassword = args[2];
        
        // Check if old password is correct
        if (!plugin.getDatabaseManager().authenticatePlayer(player.getUniqueId(), oldPassword)) {
            player.sendMessage(plugin.formatMessage("&cIncorrect old password!"));
            return true;
        }
        
        // Check if new passwords match
        if (!newPassword.equals(confirmNewPassword)) {
            player.sendMessage(plugin.formatMessage("&cNew passwords do not match!"));
            return true;
        }
        
        // Check password length
        int minLength = plugin.getConfig().getInt("auth.min-password-length", 6);
        int maxLength = plugin.getConfig().getInt("auth.max-password-length", 32);
        
        if (newPassword.length() < minLength) {
            String message = plugin.getConfig().getString("messages.register-password-too-short", 
                    "Password is too short! Minimum length: %length%")
                    .replace("%length%", String.valueOf(minLength));
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }
        
        if (newPassword.length() > maxLength) {
            String message = plugin.getConfig().getString("messages.register-password-too-long", 
                    "Password is too long! Maximum length: %length%")
                    .replace("%length%", String.valueOf(maxLength));
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }
        
        // Update password
        boolean success = plugin.getDatabaseManager().updatePassword(player.getUniqueId(), newPassword);
        
        if (success) {
            player.sendMessage(plugin.formatMessage("&aYour password has been changed successfully!"));
            
            // Log password change
            plugin.getLogger().info("Player " + player.getName() + " changed their password.");
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to change password. Please try again later."));
        }
        
        return true;
    }
}
