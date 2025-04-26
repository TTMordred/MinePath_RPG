package me.tien.nftminer.world;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Difficulty;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;

public class VoidMine {
    private final Plugin plugin;
    private final Map<UUID, PlayerMine> playerMines = new HashMap<>();

    // Kích thước khu đào
    private final int boxWidth;
    private final int boxHeight;
    private final int boxLength;

    // Tỷ lệ quặng
    private final Map<OreType, Double> oreRates = new HashMap<>();

    public VoidMine(Plugin plugin) {
        this.plugin = plugin;

        // Đọc cấu hình từ file config
        FileConfiguration config = plugin.getConfig();

        // Đọc kích thước khu đào
        this.boxWidth = config.getInt("mine-box.width", 10);
        this.boxHeight = config.getInt("mine-box.height", 6);
        this.boxLength = config.getInt("mine-box.length", 10);

        // Đọc tỷ lệ quặng
        ConfigurationSection oreSection = config.getConfigurationSection("ore-rates");
        if (oreSection != null) {
            for (String key : oreSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(key.toUpperCase());
                    double rate = oreSection.getDouble(key, 0.0);
                    if (rate > 0) {
                        oreRates.put(OreType.fromMaterial(material), rate);
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Material không hợp lệ trong config: " + key);
                }
            }
        }

        // Nếu không có tỷ lệ quặng nào, thiết lập giá trị mặc định
        if (oreRates.isEmpty()) {
            oreRates.put(OreType.STONE, 0.7);
            oreRates.put(OreType.IRON_ORE, 0.15);
            oreRates.put(OreType.GOLD_ORE, 0.1);
            oreRates.put(OreType.DIAMOND_ORE, 0.05);
        }
    }

    /**
     * Lấy hoặc tạo khu đào cho người chơi
     */
    public PlayerMine getPlayerMine(Player player) {
        UUID playerUUID = player.getUniqueId();

        // Kiểm tra xem người chơi đã có mine chưa
        if (!playerMines.containsKey(playerUUID)) {
            // Tạo mine mới cho người chơi
            PlayerMine playerMine = new PlayerMine(player);
            playerMines.put(playerUUID, playerMine);
        }

        return playerMines.get(playerUUID);
    }

    /**
     * Reset khu đào của người chơi
     */
    public void resetPlayerMine(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (playerMines.containsKey(playerUUID)) {
            PlayerMine playerMine = playerMines.get(playerUUID);
            playerMine.resetMiningBox();
        }
    }

    /**
     * Xóa khu đào của người chơi
     */
    public void removePlayerMine(UUID playerUUID) {
        if (playerMines.containsKey(playerUUID)) {
            PlayerMine playerMine = playerMines.get(playerUUID);
            playerMine.unloadWorld();
            playerMines.remove(playerUUID);
        }
    }

    /**
     * Cleanup tất cả các thế giới khi tắt plugin
     */
    public void cleanup() {
        for (PlayerMine mine : playerMines.values()) {
            mine.unloadWorld();
        }
        playerMines.clear();
    }

    /**
     * Class đại diện cho khu đào của mỗi người chơi
     */
    public class PlayerMine {
        private final UUID playerUUID;
        private final String playerName;
        private final String worldName;

        private World mineWorld;
        private Location spawnLocation;
        private int totalBlocks;
        private int remainingBlocks;

        private final Random random = new Random();

        public PlayerMine(Player player) {
            this.playerUUID = player.getUniqueId();
            this.playerName = player.getName();
            this.worldName = "mine_" + playerUUID.toString().replace("-", "");

            // Tính tổng số block
            this.totalBlocks = (boxWidth - 2) * (boxHeight - 1) * (boxLength - 2);
            this.remainingBlocks = totalBlocks;

            // Tạo thế giới
            createMineWorld();
        }

        private void createMineWorld() {
            mineWorld = Bukkit.getWorld(worldName);

            if (mineWorld == null) {
                WorldCreator worldCreator = new WorldCreator(worldName);
                worldCreator.generator(new VoidGenerator());
                worldCreator.environment(World.Environment.NORMAL);
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
                spawnLocation = new Location(mineWorld, 0, boxHeight + 1, 0);
                mineWorld.setSpawnLocation(spawnLocation);

                createSpawnPlatform();
                createMiningBox();
            } else {
                getLogger().severe("Không thể tạo hoặc tải thế giới: " + worldName + " cho người chơi " + playerName);
            }
        }

        private void createSpawnPlatform() {
            int platformWidth = 10;
            int platformLength = 10;

            int startX = spawnLocation.getBlockX() - platformWidth / 2;
            int startZ = spawnLocation.getBlockZ() - platformLength / 2;
            int y = spawnLocation.getBlockY() - 1;

            for (int x = startX; x < startX + platformWidth; x++) {
                for (int z = startZ; z < startZ + platformLength; z++) {
                    mineWorld.getBlockAt(x, y, z).setType(Material.STONE);
                }
            }
        }

        private void createMiningBox() {
            int minX = -boxWidth / 2;
            int maxX = minX + boxWidth;
            int minY = 1;
            int maxY = minY + boxHeight;
            int minZ = -boxLength / 2;
            int maxZ = minZ + boxLength;

            createBedrockFrame(minX, minY, minZ, maxX, maxY, maxZ);
            fillMiningBox(minX + 1, minY + 1, minZ + 1, maxX - 1, maxY - 1, maxZ - 1);
            getLogger().info("Đã tạo khu đào cho " + playerName + " với " + totalBlocks + " blocks");
            remainingBlocks = totalBlocks;
        }

        private void createBedrockFrame(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            // Tạo mặt đáy
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mineWorld.getBlockAt(x, minY, z).setType(Material.BEDROCK);
                }
            }

            // Tạo các cạnh bên
            for (int y = minY + 1; y <= maxY; y++) {
                // Cạnh X
                for (int x = minX; x <= maxX; x++) {
                    mineWorld.getBlockAt(x, y, minZ).setType(Material.BEDROCK);
                    mineWorld.getBlockAt(x, y, maxZ).setType(Material.BEDROCK);
                }

                // Cạnh Z
                for (int z = minZ + 1; z < maxZ; z++) {
                    mineWorld.getBlockAt(minX, y, z).setType(Material.BEDROCK);
                    mineWorld.getBlockAt(maxX, y, z).setType(Material.BEDROCK);
                }
            }

            // Không tạo mặt trên để người chơi có thể vào từ phía trên
        }

        private void fillMiningBox(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        OreType oreType = getRandomOre(oreRates);
                        mineWorld.getBlockAt(x, y, z).setType(oreType.getMaterial());
                    }
                }
            }
        }

        private OreType getRandomOre(Map<OreType, Double> oreRates) {
            double value = random.nextDouble();
            double currentThreshold = 0.0;

            for (Map.Entry<OreType, Double> entry : oreRates.entrySet()) {
                currentThreshold += entry.getValue();
                if (value < currentThreshold) {
                    return entry.getKey();
                }
            }

            return OreType.STONE;
        }

        public void resetMiningBox() {
            createMiningBox();
            Player player = Bukkit.getPlayer(playerUUID);
            if (player != null && player.isOnline()) {
                player.teleport(spawnLocation);
                player.sendMessage(ChatColor.GREEN + "Khu đào của bạn đã được tạo lại!");
            }
        }

        public void unloadWorld() {
            if (mineWorld != null) {
                Bukkit.unloadWorld(mineWorld, true);
            }
        }

        public boolean isInMiningBox(Location location) {
            int minX = -boxWidth / 2;
            int maxX = minX + boxWidth;
            int minY = 1;
            int maxY = minY + boxHeight;
            int minZ = -boxLength / 2;
            int maxZ = minZ + boxLength;

            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();

            return x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ;
        }

        public void teleportPlayer(Player player) {
            if (spawnLocation != null) {
                player.teleport(spawnLocation);
                player.sendMessage(ChatColor.GREEN + "Bạn đã được teleport đến khu đào của mình!");
            } else {
                player.sendMessage(ChatColor.RED + "Không thể xác định vị trí spawn trong khu đào của bạn!");
            }
        }
    }

    public static class VoidGenerator extends ChunkGenerator {
        @Override
        public ChunkData generateChunkData(World world, Random random, int x, int z, BiomeGrid biome) {
            return createChunkData(world);
        }
    }

    public boolean isMineWorld(String worldName) {
        return playerMines.values().stream()
                .anyMatch(mine -> mine.worldName.equals(worldName));
    }

    public PlayerMine getMineByWorldName(String worldName) {
        return playerMines.values().stream()
                .filter(mine -> mine.worldName.equals(worldName))
                .findFirst()
                .orElse(null);
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
                if (type.getMaterial() == material) {
                    return type;
                }
            }
            return STONE;
        }
    }

    private Logger getLogger() {
        return plugin.getLogger();
    }
}