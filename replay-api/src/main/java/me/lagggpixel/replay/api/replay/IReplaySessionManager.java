package me.lagggpixel.replay.api.replay;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.entity.Player;

public interface IReplaySessionManager {

    void setReplaySessionByPlayer(Player player, IReplaySession replaySession);

    IReplaySession getSessionByPlayer(Player player);
}
