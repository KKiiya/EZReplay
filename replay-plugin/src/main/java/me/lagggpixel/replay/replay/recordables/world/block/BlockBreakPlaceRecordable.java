package me.lagggpixel.replay.replay.recordables.world.block;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;

public class BlockBreakPlaceRecordable extends Recordable {

    public BlockBreakPlaceRecordable(IRecording replay) {
        super(replay);
    }

    @Override
    public void play(IReplaySession replaySession) {

    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return -1;
    }
}
