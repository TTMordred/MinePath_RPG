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


import java.util.ArrayList;
import java.util.List;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class ExportCommand implements CommandExecutor {

    MinePathCoinPlugin plugin;

    public ExportCommand(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
        }
    /**
     * Creates the instruction data for the Solana transfer
     */
    public byte[] createTransferInstructionData(double amount) {
        // Create a standard SPL token transfer instruction (7 = Mint)
        ByteBuffer buffer = ByteBuffer.allocate(9);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.put((byte) 7); // Mint instruction for SPL token program
        
        // Amount in the smallest denomination (based on decimals)
        long transferAmount = (long) (amount * Math.pow(10,plugin.tokenDecimals));
        buffer.putLong(transferAmount);
        
        return buffer.array();
    }
    private PublicKey fetchAssociatedTokenAccount(PublicKey wallet) {
        PublicKey ata = plugin.getAssociatedTokenAddress(wallet, plugin.tokenMintAddress);
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
    private void addCreateAtaInstruction(PublicKey wallet, Transaction tx) {
        TransactionInstruction createIx = AssociatedTokenProgram.createIdempotent(
            plugin.signer.getPublicKey(),   // funding account
            wallet,                         // owner of the ATA
            plugin.tokenMintAddress         // the mint
        );
        plugin.getLogger().info("Creating ATA for " + wallet.toBase58()+ "......");
        tx.addInstruction(createIx);
    }
    /**
     * Handles the export command
     */    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        
        if (!plugin.hasPermission(sender, "minepath.export")) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You are not permitted to use that command.");
            return true;
        }
        
        if (!(sender instanceof Player pSender)) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Export not available to non-players.");
            return true;
        }
        
        if (args.length < 1) {
            sender.sendMessage(plugin.chatPrefix + "/MINEPATH:export <amount> [confirm]");
            return true;
        }
        
        // Parse amount
        double amount = 0;
        try {
            amount = Double.parseDouble(args[0]);
        } catch (NullPointerException | NumberFormatException e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Failed to parse <amount>. Please make sure you're sending a valid number.");
            return true;
        }
        
        // Check minimum export amount
        if (plugin.minimumExport > 0 && amount < plugin.minimumExport) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Minimum export amount is " + ChatColor.YELLOW + plugin.minimumExport + " " + ChatColor.RED + plugin.currencySymbol + ".");
            return true;
        } else if (amount <= 0) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "<amount> must be greater than" + ChatColor.YELLOW + " 0 " + ChatColor.RED + plugin.currencySymbol + ".");
            return true;
        }
        
        // Get player's wallet address from database
        String walletAddress = plugin.fetchWalletAddress(pSender.getUniqueId().toString());
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
        
        // Check player's balance
        double playerBalance = plugin.db.getBalanceOfPlayer(pSender.getUniqueId());
        if (playerBalance < amount) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Insufficient balance.");
            return true;
        }
        
        // Check rate limiting
        if (plugin.shouldRateLimit(pSender)) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.GRAY + "Command failed, rate limited.");
            return true;
        }
        // Deduct from player's balance
        plugin.db.addBalanceToPlayer(pSender.getUniqueId(), -amount);
        try {
            plugin.getLogger().info("Requested amount: " + amount);
            plugin.getLogger().info("Server token account: " + plugin.associatedTokenAddress.toBase58());
            plugin.getLogger().info("Token mint: " + plugin.tokenMintAddress.toBase58());
            
            // Check if confirmed
            if (args.length == 1 || !args[1].equalsIgnoreCase("confirm")) {
                sender.sendMessage(plugin.chatPrefix + ChatColor.AQUA + "Please confirm you want to export " + ChatColor.YELLOW + amount + " " + ChatColor.AQUA + plugin.currencySymbol + " to " + ChatColor.GOLD + toKey.toBase58() + ChatColor.AQUA + ".");
                sender.sendMessage(plugin.chatPrefix + "/MINEPATH:export " + amount + " confirm");
                plugin.db.addBalanceToPlayer(pSender.getUniqueId(), amount);
                return true;
            }
            // Process the transaction
            PublicKey fromKey = plugin.publicKey;
            PublicKey fromTokenPublicKey = plugin.associatedTokenAddress;
            plugin.getLogger().info("From Key: " + fromKey.toBase58() + " Token Account: " + fromTokenPublicKey.toBase58());
            // Replace old Mint call with new AssociatedTokenProgram method
            PublicKey toTokenPublicKey = fetchAssociatedTokenAccount(toKey);
            Transaction tx = new Transaction();
            if (toTokenPublicKey == null) {
                // Create the associated token account if it doesn't exist
                plugin.getLogger().info("Starting creating token account for " + toKey.toBase58());
                
                addCreateAtaInstruction(toKey, tx);
                toTokenPublicKey = plugin.getAssociatedTokenAddress(toKey, plugin.tokenMintAddress);
            }
            plugin.getLogger().info("To Key: " + toKey.toBase58() + " Token Account: " + toTokenPublicKey.toBase58());
            
            try {
                plugin.getLogger().info("TransferAmount (lamports) = " +
                (long)(amount * Math.pow(10, plugin.tokenDecimals)));
                // Use standard SPL token transfer instruction
                TransactionInstruction instruction = new TransactionInstruction(
                    new PublicKey("TokenkegQfeZyiNwAJbNbGKPFXCWuBvf9Ss623VQ5DA"), // Standard Token Program
                    List.of(
                        new AccountMeta(plugin.tokenMintAddress, false, true),  // from token account (writable)
                        new AccountMeta(toTokenPublicKey, false, true),    // to token account (writable)
                        new AccountMeta(plugin.signer.getPublicKey(), true, false)              // owner/signer
                    ),
                    createTransferInstructionData(amount)
                );

                tx.addInstruction(instruction);
                
                // IMPORTANT: Explicitly fetch the latest blockhash using getLatestBlockhash 
                // (instead of getRecentBlockhash, which is deprecated on Devnet)
                String latestBlockhash = plugin.rpcClient.getApi().getLatestBlockhash().getValue().getBlockhash();
                tx.setRecentBlockHash(latestBlockhash);
                plugin.getLogger().info("Latest Blockhash set: " + latestBlockhash);
                
                // Send the transaction using the built-in API
                List<Account> signers = new ArrayList<>();
                signers.add(plugin.signer);
                
                String signature = plugin.rpcClient.getApi().sendTransaction(tx, signers, latestBlockhash);
                plugin.getLogger().info("Transaction sent with signature: " + signature);
                sender.sendMessage(plugin.chatPrefix + ChatColor.GREEN + "Transaction sent!");
                plugin.sendURLToPlayer(pSender, "Check the Transaction Status", "https://solscan.io/tx/" + signature + "?cluster=devnet", MinePathCoinPlugin.TELLRAWCOLOR.yellow);
                
                // Handle transaction confirmation and retries
                new RetryExport(plugin, pSender, amount, tx, signature);
                
            } catch (RpcException e) {
                // Handle Solana RPC errors
                plugin.getLogger().severe("RPC Error: " + e.getMessage());
                plugin.getLogger().info("Using RPC endpoint: " + plugin.rpcClient.getEndpoint());
                // If the error indicates that the token account doesn't exist, notify the user accordingly
                if (e.getMessage().contains("account not found") || 
                    e.getMessage().contains("invalid account owner") || 
                    e.getMessage().contains("could not find account")) {
                    
                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "You need a token account for this token first.");
                    plugin.sendCopyableTextToPlayer(pSender, "Create a token account for: " + 
                        plugin.tokenMintAddress.toBase58(), plugin.tokenMintAddress.toBase58(), MinePathCoinPlugin.TELLRAWCOLOR.aqua);
                    plugin.sendURLToPlayer(pSender, "You can easily create one on Jupiter", "https://jup.ag/", MinePathCoinPlugin.TELLRAWCOLOR.yellow);
                    
                } else {
                    // Generic error message
                    sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error while sending transaction. Please try again or contact admin.");
                }
                
                // Refund balance
                plugin.db.addBalanceToPlayer(pSender.getUniqueId(), amount);
                return true;
            } catch (Exception e) {
                // Handle other errors
                sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error processing transaction. Please try again later.");
                plugin.getLogger().severe("Export error: " + e.getMessage());
                e.printStackTrace();
                
                // Refund balance
                plugin.db.addBalanceToPlayer(pSender.getUniqueId(), amount);
                return true;
            }
            
        } catch (Exception e) {
            sender.sendMessage(plugin.chatPrefix + ChatColor.RED + "Error preparing transaction. Please contact admin.");
            plugin.getLogger().severe("Export preparation error: " + e.getMessage());
            e.printStackTrace();
            plugin.db.addBalanceToPlayer(pSender.getUniqueId(), amount);
            return true;
        }
        
        return true;
    }
}
