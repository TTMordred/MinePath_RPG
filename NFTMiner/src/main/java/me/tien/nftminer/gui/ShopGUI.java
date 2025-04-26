package me.tien.nftminer.gui;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;
import me.tien.nftminer.upgrade.SpeedUpgrade;
import me.tien.nftminer.upgrade.TokenValueUpgrade;
import me.tien.nftminer.upgrade.InventoryUpgrade;
import me.tien.nftminer.upgrade.UpgradeManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ShopGUI {
    private final NFTMiner plugin;
    private final TokenManager tokenManager;
    private final SpeedUpgrade speedUpgrade;
    private final TokenValueUpgrade tokenValueUpgrade;
    private final InventoryUpgrade inventoryUpgrade;

    private static final int SPEED_UPGRADE_SLOT = 11;
    private static final int TOKEN_VALUE_UPGRADE_SLOT = 13;
    private static final int INVENTORY_UPGRADE_SLOT = 15;
    private static final int TOKEN_INFO_SLOT = 22;

    public ShopGUI(NFTMiner plugin, TokenManager tokenManager, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.speedUpgrade = upgradeManager.getSpeedUpgrade();
        this.tokenValueUpgrade = upgradeManager.getTokenValueUpgrade();
        this.inventoryUpgrade = upgradeManager.getInventoryUpgrade();
    }

    /** Mở GUI cửa hàng nâng cấp */
    public void openShop(Player player) {
        Inventory gui = Bukkit.createInventory(null, 27, "§6§lCửa hàng nâng cấp");

        gui.setItem(SPEED_UPGRADE_SLOT, createSpeedUpgradeItem(player));
        gui.setItem(TOKEN_VALUE_UPGRADE_SLOT, createTokenValueUpgradeItem(player));
        gui.setItem(INVENTORY_UPGRADE_SLOT, createInventoryUpgradeItem(player));
        gui.setItem(TOKEN_INFO_SLOT, createTokenInfoItem(player));

        fillDecoration(gui);
        player.openInventory(gui);
    }

    /** Xử lý khi click trong GUI */
    public void handleClick(Player player, int slot) {
        if (slot == SPEED_UPGRADE_SLOT) {
            handleSpeedUpgrade(player);
        } else if (slot == TOKEN_VALUE_UPGRADE_SLOT) {
            handleTokenValueUpgrade(player);
        } else if (slot == INVENTORY_UPGRADE_SLOT) {
            handleInventoryUpgrade(player);
        }
    }

    /** Nâng cấp tốc độ: trừ token, tăng level, cấp Haste ngay */
    public void handleSpeedUpgrade(Player player) {
        int level = speedUpgrade.getLevel(player);
        if (level >= speedUpgrade.getMaxLevel()) {
            player.sendMessage("§cBạn đã đạt cấp độ tối đa của nâng cấp tốc độ!");
            return;
        }
        int cost = speedUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cKhông xác định được chi phí nâng cấp!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cBạn không đủ token (cần §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (speedUpgrade.upgrade(player)) {
            // Cấp hiệu ứng Haste
            speedUpgrade.applyEffect(player);
            player.sendMessage("§aNâng cấp tốc độ thành công! Hiệu ứng Haste đã được áp dụng.");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cNâng cấp thất bại!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }

    /** Nâng cấp giá trị token */
    public void handleTokenValueUpgrade(Player player) {
        int level = tokenValueUpgrade.getLevel(player);
        if (level >= tokenValueUpgrade.getMaxLevel()) {
            player.sendMessage("§cBạn đã đạt cấp độ tối đa của nâng cấp giá trị token!");
            return;
        }
        int cost = tokenValueUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cKhông xác định được chi phí nâng cấp!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cBạn không đủ token (cần §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (tokenValueUpgrade.upgrade(player)) {
            player.sendMessage("§aNâng cấp giá trị token thành công!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cNâng cấp thất bại!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }

    /** Nâng cấp inventory */
    /** Nâng cấp inventory */
    private void handleInventoryUpgrade(Player player) {
        int level = inventoryUpgrade.getLevel(player);
        if (level >= inventoryUpgrade.getMaxLevel()) {
            player.sendMessage("§cBạn đã mở khóa toàn bộ inventory!");
            return;
        }
        int cost = inventoryUpgrade.getNextLevelCost(player);
        if (cost == -1) {
            player.sendMessage("§cKhông xác định được chi phí mở khóa!");
            return;
        }
        if (tokenManager.getTokens(player).intValue() < cost) {
            player.sendMessage("§cBạn không đủ token (cần §e" + cost + "§c)!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
            return;
        }

        if (inventoryUpgrade.upgrade(player)) {
            player.sendMessage("§aMở khóa hàng inventory thành công!");
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1.5f);
            openShop(player);
        } else {
            player.sendMessage("§cMở khóa thất bại!");
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 0.5f);
        }
    }
    /* =============== UI Helpers =============== */

    private ItemStack createSpeedUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLDEN_PICKAXE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eNâng cấp tốc độ đào");
        List<String> lore = new ArrayList<>();
        int level = speedUpgrade.getLevel(player);
        lore.add("§7Cấp hiện tại: §e" + level + "§7/§e" + speedUpgrade.getMaxLevel());
        int eff = speedUpgrade.getEffectLevel(level);
        lore.add("§7Hiệu ứng Haste: §e" + eff);
        if (level < speedUpgrade.getMaxLevel()) {
            int cost = speedUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Giá: §e" + cost + " token");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick để nâng cấp"
                    : "§cKhông đủ token");
        } else {
            lore.add("§a✓ Đạt tối đa");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTokenValueUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLD_INGOT);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eNâng cấp giá trị token");

        List<String> lore = new ArrayList<>();
        int level = tokenValueUpgrade.getLevel(player);
        int max = tokenValueUpgrade.getMaxLevel();
        lore.add("§7Cấp hiện tại: §e" + level + "§7/§e" + max);

        double multiplier = tokenValueUpgrade.getValueMultiplier(player);
        lore.add("§7Hệ số: §e" + multiplier + "x");

        if (level < max) {
            int cost = tokenValueUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Giá: §e" + cost + " token");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick để nâng cấp"
                    : "§cKhông đủ token");
        } else {
            lore.add("§a✓ Đạt tối đa");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createInventoryUpgradeItem(Player player) {
        ItemStack item = new ItemStack(Material.CHEST);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§eMở khóa inventory");
        List<String> lore = new ArrayList<>();
        int level = inventoryUpgrade.getLevel(player);
        lore.add("§7Hàng mở khóa: §e" + level + "§7/§e" + inventoryUpgrade.getMaxLevel());
        if (level < inventoryUpgrade.getMaxLevel()) {
            int cost = inventoryUpgrade.getNextLevelCost(player);
            lore.add("");
            lore.add("§7Giá: §e" + cost + " token");
            lore.add(tokenManager.getTokens(player).intValue() >= cost
                    ? "§aClick để mở khóa"
                    : "§cKhông đủ token");
        } else {
            lore.add("§a✓ Đã mở khóa tất cả");
        }
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createTokenInfoItem(Player player) {
        ItemStack item = new ItemStack(Material.GOLD_NUGGET);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§e§lToken của bạn");
        List<String> lore = new ArrayList<>();
        lore.add("§fSố token: §e" + tokenManager.getTokens(player));
        lore.add("");
        lore.add("§7Đào quặng để nhận token");
        lore.add("§7Dùng để nâng cấp");
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