package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.util.DataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerDataInit implements Listener {
    private final DataManager dataManager;
    public PlayerDataInit(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent evt) {
        dataManager.createPlayer(evt.getPlayer());
    }
}
