package com.example.lootbox;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Listener for NFT lootbox interactions
 */
public class NFTLootBoxListener implements Listener {
    private final LootBoxPlugin plugin;
    private final NFTLootBoxUtil nftLootBoxUtil;
    private Object nftPlugin;
    private Method mintNftMethod;
    private boolean nftPluginLoaded = false;

    /**
     * Constructor
     * @param plugin The LootBoxPlugin instance
     * @param nftLootBoxUtil The NFTLootBoxUtil instance
     */
    public NFTLootBoxListener(LootBoxPlugin plugin, NFTLootBoxUtil nftLootBoxUtil) {
        this.plugin = plugin;
        this.nftLootBoxUtil = nftLootBoxUtil;
        loadNFTPlugin();
    }

    /**
     * Load the NFT plugin using reflection
     */
    private void loadNFTPlugin() {
        try {
            // Get the NFT plugin path from the config
            String nftPluginPath = plugin.getConfig().getString("nft_lootbox.plugin_path");
            if (nftPluginPath == null || nftPluginPath.isEmpty()) {
                plugin.getLogger().severe("NFT plugin path not configured in config.yml");
                return;
            }

            // Check if the NFT plugin exists
            File nftPluginDir = new File(nftPluginPath);
            if (!nftPluginDir.exists() || !nftPluginDir.isDirectory()) {
                plugin.getLogger().severe("NFT plugin directory not found: " + nftPluginPath);
                return;
            }

            // Get the NFT plugin JAR file
            File nftPluginJar = new File(nftPluginDir, "target/NFT-Plugin.jar");
            if (!nftPluginJar.exists()) {
                plugin.getLogger().severe("NFT plugin JAR not found: " + nftPluginJar.getAbsolutePath());
                return;
            }

            // Load the NFT plugin classes
            URLClassLoader classLoader = new URLClassLoader(
                new URL[] { nftPluginJar.toURI().toURL() },
                getClass().getClassLoader()
            );

            // Get the NFT plugin class
            Class<?> nftPluginClass = classLoader.loadClass("com.minecraft.nftplugin.NFTPlugin");

            // Get the NFT plugin instance
            nftPlugin = Bukkit.getPluginManager().getPlugin("NFTPlugin");
            if (nftPlugin == null) {
                plugin.getLogger().severe("NFT plugin not found in the server. Make sure it's loaded.");
                return;
            }

            // Get the mintNft method
            Class<?> solanaServiceClass = classLoader.loadClass("com.minecraft.nftplugin.solana.SolanaService");
            Method getSolanaServiceMethod = nftPluginClass.getMethod("getSolanaService");
            Object solanaService = getSolanaServiceMethod.invoke(nftPlugin);

            mintNftMethod = solanaServiceClass.getMethod("mintNft", Player.class, String.class);

            nftPluginLoaded = true;
            plugin.getLogger().info("NFT plugin loaded successfully");
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load NFT plugin: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onUse(PlayerInteractEvent e) {
        // Only proceed on right-click with the main hand
        if (e.getHand() != EquipmentSlot.HAND) return;
        Action action = e.getAction();
        if (action != Action.RIGHT_CLICK_AIR && action != Action.RIGHT_CLICK_BLOCK) return;
        if (e.getItem() == null) return;
        ItemMeta meta = e.getItem().getItemMeta();
        if (meta == null || !meta.getPersistentDataContainer().has(
                plugin.getKey("nft-lootbox-type"),
                PersistentDataType.STRING
        )) return;

        e.setCancelled(true);
        Player player = e.getPlayer();
        String type = meta.getPersistentDataContainer()
                          .get(plugin.getKey("nft-lootbox-type"), PersistentDataType.STRING);

        // Consume one
        ItemStack stack = e.getItem();
        stack.setAmount(stack.getAmount() - 1);

        // Open rolling GUI
        Inventory inv = Bukkit.createInventory(null, 9, "Rolling NFT " + type.replace("_", " "));
        ItemStack glass = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        ItemMeta gm = glass.getItemMeta();
        gm.setDisplayName(" ");
        glass.setItemMeta(gm);
        for (int i = 0; i < 9; i++) inv.setItem(i, glass);
        player.openInventory(inv);
        player.playSound(
            player.getLocation(),
            Sound.BLOCK_NOTE_BLOCK_PLING,
            SoundCategory.MASTER,
            1.0f,
            1.0f
        );

        // Get the lootbox type (basic, premium, ultimate)
        String lootboxType = nftLootBoxUtil.getLootboxType(type);

        // Animate & pick reward in the middle slot
        new BukkitRunnable() {
            int ticks = 0;
            String selectedTier = null;
            String selectedNFT = null;

            @Override
            public void run() {
                ticks++;

                // Select the tier and NFT on the first tick
                if (ticks == 1) {
                    selectedTier = nftLootBoxUtil.getRandomTier(lootboxType);
                    selectedNFT = nftLootBoxUtil.getRandomNFT(selectedTier);
                    plugin.getLogger().info("Selected tier: " + selectedTier + ", NFT: " + selectedNFT);
                }

                // Update the display item
                if (ticks <= 40) {
                    // During animation, show random items
                    String randomTier = nftLootBoxUtil.getRandomTier(lootboxType);
                    String randomNFT = nftLootBoxUtil.getRandomNFT(randomTier);

                    ItemStack displayItem = new ItemStack(Material.PAPER);
                    ItemMeta displayMeta = displayItem.getItemMeta();
                    displayMeta.setDisplayName(nftLootBoxUtil.getTierColor(randomTier) + randomNFT.replace("_", " "));
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Tier: " + nftLootBoxUtil.getTierColor(randomTier) + randomTier);
                    displayMeta.setLore(lore);
                    displayItem.setItemMeta(displayMeta);

                    inv.setItem(4, displayItem);

                    // Play sound every few ticks
                    if (ticks % 5 == 0) {
                        player.playSound(
                            player.getLocation(),
                            Sound.BLOCK_NOTE_BLOCK_PLING,
                            SoundCategory.MASTER,
                            1.0f,
                            1.0f + (ticks / 40.0f)
                        );
                    }
                } else if (ticks == 41) {
                    // Show the final selected item
                    ItemStack finalItem = new ItemStack(Material.PAPER);
                    ItemMeta finalMeta = finalItem.getItemMeta();
                    finalMeta.setDisplayName(nftLootBoxUtil.getTierColor(selectedTier) + selectedNFT.replace("_", " "));
                    List<String> lore = new ArrayList<>();
                    lore.add(ChatColor.GRAY + "Tier: " + nftLootBoxUtil.getTierColor(selectedTier) + selectedTier);
                    lore.add(ChatColor.YELLOW + "Congratulations!");
                    finalMeta.setLore(lore);
                    finalItem.setItemMeta(finalMeta);

                    inv.setItem(4, finalItem);

                    // Play success sound
                    player.playSound(
                        player.getLocation(),
                        Sound.ENTITY_PLAYER_LEVELUP,
                        SoundCategory.MASTER,
                        1.0f,
                        1.0f
                    );
                } else if (ticks >= 60) {
                    // Close inventory and mint the NFT
                    player.closeInventory();

                    // Mint the NFT
                    mintNFT(player, selectedNFT);

                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 2);
    }

    /**
     * Mint an NFT for a player
     * @param player The player
     * @param nftKey The NFT metadata key
     */
    private void mintNFT(Player player, String nftKey) {
        // Gửi thông báo cho người chơi rằng NFT đang được mint
        player.sendMessage(ChatColor.YELLOW + "NFT đang được mint... Vui lòng chờ thông báo hoàn thành!");

        // Sử dụng quyền op tạm thời để mint NFT
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            boolean wasOp = player.isOp();
            try {
                // Ghi log
                plugin.getLogger().info("Minting NFT cho người chơi " + player.getName() + ": " + nftKey);

                // Cấp quyền op tạm thời
                if (!wasOp) {
                    player.setOp(true);
                }

                // Thực thi lệnh với quyền op
                String command = "mintnft " + player.getName() + " " + nftKey;
                player.sendMessage(ChatColor.GREEN + "Thử mint với quyền op...");
                Bukkit.dispatchCommand(player, command);

                // Không cần gửi thông báo thêm vì lệnh /mintnft đã gửi thông báo rồi
            } catch (Exception e) {
                player.sendMessage(ChatColor.RED + "Không thể mint NFT: " + e.getMessage());
                plugin.getLogger().severe("Lỗi khi mint NFT: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Khôi phục trạng thái op
                if (!wasOp) {
                    player.setOp(false);
                }
            }
        }, 10L); // Chờ 0.5 giây để đảm bảo thông báo được hiển thị trước
    }

    @EventHandler
    public void onNFTLootboxDrop(PlayerDropItemEvent e) {
        ItemStack dropped = e.getItemDrop().getItemStack();
        ItemMeta meta = dropped.getItemMeta();
        if (meta != null) {
            var pdc = meta.getPersistentDataContainer();
            boolean isNFTBox = pdc.has(plugin.getKey("nft-lootbox-type"), PersistentDataType.STRING);
            if (isNFTBox) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(ChatColor.RED + "You cannot drop that item!");
            }
        }
    }

    @EventHandler
    public void onRollingClick(InventoryClickEvent e) {
        String title = e.getView().getTitle();
        if (title.startsWith("Rolling NFT ")) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onRollingDrag(InventoryDragEvent e) {
        String title = e.getView().getTitle();
        if (title.startsWith("Rolling NFT ")) {
            e.setCancelled(true);
        }
    }
}

