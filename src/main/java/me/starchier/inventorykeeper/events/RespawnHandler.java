package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.GameRule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class RespawnHandler implements Listener {
    private InventoryKeeper plugin;
    private DataManager dataManager;
    private CommandExec commandExec;
    private PluginHandler ph;
    public RespawnHandler(InventoryKeeper plugin, DataManager dataManager, CommandExec commandExec, PluginHandler ph) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.commandExec = commandExec;
        this.ph = ph;
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent evt) {
        ItemHandler ih = new ItemHandler(plugin);
        for (String s : ph.getDisabledWorlds()) {
            if (evt.getPlayer().getWorld().getName().equals(s)) {
                return;
            }
        }
        if(ph.isLegacy()) {
            if(evt.getPlayer().getWorld().getGameRuleValue("keepInventory").equals("true")) {
                return;
            }
        } else {
            if(evt.getPlayer().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY)) {
                return;
            }
        }
        int i;
        if(evt.getPlayer().hasPermission("inventorykeeper.keep") && PlayerStorage.isConsumed(evt.getPlayer())) {
            commandExec.doRestoreModInventory(evt.getPlayer());
            commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn");
            commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn");
            return;
        }
        boolean hasItem = false;
        for(i=0;i<evt.getPlayer().getInventory().getSize();i++) {
            if(evt.getPlayer().getInventory().getItem(i)==null) {
                continue;
            }
            try {
                if (evt.getPlayer().getInventory().getItem(i).isSimilar(ih.buildItem())) {
                    hasItem = true;
                    break;
                }
            } catch (Exception e) {
                ItemMeta itemMeta = ih.buildItem().getItemMeta();
                ItemMeta target = evt.getPlayer().getInventory().getItem(i).getItemMeta();
                if (itemMeta.getDisplayName().equals(target.getDisplayName()) && itemMeta.getLore().equals(target.getLore()) &&
                        ih.buildItem().getType().equals(evt.getPlayer().getInventory().getItem(i).getType())) {
                    hasItem = true;
                    break;
                }
            }
        }
        if(PlayerStorage.isConsumed(evt.getPlayer())) {
            if (hasItem) {
                if (evt.getPlayer().getInventory().getItem(i).getAmount() > 1) {
                    ItemStack item = ih.buildItem();
                    item.setAmount(evt.getPlayer().getInventory().getItem(i).getAmount() - 1);
                    evt.getPlayer().getInventory().setItem(i, item);
                } else {
                    evt.getPlayer().getInventory().setItem(i, null);
                }
                commandExec.doRestoreModInventory(evt.getPlayer());
                commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn");
                commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn");
                return;
            }
            if (dataManager.getVirtualCount(evt.getPlayer()) > 0) {
                commandExec.doRestoreModInventory(evt.getPlayer());
                commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn");
                commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn");
                dataManager.virtualUsed(evt.getPlayer());
                return;
            }
        }
        commandExec.runCommands(evt.getPlayer(), false, "settings.run-commands-on-respawn-if-drops");
        commandExec.runRandomCommands(evt.getPlayer(), false, "settings.run-random-commands-on-respawn-if-drops");
        PlayerStorage.resetConsumed(evt.getPlayer());
    }
}
