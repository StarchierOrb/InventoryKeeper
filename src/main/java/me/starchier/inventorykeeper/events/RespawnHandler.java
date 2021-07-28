package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.items.FoodLevel;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnHandler implements Listener {
    private final CommandExec commandExec;
    private final PluginHandler pluginHandler;
    private final InventoryKeeper plugin;

    public RespawnHandler(CommandExec commandExec, PluginHandler pluginHandler, InventoryKeeper plugin) {
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
        this.plugin = plugin;
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
            applyFoodLevel(evt.getPlayer(), finalFood, finalSaturation);
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: drop inventory");
            PlayerStorage.resetConsumed(evt.getPlayer());
            return;
        }
        commandExec.doRestoreModInventory(evt.getPlayer());
        commandExec.runCommands(evt.getPlayer(), false, consumedItem + ".run-commands-on-respawn", false);
        commandExec.runRandomCommands(evt.getPlayer(), false, consumedItem + ".run-random-commands-on-respawn", false);
        FoodLevel foodLevel = pluginHandler.getItemBase(consumedItem).getFoodLevel();
        int finalFood = foodLevel.getFinalFoodLevel(PlayerStorage.getFoodLevel(evt.getPlayer()));
        int finalSaturation = foodLevel.getFinalSaturationLevel(PlayerStorage.getSaturationLevel(evt.getPlayer()));
        applyFoodLevel(evt.getPlayer(), finalFood, finalSaturation);
        Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: consumed " + consumedItem);
        PlayerStorage.resetConsumed(evt.getPlayer());
    }

    private void applyFoodLevel(Player player, int finalFood, int finalSaturation) {
        new BukkitRunnable() {
            public void run() {
                player.setFoodLevel(finalFood);
                player.setSaturation(finalSaturation);
                PlayerStorage.removeFoodLevel(player);
                PlayerStorage.removeSaturationLevel(player);
            }
        }.runTaskLater(plugin, 15);
    }
}
