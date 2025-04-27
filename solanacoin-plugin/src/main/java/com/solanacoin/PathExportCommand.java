package com.solanacoin;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.p2p.solanaj.core.*;
import org.p2p.solanaj.programs.AssociatedTokenProgram;
import org.p2p.solanaj.rpc.RpcException;
import org.p2p.solanaj.rpc.types.SplTokenAccountInfo;
import org.p2p.solanaj.rpc.types.TokenResultObjects.TokenAmountInfo;
import com.solanacoin.util.Base58;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class PathExportCommand implements CommandExecutor {
    private final MinePathCoinPlugin plugin;
    private final double pathRate;
    private final Account pathSigner;
    private final PublicKey pathPublicKey;
    private final PublicKey pathMint;
    private final PublicKey pathAssociatedTokenAddress;
    private final int pathDecimals;

    public PathExportCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
        this.pathRate = plugin.getConfig().getDouble("pathRate", 100.0);

        String signerKey = plugin.getConfig().getString("pathSigner", "");
        this.pathSigner = new Account(Base58.decode(signerKey));

        String pubKey = plugin.getConfig().getString("pathPublicKey", "");
        this.pathPublicKey = new PublicKey(pubKey);

        String mintKey = plugin.getConfig().getString("pathMint", "");
        this.pathMint = new PublicKey(mintKey);

        int decimals = 0;
        try {
            TokenAmountInfo supply = plugin.rpcClient.getApi().getTokenSupply(pathMint);
            decimals = supply.getDecimals();
            plugin.getLogger().info("Successfully fetch token PATH decimals: " + decimals);
        } catch (RpcException e) {
            plugin.getLogger().severe("Failed to fetch PATH token decimals: " + e.getMessage());
        }
        this.pathDecimals = decimals;
        this.pathAssociatedTokenAddress = plugin.getAssociatedTokenAddress(pathPublicKey, pathMint);
        plugin.getServer().getConsoleSender().sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Token PATH load successfully. " + pathMint.toBase58());
    }
    public PublicKey getMint(){
        return pathMint;
    }
    private PublicKey fetchAssociatedTokenAccount(PublicKey wallet) {
        PublicKey ata = plugin.getAssociatedTokenAddress(wallet, pathMint);
        try {
            SplTokenAccountInfo info = plugin.rpcClient
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
    public byte[] createTransferInstructionData(double amount) {
        // Create a standard SPL token transfer instruction (3 = Transfer)
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 7); // Mint instruction for SPL token program
        
        // Amount in the smallest denomination (based on decimals)
        long transferAmount = (long) (amount * Math.pow(10, pathDecimals));
        buffer.putLong(transferAmount);
        
        return buffer.array();
    }
    private void addCreateAtaInstruction(PublicKey wallet, Transaction tx) {
        TransactionInstruction createIx = AssociatedTokenProgram.createIdempotent(
            pathPublicKey,   // funding account
            wallet,                         // owner of the ATA
            pathMint         // the mint
        );
        plugin.getLogger().info("Almost there ! ATA for " + wallet.toBase58()+ "......");
        tx.addInstruction(createIx);
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Only players can export PATH.");
            return true;
        }
        if (!plugin.hasPermission(sender, "minepath.export.path")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Missing permission.");
            return true;
        }
        if (args.length < 1) {
            sender.sendMessage(plugin.chatPrefix + "/MINEPATH:exportpath <pathAmount> [confirm]");
            return true;
        }

        double pathAmt;
        try {
            pathAmt = Double.parseDouble(args[0]);
        } catch (NumberFormatException e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Invalid PATH amount.");
            return true;
        }
        double mineAmt = pathAmt * pathRate;

        double dbBal = plugin.db.getBalanceOfPlayer(p.getUniqueId());
        if (dbBal < mineAmt) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED +
                "Insufficient mine balance. You need " + mineAmt + " mine for " + pathAmt + " PATH.");
            return true;
        }
        if (plugin.shouldRateLimit(p)) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Rate limited.");
            return true;
        }

        if (args.length < 2 || !args[1].equalsIgnoreCase("confirm")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.AQUA +
                "Export " + pathAmt + " PATH for " + mineAmt + " mine. Confirm?"
            );
            sender.sendMessage(plugin.chatPrefix + "/MINEPATH:exportpath " + pathAmt + " confirm");
            return true;
        }
        String walletAddress = plugin.fetchWalletAddress(p.getUniqueId().toString());
        if (walletAddress == null || walletAddress.isEmpty()) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You don't have a wallet address linked. Please link your wallet first.");
            return true;
        }
        PublicKey toKey;
        try {
            toKey = new PublicKey(walletAddress);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Your wallet address is invalid. Please link a valid wallet.");
            return true;
        }
        // Deduct player balance
        plugin.db.addBalanceToPlayer(p.getUniqueId(), -mineAmt);
        try {
            plugin.getLogger().info("Requested amount: " + pathAmt);
            plugin.getLogger().info("Server token account: " + pathAssociatedTokenAddress);
            plugin.getLogger().info("Token mint: " + pathMint.toBase58());
            // Deduct player balance
            plugin.db.addBalanceToPlayer(p.getUniqueId(), -mineAmt);
            // Print Log
            plugin.getLogger().info("From Key: " + pathPublicKey.toBase58() + " Token Account: " + pathAssociatedTokenAddress.toBase58());
            PublicKey toTokenPublicKey = fetchAssociatedTokenAccount(toKey);
            Transaction tx = new Transaction();
            if (toTokenPublicKey == null) {
                // Create the associated token account if it doesn't exist
                plugin.getLogger().info("Starting creating token account for " + toKey.toBase58());
                
                addCreateAtaInstruction(toKey, tx);
                toTokenPublicKey = plugin.getAssociatedTokenAddress(toKey, pathMint);
            }
            plugin.getLogger().info("To Key: " + toKey.toBase58() + " Token Account: " + toTokenPublicKey.toBase58());
            try{
                plugin.getLogger().info("TransferAmount (lamports) = " +
                (long)(pathAmt * Math.pow(10, pathDecimals)));
                TransactionInstruction instruction = new TransactionInstruction(
                    new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), // Standard Token Program
                    List.of(
                        new AccountMeta(pathMint, false, true),  // from token account (writable)
                        new AccountMeta(toTokenPublicKey, false, true),    // to token account (writable)
                        new AccountMeta(pathPublicKey, true, false)              // owner/signer
                    ),
                    createTransferInstructionData(pathAmt)
                );
                tx.addInstruction(instruction);
                String latestBlockhash = plugin.rpcClient.getApi().getLatestBlockhash().getValue().getBlockhash();
                tx.setRecentBlockHash(latestBlockhash);
                plugin.getLogger().info("Latest Blockhash set: " + latestBlockhash);
                
                // Send the transaction using the built-in API
                List<Account> signers = new ArrayList<>();
                signers.add(pathSigner);
                
                String signature = plugin.rpcClient.getApi().sendTransaction(tx, signers, latestBlockhash);
                plugin.getLogger().info("Transaction sent with signature: " + signature);
                sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Transaction sent!");
                plugin.sendURLToPlayer(p, "Check the Transaction Status", "https://solscan.io/tx/" + signature + "?cluster=devnet", MinePathCoinPlugin.TELLRAWCOLOR.yellow);
                
                // Handle transaction confirmation and retries
                new RetryExport(plugin, p, pathAmt, tx, signature);
            }catch (RpcException e){
                plugin.db.addBalanceToPlayer(p.getUniqueId(), mineAmt);
                sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error while sending transaction. Please try again or contact admin.");
                return true;
            }
        }catch(Exception e){
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error while sending transaction. Please try again or contact admin.");
            plugin.db.addBalanceToPlayer(p.getUniqueId(), mineAmt);
            return true;
        }
        return true;
    }
}
