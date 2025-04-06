package com.nftlogin.walletlogin.utils;

import java.util.regex.Pattern;

public class WalletValidator {

    // Ethereum wallet address pattern (0x followed by 40 hex characters)
    private static final Pattern ETH_WALLET_PATTERN = Pattern.compile("^0x[a-fA-F0-9]{40}$");

    // Solana wallet address pattern (base58 encoded, typically 32-44 characters)
    private static final Pattern SOLANA_WALLET_PATTERN = Pattern.compile("^[1-9A-HJ-NP-Za-km-z]{44}$");

    /**
     * Validates if the given string is a valid wallet address.
     * Currently supports Ethereum and Solana wallet formats.
     *
     * @param walletAddress The wallet address to validate
     * @return true if the wallet address is valid, false otherwise
     */
    public static boolean isValidWalletAddress(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        // Check if it's an Ethereum address
        if (ETH_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return true;
        }

        // Check if it's a Solana address
        if (SOLANA_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return true;
        }

        // Add more wallet format validations as needed

        return false;
    }

    /**
     * Gets the blockchain type based on the wallet address format.
     *
     * @param walletAddress The wallet address
     * @return The blockchain type (e.g., "Ethereum", "Solana") or "Unknown" if not recognized
     */
    public static String getBlockchainType(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return "Unknown";
        }

        if (ETH_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return "Ethereum";
        }

        if (SOLANA_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return "Solana";
        }

        return "Unknown";
    }
}
