package me.lagggpixel.replay;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import lombok.Getter;
import me.lagggpixel.replay.api.IReplay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.IReplaySessionManager;
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
import me.lagggpixel.replay.utils.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import java.util.Objects;

public final class Replay extends JavaPlugin implements IReplay {

    @Getter
    private static Replay instance;
    private static String VERSION;
    private boolean isLegacyServer;
    @Getter
    private IReplayManager replayManager;
    @Getter
    private IReplaySessionManager replaySessionManager;

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));

        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable() {
        PacketEvents.getAPI().init();
        instance = this;

        VERSION = Bukkit.getBukkitVersion().split("-")[0];
        isLegacyServer = VERSION.equals("1.8.8") || VERSION.equalsIgnoreCase("1.12.2");
        replayManager = ReplayManager.init();
        replaySessionManager = ReplaySessionManager.init();

        registerListener(
                new InventoryListener(),
                new EntityListener(),
                new BlockListener(),
                new PlayerListener(),
                new ChatListener(),
                new SessionListener());

        if (isLegacyServer()) registerListener(new ItemListener.LegacyDropPick());
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
        PacketEvents.getAPI().terminate();
    }

    private void registerListener(Listener... listeners) {
        for (Listener listener : listeners) {
            Bukkit.getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public boolean isLegacyServer() {
        return isLegacyServer;
    }
}
