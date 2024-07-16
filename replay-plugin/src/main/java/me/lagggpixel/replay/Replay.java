package me.lagggpixel.replay;

import com.tomkeuper.bedwars.api.BedWars;
import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.IReplaySessionManager;
import me.lagggpixel.replay.api.support.IVersionSupport;
import me.lagggpixel.replay.commands.ReplayMenu;
import me.lagggpixel.replay.listeners.InventoryListener;
import me.lagggpixel.replay.listeners.arena.ArenaLeave;
import me.lagggpixel.replay.listeners.arena.ChatListener;
import me.lagggpixel.replay.listeners.arena.GameStateChangeListener;
import me.lagggpixel.replay.listeners.arena.SpecialItemsListener;
import me.lagggpixel.replay.listeners.recordables.*;
import me.lagggpixel.replay.listeners.replaysession.SessionListener;
import me.lagggpixel.replay.replay.ReplayManager;
import me.lagggpixel.replay.replay.ReplaySessionManager;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import me.lagggpixel.replay.utils.LogUtil;
import org.bukkit.Bukkit;
import org.bukkit.event.Listener;
import org.bukkit.plugin.RegisteredListener;
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
    @Getter
    private IReplaySessionManager replaySessionManager;

    @Override
    public void onEnable() {
        instance = this;
        if (!Bukkit.getPluginManager().isPluginEnabled("BedWars2023")) {
            LogUtil.warn("BedWars2023 was not found! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        bedWars = Objects.requireNonNull(Bukkit.getServicesManager().getRegistration(BedWars.class)).getProvider();

        VERSION = Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
        loadVersionSupport();
        replayManager = ReplayManager.init();
        replaySessionManager = ReplaySessionManager.init();

        registerListener(
                new InventoryListener(),
                new EntityListener(),
                new BlockListener(),
                new PlayerListener(),
                new ArenaLeave(),
                new ChatListener(),
                new GameStateChangeListener(),
                new ChatListener(),
                new SpecialItemsListener(),
                new SessionListener());

        if (versionSupport.getVersion() <= 2) registerListener(new ItemListener.LegacyDropPick());
        else registerListener(new ItemListener.NewDropPick());

        Objects.requireNonNull(getCommand("replay")).setExecutor(new ReplayMenu());
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

    private void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }
}
