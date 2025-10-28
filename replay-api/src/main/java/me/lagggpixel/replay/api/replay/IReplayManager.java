package me.lagggpixel.replay.api.replay;

import me.lagggpixel.replay.api.replay.data.IRecording;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
import java.util.UUID;

public interface IReplayManager {
    File saveReplay(IRecording replay);

    IRecording loadReplay(UUID id);

    IRecording loadReplay(String id);

    List<IRecording> getReplays();

    @Nullable
    IRecording getReplayByID(String uuid);

    @Nullable
    IRecording getReplayByID(UUID uuid);

    @Nullable
    IRecording getActiveRecording(World world);

    void removeFromActiveRecordings(World world);

    void startRecording(World world);

    void pauseRecording(World world);

    void stopRecording(World world);
}
