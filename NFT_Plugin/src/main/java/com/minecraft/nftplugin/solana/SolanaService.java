package com.minecraft.nftplugin.solana;

import com.minecraft.nftplugin.NFTPlugin;
import com.minecraft.nftplugin.achievements.Achievement;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;

/**
 * Service for interacting with the Solana blockchain
 * Note: This class uses a Node.js backend service for actual Solana interactions
 */
public class SolanaService {

    private final NFTPlugin plugin;
    private final File backendDir;
    private final File nodeJsScriptFile;

    public SolanaService(NFTPlugin plugin) {
        this.plugin = plugin;
        this.backendDir = new File(plugin.getDataFolder(), "solana-backend");
        this.nodeJsScriptFile = new File(backendDir, "mint-nft.js");

        // Initialize the Node.js backend
        initializeBackend();
    }

    /**
     * Initialize the Node.js backend
     */
    private void initializeBackend() {
        // The backend directory and files are now extracted by NFTPlugin.extractSolanaBackendFiles()

        // Check if the backend directory exists
        if (!backendDir.exists()) {
            plugin.getLogger().warning("Solana backend directory not found. Please restart the server.");
            return;
        }

        // Check if the script file exists
        if (!nodeJsScriptFile.exists()) {
            plugin.getLogger().warning("Solana backend script not found. Please restart the server.");
            return;
        }

        plugin.getLogger().info("Solana backend initialized. Script path: " + nodeJsScriptFile.getAbsolutePath());

        // Create .env file from .env.example if it doesn't exist
        createEnvFileIfNeeded();

        // Check if npm is installed
        try {
            ProcessBuilder pb = new ProcessBuilder("npm", "--version");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                plugin.getLogger().warning("npm not found. Please install Node.js and npm to use the Solana NFT features.");
            } else {
                plugin.getLogger().info("npm is installed. Checking and updating dependencies...");
                updateDependencies();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to check npm installation: " + e.getMessage());
        }
    }

    /**
     * Update dependencies for the Solana backend
     * @param clean Whether to clean node_modules and reinstall all dependencies
     */
    public void updateDependencies(boolean clean) {
        File updateScriptFile = new File(backendDir, "update-dependencies.js");

        if (!updateScriptFile.exists()) {
            plugin.getLogger().warning("Update dependencies script not found: " + updateScriptFile.getAbsolutePath());
            return;
        }

        try {
            plugin.getLogger().info("Running dependency update script" + (clean ? " with clean install..." : "..."));

            ProcessBuilder pb;
            if (clean) {
                pb = new ProcessBuilder("node", updateScriptFile.getAbsolutePath(), "--clean");
            } else {
                pb = new ProcessBuilder("node", updateScriptFile.getAbsolutePath());
            }

            pb.directory(backendDir);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            // Read the output
            java.io.BufferedReader reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()));

            String line;
            while ((line = reader.readLine()) != null) {
                plugin.getLogger().info("Dependency update: " + line);
            }

            int exitCode = process.waitFor();
            if (exitCode == 0) {
                plugin.getLogger().info("Dependencies updated successfully.");
            } else {
                plugin.getLogger().warning("Failed to update dependencies. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to update dependencies: " + e.getMessage());
        }
    }

    /**
     * Update dependencies for the Solana backend
     */
    private void updateDependencies() {
        updateDependencies(false);
    }

    /**
     * Create .env file from .env.example if it doesn't exist
     */
    private void createEnvFileIfNeeded() {
        File envFile = new File(backendDir, ".env");

        if (!envFile.exists()) {
            try {
                // Create custom .env file with clear instructions
                java.util.List<String> lines = new java.util.ArrayList<>();
                lines.add("# Solana NFT Plugin Configuration");
                lines.add("# Created on: " + new java.util.Date());
                lines.add("");
                lines.add("# IMPORTANT: You must set your Solana wallet private key here");
                lines.add("# This wallet will be used to mint NFTs and pay for transaction fees");
                lines.add("# The private key should be in base58 format");
                lines.add("# Example: SOLANA_PRIVATE_KEY=4xkA4Uv1m6K1KRdoRJEgRvXCi6YVxNzBZEZ8pz4YG7bY7FLjKA9QnXZRJ7JyELxBw6GMPtBfEjNQjMf8ZnpzvJVj");
                lines.add("SOLANA_PRIVATE_KEY=your_private_key_here");
                lines.add("");
                lines.add("# Solana network settings (devnet, testnet, mainnet)");
                lines.add("SOLANA_NETWORK=devnet");
                lines.add("SOLANA_RPC_URL=https://api.devnet.solana.com");
                lines.add("");
                lines.add("# Mint fee in SOL (paid by the server wallet)");
                lines.add("MINT_FEE=0.000005");
                lines.add("");
                lines.add("# Transaction confirmation timeout (milliseconds)");
                lines.add("CONFIRMATION_TIMEOUT=60000");
                lines.add("");
                lines.add("# Number of retries for failed operations");
                lines.add("RETRY_COUNT=5");

                // Write the file
                java.nio.file.Path envPath = envFile.toPath();
                java.nio.file.Files.write(envPath, lines);

                plugin.getLogger().info("Created .env file in " + backendDir.getAbsolutePath());
                plugin.getLogger().warning("IMPORTANT: You need to edit the .env file and set your Solana wallet private key!");
                plugin.getLogger().warning("File location: " + envFile.getAbsolutePath());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create .env file: " + e.getMessage());
            }
        } else {
            // Check if the private key is set in the .env file
            try {
                java.nio.file.Path envPath = envFile.toPath();
                java.util.List<String> lines = java.nio.file.Files.readAllLines(envPath);
                boolean hasPrivateKey = false;

                for (String line : lines) {
                    if (line.startsWith("SOLANA_PRIVATE_KEY=") &&
                            !line.contains("your_private_key_here") &&
                            line.length() > 20) {
                        hasPrivateKey = true;
                        break;
                    }
                }

                if (!hasPrivateKey) {
                    plugin.getLogger().warning("SOLANA_PRIVATE_KEY is not properly set in .env file!");
                    plugin.getLogger().warning("Please edit: " + envFile.getAbsolutePath());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to check .env file: " + e.getMessage());
            }
        }
    }

    /**
     * Validate a Solana wallet address
     * @param walletAddress The wallet address to validate
     * @return True if the wallet address is valid, false otherwise
     */
    public boolean isValidWalletAddress(String walletAddress) {
        // Basic validation: Solana addresses are 32-44 characters long and base58 encoded
        if (walletAddress == null || walletAddress.length() < 32 || walletAddress.length() > 44) {
            plugin.getLogger().warning("Invalid wallet address length: " +
                    (walletAddress == null ? "null" : walletAddress.length()) +
                    ". Solana addresses should be 32-44 characters long.");
            return false;
        }

        // Check if the address contains only base58 characters
        boolean isBase58 = walletAddress.matches("^[1-9A-HJ-NP-Za-km-z]+$");
        if (!isBase58) {
            plugin.getLogger().warning("Invalid wallet address format: " + walletAddress +
                    ". Solana addresses should only contain base58 characters.");
            return false;
        }

        // Additional check: Solana addresses typically start with specific characters
        if (!walletAddress.matches("^[1-9A-HJ-NP-Za-km-z]{32,44}$")) {
            plugin.getLogger().warning("Suspicious wallet address format: " + walletAddress);
        }

        return true;
    }

    /**
     * Mint an NFT for a player
     * @param player The player
     * @param achievementKey The achievement key
     * @return A CompletableFuture that completes with the transaction ID when the NFT is minted
     */
    public CompletableFuture<String> mintNft(Player player, String achievementKey) {
        CompletableFuture<String> future = new CompletableFuture<>();

        // Get the player's wallet address from SolanaLogin
        UUID playerUuid = player.getUniqueId();
        Optional<String> walletAddressOpt = plugin.getSolanaLoginIntegration().getWalletAddress(playerUuid);

        if (!walletAddressOpt.isPresent()) {
            future.completeExceptionally(new IllegalStateException("Player does not have a registered wallet."));
            return future;
        }

        String walletAddress = walletAddressOpt.get();

        // Validate the wallet address
        if (!isValidWalletAddress(walletAddress)) {
            String errorMsg = "Invalid wallet address: " + walletAddress + ". Please check your wallet registration.";
            plugin.getLogger().severe(errorMsg);
            future.completeExceptionally(new IllegalStateException(errorMsg));
            return future;
        }

        plugin.getLogger().info("Minting NFT to wallet address: " + walletAddress + " for player: " + player.getName());

        // Get achievement from achievement manager
        Optional<Achievement> achievementOpt = plugin.getAchievementManager().getAchievement(achievementKey);
        if (!achievementOpt.isPresent()) {
            future.completeExceptionally(new IllegalStateException("Achievement not found: " + achievementKey));
            return future;
        }

        Achievement achievement = achievementOpt.get();

        // Get NFT metadata
        String nftName = achievement.getName();
        String nftDescription = achievement.getDescription();
        String nftImageUrl = achievement.getImageUrl();

        // Check if metadata file exists
        File metadataFile = new File(plugin.getDataFolder(), achievement.getMetadataFilePath());
        if (!metadataFile.exists()) {
            plugin.getLogger().warning("Metadata file not found for achievement: " + achievementKey);
            plugin.getLogger().warning("Using default metadata from achievement object");
        } else {
            plugin.getLogger().info("Using metadata file: " + metadataFile.getAbsolutePath());
        }

        // Get server wallet private key
        String privateKey = plugin.getConfigManager().getSolanaServerWalletPrivateKey();
        if (privateKey == null || privateKey.isEmpty()) {
            String errorMsg = "Server wallet private key is not configured. Please set it in one of the following ways:\n" +
                    "1. Add SOLANA_PRIVATE_KEY=your_private_key to plugins/NFTPlugin/solana-backend/.env file\n" +
                    "2. Set SOLANA_PRIVATE_KEY environment variable in your system\n" +
                    "3. Add server_wallet_private_key: 'your_private_key' to config.yml (not recommended for security reasons)";
            plugin.getLogger().severe(errorMsg);
            future.completeExceptionally(new IllegalStateException(errorMsg));
            return future;
        }

        // Get Solana network settings
        String network = plugin.getConfigManager().getSolanaNetwork();
        String rpcUrl = plugin.getConfigManager().getSolanaRpcUrl();

        // Run the minting process asynchronously
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                // Prepare the command to run the Node.js script
                ProcessBuilder pb = new ProcessBuilder(
                        "node",
                        nodeJsScriptFile.getAbsolutePath(),
                        "--network", network,
                        "--rpc-url", rpcUrl,
                        "--private-key", privateKey,
                        "--recipient", walletAddress,
                        "--name", nftName,
                        "--description", nftDescription,
                        "--image", nftImageUrl,
                        "--player", player.getName(),
                        "--achievement", achievementKey
                );

                // Set the working directory
                pb.directory(backendDir);

                // Redirect error stream to output stream
                pb.redirectErrorStream(true);

                // Start the process
                Process process = pb.start();

                // Read the output
                java.io.BufferedReader reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()));

                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    plugin.getLogger().info("Node.js: " + line);

                    // Check if the line contains the transaction ID
                    if (line.startsWith("SUCCESS:")) {
                        String[] parts = line.split(":");
                        if (parts.length >= 3) {
                            String mintAddress = parts[1].trim();
                            String transactionId = parts[2].trim();

                            // Log detailed information
                            plugin.getLogger().info("NFT minted successfully!");
                            plugin.getLogger().info("Player: " + player.getName());
                            plugin.getLogger().info("Wallet Address: " + walletAddress);
                            plugin.getLogger().info("Mint Address: " + mintAddress);
                            plugin.getLogger().info("Transaction ID: " + transactionId);

                            // Generate a unique NFT ID
                            String nftId = UUID.randomUUID().toString();

                            // Record the NFT in the database
                            boolean recorded = plugin.getDatabaseManager().recordNft(
                                    playerUuid, achievementKey, nftId, mintAddress, transactionId);

                            if (recorded) {
                                // Complete the future with the transaction ID
                                future.complete(transactionId);
                            } else {
                                future.completeExceptionally(new RuntimeException("Failed to record NFT in database."));
                            }
                        }
                    }
                }

                // Wait for the process to complete
                int exitCode = process.waitFor();

                // If the future is not completed yet, complete it exceptionally
                if (!future.isDone()) {
                    if (exitCode != 0) {
                        future.completeExceptionally(new RuntimeException(
                                "Failed to mint NFT. Exit code: " + exitCode + "\nOutput: " + output.toString()));
                    } else {
                        future.completeExceptionally(new RuntimeException(
                                "Failed to mint NFT. No transaction ID found in output.\nOutput: " + output.toString()));
                    }
                }
            } catch (Exception e) {
                plugin.log(Level.SEVERE, "Failed to mint NFT: " + e.getMessage());
                future.completeExceptionally(e);
            }
        });

        return future;
    }
}
