package me.lagggpixel.replay.replay.recordables.entity.player.status;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityMetadata;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.BYTE;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class SwordBlock extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final byte value;

    public SwordBlock(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.value = reader.read(BYTE);
    }

    public SwordBlock(IRecording replay, Player playerBlocking) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(playerBlocking.getUniqueId());
        this.value = (byte) (playerBlocking.isBlocking() ? 0x10 : 0x00);
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, value));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(fakeEntityId, metadata);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(metadataPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.SWORD_BLOCK;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(BYTE, value);
    }
}
