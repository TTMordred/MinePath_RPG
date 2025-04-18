package com.nftlogin.walletlogin.utils;

import java.util.regex.Pattern;

/**
 * Utility class for validating Solana wallet addresses.
 */
public class WalletValidator {

    private WalletValidator() {
        // Private constructor to prevent instantiation
    }

    // Solana wallet address pattern (base58 encoded, 32-44 characters)
    // Most Solana addresses are 44 characters, but some special addresses might be shorter
    private static final Pattern SOLANA_WALLET_PATTERN = Pattern.compile("^[1-9A-HJ-NP-Za-km-z]{32,44}$");

    // Phantom wallet typically uses 44 character addresses
    private static final Pattern PHANTOM_WALLET_PATTERN = Pattern.compile("^[1-9A-HJ-NP-Za-km-z]{44}$");

    /**
     * Validates if the given string is a valid Solana wallet address.
     *
     * @param walletAddress The wallet address to validate
     * @return true if the wallet address is valid, false otherwise
     */
    public static boolean isValidWalletAddress(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        // Check if it's a Solana address
        return SOLANA_WALLET_PATTERN.matcher(walletAddress).matches();
    }

    /**
     * Validates if the given string is a valid Phantom wallet address.
     *
     * @param walletAddress The wallet address to validate
     * @return true if the wallet address is a valid Phantom wallet, false otherwise
     */
    public static boolean isValidPhantomWallet(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return false;
        }

        // Check if it's a Phantom wallet address (44 characters)
        return PHANTOM_WALLET_PATTERN.matcher(walletAddress).matches();
    }

    /**
     * Gets the wallet type based on the address format.
     *
     * @param walletAddress The wallet address
     * @return The wallet type ("Solana", "Phantom") or "Unknown" if not recognized
     */
    public static String getWalletType(String walletAddress) {
        if (walletAddress == null || walletAddress.trim().isEmpty()) {
            return "Unknown";
        }

        if (PHANTOM_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return "Phantom";
        }

        if (SOLANA_WALLET_PATTERN.matcher(walletAddress).matches()) {
            return "Solana";
        }

        return "Unknown";
    }
}
