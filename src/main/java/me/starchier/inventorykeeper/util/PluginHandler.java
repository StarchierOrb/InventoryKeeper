package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class PluginHandler {
    InventoryKeeper plugin;
    public PluginHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }
    public String getVersion() {
        return plugin.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }
    public boolean isLegacy() {
        String ver = getVersion().replace(".","").replace("_","").replace("v","").
                        replace("_","").replace("R","");
        return Integer.parseInt(ver) < 1131;
    }
    public String getSettings(String path) {
        return plugin.getConfig().getString("settings."+path, null).replace("&","§");
    }
    public Boolean getBoolCfg(String path) {
        return plugin.getConfig().getBoolean("settings." + path, true);
    }
    public Boolean getStatByEntity(String entityName) {
        // if true -> restore inventory
        boolean isBlacklist = getBoolCfg("filter-entities-list.is-blacklist");
        //String entity = entityName.split("\\|")[0];
        List<String> entitiesList = getList("settings.filter-entities-list.entities");
        for(String s : entitiesList) {
            if(entityName.split("\\|")[0].equalsIgnoreCase(s)) {
                return !isBlacklist;
            }
        }
        return isBlacklist;
    }
    public Boolean getStatByName(String name) {
        // if true -> restore inventory
        String target = name.split("\\|")[1];
        boolean isBlacklist = getBoolCfg("filter-entities-name.is-blacklist");
        List<String> nameList = getList("settings.filter-entities-name.names-list");
        for(String s : nameList) {
            String fixed = ChatColor.translateAlternateColorCodes('&', s);
            if(target.equalsIgnoreCase(fixed)) {
                return !isBlacklist;
            }
        }
        return isBlacklist;
    }
    public Boolean isBlackList(boolean isEntityList) {
        if(isEntityList) {
            return getBoolCfg("filter-entities-list.is-blacklist");
        } else {
            return getBoolCfg("filter-entities-name.is-blacklist");
        }
    }
    public String getMsg(String path) {
        return plugin.getConfig().getString("messages."+path, null).replace("&","§");
    }
    public List<String> getList(String path) {
        List<String> fixList=new ArrayList<>();
        for(String s : plugin.getConfig().getStringList(path)) {
            s = ChatColor.translateAlternateColorCodes('&', s);
            fixList.add(s);
        }
        return fixList;
    }
    public boolean isEmpty(String path) {
        return plugin.getConfig().getStringList("settings."+path).isEmpty();
    }
    public boolean isNumber(String s) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(s).matches();
    }
    public FileConfiguration getCfg() {
        return plugin.getConfig();
    }
    public List<String> getDisableWorlds() {
        return getList("settings.disabled-worlds");
    }
}
