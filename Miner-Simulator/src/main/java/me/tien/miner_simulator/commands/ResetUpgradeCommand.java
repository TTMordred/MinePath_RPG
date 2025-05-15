package me.tien.miner_simulator.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.upgrade.UpgradeManager;

public class ResetUpgradeCommand implements CommandExecutor {
    private final Miner_Simulator plugin;
    private final UpgradeManager upgradeManager;

    public ResetUpgradeCommand(Miner_Simulator plugin, UpgradeManager upgradeManager) {
        this.plugin = plugin;
        this.upgradeManager = upgradeManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        // Reset all upgrade levels
        upgradeManager.getInventoryUpgrade().setLevel(player, 0);
        upgradeManager.getSpeedUpgrade().setLevel(player, 0);
        upgradeManager.getTokenValueUpgrade().setLevel(player, 0);

        // Save data
        upgradeManager.saveAllData();

        // Notify player
        player.sendMessage("§aAll your upgrade levels have been reset!");
        plugin.getLogger().info("Player " + player.getName() + " has reset all upgrade levels.");

        return true;
    }
}