package me.starchier.inventorykeeper.api.events;

import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.manager.DataManager;
import me.starchier.inventorykeeper.manager.PluginHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class PlayerConsumeItemEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final ItemBase item;
    private final PluginHandler pluginHandler;
    private final DataManager dataManager;
    private final int consumeType;

    public PlayerConsumeItemEvent(Player player, ItemBase item, PluginHandler pluginHandler, DataManager dataManager, int consumeType) {
        this.player = player;
        this.item = item;
        this.pluginHandler = pluginHandler;
        this.dataManager = dataManager;
        this.consumeType = consumeType;
    }

    public PluginHandler getPluginHandler() {
        return pluginHandler;
    }

    public DataManager getDataManager() {
        return dataManager;
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

    public int getConsumeType() {
        return consumeType;
    }
}
