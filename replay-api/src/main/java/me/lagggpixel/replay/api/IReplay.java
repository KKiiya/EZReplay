package me.lagggpixel.replay.api;

import me.lagggpixel.replay.api.replay.IReplayManager;
import org.bukkit.plugin.Plugin;

public interface IReplay extends Plugin {

    IReplayManager getReplayManager();

    /**
     * Returns whether the server is legacy (1.12 and below) or not.
     * @return true if the server is legacy, false otherwise.
     */
    boolean isLegacyServer();
}
