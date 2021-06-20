package me.starchier.inventorykeeper;

import me.starchier.inventorykeeper.bStats.MetricsLite;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.command.CommandTab;
import me.starchier.inventorykeeper.events.*;
import me.starchier.inventorykeeper.hooks.PlaceholderAPIHook;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class InventoryKeeper extends JavaPlugin {
    @Override
    public void onEnable() {
        PluginHandler ph = new PluginHandler(this);
        getLogger().info("Server version: " + ph.getVersion() + (ph.isLegacy()?" (Legacy Mode)":""));
        getLogger().info("Loading Config...");
        File cfg = new File(getDataFolder(), "config.yml");
        if(!cfg.exists()) {
            saveDefaultConfig();
        }
        File cache = new File(getDataFolder(), "skull_cache.yml");
        if(!cache.exists()) {
            saveResource("skull_cache.yml", false);
        }
        File dataFile = new File(getDataFolder(), "data.yml");
        if(!dataFile.exists()) {
            saveResource("data.yml", false);
        }
        ItemHandler ih = new ItemHandler(this);
        if(!ih.isItem()) {
            getLogger().severe("Item "+ph.getSettings("keep-inventory-item.item-id") + " is not valid!");
            getLogger().severe("The item will be replaced to STICK!");
            ph.getCfg().set("settings.keep-inventory-item.item-id", "STICK");
        }
        ih.validEnchant();
        ih.cacheSkull();
        getLogger().info("Initializing player data...");
        DataManager dataManager = new DataManager(this, dataFile);
        dataManager.startupProcess();
        getLogger().info("Initializing commands...");
        getCommand("invkeep").setExecutor(new CommandTab(this, dataManager));
        getCommand("invkeep").setTabCompleter(new CommandTab(this, dataManager));
        getLogger().info("Initializing listeners...");
        CommandExec commandExec = new CommandExec(ph, this);
        Bukkit.getPluginManager().registerEvents(new DeathHandler(this, dataManager, commandExec),this);
        Bukkit.getPluginManager().registerEvents(new EntityDamageListener(), this);
        Bukkit.getPluginManager().registerEvents(new RespawnHandler(this, dataManager, commandExec, ph),this);
        Bukkit.getPluginManager().registerEvents(new PlayerDataInit(this, dataManager),this);
        //Bukkit.getPluginManager().registerEvents(new InventoryClickHandler(this),this);
        Bukkit.getPluginManager().registerEvents(new BlockPlacing(this),this);
        if(Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null){
            this.getLogger().info("  PlaceholderAPI Found! Hooking in...");
            new PlaceholderAPIHook(this, dataManager).register();
        }
        getLogger().info("Plugin loaded! Enjoy it!:)");
        getLogger().info("Welcome to donate us:");
        getLogger().info("paypal.me/starchier");
        MetricsLite metricsLite = new MetricsLite(this, 8286);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
