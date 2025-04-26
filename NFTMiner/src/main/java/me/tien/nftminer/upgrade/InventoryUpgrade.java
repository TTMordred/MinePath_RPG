package me.tien.nftminer.upgrade;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;

public class InventoryUpgrade implements Upgrade {
    private final Map<UUID, Integer> playerLevels = new HashMap<>();
    private final Map<Integer, Integer> levelCosts = new HashMap<>();
    private final NFTMiner plugin;
    private final TokenManager tokenManager;

    public InventoryUpgrade(NFTMiner plugin, TokenManager tokenManager) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        loadConfig();
    }

    @Override
    public void loadConfig() {
        levelCosts.clear();
        levelCosts.put(1, 0); // Hàng 1: 50 token
        levelCosts.put(2, 10000); // Hàng 2: 100 token
        levelCosts.put(3, 20000); // Hàng 3: 150 token
        levelCosts.put(4, 50000); // Hàng 4: 200 token
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
        playerLevels.put(uuid, level);
        plugin.getConfig().set("player-data." + uuid + ".inventory-level", level);
        plugin.saveConfig();
    }

    @Override
    public void setLevel(Player player, int level) {
        setLevel(player.getUniqueId(), level);
    }

    @Override
    public int getNextLevelCost(Player player) {
        int currentLevel = getLevel(player);
        return levelCosts.getOrDefault(currentLevel + 1, -1);
    }

    @Override
    public int getMaxLevel() {
        return 3; // Tối đa 3 hàng
    }

    @Override
    public String getType() {
        return "InventoryUpgrade";
    }

    @Override
    public void saveData() {
        for (Map.Entry<UUID, Integer> entry : playerLevels.entrySet()) {
            plugin.getConfig().set("player-data." + entry.getKey() + ".inventory-level", entry.getValue());
        }
        plugin.saveConfig();
    }

    @Override
    public void applyEffect(Player player) {
        List<Integer> lockedSlots = getLockedSlots(player);
        int unlockedSlots = getEffectLevel(getLevel(player));

        // Xóa kính bị khóa ở các ô đã được mở khóa
        for (int i = 9; i < unlockedSlots; i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (isLockBarrier(item)) {
                player.getInventory().setItem(i, null); // Xóa kính bị khóa
            }
        }

        // Đặt kính bị khóa ở các ô vẫn còn bị khóa
        for (int slot : lockedSlots) {
            player.getInventory().setItem(slot, createLockBarrier());
        }
    }

    @Override
    public void loadPlayerData(Player player) {
        UUID id = player.getUniqueId();
        int level = plugin.getConfig().getInt("player-data." + id + ".inventory-level", 0);
        playerLevels.put(id, level);
    }

    @Override
    public int getEffectLevel(int level) {
        return level * 9; // Mỗi cấp độ mở khóa thêm 9 ô
    }

    public List<Integer> getLockedSlots(Player player) {
        List<Integer> lockedSlots = new ArrayList<>();
        int unlockedSlots = getEffectLevel(getLevel(player));

        // Chỉ khóa các slot từ 9 đến 35 (3 hàng trên)
        for (int i = 9; i < 36; i++) {
            if (i >= unlockedSlots) {
                lockedSlots.add(i);
            }
        }

        return lockedSlots;
    }

    public boolean upgrade(Player player) {
        int currentLevel = getLevel(player);
        int nextLevel = currentLevel + 1;

        if (nextLevel > getMaxLevel()) {
            player.sendMessage("§cBạn đã mở khóa toàn bộ inventory!");
            return false;
        }

        int cost = getNextLevelCost(player);
        if (cost == -1 || !tokenManager.hasEnoughTokens(player, cost)) {
            player.sendMessage("§cBạn không đủ token để mở khóa hàng tiếp theo!");
            return false;
        }

        tokenManager.removeTokens(player, BigDecimal.valueOf(cost));
        setLevel(player, nextLevel);
        applyEffect(player);
        player.sendMessage("§aMở khóa hàng inventory thành công!");
        return true;
    }

    private ItemStack createLockBarrier() {
        ItemStack item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        var meta = item.getItemMeta();
        meta.setDisplayName("§c§lÔ Bị Khóa");
        item.setItemMeta(meta);
        return item;
    }

    public boolean isLockBarrier(ItemStack item) {
        return item != null
                && item.getType() == Material.GRAY_STAINED_GLASS_PANE
                && item.hasItemMeta()
                && item.getItemMeta().hasDisplayName()
                && item.getItemMeta().getDisplayName().equals("§c§lÔ Bị Khóa");
    }

    public void moveItemsFromLockedSlots(Player player) {
        List<Integer> lockedSlots = getLockedSlots(player);
        List<ItemStack> itemsToMove = new ArrayList<>();

        // Lấy tất cả các vật phẩm từ các ô bị khóa
        for (int slot : lockedSlots) {
            ItemStack item = player.getInventory().getItem(slot);
            if (item != null && !isLockBarrier(item)) {
                itemsToMove.add(item.clone());
                player.getInventory().setItem(slot, null); // Xóa vật phẩm khỏi ô bị khóa
            }
        }

        // Di chuyển vật phẩm vào các ô trống trong inventory
        for (ItemStack item : itemsToMove) {
            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                // Nếu inventory đầy, thả vật phẩm ra ngoài
                for (ItemStack drop : leftover.values()) {
                    player.getWorld().dropItem(player.getLocation(), drop);
                    player.sendMessage("§e§lThông báo: §r§eTúi đồ đầy! Một số vật phẩm đã rơi xuống đất.");
                }
            }
        }
    }
}