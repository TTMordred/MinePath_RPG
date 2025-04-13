package com.nftlogin.walletlogin.solana;

/**
 * Simple representation of a Solana public key.
 */
public class PublicKey {
    private final String address;

    /**
     * Creates a new PublicKey instance.
     *
     * @param address The public key address
     */
    public PublicKey(String address) {
        this.address = address;
    }

    /**
     * Gets the public key address.
     *
     * @return The public key address
     */
    public String getAddress() {
        return address;
    }

    @Override
    public String toString() {
        return address;
    }
}
