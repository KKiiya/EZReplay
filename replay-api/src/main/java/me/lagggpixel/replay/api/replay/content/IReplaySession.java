package me.lagggpixel.replay.api.replay.content;

import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IHologramAdd;
import me.lagggpixel.replay.api.utils.entity.ReplayEntity;
import me.lagggpixel.replay.api.utils.entity.player.ReplayPlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;

public interface IReplaySession {
    IRecording getReplay();
    List<Player> getViewers();

    ReplayEntity getFakeEntity(String UUID);
    ReplayEntity getFakeEntity(int entityId);
    void addFakeEntity(ReplayEntity entity);

    ReplayPlayer getFakePlayer(String UUID);

    World getWorld();

    List<BukkitRunnable> startedTasks();
    List<IHologramAdd> createdHolograms();

    void start();
    void pause();
    void rewind(int seconds);
    void fastForward(int seconds);
    void end();
}
