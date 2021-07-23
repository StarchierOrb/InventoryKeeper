package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.Debugger;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnHandler implements Listener {
    private final CommandExec commandExec;

    public RespawnHandler(CommandExec commandExec) {
        this.commandExec = commandExec;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        String consumedItem;
        try {
            consumedItem = PlayerStorage.getConsumed(evt.getPlayer());
        } catch (NullPointerException e) {
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: no death cause");
            return;
        }
        if (consumedItem == null) {
            commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn-if-drops", true);
            commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn-if-drops", true);
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: drop inventory");
            PlayerStorage.resetConsumed(evt.getPlayer());
            return;
        }
        commandExec.doRestoreModInventory(evt.getPlayer());
        commandExec.runCommands(evt.getPlayer(), false, consumedItem + ".run-commands-on-respawn", false);
        commandExec.runRandomCommands(evt.getPlayer(), false, consumedItem + ".run-random-commands-on-respawn", false);
        Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: consumed " + consumedItem);
        PlayerStorage.resetConsumed(evt.getPlayer());
    }
}
