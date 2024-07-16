package me.lagggpixel.replay.utils;

import org.bukkit.ChatColor;

public class Utils {

    public static String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }
}
