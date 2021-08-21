package me.starchier.inventorykeeper.api.events;

import me.starchier.inventorykeeper.manager.DataManager;
import me.starchier.inventorykeeper.manager.PluginHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerDropInventoryEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final PluginHandler pluginHandler;
    private final DataManager dataManager;

    public PlayerDropInventoryEvent(Player player, PluginHandler pluginHandler, DataManager dataManager) {
        this.player = player;
        this.dataManager = dataManager;
        this.pluginHandler = pluginHandler;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public Player getPlayer() {
        return player;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
