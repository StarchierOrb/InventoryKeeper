package me.starchier.inventorykeeper.util;

import me.clip.placeholderapi.PlaceholderAPI;
import me.starchier.inventorykeeper.InventoryKeeper;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {
    private static final Pattern hexPattern = Pattern.compile("(#[a-fA-F0-9]{6})");

    public static String transform(String str) {
        try {
            Matcher matcher = hexPattern.matcher(str);
            while (matcher.find()) {
                String hexCode = str.substring(matcher.start(), matcher.end());
                str = str.replace(hexCode, ChatColor.of(hexCode).toString());
            }
        } catch (Exception ignored) {
        }
        return str;
    }

    public static String replacePlaceholder(String str, Player player) {
        if (InventoryKeeper.papiEnabled && PlaceholderAPI.containsPlaceholders(str)) {
            return PlaceholderAPI.setPlaceholders(player, str);
        }
        return str;
    }
}
