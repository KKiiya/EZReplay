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
import java.util.UUID;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.BOOLEAN;
import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.SHORT;

public class Sprinting extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final boolean isSprinting;

    public Sprinting(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.isSprinting = reader.read(BOOLEAN);
    }

    public Sprinting(IRecording replay, UUID player, boolean isSprinting) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(player);
        this.isSprinting = isSprinting;
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        byte flags = isSprinting ? (byte) 0x08 : (byte) 0x00;
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(fakeEntityId, metadata);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(metadataPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        byte flags = !isSprinting ? (byte) 0x08 : (byte) 0x00;
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(fakeEntityId, metadata);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(metadataPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.SPRINTING;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(BOOLEAN, isSprinting);
    }
}
