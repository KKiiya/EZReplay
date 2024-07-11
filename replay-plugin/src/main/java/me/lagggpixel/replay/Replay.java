package me.lagggpixel.replay;

import com.tomkeuper.bedwars.api.BedWars;
import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.replay.ReplayManager;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import me.lagggpixel.replay.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;

public final class Replay extends JavaPlugin implements IReplay {
    @Getter
    private static Replay instance;
    private static String VERSION;
    @Getter
    private IVersionSupport versionSupport;
    private static BedWars bedWars;
    @Getter
    private IReplayManager replayManager;

    @Override
    public void onEnable() {
        instance = this;
        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars2023")) {
            LogUtil.warn("BedWars2023 was not found! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        bedWars = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(BedWars.class)).getProvider();

        VERSION = Bukkit.getServer().getClass().getSimpleName().split("//.")[3];
        loadVersionSupport();
        replayManager = ReplayManager.init();
    }

    @Override
    public void onDisable() {

    }

    public BedWars getBedWarsAPI() {
        return bedWars;
    }

    private void loadVersionSupport() {
        switch (VERSION) {
            case "v1_8_R3":
                versionSupport = new v1_8_R3(this);
                break;
        }
    }
}
