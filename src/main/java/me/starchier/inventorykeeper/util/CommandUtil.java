package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.items.ItemBase;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandUtil {
    private final InventoryKeeper plugin;
    private final PluginHandler pluginHandler;
    private final ItemHandler itemHandler;

    public CommandUtil(InventoryKeeper plugin, PluginHandler pluginHandler, ItemHandler itemHandler) {
        this.plugin = plugin;
        this.pluginHandler = pluginHandler;
        this.itemHandler = itemHandler;
    }

    public Player findPlayer(String pl) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(pl)) {
                return p;
            }
        }
        return null;
    }
    public void giveItem(CommandSender sender, String[] args) {
        Player target = findPlayer(args[3]);
        if (target == null) {
            sender.sendMessage(pluginHandler.getMessage("player-not-found").replace("%s", args[3]));
            return;
        }
        if (!itemHandler.isItem(args[2])) {
            sender.sendMessage(String.format(pluginHandler.getMessage("item-not-exist"), args[2]));
            return;
        }
        ItemBase base = pluginHandler.getItemBase(args[2]);
        ItemStack item = base.getItem();
        if (args.length >= 5) {
            if (pluginHandler.isNumber(args[4])) {
                item.setAmount(Integer.parseInt(args[4]));
            } else {
                sender.sendMessage(pluginHandler.getMessage("is-not-number").replace("%s", args[4]));
                item.setAmount(1);
            }
        } else {
            item.setAmount(1);
        }
        target.getWorld().dropItem(target.getLocation(), item);
        sender.sendMessage(pluginHandler.getMessage("gave-item").replace("%amount%", String.valueOf(item.getAmount())).replace("%player%", target.getDisplayName())
                .replace("%item%", base.getDisplayName()));
        target.sendMessage(pluginHandler.getMessage("received-item")
                .replace("%amount%", String.valueOf(item.getAmount()))
                .replace("%item%", base.getDisplayName()));
    }
}
