package com.example.lootbox;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class ResourcePackListener implements Listener {
    private final LootBoxPlugin plugin;
    // your raw GitHub URL to pack.zip
    private static final String PACK_URL =
        "https://raw.githubusercontent.com/HungPhan-0612/rolling_music/main/pack.zip";

    public ResourcePackListener(LootBoxPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        // optionally: plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            p.setResourcePack(PACK_URL);
        // }, 20L);
    }
}
