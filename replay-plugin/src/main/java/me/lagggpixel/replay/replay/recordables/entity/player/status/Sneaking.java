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

public class Sneaking extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final boolean isSneaking;

    public Sneaking(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.isSneaking = reader.read(BOOLEAN);
    }

    public Sneaking(IRecording replay, UUID player, boolean isSneaking) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(player);
        this.isSneaking = isSneaking;
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        byte flags = isSneaking ? (byte) 0x02 : (byte) 0x00;
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
        byte flags = !isSneaking ? (byte) 0x02 : (byte) 0x00;
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(fakeEntityId, metadata);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(metadataPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.SNEAKING;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(BOOLEAN, isSneaking);
    }
}
