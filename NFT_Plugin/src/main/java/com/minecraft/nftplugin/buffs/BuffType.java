package com.minecraft.nftplugin.buffs;

/**
 * Types of buffs that can be applied to players
 */
public enum BuffType {
    LUCK("Luck"),
    MINING_SPEED("Mining Speed"),
    DAMAGE("Damage"),
    DEFENSE("Defense"),
    EXPERIENCE("Experience");
    
    private final String displayName;
    
    BuffType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get the display name of the buff type
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
}
