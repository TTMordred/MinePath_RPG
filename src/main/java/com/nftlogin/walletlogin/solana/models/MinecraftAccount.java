package com.nftlogin.walletlogin.solana.models;

import org.json.JSONObject;

/**
 * Represents a Minecraft account stored on the Solana blockchain.
 */
public class MinecraftAccount {

    private final String minecraftUuid;
    private final String walletAddress;
    private final boolean isVerified;
    private final long verificationTime;
    private final String authority;

    /**
     * Creates a new MinecraftAccount instance.
     *
     * @param minecraftUuid The Minecraft UUID
     * @param walletAddress The wallet address
     * @param isVerified Whether the wallet is verified
     * @param verificationTime The verification time
     * @param authority The authority public key
     */
    public MinecraftAccount(String minecraftUuid, String walletAddress, boolean isVerified, 
                           long verificationTime, String authority) {
        this.minecraftUuid = minecraftUuid;
        this.walletAddress = walletAddress;
        this.isVerified = isVerified;
        this.verificationTime = verificationTime;
        this.authority = authority;
    }

    /**
     * Creates a MinecraftAccount from a JSON object.
     *
     * @param json The JSON object
     * @return The MinecraftAccount
     */
    public static MinecraftAccount fromJson(JSONObject json) {
        return new MinecraftAccount(
                json.getString("minecraftUuid"),
                json.getString("walletAddress"),
                json.getBoolean("isVerified"),
                json.getLong("verificationTime"),
                json.getString("authority")
        );
    }

    /**
     * Converts this MinecraftAccount to a JSON object.
     *
     * @return The JSON object
     */
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("minecraftUuid", minecraftUuid);
        json.put("walletAddress", walletAddress);
        json.put("isVerified", isVerified);
        json.put("verificationTime", verificationTime);
        json.put("authority", authority);
        return json;
    }

    /**
     * Gets the Minecraft UUID.
     *
     * @return The Minecraft UUID
     */
    public String getMinecraftUuid() {
        return minecraftUuid;
    }

    /**
     * Gets the wallet address.
     *
     * @return The wallet address
     */
    public String getWalletAddress() {
        return walletAddress;
    }

    /**
     * Checks if the wallet is verified.
     *
     * @return true if the wallet is verified, false otherwise
     */
    public boolean isVerified() {
        return isVerified;
    }

    /**
     * Gets the verification time.
     *
     * @return The verification time
     */
    public long getVerificationTime() {
        return verificationTime;
    }

    /**
     * Gets the authority public key.
     *
     * @return The authority public key
     */
    public String getAuthority() {
        return authority;
    }
}
