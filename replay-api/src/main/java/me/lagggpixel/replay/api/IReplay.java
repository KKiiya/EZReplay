package me.lagggpixel.replay.api;

import com.tomkeuper.bedwars.api.BedWars;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.support.IVersionSupport;
import org.bukkit.plugin.Plugin;

public interface IReplay extends Plugin {
    BedWars getBedWarsAPI();

    IReplayManager getReplayManager();

    IVersionSupport getVersionSupport();
}
