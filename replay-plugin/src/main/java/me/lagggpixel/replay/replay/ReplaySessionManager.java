package me.lagggpixel.replay.replay;

import lombok.Getter;
import me.lagggpixel.replay.api.replay.IReplaySessionManager;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class ReplaySessionManager implements IReplaySessionManager {
    @Getter
    private static IReplaySessionManager instance;
    private final HashMap<Player, IReplaySession> replaySessionByPlayer;

    private ReplaySessionManager() {
        this.replaySessionByPlayer = new HashMap<>();
    }

    public static IReplaySessionManager init() {
        instance = new ReplaySessionManager();
        return instance;
    }

    @Override
    public void setReplaySessionByPlayer(Player player, IReplaySession replaySession) {
        replaySessionByPlayer.put(player, replaySession);
    }

    @Override
    public IReplaySession getSessionByPlayer(Player player) {
        return replaySessionByPlayer.get(player);
    }
}
