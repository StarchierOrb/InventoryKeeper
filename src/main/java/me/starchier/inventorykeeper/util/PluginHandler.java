package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.items.ItemBase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class PluginHandler {
    InventoryKeeper plugin;
    public static final String SERVER_VERSION = getVersion();
    public static final boolean IS_LEGACY = isLegacy();
    public FileConfiguration itemsConfig = null;
    public FileConfiguration generalConfig = null;
    public FileConfiguration skullCache = null;
    public List<ItemBase> currentItems = null;

    public PluginHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }

    public void initConfigCache() {
        this.itemsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "items.yml"));
        this.generalConfig = plugin.getConfig();
        this.skullCache = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "skull_cache.yml"));
    }

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static boolean isLegacy() {
        String ver = getVersion().replace(".", "").replace("_", "").replace("v", "").
                replace("_", "").replace("R", "");
        return Integer.parseInt(ver) < 1131;
    }

    public String getConfigValue(String path, boolean isGeneralConfig) {
        if (isGeneralConfig) {
            return ChatColor.translateAlternateColorCodes('&', generalConfig.getString("settings." + path, null));
        } else {
            return ChatColor.translateAlternateColorCodes('&', itemsConfig.getString("items." + path, null));
        }
    }

    public Boolean getBoolCfg(String path, boolean isGeneralConfig) {
        if (isGeneralConfig) {
            return generalConfig.getBoolean("settings." + path, true);
        } else {
            return itemsConfig.getBoolean("items." + path, true);
        }
    }

    public Boolean passConditionEntity(String entityName, String itemGroup) {
        // if true -> restore inventory
        boolean isBlacklist = getBoolCfg(itemGroup + ".filter-entities-list.is-blacklist", false);
        //String entity = entityName.split("\\|")[0];
        List<String> entitiesList = getList(itemGroup + ".filter-entities-list.entities", false);
        for (String s : entitiesList) {
            if (entityName.split("\\|")[0].equalsIgnoreCase(s)) {
                return !isBlacklist;
            }
        }
        return isBlacklist;
    }

    public Boolean passConditionEntityName(String name, String itemGroup) {
        // if true -> restore inventory
        String target = name.split("\\|")[1];
        boolean isBlacklist = getBoolCfg(itemGroup + ".filter-entities-name.is-blacklist", false);
        List<String> nameList = getList(itemGroup + ".filter-entities-name.names-list", false);
        for (String s : nameList) {
            String fixed = ChatColor.translateAlternateColorCodes('&', s);
            if (target.equalsIgnoreCase(fixed)) {
                return !isBlacklist;
            }
        }
        return isBlacklist;
    }

    public Boolean isBlackList(boolean isEntityList, String itemGroup) {
        if (isEntityList) {
            return getBoolCfg(itemGroup + ".filter-entities-list.is-blacklist", false);
        } else {
            return getBoolCfg(itemGroup + ".filter-entities-name.is-blacklist", false);
        }
    }

    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', generalConfig.getString("messages." + path, null));
    }

    public List<String> getList(String path, boolean isGeneralConfig) {
        List<String> originList;
        if (isGeneralConfig) {
            originList = generalConfig.getStringList(path);
        } else {
            originList = itemsConfig.getStringList("items." + path);
        }
        List<String> fixList = new ArrayList<>();
        for (String s : originList) {
            s = ChatColor.translateAlternateColorCodes('&', s);
            fixList.add(s);
        }
        return fixList;
    }

    public boolean isNumber(String s) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(s).matches();
    }

    public void loadItems(ItemHandler itemHandler) {
        if (currentItems != null) {
            currentItems = null;
        }
        //TODO verify if the material exists.
        Set<String> itemKeys = itemsConfig.getConfigurationSection("items").getKeys(false);
        for (String key : itemKeys) {
            currentItems.add(new ItemBase(key, itemHandler, this));
            plugin.getLogger().info(String.format(MessagesUtil.getMessage("loaded-item"), key));
        }
    }

    public ItemBase getItemBase(String name) {
        for (ItemBase item : currentItems) {
            if (item.getName().equals(name)) {
                return item;
            }
        }
        return null;
    }

    public List<String> getDisabledWorlds(String itemGroup) {
        return getList(itemGroup + ".disabled-worlds", false);
    }
}
