package me.starchier.inventorykeeper.items;

import me.starchier.inventorykeeper.manager.PluginHandler;

import java.util.List;

public class EntitiesNameFilter {
    private final boolean isBlacklist;
    private final List<String> nameList;

    public EntitiesNameFilter(String name, PluginHandler pluginHandler) {
        isBlacklist = pluginHandler.getBooleanConfigValue(name + ".filter-entities-name.is-blacklist", false);
        nameList = pluginHandler.getList(name + ".filter-entities-name.names-list", false);
    }

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public List<String> getNameList() {
        return nameList;
    }
}
