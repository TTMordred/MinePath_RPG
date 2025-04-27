package com.solanacoin;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;

import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;

public class VaultIntegration {
    private Economy econ = null;
    private Permission perms = null;
    private Chat chat = null;
    MinePathCoinPlugin plugin;

    public VaultIntegration(MinePathCoinPlugin plugin) {
        this.plugin = plugin;
        this.setupEconomy();
        this.setupChat();
        this.setupPermissions();
    }


    private boolean setupEconomy() {

        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        if (econ == null) {
            econ = new MinePathEconomy(plugin);
        }

        plugin.getServer().getServicesManager().register(Economy.class, econ, plugin, ServicePriority.High);

        return econ != null;
    }

    private boolean setupChat() {
        RegisteredServiceProvider<Chat> rsp = plugin.getServer().getServicesManager().getRegistration(Chat.class);
        if (rsp == null) {
            return false;
        }
        chat = rsp.getProvider();
        return chat != null;
    }

    private boolean setupPermissions() {
        RegisteredServiceProvider<Permission> rsp = plugin.getServer().getServicesManager().getRegistration(Permission.class);
        if (rsp == null) {
            return false;
        }
        perms = rsp.getProvider();
        return perms != null;
    }

    public Economy getEconomy() {
        return econ;
    }

    public Permission getPermissions() {
        return perms;
    }

    public Chat getChat() {
        return chat;
    }
}
