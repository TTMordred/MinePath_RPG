package com.nftlogin.walletlogin.utils;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for password hashing and verification.
 */
public class PasswordUtils {

    private static final Logger LOGGER = Logger.getLogger(PasswordUtils.class.getName());
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATIONS = 65536;
    private static final int KEY_LENGTH = 256;
    private static final int SALT_LENGTH = 16;

    private PasswordUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Generates a secure salt for password hashing.
     *
     * @param length The length of the salt
     * @return The generated salt
     */
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        RANDOM.nextBytes(salt);
        return salt;
    }

    /**
     * Hashes a password with a given salt.
     *
     * @param password The password to hash
     * @param salt The salt to use
     * @param iterations The number of iterations
     * @param keyLength The key length
     * @return The hashed password
     */
    public static byte[] hashPassword(char[] password, byte[] salt, int iterations, int keyLength) {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password", e);
            throw new RuntimeException("Error hashing password", e);
        }
    }

    /**
     * Hashes a password with default parameters.
     *
     * @param password The password to hash
     * @return A string containing the salt and hash, Base64 encoded
     */
    public static String hashPassword(String password) {
        byte[] salt = generateSalt(SALT_LENGTH);
        byte[] hash = hashPassword(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

        // Format: iterations:salt:hash
        return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" +
               Base64.getEncoder().encodeToString(hash);
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password The password to verify
     * @param storedHash The stored hash
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String password, String storedHash) {
        try {
            // Split the stored hash into its components
            String[] parts = storedHash.split(":");
            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] hash = Base64.getDecoder().decode(parts[2]);

            // Hash the input password with the same salt and iterations
            byte[] testHash = hashPassword(password.toCharArray(), salt, iterations, hash.length * 8);

            // Compare the hashes
            return Arrays.equals(hash, testHash);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error verifying password", e);
            return false;
        }
    }

    /**
     * Generates a random verification code.
     *
     * @deprecated This method is used for the manual wallet connection method which is being phased out.
     *             It is kept for backward compatibility but will be removed in a future version.
     *             Use the QR code or browser extension connection methods instead.
     *
     * @param length The length of the code
     * @return The generated code
     */
    @Deprecated
    public static String generateVerificationCode(int length) {
        // Use only digits for verification code
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = RANDOM.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }
}
