package com.nftlogin.walletlogin.utils;

/**
 * Test class for WalletValidator
 *
 * Note: This is a simple test class that can be run manually.
 * In a real project, you would use JUnit or another testing framework.
 *
 * @SuppressWarnings("java:S2187") // Suppress SonarLint warning about missing test methods
 */
public class WalletValidatorTest {

    public static void main(String[] args) {
        // Test Solana wallet addresses
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", true, "Phantom");
        testWalletAddress("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", true, "Phantom");
        testWalletAddress("4rJYEG3Ez2LZWNGbqg2tR2RCbCMjhyLKQiXx6uAkGx9o", true, "Phantom");

        // Test shorter Solana addresses (32-43 chars)
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK", true, "Solana"); // 43 chars
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNS", true, "Solana"); // 42 chars
        testWalletAddress("1234567890123456789012345678901", false, "Unknown"); // 31 chars (too short)

        // Test invalid Solana addresses
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKKO", false, "Unknown"); // 45 chars (too long)
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK!", false, "Unknown"); // Invalid character
        testWalletAddress("0x71C7656EC7ab88b098defB751B7401B5f6d8976F", false, "Unknown"); // Ethereum format

        // Test invalid inputs
        testWalletAddress("", false, "Unknown");
        testWalletAddress(null, false, "Unknown");
        testWalletAddress("not-a-wallet-address", false, "Unknown");

        // Test Phantom wallet specific validation
        System.out.println("\nTesting Phantom wallet specific validation:");
        testPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", true);
        testPhantomWallet("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v", true);
        testPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK", false); // 43 chars
        testPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNS", false); // 42 chars
        testPhantomWallet("not-a-wallet-address", false);
        testPhantomWallet(null, false);
    }

    private static void testWalletAddress(String address, boolean expectedValid, String expectedType) {
        boolean isValid = WalletValidator.isValidWalletAddress(address);
        String type = WalletValidator.getWalletType(address);

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

    private static void testPhantomWallet(String address, boolean expectedValid) {
        boolean isValid = WalletValidator.isValidPhantomWallet(address);

        System.out.println("Phantom wallet test - Address: " + address);
        System.out.println("  Expected valid: " + expectedValid + ", Actual: " + isValid);

        if (isValid != expectedValid) {
            System.out.println("  TEST FAILED!");
        } else {
            System.out.println("  TEST PASSED!");
        }

        System.out.println();
    }
}
