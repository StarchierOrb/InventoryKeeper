package me.starchier.inventorykeeper.util;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

public class ConversionUtil {
    private final InventoryKeeper plugin;

    public ConversionUtil(InventoryKeeper plugin) {
        this.plugin = plugin;
    }

    public void convertConfig() {
        File config = new File(plugin.getDataFolder(), "config.yml");
        if (!config.exists()) {
            return;
        }
        FileConfiguration cfg = YamlConfiguration.loadConfiguration(config);
        if (cfg.getConfigurationSection("settings").getValues(true).isEmpty()) {
            return;
        }
        plugin.getLogger().info(MessagesUtil.getMessage("converting"));
        try {
            Files.copy(config.toPath(), new File(plugin.getDataFolder(), "config_old.yml").toPath());
            File itemsFile = new File(plugin.getDataFolder(), "items.yml");
            if (!itemsFile.exists() && itemsFile.createNewFile()) {
                FileConfiguration data = YamlConfiguration.loadConfiguration(itemsFile);
                data.set("items.default.item-id", cfg.getString("settings.keep-inventory-item.item-id", "STICK"));
                data.set("items.default.name", cfg.getString("settings.keep-inventory-item.item-name"));
                data.set("items.default.item-name", cfg.getString("settings.keep-inventory-item.item-name"));
                data.set("items.default.item-lore", cfg.getList("settings.keep-inventory-item.item-lore"));
                data.set("items.default.item-enchantments", cfg.getList("settings.keep-inventory-item.item-enchantments"));
                data.set("items.default.save-exp", cfg.getBoolean("settings.save-exp", false));
                data.set("items.default.exp-lose-percentage", cfg.getString("settings.exp-lose-percentage", "10-30"));
                data.set("items.default.disabled-worlds", cfg.getList("settings.disabled-worlds"));
                data.set("items.default.run-commands-on-death", cfg.getList("settings.run-commands-on-death"));
                data.set("items.default.run-random-commands-on-death", cfg.getList("settings.run-random-commands-on-death"));
                data.set("items.default.run-commands-on-respawn", cfg.getList("settings.run-commands-on-respawn"));
                data.set("items.default.run-random-commands-on-respawn", cfg.getList("settings.run-random-commands-on-respawn"));
                data.set("items.default.enabled-death-type", cfg.getConfigurationSection("settings.enabled-death-type").getValues(false));
                data.set("items.default.filter-entities-list.is-blacklist", cfg.getBoolean("settings.filter-entities-list.is-blacklist", true));
                data.set("items.default.filter-entities-list.entities", cfg.getList("settings.filter-entities-list.entities"));
                data.set("items.default.filter-entities-name.is-blacklist", cfg.getBoolean("settings.filter-entities-name.is-blacklist", true));
                data.set("items.default.filter-entities-name.names-list", cfg.getList("settings.filter-entities-name.names-list"));
                data.set("items.default.items-with-lore-to-be-removed-on-death", cfg.getList("settings.items-with-lore-to-be-removed-on-death"));
                data.set("items.default.death-message", cfg.getString("messages.saved-inventory"));
                data.set("items.default.priority", 10);
                data.save(itemsFile);
            }
            HashMap<String, Object> settings = new HashMap<>();
            // Using default values of old version
            settings.put("galacticraft-mod-support", cfg.getBoolean("settings.galacticraft-mod-support", false));
            settings.put("clear-vanishing-curse-items", cfg.getBoolean("settings.clear-vanishing-curse-items", true));
            settings.put("show-death-cause-on-death", cfg.getBoolean("settings.show-death-cause-on-death", false));
            settings.put("drop-binding-curse-items", cfg.getBoolean("settings.drop-binding-curse-items", false));
            settings.put("run-commands-on-death-if-drops", cfg.getList("settings.run-commands-on-death-if-drops"));
            settings.put("run-random-commands-on-death-if-drops", cfg.getList("settings.run-random-commands-on-death-if-drops"));
            settings.put("run-commands-on-respawn-if-drops", cfg.getList("settings.run-commands-on-respawn-if-drops"));
            settings.put("run-random-commands-on-respawn-if-drops", cfg.getList("settings.run-random-commands-on-respawn-if-drops"));
            cfg.set("settings", settings);
            cfg.set("messages", new HashMap<>());
            cfg.save(config);
            plugin.getLogger().info(MessagesUtil.getMessage("convert-old-config"));
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().warning(MessagesUtil.getMessage("convert-failed"));
        }
    }

    public void convertData() {
        File dataFile = new File(plugin.getDataFolder(), "data.yml");
        if (!dataFile.exists()) {
            return;
        }
        FileConfiguration data = YamlConfiguration.loadConfiguration(dataFile);
        try {
            for (Map.Entry<String, Object> e : data.getConfigurationSection("playerdata").getValues(true).entrySet()) {
                if (!(e.getValue() instanceof Integer)) {
                    return;
                } else {
                    break;
                }
            }
        } catch (NullPointerException e) {
            return;
        }
        plugin.getLogger().info(MessagesUtil.getMessage("converting-data"));
        HashMap<String, Object> dataMap = (HashMap<String, Object>) data.getConfigurationSection("playerdata").getValues(false);
        try {
            Files.copy(dataFile.toPath(), new File(plugin.getDataFolder(), "data_old.yml").toPath());
            data.set("playerdata", new HashMap<>());
            for (Map.Entry<String, Object> e : dataMap.entrySet()) {
                data.set("playerdata." + e.getKey() + ".default", e.getValue());
            }
            data.save(dataFile);
            plugin.getLogger().info(MessagesUtil.getMessage("convert-old-data"));
        } catch (IOException e) {
            e.printStackTrace();
            plugin.getLogger().warning(MessagesUtil.getMessage("convert-failed"));
        }
    }

    public void convertSkull() {
        File skullCache = new File(plugin.getDataFolder(), "skull_cache.yml");
        if (!skullCache.exists()) {
            return;
        }
        FileConfiguration skull = YamlConfiguration.loadConfiguration(skullCache);
        if (skull.getString("cache.uuid", null) == null) {
            return;
        }
        String skullTexture = skull.getString("cache.skull");
        String uuid = skull.getString("cache.uuid");
        skull.set("cache.uuid", null);
        skull.set("cache.skull", null);
        skull.set("cache.default.uuid", uuid);
        skull.set("cache.default.skull", skullTexture);
        try {
            skull.save(skullCache);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
