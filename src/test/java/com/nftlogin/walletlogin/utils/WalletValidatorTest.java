package com.nftlogin.walletlogin.utils;

/**
 * Test class for WalletValidator
 *
 * Note: This is a simple test class that can be run manually.
 * In a real project, you would use JUnit or another testing framework.
 */
public class WalletValidatorTest {

    public static void main(String[] args) {
        // Test Ethereum wallet addresses
        testWalletAddress("0x71C7656EC7ab88b098defB751B7401B5f6d8976F", true, "Ethereum");
        testWalletAddress("0x1234567890123456789012345678901234567890", true, "Ethereum");
        testWalletAddress("0xabcdef1234567890abcdef1234567890abcdef12", true, "Ethereum");
        testWalletAddress("0x71C7656EC7ab88b098defB751B7401B5f6d8976", false, "Unknown"); // Too short
        testWalletAddress("0x71C7656EC7ab88b098defB751B7401B5f6d8976FG", false, "Unknown"); // Invalid character
        testWalletAddress("71C7656EC7ab88b098defB751B7401B5f6d8976F", false, "Unknown"); // Missing 0x prefix

        // Test Solana wallet addresses
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", true, "Solana");
        testWalletAddress("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", true, "Solana");
        testWalletAddress("4rJYEG3Ez2LZWNGbqg2tR2RCbCMjhyLKQiXx6uAkGx9o", true, "Solana");
        // Note: Our validator requires exactly 44 characters for Solana addresses
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK", false, "Unknown"); // Too short (43 chars)
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKKO", false, "Unknown"); // Invalid character

        // Test invalid inputs
        testWalletAddress("", false, "Unknown");
        testWalletAddress(null, false, "Unknown");
        testWalletAddress("not-a-wallet-address", false, "Unknown");
    }

    private static void testWalletAddress(String address, boolean expectedValid, String expectedType) {
        boolean isValid = WalletValidator.isValidWalletAddress(address);
        String type = WalletValidator.getBlockchainType(address);

        System.out.println("Address: " + address);
        System.out.println("  Expected valid: " + expectedValid + ", Actual: " + isValid);
        System.out.println("  Expected type: " + expectedType + ", Actual: " + type);

        if (isValid != expectedValid || !type.equals(expectedType)) {
            System.out.println("  TEST FAILED!");
        } else {
            System.out.println("  TEST PASSED!");
        }

        System.out.println();
    }
}
