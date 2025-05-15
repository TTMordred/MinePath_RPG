package me.tien.miner_simulator.gui;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;
import me.tien.miner_simulator.upgrade.SpeedUpgrade;
import me.tien.miner_simulator.upgrade.TokenValueUpgrade;
import me.tien.miner_simulator.upgrade.InventoryUpgrade;
import me.tien.miner_simulator.upgrade.UpgradeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final SpeedUpgrade speedUpgrade;
    private final TokenValueUpgrade tokenValueUpgrade;
    private final InventoryUpgrade inventoryUpgrade;

    private static final int SPEED_UPGRADE_SLOT = 11;
    private static final int TOKEN_VALUE_UPGRADE_SLOT = 13;
    private static final int INVENTORY_UPGRADE_SLOT = 15;
    private static final int TOKEN_INFO_SLOT = 22;

    public ShopGUI(Miner_Simulator plugin, TokenManager tokenManager, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.speedUpgrade = upgradeManager.getSpeedUpgrade();
        this.tokenValueUpgrade = upgradeManager.getTokenValueUpgrade();
        this.inventoryUpgrade = upgradeManager.getInventoryUpgrade();
    }

    /** Open upgrade shop GUI */
    public void openShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lUpgrade Shop");

        gui.setItem(SPEED_UPGRADE_SLOT, createSpeedUpgradeItem(player));
        gui.setItem(TOKEN_VALUE_UPGRADE_SLOT, createTokenValueUpgradeItem(player));
        gui.setItem(INVENTORY_UPGRADE_SLOT, createInventoryUpgradeItem(player));
        gui.setItem(TOKEN_INFO_SLOT, createTokenInfoItem(player));

        fillDecoration(gui);
        player.openInventory(gui);
    }

    /** Handle clicks in GUI */
    public void handleClick(Player player, int slot) {
        if (slot == SPEED_UPGRADE_SLOT) {
            handleSpeedUpgrade(player);
        } else if (slot == TOKEN_VALUE_UPGRADE_SLOT) {
            handleTokenValueUpgrade(player);
        } else if (slot == INVENTORY_UPGRADE_SLOT) {
            handleInventoryUpgrade(player);
        }
    }

    /** Speed upgrade: deduct tokens, increase level, apply Haste effect immediately */
    public void handleSpeedUpgrade(Player player) {
        int level = speedUpgrade.getLevel(player);
        if (level >= speedUpgrade.getMaxLevel()) {
            player.sendMessage("§cYou've reached the maximum level for speed upgrade!");
            return;
        }
        int cost = speedUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cCouldn't determine upgrade cost!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cNot enough tokens (need §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (speedUpgrade.upgrade(player)) {
            // Apply Haste effect
            speedUpgrade.applyEffect(player);
            player.sendMessage("§aSpeed upgrade successful! Haste effect has been applied.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cUpgrade failed!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }

    /** Token value upgrade */
    public void handleTokenValueUpgrade(Player player) {
        int level = tokenValueUpgrade.getLevel(player);
        if (level >= tokenValueUpgrade.getMaxLevel()) {
            player.sendMessage("§cYou've reached the maximum level for token value upgrade!");
            return;
        }
        int cost = tokenValueUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cCouldn't determine upgrade cost!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cNot enough tokens (need §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (tokenValueUpgrade.upgrade(player)) {
            player.sendMessage("§aToken value upgrade successful!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cUpgrade failed!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }

    /** Inventory upgrade */
    private void handleInventoryUpgrade(Player player) {
        int level = inventoryUpgrade.getLevel(player);
        if (level >= inventoryUpgrade.getMaxLevel()) {
            player.sendMessage("§cYou've unlocked the entire inventory!");
            return;
        }
        int cost = inventoryUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cCouldn't determine unlock cost!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cNot enough tokens (need §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (inventoryUpgrade.upgrade(player)) {
            player.sendMessage("§aInventory row unlock successful!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cUnlock failed!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }
    /* =============== UI Helpers =============== */

    private ItemStack createSpeedUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eMining Speed Upgrade");
        List<String> lore = new ArrayList<>();
        int level = speedUpgrade.getLevel(player);
        lore.add("§7Current level: §e" + level + "§7/§e" + speedUpgrade.getMaxLevel());
        int eff = speedUpgrade.getEffectLevel(level);
        lore.add("§7Haste effect: §e" + eff);
        if (level < speedUpgrade.getMaxLevel()) {
            int cost = speedUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Cost: §e" + cost + " tokens");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick to upgrade"
                    : "§cNot enough tokens");
        } else {
            lore.add("§a✓ Maximum level reached");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTokenValueUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eToken Value Upgrade");

        List<String> lore = new ArrayList<>();
        int level = tokenValueUpgrade.getLevel(player);
        int max = tokenValueUpgrade.getMaxLevel();
        lore.add("§7Current level: §e" + level + "§7/§e" + max);

        double multiplier = tokenValueUpgrade.getValueMultiplier(player);
        lore.add("§7Multiplier: §e" + multiplier + "x");

        if (level < max) {
            int cost = tokenValueUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Cost: §e" + cost + " tokens");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick to upgrade"
                    : "§cNot enough tokens");
        } else {
            lore.add("§a✓ Maximum level reached");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInventoryUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eUnlock Inventory");
        List<String> lore = new ArrayList<>();
        int level = inventoryUpgrade.getLevel(player);
        lore.add("§7Unlocked rows: §e" + level + "§7/§e" + inventoryUpgrade.getMaxLevel());
        if (level < inventoryUpgrade.getMaxLevel()) {
            int cost = inventoryUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Cost: §e" + cost + " tokens");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick to unlock"
                    : "§cNot enough tokens");
        } else {
            lore.add("§a✓ All rows unlocked");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTokenInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lYour Tokens");
        List<String> lore = new ArrayList<>();
        lore.add("§fTokens: §e" + tokenManager.getTokens(player));
        lore.add("");
        lore.add("§7Mine ores to get tokens");
        lore.add("§7Use them for upgrades");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void fillDecoration(Inventory gui) {
        ItemStack deco = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta dm = deco.getItemMeta();
        dm.setDisplayName(" ");
        deco.setItemMeta(dm);
        for (int i = 0; i < gui.getSize(); i++) {
            if (gui.getItem(i) == null)
                gui.setItem(i, deco);
        }
    }
}