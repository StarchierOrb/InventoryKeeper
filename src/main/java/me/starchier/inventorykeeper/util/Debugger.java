package me.starchier.inventorykeeper.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Debugger {
    public static boolean enabledDebug = false;

    public static void logDebugMessage(String message) {
        if (!enabledDebug) {
            return;
        }
        Bukkit.getLogger().info("[INVKEEPER-DEBUG] " + ChatColor.AQUA + message);
    }
}
