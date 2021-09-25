package me.starchier.inventorykeeper.api.addon;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import me.starchier.inventorykeeper.manager.PluginHandler;

public abstract class InventoryKeeperAddon {
    public void register() {
        PluginHandler.registerAddon(this);
        InventoryKeeper.getInstance().getLogger().info(MessagesUtil.getMessage("register-addon"));
    }
    public String getDescription() {
        return null;
    }
    public abstract String getAddonName();
    public abstract String getVersion();
    public abstract String getAuthor();
    public abstract void onReload();
}
