package me.starchier.inventorykeeper.hooks;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.util.DataManager;
import me.starchier.inventorykeeper.util.Debugger;
import me.starchier.inventorykeeper.util.PluginHandler;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final InventoryKeeper plugin;
    private final DataManager dataManager;
    private final PluginHandler pluginHandler;

    public PlaceholderAPIHook(InventoryKeeper plugin, DataManager dataManager, PluginHandler pluginHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.pluginHandler = pluginHandler;
    }

    /**
     * Because this is an internal class,
     * you must override this method to let PlaceholderAPI know to not unregister your expansion class when
     * PlaceholderAPI is reloaded
     *
     * @return true to persist through reloads
     */
    @Override
    public boolean persist(){
        return true;
    }
    /**
     * Because this is a internal class, this check is not needed
     * and we can simply return {@code true}
     *
     * @return Always true since it's an internal class.
     */
    @Override
    public boolean canRegister(){
        return true;
    }
    /**
     * The name of the person who created this expansion should go here.
     * <br>For convienience do we return the author from the plugin.yml
     *
     * @return The name of the author as a String.
     */
    @Override
    public String getAuthor(){
        return plugin.getDescription().getAuthors().toString();
    }
    /**
     * The placeholder identifier should go here.
     * <br>This is what tells PlaceholderAPI to call our onRequest
     * method to obtain a value if a placeholder starts with our
     * identifier.
     * <br>This must be unique and can not contain % or _
     *
     * @return The identifier in {@code %<identifier>_<value>%} as String.
     */
    @Override
    public String getIdentifier(){
        return "inventorykeeper";
    }
    /**
     * This is the version of the expansion.
     * <br>You don't have to use numbers, since it is set as a String.
     *
     * For convienience do we return the version from the plugin.yml
     *
     * @return The version as a String.
     */
    @Override
    public String getVersion(){
        return plugin.getDescription().getVersion();
    }
    @Override
    public String onPlaceholderRequest(Player player, String identifier) {

        if (player == null) {
            return null;
        }


        if (identifier.startsWith("amount_")) {
            String element = identifier.split("_", 2)[1];
            int index = element.lastIndexOf(',');
            if (index == -1) {
                if (pluginHandler.itemNames.contains(element)) {
                    return String.valueOf(dataManager.getVirtualCount(player, element));
                } else {
                    return null;
                }
            }
            String playerName = element.substring(index).replace(",", "");
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().equals(playerName)) {
                    String itemName = element.replace("," + playerName, "");
                    if (!pluginHandler.itemNames.contains(itemName)) {
                        return null;
                    }
                    return String.valueOf(dataManager.getVirtualCount(p, itemName));
                }
            }
        }

        // We return null if an invalid placeholder (f.e. %someplugin_placeholder3%)
        // was provided
        return null;
    }
}
