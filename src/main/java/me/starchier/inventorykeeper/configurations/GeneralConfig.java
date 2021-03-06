package me.starchier.inventorykeeper.configurations;

import me.starchier.inventorykeeper.InventoryKeeper;
import me.starchier.inventorykeeper.i18n.MessagesUtil;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class GeneralConfig {
    public static class ConfigElement<T> {
        protected String path;
        protected T value;

        public ConfigElement(String path, T value) {
            this.path = path;
            this.value = value;
        }

    }

    public static void initConfig(InventoryKeeper plugin) {
        File cfg = new File(plugin.getDataFolder(), "config.yml");
        try {
            if (!cfg.exists() && !cfg.createNewFile()) {
                throw new IOException("config.yml could not be created.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(cfg);
        config.options().header(MessagesUtil.getMessage("commit.header"));
        for (Field field : ConfigDefault.class.getDeclaredFields()) {
            if (field != null) {
                try {
                    ConfigElement<?> element = (ConfigElement<?>) field.get(null);
                    if (element.value != null) {
                        config.addDefault(element.path, element.value);
                    }
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            config.options().copyDefaults(true);
            config.save(cfg);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static class ConfigDefault {
        public static final ConfigElement<Boolean> DEBUG = new ConfigElement<>("settings.debug", false);
        public static final ConfigElement<Boolean> SHOW_DEATH_CAUSE = new ConfigElement<>("settings.show-death-cause-on-death", false);
        public static final ConfigElement<Boolean> GALACTICRAFT_MOD_SUPPORT = new ConfigElement<>("settings.galacticraft-mod-support", false);
        public static final ConfigElement<Boolean> CLEAR_VANISHING_CURSE_ITEMS = new ConfigElement<>("settings.clear-vanishing-curse-items", true);
        public static final ConfigElement<Boolean> DROP_BINDING_CURSE_ITEMS = new ConfigElement<>("settings.drop-binding-curse-items", false);
        public static final ConfigElement<String> HUNGER_LEVEL = new ConfigElement<>("settings.default-hunger-level", "set,10");
        public static final ConfigElement<String> SATURATION_LEVEL = new ConfigElement<>("settings.default-saturation-level", "set,0,5");
        public static final ConfigElement<Boolean> COMPATIBILITY_MODE_INVENTORY = new ConfigElement<>("settings.compatibility-mode.inventory", false);
        public static final ConfigElement<Boolean> COMPATIBILITY_MODE_EXP = new ConfigElement<>("settings.compatibility-mode.exp", false);
        public static final ConfigElement<List<String>> RUN_COMMANDS_ON_DEATH_IF_DROPS = new ConfigElement<>("settings.run-commands-on-death-if-drops", Arrays.asList(
                "[OP]fly off"
        ));
        public static final ConfigElement<List<String>> RUN_RANDOM_COMMANDS_ON_DEATH_IF_DROPS = new ConfigElement<>("settings.run-random-commands-on-death-if-drops", Arrays.asList(
                "10|50|eco take %player% %random%;tell %player% You lost %random% coins!",
                "1|10|eco give %player% %random%;tell %player% You gained %random% coins!"
        ));
        public static final ConfigElement<List<String>> RUN_COMMANDS_ON_RESPAWN_IF_DROPS = new ConfigElement<>("settings.run-commands-on-respawn-if-drops", Arrays.asList(
                "15|90|effect %player% minecraft:nausea %random% 1"
        ));
        public static final ConfigElement<List<String>> RUN_RANDOM_COMMANDS_ON_RESPAWN_IF_DROPS = new ConfigElement<>("settings.run-random-commands-on-respawn-if-drops", Arrays.asList(
                "15|90|effect %player% minecraft:weakness %random% 1",
                "15|30|effect %player% minecraft:strength %random% 1",
                "15|30|effect %player% minecraft:resistance %random% 1"
        ));


        //Messages
        public static final ConfigElement<String> LOST_EXP = new ConfigElement<>("messages.lost-exp", MessagesUtil.getConfigValue("lost-exp"));
        public static final ConfigElement<String> CONFIG_RELOADED = new ConfigElement<>("messages.reloaded-config", MessagesUtil.getConfigValue("reloaded-config"));
        public static final ConfigElement<String> RECEIVED_ITEM = new ConfigElement<>("messages.received-item", MessagesUtil.getConfigValue("received-item"));
        public static final ConfigElement<String> RECEIVED_VIRTUAL_ITEM = new ConfigElement<>("messages.received-virtual-item", MessagesUtil.getConfigValue("received-virtual-item"));
        public static final ConfigElement<String> SET_VIRTUAL_ITEM = new ConfigElement<>("messages.set-virtual-item", MessagesUtil.getConfigValue("set-virtual-item"));
        public static final ConfigElement<String> TAKE_VIRTUAL_ITEM = new ConfigElement<>("messages.take-virtual-item", MessagesUtil.getConfigValue("take-virtual-item"));
        public static final ConfigElement<String> GIVE_VIRTUAL_ITEM = new ConfigElement<>("messages.give-virtual-item", MessagesUtil.getConfigValue("give-virtual-item"));
        public static final ConfigElement<String> MODIFY_VIRTUAL_ITEM = new ConfigElement<>("messages.modified-amount", MessagesUtil.getConfigValue("modified-amount"));
        public static final ConfigElement<String> VIRTUAL_ITEM_COUNT = new ConfigElement<>("messages.virtual-item-count", MessagesUtil.getConfigValue("virtual-item-count"));
        public static final ConfigElement<String> VIRTUAL_ITEM_COUNT_OTHERS = new ConfigElement<>("messages.virtual-item-count-others", MessagesUtil.getConfigValue("virtual-item-count-others"));
        public static final ConfigElement<String> VIRTUAL_ITEM_COUNT_FORMAT = new ConfigElement<>("messages.virtual-item-format", MessagesUtil.getConfigValue("virtual-item-format"));
        public static final ConfigElement<String> NO_PERMISSION = new ConfigElement<>("messages.no-permission", MessagesUtil.getConfigValue("no-permission"));
        public static final ConfigElement<String> PLAYER_ONLY = new ConfigElement<>("messages.player-only", MessagesUtil.getConfigValue("player-only"));
        public static final ConfigElement<String> NOT_A_NUMBER = new ConfigElement<>("messages.is-not-number", MessagesUtil.getConfigValue("is-not-number"));
        public static final ConfigElement<String> ITEM_NOT_EXISTS = new ConfigElement<>("messages.item-not-exist", MessagesUtil.getConfigValue("item-not-exist"));
        public static final ConfigElement<String> USAGE_GIVE = new ConfigElement<>("messages.give-usage", MessagesUtil.getConfigValue("give-usage"));
        public static final ConfigElement<String> USAGE_SET = new ConfigElement<>("messages.set-usage", MessagesUtil.getConfigValue("set-usage"));
        public static final ConfigElement<String> USAGE_TAKE = new ConfigElement<>("messages.take-usage", MessagesUtil.getConfigValue("take-usage"));
        public static final ConfigElement<String> USAGE_GET = new ConfigElement<>("messages.get-usage", MessagesUtil.getConfigValue("get-usage"));
        public static final ConfigElement<String> PLAYER_NOT_FOUND = new ConfigElement<>("messages.player-not-found", MessagesUtil.getConfigValue("player-not-found"));
        public static final ConfigElement<String> GIVE_ITEM = new ConfigElement<>("messages.gave-item", MessagesUtil.getConfigValue("gave-item"));
        public static final ConfigElement<String> INVALID_ITEM = new ConfigElement<>("messages.invalid-item", MessagesUtil.getConfigValue("invalid-item"));
        public static final ConfigElement<List<String>> HELP_CONTENT = new ConfigElement<>("messages.help-msg", MessagesUtil.getConfigArrayValue("config.help-msg"));
    }

}
