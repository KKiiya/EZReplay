package me.lagggpixel.replay.replay.recordables.world.block;

import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerAcknowledgePlayerDigging;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3i;
import org.bukkit.entity.Player;

public class BlockDigRecordable extends Recordable {
    @Writeable private final Vector3i position;
    @Writeable private final short playerId;
    @Writeable private final int stage;

    public BlockDigRecordable(IRecording replay, Player player, Vector3i pos, int stage) {
        super(replay);
        this.position = pos;
        this.playerId = replay.getEntityIndex().getOrRegister(player.getUniqueId());
        this.stage = stage;
    }

    @Override
    public void play(IReplaySession replaySession) {
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.BLOCK_DIG;
    }
}
