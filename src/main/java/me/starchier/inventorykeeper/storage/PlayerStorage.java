package me.starchier.inventorykeeper.storage;

import me.starchier.inventorykeeper.items.ItemBase;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PlayerStorage {
    //Storage the death cause for player
    public static HashMap<Player, EntityDamageEvent.DamageCause> deathType = new HashMap<>();
    //Storage the killer for a player
    public static HashMap<Player, String> killerMap = new HashMap<>();
    //Storage if any items are consumed, return null if drops
    public static HashMap<Player, ItemBase> consumeMap = new HashMap<>();
    //Storage players' food level
    public static HashMap<Player, Integer> foodLevel = new HashMap<>();
    //Storage players' saturation level
    public static HashMap<Player, Integer> saturationLevel = new HashMap<>();
    //Storage players' inventory
    public static HashMap<Player, PlayerInventoryStorage> inventory = new HashMap<>();
    //Storage players' level
    public static HashMap<Player, Integer> playerLevels = new HashMap<>();
    //Storage players' status
    public static HashMap<Player, Boolean> isKeep = new HashMap<>();

    public static void setDeathType(Player player, EntityDamageEvent.DamageCause cause) {
        deathType.remove(player);
        deathType.put(player, cause);
    }

    public static void clearPlayer(Player player) {
        //Should remove on player respawn.
        deathType.remove(player);
    }

    public static int getFoodLevel(Player player) {
        try {
            return foodLevel.get(player);
        } catch (NullPointerException e) {
            return 20;
        }
    }

    public static int getSaturationLevel(Player player) {
        try {
            return saturationLevel.get(player);
        } catch (NullPointerException e) {
            return 5;
        }
    }

    public static void setFoodLevel(Player player, int amount) {
        foodLevel.put(player, amount);
    }

    public static void setSaturationLevel(Player player, int amount) {
        saturationLevel.put(player, amount);
    }

    public static void removeFoodLevel(Player player) {
        try {
            foodLevel.remove(player);
        } catch (NullPointerException ignored) {
        }
    }

    public static void removeSaturationLevel(Player player) {
        try {
            saturationLevel.remove(player);
        } catch (NullPointerException ignored) {
        }
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
        for (Map.Entry<Player, String> entry : killerMap.entrySet()) {
            if (entry.getKey() == player) {
                return entry.getValue();
            }
        }
        return null;
    }

    public static boolean isKilledByEntity(Player player) {
        return getKiller(player) != null;
    }

    public static ItemBase getConsumed(Player player) {
        return consumeMap.get(player);
    }

    public static void setConsumed(Player player, ItemBase consumed) {
        consumeMap.remove(player);
        consumeMap.put(player, consumed);
    }

    public static void resetConsumed(Player player) {
        consumeMap.remove(player);
    }

    public static void saveInventory(Player player, PlayerInventoryStorage items) {
        inventory.put(player, items);
    }

    public static void removeInventory(Player player) {
        inventory.remove(player);
    }

    public static PlayerInventoryStorage getInventory(Player player) {
        return inventory.get(player);
    }

    public static int getLevel(Player player) {
        return playerLevels.get(player);
    }

    public static void saveLevel(Player player, int level) {
        playerLevels.put(player, level);
    }

    public static void removeLevel(Player player) {
        playerLevels.remove(player);
    }
}
