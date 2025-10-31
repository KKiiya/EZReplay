package me.lagggpixel.replay;

import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.IReplaySessionManager;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.commands.ReplayMenu;
import me.lagggpixel.replay.commands.Startrecording;
import me.lagggpixel.replay.commands.Stoprecording;
import me.lagggpixel.replay.listeners.InventoryListener;
import me.lagggpixel.replay.listeners.player.ChatListener;
import me.lagggpixel.replay.listeners.player.PlayerListener;
import me.lagggpixel.replay.listeners.world.*;
import me.lagggpixel.replay.listeners.replaysession.SessionListener;
import me.lagggpixel.replay.replay.ReplayManager;
import me.lagggpixel.replay.replay.ReplaySessionManager;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import me.lagggpixel.replay.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import com.tomkeuper.spigot.versionsupport.MaterialSupport;
import com.tomkeuper.spigot.versionsupport.MaterialSupport.SupportBuilder;

import java.util.Objects;

public final class Replay extends JavaPlugin implements IReplay {

    @Getter
    private static Replay instance;
    private static String VERSION;
    @Getter
    private IVersionSupport versionSupport;
    @Getter
    private IReplayManager replayManager;
    @Getter
    private IReplaySessionManager replaySessionManager;
    @Getter
    private MaterialSupport materialSupport;

    @Override
    public void onEnable() {
        instance = this;

        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        if (!loadVersionSupport()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        replayManager = ReplayManager.init();
        replaySessionManager = ReplaySessionManager.init();

        registerListener(
                new InventoryListener(),
                new EntityListener(),
                new BlockListener(),
                new PlayerListener(),
                new ChatListener(),
                new SessionListener());

        if (versionSupport.getVersion() <= 2) registerListener(new ItemListener.LegacyDropPick());
        else registerListener(new ItemListener.NewDropPick());

        for (World world : getServer().getWorlds()) {
            FileUtils.saveWorldToCache(world);
        }

        Objects.requireNonNull(getCommand("replay")).setExecutor(new ReplayMenu());
        Objects.requireNonNull(getCommand("startrecording")).setExecutor(new Startrecording());
        Objects.requireNonNull(getCommand("stoprecording")).setExecutor(new Stoprecording());
    }

    @Override
    public void onDisable() {

    }

    private boolean loadVersionSupport() {
        switch (VERSION) {
            case "v1_8_R3":
                versionSupport = new v1_8_R3(this);
                break;
            default:
                getLogger().severe("Your version " + VERSION + " is not supported, the plugin will not be loaded!");
        }
        if (versionSupport != null) {
            new MaterialSupport.SupportBuilder();
            materialSupport = SupportBuilder.load();
        }
        return versionSupport != null;
    }

    private void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
