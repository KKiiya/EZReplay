package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.content.RecEntity;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public class EntityRecordable extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final double x;
    @Writeable private final double y;
    @Writeable private final double z;
    @Writeable private final float yaw;
    @Writeable private final float pitch;

    public EntityRecordable (ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.x = reader.read(DOUBLE);
        this.y = reader.read(DOUBLE);
        this.z = reader.read(DOUBLE);
        this.yaw = reader.read(FLOAT);
        this.pitch = reader.read(FLOAT);
    }

    public EntityRecordable(IRecording replay, Entity entity) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        Location location = entity.getLocation();
        this.x = location.getX();
        this.y = location.getY();
        this.z = location.getZ();
        this.yaw = location.getYaw();
        this.pitch = location.getPitch();
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;
        RecEntity recEntity = replaySession.getSpawnedEntities().get(entityId);

        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(fakeEntityId, new Vector3d(x, y, z), yaw, pitch, true);
        WrapperPlayServerEntityRotation rotationPacket = new WrapperPlayServerEntityRotation(fakeEntityId, yaw, pitch, true);
        recEntity.setPosition(new me.lagggpixel.replay.api.utils.Vector3d(x, y, z, yaw, pitch));
        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(rotationPacket);
            user.sendPacket(teleportPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_RECORDABLE;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(DOUBLE, x);
        writer.write(DOUBLE, y);
        writer.write(DOUBLE, z);
        writer.write(FLOAT, yaw);
        writer.write(FLOAT, pitch);
    }
}
