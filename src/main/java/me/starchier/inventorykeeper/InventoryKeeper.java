package me.starchier.inventorykeeper;

import me.starchier.inventorykeeper.bStats.MetricsLite;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.command.CommandTab;
import me.starchier.inventorykeeper.configurations.GeneralConfig;
import me.starchier.inventorykeeper.events.*;
import me.starchier.inventorykeeper.hooks.PlaceholderAPIHook;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class InventoryKeeper extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginHandler ph = new PluginHandler(this);
        MessagesUtil.initMessageBundle();
        getLogger().info(MessagesUtil.getMessage("server-version") + ph.getVersion() + (ph.isLegacy() ? MessagesUtil.getMessage("is-legacy") : ""));
        getLogger().info(MessagesUtil.getMessage("loading-config"));
        File itemsConfig = new File(getDataFolder(), "items.yml");
        if (!itemsConfig.exists()) {
            saveResource("items.yml", false);
        }
        GeneralConfig.initConfig(this);
        File cache = new File(getDataFolder(), "skull_cache.yml");
        if (!cache.exists()) {
            try {
                cache.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        File dataFile = new File(getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ItemHandler ih = new ItemHandler(this);
        if(!ih.isItem()) {
            getLogger().severe(String.format(MessagesUtil.getMessage("item-not-valid"), ph.getSettings("keep-inventory-item.item-id")));
            getLogger().severe(MessagesUtil.getMessage("replace-not-valid-item"));
            ph.getCfg().set("settings.keep-inventory-item.item-id", "STICK");
        }
        ih.validEnchant();
        ih.cacheSkull();
        getLogger().info(MessagesUtil.getMessage("init-player-data"));
        DataManager dataManager = new DataManager(this, dataFile);
        dataManager.startupProcess();
        getLogger().info(MessagesUtil.getMessage("init-commands"));
        getCommand("invkeep").setExecutor(new CommandTab(this, dataManager));
        getCommand("invkeep").setTabCompleter(new CommandTab(this, dataManager));
        getLogger().info(MessagesUtil.getMessage("init-listeners"));
        CommandExec commandExec = new CommandExec(ph, this);
        Bukkit.getPluginManager().registerEvents(new DeathHandler(this, dataManager, commandExec), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnHandler(this, dataManager, commandExec, ph), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDataInit(this, dataManager), this);
        //Bukkit.getPluginManager().registerEvents(new InventoryClickHandler(this),this);
        Bukkit.getPluginManager().registerEvents(new BlockPlacing(this), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info(MessagesUtil.getMessage("papi-hook"));
            new PlaceholderAPIHook(this, dataManager).register();
        }
        getLogger().info(MessagesUtil.getMessage("plugin-loaded"));
        getLogger().info(MessagesUtil.getMessage("donate-msg"));
        getLogger().info(MessagesUtil.getMessage("donate-link"));
        MetricsLite metricsLite = new MetricsLite(this, 8286);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
