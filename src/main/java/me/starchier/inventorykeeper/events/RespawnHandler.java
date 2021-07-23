package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RespawnHandler implements Listener {
    private final InventoryKeeper plugin;
    private final DataManager dataManager;
    private final CommandExec commandExec;
    private final PluginHandler pluginHandler;

    public RespawnHandler(InventoryKeeper plugin, DataManager dataManager, CommandExec commandExec, PluginHandler pluginHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        String consumedItem;
        try {
            consumedItem = PlayerStorage.getConsumed(evt.getPlayer());
        } catch (NullPointerException e) {
            return;
        }
        if (consumedItem == null) {
            commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn-if-drops", true);
            commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn-if-drops", true);
            PlayerStorage.resetConsumed(evt.getPlayer());
            return;
        }
        commandExec.doRestoreModInventory(evt.getPlayer());
        commandExec.runCommands(evt.getPlayer(), false, consumedItem + ".run-commands-on-respawn", false);
        commandExec.runRandomCommands(evt.getPlayer(), false, consumedItem + ".run-random-commands-on-respawn", false);
    }
}
