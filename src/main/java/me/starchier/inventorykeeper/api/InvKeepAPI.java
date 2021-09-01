package me.starchier.inventorykeeper.api;

import me.starchier.inventorykeeper.manager.DataManager;

public class InvKeepAPI {
    private final DataManager dataManager;

    public InvKeepAPI(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
