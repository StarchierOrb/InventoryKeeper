package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Random;

public class ExpHandler {
    InventoryKeeper plugin;
    public ExpHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }
    public int loseExp(PlayerDeathEvent evt) {
        PluginHandler ph = new PluginHandler(plugin);
        String amount = ph.getSettings("exp-lose-percentage").replace("%","").replace("L","");
        if(amount.contains("-")) {
            String[] random = amount.split("-");
            Random r = new Random();
            int rand = r.nextInt(Integer.parseInt(random[1])-Integer.parseInt(random[0]))+Integer.parseInt(random[0]);
            int exp = evt.getEntity().getLevel()*rand/100;
            evt.setNewLevel(evt.getEntity().getLevel()-exp);
            return exp;
        } else {
            int exp = evt.getEntity().getLevel()*Integer.parseInt(amount)/100;
            evt.setNewLevel(evt.getEntity().getLevel()-exp);
            return exp;
        }
    }
}
