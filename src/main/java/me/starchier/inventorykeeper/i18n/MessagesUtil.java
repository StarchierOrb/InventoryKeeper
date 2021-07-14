package me.starchier.inventorykeeper.i18n;

import java.util.*;

public class MessagesUtil {
    private static ResourceBundle rb;

    public static void initMessageBundle() {
        rb = ResourceBundle.getBundle("locales/messages", Locale.getDefault());
    }

    public static void setLocale(String locale) {
        Locale lang;
        try {
            lang = Locale.forLanguageTag(locale.replace("_", "-"));
        } catch (NullPointerException e) {
            lang = Locale.getDefault();
        }
        rb = ResourceBundle.getBundle("locales/messages", lang);
    }

    public static String getMessage(String key) {
        try {
            return rb.getString(key);
        } catch (MissingResourceException | NullPointerException e) {
            return key;
        }
    }

    public static String[] getConfigCommit(String key) {
        try {
            return rb.getString("commit." + key).split("\n");
        } catch (MissingResourceException | NullPointerException e) {
            return new String[]{"commit." + key};
        }
    }

    public static String getConfigValue(String key) {
        try {
            return rb.getString("config." + key);
        } catch (MissingResourceException | NullPointerException e) {
            return "config." + key;
        }
    }

    public static List<String> getConfigArrayValue(String key) {
        try {
            return Arrays.asList(rb.getString(key).split("\n"));
        } catch (MissingResourceException | NullPointerException e) {
            return Collections.singletonList(key);
        }
    }
}
