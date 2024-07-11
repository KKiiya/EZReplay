package me.lagggpixel.replay.listeners.arena;

import com.tomkeuper.bedwars.api.arena.GameState;
import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.api.events.gameplay.GameEndEvent;
import com.tomkeuper.bedwars.api.events.gameplay.GameStateChangeEvent;
import com.tomkeuper.bedwars.api.events.player.PlayerLeaveArenaEvent;
import me.lagggpixel.replay.Replay;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class GameStateChangeListener implements Listener {

    @EventHandler
    public void onGameStart(GameStateChangeEvent e) {
        if (e.getNewState() != GameState.playing) return;
        IArena a = e.getArena();
        Replay.getInstance().getReplayManager().startRecording(a);
    }

    @EventHandler
    public void onGameEndEvent(GameEndEvent e) {
        IArena a = e.getArena();
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> Replay.getInstance().getReplayManager().startRecording(a), (long) 20*9);
    }

    @EventHandler
    public void onArenaLeave(PlayerLeaveArenaEvent e) {
        IArena a = e.getArena();
        if (a.getPlayers().size() > 2) return;
        Bukkit.getScheduler().runTaskLater(Replay.getInstance(), () -> Replay.getInstance().getReplayManager().startRecording(a), (long) 20*3);
    }
}
