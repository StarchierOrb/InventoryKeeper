package me.starchier.inventorykeeper.items;

import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;

public class ItemBase {
    private final String name;
    private final ItemStack item;
    private final boolean saveExp;
    private final String expLostPercentage;
    private final List<String> disabledWorlds;
    private final List<String> runCommandsOnDeath;
    private final List<String> runRandomCommandsOnDeath;
    private final List<String> runCommandsOnRespawn;
    private final List<String> runRandomCommandsOnRespawn;
    private final List<String> removeItemsWithLore;
    private final int priority;
    private final EntitiesListFilter entitiesListFilter;
    private final EntitiesNameFilter entitiesNameFilter;
    private final HashMap<String, Boolean> enabledDeathType;
    private final String deathMessage;

    public ItemBase(String name, ItemHandler itemHandler, PluginHandler pluginHandler) {
        this.name = name;
        item = itemHandler.buildItem(name);
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
        enabledDeathType = (HashMap<String, Boolean>) pluginHandler.itemsConfig.getMapList("items." + name + ".enabled-death-type").get(0);
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

    public EntitiesNameFilter getEntitiesNameFilter() {
        return entitiesNameFilter;
    }

    public HashMap<String, Boolean> getEnabledDeathType() {
        return enabledDeathType;
    }

}
