package com.nftlogin.walletlogin.commands;

import com.nftlogin.walletlogin.SolanaLogin;
import com.nftlogin.walletlogin.utils.PasswordUtils;
import com.nftlogin.walletlogin.utils.WalletValidator;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

        // Check if web server is enabled
        boolean webServerEnabled = plugin.getConfig().getBoolean("web-server.enabled", false);

        // If no arguments, show QR code login option if web server is enabled
        if (args.length == 0) {
            if (webServerEnabled) {
                return showQRCodeLogin(player);
            } else {
                player.sendMessage(plugin.formatMessage("&cUsage: /connectwallet <wallet_address>"));
                return false;
            }
        }

        // If argument is "qr", show QR code login option
        if (args.length == 1 && args[0].equalsIgnoreCase("qr")) {
            if (webServerEnabled) {
                return showQRCodeLogin(player);
            } else {
                player.sendMessage(plugin.formatMessage("&cQR code login is not enabled on this server."));
                return true;
            }
        }

        // Otherwise, treat as wallet address
        if (args.length == 1) {
            String walletAddress = args[0];
            return connectWallet(player, walletAddress);
        } else {
            player.sendMessage(plugin.formatMessage("&cUsage: /connectwallet <wallet_address> or /connectwallet qr"));
            return false;
        }
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
            return false;
        }

        // Check if player already has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.already-connected",
                    "You already have a wallet connected. Use /disconnectwallet first.");
            player.sendMessage(plugin.formatMessage(message));
            return false;
        }

        // Validate wallet address if validation is enabled
        if (plugin.getConfig().getBoolean("settings.wallet-validation", true) &&
                !WalletValidator.isValidWalletAddress(walletAddress)) {
            String message = plugin.getConfig().getString("messages.invalid-wallet",
                    "The wallet address you provided is not a valid Solana address.");
            player.sendMessage(plugin.formatMessage(message));
            return false;
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
            return true;
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to connect your wallet. Please try again later."));
            return false;
        }
    }

    /**
     * Show QR code login option to a player.
     *
     * @param player The player
     * @return true if the QR code was shown successfully, false otherwise
     */
    private boolean showQRCodeLogin(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player can use QR code login
        if (!canUseQRCodeLogin(player)) {
            return false;
        }

        // Generate authentication data
        String[] authData = generateAuthData(playerUuid);
        String sessionId = authData[0];
        String webServerUrl = authData[1];
        String loginUrl = authData[2];

        // Send login instructions to player
        sendLoginInstructions(player, loginUrl);

        // Start checking for wallet connection
        startConnectionCheck(player, playerUuid, sessionId, webServerUrl);

        return true;
    }

    /**
     * Check if a player can use QR code login.
     *
     * @param player The player
     * @return true if the player can use QR code login, false otherwise
     */
    private boolean canUseQRCodeLogin(Player player) {
        UUID playerUuid = player.getUniqueId();

        // Check if player is logged in
        if (!plugin.getSessionManager().hasSession(playerUuid) ||
                !plugin.getSessionManager().getSession(playerUuid).isAuthenticated()) {
            String message = plugin.getConfig().getString("messages.not-logged-in",
                    "You must be logged in to use this command!");
            player.sendMessage(plugin.formatMessage(message));
            return false;
        }

        // Check if player already has a wallet connected
        Optional<String> existingWallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
        if (existingWallet.isPresent()) {
            String message = plugin.getConfig().getString("messages.already-connected",
                    "You already have a wallet connected. Use /disconnectwallet first.");
            player.sendMessage(plugin.formatMessage(message));
            return false;
        }

        return true;
    }

    /**
     * Generate authentication data for QR code login.
     *
     * @param playerUuid The player's UUID
     * @return Array containing [sessionId, webServerUrl, loginUrl]
     */
    private String[] generateAuthData(UUID playerUuid) {
        // Get player name
        String playerName = plugin.getServer().getOfflinePlayer(playerUuid).getName();
        if (playerName == null) {
            playerName = playerUuid.toString();
        }

        // Generate a nonce for secure authentication
        String nonce = plugin.getSessionManager().generateAuthNonce(playerUuid);

        // Generate a session ID
        String sessionId = UUID.randomUUID().toString();
        plugin.getSessionManager().storeAuthSession(playerUuid, sessionId);

        // Get web server URL from config
        String webServerUrl = plugin.getConfig().getString("web-server.url", "http://localhost:3000");

        // Create login URL
        String loginUrl = webServerUrl + "/login?session=" + sessionId + "&nonce=" + nonce + "&player=" + playerName;

        return new String[] {sessionId, webServerUrl, loginUrl};
    }

    /**
     * Send login instructions to the player.
     *
     * @param player The player
     * @param loginUrl The login URL
     */
    @SuppressWarnings("deprecation") // Using deprecated methods for clickable links
    private void sendLoginInstructions(Player player, String loginUrl) {
        // Send clickable link to player
        player.sendMessage(plugin.formatMessage("&a=== Solana Wallet Connection ==="));
        player.sendMessage(plugin.formatMessage("&eConnect your Solana wallet using one of these methods:"));

        // Create clickable links
        // Note: We're using deprecated methods because they're the only way to create clickable links
        // in Minecraft chat. These will be updated when a better alternative becomes available.
        // @SuppressWarnings("deprecation")
        TextComponent browserLink = new TextComponent(plugin.formatMessage("&6➤ &bClick here to connect via browser extension"));
        browserLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, loginUrl));
        browserLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Open wallet connection page in your browser").create()));
        player.spigot().sendMessage(browserLink);

        TextComponent qrLink = new TextComponent(plugin.formatMessage("&6➤ &bClick here to show QR code for mobile wallet"));
        qrLink.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, loginUrl + "&qr=true"));
        qrLink.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder("Show QR code to scan with your mobile wallet").create()));
        player.spigot().sendMessage(qrLink);

        player.sendMessage(plugin.formatMessage("&eThe connection link will expire in 5 minutes."));
    }

    /**
     * Start checking for wallet connection.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     * @param sessionId The session ID
     * @param webServerUrl The web server URL
     */
    private void startConnectionCheck(Player player, UUID playerUuid, String sessionId, String webServerUrl) {
        int checkInterval = plugin.getConfig().getInt("web-server.check-interval", 5);
        new BukkitRunnable() {
            private int attempts = 0;
            private final int maxAttempts = plugin.getConfig().getInt("web-server.qr-code-timeout", 300) / checkInterval;

            @Override
            public void run() {
                attempts++;

                // Check if player is still online
                if (!player.isOnline()) {
                    this.cancel();
                    return;
                }

                checkWalletConnection(player, playerUuid, sessionId, webServerUrl, attempts, maxAttempts, this);
            }
        }.runTaskTimerAsynchronously(plugin, checkInterval * 20L, checkInterval * 20L); // Convert seconds to ticks
    }

    /**
     * Check if a wallet has been connected.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     * @param sessionId The session ID
     * @param webServerUrl The web server URL
     * @param attempts The number of attempts so far
     * @param maxAttempts The maximum number of attempts
     * @param task The BukkitRunnable task
     */
    private void checkWalletConnection(Player player, UUID playerUuid, String sessionId, String webServerUrl,
                                      int attempts, int maxAttempts, BukkitRunnable task) {
        try {
            // Check if player already has a wallet connected (might have been connected manually)
            Optional<String> wallet = plugin.getDatabaseManager().getWalletAddress(playerUuid);
            if (wallet.isPresent()) {
                task.cancel();
                return;
            }

            // Check web server for connection status
            String responseStr = getConnectionStatus(sessionId, webServerUrl);

            if (responseStr.contains("\"connected\":true")) {
                handleSuccessfulConnection(player, playerUuid, responseStr, task);
            } else if (attempts >= maxAttempts) {
                handleConnectionTimeout(player, playerUuid, task);
            }
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING, "Error checking wallet connection status", e);

            if (attempts >= maxAttempts) {
                handleConnectionTimeout(player, playerUuid, task);
            }
        }
    }

    /**
     * Get the connection status from the web server.
     *
     * @param sessionId The session ID
     * @param webServerUrl The web server URL
     * @return The response string
     * @throws IOException If an I/O error occurs
     */
    private String getConnectionStatus(String sessionId, String webServerUrl) throws IOException {
        URL url = new URL(webServerUrl + "/status?session=" + sessionId);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;

        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        return response.toString();
    }

    /**
     * Handle a successful wallet connection.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     * @param responseStr The response string
     * @param task The BukkitRunnable task
     */
    private void handleSuccessfulConnection(Player player, UUID playerUuid, String responseStr, BukkitRunnable task) {
        // Extract wallet address
        int startIndex = responseStr.indexOf("\"walletAddress\":") + 17;
        int endIndex = responseStr.indexOf("\"", startIndex);
        String walletAddress = responseStr.substring(startIndex, endIndex);

        // Connect wallet in database
        String walletType = WalletValidator.getWalletType(walletAddress);
        boolean success = plugin.getDatabaseManager().connectWallet(playerUuid, walletAddress, walletType);

        if (success) {
            // Mark wallet as verified since it was connected through direct wallet authentication
            plugin.getDatabaseManager().setWalletVerified(playerUuid, true);

            // Update session
            plugin.getSessionManager().getSession(playerUuid).setWalletVerified(true);

            String successMessage = plugin.getConfig().getString("messages.wallet-connected",
                    "Your Solana wallet has been successfully connected and verified!");
            player.sendMessage(plugin.formatMessage(successMessage));

            // Log the wallet connection
            if (plugin.getLogger().isLoggable(Level.INFO)) {
                plugin.getLogger().info(String.format("Player %s connected and verified a %s wallet: %s",
                        player.getName(), walletType, walletAddress));
            }
        } else {
            player.sendMessage(plugin.formatMessage("&cFailed to connect your wallet. Please try again later."));
        }

        // Clean up
        plugin.getSessionManager().removeAuthSession(playerUuid);
        task.cancel();
    }

    /**
     * Handle a connection timeout.
     *
     * @param player The player
     * @param playerUuid The player's UUID
     * @param task The BukkitRunnable task
     */
    private void handleConnectionTimeout(Player player, UUID playerUuid, BukkitRunnable task) {
        player.sendMessage(plugin.formatMessage("&cWallet connection timed out. Please try again."));
        plugin.getSessionManager().removeAuthSession(playerUuid);
        task.cancel();
    }
}
