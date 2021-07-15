package me.starchier.inventorykeeper.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.starchier.inventorykeeper.InventoryKeeper;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;

public class ItemHandler {
    private InventoryKeeper plugin;

    public ItemHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }

    public boolean isItem() {
        PluginHandler ph = new PluginHandler(plugin);
        return Material.matchMaterial(ph.getSettings("keep-inventory-item.item-id").split(":")[0]) != null;
    }

    public void validEnchant() {
        PluginHandler ph = new PluginHandler(plugin);
        if (ph.isLegacy()) {
            for (String s : ph.getList("settings.keep-inventory-item.item-enchantments")) {
                if (Enchantment.getByName(s.split("-")[0].toUpperCase()) == null) {
                    plugin.getLogger().warning(new StringBuilder().append("Enchantment ").append(s.split("-")[0]).append(" is not valid!").toString());
                }
            }
        } else {
            for (String s : ph.getList("settings.keep-inventory-item.item-enchantments")) {
                EnchantmentWrapper enchantmentWrapper;
                try {
                    enchantmentWrapper = new EnchantmentWrapper(s.split("-")[0].toLowerCase());
                    ItemStack temp = new ItemStack(Material.STICK);
                    temp.addUnsafeEnchantment(enchantmentWrapper.getEnchantment(), 1);
                } catch (Exception e) {
                    plugin.getLogger().severe(new StringBuilder().append("Enchantment ").append(s.split("-")[0]).append(" is not valid!").toString());
                }
            }
        }
    }

    public boolean isSkull() {
        PluginHandler pluginHandler = new PluginHandler(plugin);
        try {
            pluginHandler.getSettings("keep-inventory-item.custom-texture");
        } catch (Exception e) {
            return false;
        }
        if (pluginHandler.isLegacy()) {
            if (pluginHandler.getSettings("keep-inventory-item.item-id").contains(":")) {
                return pluginHandler.getSettings("keep-inventory-item.item-id").equals("397:3") ||
                        pluginHandler.getSettings("keep-inventory-item.item-id").equalsIgnoreCase("skull_item:3");
            }
        } else {
            return Material.matchMaterial(pluginHandler.getSettings("keep-inventory-item.item-id")) == Material.PLAYER_HEAD;
        }
        return false;
    }

    public String getCustomText() {
        PluginHandler pluginHandler = new PluginHandler(plugin);
        try {
            return pluginHandler.getSettings("keep-inventory-item.custom-texture");
        } catch (Exception e) {
            return null;
        }
    }

    public void cacheSkull() {
        if (isSkull()) {
            File cache = new File(plugin.getDataFolder(), "skull_cache.yml");
            YamlConfiguration cacheData = YamlConfiguration.loadConfiguration(cache);
            PluginHandler ph = new PluginHandler(plugin);
            String base = ph.getSettings("keep-inventory-item.custom-texture");
            String skull = cacheData.getString("cache.skull", null);
            if (base != null) {
                if (skull == null || !Objects.equals(base, skull)) {
                    cacheData.set("cache.skull", base);
                    cacheData.set("cache.uuid", UUID.randomUUID().toString());
                    try {
                        cacheData.save(cache);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public ItemStack getSaveItem() {
        PluginHandler ph = new PluginHandler(plugin);
        String id = ph.getSettings("keep-inventory-item.item-id");
        ItemStack item;
        if (id.contains(":")) {
            item = new ItemStack(Material.matchMaterial(id.split(":")[0]), 1, Short.parseShort(id.split(":")[1]));
        } else {
            item = new ItemStack(Material.matchMaterial(id.split(":")[0]));
        }
        ItemMeta data = item.getItemMeta();
        data.setDisplayName(ph.getSettings("keep-inventory-item.item-name"));
        data.setLore(ph.getList("settings.keep-inventory-item.item-lore"));
        if (!ph.isLegacy()) {
            int model = ph.getCfg().getInt("settings.keep-inventory-item.custom-model-data", -1);
            if (model != -1) data.setCustomModelData(model);
        }
        if (isSkull()) {
            File cache = new File(plugin.getDataFolder(), "skull_cache.yml");
            YamlConfiguration cacheData = YamlConfiguration.loadConfiguration(cache);
            UUID skullUUID = UUID.fromString(cacheData.getString("cache.uuid"));
            GameProfile profile = new GameProfile(skullUUID, null);
            profile.getProperties().put("textures", new Property("textures", getCustomText()));
            Field profileField;
            try {
                profileField = data.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(data, profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        item.setItemMeta(data);
        if (!ph.isEmpty("keep-inventory-item.item-enchantments")) {
            if (ph.isLegacy()) {
                for (String s : ph.getList("settings.keep-inventory-item.item-enchantments")) {
                    if (Enchantment.getByName(s.split("-")[0].toUpperCase()) == null) {
                        continue;
                    }
                    item.addUnsafeEnchantment(Enchantment.getByName(s.split("-")[0].toUpperCase()), Integer.parseInt(s.split("-")[1]));
                }
            } else {
                for (String s : ph.getList("settings.keep-inventory-item.item-enchantments")) {
                    EnchantmentWrapper enchantmentWrapper;
                    try {
                        enchantmentWrapper = new EnchantmentWrapper(s.split("-")[0].toLowerCase());
                        item.addUnsafeEnchantment(enchantmentWrapper.getEnchantment(),
                                Integer.parseInt(s.split("-")[1]));
                    } catch (Exception e) {
                    }
                }
            }
        }
        return item;
    }
}
