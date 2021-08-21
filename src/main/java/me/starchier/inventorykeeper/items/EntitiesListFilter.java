package me.starchier.inventorykeeper.items;

import me.starchier.inventorykeeper.manager.PluginHandler;

import java.util.List;

public class EntitiesListFilter {
    private final boolean isBlacklist;
    private final List<String> entities;

    public boolean isBlacklist() {
        return isBlacklist;
    }

    public List<String> getEntities() {
        return entities;
    }

    public EntitiesListFilter(String name, PluginHandler pluginHandler) {
        isBlacklist = pluginHandler.getBooleanConfigValue(name + ".filter-entities-list.is-blacklist", false);
        entities = pluginHandler.getList(name + ".filter-entities-list.entities", false);
    }
}
