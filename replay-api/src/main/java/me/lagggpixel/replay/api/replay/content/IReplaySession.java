package me.lagggpixel.replay.api.replay.content;

import com.tomkeuper.bedwars.api.hologram.containers.IHologram;
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

    String getPrefix(String UUID);
    String getSuffix(String UUID);
    String getLevelName(String UUID);

    List<BukkitRunnable> startedTasks();
    List<IHologram> createdHolograms();

    void start();
    void pause();
    void resume();
    boolean isPaused();
    void rewind(int seconds);
    void fastForward(int seconds);
    int getSpeed();
    float getSpeedAsFloat();
    void setSpeed(int multiplier);
    void resetSpeed();
    void end();
}
