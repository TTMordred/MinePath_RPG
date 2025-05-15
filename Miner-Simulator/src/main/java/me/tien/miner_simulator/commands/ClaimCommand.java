package me.tien.miner_simulator.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.token.TokenManager;
import me.tien.miner_simulator.token.TokenManager.ClaimResult;
import me.tien.miner_simulator.upgrade.TokenValueUpgrade;

public class ClaimCommand implements CommandExecutor, TabCompleter {
    private final Miner_Simulator plugin;
    private final TokenManager tokenManager;
    private final TokenValueUpgrade tokenValueUpgrade;

    public ClaimCommand(Miner_Simulator plugin, TokenManager tokenManager, TokenValueUpgrade tokenValueUpgrade) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.tokenValueUpgrade = tokenValueUpgrade;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // Automatically convert all valuable blocks in inventory
        ClaimResult result = tokenManager.claimBlocks(player);

        if (result.getTokensEarned().compareTo(BigDecimal.ZERO) > 0) {
            // Display success message
            player.sendMessage("§a§l⚡ CLAIM SUCCESSFUL! ⚡");
            player.sendMessage("§aYou converted §f" + result.getBlocksConverted() + " blocks §ainto §e"
                    + result.getTokensEarned() + " tokens§a!");

            // Display details of converted blocks
            player.sendMessage("§a§lDetails of converted blocks:");
            double multiplier = tokenValueUpgrade.getValueMultiplier(player); // Get current multiplier
            for (Map.Entry<Material, Integer> entry : result.getClaimedItems().entrySet()) {
                String blockName = formatMaterialName(entry.getKey());
                int amount = entry.getValue();
                BigDecimal baseValue = tokenManager.getBlockValue(entry.getKey()).multiply(BigDecimal.valueOf(amount));
                BigDecimal finalValue = baseValue.multiply(BigDecimal.valueOf(multiplier)); // Apply multiplier
                player.sendMessage(
                        "§f- " + amount + "x §e" + blockName + " §f→ §e" + finalValue + " tokens (x" + multiplier + ")");
            }

            // Display current token value multiplier
            player.sendMessage("§a§lYour current token value multiplier: §e" + multiplier + "x");

            // Display current tokens with clear formatting
            BigDecimal currentTokens = tokenManager.getTokens(player);
            plugin.getLogger().info("Tokens for " + player.getName() + ": " + currentTokens);
            player.sendMessage("§a§lCurrent tokens: §e" + currentTokens.toPlainString());

            // Play sound effect
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        } else {
            // Check if there are any blocks that can be converted
            boolean hasConvertibleBlocks = false;
            for (Material material : tokenManager.getSupportedBlocks()) {
                if (player.getInventory().contains(material)) {
                    hasConvertibleBlocks = true;
                    break;
                }
            }

            if (hasConvertibleBlocks) {
                // Blocked but transaction failed
                player.sendMessage("§c§lBLOCKCHAIN TRANSACTION FAILED!");
                player.sendMessage("§cItems have been returned to your inventory.");
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            } else {
                // No blocks can be transferred
                player.sendMessage("§eOnly these blocks can be converted to tokens: §f" +
                        String.join(", ", tokenManager.getSupportedBlocks().stream()
                                .map(Material::name)
                                .map(String::toLowerCase)
                                .map(name -> name.replace('_', ' '))
                                .collect(Collectors.toList())));
                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            }
        }

        return true;
    }

    /**
     * Format material name for more friendly display
     */
    private String formatMaterialName(Material material) {
        String name = material.name();
        name = name.replace('_', ' ').toLowerCase();

        // Capitalize first letter of each word
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = true;

        for (char c : name.toCharArray()) {
            if (c == ' ') {
                result.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                result.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        // No additional parameters needed
        return new ArrayList<>();
    }
}