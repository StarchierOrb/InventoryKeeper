package me.starchier.inventorykeeper.manager;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.items.FoodLevel;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.util.ItemUtils;
import me.starchier.inventorykeeper.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class PluginHandler {
    private final InventoryKeeper plugin;
    public static final String SERVER_VERSION = getVersion();
    public static final int FIXED_SERVER_VERSION = Integer.parseInt(getVersion().replace(".", "").replace("_", "").replace("v", "").replace("R", ""));
    public static final boolean IS_LEGACY = isLegacy();
    public FileConfiguration itemsConfig = null;
    public FileConfiguration generalConfig = null;
    public FileConfiguration skullCache = null;
    public List<ItemBase> currentItems = null;
    public List<String> itemNames = null;
    public FoodLevel defaultFoodLevel;
    public boolean compatInventory;
    public boolean compatLevel;
    public static final ItemBase EMPTY_ITEM = new ItemBase(null);

    public PluginHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }

    public void initConfigCache() {
        itemsConfig = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "items.yml"));
        generalConfig = plugin.getConfig();
        skullCache = YamlConfiguration.loadConfiguration(new File(plugin.getDataFolder(), "skull_cache.yml"));
        defaultFoodLevel = new FoodLevel(getConfigValue("default-hunger-level", true), getConfigValue("default-saturation-level", true));
        compatInventory = getBooleanConfigValue("compatibility-mode.inventory", true);
        compatLevel = getBooleanConfigValue("compatibility-mode.exp", true);
    }

    public static String getVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
    }

    public static boolean isLegacy() {
        return FIXED_SERVER_VERSION < 1131;
    }

    public String getConfigValue(String path, boolean isGeneralConfig) {
        String text;
        if (isGeneralConfig) {
            text = generalConfig.getString("settings." + path, null);
        } else {
            text = itemsConfig.getString("items." + path, null);
        }
        return (text == null ? null : StringUtil.transform(ChatColor.translateAlternateColorCodes('&', text)));
    }

    public Boolean getBooleanConfigValue(String path, boolean isGeneralConfig) {
        if (isGeneralConfig) {
            return generalConfig.getBoolean("settings." + path, true);
        } else {
            return itemsConfig.getBoolean("items." + path, true);
        }
    }

    public Boolean passConditionEntity(String entityName, String itemGroup) {
        // if true -> restore inventory
        boolean isBlacklist = getBooleanConfigValue(itemGroup + ".filter-entities-list.is-blacklist", false);
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
        boolean isBlacklist = getBooleanConfigValue(itemGroup + ".filter-entities-name.is-blacklist", false);
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
            return getBooleanConfigValue(itemGroup + ".filter-entities-list.is-blacklist", false);
        } else {
            return getBooleanConfigValue(itemGroup + ".filter-entities-name.is-blacklist", false);
        }
    }

    public String getMessage(String path) {
        return StringUtil.transform(ChatColor.translateAlternateColorCodes('&', generalConfig.getString("messages." + path, null)));
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
            s = StringUtil.transform(s);
            fixList.add(s);
        }
        return fixList;
    }

    public boolean isNumber(String s) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(s).matches();
    }

    public void loadItems(ItemUtils itemUtils) {
        currentItems = new ArrayList<>();
        itemNames = new ArrayList<>();
        Set<String> itemKeys;
        try {
            itemKeys = itemsConfig.getConfigurationSection("items").getKeys(false);
        } catch (NullPointerException e) {
            itemKeys = new HashSet<>();
        }
        for (String key : itemKeys) {
            String itemID = getConfigValue(key + ".item-id", false);
            if (!itemUtils.isItem(key)) {
                plugin.getLogger().warning(String.format(MessagesUtil.getMessage("item-not-valid"), itemID));
                continue;
            }
            itemUtils.validEnchant(key);
            itemUtils.cacheSkull(key);
            currentItems.add(new ItemBase(key, itemUtils, this));
            itemNames.add(key);
            plugin.getLogger().info(" - " + String.format(MessagesUtil.getMessage("loaded-item"), key));
        }
        plugin.getLogger().info("(!)" + String.format(MessagesUtil.getMessage("load-item-completed"), currentItems.size()));
        currentItems.sort((o1, o2) -> Integer.compare(o2.getPriority(), o1.getPriority()));
    }

    public List<String> getItemNames() {
        return itemNames;
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
