package me.tien.nftminer.listeners;

import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.upgrade.InventoryUpgrade;
import me.tien.nftminer.upgrade.UpgradeManager;

public class InventoryListener implements Listener {
    private final NFTMiner plugin;
    private final InventoryUpgrade invUpgrade;

    public InventoryListener(NFTMiner plugin, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.invUpgrade = upgradeManager.getInventoryUpgrade();
        plugin.getLogger().info("[InventoryLock] InventoryListener initialized");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent e) {
        Player player = e.getPlayer();
        plugin.getLogger().info("[InventoryLock] Applying locks for player " + player.getName() + " on join");

        // Apply lock when player joins
        invUpgrade.applyEffect(player);

        // Notify player about locked slots
        if (player.hasPlayedBefore()) {
            player.sendMessage(
                    "§e§lThông báo: §r§eMột số ô trong túi đồ của bạn bị khóa. Sử dụng token để mở khóa tại §f/shop");
        } else {
            // First time player gets a more detailed message
            player.sendMessage("§e§l═══════════ Thông Báo ═══════════");
            player.sendMessage("§eChào mừng! Túi đồ của bạn bị giới hạn.");
            player.sendMessage("§eHãy kiếm token và nâng cấp tại §f/shop§e để mở khóa thêm ô.");
            player.sendMessage("§e§l═════════════════════════════════");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryOpen(InventoryOpenEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player player = (Player) e.getPlayer();
        plugin.getLogger().info("[InventoryLock] Applying locks for player " + player.getName() + " on inventory open");

        // Apply lock when player opens inventory
        invUpgrade.applyEffect(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryClose(InventoryCloseEvent e) {
        if (!(e.getPlayer() instanceof Player))
            return;
        Player player = (Player) e.getPlayer();
        plugin.getLogger().info("[InventoryLock] Moving items from locked slots for player " + player.getName());

        // Move any items from locked slots when inventory closes
        invUpgrade.moveItemsFromLockedSlots(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
public void onInventoryClick(InventoryClickEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
        return;
    Player player = (Player) e.getWhoClicked();

    // Kiểm tra nếu slot bị khóa
    if (e.getClickedInventory() == player.getInventory()) {
        List<Integer> lockedSlots = invUpgrade.getLockedSlots(player);
        if (lockedSlots.contains(e.getSlot())) {
            e.setCancelled(true);
            player.sendMessage("§c§lThông báo: §r§cÔ này đang bị khóa. Mở khóa tại §f/shop");
        }
    }
}

@EventHandler(priority = EventPriority.HIGHEST)
public void onInventoryDrag(InventoryDragEvent e) {
    if (!(e.getWhoClicked() instanceof Player))
        return;
    Player player = (Player) e.getWhoClicked();
    List<Integer> lockedSlots = invUpgrade.getLockedSlots(player);

    // Kiểm tra từng slot bị ảnh hưởng
    for (int slot : e.getRawSlots()) {
        if (lockedSlots.contains(slot)) {
            e.setCancelled(true);
            player.sendMessage("§c§lThông báo: §r§cBạn không thể kéo đồ vào ô bị khóa. Mở khóa tại §f/shop");
            return;
        }
    }
}

@EventHandler(priority = EventPriority.HIGHEST)
public void onPlayerPickupItem(EntityPickupItemEvent e) {
    if (!(e.getEntity() instanceof Player))
        return;
    Player player = (Player) e.getEntity();
    List<Integer> lockedSlots = invUpgrade.getLockedSlots(player);

    // Kiểm tra nếu túi đồ đầy và có ô bị khóa
    for (int slot : lockedSlots) {
        ItemStack item = player.getInventory().getItem(slot);
        if (item == null || item.getType() == Material.AIR) {
            e.setCancelled(true);
            player.sendMessage("§c§lThông báo: §r§cBạn không thể nhặt đồ vì có ô bị khóa trong túi đồ.");
            return;
        }
    }
}
}