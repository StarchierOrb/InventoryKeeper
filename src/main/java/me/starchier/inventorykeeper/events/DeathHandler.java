package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.items.ItemBase;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.util.ExpHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.enchantments.Enchantment;
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

    private boolean keep;

    public DeathHandler(InventoryKeeper plugin, DataManager dataManager, CommandExec commandExec, PluginHandler pluginHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.commandExec = commandExec;
        this.pluginHandler = pluginHandler;
        expHandler = new ExpHandler(pluginHandler);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        //Consume Type
        int CONSUME_NONE = -1;
        int CONSUME_PERMISSION = 0;
        int CONSUME_PHYSICAL = 1;
        int CONSUME_VIRTUAL = 2;

        //DEBUG OPTION
        if (pluginHandler.getBooleanConfigValue("show-death-cause-on-death", true)) {
            plugin.getLogger().info(ChatColor.GOLD + String.format(MessagesUtil.getMessage("debug.death-cause"), evt.getEntity().getName(), PlayerStorage.getDeathCause(evt.getEntity())));
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
        keep = false;

        //Shared list
        List<String> passedItems = new ArrayList<>();
        String playerWorld = evt.getEntity().getWorld().getName();
        int consumeType = CONSUME_NONE;
        String[] consumeItemNames = new String[3];

        //Permission check
        permissionCheck:
        for (String itemName : pluginHandler.itemNames) {
            if (evt.getEntity().hasPermission("inventorykeeper.unlimited." + itemName)) {
                for (String s : pluginHandler.getDisabledWorlds(itemName)) {
                    if (playerWorld.equalsIgnoreCase(s)) {
                        continue permissionCheck;
                    }
                }
                passedItems.add(itemName);
            }
        }
        if (!passedItems.isEmpty()) {
            for (ItemBase itemBase : pluginHandler.currentItems) {
                if (passedItems.contains(itemBase.getName())) {
                    consumeItemNames[CONSUME_PERMISSION] = itemBase.getName();
                    consumeType = CONSUME_PERMISSION;
                    break;
                }
            }
        }

        //Physical items check
        int physicalSlot = -1;
        if (consumeType == CONSUME_NONE) {
            HashMap<String, Integer> passedPhysicalItems = new HashMap<>();
            inventoryCheck:
            for (int i = 0; i < evt.getEntity().getInventory().getSize(); i++) {
                if (evt.getEntity().getInventory().getItem(i) == null) {
                    continue;
                }
                for (ItemBase itemBase : pluginHandler.currentItems) {
                    try {
                        if (evt.getEntity().getInventory().getItem(i).isSimilar(itemBase.getItem())) {
                            for (String world : pluginHandler.getDisabledWorlds(itemBase.getName())) {
                                if (world.equalsIgnoreCase(playerWorld)) {
                                    continue inventoryCheck;
                                }
                            }
                            passedPhysicalItems.put(itemBase.getName(), i);
                            break;
                        }
                    } catch (Exception e) {
                        ItemMeta item = itemBase.getItem().getItemMeta();
                        ItemMeta target = evt.getEntity().getInventory().getItem(i).getItemMeta();
                        if (item.getDisplayName().equals(target.getDisplayName()) && item.getLore().equals(target.getLore()) &&
                                itemBase.getItem().getType().equals(evt.getEntity().getInventory().getItem(i).getType())) {
                            for (String world : pluginHandler.getDisabledWorlds(itemBase.getName())) {
                                if (world.equalsIgnoreCase(playerWorld)) {
                                    continue inventoryCheck;
                                }
                            }
                            passedPhysicalItems.put(itemBase.getName(), i);
                            break;
                        }
                    }
                }
            }
            if (!passedPhysicalItems.isEmpty()) {
                for (ItemBase itemBase : pluginHandler.currentItems) {
                    if (passedPhysicalItems.containsKey(itemBase.getName())) {
                        physicalSlot = passedPhysicalItems.get(itemBase.getName());
                        consumeItemNames[CONSUME_PHYSICAL] = itemBase.getName();
                        consumeType = CONSUME_PHYSICAL;
                        break;
                    }
                }
            }
        }

        //Virtual items check
        if (consumeType == CONSUME_NONE) {
            passedItems = new ArrayList<>();
            virtualCheck:
            for (String key : pluginHandler.itemNames) {
                if (dataManager.getVirtualCount(evt.getEntity(), key) > 0) {
                    for (String s : pluginHandler.getDisabledWorlds(key)) {
                        if (playerWorld.equalsIgnoreCase(s)) {
                            continue virtualCheck;
                        }
                    }
                    passedItems.add(key);
                }
            }
            if (!passedItems.isEmpty()) {
                for (ItemBase itemBase : pluginHandler.currentItems) {
                    if (passedItems.contains(itemBase.getName())) {
                        consumeItemNames[CONSUME_VIRTUAL] = itemBase.getName();
                        consumeType = CONSUME_VIRTUAL;
                        break;
                    }
                }
            }
        }

        Debugger.logDebugMessage(evt.getEntity().getName() + " died, consumeType: " + consumeType);

        if (consumeType == CONSUME_NONE) {
            PlayerStorage.removeKiller(evt.getEntity());
            PlayerStorage.clearPlayer(evt.getEntity());
            PlayerStorage.setConsumed(evt.getEntity(), "");
            evt.setKeepLevel(false);
            evt.setDroppedExp(Math.min(evt.getEntity().getLevel() * 7, 100));
            commandExec.runCommands(evt.getEntity(), true, "settings.run-commands-on-death-if-drops", true);
            commandExec.runRandomCommands(evt.getEntity(), true, "settings.run-random-commands-on-death-if-drops", true);
            return;
        }


        //Process player inventory stage
        boolean isConsumedFinally;
        if (PlayerStorage.isKilledByEntity(evt.getEntity())) {
            if (PlayerStorage.getKiller(evt.getEntity()).contains("PLAYER|")) {
                isConsumedFinally = pluginHandler.getBooleanConfigValue(consumeItemNames[consumeType] + ".enabled-death-type.PVP", false);
            } else if (pluginHandler.getBooleanConfigValue(consumeItemNames[consumeType] + ".enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString(), false)) {
                if (PlayerStorage.getKiller(evt.getEntity()).contains("|")) {
                    boolean isPassedEntity = pluginHandler.passConditionEntity(PlayerStorage.getKiller(evt.getEntity()), consumeItemNames[consumeType]);
                    boolean isPassedName = pluginHandler.passConditionEntityName(PlayerStorage.getKiller(evt.getEntity()), consumeItemNames[consumeType]);
                    if (pluginHandler.isBlackList(false, consumeItemNames[consumeType])) {
                        isConsumedFinally = isPassedEntity && isPassedName;
                    } else {
                        isConsumedFinally = isPassedName;
                    }
                } else {
                    isConsumedFinally = pluginHandler.passConditionEntity(PlayerStorage.getKiller(evt.getEntity()), consumeItemNames[consumeType]);
                }
            } else {
                isConsumedFinally = false;
            }
        } else {
            isConsumedFinally = pluginHandler.getBooleanConfigValue(consumeItemNames[consumeType] + ".enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString(), false);
        }
        PlayerStorage.removeKiller(evt.getEntity());
        PlayerStorage.clearPlayer(evt.getEntity());
        if (isConsumedFinally) {
            PlayerStorage.setConsumed(evt.getEntity(), consumeItemNames[consumeType]);
            Debugger.logDebugMessage(evt.getEntity().getName() + " died, consumed item: " + consumeItemNames[consumeType]);
            commandExec.doKeepModInventory(evt.getEntity());
            commandExec.runCommands(evt.getEntity(), true, consumeItemNames[consumeType] + ".run-commands-on-death", false);
            commandExec.runRandomCommands(evt.getEntity(), true, consumeItemNames[consumeType] + ".run-random-commands-on-death", false);
            evt.setKeepInventory(true);
            keep = true;
            if (!PluginHandler.IS_LEGACY) {
                evt.getDrops().clear();
            }
            boolean clearVanish = pluginHandler.getBooleanConfigValue("clear-vanishing-curse-items", true);
            boolean dropBinding = pluginHandler.getBooleanConfigValue("drop-binding-curse-items", true);
            if (consumeType == CONSUME_PHYSICAL) {
                ItemStack targetItem = evt.getEntity().getInventory().getItem(physicalSlot);
                int amount = targetItem.getAmount() - 1;
                if (amount <= 0) {
                    targetItem = null;
                } else {
                    targetItem.setAmount(amount);
                }
                evt.getEntity().getInventory().setItem(physicalSlot, targetItem);
                Debugger.logDebugMessage(evt.getEntity().getName() + " died, target slot:" + physicalSlot);
            } else if (consumeType == CONSUME_VIRTUAL) {
                dataManager.setConsumed(evt.getEntity(), consumeItemNames[consumeType]);
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
                            evt.getEntity().getInventory().setItem(i, null);
                        }
                        if (dropBinding && item.containsEnchantment(Enchantment.BINDING_CURSE)) {
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
                for (String lore : pluginHandler.getList(consumeItemNames[consumeType] + ".items-with-lore-to-be-removed-on-death", false)) {
                    for (String itemLore : item.getItemMeta().getLore()) {
                        if (itemLore.equalsIgnoreCase(lore)) {
                            evt.getEntity().getInventory().setItem(i, null);
                            break clearSpecificItems;
                        }
                    }
                }
                i++;
            }
            evt.getEntity().sendMessage(pluginHandler.getConfigValue(consumeItemNames[consumeType] + ".death-message", false));
            if (pluginHandler.getBooleanConfigValue(consumeItemNames[consumeType] + ".save-exp", false)) {
                evt.setKeepLevel(true);
                evt.setDroppedExp(0);
            } else {
                evt.setKeepLevel(false);
                evt.setDroppedExp(0);
                int lost = expHandler.loseExp(evt, consumeItemNames[consumeType]);
                evt.getEntity().sendMessage(pluginHandler.getMessage("lost-exp")
                        .replace("%amount%", String.valueOf(lost))
                        .replace("%total%", String.valueOf(evt.getEntity().getLevel() - lost)));
            }
            Debugger.logDebugMessage(evt.getEntity().getName() + " death status:");
            Debugger.logDebugMessage("keep level: " + evt.getKeepLevel());
            Debugger.logDebugMessage("keep inventory: " + evt.getKeepInventory());
            Debugger.logDebugMessage("new level: " + evt.getNewLevel());
            Debugger.logDebugMessage("old level: " + evt.getEntity().getLevel());
        }
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
        //force override the result if other plugin changed it
        if (keep && !evt.getKeepInventory()) {
            Debugger.logDebugMessage("override result to keep inventory.");
            evt.setKeepInventory(true);
        }
    }

}
