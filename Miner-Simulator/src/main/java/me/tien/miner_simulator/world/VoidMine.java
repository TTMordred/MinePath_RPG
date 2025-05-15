package me.tien.miner_simulator.world;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.*;
import java.util.*;
import java.util.logging.Logger;

public class VoidMine implements Listener {
    private final Plugin plugin;
    private final Map<UUID, PlayerMine> playerMines = new HashMap<>();
    private final Map<OreType, Double> oreRates = new HashMap<>();
    private MineProtectionListener protectionListener;

    // Path to DeluxeHub config file
    private static final String DELUXEHUB_CONFIG_PATH = "plugins/DeluxeHub/config.yml";

    public VoidMine(Plugin plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();

        ConfigurationSection oreSection = config.getConfigurationSection("ore-rates");
        if (oreSection != null) {
            for (String key : oreSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double rate = oreSection.getDouble(key, 0.0);
                    if (rate > 0) {
                        oreRates.put(OreType.fromMaterial(material), Double.valueOf(rate));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in config: " + key);
                }
            }
        }

        if (oreRates.isEmpty()) {
            oreRates.put(OreType.STONE, Double.valueOf(0.7));
            oreRates.put(OreType.IRON_ORE, Double.valueOf(0.15));
            oreRates.put(OreType.GOLD_ORE, Double.valueOf(0.1));
            oreRates.put(OreType.DIAMOND_ORE, Double.valueOf(0.05));
        }

        // Initialize protection listener (create only, don't register)
        this.protectionListener = new MineProtectionListener(plugin, this);
        
        // Only register VoidMine listener, not MineProtectionListener (it's registered in its constructor)
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        plugin.getLogger().info("VoidMine has been successfully initialized");
    }

    // Getter for protection listener
    public MineProtectionListener getProtectionListener() {
        return protectionListener;
    }

    public class PlayerMine {
        private final UUID playerUUID;
        private final String playerName;
        private final String worldName;
        private static final int PASTE_X = 0;
        private static final int PASTE_Y = 10;
        private static final int PASTE_Z = 0;
        private World mineWorld;
        private Location spawnLocation;

        private int minX, minY, minZ, maxX, maxY, maxZ;

        public PlayerMine(Player player) {
            this.playerUUID = player.getUniqueId();
            this.playerName = player.getName();
            this.worldName = "mine_" + playerUUID.toString().replace("-", "");
            playerMines.put(playerUUID, this); // save the mine
            createMineWorld();
        }

        public void createMineWorld() {
            mineWorld = Bukkit.getWorld(worldName);
            if (mineWorld == null) {
                WorldCreator worldCreator = new WorldCreator("mines/" + worldName);
                worldCreator.generator(new VoidGenerator());
                worldCreator.generateStructures(false);
                mineWorld = worldCreator.createWorld();
                if (mineWorld != null) {
                    mineWorld.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                    mineWorld.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
                    mineWorld.setTime(6000);
                    mineWorld.setDifficulty(Difficulty.PEACEFUL);
                    mineWorld.setGameRule(GameRule.DO_MOB_SPAWNING, false);
                }
            }
            if (mineWorld != null) {
                // Set up safe settings for the world
                // Wait for the server to fully load the world before setting up
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    // Use the exact world name
                    String exactWorldName = mineWorld.getName();
                    plugin.getLogger().info("Setting up protection for world: " + exactWorldName);
                    
                    // Continue with schematic paste and mining box creation
                    File schematicFile = new File(plugin.getDataFolder(), "schematics/mine_template.schem");
                    Location pasteLocation = new Location(mineWorld, PASTE_X, PASTE_Y, PASTE_Z);
                    pasteSchematic(schematicFile, pasteLocation, exactWorldName);
                    
                    // Spawn position near the new mining box (-25,-11,-26) to (-7,-1,-8)
                    // Set spawn at a position near the mining area but not inside it
                    int spawnX = -28; // 3 blocks away from mining box in negative X
                    int spawnY = 1;   // 1 block above the bottom of mining box
                    int spawnZ = -16; // 3 blocks away in negative Z
                    spawnLocation = new Location(mineWorld, spawnX, spawnY, spawnZ);
                    mineWorld.setSpawnLocation(spawnLocation);
                    fillMiningBox(exactWorldName);
                    
                    // Teleport player to the mine after it's fully set up
                    Player player = Bukkit.getPlayer(playerUUID);
                    if (player != null) {
                        teleportPlayer(player);
                    }
                }, 20L); // Wait 1 second to ensure the world is fully loaded
            } else {
                getLogger().severe("Could not create or load world: " + worldName + " for player " + playerName);
            }
        }

        private void pasteSchematic(File schematicFile, Location location, String worldName) {
            try {
                // First, check if the file exists in the plugins folder
                if (!schematicFile.exists()) {
                    // If file not found, try to get it from inside the JAR
                    plugin.getLogger().info("Schematic file not found in plugin folder, trying resources...");
                }
                
                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    plugin.getLogger().warning("Could not find format for schematic: " + schematicFile.getName());
                    return;
                }
                Clipboard clipboard;
                try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                    clipboard = reader.read();
                }
                try (EditSession editSession = WorldEdit.getInstance()
                        .newEditSession(BukkitAdapter.adapt(mineWorld))) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(location.getBlockX(), location.getBlockY(), location.getBlockZ()))
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);
                }
                
                // Create schematic protection region
                try (ClipboardReader reader = ClipboardFormats.findByFile(schematicFile)
                        .getReader(new FileInputStream(schematicFile))) {
                    clipboard = reader.read();
                    BlockVector3 min = clipboard.getMinimumPoint();
                    BlockVector3 max = clipboard.getMaximumPoint();

                    String regionName = "schem_" + playerUUID.toString().replace("-", "");
                    
                    // Calculate exact coordinates for the region
                    int x1 = location.getBlockX() + min.getBlockX();
                    int y1 = location.getBlockY() + min.getBlockY();
                    int z1 = location.getBlockZ() + min.getBlockZ();
                    int x2 = location.getBlockX() + max.getBlockX();
                    int y2 = location.getBlockY() + max.getBlockY();
                    int z2 = location.getBlockZ() + max.getBlockZ();

                    // Extend protection area by 5 blocks for safety
                    x1 -= 5;
                    y1 -= 5;
                    z1 -= 5;
                    x2 += 5;
                    y2 += 5;
                    z2 += 5;

                    // Create schematic protection region with highest priority (100)
                    MineProtectionListener.MineRegion schemRegion = new MineProtectionListener.MineRegion(
                        worldName, regionName, x1, y1, z1, x2, y2, z2, false, 100
                    );
                    
                    // Remove old region if exists
                    protectionListener.removeRegion(regionName);
                    
                    // Add new region
                    protectionListener.addRegion(schemRegion);
                    
                    plugin.getLogger().info(String.format("Created schematic protection region %s from (%d,%d,%d) to (%d,%d,%d) with priority 100",
                            regionName, x1, y1, z1, x2, y2, z2));
                    
                    // Add a second region covering the entire world except mining box and spawn
                    String globalRegionName = "global_" + playerUUID.toString().replace("-", "");
                    
                    // Remove old region if exists
                    protectionListener.removeRegion(globalRegionName);
                    
                    // Create new global region
                    MineProtectionListener.MineRegion globalRegion = new MineProtectionListener.MineRegion(
                        worldName, globalRegionName, -1000, -64, -1000, 1000, 320, 1000, false, 5
                    );
                    protectionListener.addRegion(globalRegion);
                    
                    plugin.getLogger().info("Created global protection region for world: " + worldName);
                    
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not create schematic protection region: " + e.getMessage());
                }
            } catch (IOException | com.sk89q.worldedit.WorldEditException e) {
                plugin.getLogger().warning("Error when pasting schematic: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        private void fillMiningBox(String worldName) {
            // Use fixed coordinates instead of reading from config
            // Coordinates 1: (-25,-11,-26)
            // Coordinates 2: (-7,-1,-8)
            
            minX = -25;
            minY = -11;
            minZ = -26;
            
            // Calculate dimensions from given coordinates
            maxX = -7;
            maxY = -1;
            maxZ = -8;
            
            // Calculate dimensions for logging
            int width = maxX - minX;
            int height = maxY - minY;
            int length = maxZ - minZ;
            
            plugin.getLogger().info(String.format("Creating mining box for player %s from coordinates (%d,%d,%d) to (%d,%d,%d)", 
                    playerName, minX, minY, minZ, maxX, maxY, maxZ));
            
            // Set up region for mining box
            String regionName = "box_" + playerUUID.toString().replace("-", "");
            
            plugin.getLogger().info("Creating region for mining box: " + regionName);
            
            // Create region for mining box (allow block breaking)
            MineProtectionListener.MineRegion boxRegion = new MineProtectionListener.MineRegion(
                worldName, regionName, minX, minY, minZ, maxX, maxY, maxZ, true, 10
            );
            protectionListener.addRegion(boxRegion);

            // Fill blocks in the mining area
            Random random = new Random();
            int totalBlocks = 0;
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        OreType ore = getRandomOre(random);
                        mineWorld.getBlockAt(x, y, z).setType(ore.getMaterial());
                        totalBlocks++;
                    }
                }
            }
            
            plugin.getLogger().info(String.format("Created mining box with %d blocks for player %s", 
                    totalBlocks, playerName));
        }
        
        private OreType getRandomOre(Random random) {
            double value = random.nextDouble();
            double current = 0.0;
            for (Map.Entry<OreType, Double> entry : oreRates.entrySet()) {
                current += entry.getValue();
                if (value < current) return entry.getKey();
            }
            return OreType.STONE;
        }

        public void resetMiningBox() {
            Player player = Bukkit.getPlayer(playerUUID);
            if (player == null) return;
            
            // Teleport player to spawn
            teleportPlayer(player);
            
            // Display notification
            player.sendTitle(
                ChatColor.RED + "Resetting mining area", 
                ChatColor.YELLOW + "Please stand still for 5 seconds", 
                10, 70, 20
            );
            
            // Send warning message
            player.sendMessage(ChatColor.RED + "⚠ " + ChatColor.YELLOW + "Your mining area is being reset. Please stand still for 5 seconds.");
            
            // Register listener to prevent movement
            final UUID playerUUID = this.playerUUID;
            
            // Create a temporary event handler
            Listener moveListener = new Listener() {
                @org.bukkit.event.EventHandler
                public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
                    if (event.getPlayer().getUniqueId().equals(playerUUID)) {
                        // Only cancel if player changes position (not just looking around)
                        if (event.getFrom().getBlockX() != event.getTo().getBlockX() || 
                            event.getFrom().getBlockY() != event.getTo().getBlockY() || 
                            event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
                            
                            event.setCancelled(true);
                            event.getPlayer().sendActionBar(
                                ChatColor.RED + "Cannot move while mining area is being reset!"
                            );
                        }
                    }
                }
            };
            
            // Register the listener
            Bukkit.getPluginManager().registerEvents(moveListener, plugin);
            
            // Countdown and reset mining box
            final int[] countdown = {5};
            final int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
                if (countdown[0] > 0) {
                    player.sendActionBar(ChatColor.YELLOW + "Resetting mining area in " + ChatColor.RED + countdown[0] + ChatColor.YELLOW + " seconds...");
                    countdown[0]--;
                }
            }, 0L, 20L); // Run every second
            
            // After 5 seconds, cancel task and listener, proceed with mining box reset
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                // Cancel countdown task
                Bukkit.getScheduler().cancelTask(taskId);
                
                // Unregister listener
                org.bukkit.event.HandlerList.unregisterAll(moveListener);
                
                // Get old region to remove
                String regionName = "box_" + playerUUID.toString().replace("-", "");
                protectionListener.removeRegion(regionName);
                
                // Reset mining box
                fillMiningBox(worldName);
                
                // Completion notification
                player.sendTitle(
                    ChatColor.GREEN + "Reset complete", 
                    ChatColor.YELLOW + "You can start mining!", 
                    10, 40, 20
                );
                player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.YELLOW + "Your mining area has been successfully reset!");
                
                // Sound and particle effects
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 1.0f);
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 30, 0.5, 0.5, 0.5, 0);
            }, 5 * 20L); // 5 seconds
        }

        public void unloadWorld() {
            if (mineWorld != null) {
                // Remove all regions before unloading world
                String schemRegionName = "schem_" + playerUUID.toString().replace("-", "");
                String boxRegionName = "box_" + playerUUID.toString().replace("-", "");
                String globalRegionName = "global_" + playerUUID.toString().replace("-", "");
                
                protectionListener.removeRegion(schemRegionName);
                protectionListener.removeRegion(boxRegionName);
                protectionListener.removeRegion(globalRegionName);
                
                // Unload world
                Bukkit.unloadWorld(mineWorld, true);
            }
        }

        public void teleportPlayer(Player player) {
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
                player.sendMessage(ChatColor.GREEN + "You have been teleported to your mining area!");
                
                // Check and give pickaxe to player if needed
                giveInitialPickaxe(player);
            } else {
                player.sendMessage(ChatColor.RED + "Could not determine spawn location in your mining area!");
            }
        }

        /**
         * Give a pickaxe to the player if it's their first time in the mining area
         * @param player Player to give pickaxe to
         */
        private void giveInitialPickaxe(Player player) {
            // Path to file storing player data for pickaxe receipt
            File dataFolder = new File(plugin.getDataFolder(), "player_data");
            if (!dataFolder.exists()) {
                dataFolder.mkdirs();
            }
            File playerFile = new File(dataFolder, player.getUniqueId().toString() + ".yml");
            // Check if player has received a pickaxe before
            if (!playerFile.exists()) {
                // Create pickaxe with basic enchantments
                ItemStack pickaxe = new ItemStack(Material.IRON_PICKAXE);
                ItemMeta meta = pickaxe.getItemMeta();
                meta.setDisplayName(ChatColor.GOLD + "Mining Pickaxe");
                List<String> lore = new ArrayList<>();
                lore.add(ChatColor.GRAY + "Use to mine in your personal mining area");
                lore.add(ChatColor.YELLOW + "Efficiency: " + ChatColor.GREEN + "★★☆");
                meta.setLore(lore);
                pickaxe.setItemMeta(meta);
                // Add pickaxe to inventory
                player.getInventory().addItem(pickaxe);
                player.sendMessage(ChatColor.GREEN + "✓ " + ChatColor.YELLOW + "You received a " + 
                                  ChatColor.GOLD + "Mining Pickaxe" + ChatColor.YELLOW + "!");
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                player.spawnParticle(Particle.VILLAGER_HAPPY, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0);
                
                // Record that player has received pickaxe
                try {
                    YamlConfiguration config = new YamlConfiguration();
                    config.set("has_received_pickaxe", true);
                    config.set("received_date", System.currentTimeMillis());
                    config.save(playerFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not save player data: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Add world to disabled-worlds list in DeluxeHub config
     * @param worldName World name to add
     */
    private void addWorldToDeluxeHubDisabledList(String worldName) {
        try {
            File configFile = new File(DELUXEHUB_CONFIG_PATH);
            
            if (!configFile.exists()) {
                plugin.getLogger().warning("Could not find config.yml for DeluxeHub!");
                return;
            }
            
            YamlConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            List<String> disabledWorlds = config.getStringList("disabled-worlds.worlds");
            
            // Check if world is already in the list
            if (!disabledWorlds.contains(worldName)) {
                disabledWorlds.add(worldName);
                config.set("disabled-worlds.worlds", disabledWorlds);
                config.save(configFile);
                plugin.getLogger().info("Added world " + worldName + " to DeluxeHub disabled-worlds list");
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Error updating DeluxeHub config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static class VoidGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            ChunkData chunkData = createChunkData(world);
            for (int i = 0; i < 16; i++) {
                for (int j = 0; j < 16; j++) {
                    biome.setBiome(i, 0, j, org.bukkit.block.Biome.PLAINS);
                }
            }
            return chunkData;
        }
    }

    public enum OreType {
        STONE(Material.STONE),
        IRON_ORE(Material.IRON_ORE),
        GOLD_ORE(Material.GOLD_ORE),
        DIAMOND_ORE(Material.DIAMOND_ORE);

        private final Material material;

        OreType(Material material) {
            this.material = material;
        }

        public Material getMaterial() {
            return material;
        }

        public static OreType fromMaterial(Material material) {
            for (OreType type : values()) {
                if (type.getMaterial() == material) return type;
            }
            return STONE;
        }
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }

    public PlayerMine getPlayerMine(Player player) {
        return playerMines.get(player.getUniqueId());
    }
}
