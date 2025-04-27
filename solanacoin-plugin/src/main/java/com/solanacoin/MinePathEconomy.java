package com.solanacoin;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;

import static com.solanacoin.MinePathCoinPlugin.NUMBER_FORMAT;


import java.util.List;


public class MinePathEconomy implements Economy {

    MinePathCoinPlugin plugin;

    public MinePathEconomy(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "MinePath Economy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return NUMBER_FORMAT.format(amount);
    }

    @Override
    public String currencyNamePlural() {
        return "USD";
    }

    @Override
    public String currencyNameSingular() {
        return "$";
    }

    @Override
    public boolean hasAccount(String playerName) {
        OfflinePlayer offlinePlayer = plugin.getServer().getOfflinePlayer(playerName);
        return hasAccount(offlinePlayer);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player) {
        return plugin.db.playerExistsInBalanceTable(player.getUniqueId());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(String playerName) {
        return getBalance(plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    public double getBalance(OfflinePlayer player) {
        return plugin.db.getBalanceOfPlayer(player.getUniqueId());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(OfflinePlayer player, String world) {
        return getBalance(player);
    }

    @Override
    public boolean has(String playerName, double amount) {
        return has(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    public boolean has(OfflinePlayer player, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(OfflinePlayer player, String worldName, double amount) {
        return getBalance(player) >= amount;
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        return withdrawPlayer(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, double amount) {

        double balance = getBalance(player);
        if (balance >= amount) {
            boolean success = plugin.db.addBalanceToPlayer(player.getUniqueId(), -amount);
            if (success) {
                return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
            } else {
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Something went wrong.");
            }
        } else {
            return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient balance in players account!");
        }

    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        return depositPlayer(plugin.getServer().getOfflinePlayer(playerName), amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, double amount) {
        boolean success = plugin.db.addBalanceToPlayer(player.getUniqueId(), amount);
        if (success) {
            return new EconomyResponse(amount, getBalance(player), EconomyResponse.ResponseType.SUCCESS, null);
        } else {
            return new EconomyResponse(0, getBalance(player), EconomyResponse.ResponseType.FAILURE, "Something went wrong.");
        }
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse createBank(String name, OfflinePlayer player) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse isBankOwner(String name, OfflinePlayer player) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public EconomyResponse isBankMember(String name, OfflinePlayer player) {
        return new EconomyResponse(0 , 0,EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
    }

    @Override
    public List<String> getBanks() {
        return null;
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        return createPlayerAccount(plugin.getServer().getOfflinePlayer(playerName));
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player) {
        return plugin.db.addPlayerToBalanceTable(player);
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }
}
