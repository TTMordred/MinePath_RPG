package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LogoutCommand implements CommandExecutor {

    private final SolanaLogin plugin;

    public LogoutCommand(SolanaLogin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.formatMessage("&cOnly players can use this command."));
            return true;
        }

        Player player = (Player) sender;

        return handleLogout(player);
    }

    /**
     * Handle the logout process for a player.
     *
     * @param player The player to log out
     * @return true if the logout was successful, false otherwise
     */
    private boolean handleLogout(Player player) {
        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(player.getUniqueId()) ||
                !plugin.getSessionManager().getSession(player.getUniqueId()).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in",
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return true;
        }

        // Remove session
        plugin.getSessionManager().removeSession(player.getUniqueId());
        plugin.getDatabaseManager().removeSession(player.getUniqueId());

        player.sendMessage(plugin.formatMessage("&aYou have been logged out."));

        // If login is required, kick the player
        if (plugin.getConfig().getBoolean("settings.require-login", true)) {
            player.kickPlayer(plugin.formatMessage("&aYou have been logged out."));
        }

        return true;
    }
}
