package me.lagggpixel.replay.replay.recordables.entity.entity;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import com.github.retrooper.packetevents.protocol.entity.data.EntityDataTypes;
import com.github.retrooper.packetevents.protocol.entity.type.EntityTypes;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.util.Vector3d;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.play.server.*;
import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import me.lagggpixel.replay.utils.SerializerUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class EntitySpawn extends Recordable {

    @Writeable private final EntityType entityType;
    @Writeable private final String customName;
    @Writeable private final boolean isCustomNameVisible;
    @Writeable private final me.lagggpixel.replay.api.utils.Vector3d spawnLocation;
    @Writeable private final double motX;
    @Writeable private final double motY;
    @Writeable private final double motZ;
    @Writeable private final short entityId;
    @Writeable private final boolean isLiving;

    public EntitySpawn(ReplayByteBuffer reader) {
        super(null);
        this.entityType = EntityType.values()[reader.read(INT)];
        this.customName = reader.read(STRING);
        this.isCustomNameVisible = reader.read(BOOLEAN);
        this.spawnLocation = SerializerUtil.read3DVector(reader);
        this.motX = reader.read(DOUBLE);
        this.motY = reader.read(DOUBLE);
        this.motZ = reader.read(DOUBLE);
        this.entityId = reader.read(SHORT);
        this.isLiving = reader.read(BOOLEAN);
    }

    public EntitySpawn(IRecording replay, Entity entity) {
        super(replay);
        this.spawnLocation = me.lagggpixel.replay.api.utils.Vector3d.fromBukkitLocation(entity.getLocation());
        this.customName = entity.getCustomName();
        this.isCustomNameVisible = entity.isCustomNameVisible();
        this.entityType = entity.getType();
        this.entityId = replay.getEntityIndex().getOrRegister(entity.getUniqueId());
        this.motX = entity.getVelocity().getX();
        this.motY = entity.getVelocity().getY();
        this.motZ = entity.getVelocity().getZ();
        this.isLiving = entity instanceof LivingEntity;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(INT, entityType.ordinal());
        writer.write(STRING, customName);
        writer.write(BOOLEAN, isCustomNameVisible);
        SerializerUtil.write3DVector(writer, spawnLocation);
        writer.write(DOUBLE, motX);
        writer.write(DOUBLE, motY);
        writer.write(DOUBLE, motZ);
        writer.write(SHORT, entityId);
        writer.write(BOOLEAN, isLiving);
    }

    @Override
    public void play(IReplaySession replaySession) {
        int fakeEntityId = entityId + 100000; // Use a fake entity ID for packet purposes

        Vector3d position = new Vector3d(spawnLocation.getX(), spawnLocation.getY(), spawnLocation.getZ());
        com.github.retrooper.packetevents.protocol.entity.type.EntityType peEntityType = convertEntityType(entityType);

        // Prepare entity metadata for custom name if present
        List<EntityData<?>> metadata = new ArrayList<>();
        if (customName != null) {
            metadata.add(new EntityData<>(2, EntityDataTypes.OPTIONAL_COMPONENT, Optional.of(customName)));
            metadata.add(new EntityData<>(3, EntityDataTypes.BOOLEAN, isCustomNameVisible));
        }

        PacketWrapper<?> spawnPacket;
        if (isLiving) spawnPacket = new WrapperPlayServerSpawnLivingEntity(fakeEntityId, java.util.UUID.randomUUID(), peEntityType, position, spawnLocation.getYaw(), spawnLocation.getPitch(), spawnLocation.getYaw(), new Vector3d(motX, motY, motZ), metadata);
        else spawnPacket = new WrapperPlayServerSpawnEntity(fakeEntityId, Optional.of(java.util.UUID.randomUUID()), peEntityType, position, spawnLocation.getPitch(), spawnLocation.getYaw(), spawnLocation.getYaw(), 0, Optional.of(new Vector3d(motX, motY, motZ)));

        WrapperPlayServerEntityRotation rotationPacket = new WrapperPlayServerEntityRotation(fakeEntityId, spawnLocation.getYaw(), spawnLocation.getPitch(), true);
        WrapperPlayServerEntityVelocity velocityPacket = new WrapperPlayServerEntityVelocity(fakeEntityId, new Vector3d(motX, motY, motZ));

        for (Player viewer : replaySession.getViewers()) {
            User user = PacketEvents.getAPI().getPlayerManager().getUser(viewer);

            user.sendPacket(spawnPacket);
            user.sendPacket(rotationPacket);
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
    
    private com.github.retrooper.packetevents.protocol.entity.type.EntityType convertEntityType(EntityType bukkitType) {
        // Convert Bukkit EntityType to PacketEvents EntityType
        try {
            return EntityTypes.getByName("minecraft:" + bukkitType.name().toLowerCase());
        } catch (Exception e) {
            return EntityTypes.PIG; // Fallback
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_SPAWN;
    }

}
