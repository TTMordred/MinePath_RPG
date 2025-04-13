package com.nftlogin.walletlogin.solana;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * Configuration class for Solana blockchain integration.
 */
public class SolanaConfig {

    private final SolanaLogin plugin;
    private final boolean enabled;
    private final String network;
    private final String rpcUrl;
    private final String programId;
    private final String storageMode;
    private final String verificationMessage;

    /**
     * Creates a new SolanaConfig instance.
     *
     * @param plugin The SolanaLogin plugin instance
     */
    public SolanaConfig(SolanaLogin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        
        this.enabled = config.getBoolean("solana.enabled", false);
        this.network = config.getString("solana.network", "devnet");
        this.rpcUrl = config.getString("solana.rpc-url", "https://api.devnet.solana.com");
        this.programId = config.getString("solana.program-id", "");
        this.storageMode = config.getString("solana.storage-mode", "sql");
        this.verificationMessage = config.getString("solana.verification-message", 
                "I confirm that I own this wallet and authorize its use on the Minecraft server.");
    }

    /**
     * Checks if Solana integration is enabled.
     *
     * @return true if Solana integration is enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the Solana network to use.
     *
     * @return The Solana network (mainnet, testnet, or devnet)
     */
    public String getNetwork() {
        return network;
    }

    /**
     * Gets the Solana RPC URL.
     *
     * @return The Solana RPC URL
     */
    public String getRpcUrl() {
        return rpcUrl;
    }

    /**
     * Gets the Solana program ID.
     *
     * @return The Solana program ID
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * Gets the storage mode.
     *
     * @return The storage mode (sql, solana, or hybrid)
     */
    public String getStorageMode() {
        return storageMode;
    }

    /**
     * Gets the verification message.
     *
     * @return The verification message
     */
    public String getVerificationMessage() {
        return verificationMessage;
    }

    /**
     * Checks if a specific data type should be stored on Solana.
     *
     * @param dataType The data type to check
     * @return true if the data type should be stored on Solana, false otherwise
     */
    public boolean shouldStoreOnSolana(String dataType) {
        if (!enabled) {
            return false;
        }
        
        if (storageMode.equalsIgnoreCase("solana")) {
            return true;
        } else if (storageMode.equalsIgnoreCase("hybrid")) {
            String configKey = "storage." + dataType;
            String storageType = plugin.getConfig().getString(configKey, "sql");
            return storageType.equalsIgnoreCase("solana");
        }
        
        return false;
    }
}
