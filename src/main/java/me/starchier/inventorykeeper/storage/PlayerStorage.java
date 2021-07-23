package me.starchier.inventorykeeper.storage;

import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PlayerStorage {
    //Storage the death cause for player
    public static Map<Player, EntityDamageEvent.DamageCause> deathType = new HashMap<>();
    //Storage the killer for a player
    public static Map<Player, String> killerMap = new HashMap<>();
    //Storage if any items are consumed, return null if drops
    public static Map<Player, String> consumeMap = new HashMap<>();

    public static void setDeathType(Player player, EntityDamageEvent.DamageCause cause) {
        deathType.remove(player);
        deathType.put(player, cause);
    }

    public static void clearPlayer(Player player) {
        //Should remove on player respawn.
        deathType.remove(player);
    }

    public static EntityDamageEvent.DamageCause getDeathCause(Player player) {
        for (Map.Entry<Player, EntityDamageEvent.DamageCause> entry : deathType.entrySet()) {
            if (entry.getKey() == player) {
                return entry.getValue();
            }
        }
        //Should not reach here.
        return EntityDamageEvent.DamageCause.CUSTOM;
    }
    public static void setKiller(Player player, String killer) {
        killerMap.remove(player);
        killerMap.put(player, killer);
    }
    public static void removeKiller(Player player) {
        //Should remove on player respawn.
        killerMap.remove(player);
    }
    public static String getKiller(Player player) {
        Set<Map.Entry<Player, String>> p = killerMap.entrySet();
        for (Map.Entry entry : p) {
            if (entry.getKey() == player) {
                return entry.getValue().toString();
            }
        }
        return null;
    }

    public static boolean isKilledByEntity(Player player) {
        return getKiller(player) != null;
    }

    public static String getConsumed(Player player) {
        return consumeMap.get(player);
    }

    public static void setConsumed(Player player, String consumed) {
        consumeMap.remove(player);
        consumeMap.put(player, consumed);
    }

    public static void resetConsumed(Player player) {
        consumeMap.remove(player);
    }
}
