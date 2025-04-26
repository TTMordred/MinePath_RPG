package me.tien.nftminer.listeners;

import me.tien.nftminer.gui.ShopGUI;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class ShopListener implements Listener {
    private final ShopGUI shopGUI;

    public ShopListener(ShopGUI shopGUI) {
        this.shopGUI = shopGUI;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getInventory();
        String title = event.getView().getTitle();

        // Check if this is the shop GUI
        if (title.equals("§6§lCửa hàng nâng cấp")) {
            event.setCancelled(true); // Prevent taking items from GUI

            // Process the click
            shopGUI.handleClick(player, event.getRawSlot());
        }
    }
}