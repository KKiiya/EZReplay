package me.lagggpixel.replay.api.utils;

public class NMSVersion {
    private static final String VERSION;

    static {
        // Detect from the server package name
        String serverPackage = org.bukkit.Bukkit.getServer().getClass().getPackage().getName();
        VERSION = serverPackage.substring(serverPackage.lastIndexOf('.') + 1);
    }

    public static String getVersion() {
        return VERSION;
    }

    private NMSVersion() {}
}
