package me.lagggpixel.replay.api.replay;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.api.replay.data.IRecording;

import javax.annotation.Nullable;
import java.io.File;
import java.util.List;
import java.util.UUID;

public interface IReplayManager {
    File saveReplay(IRecording replay);

    void loadReplay(UUID id);

    void loadReplay(String id);

    List<IRecording> getReplays();

    @Nullable
    IRecording getReplayByID(String uuid);

    @Nullable
    IRecording getReplayByID(UUID uuid);

    @Nullable
    IRecording getActiveReplay(IArena a);

    void removeFromActiveReplays(IArena a);

    void startRecording(IArena a);

    void pauseRecording(IArena a);

    void stopRecording(IArena a);
}
