package me.starchier.inventorykeeper.api.events;

import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RespawnCompleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemBase item;
    private final PluginHandler pluginHandler;
    private final DataManager dataManager;

    public RespawnCompleteEvent(Player player, ItemBase item, PluginHandler pluginHandler, DataManager dataManager) {
        this.player = player;
        this.item = item;
        this.pluginHandler = pluginHandler;
        this.dataManager = dataManager;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemBase getItem() {
        return item;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public DataManager getDataManager() {
        return dataManager;
    }
}
