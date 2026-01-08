package me.lagggpixel.replay.replay;

import lombok.Getter;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.replay.data.Recording;
import me.lagggpixel.replay.utils.LogUtil;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ReplayManager implements IReplayManager {
    @Getter
    private static IReplayManager instance;
    private final List<IRecording> replays;
    private final HashMap<String, IRecording> replayById;
    private final HashMap<World, IRecording> activeRecordings;

    private ReplayManager() {
        this.replays = new ArrayList<>();
        this.replayById = new HashMap<>();
        this.activeRecordings = new HashMap<>();
        loadReplays();
    }

    public static IReplayManager init() {
        if (instance != null) throw new IllegalStateException("ReplayManager is already initialized");
        instance = new ReplayManager();
        // Load replays from file
        return instance;
    }

    @Override
    public File saveReplay(IRecording replay) {
        replayById.put(replay.getID().toString(), replay);
        return replay.toFile();
    }

    public void cacheReplay(IRecording replay) {
        replayById.put(replay.getID().toString(), replay);
    }

    public IRecording loadReplay(UUID id) {
        File folder = new File(Replay.getInstance().getDataFolder(), "recordings");
        File file = new File(folder, id + ".replay");
        if (!file.exists()) {
            throw new IllegalArgumentException("Replay file not found: " + file.getAbsolutePath());
        }
        IRecording recording = Replay.getInstance().getRecordingFileProcessor().loadRecording(file);
        if (recording == null) return null;
        replayById.put(recording.getID().toString(), recording);
        return recording;
    }

    public IRecording loadReplay(String id) {
        return loadReplay(UUID.fromString(id));
    }

    @Override
    public List<IRecording> getReplays() {
        return replays;
    }

    @Nullable
    public IRecording getReplayByID(String uuid) {
        return replayById.get(uuid);
    }

    @Nullable
    @Override
    public IRecording getReplayByID(UUID uuid) {
        return replayById.get(uuid.toString());
    }

    @Nullable
    public IRecording getActiveRecording(World world) {
        return activeRecordings.get(world);
    }

    @Override
    public void removeFromActiveRecordings(World world) {
        activeRecordings.remove(world);
    }

    @Override
    public IRecording startRecording(World world) {
        if (activeRecordings.containsKey(world)) return null;
        IRecording recording = new Recording(world);
        activeRecordings.put(world, recording);
        recording.start();
        return recording;
    }

    @Override
    public IRecording pauseRecording(World world) {
        if (!activeRecordings.containsKey(world)) return null;
        IRecording recording = activeRecordings.get(world);
        recording.pause();
        return recording;
    }

    @Override
    public IRecording stopRecording(World world) {
        if (!activeRecordings.containsKey(world)) return null;
        IRecording recording = activeRecordings.get(world);
        recording.stop();
        replayById.put(recording.getID().toString(), recording);
        LogUtil.info(replayById.toString());
        activeRecordings.remove(world);
        return recording;
    }

    private void loadReplays() {
        // Load all replays from file

    }
}
