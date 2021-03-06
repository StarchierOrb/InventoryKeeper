package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.api.events.RespawnCompleteEvent;
import me.starchier.inventorykeeper.util.CommandExec;
import me.starchier.inventorykeeper.items.FoodLevel;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.storage.PlayerInventoryStorage;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.manager.DataManager;
import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.manager.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

public class RespawnListener implements Listener {
    private final CommandExec commandExec;
    private final PluginHandler pluginHandler;
    private final InventoryKeeper plugin;
    private final DataManager dataManager;

    public RespawnListener(CommandExec commandExec, PluginHandler pluginHandler, InventoryKeeper plugin, DataManager dataManager) {
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        ItemBase consumedItem = PlayerStorage.getConsumed(evt.getPlayer());
        if (consumedItem == null) {
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: no death cause");
            return;
        }
        if (consumedItem == PluginHandler.EMPTY_ITEM) {
            commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn-if-drops", true);
            commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn-if-drops", true);
            int finalFood = pluginHandler.defaultFoodLevel.getFinalFoodLevel(PlayerStorage.getFoodLevel(evt.getPlayer()));
            int finalSaturation = pluginHandler.defaultFoodLevel.getFinalSaturationLevel(PlayerStorage.getSaturationLevel(evt.getPlayer()));
            applyFoodLevel(evt.getPlayer(), finalFood, finalSaturation);
            callEvent(evt.getPlayer(), null);
            Debugger.logDebugMessage(evt.getPlayer().getName() + " Respawn: drop inventory");
            PlayerStorage.resetConsumed(evt.getPlayer());
            return;
        }
        restoreInventory(evt.getPlayer());
        commandExec.doRestoreModInventory(evt.getPlayer());
        commandExec.runCommands(evt.getPlayer(), false, consumedItem + ".run-commands-on-respawn", false);
        commandExec.runRandomCommands(evt.getPlayer(), false, consumedItem + ".run-random-commands-on-respawn", false);
        FoodLevel foodLevel = consumedItem.getFoodLevel();
        int finalFood = foodLevel.getFinalFoodLevel(PlayerStorage.getFoodLevel(evt.getPlayer()));
        int finalSaturation = foodLevel.getFinalSaturationLevel(PlayerStorage.getSaturationLevel(evt.getPlayer()));
        applyFoodLevel(evt.getPlayer(), finalFood, finalSaturation);
        callEvent(evt.getPlayer(), consumedItem);
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
        }.runTaskLater(plugin, 12);
    }

    private void restoreInventory(Player player) {
        new BukkitRunnable() {
            public void run() {
                if (pluginHandler.compatInventory) {
                    PlayerInventoryStorage storage = PlayerStorage.getInventory(player);
                    ItemStack[] items = storage.getItems();
                    for (int i = 0; i < player.getInventory().getSize(); i++) {
                        player.getInventory().setItem(i, items[i]);
                    }
                    player.getInventory().setArmorContents(storage.getArmor());
                    Debugger.logDebugMessage("restored " + player.getName() + "'s inventory");
                    PlayerStorage.removeInventory(player);
                }
                if (pluginHandler.compatLevel) {
                    player.setLevel(PlayerStorage.getLevel(player));
                    Debugger.logDebugMessage("restored " + player.getName() + "'s level");
                    PlayerStorage.removeLevel(player);
                }
            }
        }.runTaskLater(plugin, 10);
    }

    private void callEvent(Player player, ItemBase item) {
        new BukkitRunnable() {
            public void run() {
                Bukkit.getServer().getPluginManager().callEvent(new RespawnCompleteEvent(player,
                        item, pluginHandler, dataManager));
            }
        }.runTaskLater(plugin, 12);
    }
}
