package me.starchier.inventorykeeper.command;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class CommandExec {
    private final PluginHandler pluginHandler;
    private final InventoryKeeper plugin;
    private final Random random = new Random();

    public CommandExec(PluginHandler pluginHandler, InventoryKeeper plugin) {
        this.pluginHandler = pluginHandler;
        this.plugin = plugin;
    }

    public void doKeepModInventory(Player player) {
        String value = pluginHandler.getConfigValue("galacticraft-mod-support", true);
        if (value == null) {
            return;
        }
        if (value.equals("true")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gcinv save " + player.getName());
        }
    }
    public void doRestoreModInventory(Player player) {
        String value = pluginHandler.getConfigValue("galacticraft-mod-support", true);
        if(value==null) {
            return;
        }
        if (value.equals("true")) {
            new BukkitRunnable() {
                public void run() {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gcinv restore " + player.getName());
                }
            }.runTaskLater(plugin, 10);
        }
    }

    public void runCommands(Player player, boolean isDeathCommand, String path, boolean useGlobal) {
        List<String> commands = pluginHandler.getList(path, useGlobal);
        List<String> runCmds = new ArrayList<>();
        for (String s : commands) {
            int num;
            if (s.contains("%random%")) {
                num = processRandom(s);
                s = s.split("\\|", 3)[2].replace("%random%", String.valueOf(num));
            }
            if (runOpCommand(player, isDeathCommand, s)) {
                continue;
            }
            if (isDeathCommand) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
            } else {
                runCmds.add(s.replace("%player%", player.getName()));
            }
        }
        runCommandsOnRespawn(player, isDeathCommand, runCmds);
    }

    private void runCommandsOnRespawn(Player player, boolean runOnDeath, List<String> runCmds) {
        if (!runOnDeath) {
            new BukkitRunnable() {
                public void run() {
                    for (String s : runCmds) {
                        if (s.contains("[op]")) {
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

    private boolean runOpCommand(Player player, boolean isDeath, String s) {
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
                runCommandsOnRespawn(player, false, Collections.singletonList("[op]" + fixCmd.replace("%player%", player.getName())));
            }
            return true;
        }
        return false;
    }

    public void runRandomCommands(Player player, boolean isDeath, String path, boolean useGlobal) {
        List<String> commands = pluginHandler.getList(path, useGlobal);
        List<String> runCmds = new ArrayList<>();
        if (commands.isEmpty()) {
            return;
        }
        int count = random.nextInt(commands.size());
        //if(count<0) count=0;
        int num;
        String cmd = commands.get(count);
        if (cmd.contains("%random%")) {
            num = processRandom(cmd);
            cmd = cmd.split("\\|", 3)[2].replace("%random%", String.valueOf(num));
        }
        String[] group = cmd.split(";");
        for (String s : group) {
            if (runOpCommand(player, isDeath, s)) {
                continue;
            }
            if (!isDeath) {
                runCmds.add(s.replace("%player%", player.getName()));
                continue;
            }
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), s.replace("%player%", player.getName()));
        }
        runCommandsOnRespawn(player, isDeath, runCmds);
    }
    public int processRandom(String s) {
        String[] group = s.split("\\|", 3);
        Random r = new Random();
        return r.nextInt(Integer.parseInt(group[1])-Integer.parseInt(group[0]))+Integer.parseInt(group[0]);
    }
}
