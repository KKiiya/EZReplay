package me.lagggpixel.replay.api.replay.content;

import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.List;

public interface IReplaySession {
    IRecording getReplay();
    List<Player> getViewers();
    HashMap<Player, IControls> getPlayerControls();

    World getWorld();
    HashMap<String, Entity> getSpawnedEntities();

    List<BukkitRunnable> startedTasks();

    long getCurrentTick();

    void start();
    void pause();
    void resume();
    boolean isPaused();
    void rewind(int seconds);
    void fastForward(int seconds);
    int getSpeed();
    double getSpeedAsDouble();
    void setSpeed(int multiplier);
    void resetSpeed();
    void reset();
    void end();
}
