package me.starchier.inventorykeeper.manager;

import me.starchier.inventorykeeper.items.ItemBase;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class DataManager {
    private final File dataFile;
    private final PluginHandler pluginHandler;

    public DataManager(File dataFile, PluginHandler pluginHandler) {
        this.dataFile = dataFile;
        this.pluginHandler = pluginHandler;
    }

    private FileConfiguration data = null;

    //private File dataFile= new File(System.getProperty("user.dir") + "\\plugins\\InventoryKeeper\\data.yml");
    public FileConfiguration getData() {
        if (data == null) {
            data = YamlConfiguration.loadConfiguration(dataFile);
        }
        return data;
    }

    public void reloadData() {
        //dataFile = new File(plugin.getDataFolder(), "data.yml");
        try {
            getData().load(dataFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getVirtualCount(Player target, String name) {
        return getData().getInt("playerdata." + target.getUniqueId() + "." + name, -1);
    }

    public void initPlayer(Player player) {
        for (ItemBase item : pluginHandler.currentItems) {
            if (getVirtualCount(player, item.getName()) == -1) {
                getData().set("playerdata." + player.getUniqueId() + "." + item.getName(), 0);
            }
        }
    }

    public void createPlayer(Player player) {
        initPlayer(player);
        saveData();
    }

    public void setConsumed(Player target, String name) {
        getData().set("playerdata." + target.getUniqueId() + "." + name, getVirtualCount(target, name) - 1);
        saveData();
    }

    public void saveData() {
        //File dataFile = new File(plugin.getDataFolder(), "data.yml");
        try {
            getData().save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int setVirtual(Player player, int amount, String name) {
        return processModify(player, name, amount);
    }

    public int takeVirtual(Player player, int amount, String name) {
        int count = getVirtualCount(player, name) - amount;
        return processModify(player, name, count);
    }

    private int processModify(Player player, String name, int count) {
        if (count < 0) {
            getData().set("playerdata." + player.getUniqueId() + "." + name, 0);
            saveData();
            return 0;
        }
        getData().set("playerdata." + player.getUniqueId() + "." + name, count);
        saveData();
        return count;
    }

    public int addVirtual(Player player, int amount, String name) {
        int count = getVirtualCount(player, name) + amount;
        getData().set("playerdata." + player.getUniqueId() + "." + name, count);
        saveData();
        return count;
    }

    /*
    To prevent issues if server try to hot load the plugin.
     */
    public void startupProcess() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            initPlayer(p);
        }
        saveData();
    }
}
