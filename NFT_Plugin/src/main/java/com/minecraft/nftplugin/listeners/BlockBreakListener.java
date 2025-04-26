package com.minecraft.nftplugin.listeners;

import com.minecraft.nftplugin.NFTPlugin;
import org.bukkit.event.Listener;

/**
 * Listener for block break events
 * Note: This listener is now empty as we only use named item achievements.
 */
public class BlockBreakListener implements Listener {

    private final NFTPlugin plugin;

    public BlockBreakListener(NFTPlugin plugin) {
        this.plugin = plugin;
    }

    // No event handlers needed
}
