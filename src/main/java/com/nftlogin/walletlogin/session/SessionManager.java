package com.nftlogin.walletlogin.session;

import com.nftlogin.walletlogin.SolanaLogin;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player authentication sessions.
 */
public class SessionManager {

    private final SolanaLogin plugin;
    private final Map<UUID, Session> sessions;
    private final Map<UUID, LoginAttempt> loginAttempts;
    private final Map<String, Integer> ipRegistrations;
    private final Map<UUID, String> verificationCodes;
    private final Map<UUID, String> authNonces;
    private final Map<UUID, String> authSessions;

    public SessionManager(SolanaLogin plugin) {
        this.plugin = plugin;
        this.sessions = new ConcurrentHashMap<>();
        this.loginAttempts = new ConcurrentHashMap<>();
        this.ipRegistrations = new ConcurrentHashMap<>();
        this.verificationCodes = new ConcurrentHashMap<>();
        this.authNonces = new ConcurrentHashMap<>();
        this.authSessions = new ConcurrentHashMap<>();
    }

    /**
     * Creates a new session for a player.
     *
     * @param player The player
     */
    public void createSession(Player player) {
        UUID uuid = player.getUniqueId();
        Session session = new Session(uuid, player.getAddress().getAddress().getHostAddress());
        sessions.put(uuid, session);
    }

    /**
     * Removes a player's session.
     *
     * @param uuid The player's UUID
     */
    public void removeSession(UUID uuid) {
        sessions.remove(uuid);
    }

    /**
     * Checks if a player has an active session.
     *
     * @param uuid The player's UUID
     * @return true if the player has an active session, false otherwise
     */
    public boolean hasSession(UUID uuid) {
        Session session = sessions.get(uuid);
        if (session == null) {
            return false;
        }

        // Check if the session has expired
        long sessionTimeout = plugin.getConfig().getLong("settings.session-timeout", 1440) * 60 * 1000; // Convert minutes to milliseconds
        if (System.currentTimeMillis() - session.getCreationTime() > sessionTimeout) {
            sessions.remove(uuid);
            return false;
        }

        return true;
    }

    /**
     * Gets a player's session.
     *
     * @param uuid The player's UUID
     * @return The session, or null if the player doesn't have an active session
     */
    public Session getSession(UUID uuid) {
        return sessions.get(uuid);
    }

    /**
     * Records a failed login attempt.
     *
     * @param uuid The player's UUID
     * @return The number of attempts remaining before timeout
     */
    public int recordFailedLoginAttempt(UUID uuid) {
        LoginAttempt attempt = loginAttempts.computeIfAbsent(uuid, k -> new LoginAttempt());
        attempt.incrementAttempts();

        int maxAttempts = plugin.getConfig().getInt("settings.max-login-attempts", 5);
        return maxAttempts - attempt.getAttempts();
    }

    /**
     * Checks if a player has exceeded the maximum number of login attempts.
     *
     * @param uuid The player's UUID
     * @return true if the player has exceeded the maximum number of login attempts, false otherwise
     */
    public boolean hasExceededLoginAttempts(UUID uuid) {
        LoginAttempt attempt = loginAttempts.get(uuid);
        if (attempt == null) {
            return false;
        }

        int maxAttempts = plugin.getConfig().getInt("settings.max-login-attempts", 5);
        if (attempt.getAttempts() >= maxAttempts) {
            // Check if the timeout has expired
            long attemptTimeout = plugin.getConfig().getLong("settings.login-attempt-timeout", 10) * 60 * 1000; // Convert minutes to milliseconds
            if (System.currentTimeMillis() - attempt.getLastAttemptTime() > attemptTimeout) {
                // Reset attempts if timeout has expired
                loginAttempts.remove(uuid);
                return false;
            }
            return true;
        }

        return false;
    }

    /**
     * Resets a player's login attempts.
     *
     * @param uuid The player's UUID
     */
    public void resetLoginAttempts(UUID uuid) {
        loginAttempts.remove(uuid);
    }

    /**
     * Records a registration for an IP address.
     *
     * @param ip The IP address
     * @return true if the IP has not exceeded the registration limit, false otherwise
     */
    public boolean recordIpRegistration(String ip) {
        int count = ipRegistrations.getOrDefault(ip, 0) + 1;
        int limit = plugin.getConfig().getInt("settings.register-ip-limit", 3);

        if (count > limit) {
            return false;
        }

        ipRegistrations.put(ip, count);
        return true;
    }

    /**
     * Gets the number of registrations for an IP address.
     *
     * @param ip The IP address
     * @return The number of registrations
     */
    public int getIpRegistrations(String ip) {
        return ipRegistrations.getOrDefault(ip, 0);
    }

    /**
     * Stores a verification code for a player.
     *
     * @deprecated This method is used for the manual wallet connection method which is being phased out.
     *             It is kept for backward compatibility but will be removed in a future version.
     *             Use the QR code or browser extension connection methods instead.
     *
     * @param uuid The player's UUID
     * @param code The verification code
     */
    @Deprecated
    public void storeVerificationCode(UUID uuid, String code) {
        verificationCodes.put(uuid, code);
    }

    /**
     * Verifies a code for a player.
     *
     * @deprecated This method is used for the manual wallet connection method which is being phased out.
     *             It is kept for backward compatibility but will be removed in a future version.
     *             Use the QR code or browser extension connection methods instead.
     *
     * @param uuid The player's UUID
     * @param code The code to verify
     * @return true if the code is valid, false otherwise
     */
    @Deprecated
    public boolean verifyCode(UUID uuid, String code) {
        String storedCode = verificationCodes.get(uuid);
        if (storedCode != null && storedCode.equals(code)) {
            verificationCodes.remove(uuid);
            return true;
        }
        return false;
    }

    /**
     * Removes a verification code for a player.
     *
     * @deprecated This method is used for the manual wallet connection method which is being phased out.
     *             It is kept for backward compatibility but will be removed in a future version.
     *             Use the QR code or browser extension connection methods instead.
     *
     * @param uuid The player's UUID
     */
    @Deprecated
    public void removeVerificationCode(UUID uuid) {
        verificationCodes.remove(uuid);
    }

    /**
     * Generates a secure nonce for wallet authentication.
     *
     * @param uuid The player's UUID
     * @return The generated nonce
     */
    public String generateAuthNonce(UUID uuid) {
        // Generate a random nonce (12 characters)
        StringBuilder nonce = new StringBuilder();
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        java.security.SecureRandom random = new java.security.SecureRandom();
        for (int i = 0; i < 12; i++) {
            nonce.append(chars.charAt(random.nextInt(chars.length())));
        }

        String nonceStr = nonce.toString();
        authNonces.put(uuid, nonceStr);
        return nonceStr;
    }

    /**
     * Verifies an authentication nonce.
     *
     * @param uuid The player's UUID
     * @param nonce The nonce to verify
     * @return true if the nonce is valid, false otherwise
     */
    public boolean verifyAuthNonce(UUID uuid, String nonce) {
        String storedNonce = authNonces.get(uuid);
        if (storedNonce != null && storedNonce.equals(nonce)) {
            authNonces.remove(uuid);
            return true;
        }
        return false;
    }

    /**
     * Stores an authentication session ID.
     *
     * @param uuid The player's UUID
     * @param sessionId The session ID
     */
    public void storeAuthSession(UUID uuid, String sessionId) {
        authSessions.put(uuid, sessionId);
    }

    /**
     * Gets an authentication session ID.
     *
     * @param uuid The player's UUID
     * @return The session ID, or null if not found
     */
    public String getAuthSession(UUID uuid) {
        return authSessions.get(uuid);
    }

    /**
     * Removes an authentication session ID.
     *
     * @param uuid The player's UUID
     */
    public void removeAuthSession(UUID uuid) {
        authSessions.remove(uuid);
    }

    /**
     * Class representing a player session.
     */
    public static class Session {
        private final UUID uuid;
        private final String ip;
        private final long creationTime;
        private boolean authenticated;
        private boolean walletVerified;

        public Session(UUID uuid, String ip) {
            this.uuid = uuid;
            this.ip = ip;
            this.creationTime = System.currentTimeMillis();
            this.authenticated = false;
            this.walletVerified = false;
        }

        public UUID getUuid() {
            return uuid;
        }

        public String getIp() {
            return ip;
        }

        public long getCreationTime() {
            return creationTime;
        }

        public boolean isAuthenticated() {
            return authenticated;
        }

        public void setAuthenticated(boolean authenticated) {
            this.authenticated = authenticated;
        }

        public boolean isWalletVerified() {
            return walletVerified;
        }

        public void setWalletVerified(boolean walletVerified) {
            this.walletVerified = walletVerified;
        }
    }

    /**
     * Class representing login attempts.
     */
    private static class LoginAttempt {
        private int attempts;
        private long lastAttemptTime;

        public LoginAttempt() {
            this.attempts = 0;
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public int getAttempts() {
            return attempts;
        }

        public void incrementAttempts() {
            this.attempts++;
            this.lastAttemptTime = System.currentTimeMillis();
        }

        public long getLastAttemptTime() {
            return lastAttemptTime;
        }
    }
}
