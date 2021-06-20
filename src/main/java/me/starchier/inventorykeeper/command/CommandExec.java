package me.starchier.inventorykeeper.command;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CommandExec {
    private PluginHandler pluginHandler;
    private InventoryKeeper plugin;
    public CommandExec(PluginHandler pluginHandler, InventoryKeeper plugin) {
        this.pluginHandler = pluginHandler;
        this.plugin = plugin;
    }
    public void doKeepModInventory(Player player) {
        String value = pluginHandler.getSettings("galacticraft-mod-support");
        if(value==null) {
            return;
        }
        if(value.equals("true")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gcinv save " + player.getName());
        }
    }
    public void doRestoreModInventory(Player player) {
        String value = pluginHandler.getSettings("galacticraft-mod-support");
        if(value==null) {
            return;
        }
        if(value.equals("true")) {
            new BukkitRunnable() {
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gcinv restore " +player.getName());
                }
            }.runTaskLater(plugin, 10);
        }
    }
    public void runCommands(Player player, boolean isDeath, String path) {
        List<String> commands = pluginHandler.getList(path);
        List<String> runCmds = new ArrayList<>();
        for(String s : commands) {
            int num;
            if (s.contains("%random%")) {
                num = processRandom(s);
                s = s.split("\\|", 3)[2].replace("%random%", String.valueOf(num));
            }
            if (runOpCommands(player, isDeath, runCmds, s)) continue;
            if (isDeath) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
            } else {
                runCmds.add(s.replace("%player%", player.getName()));
            }
        }
        runDeathCommands(player, isDeath, runCmds);
    }

    private void runDeathCommands(Player player, boolean isDeath, List<String> runCmds) {
        if(!isDeath) {
            new BukkitRunnable() {
                public void run() {
                    for(String s: runCmds) {
                        if(s.contains("[op]")) {
                            boolean isOP = player.isOp();
                            try {
                                player.setOp(true);
                                Bukkit.dispatchCommand(player, s.replace("[op]", ""));
                            } catch (Exception e) {
                            } finally {
                                player.setOp(isOP);
                            }
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s);
                        }
                    }
                }
            }.runTaskLater(plugin, 10);
        }
    }

    private boolean runOpCommands(Player player, boolean isDeath, List<String> runCmds, String s) {
        if (s.contains("[OP]") || s.contains("[op]")) {
            String fixCmd = s.replace("[OP]", "").replace("[op]", "");
            if (isDeath) {
                boolean isOP = player.isOp();
                try {
                    player.setOp(true);
                    Bukkit.dispatchCommand(player, fixCmd.replace("%player%", player.getName()));
                } catch (Exception e) {
                } finally {
                    player.setOp(isOP);
                }
            } else {
                runCmds.add("[op]"+fixCmd.replace("%player%", player.getName()));
            }
            return true;
        }
        return false;
    }

    public void runRandomCommands(Player player, boolean isDeath, String path) {
        List<String> commands = pluginHandler.getList(path);
        List<String> runCmds = new ArrayList<>();
        if(commands.isEmpty()) return;
        Random random = new Random();
        int count = random.nextInt(commands.size());
        //if(count<0) count=0;
        int num;
        String cmd = commands.get(count);
        if(cmd.contains("%random%")) {
            num = processRandom(cmd);
            cmd = cmd.split("\\|", 3)[2].replace("%random%", String.valueOf(num));
        }
        String[] group = cmd.split(";");
        for(String s : group) {
            if (runOpCommands(player, isDeath, runCmds, s)) continue;
            if(!isDeath) {
                runCmds.add(s.replace("%player%", player.getName()));
                continue;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
        }
        runDeathCommands(player, isDeath, runCmds);
    }
    public int processRandom(String s) {
        String[] group = s.split("\\|", 3);
        Random r = new Random();
        return r.nextInt(Integer.parseInt(group[1])-Integer.parseInt(group[0]))+Integer.parseInt(group[0]);
    }
}
