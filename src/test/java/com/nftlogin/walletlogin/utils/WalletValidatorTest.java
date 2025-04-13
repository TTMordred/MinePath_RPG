package com.nftlogin.walletlogin.utils;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WalletValidator
 */
public class WalletValidatorTest {

    @Test
    public void testValidSolanaAddresses() {
        // Test valid Solana wallet addresses
        assertTrue(WalletValidator.isValidWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK"));
        assertTrue(WalletValidator.isValidWalletAddress("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"));
        assertTrue(WalletValidator.isValidWalletAddress("4rJYEG3Ez2LZWNGbqg2tR2RCbCMjhyLKQiXx6uAkGx9o"));

        // Test shorter Solana addresses (32-43 chars)
        assertTrue(WalletValidator.isValidWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK")); // 43 chars
        assertTrue(WalletValidator.isValidWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNS")); // 42 chars
    }

    @Test
    public void testInvalidSolanaAddresses() {
        // Test invalid Solana addresses
        assertFalse(WalletValidator.isValidWalletAddress("1234567890123456789012345678901")); // 31 chars (too short)
        assertFalse(WalletValidator.isValidWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKKO")); // 45 chars (too long)
        assertFalse(WalletValidator.isValidWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK!")); // Invalid character
        assertFalse(WalletValidator.isValidWalletAddress("0x71C7656EC7ab88b098defB751B7401B5f6d8976F")); // Ethereum format

        // Test invalid inputs
        assertFalse(WalletValidator.isValidWalletAddress(""));
        assertFalse(WalletValidator.isValidWalletAddress(null));
        assertFalse(WalletValidator.isValidWalletAddress("not-a-wallet-address"));
    }

    @Test
    public void testWalletTypeDetection() {
        // Test wallet type detection
        assertEquals("Phantom", WalletValidator.getWalletType("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK"));
        assertEquals("Phantom", WalletValidator.getWalletType("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"));
        assertEquals("Solana", WalletValidator.getWalletType("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK")); // 43 chars
        assertEquals("Solana", WalletValidator.getWalletType("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNS")); // 42 chars
        assertEquals("Unknown", WalletValidator.getWalletType("not-a-wallet-address"));
        assertEquals("Unknown", WalletValidator.getWalletType(null));
    }

    @Test
    public void testPhantomWalletValidation() {
        // Test Phantom wallet specific validation
        assertTrue(WalletValidator.isValidPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK"));
        assertTrue(WalletValidator.isValidPhantomWallet("EPjFWdd5AufqSSqeM2qN1xzybapC8G4wEGGkZwyTDt1v"));
        assertFalse(WalletValidator.isValidPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK")); // 43 chars
        assertFalse(WalletValidator.isValidPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNS")); // 42 chars
        assertFalse(WalletValidator.isValidPhantomWallet("not-a-wallet-address"));
        assertFalse(WalletValidator.isValidPhantomWallet(null));
    }

    // Manual test method (kept for reference)
    public static void main(String[] args) {
        // This method is kept for manual testing if needed
        System.out.println("Running manual tests for WalletValidator");

        // Test a few wallet addresses
        testWalletAddress("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", true, "Phantom");
        testWalletAddress("not-a-wallet-address", false, "Unknown");

        // Test Phantom wallet validation
        testPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSKK", true);
        testPhantomWallet("DYw8jCTfwHNRJhhmFcbXvVDTqWMEVFBX6ZKUmG5CNSK", false); // 43 chars
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
