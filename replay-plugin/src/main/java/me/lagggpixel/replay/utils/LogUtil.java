package me.lagggpixel.replay.utils;

import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;

public class LogUtil {

    public static void info(String log) {
        Bukkit.getConsoleSender().sendMessage(Utils.c("[" + Replay.getInstance().getName() + "] " + log));
    }

    public static void warn(String warning) {
        Replay.getInstance().getLogger().warning(Utils.c((warning)));
    }

    public static void error(String error) {
        Replay.getInstance().getLogger().severe(Utils.c(error));
    }
}
