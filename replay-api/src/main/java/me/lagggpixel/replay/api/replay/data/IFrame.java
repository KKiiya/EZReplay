package me.lagggpixel.replay.api.replay.data;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.serialize.BinarySerializable;
import org.bukkit.entity.Player;

import java.util.List;

public interface IFrame extends BinarySerializable {
    List<Recordable> getRecordables();

    IRecording getReplay();

    void addRecordable(Recordable... recordables);

    void addAsList(List<Recordable> recordableList);

    void play(IReplaySession replaySession, Player player);

    void unplay(IReplaySession replaySession, Player player);
}
