package com.minecraft.nftplugin.database;

import java.util.Date;
import java.util.UUID;

/**
 * Class to store NFT data
 */
public class NFTData {
    private final int id;
    private final UUID uuid;
    private final String achievementKey;
    private final String nftId;
    private final String mintAddress;
    private final String transactionId;
    private final Date mintedAt;

    /**
     * Constructor
     * @param id The NFT ID in the database
     * @param uuid The player's UUID
     * @param achievementKey The achievement key
     * @param nftId The NFT ID
     * @param mintAddress The mint address
     * @param transactionId The transaction ID
     * @param mintedAt The date the NFT was minted
     */
    public NFTData(int id, UUID uuid, String achievementKey, String nftId, String mintAddress, String transactionId, Date mintedAt) {
        this.id = id;
        this.uuid = uuid;
        this.achievementKey = achievementKey;
        this.nftId = nftId;
        this.mintAddress = mintAddress;
        this.transactionId = transactionId;
        this.mintedAt = mintedAt;
    }

    /**
     * Get the NFT ID in the database
     * @return The NFT ID in the database
     */
    public int getId() {
        return id;
    }

    /**
     * Get the player's UUID
     * @return The player's UUID
     */
    public UUID getUuid() {
        return uuid;
    }

    /**
     * Get the achievement key
     * @return The achievement key
     */
    public String getAchievementKey() {
        return achievementKey;
    }

    /**
     * Get the NFT ID
     * @return The NFT ID
     */
    public String getNftId() {
        return nftId;
    }

    /**
     * Get the mint address
     * @return The mint address
     */
    public String getMintAddress() {
        return mintAddress;
    }

    /**
     * Get the transaction ID
     * @return The transaction ID
     */
    public String getTransactionId() {
        return transactionId;
    }

    /**
     * Get the date the NFT was minted
     * @return The date the NFT was minted
     */
    public Date getMintedAt() {
        return mintedAt;
    }

    /**
     * Get a formatted string of the minted date
     * @return The formatted date string
     */
    public String getFormattedTimestamp() {
        if (mintedAt == null) {
            return "Unknown";
        }
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(mintedAt);
    }
}
