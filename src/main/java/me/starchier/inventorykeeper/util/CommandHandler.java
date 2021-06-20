package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class CommandHandler {
    InventoryKeeper plugin;
    public CommandHandler(InventoryKeeper plugin){
        this.plugin = plugin;
    }
    public Player findPlayer(String pl) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getName().equalsIgnoreCase(pl)) {
                return p;
            }
        }
        return null;
    }
    public void giveItem(CommandSender sender, String[] args) {
        PluginHandler ph = new PluginHandler(plugin);
        Player target = findPlayer(args[2]);
        if(target==null) {
            sender.sendMessage(ph.getMsg("player-not-found").replace("%s", args[2]));
            return;
        }
        ItemHandler ih = new ItemHandler(plugin);
        if(!ih.isItem()) {
            sender.sendMessage(ph.getMsg("item-not-exist").replace("%s",ph.getSettings("keep-inventory-item.item-id")));
            return;
        }
        ItemStack item = ih.getSaveItem();
        if(args.length>=4) {
            if(ph.isNumber(args[3])) {
                item.setAmount(Integer.parseInt(args[3]));
            } else {
                sender.sendMessage(ph.getMsg("is-not-number").replace("%s",args[3]));
                item.setAmount(1);
            }
        } else {
            item.setAmount(1);
        }
        target.getWorld().dropItem(target.getLocation(), item);
        sender.sendMessage(ph.getMsg("gave-item").replace("%s", String.valueOf(item.getAmount())).replace("%p",target.getDisplayName()));
        target.sendMessage(ph.getMsg("received-item").replace("%amount%", String.valueOf(item.getAmount())));
    }
}
