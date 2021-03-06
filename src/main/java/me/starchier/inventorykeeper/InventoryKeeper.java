package me.starchier.inventorykeeper;

import me.starchier.inventorykeeper.api.InvKeepAPI;
import me.starchier.inventorykeeper.bStats.MetricsLite;
import me.starchier.inventorykeeper.manager.DataManager;
import me.starchier.inventorykeeper.manager.PluginHandler;
import me.starchier.inventorykeeper.util.CommandExec;
import me.starchier.inventorykeeper.command.CommandInvKeep;
import me.starchier.inventorykeeper.configurations.GeneralConfig;
import me.starchier.inventorykeeper.configurations.ItemsConfig;
import me.starchier.inventorykeeper.events.*;
import me.starchier.inventorykeeper.hooks.PlaceholderAPIHook;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.util.*;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class InventoryKeeper extends JavaPlugin {
    public static boolean papiEnabled = false;
    public static InventoryKeeper instance = null;
    private InvKeepAPI api = null;

    @Override
    public void onEnable() {
        instance = this;
        PluginHandler ph = new PluginHandler(this);
        MessagesUtil.initMessageBundle();
        getLogger().info(MessagesUtil.getMessage("server-version") + PluginHandler.SERVER_VERSION + (PluginHandler.IS_LEGACY ? MessagesUtil.getMessage("is-legacy") : ""));
        getLogger().info(MessagesUtil.getMessage("loading-config"));
        ConversionUtil conversionUtil = new ConversionUtil(this);
        conversionUtil.convertConfig();
        conversionUtil.convertSkull();
        conversionUtil.convertData();
        File itemsConfig = new File(getDataFolder(), "items.yml");
        if (!itemsConfig.exists()) {
            saveResource("items.yml", false);
        }
        GeneralConfig.initConfig(this);
        ItemsConfig.initItemsConfig(this);
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
        ph.initConfigCache();
        ItemUtils ih = new ItemUtils(this, ph);
        Debugger.enabledDebug = ph.getBooleanConfigValue("debug", true);
        ph.loadItems(ih);
        getLogger().info(MessagesUtil.getMessage("init-player-data"));
        DataManager dataManager = new DataManager(dataFile, ph);
        dataManager.startupProcess();
        api = new InvKeepAPI(dataManager, ph);
        getLogger().info(MessagesUtil.getMessage("init-commands"));
        CommandInvKeep commandInvKeep = new CommandInvKeep(this, dataManager, ph, ih);
        getCommand("invkeep").setExecutor(commandInvKeep);
        getCommand("invkeep").setTabCompleter(commandInvKeep);
        getLogger().info(MessagesUtil.getMessage("init-listeners"));
        CommandExec commandExec = new CommandExec(ph, this);
        Bukkit.getPluginManager().registerEvents(new DeathListener(this, dataManager, commandExec, ph), this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnListener(commandExec, ph, this, dataManager), this);
        Bukkit.getPluginManager().registerEvents(new PlayerDataInit(dataManager), this);
        //Bukkit.getPluginManager().registerEvents(new InventoryClickHandler(this),this);
        Bukkit.getPluginManager().registerEvents(new BlockPlaceListener(ih, ph), this);
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getLogger().info(MessagesUtil.getMessage("papi-hook"));
            papiEnabled = true;
            new PlaceholderAPIHook(this, dataManager, ph).register();
        }
        getLogger().info(MessagesUtil.getMessage("plugin-loaded"));
        getLogger().info(MessagesUtil.getMessage("donate-msg"));
        getLogger().info(MessagesUtil.getMessage("donate-link"));
        try {
            new MetricsLite(this, 8286);
        } catch (Throwable e) {
            getLogger().warning(MessagesUtil.getMessage("cannot-init-metrics"));
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static InventoryKeeper getInstance() {
        return instance;
    }

    public InvKeepAPI getAPI() {
        return api;
    }

}
