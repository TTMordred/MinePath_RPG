package com.example.lootbox;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class LootBoxListener implements Listener {
    private final LootBoxPlugin plugin;

    public LootBoxListener(LootBoxPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
            // 1) Only proceed on right‑click with the main hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        ItemMeta meta = e.getItem().getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(
                plugin.getKey("lootbox-type"),
                PersistentDataType.STRING
        )) return;

        e.setCancelled(true);
        Player p = e.getPlayer();
        String type = meta.getPersistentDataContainer()
                          .get(plugin.getKey("lootbox-type"), PersistentDataType.STRING);

        // consume one
        ItemStack stack = e.getItem();
        stack.setAmount(stack.getAmount() - 1);

        // open rolling GUI
        Inventory inv = Bukkit.createInventory(null, 9, "Rolling " + type);
        ItemStack glass = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        p.openInventory(inv);
        p.playSound(
            p.getLocation(),
            "lootbox:rolling_music",
            SoundCategory.MUSIC,
            1.0f,
            1.0f
        );
        // animate & pick reward in the middle slot
        new BukkitRunnable() {
            int ticks = 0;
            @Override public void run() {
                ticks++;
                ItemStack reward = plugin.getRewardFor(type);
                inv.setItem(4, reward);
                if (ticks >= 60) {
                    p.stopSound("lootbox:rolling_music", SoundCategory.MUSIC);
                    p.closeInventory();
                    p.getInventory().addItem(reward);
                    p.sendMessage(ChatColor.GREEN + "You won: " +
                    reward.getItemMeta().getDisplayName());
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    @EventHandler
    public void onLootboxDrop(PlayerDropItemEvent e) {
        ItemStack dropped = e.getItemDrop().getItemStack();
        ItemMeta meta = dropped.getItemMeta();
        if (meta != null) {
            var pdc = meta.getPersistentDataContainer();
            boolean isBox    = pdc.has(plugin.getKey("lootbox-type"),   PersistentDataType.STRING);
            boolean isReward = pdc.has(plugin.getKey("lootbox-reward"), PersistentDataType.BYTE);
            if (isBox || isReward) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot drop that item!");
            }
        }
    }
    @EventHandler
    public void onRollingClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (title.startsWith("Rolling ")) {
            e.setCancelled(true);
        }
    }
    @EventHandler
    public void onRollingDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.startsWith("Rolling ")) {
            e.setCancelled(true);
        }
    }
}
