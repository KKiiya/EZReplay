package me.lagggpixel.replay.replay.recordables.entity.status;

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
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Burning extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final int fireTicks;

    public Burning(IRecording replay, Entity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.fireTicks = entity.getFireTicks();
    }

    @Override
    public void play(IReplaySession replaySession) {
        Entity fakeEntity = replaySession.getSpawnedEntities().get(entityId);
        if (fakeEntity != null) fakeEntity.setFireTicks(fireTicks);
        
        int fakeEntityId = entityId + 100000;

        List<EntityData<?>> metadata = new ArrayList<>();
        byte flags = fireTicks > 0 ? (byte) 0x01 : (byte) 0x00; // 0x01 is on fire flag
        metadata.add(new EntityData<>(0, EntityDataTypes.BYTE, flags));
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
        return RecordableRegistry.BURNING;
    }
}
