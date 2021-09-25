package me.starchier.inventorykeeper.api;

import me.starchier.inventorykeeper.manager.DataManager;
import me.starchier.inventorykeeper.manager.PluginHandler;

public class InvKeepAPI {
    private final DataManager dataManager;
    private final PluginHandler pluginHandler;

    public InvKeepAPI(DataManager dataManager, PluginHandler pluginHandler) {
        this.dataManager = dataManager;
        this.pluginHandler = pluginHandler;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }
}
