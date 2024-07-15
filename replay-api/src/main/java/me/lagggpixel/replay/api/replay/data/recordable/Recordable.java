package me.lagggpixel.replay.api.replay.data.recordable;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.entity.Player;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public abstract class Recordable {
    private final IRecording replay;

    public Recordable(IRecording replay) {
        this.replay = replay;
    }

    public IRecording getRecording() {
        return replay;
    }

    public abstract void play(IReplaySession replaySession, Player player);
}
