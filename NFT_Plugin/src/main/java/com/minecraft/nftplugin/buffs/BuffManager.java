package com.minecraft.nftplugin.buffs;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import com.minecraft.nftplugin.database.NFTData;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Manages buffs from NFT items
 */
public class BuffManager {

    private final NFTPlugin plugin;
    private final NamespacedKey buffTypeKey;
    private final NamespacedKey buffValueKey;

    // Patterns to extract buff values from lore
    private final Pattern luckPattern = Pattern.compile("§b- Luck: §7\\+(\\d+)% chance");
    private final Pattern luckyCharmPattern = Pattern.compile("Lucky Charm \\+(\\d+)%");
    private final Pattern luckyCharmSimplePattern = Pattern.compile("\\+(\\d+)%");

    // Map to store player buffs
    private final Map<UUID, Map<BuffType, Integer>> playerBuffs = new HashMap<>();

    public BuffManager(NFTPlugin plugin) {
        this.plugin = plugin;
        this.buffTypeKey = new NamespacedKey(plugin, "buff_type");
        this.buffValueKey = new NamespacedKey(plugin, "buff_value");
    }

    /**
     * Get the total buff value for a player
     * @param player The player
     * @param buffType The type of buff
     * @return The total buff value
     */
    public int getPlayerBuffValue(Player player, BuffType buffType) {
        Map<BuffType, Integer> buffs = playerBuffs.getOrDefault(player.getUniqueId(), new HashMap<>());
        return buffs.getOrDefault(buffType, 0);
    }

    /**
     * Update buffs for a player based on their inventory
     * @param player The player
     */
    public void updatePlayerBuffs(Player player) {
        // Clear existing buffs for this player
        playerBuffs.put(player.getUniqueId(), new HashMap<>());

        // Check only items in the player's main inventory and armor slots
        // This excludes items in ender chest, chests, etc.
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && plugin.getItemManager().isNftItem(item)) {
                addBuffFromItem(player, item);
            }
        }

        // Also check armor slots
        for (ItemStack item : player.getInventory().getArmorContents()) {
            if (item != null && plugin.getItemManager().isNftItem(item)) {
                addBuffFromItem(player, item);
            }
        }

        // Check equipped NFTs from NFTInventoryCommand
        if (player.hasMetadata("nft_equipped_nfts")) {
            try {
                @SuppressWarnings("unchecked")
                Set<String> equippedNFTs = (Set<String>) player.getMetadata("nft_equipped_nfts").get(0).value();
                if (equippedNFTs != null && !equippedNFTs.isEmpty()) {
                    addBuffsFromEquippedNFTs(player, equippedNFTs);
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to get equipped NFTs for player " + player.getName() + ": " + e.getMessage());
            }
        }
    }

    /**
     * Add a buff from an item to a player
     * @param player The player
     * @param item The item
     */
    private void addBuffFromItem(Player player, ItemStack item) {
        if (item == null || !item.hasItemMeta()) {
            return;
        }

        ItemMeta meta = item.getItemMeta();

        // First check persistent data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        String buffTypeStr = container.get(buffTypeKey, PersistentDataType.STRING);
        Integer buffValue = container.get(buffValueKey, PersistentDataType.INTEGER);

        if (buffTypeStr != null && buffValue != null) {
            try {
                BuffType buffType = BuffType.valueOf(buffTypeStr);
                addBuff(player, buffType, buffValue);
                return;
            } catch (IllegalArgumentException e) {
                // Invalid buff type, continue to check lore
            }
        }

        // Check if this is a Lucky Charm item by achievement key
        String achievementKey = plugin.getItemManager().getAchievementKey(item);
        if (achievementKey != null && achievementKey.startsWith("lucky_charm_")) {
            try {
                // Extract the number from lucky_charm_X
                int value = Integer.parseInt(achievementKey.substring("lucky_charm_".length()));
                plugin.getLogger().info("Found Lucky Charm with value: " + value + " for player " + player.getName());
                addBuff(player, BuffType.LUCK, value);
                return;
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid Lucky Charm format: " + achievementKey);
            }
        }

        // If not found in persistent data or achievement key, check lore
        if (meta.hasLore()) {
            for (String line : meta.getLore()) {
                // Check for luck buff in standard format
                Matcher luckMatcher = luckPattern.matcher(line);
                if (luckMatcher.find()) {
                    int value = Integer.parseInt(luckMatcher.group(1));
                    addBuff(player, BuffType.LUCK, value);
                    continue;
                }

                // Check for Lucky Charm format
                Matcher charmMatcher = luckyCharmPattern.matcher(line);
                if (charmMatcher.find()) {
                    int value = Integer.parseInt(charmMatcher.group(1));
                    addBuff(player, BuffType.LUCK, value);
                    continue;
                }

                // Check for simple +X% format
                Matcher simpleMatcher = luckyCharmSimplePattern.matcher(line);
                if (simpleMatcher.find() && line.toLowerCase().contains("luck")) {
                    int value = Integer.parseInt(simpleMatcher.group(1));
                    addBuff(player, BuffType.LUCK, value);
                }
            }
        }
    }

    /**
     * Add buffs from equipped NFTs
     * @param player The player
     * @param equippedNFTs The set of equipped NFT IDs
     */
    public void addBuffsFromEquippedNFTs(Player player, Set<String> equippedNFTs) {
        if (equippedNFTs == null || equippedNFTs.isEmpty()) {
            return;
        }

        for (String nftId : equippedNFTs) {
            NFTData nftData = plugin.getDatabaseManager().getNFTById(nftId);
            if (nftData != null) {
                String achievementKey = nftData.getAchievementKey();

                // Check if it's a Lucky Charm
                if (achievementKey != null && achievementKey.startsWith("lucky_charm_")) {
                    try {
                        // Extract the number from lucky_charm_X
                        int value = Integer.parseInt(achievementKey.substring("lucky_charm_".length()));
                        plugin.getLogger().info("Adding buff from equipped Lucky Charm: " + value + " for player " + player.getName());
                        addBuff(player, BuffType.LUCK, value);
                    } catch (NumberFormatException e) {
                        plugin.getLogger().warning("Invalid Lucky Charm format: " + achievementKey);
                    }
                }
            }
        }
    }

    /**
     * Add a buff to a player
     * @param player The player
     * @param buffType The type of buff
     * @param value The value of the buff
     */
    private void addBuff(Player player, BuffType buffType, int value) {
        Map<BuffType, Integer> buffs = playerBuffs.getOrDefault(player.getUniqueId(), new HashMap<>());
        int currentValue = buffs.getOrDefault(buffType, 0);
        buffs.put(buffType, currentValue + value);
        playerBuffs.put(player.getUniqueId(), buffs);
    }

    /**
     * Apply buff to an item
     * @param item The item
     * @param buffType The type of buff
     * @param value The value of the buff
     * @return The buffed item
     */
    public ItemStack applyBuff(ItemStack item, BuffType buffType, int value) {
        if (item == null || !item.hasItemMeta()) {
            return item;
        }

        ItemMeta meta = item.getItemMeta();

        // Store in persistent data
        PersistentDataContainer container = meta.getPersistentDataContainer();
        container.set(buffTypeKey, PersistentDataType.STRING, buffType.name());
        container.set(buffValueKey, PersistentDataType.INTEGER, value);

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Get a map of all player buffs
     * @return A map of player UUIDs to their buffs
     */
    public Map<UUID, Map<BuffType, Integer>> getAllPlayerBuffs() {
        return new HashMap<>(playerBuffs);
    }

    /**
     * Update buffs for all online players
     */
    public void updateAllPlayerBuffs() {
        plugin.getServer().getOnlinePlayers().forEach(this::updatePlayerBuffs);
    }
}
