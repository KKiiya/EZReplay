package me.lagggpixel.replay.replay.recordables.entity.projectile;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerDestroyEntities;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityVelocity;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerSpawnEntity;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

import java.util.Optional;
import java.util.UUID;

public class ProjectileLaunchRecordable extends Recordable {

    @Writeable private final EntityType type;
    @Writeable private final short shooterId;
    @Writeable private final short entityId;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d position;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d velocity;
    @Writeable private final float yaw;
    @Writeable private final float pitch;

    public ProjectileLaunchRecordable(IRecording replay, Entity shooter, Projectile projectile) {
        super(replay);
        EntityIndex index = replay.getEntityIndex();
        this.entityId = index.getOrRegister(projectile.getUniqueId());
        this.shooterId = index.getOrRegister(shooter.getUniqueId());
        
        Location loc = projectile.getLocation();
        this.position = new me.lagggpixel.replay.api.utils.Vector3d(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.velocity = me.lagggpixel.replay.api.utils.Vector3d.fromBukkitVector(projectile.getVelocity());
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.type = projectile.getType();
    }

    @Override
    public void play(IReplaySession replaySession) {
        Entity shooter = replaySession.getSpawnedEntities().get(this.shooterId);
        if (shooter == null) return;

        int fakeEntityId = entityId + 100000;
        Vector3d pePosition = new Vector3d(position.getX(), position.getY(), position.getZ());
        Vector3d peVelocity = new Vector3d(velocity.getX(), velocity.getY(), velocity.getZ());
        
        com.github.retrooper.packetevents.protocol.entity.type.EntityType peType = convertEntityType(type);
        WrapperPlayServerSpawnEntity spawnPacket = new WrapperPlayServerSpawnEntity(fakeEntityId, Optional.of(UUID.randomUUID()), peType, pePosition, pitch, yaw, yaw, 0, Optional.of(peVelocity));
        WrapperPlayServerEntityVelocity velocityPacket = new WrapperPlayServerEntityVelocity(fakeEntityId, peVelocity);

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(spawnPacket);
            user.sendPacket(velocityPacket);
        }
    }

    @Override
    public void unplay(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000;

        WrapperPlayServerDestroyEntities destroyPacket = new WrapperPlayServerDestroyEntities(fakeEntityId);
        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);
            user.sendPacket(destroyPacket);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.PROJECTILE_LAUNCH;
    }

    private com.github.retrooper.packetevents.protocol.entity.type.EntityType convertEntityType(EntityType bukkitType) {
        try {
            return EntityTypes.getByName("minecraft:" + bukkitType.name().toLowerCase());
        } catch (Exception e) {
            return EntityTypes.ARROW; // Fallback
        }
    }
}