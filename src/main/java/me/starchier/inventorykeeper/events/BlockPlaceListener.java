package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class BlockPlaceListener implements Listener {
    private final ItemHandler itemHandler;
    private final PluginHandler pluginHandler;

    public BlockPlaceListener(ItemHandler itemHandler, PluginHandler pluginHandler) {
        this.itemHandler = itemHandler;
        this.pluginHandler = pluginHandler;
    }

    @EventHandler
    public void onPlacingBlock(BlockPlaceEvent evt) {
        for (ItemBase item : pluginHandler.currentItems) {
            try {
                if (itemHandler.buildItem(item.getName()).isSimilar(evt.getItemInHand())) {
                    evt.setBuild(false);
                }
            } catch (Throwable e) {
                ItemStack realItem = itemHandler.buildItem(item.getName());
                ItemMeta itemMeta = realItem.getItemMeta();
                ItemMeta target = evt.getItemInHand().getItemMeta();
                if (itemMeta.getDisplayName().equals(target.getDisplayName()) && itemMeta.getLore().equals(target.getLore()) &&
                        realItem.getType().equals(evt.getItemInHand().getType())) {
                    evt.setBuild(false);
                }
            }
        }
    }
}
