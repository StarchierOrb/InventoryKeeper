package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockPlacing implements Listener {
    InventoryKeeper plugin;
    public BlockPlacing(InventoryKeeper plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlacingBlock(BlockPlaceEvent evt) {
        ItemHandler itemHandler = new ItemHandler(plugin);
        PluginHandler pluginHandler = new PluginHandler(plugin);
        if(!pluginHandler.getCfg().getString("settings.prevent-placing-the-item", "true").equalsIgnoreCase("true")) {
            return;
        }
        try {
            if (itemHandler.buildItem().isSimilar(evt.getItemInHand())) {
                evt.setBuild(false);
            }
        } catch(Exception e) {
            ItemMeta item = itemHandler.buildItem().getItemMeta();
            ItemMeta target = evt.getItemInHand().getItemMeta();
            if (item.getDisplayName().equals(target.getDisplayName()) && item.getLore().equals(target.getLore()) &&
                    itemHandler.buildItem().getType().equals(evt.getItemInHand().getType())) {
                evt.setBuild(false);
            }
        }
    }
}
