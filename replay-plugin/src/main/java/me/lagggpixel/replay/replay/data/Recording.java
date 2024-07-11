package me.lagggpixel.replay.replay.data;

import com.tomkeuper.bedwars.api.arena.IArena;
import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IFrame;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.replay.tasks.EquipmentTrackerTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Recording implements IRecording {
    @Getter
    public final UUID id;
    @Getter
    public final IArena arena;

    private final List<IFrame> frames;
    private int frameGeneratorTaskId = -1;
    private int equipmentTrackerTaskId = -1;
    private boolean isRecording = false;
    private boolean finished = false;

    public Recording(IArena arena) {
        this.frames = new ArrayList<>();
        this.id = UUID.randomUUID();
        this.arena = arena;
    }

    @Override
    public UUID getID() {
        return id;
    }


    @Override
    public void add(IFrame... frames) {
        this.frames.addAll(List.of(frames));
    }

    @Override
    public void add(List<IFrame> frames) {
        this.frames.addAll(frames);
    }

    @Override
    public IFrame getFrame(int tick) {
        return frames.get(tick);
    }

    @Override
    public IFrame getLastFrame() {
        return frames.get(frames.size() - 1);
    }

    @Override
    public List<IFrame> getFrames() {
        return Collections.unmodifiableList(frames);
    }

    @Override
    public File toFile() {
        return null;
    }

    @Override
    public List<Entity> getSpawnedEntities() {
        return List.of();
    }

    @Override
    public void start() {
        isRecording = true;
        if (finished) {
            throw new UnsupportedOperationException("Tried resuming replay with ID '" + getId().toString() + "' while finished");
        }
        frameGeneratorTaskId = Bukkit.getScheduler().runTaskTimer(Replay.getInstance(), () -> {
            frames.add(new Frame(this));
            for (Player player : arena.getPlayers()) {
                getLastFrame().addRecordable(Replay.getInstance().getVersionSupport().createPlayerStatusRecordable(this, player));
            }

            for (Entity entity : getSpawnedEntities()) {
                getLastFrame().addRecordable(Replay.getInstance().getVersionSupport().createEntityStatusRecordable(this, entity));
            }
        }, 0, 1L).getTaskId();
        equipmentTrackerTaskId = Bukkit.getScheduler().runTaskTimer(Replay.getInstance(), new EquipmentTrackerTask(this), 0, 1L).getTaskId();
    }

    @Override
    public void pause() {
        isRecording = false;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
        equipmentTrackerTaskId = -1;
    }

    @Override
    public void stop() {
        isRecording = false;
        finished = true;
        Bukkit.getScheduler().cancelTask(frameGeneratorTaskId);
        Bukkit.getScheduler().cancelTask(equipmentTrackerTaskId);
        frameGeneratorTaskId = -1;
    }

    @Override
    public Entity getSpawnedEntity(int id) {
        return null;
    }

    @Override
    public boolean isRecording() {
        return isRecording;
    }

    @Override
    public boolean isFinished() {
        return isFinished();
    }
}
