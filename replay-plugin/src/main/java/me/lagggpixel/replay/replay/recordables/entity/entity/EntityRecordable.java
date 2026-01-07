package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityRotation;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityTeleport;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

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
        Entity replayEntity = replaySession.getSpawnedEntities().get(entityId);
        if (replayEntity != null) replayEntity.teleport(new Location(replaySession.getWorld(), x, y, z, yaw, pitch));
        int fakeEntityId = entityId + 100000;

        WrapperPlayServerEntityTeleport teleportPacket = new WrapperPlayServerEntityTeleport(fakeEntityId, new Vector3d(x, y, z), yaw, pitch, true);
        WrapperPlayServerEntityRotation rotationPacket = new WrapperPlayServerEntityRotation(fakeEntityId, yaw, pitch, true);
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
}
