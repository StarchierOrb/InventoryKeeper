package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDataInit implements Listener {
    private InventoryKeeper plugin;
    private DataManager dataManager;
    public PlayerDataInit(InventoryKeeper plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        dataManager.createPlayer(evt.getPlayer());
    }
}
