package com.solanacoin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.p2p.solanaj.programs.AssociatedTokenProgram;
import org.p2p.solanaj.rpc.types.SplTokenAccountInfo;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;
import com.solanacoin.util.Base58;
import org.p2p.solanaj.rpc.Cluster;
import org.p2p.solanaj.rpc.RpcClient;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.core.*;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;


public class MinePathCoinPlugin extends JavaPlugin implements Listener {

    public static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.US);
    static {
        NUMBER_FORMAT.setRoundingMode(RoundingMode.FLOOR);
        NUMBER_FORMAT.setGroupingUsed(true);
        NUMBER_FORMAT.setMinimumFractionDigits(0);
        NUMBER_FORMAT.setMaximumFractionDigits(2);
    }

    FileConfiguration config = getConfig();
    protected String rpcURL;
    protected RpcClient rpcClient;
    protected Account signer;
    protected PublicKey publicKey;
    protected PublicKey tokenMintAddress;
    protected PublicKey associatedTokenAddress;
    protected int requestLimitPerSecond;
    protected double minimumExport;
    protected double startingBalance = 0.0;
    protected org.p2p.solanaj.rpc.types.TokenResultObjects.TokenInfo tokenMintInfo;
    protected SQL db;
    boolean enabled;
    protected VaultIntegration vaultIntegration;
    protected String currencySymbol;
    protected int tokenDecimals;

    protected String chatPrefix = ChatColor.GRAY + "["+ChatColor.RESET+"MINEPATH"+ChatColor.GRAY + "]: " + ChatColor.RESET;


    public void loadSignerAccount() {
        this.chatPrefix = ChatColor.GRAY + "["+ChatColor.RESET+"MINEPATH"+ChatColor.GRAY + "]: " + ChatColor.RESET;
        getServer().getConsoleSender().sendMessage(chatPrefix + "Statring to collecting Signer");
        if (config.contains("signer")) {
            this.signer = new Account(Base58.decode(Objects.requireNonNull(config.getString("signer"))));
        }
    }

    public void loadPublicKey() {
        String publicKey = config.getString("publicKey");
        getServer().getConsoleSender().sendMessage(chatPrefix + "Statring to collecting publicKey");
        try {
            assert publicKey != null;
            this.publicKey = new PublicKey(publicKey);
        } catch(IllegalArgumentException e) {
            getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.RED + " config field 'publicKey' invalid.");
        }

    }
    
    public PublicKey getAssociatedTokenAddress(PublicKey wallet, PublicKey mint) {
        try {
            // Use the three seeds: wallet, TokenProgram.PROGRAM_ID, mint
            return PublicKey.findProgramAddress(
                    List.of(
                            wallet.toByteArray(),
                            org.p2p.solanaj.programs.TokenProgram.PROGRAM_ID.toByteArray(),
                            mint.toByteArray()
                    ),
                    org.p2p.solanaj.programs.AssociatedTokenProgram.PROGRAM_ID
            ).getAddress();
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute associated token address", e);
        }
    }
    public PublicKey fetchAssociatedTokenAccount(PublicKey wallet, PublicKey tokenMintAddress) {
        PublicKey ata = getAssociatedTokenAddress(wallet, tokenMintAddress);
        try {
            SplTokenAccountInfo info = rpcClient
                .getApi()
                .getSplTokenAccountInfo(ata);
            // if getSplTokenAccountInfo returns a non-null value, the ATA exists
            if (info != null && info.getValue() != null) {
                return ata;
            }
        } catch (RpcException e) {
            // account not found or invalid owner
        }
        return null;
    }
    public void addCreateAtaInstruction_MINE(PublicKey wallet, Transaction tx) {
        TransactionInstruction createIx = AssociatedTokenProgram.createIdempotent(
            signer.getPublicKey(),   // funding account
            wallet,                         // owner of the ATA
            tokenMintAddress         // the mint
        );
        getLogger().info("Creating ATA for " + wallet.toBase58()+ "......");
        tx.addInstruction(createIx);
    }
    public byte[] createInstructionData(double amount, byte num) {
        // Create a standard SPL token transfer instruction (7 = Mint,3 = Transfer)
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put(num);
        
        // Amount in the smallest denomination (based on decimals)
        long transferAmount = (long) (amount * Math.pow(10,tokenDecimals));
        buffer.putLong(transferAmount);
        
        return buffer.array();
    }

    public void loadTokenMint() {
        String tokenMint = config.getString("tokenMint");
        getServer().getConsoleSender().sendMessage(chatPrefix + "Statring to collecting tokenMint");
        try {
            assert tokenMint != null;
            this.tokenMintAddress = new PublicKey(tokenMint);
            TokenAmountInfo supplyInfo = rpcClient.getApi().getTokenSupply(this.tokenMintAddress);
            this.tokenDecimals = supplyInfo.getDecimals();
            getServer().getConsoleSender().sendMessage(chatPrefix + "Detected token decimals = " + this.tokenDecimals);
            try {
            this.associatedTokenAddress = getAssociatedTokenAddress(this.publicKey, this.tokenMintAddress);
            } catch (Exception e) {
                getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.RED + "Failed to get mint info: " + e.getMessage());
                e.printStackTrace();
            }
        } catch(IllegalArgumentException e) {
            getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.RED + "Config field 'tokenMint' invalid.");
        } catch (Exception e) {
            getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.RED + "Failed to find associated token account, make sure you have some of the token in your account.");
            e.printStackTrace();
        }
        getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.GREEN + "Token MINE load successfully. " + tokenMintAddress.toBase58());
    }
    public void loadSQL() {
        this.getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.GRAY + "Loading SQL...");
        if (this.db != null) {
            this.db.disconnect();
        }
        this.db = new SQL(
                this
        );
        try {
            if (Objects.requireNonNull(config.getString("dbType")).equalsIgnoreCase("sqlite")) {
                this.db.connectSQLite(config.getString("sqliteLocation"));
            } else {
                this.db.connectSQL(config.getString("dbType"), config.getString("dbHost"),
                        config.getString("dbPort"),
                        config.getString("dbName"),
                        config.getString("dbUsername"),
                        config.getString("dbPassword"),
                        config.getBoolean("dbUseSSL"));
            }
            this.getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.GREEN + "Database connected! Ahihi");
        } catch (SQLException e) {
            e.printStackTrace();
            this.getServer().getConsoleSender().sendMessage(chatPrefix + ChatColor.RED + "Database connection failed. Oh no!");
        }
    }

    public void setupSQL() {
        this.loadSQL();
        if (this.db.isConnected()){
            this.db.setupBalanceTable();
            this.db.setupSessionTable();
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        this.tryHookVault("onLoad");
    }
    public void sendUsage(CommandSender sender, String usage) {
        sender.sendMessage(chatPrefix + ChatColor.YELLOW + "Usage: " + ChatColor.WHITE + usage);
    }
    @Override
    public void onEnable () {

        this.reloadConfig();
        if (this.enabled) {

            Objects.requireNonNull(this.getCommand("tokenadmin")).setExecutor(new AdminCommand(this));
            Objects.requireNonNull(this.getCommand("tokenbalance")).setExecutor(new BalanceCommand(this));
            Objects.requireNonNull(this.getCommand("tokendb")).setExecutor(new DBCommand(this));
            Objects.requireNonNull(this.getCommand("tokenlbh")).setExecutor(new LbhCommand(this));
            Objects.requireNonNull(this.getCommand("tokensend")).setExecutor(new SendCommand(this));
            Objects.requireNonNull(this.getCommand("tokenhelp")).setExecutor(new HelpCommand(this));
            Objects.requireNonNull(this.getCommand("tokenbuy")).setExecutor(new BuyCommand(this));
            Objects.requireNonNull(this.getCommand("tokenauthorize")).setExecutor(new AuthorizeCommand(this));
            Objects.requireNonNull(this.getCommand("tokenburn")).setExecutor(new BurnCommand(this));
            Objects.requireNonNull(this.getCommand("tokenclaim")).setExecutor(new ClaimCommand(this));
            getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.GREEN + "MINEPATH Enabled");
            this.tryHookVault("onEnable");
            getServer().getPluginManager().registerEvents(this, this);
        } else {
            getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.GREEN + "MINEPATH Not Enabled");
        }

        startAutoBalanceUpdates();
    }

    @Override
    public void onDisable () {
        if (this.db != null) {
            if (this.db.isConnected()) {
                this.db.disconnect();
            }
        }
        getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.RED + "MINEPATH Disabled");

    }

    // This method checks for incoming players and sends them a message
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if (this.enabled) {
           player.sendMessage(chatPrefix+"This server has MINEPATH support!");
            if(this.db.isConnected()) {
                this.db.addPlayerToBalanceTable(player);
            }
        }
    }

    public enum TELLRAWCOLOR {
        red,
        dark_red,
        yellow,
        gold,
        green,
        dark_green,
        blue,
        dark_blue,
        aqua,
        dark_aqua,
        light_purple,
        dark_purple,
        gray,
        dark_gray,
        white,
        black
    }

    public void sendURLToPlayer(Player player, String message, String url, TELLRAWCOLOR color) {
        this.getServer().dispatchCommand(
                this.getServer().getConsoleSender(),
                "tellraw " + player.getName() +
                        " {\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" +
                        url + "\"},\"color\":\""+color.name()+"\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\""+url+"\"]}}");
    }

    public void sendCopyableTextToPlayer(Player player, String message, String toCopy, TELLRAWCOLOR color) {
        this.getServer().dispatchCommand(
                this.getServer().getConsoleSender(),
                "tellraw " + player.getName() +
                        " {\"text\":\"" + message + "\",\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"" +
                        toCopy + "\"},\"color\":\""+color.name()+"\",\"underlined\":true,\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"Copy to clipboard\"]}}");
    }

    public Timestamp getNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String now = formatter.format((new Date(System.currentTimeMillis())));
        return Timestamp.valueOf(now);
    }

    public boolean shouldRateLimit(Player player) {

        Timestamp lastRequest = Timestamp.valueOf(this.db.getLastRequestTimestamp(player.getUniqueId()));
        Timestamp now = this.getNow();
        long milliseconds = now.getTime() - lastRequest.getTime();
        int seconds = (int) milliseconds / 1000;
        return (seconds < this.requestLimitPerSecond);
    }

    public void tryHookVault(String where) {
        if (this.config.getBoolean("vaultEnabled")) {
            if (this.getServer().getPluginManager().getPlugin("Vault") != null) {
                if (this.vaultIntegration == null) {
                    this.vaultIntegration = new VaultIntegration(this);
                }
            } else {
                this.getServer().getLogger().warning("[WARNING][MINEPATH] Vault not found during " + where + ".");
            }
        }
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (this.config.getBoolean("vaultEnabled")) {
            if (this.vaultIntegration != null) {
                if (this.vaultIntegration.getPermissions() != null) {
                    if (this.vaultIntegration.getPermissions().isEnabled()) {
                        return vaultIntegration.getPermissions().has(sender, permission);
                    }
                }
            }
        }
        return sender.hasPermission(permission);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        saveDefaultConfig();
        config = getConfig();
        config.options().copyDefaults(true);
        saveConfig();

        this.enabled = config.getBoolean("enabled");
        if (this.enabled) {

//            this.tryHookVault("reload");


            if (config.contains("rpcURL")) {
                String rpcURL = config.getString("rpcURL");
                boolean loadClient = true;
                if (this.rpcURL == null) {
                    this.rpcURL = rpcURL;
                } else if (this.rpcURL.equalsIgnoreCase(rpcURL) && this.rpcClient == null) {
                    loadClient = false;
                }
                if (loadClient && !Objects.requireNonNull(rpcURL).equals("")) {
                    this.rpcClient = new RpcClient(rpcURL);
                } else {
                    this.rpcClient = new RpcClient(Cluster.MAINNET);
                    getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.RED + "config field 'rpcURL' is blank. Defaulting to \"https://api.mainnet-beta.solana.com\".");
                }
            }


            if (config.contains("signer")) {
                if (this.signer == null ||
                !Base58.encode(this.signer.getSecretKey()).equalsIgnoreCase(config.getString("signer"))) {
                    if(this.signer != null) {
                        getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.YELLOW + "'signer' config field changed!");
                    }
                    this.loadSignerAccount();
                }
            } else {
                getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.RED + "config field 'signer' missing! Won't be able to send transactions.");
            }

            if (config.contains("publicKey")) {
                if (this.publicKey == null
                || !this.publicKey.toBase58().equalsIgnoreCase(config.getString("publicKey"))) {
                    if (this.publicKey != null) {
                        getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.YELLOW + "'publicKey' config field changed!");
                    }
                this.loadPublicKey();
                }
            } else {
                getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.RED + "config field 'publicKey' missing. Won't be able to send transactions.");
            }

            if (config.contains("tokenMint")) {
                String cfgMint = config.getString("tokenMint");
                // always load if we haven't loaded one yet, or if it differs
                if (this.tokenMintAddress == null
                    || !this.tokenMintAddress.toBase58().equalsIgnoreCase(cfgMint)) {
                    if (this.tokenMintAddress != null) {
                        getServer().getConsoleSender()
                        .sendMessage(chatPrefix + ChatColor.YELLOW + "'tokenMint' config field changed!");
                    }
                    loadTokenMint();           // now runs on first-ever load, and whenever config changes
                }
            } else {
                getServer().getConsoleSender()
                  .sendMessage(chatPrefix + ChatColor.RED  + "config field 'tokenMint' missing.");
            }

            if (config.contains("minimumExport")) {
                this.minimumExport = config.getDouble("minimumExport");
            }

            if (config.contains("requestLimitPerSecond")) {
                this.requestLimitPerSecond = config.getInt("requestLimitPerSecond");
            }

            if(config.contains("startingBalance")) {
                this.startingBalance = config.getDouble("startingBalance");
            }

            if (config.contains("currencySymbol")) {
                boolean loadCurrencySymbol = true;
                if (this.currencySymbol != null) {
                    if (!this.currencySymbol.equals("")) {
                        if (!this.currencySymbol.equalsIgnoreCase(config.getString("currencySymbol"))) {
                            getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.YELLOW + "'currencySymbol' config field changed!");
                        } else {
                            loadCurrencySymbol = false;
                        }
                    }
                }
                if (loadCurrencySymbol) {
                    this.currencySymbol = config.getString("currencySymbol");
                }

            }
            this.setupSQL();

        } else {
            if (this.db != null) {
                if (this.db.isConnected()) {
                    this.db.disconnect();
                }
                getServer().getConsoleSender().sendMessage(chatPrefix+ChatColor.RED + "MINEPATH Disabled via Config Reload");
            }
        }


    }
    private void startAutoBalanceUpdates() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    // Fetch and display the player's wallet balance asynchronously
                    Bukkit.getScheduler().runTaskAsynchronously(MinePathCoinPlugin.this, () -> {
                        String mineBal = "Fetching...";
                        try {
                            // Fetch the player's wallet address from the database
                            String walletAddress = fetchWalletAddress(player.getUniqueId().toString());
                            if (walletAddress != null && !walletAddress.isEmpty()) {
                                PublicKey wallet = new PublicKey(walletAddress);

                                // Fetch associated token address & balance
                                PublicKey UserATA = getAssociatedTokenAddress(wallet, tokenMintAddress);
                                TokenAmountInfo balance = rpcClient.getApi().getTokenAccountBalance(UserATA, null);
                                mineBal = balance.getUiAmountString();
                            } else {
                                mineBal = "Wallet not linked";
                            }
                        } catch (Exception e) {
                            mineBal = "No ATA dectected";
                        }
                        // Update the player's scoreboard with the fetched balance
                        String finalMine = mineBal;
                        Bukkit.getScheduler().runTask(MinePathCoinPlugin.this, () -> {
                            updatePlayerScoreboard(player, finalMine);
                        });
                    });
                }
            }
        }.runTaskTimer(this, 0L, 10L);
    }
    // Function to update the player's scoreboard with the latest balance
    private void updatePlayerScoreboard(Player player, String mineBal) {
        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = player.getScoreboard();
        Objective objective = scoreboard.getObjective("walletBal");

        // If the objective doesn't exist yet, create it
        if (objective == null) {
            scoreboard = scoreboardManager.getNewScoreboard();
            objective = scoreboard.registerNewObjective("walletBal", "dummy", ChatColor.GOLD + "Your Wallet");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            player.setScoreboard(scoreboard);
        }

        // Reset existing entries (optional: track specific entries for updates)
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }

        // Add the balance to the sidebar
        objective.getScore(ChatColor.GREEN + "Balance: " + ChatColor.YELLOW + mineBal + " MINE").setScore(1);
    }
    /**
     * Centralized helper for fetching a player's wallet address
     */
    public String fetchWalletAddress(String playerUUID) {
        return this.db.fetchWalletAddress(playerUUID);
    }
    public String burnTokens(Player player, String amount) throws Exception {
        // skip the hasPermission check—this is a trusted API
        BurnCommand cmd = new BurnCommand(this);
        return cmd.burnTokens(player, amount);
      }
    
      public String claimTokens(Player player, String amount) throws Exception {
        ClaimCommand cmd = new ClaimCommand(this);
        return cmd.claimTokens(player, amount);
      }
    
      public String getBalance(Player player) throws Exception {
        BalanceCommand cmd = new BalanceCommand(this);
        return cmd.getBalance(player);
      }
}
