package me.starchier.inventorykeeper.command;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.CommandHandler;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandTab implements TabExecutor {
    private InventoryKeeper plugin;
    private DataManager dataManager;
    public CommandTab(InventoryKeeper plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }
    private final String[] subCommands = {"give", "get", "reload", "check", "take", "set"};
    private final String[] playerCommands = {"check"};
    private final String essPerm = "inventorykeeper.check";
    private final String advPerm = "inventorykeeper.check.others";
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(cmd.getName().equalsIgnoreCase("invkeep")) {
            PluginHandler ph = new PluginHandler(plugin);
            if(sender instanceof Player && (!playerPerm(sender) && !sender.hasPermission("inventorykeeper.admin"))) {
                sender.sendMessage(ph.getMessage("no-permission"));
                return true;
            }
            if(args.length<1&&playerPerm(sender)) {
                for(String s : ph.getList("messages.help-msg")) {
                    sender.sendMessage(s);
                }
                return true;
            }
            if(args.length>0&&!sender.hasPermission("inventorykeeper.admin")&&!args[0].equalsIgnoreCase("check")&&sender instanceof Player) {
                sender.sendMessage(ph.getMessage("no-permission"));
                return true;
            }
            CommandHandler commandHandler = new CommandHandler(plugin);
            if(!sender.hasPermission("inventorykeeper.admin")&&sender instanceof Player&&args[0].equalsIgnoreCase("check")) {
                if(args.length<2) {
                    sender.sendMessage(ph.getMessage("virtual-item-count").replace("%amount%", String.valueOf(dataManager.getVirtualCount((Player) sender))));
                    return true;
                }
                if(sender.hasPermission(advPerm)) {
                    Player target = commandHandler.findPlayer(args[1]);
                    if(target==null) {
                        sender.sendMessage(ph.getMessage("player-not-found").replace("%s", args[1]));
                        return true;
                    }
                    sender.sendMessage(ph.getMessage("virtual-item-count-others").replace("%amount%", String.valueOf(dataManager.getVirtualCount(target)))
                            .replace("%p", target.getName()));
                }
                return true;
            }
            switch(args[0]) {
                case "reload": {
                    plugin.getLogger().info("Reloading config...");
                    plugin.reloadConfig();
                    ItemHandler ih = new ItemHandler(plugin);
                    if(!ih.isItem()) {
                        plugin.getLogger().severe("Item " + ph.getConfigValue("keep-inventory-item.item-id") + " is not valid!");
                        plugin.getLogger().severe("The item will be replaced to STICK!");
                        ph.getCfg().set("settings.keep-inventory-item.item-id", "STICK");
                    }
                    ih.validEnchant();
                    ih.cacheSkull();
                    plugin.getLogger().info("Reloading player data...");
                    dataManager.reloadData();
                    sender.sendMessage(ph.getMessage("reloaded-config"));
                    return true;
                }
                case "get": {
                    if(!(sender instanceof Player)) {
                        sender.sendMessage(ph.getMessage("player-only"));
                        return true;
                    }
                    if(args.length<2||args.length>3) {
                        sender.sendMessage(ph.getMessage("get-usage"));
                        return true;
                    }
                    switch (args[1]) {
                        case "v": {
                            int count=0;
                            if(args.length<3) count=1;
                            else {
                                if (ph.isNumber(args[2])) {
                                    count = Integer.parseInt(args[2]);
                                } else {
                                    count = 1;
                                    sender.sendMessage(ph.getMessage("is-not-number").replace("%s", args[2]));
                                }
                            }
                            int total = dataManager.addVirtual((Player) sender, count);
                            sender.sendMessage(ph.getMessage("received-virtual-item").replace("%amount%", String.valueOf(count)
                            ).replace("%total%", String.valueOf(total)));
                            return true;
                        }
                        default: {
                            ItemHandler ih = new ItemHandler(plugin);
                            if(!ih.isItem()) {
                                sender.sendMessage(ph.getMessage("item-not-exist").replace("%s", ph.getConfigValue("keep-inventory-item.item-id")));
                                return true;
                            }
                            ItemStack item = ih.buildItem();
                            if(args.length>=3) {
                                if(ph.isNumber(args[2])) {
                                    item.setAmount(Integer.parseInt(args[2]));
                                } else {
                                    sender.sendMessage(ph.getMessage("is-not-number").replace("%s", args[2]));
                                    item.setAmount(1);
                                }
                            } else {
                                item.setAmount(1);
                            }
                            ((Player) sender).getPlayer().getWorld().dropItem(((Player) sender).getLocation(), item);
                            sender.sendMessage(ph.getMessage("received-item").replace("%amount%", String.valueOf(item.getAmount())));
                            return true;
                        }
                    }
                }
                case "give": {
                    if(args.length<3||args.length>4) {
                        sender.sendMessage(ph.getMessage("give-usage"));
                        return true;
                    }
                    switch(args[1]) {
                        case "v": {
                            int count=0;
                            if(args.length<4) count=1;
                            else {
                                if(ph.isNumber(args[3])) {
                                    count=Integer.parseInt(args[3]);
                                } else {
                                    count = 1;
                                    sender.sendMessage(ph.getMessage("is-not-number").replace("%s", args[3]));
                                }
                            }
                            Player target = commandHandler.findPlayer(args[2]);
                            if(target!=null) {
                                int total = dataManager.addVirtual(target, count);
                                sender.sendMessage(ph.getMessage("give-virtual-item").replace("%total%", String.valueOf(total))
                                        .replace("%p", args[2]).replace("%amount%", String.valueOf(count)));
                                target.sendMessage(ph.getMessage("received-virtual-item").replace("%amount%", String.valueOf(count)
                                ).replace("%total%", String.valueOf(total)));
                                return true;
                            }
                            sender.sendMessage(ph.getMessage("player-not-found").replace("%s", args[2]));
                            return true;
                        }
                        default: {
                            CommandHandler ch = new CommandHandler(plugin);
                            ch.giveItem(sender, args);
                            return true;
                        }
                    }
                }
                case "check": {
                    if(args.length<2) {
                        if(!(sender instanceof Player)) {
                            sender.sendMessage(ph.getMessage("player-only"));
                            return true;
                        }
                        sender.sendMessage(ph.getMessage("virtual-item-count").replace("%amount%", String.valueOf(dataManager.getVirtualCount((Player) sender))));
                        return true;
                    }
                    Player target = commandHandler.findPlayer(args[1]);
                    if(target==null) {
                        sender.sendMessage(ph.getMessage("player-not-found").replace("%s", args[1]));
                        return true;
                    }
                    sender.sendMessage(ph.getMessage("virtual-item-count-others").replace("%amount%", String.valueOf(dataManager.getVirtualCount(target)))
                            .replace("%p", target.getName()));
                    return true;
                }
                case "set": {
                    if(args.length!=3) {
                        sender.sendMessage(ph.getMessage("set-usage"));
                        return true;
                    }
                    if(!ph.isNumber(args[2])) {
                        sender.sendMessage(ph.getMessage("is-not-number").replace("%s", args[2]));
                        return true;
                    }
                    Player target = commandHandler.findPlayer(args[1]);
                    if(target!=null) {
                        int a = dataManager.setVirtual(target, Integer.parseInt(args[2]));
                        sender.sendMessage(ph.getMessage("set-virtual-item").replace("%total%", String.valueOf(a))
                                .replace("%p", args[1]));
                        target.sendMessage(ph.getMessage("modified-amount").replace("%amount%", String.valueOf(a)));
                        return true;
                    }
                    sender.sendMessage(ph.getMessage("player-not-found").replace("%s", args[1]));
                    return true;
                }
                case "take": {
                    if(args.length!=3) {
                        sender.sendMessage(ph.getMessage("take-usage"));
                        return true;
                    }
                    if(!ph.isNumber(args[2])) {
                        sender.sendMessage(ph.getMessage("is-not-number").replace("%s", args[2]));
                        return true;
                    }
                    Player target = commandHandler.findPlayer(args[1]);
                    if(target!=null) {
                        int a = dataManager.takeVirtual(target, Integer.parseInt(args[2]));
                        sender.sendMessage(ph.getMessage("take-virtual-item").replace("%amount%", args[2])
                                .replace("%total%", String.valueOf(a)).replace("%p", args[1]));
                        target.sendMessage(ph.getMessage("modified-amount").replace("%amount%", String.valueOf(a)));
                        return true;
                    }
                    sender.sendMessage(ph.getMessage("player-not-found").replace("%s", args[1]));
                    return true;
                }
                default: {
                    for(String s : ph.getList("messages.help-msg")) {
                        sender.sendMessage(s);
                    }
                    return true;
                }
            }
        }
        return true;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String alias, String[] args) {
        if(sender instanceof Player && !(sender.hasPermission("inventorykeeper.admin")||playerPerm(sender))) {
            return new ArrayList<>();
        }
        if(args.length<2&&!sender.hasPermission("inventorykeeper.admin")) {
            return Arrays.asList(playerCommands);
        } else if(args.length==2 && args[0].equalsIgnoreCase("check")) {
            if(sender.hasPermission("inventorykeeper.admin")||sender.hasPermission(advPerm)) {
                return getPlayers();
            }
            return new ArrayList<>();
        }
        if(sender.hasPermission("inventorykeeper.admin")||!(sender instanceof Player)) {
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("give")) {
                    return typeList();
                }
                if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("take")) {
                    return getPlayers();
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")
                        || args[0].equalsIgnoreCase("take")) {
                    return amountList();
                }
                if (args[0].equalsIgnoreCase("give")) {
                    return getPlayers();
                }
            }
            if (args.length == 4) {
                if (args[0].equalsIgnoreCase("give")) {
                    return amountList();
                }
            }
            if (args.length < 2) {
                return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }
    public List<String> typeList() {
        List<String> c1 = new ArrayList<>();
        c1.add("v");
        c1.add("p");
        return c1;
    }
    public List<String> amountList() {
        List<String> c1 = new ArrayList<>();
        c1.add("[amount]");
        c1.add("1");
        c1.add("2");
        return c1;
    }
    public boolean playerPerm(CommandSender sender) {
        return sender.hasPermission(essPerm)||sender.hasPermission(advPerm);
    }
    public List<String> getPlayers() {
        List<String> pl = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            pl.add(p.getName());
        }
        return pl;
    }
}
