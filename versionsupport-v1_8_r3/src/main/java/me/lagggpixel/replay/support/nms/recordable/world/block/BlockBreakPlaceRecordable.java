package me.lagggpixel.replay.support.nms.recordable.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.Player;

public class BlockBreakPlaceRecordable extends Recordable {

    public BlockBreakPlaceRecordable(IRecording replay) {
        super(replay);
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {

    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public short getTypeId() {
        return -1;
    }
}
