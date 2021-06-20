package me.starchier.inventorykeeper.events;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.command.CommandExec;
import me.starchier.inventorykeeper.storage.PlayerStorage;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.ExpHandler;
import me.starchier.inventorykeeper.util.ItemHandler;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.ChatColor;
import org.bukkit.GameRule;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;
import java.util.Locale;

public class DeathHandler implements Listener {
    private final InventoryKeeper plugin;
    private final DataManager dataManager;
    private final CommandExec commandExec;
    private final boolean getLang = Locale.getDefault().getLanguage().equalsIgnoreCase("zh");
    public DeathHandler(InventoryKeeper plugin, DataManager dataManager, CommandExec commandExec) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.commandExec = commandExec;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent evt) {
        PluginHandler ph = new PluginHandler(plugin);
        int lost=0;
        boolean isDisabled = false, keepInv=false;
        for(String s: ph.getDisableWorlds()) {
            if(evt.getEntity().getWorld().getName().equalsIgnoreCase(s)) {
                isDisabled = true;
                break;
            }
        }
        //DEBUG OPTION
        if(ph.getBoolCfg("show-death-cause-on-death")) {
            if (getLang) {
                plugin.getLogger().info(ChatColor.GOLD + " [DEBUG]  玩家" + evt.getEntity().getName() + "的死亡原因： " + PlayerStorage.getDeathCause(evt.getEntity()));
            } else {
                plugin.getLogger().info(ChatColor.GOLD + " [DEBUG]  Player " + evt.getEntity().getName() + " died from : " + PlayerStorage.getDeathCause(evt.getEntity()));
            }
        }
        if(ph.isLegacy()) {
            keepInv = evt.getEntity().getWorld().getGameRuleValue("keepInventory").equals("true");
        } else {
            keepInv = evt.getEntity().getWorld().getGameRuleValue(GameRule.KEEP_INVENTORY);
        }
        if(keepInv) {
            return;
        }
        //boolean isSet = false;
        if(PlayerStorage.isKilledByEntity(evt.getEntity())) {
            if(PlayerStorage.getKiller(evt.getEntity()).contains("PLAYER|")) {
                isDisabled = !ph.getBoolCfg("enabled-death-type.PVP");
                //isDisabled = !ph.getSettings("enabled-death-type.PVP").equalsIgnoreCase("true");
                //isSet = true;
            } else if(ph.getBoolCfg("enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString())) {
                if(PlayerStorage.getKiller(evt.getEntity()).contains("|")) {
                    boolean entityStat = ph.getStatByEntity(PlayerStorage.getKiller(evt.getEntity()));
                    boolean nameStat = ph.getStatByName(PlayerStorage.getKiller(evt.getEntity()));
                    if(ph.isBlackList(false)) {
                        isDisabled = !(entityStat && nameStat);
                    } else {
                        isDisabled = !nameStat;
                    }
                } else {
                    isDisabled = !ph.getStatByEntity(PlayerStorage.getKiller(evt.getEntity()));
                }
            } else {
                isDisabled = true;
            }
        } else if(ph.getBoolCfg("enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString())==null) {
            plugin.getLogger().severe("Could not find key: " + "enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString());
        } else if(!ph.getBoolCfg("enabled-death-type." + PlayerStorage.getDeathCause(evt.getEntity()).toString())) {
            isDisabled = true;
        }
        PlayerStorage.removeKiller(evt.getEntity());
        PlayerStorage.clearPlayer(evt.getEntity());
        if(!isDisabled) {
            boolean clearVanish = ph.getSettings("clear-vanishing-curse-items").equalsIgnoreCase("true")||
                    ph.getSettings("clear-vanishing-curse-items")==null;
            boolean dropBinding = ph.getSettings("drop-binding-curse-items").equalsIgnoreCase("true");
            ItemHandler ih = new ItemHandler(plugin);
            boolean hasItem =false;
            if(evt.getEntity().hasPermission("inventorykeeper.keep")) {
                hasItem = true;
            }
            if(!hasItem) {
                for (int i = 0; i < evt.getEntity().getInventory().getSize(); i++) {
                    if (evt.getEntity().getInventory().getItem(i) == null) {
                        continue;
                    }
                    try {
                        if (evt.getEntity().getInventory().getItem(i).isSimilar(ih.getSaveItem())) {
                            hasItem = true;
                            break;
                        }
                    } catch (Exception e) {
                        ItemMeta item = ih.getSaveItem().getItemMeta();
                        ItemMeta target = evt.getEntity().getInventory().getItem(i).getItemMeta();
                        if (item.getDisplayName().equals(target.getDisplayName()) && item.getLore().equals(target.getLore()) &&
                                ih.getSaveItem().getType().equals(evt.getEntity().getInventory().getItem(i).getType())) {
                            hasItem = true;
                            break;
                        }
                    }
                }
            }
            if(!hasItem) {
                if(dataManager.getVirtualCount(evt.getEntity())>0) {
                    hasItem=true;
                }
            }
            if(hasItem) {
                PlayerStorage.setConsumed(evt.getEntity(), true);
                commandExec.doKeepModInventory(evt.getEntity());
                commandExec.runCommands(evt.getEntity(), true, "settings.run-commands-on-death");
                commandExec.runRandomCommands(evt.getEntity(), true, "settings.run-random-commands-on-death");
                evt.setKeepInventory(true);
                if (!ph.isLegacy()) {
                    evt.getDrops().clear();
                }
                String ver = ph.getVersion().replace(".","").replace("_","").replace("v","").
                        replace("_","").replace("R","");
                int i = 0;
                for(ItemStack item : evt.getEntity().getInventory()) {
                    if(item == null) {
                        i++;
                        continue;
                    }
                    if(!item.hasItemMeta()) {
                        i++;
                        continue;
                    }
                    if(!item.getItemMeta().hasLore()) {
                        i++;
                        continue;
                    }
                    for(String lore : ph.getList("settings.items-with-lore-to-be-removed-on-death")) {
                        boolean done = false;
                        for(String itemLore : item.getItemMeta().getLore()) {
                            if(itemLore.equalsIgnoreCase(lore)) {
                                evt.getEntity().getInventory().setItem(i, null);
                                done = true;
                                break;
                            }
                        }
                        if(done) {
                            break;
                        }
                    }
                    i++;
                }
                if (Integer.parseInt(ver) > 1101) {
                    i = 0;
                    for (ItemStack item : evt.getEntity().getInventory()) {
                        if (clearVanish) {
                            try {
                                if (item.containsEnchantment(Enchantment.VANISHING_CURSE)) {
                                    evt.getEntity().getInventory().setItem(i, null);
                                    i++;
                                    continue;
                                }
                            } catch (Exception e) {
                            }
                        }
                        if (dropBinding) {
                            try {
                                if (item.containsEnchantment(Enchantment.BINDING_CURSE)) {
                                    evt.getEntity().getWorld().dropItem(evt.getEntity().getLocation(), item);
                                    evt.getEntity().getInventory().setItem(i, null);
                                }
                            } catch (Exception e) {
                            }
                        }
                        i++;
                    }
                }
                evt.getEntity().sendMessage(ph.getMsg("saved-inventory"));
                if (ph.getCfg().getBoolean("settings.save-exp")) {
                    evt.setKeepLevel(true);
                    evt.setDroppedExp(0);
                } else {
                    evt.setKeepLevel(false);
                    evt.setDroppedExp(0);
                    ExpHandler eh = new ExpHandler(plugin);
                    lost = eh.loseExp(evt);
                    evt.getEntity().sendMessage(ph.getMsg("lost-exp").replace("%s1", String.valueOf(lost)).replace("%s2", String.valueOf(evt.getEntity().getLevel() - lost)));
                }
                return;
            }
        }
        PlayerStorage.setConsumed(evt.getEntity(), false);
        evt.setKeepLevel(false);
        evt.setDroppedExp(Math.min(evt.getEntity().getLevel() * 7, 100));
        commandExec.runCommands(evt.getEntity(), true, "settings.run-commands-on-death-if-drops");
        commandExec.runRandomCommands(evt.getEntity(), true, "settings.run-random-commands-on-death-if-drops");
    }
}
