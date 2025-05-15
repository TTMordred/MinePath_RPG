package me.tien.miner_simulator.upgrade;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;

public class InventoryUpgrade implements Upgrade {
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<Integer, BigDecimal> levelCosts = new HashMap<>();
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final UpgradeDataManager dataManager;

    public InventoryUpgrade(Miner_Simulator plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.dataManager = new UpgradeDataManager(plugin, "inventory");
        loadConfig();
    }

    @Override
    public void loadConfig() {
        // Load costs for each level from config
        levelCosts.put(1, BigDecimal.valueOf(plugin.getConfig().getDouble("upgrade.inventory.level1", 100)));
        levelCosts.put(2, BigDecimal.valueOf(plugin.getConfig().getDouble("upgrade.inventory.level2", 200)));
        levelCosts.put(3, BigDecimal.valueOf(plugin.getConfig().getDouble("upgrade.inventory.level3", 300)));
    }

    @Override
    public int getLevel(UUID uuid) {
        return playerLevels.getOrDefault(uuid, 0);
    }

    @Override
    public int getLevel(Player player) {
        return getLevel(player.getUniqueId());
    }

    @Override
    public void setLevel(UUID uuid, int level) {
        int finalLevel = Math.min(level, getMaxLevel());
        playerLevels.put(uuid, finalLevel);
        dataManager.setPlayerLevel(uuid, finalLevel); // Save to player file
    }

    @Override
    public void setLevel(Player player, int level) {
        setLevel(player.getUniqueId(), level);
    }

    @Override
    public int getNextLevelCost(Player player) {
        int currentLevel = getLevel(player);
        BigDecimal cost = levelCosts.get(currentLevel + 1);
        return cost != null ? cost.intValue() : -1;
    }

    @Override
    public int getMaxLevel() {
        return 3; // Maximum 3 rows
    }

    @Override
    public String getType() {
        return "InventoryUpgrade";
    }

    @Override
    public void saveData() {
        // Save all player data from memory to file
        for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
            dataManager.setPlayerLevel(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void applyEffect(Player player) {
        List<Integer> lockedSlots = getLockedSlots(player);

        // Remove items from unlocked slots (if any)
        for (int i = 9; i <= 35; i++) {
            if (!lockedSlots.contains(i)) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && isLockBarrier(item)) {
                    player.getInventory().setItem(i, null);
                }
            }
        }

        // Place glass barriers in slots that are still locked
        for (int slot : lockedSlots) {
            player.getInventory().setItem(slot, createLockBarrier());
        }

        // Update inventory for player
        player.updateInventory();
    }

    @Override
    public void loadPlayerData(Player player) {
        UUID uuid = player.getUniqueId();
        int level = dataManager.getPlayerLevel(uuid);
        playerLevels.put(uuid, level);
    }

    @Override
    public int getEffectLevel(int level) {
        // Number of unlocked rows corresponds to level
        return level;
    }

    public List<Integer> getLockedSlots(Player player) {
        List<Integer> lockedSlots = new ArrayList<>();
        int level = getLevel(player);

        // First row (0-8) is hotbar, not locked
        // Lock second row (9-17) if level < 1
        // Lock third row (18-26) if level < 2
        // Lock last row (27-35) if level < 3

        if (level < 1) {
            for (int i = 9; i <= 17; i++) {
                lockedSlots.add(i);
            }
        }

        if (level < 2) {
            for (int i = 18; i <= 26; i++) {
                lockedSlots.add(i);
            }
        }

        if (level < 3) {
            for (int i = 27; i <= 35; i++) {
                lockedSlots.add(i);
            }
        }

        return lockedSlots;
    }

    public boolean upgrade(Player player) {
        int currentLevel = getLevel(player);
        if (currentLevel >= getMaxLevel()) {
            player.sendMessage("§c§lNotice: §r§cYou have unlocked maximum inventory rows!");
            return false;
        }

        BigDecimal cost = levelCosts.get(currentLevel + 1);
        if (cost == null || !tokenManager.removeTokens(player, cost)) {
            player.sendMessage(
                    "§c§lNotice: §r§cYou don't have enough tokens to unlock the next row. Required: " + cost.intValue()
                            + " tokens.");
            return false;
        }

        setLevel(player, currentLevel + 1);

        // Save data immediately to ensure synchronization
        dataManager.setPlayerLevel(player.getUniqueId(), currentLevel + 1);

        // Update inventory immediately to remove newly unlocked slots
        clearLockBarriersForNewLevel(player, currentLevel + 1);

        // Notify player
        int newLevel = currentLevel + 1;
        String rowMessage = newLevel == 1 ? "first row" : (newLevel == 2 ? "second row" : "last row");
        player.sendMessage("§a§lSuccess: §r§aUnlocked the " + rowMessage + " of your inventory!");
        player.sendMessage("§e§lNote: §r§eYou can now place items in the newly unlocked row.");

        return true;
    }

    /**
     * Remove all barriers in the newly unlocked row without adding new barriers
     * 
     * @param player   Player who needs barriers removed
     * @param newLevel New level (after upgrade)
     */
    private void clearLockBarriersForNewLevel(Player player, int newLevel) {
        // Identify the newly unlocked row
        int startSlot = -1;
        int endSlot = -1;

        if (newLevel == 1) {
            // First row (9-17) just unlocked
            startSlot = 9;
            endSlot = 17;
        } else if (newLevel == 2) {
            // Second row (18-26) just unlocked
            startSlot = 18;
            endSlot = 26;
        } else if (newLevel == 3) {
            // Last row (27-35) just unlocked
            startSlot = 27;
            endSlot = 35;
        }

        // Remove all barriers in the newly unlocked row
        if (startSlot != -1) {
            for (int i = startSlot; i <= endSlot; i++) {
                ItemStack item = player.getInventory().getItem(i);
                if (item != null && isLockBarrier(item)) {
                    player.getInventory().setItem(i, null);
                }
            }
            player.updateInventory();
        }

        // Place barriers back in slots that are still locked
        List<Integer> lockedSlots = getLockedSlots(player);
        for (int slot : lockedSlots) {
            player.getInventory().setItem(slot, createLockBarrier());
        }

        player.updateInventory();
    }

    private ItemStack createLockBarrier() {
        ItemStack barrier = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = barrier.getItemMeta();
        meta.setDisplayName("§c§lInventory Slot Locked");

        List<String> lore = new ArrayList<>();
        lore.add("§7This slot is locked.");
        lore.add("§7Unlock at §f/shop");
        lore.add("");
        lore.add("§eClick here to open upgrade shop.");

        meta.setLore(lore);
        barrier.setItemMeta(meta);

        return barrier;
    }

    public boolean isLockBarrier(ItemStack item) {
        if (item == null || item.getType() != Material.RED_STAINED_GLASS_PANE) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return false;
        }

        return meta.getDisplayName().equals("§c§lInventory Slot Locked");
    }
}