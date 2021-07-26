package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.items.FoodLevel;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

public class RespawnHandler implements Listener {
    private final CommandExec commandExec;
    private final PluginHandler pluginHandler;

    public RespawnHandler(CommandExec commandExec, PluginHandler pluginHandler) {
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
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
            int finalFood = pluginHandler.defaultFoodLevel.getFinalFoodLevel(PlayerStorage.getFoodLevel(evt.getPlayer()));
            int finalSaturation = pluginHandler.defaultFoodLevel.getFinalSaturationLevel(PlayerStorage.getSaturationLevel(evt.getPlayer()));
            evt.getPlayer().setFoodLevel(finalFood);
            evt.getPlayer().setSaturation(finalSaturation);
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: drop inventory");
            PlayerStorage.resetConsumed(evt.getPlayer());
            PlayerStorage.removeFoodLevel(evt.getPlayer());
            PlayerStorage.removeSaturationLevel(evt.getPlayer());
            return;
        }
        commandExec.doRestoreModInventory(evt.getPlayer());
        commandExec.runCommands(evt.getPlayer(), false, consumedItem + ".run-commands-on-respawn", false);
        commandExec.runRandomCommands(evt.getPlayer(), false, consumedItem + ".run-random-commands-on-respawn", false);
        FoodLevel foodLevel = pluginHandler.getItemBase(consumedItem).getFoodLevel();
        int finalFood = foodLevel.getFinalFoodLevel(PlayerStorage.getFoodLevel(evt.getPlayer()));
        int finalSaturation = foodLevel.getFinalSaturationLevel(PlayerStorage.getSaturationLevel(evt.getPlayer()));
        evt.getPlayer().setFoodLevel(finalFood);
        evt.getPlayer().setSaturation(finalSaturation);
        Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: consumed " + consumedItem);
        PlayerStorage.resetConsumed(evt.getPlayer());
        PlayerStorage.removeFoodLevel(evt.getPlayer());
        PlayerStorage.removeSaturationLevel(evt.getPlayer());
    }
}
