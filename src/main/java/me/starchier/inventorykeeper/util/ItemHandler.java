package me.starchier.inventorykeeper.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ItemHandler {
    private InventoryKeeper plugin;
    private PluginHandler pluginHandler;

    public ItemHandler(InventoryKeeper plugin) {
        this.plugin = plugin;
    }

    public boolean isItem(String name) {
        PluginHandler ph = new PluginHandler(plugin);
        return Material.matchMaterial(ph.getConfigValue(name + ".item-id", false).split(":")[0]) != null;
    }

    public void validEnchant(String name) {
        PluginHandler ph = new PluginHandler(plugin);
        if (PluginHandler.IS_LEGACY) {
            for (String s : ph.getList(name + ".item-enchantments", false)) {
                if (Enchantment.getByName(s.split("-")[0].toUpperCase()) == null) {
                    plugin.getLogger().warning(String.format(MessagesUtil.getMessage("invalid-enchantment"), s.split("-")[0], name));
                }
            }
        } else {
            for (String s : ph.getList(name + ".item-enchantments", false)) {
                EnchantmentWrapper enchantmentWrapper;
                try {
                    enchantmentWrapper = new EnchantmentWrapper(s.split("-")[0].toLowerCase());
                    ItemStack temp = new ItemStack(Material.STICK);
                    temp.addUnsafeEnchantment(enchantmentWrapper.getEnchantment(), 1);
                } catch (Exception e) {
                    plugin.getLogger().warning(String.format(MessagesUtil.getMessage("invalid-enchantment"), s.split("-")[0], name));
                }
            }
        }
    }

    public boolean isSkull(String name) {
        PluginHandler pluginHandler = new PluginHandler(plugin);
        String skullTexture = getSkullTexture(name);
        if (skullTexture == null) {
            return false;
        }
        if (PluginHandler.IS_LEGACY) {
            String itemID = pluginHandler.getConfigValue(name + ".item-id", false);
            if (itemID.contains(":")) {
                return itemID.equals("397:3") || itemID.equalsIgnoreCase("skull_item:3");
            }
        } else {
            return Material.matchMaterial(pluginHandler.getConfigValue(name + ".item-id", false)) == Material.PLAYER_HEAD;
        }
        return false;
    }

    public String getSkullTexture(String name) {
        PluginHandler pluginHandler = new PluginHandler(plugin);
        try {
            return pluginHandler.getConfigValue(name + ".custom-texture", false);
        } catch (Exception e) {
            return null;
        }
    }

    public void cacheSkull(String name) {
        if (isSkull(name)) {
            File cache = new File(plugin.getDataFolder(), "skull_cache.yml");
            PluginHandler ph = new PluginHandler(plugin);
            String base = getSkullTexture(name);
            String skull = ph.skullCache.getString("cache." + name + ".skull", null);
            if (base != null) {
                if (skull == null || !Objects.equals(base, skull)) {
                    ph.skullCache.set("cache." + name + ".skull", base);
                    ph.skullCache.set("cache." + name + ".uuid", UUID.randomUUID().toString());
                    try {
                        ph.skullCache.save(cache);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public ItemStack buildItem(String name) {
        PluginHandler ph = new PluginHandler(plugin);
        String id = ph.getConfigValue(name + ".item-id", false);
        ItemStack item;
        if (id.contains(":")) {
            item = new ItemStack(Material.matchMaterial(id.split(":")[0]), 1, Short.parseShort(id.split(":")[1]));
        } else {
            item = new ItemStack(Material.matchMaterial(id.split(":")[0]));
        }
        ItemMeta data = item.getItemMeta();
        String displayName = ph.getConfigValue(name + ".item-name", false);
        if (displayName != null) {
            data.setDisplayName(displayName);
        }
        List<String> itemLore = ph.getList(name + ".item-lore", false);
        if (!itemLore.isEmpty()) {
            data.setLore(itemLore);
        }
        if (!PluginHandler.IS_LEGACY) {
            int model = ph.itemsConfig.getInt("items." + name + ".custom-model-data", -1);
            if (model != -1) data.setCustomModelData(model);
        }
        if (isSkull(name)) {
            UUID skullUUID = UUID.fromString(ph.skullCache.getString("cache." + name + ".uuid"));
            GameProfile profile = new GameProfile(skullUUID, null);
            profile.getProperties().put("textures", new Property("textures", getSkullTexture(name)));
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
        List<String> enchantments = ph.getList(name + "item-enchantments", false);
        if (!enchantments.isEmpty()) {
            if (PluginHandler.IS_LEGACY) {
                for (String s : enchantments) {
                    if (Enchantment.getByName(s.split("-")[0].toUpperCase()) == null) {
                        continue;
                    }
                    item.addUnsafeEnchantment(Enchantment.getByName(s.split("-")[0].toUpperCase()), Integer.parseInt(s.split("-")[1]));
                }
            } else {
                for (String s : enchantments) {
                    EnchantmentWrapper enchantmentWrapper;
                    try {
                        enchantmentWrapper = new EnchantmentWrapper(s.split("-")[0].toLowerCase());
                        item.addUnsafeEnchantment(enchantmentWrapper.getEnchantment(),
                                Integer.parseInt(s.split("-")[1]));
                    } catch (Exception ignored) {
                    }
                }
            }
        }
        return item;
    }
}
