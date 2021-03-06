package me.starchier.inventorykeeper.items;

import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.util.ItemUtils;
import me.starchier.inventorykeeper.manager.PluginHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemBase {
    private final String name;
    private final ItemStack item;
    private final boolean saveExp;
    private final String expLostPercentage;
    private final String displayName;
    private final List<String> disabledWorlds;
    private final List<String> runCommandsOnDeath;
    private final List<String> runRandomCommandsOnDeath;
    private final List<String> runCommandsOnRespawn;
    private final List<String> runRandomCommandsOnRespawn;
    private final List<String> removeItemsWithLore;
    private final int priority;
    private final EntitiesListFilter entitiesListFilter;
    private final EntitiesNameFilter entitiesNameFilter;
    private final HashMap<String, Boolean> enabledDeathType = new HashMap<>();
    private final String deathMessage;
    private final FoodLevel foodLevel;


    public ItemBase(String name, ItemUtils itemUtils, PluginHandler pluginHandler) {
        this.name = name;
        item = itemUtils.buildItem(name);
        displayName = pluginHandler.getConfigValue(name + ".name", false);
        saveExp = pluginHandler.getBooleanConfigValue(name + ".save-exp", false);
        expLostPercentage = pluginHandler.getConfigValue(name + ".exp-lose-percentage", false);
        disabledWorlds = pluginHandler.getDisabledWorlds(name);
        runCommandsOnDeath = pluginHandler.getList(name + ".run-commands-on-death", false);
        runRandomCommandsOnDeath = pluginHandler.getList(name + ".run-random-commands-on-death", false);
        runCommandsOnRespawn = pluginHandler.getList(name + ".run-commands-on-respawn", false);
        runRandomCommandsOnRespawn = pluginHandler.getList(name + ".run-random-commands-on-respawn", false);
        entitiesListFilter = new EntitiesListFilter(name, pluginHandler);
        entitiesNameFilter = new EntitiesNameFilter(name, pluginHandler);
        removeItemsWithLore = pluginHandler.getList(name + ".items-with-lore-to-be-removed-on-death", false);
        priority = pluginHandler.itemsConfig.getInt("items." + name + ".priority", 1);
        deathMessage = pluginHandler.getConfigValue(name + ".death-message", false);
        foodLevel = new FoodLevel(pluginHandler.getConfigValue(name + ".hunger-level", false), pluginHandler.getConfigValue(name + ".saturation-level", false));
        Map<String, Object> typeList = pluginHandler.itemsConfig.getConfigurationSection("items." + name + ".enabled-death-type").getValues(false);
        for (Map.Entry<String, Object> entry : typeList.entrySet()) {
            try {
                enabledDeathType.put(entry.getKey(), (Boolean) entry.getValue());
            } catch (ClassCastException e) {
                Debugger.logDebugMessage("value of" + entry.getKey() + " is not boolean: " + e.getMessage());
                enabledDeathType.put(entry.getKey(), true);
            }
        }
        Debugger.logDebugMessage("loaded " + enabledDeathType.size() + " death types");
    }

    public ItemBase(String name) {
        this.name = name;
        item = null;
        saveExp = false;
        expLostPercentage = null;
        displayName = null;
        disabledWorlds = null;
        runCommandsOnDeath = null;
        runRandomCommandsOnDeath = null;
        runCommandsOnRespawn = null;
        runRandomCommandsOnRespawn = null;
        removeItemsWithLore = null;
        priority = 0;
        entitiesListFilter = null;
        entitiesNameFilter = null;
        deathMessage = null;
        foodLevel = null;
    }

    public ItemStack getItem() {
        return item;
    }

    public String getName() {
        return name;
    }

    public String getDeathMessage() {
        return deathMessage;
    }

    public boolean isSaveExp() {
        return saveExp;
    }

    public String getExpLostPercentage() {
        return expLostPercentage;
    }

    public List<String> getDisabledWorlds() {
        return disabledWorlds;
    }

    public List<String> getRunCommandsOnDeath() {
        return runCommandsOnDeath;
    }

    public List<String> getRunRandomCommandsOnDeath() {
        return runRandomCommandsOnDeath;
    }

    public List<String> getRunCommandsOnRespawn() {
        return runCommandsOnRespawn;
    }

    public List<String> getRunRandomCommandsOnRespawn() {
        return runRandomCommandsOnRespawn;
    }

    public List<String> getRemoveItemsWithLore() {
        return removeItemsWithLore;
    }

    public int getPriority() {
        return priority;
    }

    public EntitiesListFilter getEntitiesListFilter() {
        return entitiesListFilter;
    }

    public String getDisplayName() {
        return displayName;
    }

    public EntitiesNameFilter getEntitiesNameFilter() {
        return entitiesNameFilter;
    }

    public HashMap<String, Boolean> getEnabledDeathType() {
        return enabledDeathType;
    }

    public FoodLevel getFoodLevel() {
        return foodLevel;
    }

    @Override
    public String toString() {
        return name;
    }
}
