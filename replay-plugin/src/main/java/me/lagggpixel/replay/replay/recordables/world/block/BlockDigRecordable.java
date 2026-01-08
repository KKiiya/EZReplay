package me.lagggpixel.replay.replay.recordables.world.block;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.api.utils.Vector3i;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class BlockDigRecordable extends Recordable {
    @Writeable private final Vector3i position;
    @Writeable private final short playerId;
    @Writeable private final int stage;

    public BlockDigRecordable(ReplayByteBuffer reader) {
        super(null);
        this.position = new Vector3i(reader.read(INT), reader.read(INT), reader.read(INT));
        this.playerId = reader.read(SHORT);
        this.stage = reader.read(INT);
    }

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

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(INT, position.getX());
        writer.write(INT, position.getY());
        writer.write(INT, position.getZ());
        writer.write(SHORT, playerId);
        writer.write(INT, stage);
    }
}
