package me.tien.miner_simulator.commands;

import me.tien.miner_simulator.Miner_Simulator;
import me.tien.miner_simulator.world.VoidMine;
import me.tien.miner_simulator.world.VoidMine.PlayerMine;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MiningBoxCommand implements CommandExecutor {

    private final Miner_Simulator plugin;
    private final VoidMine voidMine;

    public MiningBoxCommand(Miner_Simulator plugin, VoidMine voidMine) {
        this.plugin = plugin;
        this.voidMine = voidMine;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;

        player.sendMessage(ChatColor.YELLOW + "Checking your mining area...");

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                PlayerMine mine = voidMine.getPlayerMine(player);
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    if (mine == null) {
                        player.sendMessage(ChatColor.YELLOW + "No mining area found. Creating one...");
                        PlayerMine newMine = voidMine.new PlayerMine(player);
                        newMine.teleportPlayer(player);
                    } else {
                        mine.createMineWorld(); // ensure world exists

                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                            mine.teleportPlayer(player);
                        }, 40L);
                    }
                });
            } catch (Exception e) {
                plugin.getServer().getScheduler().runTask(plugin, () -> {
                    player.sendMessage(ChatColor.RED + "An error occurred while teleporting you to your mining area.");
                    plugin.getLogger().warning("Teleport error for " + player.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                });
            }
        });

        return true;
    }
}
