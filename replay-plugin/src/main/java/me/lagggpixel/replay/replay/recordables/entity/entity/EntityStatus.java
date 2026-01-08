package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class EntityStatus extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final boolean isDead;
    @Writeable private final boolean isLiving;

    public EntityStatus(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.isDead = reader.read(BOOLEAN);
        this.isLiving = reader.read(BOOLEAN);
    }

    public EntityStatus(IRecording replay, Entity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.isDead = entity.isDead();
        this.isLiving = entity instanceof LivingEntity;
    }

    @Override
    public void play(IReplaySession replaySession) {
        
        if (!isLiving && isDead) {
            int fakeEntityId = entityId + 100000;

            WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(fakeEntityId);
            for (Player viewer : replaySession.getViewers()) {
                User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
                user.sendPacket(destroyPacket);
            }
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_STATUS;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(BOOLEAN, isDead);
        writer.write(BOOLEAN, isLiving);
    }
}
