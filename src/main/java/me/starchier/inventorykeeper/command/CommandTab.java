package me.starchier.inventorykeeper.command;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
    private final InventoryKeeper plugin;
    private final DataManager dataManager;
    private final PluginHandler pluginHandler;
    private final ItemHandler itemHandler;
    private final CommandUtil commandUtil;

    public CommandTab(InventoryKeeper plugin, DataManager dataManager, PluginHandler pluginHandler, ItemHandler itemHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.pluginHandler = pluginHandler;
        this.itemHandler = itemHandler;
        commandUtil = new CommandUtil(plugin, pluginHandler, itemHandler);
    }

    private final String[] subCommands = {"give", "get", "reload", "check", "take", "set"};
    private final String[] playerCommands = {"check"};
    private final String essPerm = "inventorykeeper.check";
    private final String advPerm = "inventorykeeper.check.others";

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("invkeep")) {
            if (sender instanceof Player && (!playerPerm(sender) && !sender.hasPermission("inventorykeeper.admin"))) {
                sender.sendMessage(pluginHandler.getMessage("no-permission"));
                return true;
            }
            if (args.length < 1 && playerPerm(sender)) {
                for (String s : pluginHandler.getList("messages.help-msg", true)) {
                    sender.sendMessage(s);
                }
                return true;
            }
            if (args.length > 0 && !sender.hasPermission("inventorykeeper.admin") && !args[0].equalsIgnoreCase("check") && sender instanceof Player) {
                sender.sendMessage(pluginHandler.getMessage("no-permission"));
                return true;
            }
            if (!sender.hasPermission("inventorykeeper.admin") && sender instanceof Player && args[0].equalsIgnoreCase("check")) {
                if (args.length < 2) {
                    sender.sendMessage(pluginHandler.getMessage("virtual-item-count"));
                    for (ItemBase item : pluginHandler.currentItems) {
                        String name = item.getName();
                        sender.sendMessage(String.format(pluginHandler.getMessage("virtual-item-format"), name, dataManager.getVirtualCount((Player) sender, name)));
                    }
                    return true;
                }
                if (sender.hasPermission(advPerm)) {
                    Player target = commandUtil.findPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(pluginHandler.getMessage("player-not-found").replace("%s", args[1]));
                        return true;
                    }
                    sender.sendMessage(pluginHandler.getMessage("virtual-item-count-others").replace("%s", args[1]));
                    for (ItemBase item : pluginHandler.currentItems) {
                        String name = item.getName();
                        sender.sendMessage(String.format(pluginHandler.getMessage("virtual-item-format"), name, dataManager.getVirtualCount(target, name)));
                    }
                }
                return true;
            }
            switch (args[0]) {
                case "reload": {
                    plugin.getLogger().info(MessagesUtil.getMessage("reloading-config"));
                    plugin.reloadConfig();
                    pluginHandler.initConfigCache();
                    pluginHandler.loadItems(itemHandler);
                    plugin.getLogger().info(MessagesUtil.getMessage("reloading-player-data"));
                    dataManager.reloadData();
                    Debugger.enabledDebug = pluginHandler.getBooleanConfigValue("debug", true);
                    sender.sendMessage(ChatColor.GOLD + "(!)" + ChatColor.GREEN + String.format(MessagesUtil.getMessage("load-item-completed"), pluginHandler.currentItems.size()));
                    sender.sendMessage(MessagesUtil.getMessage("reloaded-config"));
                    return true;
                }
                case "get": {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(pluginHandler.getMessage("player-only"));
                        return true;
                    }
                    if (args.length < 3 || args.length > 4) {
                        sender.sendMessage(pluginHandler.getMessage("get-usage"));
                        return true;
                    }
                    switch (args[1]) {
                        case "v": {
                            ItemBase item = pluginHandler.getItemBase(args[2]);
                            if (item == null) {
                                sender.sendMessage(pluginHandler.getMessage("invalid-item"));
                                return true;
                            }
                            int count = 0;
                            if (args.length < 4) {
                                count = 1;
                            } else {
                                if (!pluginHandler.isNumber(args[3])) {
                                    sender.sendMessage(String.format(pluginHandler.getMessage("is-not-number"), args[3]));
                                    return true;
                                }
                                count = Integer.parseInt(args[3]);
                            }
                            int total = dataManager.addVirtual((Player) sender, count, item.getName());
                            sender.sendMessage(String.format(pluginHandler.getMessage("received-virtual-item"), item.getName(), count, total));
                            return true;
                        }
                        default: {
                            ItemBase itemBase = pluginHandler.getItemBase(args[2]);
                            if (itemBase == null) {
                                sender.sendMessage(pluginHandler.getMessage("invalid-item"));
                                return true;
                            }
                            ItemStack item = itemBase.getItem();
                            if (args.length >= 4) {
                                if (!pluginHandler.isNumber(args[3])) {
                                    sender.sendMessage(String.format(pluginHandler.getMessage("is-not-number"), args[3]));
                                    return true;
                                }
                                item.setAmount(Integer.parseInt(args[3]));
                            } else {
                                item.setAmount(1);
                            }
                            ((Player) sender).getPlayer().getWorld().dropItem(((Player) sender).getLocation(), item);
                            sender.sendMessage(String.format(pluginHandler.getMessage("received-item"), item.getAmount(), itemBase.getName()));
                            return true;
                        }
                    }
                }
                case "give": {
                    if (args.length < 4 || args.length > 5) {
                        sender.sendMessage(pluginHandler.getMessage("give-usage"));
                        return true;
                    }
                    switch (args[1]) {
                        case "v": {
                            int count = 0;
                            if (args.length < 5) {
                                count = 1;
                            } else {
                                if (!pluginHandler.isNumber(args[4])) {
                                    sender.sendMessage(String.format(pluginHandler.getMessage("is-not-number"), args[4]));
                                    return true;
                                }
                                count = Integer.parseInt(args[4]);
                            }
                            Player target = commandUtil.findPlayer(args[3]);
                            if (target != null) {
                                if (!pluginHandler.itemNames.contains(args[2])) {
                                    sender.sendMessage(pluginHandler.getMessage("invalid-item"));
                                    return true;
                                }
                                int total = dataManager.addVirtual(target, count, args[2]);
                                sender.sendMessage(String.format(pluginHandler.getMessage("give-virtual-item"), args[2], args[3], count, total));
                                target.sendMessage(String.format(pluginHandler.getMessage("received-virtual-item"), args[2], count, total));
                                return true;
                            }
                            sender.sendMessage(String.format(pluginHandler.getMessage("player-not-found"), args[2]));
                            return true;
                        }
                        default: {
                            commandUtil.giveItem(sender, args);
                            return true;
                        }
                    }
                }
                case "check": {
                    if (args.length < 2) {
                        if (!(sender instanceof Player)) {
                            sender.sendMessage(pluginHandler.getMessage("player-only"));
                            return true;
                        }
                        sender.sendMessage(pluginHandler.getMessage("virtual-item-count"));
                        for (ItemBase item : pluginHandler.currentItems) {
                            String name = item.getName();
                            sender.sendMessage(String.format(pluginHandler.getMessage("config.virtual-item-format"), name, dataManager.getVirtualCount((Player) sender, name)));
                        }
                        return true;
                    }
                    Player target = commandUtil.findPlayer(args[1]);
                    if (target == null) {
                        sender.sendMessage(String.format(pluginHandler.getMessage("player-not-found"), args[1]));
                        return true;
                    }
                    sender.sendMessage(pluginHandler.getMessage("virtual-item-count-others").replace("%p", args[1]));
                    for (String s : pluginHandler.itemNames) {
                        sender.sendMessage(String.format(pluginHandler.getMessage("config.virtual-item-format"), s, dataManager.getVirtualCount(target, s)));
                    }
                    return true;
                }
                case "set": {
                    if (args.length != 4) {
                        sender.sendMessage(pluginHandler.getMessage("set-usage"));
                        return true;
                    }
                    if (!pluginHandler.isNumber(args[3])) {
                        sender.sendMessage(String.format(pluginHandler.getMessage("is-not-number"), args[3]));
                        return true;
                    }
                    Player target = commandUtil.findPlayer(args[1]);
                    if (target != null) {
                        if (!pluginHandler.itemNames.contains(args[2])) {
                            sender.sendMessage(pluginHandler.getMessage("invalid-item"));
                            return true;
                        }
                        int amount = dataManager.setVirtual(target, Integer.parseInt(args[3]), args[2]);
                        sender.sendMessage(String.format(pluginHandler.getMessage("set-virtual-item"), args[2], args[1], amount));
                        target.sendMessage(String.format(pluginHandler.getMessage("modified-amount"), args[2], amount));
                        return true;
                    }
                    sender.sendMessage(String.format(pluginHandler.getMessage("player-not-found"), args[1]));
                    return true;
                }
                case "take": {
                    if (args.length != 4) {
                        sender.sendMessage(pluginHandler.getMessage("take-usage"));
                        return true;
                    }
                    if (!pluginHandler.isNumber(args[3])) {
                        sender.sendMessage(String.format(pluginHandler.getMessage("is-not-number"), args[3]));
                        return true;
                    }
                    Player target = commandUtil.findPlayer(args[1]);
                    if (target != null) {
                        if (pluginHandler.itemNames.contains(args[2])) {
                            sender.sendMessage(pluginHandler.getMessage("invalid-item"));
                            return true;
                        }
                        int amount = dataManager.takeVirtual(target, Integer.parseInt(args[3]), args[2]);
                        sender.sendMessage(String.format(pluginHandler.getMessage("take-virtual-item"), args[2], args[1], Integer.parseInt(args[3]), amount));
                        target.sendMessage(String.format(pluginHandler.getMessage("modified-amount"), args[2], amount));
                        return true;
                    }
                    sender.sendMessage(String.format(pluginHandler.getMessage("player-not-found"), args[1]));
                    return true;
                }
                default: {
                    for (String s : pluginHandler.getList("messages.help-msg", true)) {
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
        if (sender instanceof Player && !(sender.hasPermission("inventorykeeper.admin") || playerPerm(sender))) {
            return new ArrayList<>();
        }
        if (args.length < 2 && !sender.hasPermission("inventorykeeper.admin")) {
            return Arrays.asList(playerCommands);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("check")) {
            if (sender.hasPermission("inventorykeeper.admin") || sender.hasPermission(advPerm)) {
                return getPlayers();
            }
            return new ArrayList<>();
        }
        if (sender.hasPermission("inventorykeeper.admin") || !(sender instanceof Player)) {
            if (args.length == 2) {
                if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("give")) {
                    return TYPE_LIST;
                }
                if (args[0].equalsIgnoreCase("set") || args[0].equalsIgnoreCase("take")) {
                    return getPlayers();
                }
            }
            if (args.length == 3) {
                if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")
                        || args[0].equalsIgnoreCase("take") || args[0].equalsIgnoreCase("give")) {
                    return pluginHandler.getItemNames();
                }
            }
            if (args.length == 4) {
                if (args[0].equalsIgnoreCase("get") || args[0].equalsIgnoreCase("set")
                        || args[0].equalsIgnoreCase("take")) {
                    return AMOUNT_LIST;
                }
                if (args[0].equalsIgnoreCase("give")) {
                    return getPlayers();
                }
            }
            if (args.length == 5) {
                if (args[0].equalsIgnoreCase("give")) {
                    return AMOUNT_LIST;
                }
            }
            if (args.length < 2) {
                return Arrays.stream(subCommands).filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
            }
        }
        return new ArrayList<>();
    }

    public final List<String> TYPE_LIST = new ArrayList<>(Arrays.asList("v", "p"));
    public final List<String> AMOUNT_LIST = new ArrayList<>(Arrays.asList("[amount]", "1", "2"));

    public boolean playerPerm(CommandSender sender) {
        return sender.hasPermission(essPerm) || sender.hasPermission(advPerm);
    }

    public List<String> getPlayers() {
        List<String> pl = new ArrayList<>();
        for (Player p : Bukkit.getOnlinePlayers()) {
            pl.add(p.getName());
        }
        return pl;
    }
}
