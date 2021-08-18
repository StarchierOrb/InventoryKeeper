package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.api.events.PlayerConsumeItemEvent;
import me.starchier.inventorykeeper.api.events.PlayerDropInventoryEvent;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.storage.PlayerInventoryStorage;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DeathHandler implements Listener {
    private final InventoryKeeper plugin;
    private final DataManager dataManager;
    private final CommandExec commandExec;
    private final PluginHandler pluginHandler;
    private final ExpHandler expHandler;


    public DeathHandler(InventoryKeeper plugin, DataManager dataManager, CommandExec commandExec, PluginHandler pluginHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
        expHandler = new ExpHandler(pluginHandler);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {

        //DEBUG OPTION
        if (pluginHandler.getBooleanConfigValue("show-death-cause-on-death", true)) {
            plugin.getLogger().info(ChatColor.GOLD + String.format(MessagesUtil.getMessage("debug.death-cause"), evt.getEntity().getName(), PlayerStorage.getDeathCause(evt.getEntity())));
            if (PlayerStorage.isKilledByEntity(evt.getEntity())) {
                plugin.getLogger().info(ChatColor.GOLD + String.format(MessagesUtil.getMessage("debug.death-cause-entity"), PlayerStorage.getKiller(evt.getEntity())));
            }
        }
        boolean keepInv;
        if (PluginHandler.IS_LEGACY) {
            keepInv = evt.getEntity().getWorld().getGameRuleValue("keepInventory").equals("true");
        } else {
            keepInv = evt.getEntity().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        }
        if (keepInv) {
            return;
        }

        PlayerStorage.setFoodLevel(evt.getEntity(), evt.getEntity().getFoodLevel());
        PlayerStorage.setSaturationLevel(evt.getEntity(), (int) evt.getEntity().getSaturation());
        PlayerStorage.isKeep.put(evt.getEntity(), false);

        //Shared list
        List<String> passedItems = new ArrayList<>();
        String playerWorld = evt.getEntity().getWorld().getName();
        int consumeType = ConsumeType.CONSUME_NONE;
        ItemBase[] consumeItems = new ItemBase[3];

        //Permission check
        permissionCheck:
        for (String itemName : pluginHandler.itemNames) {
            if (evt.getEntity().hasPermission("inventorykeeper.unlimited." + itemName)) {
                for (String s : pluginHandler.getDisabledWorlds(itemName)) {
                    if (playerWorld.equalsIgnoreCase(s)) {
                        continue permissionCheck;
                    }
                }
                if (processCondition(itemName, evt.getEntity())) {
                    passedItems.add(itemName);
                }
            }
        }
        if (!passedItems.isEmpty()) {
            for (ItemBase itemBase : pluginHandler.currentItems) {
                if (passedItems.contains(itemBase.getName())) {
                    consumeItems[ConsumeType.CONSUME_PERMISSION] = itemBase;
                    consumeType = ConsumeType.CONSUME_PERMISSION;
                    break;
                }
            }
        }

        //Physical items check
        int physicalSlot = -1;
        HashMap<String, Integer> passedPhysicalItems = new HashMap<>();
        for (int i = 0; i < evt.getEntity().getInventory().getSize(); i++) {
            if (evt.getEntity().getInventory().getItem(i) == null) {
                continue;
            }
            for (ItemBase itemBase : pluginHandler.currentItems) {
                try {
                    if (evt.getEntity().getInventory().getItem(i).isSimilar(itemBase.getItem())) {
                        processPhysicalItems(evt, playerWorld, passedPhysicalItems, i, itemBase);
                        break;
                    }
                } catch (Exception e) {
                    ItemMeta item = itemBase.getItem().getItemMeta();
                    ItemMeta target = evt.getEntity().getInventory().getItem(i).getItemMeta();
                    if (item.getDisplayName().equals(target.getDisplayName()) && item.getLore().equals(target.getLore()) &&
                            itemBase.getItem().getType().equals(evt.getEntity().getInventory().getItem(i).getType())) {
                        processPhysicalItems(evt, playerWorld, passedPhysicalItems, i, itemBase);
                        break;
                    }
                }
            }
        }
        if (!passedPhysicalItems.isEmpty()) {
            for (ItemBase itemBase : pluginHandler.currentItems) {
                if (passedPhysicalItems.containsKey(itemBase.getName())) {
                    physicalSlot = passedPhysicalItems.get(itemBase.getName());
                    consumeItems[ConsumeType.CONSUME_PHYSICAL] = itemBase;
                    consumeType = ConsumeType.CONSUME_PHYSICAL;
                    break;
                }
            }
        }

        //Virtual items check
        passedItems = new ArrayList<>();
        virtualCheck:
        for (String key : pluginHandler.itemNames) {
            if (dataManager.getVirtualCount(evt.getEntity(), key) > 0) {
                for (String s : pluginHandler.getDisabledWorlds(key)) {
                    if (playerWorld.equalsIgnoreCase(s)) {
                        continue virtualCheck;
                    }
                }
                if (!processCondition(key, evt.getEntity())) {
                    continue;
                }
                passedItems.add(key);
            }
        }
        if (!passedItems.isEmpty()) {
            for (ItemBase itemBase : pluginHandler.currentItems) {
                if (passedItems.contains(itemBase.getName())) {
                    consumeItems[ConsumeType.CONSUME_VIRTUAL] = itemBase;
                    consumeType = ConsumeType.CONSUME_VIRTUAL;
                    break;
                }
            }
        }
        if (consumeType != ConsumeType.CONSUME_NONE) {
            ItemBase highestItem = null;
            for (int i = 0; i < consumeItems.length; i++) {
                if (consumeItems[i] == null) {
                    continue;
                }
                if (highestItem == null) {
                    highestItem = consumeItems[i];
                    consumeType = i;
                } else if (consumeItems[i].getPriority() > highestItem.getPriority()) {
                    highestItem = consumeItems[i];
                    consumeType = i;
                }
            }
        } else {
            PlayerStorage.removeKiller(evt.getEntity());
            PlayerStorage.clearPlayer(evt.getEntity());
            PlayerStorage.setConsumed(evt.getEntity(), PluginHandler.EMPTY_ITEM);
            if (evt.getKeepInventory()) {
                evt.setKeepInventory(false);
            }
            evt.setKeepLevel(false);
            evt.setDroppedExp(Math.min(evt.getEntity().getLevel() * 7, 100));
            commandExec.runCommands(evt.getEntity(), true, "settings.run-commands-on-death-if-drops", true);
            commandExec.runRandomCommands(evt.getEntity(), true, "settings.run-random-commands-on-death-if-drops", true);
            Bukkit.getServer().getPluginManager().callEvent(new PlayerDropInventoryEvent(evt.getEntity(), pluginHandler, dataManager));
            Debugger.logDebugMessage(evt.getEntity().getName() + " died, consumeType: " + consumeType);
            return;
        }

        Debugger.logDebugMessage(evt.getEntity().getName() + " died, consumeType: " + consumeType);

        //Process player inventory stage
        PlayerStorage.removeKiller(evt.getEntity());
        PlayerStorage.clearPlayer(evt.getEntity());
        PlayerStorage.setConsumed(evt.getEntity(), consumeItems[consumeType]);
        Debugger.logDebugMessage(evt.getEntity().getName() + " died, consumed item: " + consumeItems[consumeType]);
        commandExec.doKeepModInventory(evt.getEntity());
        commandExec.runCommands(evt.getEntity(), true, consumeItems[consumeType] + ".run-commands-on-death", false);
        commandExec.runRandomCommands(evt.getEntity(), true, consumeItems[consumeType] + ".run-random-commands-on-death", false);
        PlayerStorage.isKeep.put(evt.getEntity(), true);
        boolean clearVanish = pluginHandler.getBooleanConfigValue("clear-vanishing-curse-items", true);
        boolean dropBinding = pluginHandler.getBooleanConfigValue("drop-binding-curse-items", true);
        if (consumeType == ConsumeType.CONSUME_PHYSICAL) {
            ItemStack targetItem = evt.getEntity().getInventory().getItem(physicalSlot);
            int amount = targetItem.getAmount() - 1;
            if (amount <= 0) {
                targetItem = null;
            } else {
                targetItem.setAmount(amount);
            }
            evt.getEntity().getInventory().setItem(physicalSlot, targetItem);
            Debugger.logDebugMessage(evt.getEntity().getName() + " died, target slot:" + physicalSlot);
        } else if (consumeType == ConsumeType.CONSUME_VIRTUAL) {
            dataManager.setConsumed(evt.getEntity(), consumeItems[consumeType].getName());
        }
        int i = 0;
        boolean isModern = PluginHandler.FIXED_SERVER_VERSION > 1101;
        for (ItemStack item : evt.getEntity().getInventory()) {
            //Clear custom item
            if (item == null) {
                i++;
                continue;
            }
            if (isModern) {
                try {
                    if (clearVanish && item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                        Debugger.logDebugMessage("vanishing curse item slot: " + i);
                        evt.getEntity().getInventory().setItem(i, null);
                    }
                    if (dropBinding && item.containsEnchantment(Enchantment.BINDING_CURSE)) {
                        Debugger.logDebugMessage("binding curse item slot: " + i);
                        evt.getEntity().getWorld().dropItem(evt.getEntity().getLocation(), item);
                        evt.getEntity().getInventory().setItem(i, null);
                    }
                } catch (Exception ignored) {
                }
            }
            if (!item.hasItemMeta()) {
                i++;
                continue;
            }
            if (!item.getItemMeta().hasLore()) {
                i++;
                continue;
            }
            clearSpecificItems:
            for (String lore : pluginHandler.getList(consumeItems[consumeType] + ".items-with-lore-to-be-removed-on-death", false)) {
                for (String itemLore : item.getItemMeta().getLore()) {
                    if (itemLore.equalsIgnoreCase(lore)) {
                        evt.getEntity().getInventory().setItem(i, null);
                        break clearSpecificItems;
                    }
                }
            }
            i++;
        }
        Debugger.logDebugMessage("inventory size: " + i + " / " + evt.getEntity().getInventory().getSize());
        Bukkit.getServer().getPluginManager().callEvent(new PlayerConsumeItemEvent(evt.getEntity(), consumeItems[consumeType],
                pluginHandler, dataManager, consumeType));
        if (pluginHandler.compatInventory) {
            PlayerStorage.saveInventory(evt.getEntity(), new PlayerInventoryStorage(evt.getEntity()));
            evt.getEntity().getInventory().clear();
            evt.getEntity().getInventory().setArmorContents(null);
        } else {
            evt.setKeepInventory(true);
        }
        evt.getDrops().clear();
        evt.getEntity().sendMessage(pluginHandler.getConfigValue(consumeItems[consumeType] + ".death-message", false));
        if (pluginHandler.getBooleanConfigValue(consumeItems[consumeType] + ".save-exp", false)) {
            if (pluginHandler.compatLevel) {
                PlayerStorage.saveLevel(evt.getEntity(), evt.getEntity().getLevel());
                evt.setKeepLevel(false);
                evt.setDroppedExp(0);
                evt.setNewLevel(0);
            } else {
                evt.setKeepLevel(true);
                evt.setDroppedExp(0);
            }
        } else {
            int lost = expHandler.loseExp(evt, consumeItems[consumeType].getName());
            if (pluginHandler.compatLevel) {
                PlayerStorage.saveLevel(evt.getEntity(), evt.getEntity().getLevel() - lost);
                evt.setNewLevel(0);
            }
            evt.setKeepLevel(false);
            evt.setDroppedExp(0);
            evt.getEntity().sendMessage(pluginHandler.getMessage("lost-exp")
                    .replace("%amount%", String.valueOf(lost))
                    .replace("%total%", String.valueOf(evt.getEntity().getLevel() - lost)));
        }
        Debugger.logDebugMessage(evt.getEntity().getName() + " death status:");
        Debugger.logDebugMessage("keep level: " + evt.getKeepLevel());
        Debugger.logDebugMessage("keep inventory: " + evt.getKeepInventory());
        Debugger.logDebugMessage("compat inventory: " + pluginHandler.compatInventory);
        Debugger.logDebugMessage("compat exp: " + pluginHandler.compatLevel);
        Debugger.logDebugMessage("new level: " + evt.getNewLevel());
        Debugger.logDebugMessage("old level: " + evt.getEntity().getLevel());
    }

    private void processPhysicalItems(PlayerDeathEvent evt, String playerWorld, HashMap<String, Integer> passedPhysicalItems, int i, ItemBase itemBase) {
        for (String world : pluginHandler.getDisabledWorlds(itemBase.getName())) {
            if (world.equalsIgnoreCase(playerWorld)) {
                return;
            }
        }
        if (!processCondition(itemBase.getName(), evt.getEntity())) {
            return;
        }
        passedPhysicalItems.put(itemBase.getName(), i);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeathDebugger(PlayerDeathEvent evt) {
        Debugger.logDebugMessage(evt.getEntity().getName() + " : final death status:");
        Debugger.logDebugMessage("keep level: " + evt.getKeepLevel());
        Debugger.logDebugMessage("keep inventory: " + evt.getKeepInventory());
        Debugger.logDebugMessage("new level: " + evt.getNewLevel());
        Debugger.logDebugMessage("old level: " + evt.getEntity().getLevel());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void forceKeepInventory(PlayerDeathEvent evt) {
        boolean isKeep;
        try {
            isKeep = PlayerStorage.isKeep.get(evt.getEntity());
        } catch (NullPointerException e) {
            return;
        }
        //force override the result if other plugin changed it
        if (isKeep && !pluginHandler.compatInventory && !evt.getKeepInventory()) {
            Debugger.logDebugMessage("override result to keep inventory.");
            evt.setKeepInventory(true);
        } else if (!isKeep && evt.getKeepInventory()) {
            Debugger.logDebugMessage("override result to drop inventory");
            evt.setKeepInventory(false);
        }
        PlayerStorage.isKeep.remove(evt.getEntity());
    }

    public boolean processCondition(String itemName, Player player) {
        if (PlayerStorage.isKilledByEntity(player)) {
            if (PlayerStorage.getKiller(player).contains("PLAYER|")) {
                if (!pluginHandler.getBooleanConfigValue(itemName + ".enabled-death-type.PVP", false)) {
                    return false;
                }
            } else if (pluginHandler.getBooleanConfigValue(itemName + ".enabled-death-type." + PlayerStorage.getDeathCause(player).toString(), false)) {
                if (PlayerStorage.getKiller(player).contains("|")) {
                    boolean isPassedEntity = pluginHandler.passConditionEntity(PlayerStorage.getKiller(player), itemName);
                    boolean isPassedName = pluginHandler.passConditionEntityName(PlayerStorage.getKiller(player), itemName);
                    if (pluginHandler.isBlackList(false, itemName)) {
                        return isPassedEntity && isPassedName;
                    } else {
                        return isPassedName;
                    }
                } else {
                    return pluginHandler.passConditionEntity(PlayerStorage.getKiller(player), itemName);
                }
            } else {
                return false;
            }
        } else {
            return pluginHandler.getBooleanConfigValue(itemName + ".enabled-death-type." + PlayerStorage.getDeathCause(player).toString(), false);
        }
        return true;
    }

}
