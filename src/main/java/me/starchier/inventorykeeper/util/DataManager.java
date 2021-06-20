package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class DataManager {
    private InventoryKeeper plugin;
    private File dataFile;
    public DataManager(InventoryKeeper plugin, File dataFile) {
        this.plugin = plugin;
        this.dataFile = dataFile;
    }
    private FileConfiguration data = null;
    //private File dataFile= new File(System.getProperty("user.dir") + "\\plugins\\InventoryKeeper\\data.yml");
    public FileConfiguration getData() {
        if(data==null) {
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
    public int getVirtualCount(Player target) {
        return getData().getInt("playerdata." + target.getUniqueId().toString(), -1);
    }
    public void initPlayer(Player player) {
        getData().set("playerdata." + player.getUniqueId().toString(), 0);
    }
    public void createPlayer(Player player) {
        if(getVirtualCount(player)==-1) {
            initPlayer(player);
            saveData();
        }
    }
    public void virtualUsed(Player target) {
        getData().set("playerdata." + target.getUniqueId().toString(), getVirtualCount(target)-1);
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
    public int setVirtual(Player player, int amount) {
        if(amount<0) {
            getData().set("playerdata."+player.getUniqueId().toString(), 0);
            saveData();
            return 0;
        }
        getData().set("playerdata."+player.getUniqueId().toString(), amount);
        saveData();
        return amount;
    }
    public int takeVirtual(Player player, int amount) {
        int count=getVirtualCount(player)-amount;
        if(count<0) {
            getData().set("playerdata."+player.getUniqueId().toString(), 0);
            saveData();
            return 0;
        }
        getData().set("playerdata." + player.getUniqueId().toString(), count);
        saveData();
        return count;
    }
    public int addVirtual(Player player, int amount) {
        int count = getVirtualCount(player)+amount;
        getData().set("playerdata." + player.getUniqueId().toString(), count);
        saveData();
        return count;
    }
    public void startupProcess() {
        for(Player p : Bukkit.getOnlinePlayers()) {
            if(getVirtualCount(p)==-1) {
                initPlayer(p);
            }
        }
        saveData();
    }
}
