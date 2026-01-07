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
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Invisible extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final boolean isInvisible;

    public Invisible(IRecording replay, UUID player, boolean isInvisible) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(player);
        this.isInvisible = isInvisible;
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        byte flags = isInvisible ? (byte) 0x20 : (byte) 0x00;
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
        byte flags = !isInvisible ? (byte) 0x20 : (byte) 0x00;
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
        WrapperPlayServerEntityMetadata metadataPacket = new WrapperPlayServerEntityMetadata(fakeEntityId, metadata);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(metadataPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.INVISIBLE;
    }
}
