package me.tien.miner_simulator.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import me.tien.miner_simulator.Miner_Simulator;

public class HelpCommand implements CommandExecutor {
    private final Miner_Simulator plugin;

    public HelpCommand(Miner_Simulator plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        sender.sendMessage("§a§l╔═══════ §e§lNFTMiner Help §a§l═══════╗");

        // Basic commands
        sender.sendMessage("§a§l║ §e/claim §f- Convert blocks to tokens");
        sender.sendMessage("§a§l║ §e/token §f- View your tokens and block values");
        sender.sendMessage("§a§l║ §e/shop §f- Open the upgrade shop");
        sender.sendMessage("§a§l║ §e/miningbox §f- Teleport to your mining area");
        sender.sendMessage("§a§l║ §e/resetmine §f- Reset your mining area");

        // Game mechanics info
        sender.sendMessage("§a§l╠═══════ §6§lInventory §a§l═══════╣");
        sender.sendMessage("§a§l║ §f- In the MiningBox world, inventory slots are locked by glass panes");
        sender.sendMessage("§a§l║ §f- Click on glass panes to unlock slots using tokens");
        sender.sendMessage("§a§l║ §f- Upgrade inventory space at §e/shop §fto reduce costs");

        // Only show admin commands to players with appropriate permissions
        if (sender.hasPermission("miner_simulator.resetupgrades")) {
            sender.sendMessage("§a§l╠═══════ §c§lAdmin Commands §a§l═══════╣");
            sender.sendMessage("§a§l║ §c/resetupgrade §f- Reset all upgrades");
            sender.sendMessage("§a§l║ §c/nftminer reload §f- Reload configuration");
        }

        sender.sendMessage("§a§l╚════════════════════════╝");

        return true;
    }
}