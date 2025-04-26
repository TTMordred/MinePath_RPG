package me.tien.nftminer.commands;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import me.tien.nftminer.NFTMiner;
import me.tien.nftminer.token.TokenManager;
import me.tien.nftminer.token.TokenManager.ClaimResult;
import me.tien.nftminer.upgrade.TokenValueUpgrade;

public class ClaimCommand implements CommandExecutor, TabCompleter {
    private final NFTMiner plugin;
    private final TokenManager tokenManager;
    private final TokenValueUpgrade tokenValueUpgrade;

    public ClaimCommand(NFTMiner plugin, TokenManager tokenManager, TokenValueUpgrade tokenValueUpgrade) {
        this.plugin = plugin;
        this.tokenManager = tokenManager;
        this.tokenValueUpgrade = tokenValueUpgrade;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cChỉ người chơi mới có thể sử dụng lệnh này!");
            return true;
        }

        Player player = (Player) sender;

        // Tự động đổi tất cả block có giá trị trong túi đồ
        ClaimResult result = tokenManager.claimBlocks(player);

        if (result.getTokensEarned().compareTo(BigDecimal.ZERO) > 0) {
            // Hiển thị thông báo thành công
            player.sendMessage("§a§l⚡ CLAIM THÀNH CÔNG! ⚡");
            player.sendMessage("§aBạn đã đổi §f" + result.getBlocksConverted() + " block §athành §e"
                    + result.getTokensEarned() + " token§a!");

            // Hiển thị chi tiết các block đã đổi
            player.sendMessage("§a§lChi tiết các block đã đổi:");
            double multiplier = tokenValueUpgrade.getValueMultiplier(player); // Lấy hệ số nhân hiện tại
            for (Map.Entry<Material, Integer> entry : result.getClaimedItems().entrySet()) {
                String blockName = formatMaterialName(entry.getKey());
                int amount = entry.getValue();
                BigDecimal baseValue = tokenManager.getBlockValue(entry.getKey()).multiply(BigDecimal.valueOf(amount));
                BigDecimal finalValue = baseValue.multiply(BigDecimal.valueOf(multiplier)); // Áp dụng hệ số nhân
                player.sendMessage(
                        "§f- " + amount + "x §e" + blockName + " §f→ §e" + finalValue + " token (x" + multiplier + ")");
            }

            // Hiển thị hệ số nhân giá trị token hiện tại
            player.sendMessage("§a§lHệ số nhân giá trị token hiện tại của bạn: §e" + multiplier + "x");

            // Hiển thị số token hiện tại
            player.sendMessage("§a§lSố token hiện tại: §e" + tokenManager.getTokens(player));

            // Phát hiệu ứng âm thanh
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.5f);
        } else {
            player.sendMessage("§eChỉ các block sau có thể đổi thành token: §f" +
                    String.join(", ", tokenManager.getSupportedBlocks().stream()
                            .map(Material::name)
                            .map(String::toLowerCase)
                            .map(name -> name.replace('_', ' '))
                            .toList()));
            // Phát hiệu ứng âm thanh thất bại
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
        }

        return true;
    }

    /**
     * Định dạng tên vật liệu để hiển thị thân thiện hơn
     */
    private String formatMaterialName(Material material) {
        String name = material.name();
        name = name.replace('_', ' ').toLowerCase();

        // Viết hoa chữ cái đầu của mỗi từ
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
        // Không cần tham số phụ nào nữa
        return new ArrayList<>();
    }
}