package me.tien.nftminer;

import me.tien.nftminer.listeners.MiningListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import me.tien.nftminer.commands.ClaimCommand;
import me.tien.nftminer.commands.MiningBoxCommand;
import me.tien.nftminer.commands.ResetMineCommand;
import me.tien.nftminer.commands.ResetUpgradeCommand;
import me.tien.nftminer.commands.ShopCommand;
import me.tien.nftminer.commands.TokenCommand;
import me.tien.nftminer.gui.ShopGUI;
import me.tien.nftminer.integration.MinePathIntegration;
import me.tien.nftminer.integration.NFTPluginIntegration;
import me.tien.nftminer.listeners.InventoryListener;
import me.tien.nftminer.listeners.PlayerListener;
import me.tien.nftminer.listeners.ShopListener;
import me.tien.nftminer.scoreboard.CustomScoreboardManager;
import me.tien.nftminer.token.TokenManager;
import me.tien.nftminer.upgrade.InventoryUpgrade;
import me.tien.nftminer.upgrade.SpeedUpgrade;
import me.tien.nftminer.upgrade.TokenValueUpgrade;
import me.tien.nftminer.upgrade.UpgradeManager;
import me.tien.nftminer.world.VoidMine;

public class NFTMiner extends JavaPlugin {
    private VoidMine voidMine;
    private NFTPluginIntegration nftIntegration;
    private MinePathIntegration minePathIntegration;
    private TokenManager tokenManager;
    private UpgradeManager upgradeManager;
    private InventoryUpgrade inventoryUpgrade;
    private ShopGUI shopGUI;
    private SpeedUpgrade speedUpgrade;
    private CustomScoreboardManager scoreboardManager;
    private TokenValueUpgrade tokenValueUpgrade;

    @Override
    public void onEnable() {
        // Khởi tạo tích hợp NFTPlugin
        nftIntegration = new NFTPluginIntegration(this);

        // 1) Tạo trước TokenManager tạm (không dùng upgradeManager)
        tokenManager = new TokenManager(this);
        getLogger().info("TokenManager initialized successfully");

        // 2) Tạo UpgradeManager, truyền vào TokenManager
        upgradeManager = new UpgradeManager(this, tokenManager);
        getLogger().info("UpgradeManager initialized successfully");

        // Registering InventoryListener
        try {
            InventoryListener inventoryListener = new InventoryListener(this, upgradeManager);
            getServer().getPluginManager().registerEvents(inventoryListener, this);
            getLogger().info("InventoryListener registered successfully!");

            // Log number of locked slots for verification
            getLogger().info("InventoryListener is ready to handle inventory locks");
        } catch (Exception e) {
            getLogger().severe("Failed to register InventoryListener: " + e.getMessage());
            e.printStackTrace();
        }

        // 3) Giờ TokenManager có thể lấy được tokenValueUpgrade
        tokenManager.setTokenValueUpgrade(upgradeManager.getTokenValueUpgrade());
        tokenValueUpgrade = upgradeManager.getTokenValueUpgrade();
        // Khởi tạo GUI
        shopGUI = new ShopGUI(this, tokenManager, upgradeManager);
        inventoryUpgrade = new InventoryUpgrade(this, tokenManager);
        speedUpgrade = upgradeManager.getSpeedUpgrade();
        // Lưu config mặc định
        saveDefaultConfig();

        // Đăng ký lệnh
        getCommand("claim").setExecutor(new ClaimCommand(this, tokenManager,tokenValueUpgrade));
        getCommand("token").setExecutor(new TokenCommand(this, tokenManager));
        getCommand("shop").setExecutor(new ShopCommand(this, shopGUI));
        getCommand("miningbox").setExecutor(new MiningBoxCommand(this));
        getCommand("resetmine").setExecutor(new ResetMineCommand(this));
        getCommand("resetupgrades").setExecutor(new ResetUpgradeCommand(this, upgradeManager));

        // Đăng ký listener
        getServer().getPluginManager().registerEvents(new ShopListener(shopGUI), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(upgradeManager), this);
        getServer().getPluginManager().registerEvents(new MiningListener(this), this);

        if (nftIntegration.isNFTPluginAvailable()) {
            getLogger().info("Tìm thấy NFTPlugin - Chức năng NFT đã được kích hoạt!");
        } else {
            getLogger().warning("Không tìm thấy NFTPlugin - Chức năng NFT bị vô hiệu hóa!");
        }
        // Khởi tạo tích hợp SolanaCoin

        Bukkit.getScheduler().runTaskLater(this, () -> {
            Plugin minePath = getServer().getPluginManager().getPlugin("MinePath");
            if (minePath != null && minePath.isEnabled()) {
                getLogger().info("MinePath đã sẵn sàng!");
                this.minePathIntegration = new MinePathIntegration(this);
                if (minePathIntegration.isMinePathAvailable()) {
                    scoreboardManager = new CustomScoreboardManager(this, minePathIntegration);
                    getLogger().info("Đã kết nối thành công với MinePath!");

                } else {
                    getLogger().warning("Không thể kết nối với MinePath.");
                }
            } else {
                getLogger().warning("MinePath chưa sẵn sàng!");
            }
        }, 140L); // chờ 1 giây (20 ticks) sau khi server load
        // Khởi tạo VoidMine
        voidMine = new VoidMine(this);
    }

    /**
     * Lấy NFT Integration
     */
    public NFTPluginIntegration getNFTIntegration() {
        return nftIntegration;
    }

    /**
     * Lấy SolanaCoin Integration
     */
    public MinePathIntegration getMinePathIntegration() {
        return minePathIntegration;
    }

    /**
     * Lấy VoidMine
     */
    public VoidMine getVoidMine() {
        return voidMine;
    }

    /** Thêm getter này: */
    public UpgradeManager getUpgradeManager() {
        return upgradeManager;
    }

    public CustomScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }
}