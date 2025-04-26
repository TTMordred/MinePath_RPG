package com.example.lootbox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Utility class for handling NFT lootbox rewards
 */
public class NFTLootBoxUtil {
    private final LootBoxPlugin plugin;
    private final Map<String, Map<String, Integer>> tierRates = new HashMap<>();
    private final Map<String, Map<String, Integer>> nftRates = new HashMap<>();
    private final Random random = new Random();

    /**
     * Constructor
     * @param plugin The LootBoxPlugin instance
     */
    public NFTLootBoxUtil(LootBoxPlugin plugin) {
        this.plugin = plugin;
        loadRates();
    }

    /**
     * Load the tier and NFT rates from the config
     */
    private void loadRates() {
        // Load tier rates for each lootbox type
        ConfigurationSection nftLootboxSection = plugin.getConfig().getConfigurationSection("nft_lootbox");
        if (nftLootboxSection == null) {
            plugin.getLogger().severe("NFT lootbox configuration not found in config.yml");
            return;
        }

        // Load tier rates for basic lootbox
        loadTierRates("basic", nftLootboxSection);
        loadTierRates("premium", nftLootboxSection);
        loadTierRates("ultimate", nftLootboxSection);

        // Load NFT rates for each tier
        ConfigurationSection tiersSection = nftLootboxSection.getConfigurationSection("tiers");
        if (tiersSection == null) {
            plugin.getLogger().severe("NFT tiers configuration not found in config.yml");
            return;
        }

        // Load NFT rates for each tier
        loadNFTRates("common", tiersSection);
        loadNFTRates("rare", tiersSection);
        loadNFTRates("epic", tiersSection);
        loadNFTRates("legendary", tiersSection);
        loadNFTRates("mythic", tiersSection);
    }

    /**
     * Load tier rates for a specific lootbox type
     * @param type The lootbox type
     * @param section The configuration section
     */
    private void loadTierRates(String type, ConfigurationSection section) {
        ConfigurationSection typeSection = section.getConfigurationSection(type);
        if (typeSection == null) {
            plugin.getLogger().severe("NFT lootbox type " + type + " not found in config.yml");
            return;
        }

        Map<String, Integer> rates = new HashMap<>();
        for (String tier : typeSection.getKeys(false)) {
            rates.put(tier, typeSection.getInt(tier));
        }
        tierRates.put(type, rates);
    }

    /**
     * Load NFT rates for a specific tier
     * @param tier The tier
     * @param section The configuration section
     */
    private void loadNFTRates(String tier, ConfigurationSection section) {
        ConfigurationSection tierSection = section.getConfigurationSection(tier);
        if (tierSection == null) {
            plugin.getLogger().severe("NFT tier " + tier + " not found in config.yml");
            return;
        }

        Map<String, Integer> rates = new HashMap<>();
        for (String nft : tierSection.getKeys(false)) {
            rates.put(nft, tierSection.getInt(nft));
        }
        nftRates.put(tier, rates);
    }

    /**
     * Get a random tier based on the lootbox type
     * @param type The lootbox type (basic, premium, ultimate)
     * @return The selected tier
     */
    public String getRandomTier(String type) {
        Map<String, Integer> rates = tierRates.get(type);
        if (rates == null || rates.isEmpty()) {
            plugin.getLogger().warning("No tier rates found for lootbox type: " + type);
            return "common"; // Default to common if no rates found
        }

        // Calculate total weight
        int totalWeight = rates.values().stream().mapToInt(Integer::intValue).sum();
        
        // Generate a random number between 0 and totalWeight
        int randomValue = random.nextInt(totalWeight);
        
        // Select a tier based on the random number
        int cumulativeWeight = 0;
        for (Map.Entry<String, Integer> entry : rates.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }
        
        // Default to the last tier if something goes wrong
        return rates.keySet().iterator().next();
    }

    /**
     * Get a random NFT metadata key based on the tier
     * @param tier The tier (common, rare, epic, legendary, mythic)
     * @return The selected NFT metadata key
     */
    public String getRandomNFT(String tier) {
        Map<String, Integer> rates = nftRates.get(tier);
        if (rates == null || rates.isEmpty()) {
            plugin.getLogger().warning("No NFT rates found for tier: " + tier);
            return "lucky_charm_1"; // Default to lucky_charm_1 if no rates found
        }

        // Calculate total weight
        int totalWeight = rates.values().stream().mapToInt(Integer::intValue).sum();
        
        // Generate a random number between 0 and totalWeight
        int randomValue = random.nextInt(totalWeight);
        
        // Select an NFT based on the random number
        int cumulativeWeight = 0;
        for (Map.Entry<String, Integer> entry : rates.entrySet()) {
            cumulativeWeight += entry.getValue();
            if (randomValue < cumulativeWeight) {
                return entry.getKey();
            }
        }
        
        // Default to the first NFT if something goes wrong
        return rates.keySet().iterator().next();
    }

    /**
     * Create an NFT lootbox item
     * @param type The lootbox type (basic_nft, premium_nft, ultimate_nft)
     * @param amount The amount of lootboxes
     * @return The lootbox item
     */
    public ItemStack createNFTLootbox(String type, int amount) {
        ItemStack box = new ItemStack(Material.ENDER_CHEST, amount);
        ItemMeta meta = box.getItemMeta();
        
        String displayName;
        List<String> lore = new ArrayList<>();
        
        switch (type) {
            case "basic_nft":
                displayName = ChatColor.GREEN + "Basic NFT Lootbox";
                lore.add(ChatColor.GRAY + "Contains a random NFT item");
                lore.add(ChatColor.GRAY + "Tier chances:");
                lore.add(ChatColor.GREEN + "  Common: 80%");
                lore.add(ChatColor.BLUE + "  Rare: 15%");
                lore.add(ChatColor.DARK_PURPLE + "  Epic: 4%");
                lore.add(ChatColor.GOLD + "  Legendary: 0.9%");
                lore.add(ChatColor.LIGHT_PURPLE + "  Mythic: 0.1%");
                break;
            case "premium_nft":
                displayName = ChatColor.BLUE + "Premium NFT Lootbox";
                lore.add(ChatColor.GRAY + "Contains a random NFT item");
                lore.add(ChatColor.GRAY + "Tier chances:");
                lore.add(ChatColor.GREEN + "  Common: 50%");
                lore.add(ChatColor.BLUE + "  Rare: 35%");
                lore.add(ChatColor.DARK_PURPLE + "  Epic: 10%");
                lore.add(ChatColor.GOLD + "  Legendary: 4%");
                lore.add(ChatColor.LIGHT_PURPLE + "  Mythic: 1%");
                break;
            case "ultimate_nft":
                displayName = ChatColor.GOLD + "Ultimate NFT Lootbox";
                lore.add(ChatColor.GRAY + "Contains a random NFT item");
                lore.add(ChatColor.GRAY + "Tier chances:");
                lore.add(ChatColor.GREEN + "  Common: 30%");
                lore.add(ChatColor.BLUE + "  Rare: 40%");
                lore.add(ChatColor.DARK_PURPLE + "  Epic: 20%");
                lore.add(ChatColor.GOLD + "  Legendary: 8%");
                lore.add(ChatColor.LIGHT_PURPLE + "  Mythic: 2%");
                break;
            default:
                displayName = ChatColor.RED + "Unknown NFT Lootbox";
                lore.add(ChatColor.GRAY + "Contains a random NFT item");
                break;
        }
        
        meta.setDisplayName(displayName);
        meta.setLore(lore);
        
        // Add persistent data to identify this as an NFT lootbox
        meta.getPersistentDataContainer().set(
            plugin.getKey("nft-lootbox-type"),
            PersistentDataType.STRING,
            type
        );
        
        box.setItemMeta(meta);
        return box;
    }

    /**
     * Get the lootbox type from the NFT lootbox type
     * @param nftLootboxType The NFT lootbox type (basic_nft, premium_nft, ultimate_nft)
     * @return The lootbox type (basic, premium, ultimate)
     */
    public String getLootboxType(String nftLootboxType) {
        switch (nftLootboxType) {
            case "basic_nft":
                return "basic";
            case "premium_nft":
                return "premium";
            case "ultimate_nft":
                return "ultimate";
            default:
                return "basic";
        }
    }

    /**
     * Get the color for a tier
     * @param tier The tier
     * @return The color
     */
    public ChatColor getTierColor(String tier) {
        switch (tier) {
            case "common":
                return ChatColor.GREEN;
            case "rare":
                return ChatColor.BLUE;
            case "epic":
                return ChatColor.DARK_PURPLE;
            case "legendary":
                return ChatColor.GOLD;
            case "mythic":
                return ChatColor.LIGHT_PURPLE;
            default:
                return ChatColor.WHITE;
        }
    }
}
