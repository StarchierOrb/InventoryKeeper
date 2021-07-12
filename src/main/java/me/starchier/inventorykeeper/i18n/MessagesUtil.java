package me.starchier.inventorykeeper.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

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
        } catch (NullPointerException e) {
            return key;
        }
    }

    public static String getConfigHeader(String key) {
        try {
            return rb.getString("header." + key);
        } catch (NullPointerException e) {
            return "header." + key;
        }
    }
}
