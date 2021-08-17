package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.storage.PlayerStorage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {
    @EventHandler
    public void onDamageReceived(EntityDamageEvent evt) {
        if(!(evt.getEntity() instanceof Player)) {
            return;
        }
        double finalHealth = ((Player) evt.getEntity()).getHealth() - evt.getFinalDamage();
        if(finalHealth<=0) {
            PlayerStorage.setDeathType(((Player) evt.getEntity()).getPlayer(), evt.getCause());
        }
    }

    @EventHandler
    public void onAttackByEntity(EntityDamageByEntityEvent evt) {
        if(evt.getEntityType() != EntityType.PLAYER) {
            return;
        }
        double finalHealth = ((Player) evt.getEntity()).getHealth() - evt.getFinalDamage();
        if(finalHealth>0) {
            return;
        }
        if(evt.getDamager().getCustomName()!=null) {
            PlayerStorage.setKiller((Player) evt.getEntity(), evt.getDamager().getType() + "|" + evt.getDamager().getCustomName());
            return;
        }
        PlayerStorage.setKiller((Player) evt.getEntity(), evt.getDamager().getType().toString());
    }
}
