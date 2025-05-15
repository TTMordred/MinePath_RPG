package me.tien.miner_simulator.world;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;

public class MineProtectionListener implements Listener {
    private final Plugin plugin;
    private final VoidMine voidMine;
    private final Map<String, MineRegion> regions = new HashMap<>();
    private final Map<String, Map<String, MineRegion>> worldRegions = new HashMap<>();

    public MineProtectionListener(Plugin plugin, VoidMine voidMine) {
        this.plugin = plugin;
        this.voidMine = voidMine;

        // Register listener with all priorities to ensure maximum protection
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // Log message
        plugin.getLogger().info("MineProtectionListener has been initialized");
    }

    // Simple region for area protection
    public static class MineRegion {
        private final String worldName;
        private final int minX, minY, minZ, maxX, maxY, maxZ;
        private final String name;
        private final boolean allowBreak;
        private final int priority;

        public MineRegion(String worldName, String name, int minX, int minY, int minZ, int maxX, int maxY, int maxZ,
                boolean allowBreak, int priority) {
            this.worldName = worldName;
            this.name = name;
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
            this.allowBreak = allowBreak;
            this.priority = priority;
        }

        public boolean isInRegion(Location loc) {
            if (!loc.getWorld().getName().equals(worldName))
                return false;
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();
            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        public String getName() {
            return name;
        }

        public boolean isAllowBreak() {
            return allowBreak;
        }

        public int getPriority() {
            return priority;
        }

        public String getWorldName() {
            return worldName;
        }

        @Override
        public String toString() {
            return String.format("Region[name=%s, world=%s, pos=(%d,%d,%d)-(%d,%d,%d), allowBreak=%b, priority=%d]",
                    name, worldName, minX, minY, minZ, maxX, maxY, maxZ, allowBreak, priority);
        }
    }

    // Add region to management list
    public void addRegion(MineRegion region) {
        regions.put(region.getName(), region);
        
        // Also add to world-specific region map for faster lookup
        String worldName = region.getWorldName();
        Map<String, MineRegion> worldMap = worldRegions.computeIfAbsent(worldName, k -> new HashMap<>());
        worldMap.put(region.getName(), region);
        
        plugin.getLogger().info("Added region: " + region.toString());

        // List current number of regions
        plugin.getLogger().info("Total regions currently: " + regions.size());

        // List all regions
        plugin.getLogger().info("List of all regions:");
        for (MineRegion r : regions.values()) {
            plugin.getLogger().info("- " + r.toString());
        }
    }

    // Remove region from management list
    public void removeRegion(String name) {
        MineRegion region = regions.remove(name);
        if (region != null) {
            // Remove from world-specific map too
            Map<String, MineRegion> worldMap = worldRegions.get(region.getWorldName());
            if (worldMap != null) {
                worldMap.remove(name);
            }
            plugin.getLogger().info("Removed region: " + region.toString());
        } else {
            plugin.getLogger().info("Region not found for removal: " + name);
        }

        // List current number of regions
        plugin.getLogger().info("Total regions currently: " + regions.size());
    }

    // Get highest priority region at location
    private MineRegion getHighestPriorityRegion(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return null;

        MineRegion highestRegion = null;
        int highestPriority = -1;

        // First check world-specific regions for performance
        Map<String, MineRegion> worldMap = worldRegions.get(loc.getWorld().getName());
        if (worldMap != null) {
            for (MineRegion region : worldMap.values()) {
                if (region.isInRegion(loc) && region.getPriority() > highestPriority) {
                    highestRegion = region;
                    highestPriority = region.getPriority();
                }
            }
        }

        return highestRegion;
    }

    // Check if it's a mine world
    private boolean isMineWorld(String worldName) {
        return worldName != null && worldName.startsWith("mine_");
    }

    // Enhanced schematic region check
    private boolean isInSchematicRegion(Location loc) {
        if (loc == null || loc.getWorld() == null)
            return false;

        String worldName = loc.getWorld().getName();
        if (!isMineWorld(worldName))
            return false;

        // Check world-specific regions first for performance
        Map<String, MineRegion> worldMap = worldRegions.get(worldName);
        if (worldMap != null) {
            for (MineRegion region : worldMap.values()) {
                if (region.getName().startsWith("schem_") && region.isInRegion(loc)) {
                    return true;
                }
            }
        }

        return false;
    }

    // Check if player is in their mining box
    private boolean isInMiningBox(Player player, Location loc) {
        if (loc == null || loc.getWorld() == null)
            return false;

        String worldName = loc.getWorld().getName();
        if (!isMineWorld(worldName))
            return false;

        // Check if player is the world owner first
        if (!isWorldOwner(player, worldName))
            return false;

        // Get player UUID in format for region name
        String playerUUID = player.getUniqueId().toString().replace("-", "");
        String boxRegionName = "box_" + playerUUID;

        // Check world-specific regions for mining box
        Map<String, MineRegion> worldMap = worldRegions.get(worldName);
        if (worldMap != null && worldMap.containsKey(boxRegionName)) {
            MineRegion boxRegion = worldMap.get(boxRegionName);
            if (boxRegion.isInRegion(loc)) {
                return true;
            }
        }

        // Check all regions as fallback
        for (MineRegion region : regions.values()) {
            if (region.getName().equals(boxRegionName) && region.isInRegion(loc)) {
                return true;
            }
        }

        return false;
    }

    // Check if player is world owner
    private boolean isWorldOwner(Player player, String worldName) {
        if (!isMineWorld(worldName))
            return false;

        // Get UUID from world name (mine_UUID)
        String worldUUID = worldName.substring(5);
        return player.getUniqueId().toString().replace("-", "").equals(worldUUID);
    }

    // Block break event handler
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String worldName = block.getWorld().getName();

        // Admin bypass
        if (player.isOp()) {
            plugin.getLogger().info("Admin " + player.getName() + " broke a block in " + worldName);
            return; // Allow admins to break blocks anywhere
        }

        // Only process in mine worlds
        if (!isMineWorld(worldName))
            return;

        // Log the event
        plugin.getLogger().info("BlockBreakEvent by " + player.getName() + " in world: " + worldName);

        // Check if in schematic region (highest priority protection)
        if (isInSchematicRegion(loc)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "⛔ You cannot break blocks in the schematic area!");
            plugin.getLogger().info("Blocked " + player.getName() + " from breaking block in schematic region");
            return;
        }

        // Check if player is the world owner and in their mining box
        if (isWorldOwner(player, worldName)) {
            if (isInMiningBox(player, loc)) {
                // Allow breaking in their mining box
                plugin.getLogger().info("Allowed " + player.getName() + " to break block in their mining box");
                return;
            } else {
                // Cancel if not in mining box
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You can only break blocks in your mining box!");
                plugin.getLogger().info("Blocked " + player.getName() + " from breaking block outside mining box");
                return;
            }
        } else {
            // Cancel if not the world owner
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You can only break blocks in your own mine world!");
            plugin.getLogger().info("Blocked " + player.getName() + " from breaking block in another player's mine world");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location loc = block.getLocation();
        String worldName = block.getWorld().getName();

        // Admin bypass
        if (player.isOp()) {
            return; // Allow admins to damage blocks anywhere
        }

        // Only process in mine worlds
        if (!isMineWorld(worldName))
            return;

        // Check if in schematic region (highest priority protection)
        if (isInSchematicRegion(loc)) {
            event.setCancelled(true);
            return;
        }

        // Check if player is the world owner and in their mining box
        if (isWorldOwner(player, worldName)) {
            if (!isInMiningBox(player, loc)) {
                // Cancel if not in mining box
                event.setCancelled(true);
                return;
            }
        } else {
            // Cancel if not the world owner
            event.setCancelled(true);
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String worldName = block.getWorld().getName();

        // Admin bypass
        if (player.isOp()) {
            plugin.getLogger().info("Admin " + player.getName() + " bypassing protection to place a block");
            return; // Allow admins to place blocks
        }

        // Debug log
        plugin.getLogger().info("BlockPlaceEvent by " + player.getName() + " in world: " + worldName);

        // Check if in mine world
        if (isMineWorld(worldName)) {
            // Block placing blocks in the entire mine world
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot place blocks in the mining area!");
            plugin.getLogger().info("Blocked placing block in mine world");
            return;
        }

        // Check specific region
        Location loc = block.getLocation();
        MineRegion region = getHighestPriorityRegion(loc);        // If in a protected area, cancel the block place event
        if (region != null) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Cannot place blocks in a protected area!");
            plugin.getLogger().info("Blocked placing block in region " + region.getName());
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        String worldName = event.getEntity().getWorld().getName();

        // Block TNT or Creeper explosions in all mine areas
        if (isMineWorld(worldName)) {
            event.setCancelled(true);
            event.blockList().clear();
            plugin.getLogger().info("Blocked explosion in mine world");
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity entity = event.getEntity();
        String worldName = entity.getWorld().getName();

        // Block mob spawning in all mine areas
        if (isMineWorld(worldName)) {
            // If not a player or dropped item
            if (entity.getType() != EntityType.PLAYER &&
                    entity.getType() != EntityType.DROPPED_ITEM) {
                event.setCancelled(true);
                plugin.getLogger().info("Blocked spawning " + entity.getType() + " in mine world");
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() == null)
            return;

        // Admin bypass
        if (player.isOp()) {
            return; // Allow admins to interact with blocks
        }

        Block block = event.getClickedBlock();
        Location loc = block.getLocation();
        String worldName = block.getWorld().getName();

        // Check schematic area with high priority
        if (isMineWorld(worldName) && isInSchematicRegion(loc)) {
            event.setCancelled(true);
            plugin.getLogger().info("Blocked interaction with block in schematic area");
            return;
        }

        // Check regions
        MineRegion region = getHighestPriorityRegion(loc);

        // If in protected area, block interaction with sensitive blocks
        if (region != null) {
            // Block interaction with blocks that can contain items
            Material type = block.getType();
            if (block.getState() instanceof InventoryHolder ||
                    type == Material.LEVER ||
                    type == Material.STONE_BUTTON ||
                    type.toString().contains("_BUTTON") ||
                    type.toString().contains("_PRESSURE_PLATE") ||
                    type == Material.TRIPWIRE) {
                event.setCancelled(true);
                player.sendMessage(ChatColor.RED + "You cannot interact with this block in a protected area!");
                plugin.getLogger().info("Blocked interaction with " + type + " in region " + region.getName());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        String worldName = event.getBlock().getWorld().getName();

        // Admin bypass
        if (player.isOp()) {
            return; // Allow admins to use buckets
        }

        // Block water/lava pouring in the entire mine world
        if (isMineWorld(worldName)) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot pour water/lava in the mining area!");
            plugin.getLogger().info("Blocked emptying bucket in mine world");
            return;
        }

        // Check region
        Location loc = event.getBlock().getLocation();
        MineRegion region = getHighestPriorityRegion(loc);

        if (region != null) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "You cannot pour water/lava in a protected area!");
            plugin.getLogger().info("Blocked emptying bucket in region " + region.getName());
        }
    }
}