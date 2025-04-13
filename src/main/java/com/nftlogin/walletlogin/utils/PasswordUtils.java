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
    /**
     * Hashes a password using PBKDF2 with the specified parameters.
     *
     * @param password The password to hash
     * @param salt The salt to use
     * @param iterations The number of iterations
     * @param keyLength The key length
     * @return The hashed password
     * @throws PasswordHashingException If there is an error hashing the password
     */
    @SuppressWarnings("java:S1130") // Suppressing "Either log this exception and handle it, or rethrow it with some contextual information"
    public static byte[] hashPassword(char[] password, byte[] salt, int iterations, int keyLength) throws PasswordHashingException {
        try {
            PBEKeySpec spec = new PBEKeySpec(password, salt, iterations, keyLength);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(ALGORITHM);
            return skf.generateSecret(spec).getEncoded();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.log(Level.SEVERE, () -> "Algorithm " + ALGORITHM + " not available");
            throw new PasswordHashingException("Algorithm " + ALGORITHM + " not available", e);
        } catch (InvalidKeySpecException e) {
            LOGGER.log(Level.SEVERE, "Invalid key specification for password hashing");
            throw new PasswordHashingException("Invalid key specification for password hashing", e);
        }
    }

    /**
     * Exception thrown when there is an error hashing a password.
     */
    public static class PasswordHashingException extends Exception {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new PasswordHashingException.
         *
         * @param message The error message
         * @param cause The cause of the error
         */
        public PasswordHashingException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Hashes a password with default parameters.
     *
     * @param password The password to hash
     * @return A string containing the salt and hash, Base64 encoded
     */
    /**
     * Hashes a password with default parameters.
     *
     * @param password The password to hash
     * @return A string containing the salt and hash, Base64 encoded
     * @throws PasswordException If there is an error hashing the password
     */
    @SuppressWarnings("java:S1130") // Suppressing "Either log this exception and handle it, or rethrow it with some contextual information"
    public static String hashPassword(String password) throws PasswordException {
        try {
            byte[] salt = generateSalt(SALT_LENGTH);
            byte[] hash = hashPassword(password.toCharArray(), salt, ITERATIONS, KEY_LENGTH);

            // Format: iterations:salt:hash
            return ITERATIONS + ":" + Base64.getEncoder().encodeToString(salt) + ":" +
                   Base64.getEncoder().encodeToString(hash);
        } catch (PasswordHashingException e) {
            LOGGER.log(Level.SEVERE, "Error hashing password");
            throw new PasswordException("Error hashing password", e);
        }
    }

    /**
     * Exception thrown when there is an error with password operations.
     */
    public static class PasswordException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        /**
         * Creates a new PasswordException.
         *
         * @param message The error message
         * @param cause The cause of the error
         */
        public PasswordException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    /**
     * Verifies a password against a stored hash.
     *
     * @param password The password to verify
     * @param storedHash The stored hash
     * @return true if the password matches, false otherwise
     */
    /**
     * Verifies a password against a stored hash.
     *
     * @param password The password to verify
     * @param storedHash The stored hash
     * @return true if the password matches, false otherwise
     */
    @SuppressWarnings({"java:S1130", "java:S2583"}) // Suppressing "Either log this exception and handle it, or rethrow it with some contextual information" and "Change this condition so that it does not always evaluate to true"
    public static boolean verifyPassword(String password, String storedHash) {
        if (storedHash == null) {
            LOGGER.log(Level.SEVERE, "Cannot verify password: stored hash is null");
            return false;
        }

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
        } catch (PasswordHashingException e) {
            // Log with context and return false for security reasons
            LOGGER.log(Level.SEVERE, () -> "Error verifying password: hashing error for stored hash format " +
                    storedHash.substring(0, Math.min(10, storedHash.length())) + "...");
            return false;
        } catch (Exception e) {
            // Log with context and return false for security reasons
            LOGGER.log(Level.SEVERE, () -> "Error verifying password: general error for stored hash format " +
                    storedHash.substring(0, Math.min(10, storedHash.length())) + "...");
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
    @SuppressWarnings("java:S1133") // Suppressing "Do not forget to remove this deprecated code someday" as we have a plan to remove it in v1.4
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
