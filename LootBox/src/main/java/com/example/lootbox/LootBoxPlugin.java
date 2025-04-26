package com.example.lootbox;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

public class LootBoxPlugin extends JavaPlugin implements Listener {
    private static Economy econ;
    private final Map<String, List<WeightedItem>> weightedRewards = new HashMap<>();
    private Connection connection;
    private static class WeightedItem {
        final ItemStack item;
        final int weight;
        WeightedItem(ItemStack item, int weight) {
            this.item   = item;
            this.weight = weight;
        }
    }
    private NFTLootBoxUtil nftLootBoxUtil;

    @Override
    public void onEnable() {
        // Vault setup
        if (!setupEconomy()) {
            getLogger().severe("Vault not found! Disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Load config.yml defaults if missing
        saveDefaultConfig();
        openDatabase();
        loadRewardsFromConfig();

        // Initialize NFT lootbox utility
        nftLootBoxUtil = new NFTLootBoxUtil(this);

        // Register command & listener
        getCommand("nftlootbox").setExecutor(new NFTLootBoxCommand(this, nftLootBoxUtil));
        getServer().getPluginManager().registerEvents(new NFTLootBoxListener(this, nftLootBoxUtil), this);

        getLogger().info("NFT Lootbox System enabled!");
    }

    private boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp =
            getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        econ = rsp.getProvider();
        return econ != null;
    }
    public static Economy getEconomy() {
        return econ;
    }

    /** Helper to generate namespaced keys for PersistentDataContainer */
    public NamespacedKey getKey(String key) {
        return new NamespacedKey(this, key);
    }



    /** Load your reward lists from config.yml under "rewards". */
    private void loadRewardsFromConfig() {
        ConfigurationSection rewardTypes = getConfig().getConfigurationSection("rewards");
        if (rewardTypes == null) return;

        for (String type : rewardTypes.getKeys(false)) {
            ConfigurationSection itemsSec = rewardTypes.getConfigurationSection(type + ".items");
            if (itemsSec == null) continue;

            List<WeightedItem> list = new ArrayList<>();
            for (String matName : itemsSec.getKeys(false)) {
                int weight = itemsSec.getInt(matName);
                Material mat = Material.matchMaterial(matName);
                if (mat != null && weight > 0) {
                    ItemStack stack = new ItemStack(mat);
                    ItemMeta im = stack.getItemMeta();
                    im.setDisplayName(
                        ChatColor.AQUA +
                        matName.replace('_', ' ').substring(0,1).toUpperCase() +
                        matName.replace('_', ' ').substring(1)
                    );
                    stack.setItemMeta(im);
                    list.add(new WeightedItem(stack, weight));
                }
            }
            weightedRewards.put(type, list);
    }
    }

    /** Pick a random reward (or dirt if empty). */
    public ItemStack getRewardFor(String type) {
        List<WeightedItem> list = weightedRewards.get(type);
        if (list == null || list.isEmpty()) {
            return new ItemStack(Material.DIRT);
        }
        // Sum up all weights
        int totalWeight = list.stream().mapToInt(wi -> wi.weight).sum();
        // Pick a random point in [1..totalWeight]
        int r = new Random().nextInt(totalWeight) + 1;

        int cum = 0;
        for (WeightedItem wi : list) {
            cum += wi.weight;
            if (r <= cum) {
                // clone & tag as a “lootbox reward”
                ItemStack pick = wi.item.clone();
                ItemMeta meta = pick.getItemMeta();
                // set flag
                meta.getPersistentDataContainer().set(
                    getKey("lootbox-reward"),
                    PersistentDataType.BYTE,
                    (byte)1
                );
                pick.setItemMeta(meta);
                return pick;
            }
        }
        // fallback
        ItemStack fallback = list.get(list.size() - 1).item.clone();
        ItemMeta fm = fallback.getItemMeta();
        fm.getPersistentDataContainer().set(
            getKey("lootbox-reward"),
            PersistentDataType.BYTE,
            (byte)1
        );
        fallback.setItemMeta(fm);
        return fallback;
    }
    private void openDatabase() {
        try {
            String url      = getConfig().getString("database.url");
            String user     = getConfig().getString("database.user");
            String password = getConfig().getString("database.password");
            // Load your driver explicitly if needed:
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, user, password);
            getLogger().info(ChatColor.GREEN +"Connected to SQL DB at " + url);
        } catch (ClassNotFoundException | SQLException e) {
            getLogger().severe("Could not open SQL database: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
        }
    }
    public Connection getConnection() {
        return connection;
    }
}
