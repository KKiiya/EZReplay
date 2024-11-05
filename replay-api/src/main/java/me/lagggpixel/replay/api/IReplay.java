package me.lagggpixel.replay.api;

import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.support.IVersionSupport;
import org.bukkit.plugin.Plugin;

public interface IReplay extends Plugin {

    IReplayManager getReplayManager();

    IVersionSupport getVersionSupport();
}
