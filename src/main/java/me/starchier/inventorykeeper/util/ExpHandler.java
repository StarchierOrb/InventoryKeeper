package me.starchier.inventorykeeper.util;

import org.bukkit.event.entity.PlayerDeathEvent;

import java.util.Random;

public class ExpHandler {
    private final PluginHandler pluginHandler;

    public ExpHandler(PluginHandler pluginHandler) {
        this.pluginHandler = pluginHandler;
    }

    public int loseExp(PlayerDeathEvent evt, String name) {
        String amount = pluginHandler.getItemBase(name).getExpLostPercentage().replace("%", "").replace("L", "");
        if (amount.contains("-")) {
            String[] random = amount.split("-");
            Random r = new Random();
            int rand = r.nextInt(Integer.parseInt(random[1]) - Integer.parseInt(random[0])) + Integer.parseInt(random[0]);
            int exp = evt.getEntity().getLevel() * rand / 100;
            evt.setNewLevel(evt.getEntity().getLevel() - exp);
            return exp;
        } else {
            int exp = evt.getEntity().getLevel() * Integer.parseInt(amount) / 100;
            evt.setNewLevel(evt.getEntity().getLevel() - exp);
            return exp;
        }
    }
}
