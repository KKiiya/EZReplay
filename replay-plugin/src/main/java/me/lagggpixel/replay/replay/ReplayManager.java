package me.lagggpixel.replay.replay;

import com.tomkeuper.bedwars.api.arena.IArena;
import lombok.Getter;
import me.lagggpixel.replay.api.replay.IReplayManager;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.replay.data.Recording;

import javax.annotation.Nullable;
import java.io.File;
import java.util.HashMap;
import java.util.UUID;

public class ReplayManager implements IReplayManager {
    @Getter
    private static IReplayManager instance;
    private final HashMap<String, IRecording> replays;
    private final HashMap<IArena, IRecording> activeReplays;

    public ReplayManager() {
        this.replays = new HashMap<>();
        this.activeReplays = new HashMap<>();
        loadReplays();
    }

    public static IReplayManager init() {
        if (instance != null) throw new IllegalStateException("ReplayManager is already initialized");

        instance = new ReplayManager();
        // Load replays from file
        return instance;
    }

    public File saveReplay(IRecording replay) {
        replays.put(replay.getID().toString(), replay);
        return replay.toFile();
    }

    public void loadReplay(UUID id) {
        // Load replay from file

    }

    public void loadReplay(String id) {
        // Load replay from file

    }

    @Nullable
    public IRecording getReplayByID(String uuid) {
        return replays.get(uuid);
    }

    @Nullable
    @Override
    public IRecording getReplayByID(UUID uuid) {
        return replays.get(uuid.toString());
    }

    @Nullable
    public IRecording getActiveReplay(IArena a) {
        return activeReplays.get(a);
    }

    @Override
    public void removeFromActiveReplays(IArena a) {
        activeReplays.remove(a);
    }

    @Override
    public void startRecording(IArena a) {
        if (activeReplays.containsKey(a)) return;
        IRecording recording = new Recording(a);
        activeReplays.put(a, recording);
        recording.start();
    }

    @Override
    public void pauseRecording(IArena a) {
        if (!activeReplays.containsKey(a)) return;
        IRecording recording = activeReplays.get(a);
        recording.pause();
    }

    @Override
    public void stopRecording(IArena a) {
        if (!activeReplays.containsKey(a)) return;
        IRecording recording = activeReplays.get(a);
        recording.stop();
        activeReplays.remove(a);
        replays.put(recording.getID().toString(), recording);
    }

    private void loadReplays() {
        // Load all replays from file

    }
}
