package me.lagggpixel.replay.replay.recordables.entity.entity;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.api.utils.entity.EntityTypes;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class EntitySpawn extends Recordable {

    @Writeable private final EntityType entityType;
    @Writeable private final String customName;
    @Writeable private final boolean isCustomNameVisible;
    @Writeable private final Vector3d spawnLocation;
    @Writeable private final double motX;
    @Writeable private final double motY;
    @Writeable private final double motZ;
    @Writeable private final short entityId;
    @Writeable private final boolean isLiving;

    public EntitySpawn(IRecording replay, org.bukkit.entity.Entity entity) {
        super(replay);
        this.spawnLocation = Vector3d.fromBukkitLocation(entity.getLocation());
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
    public void play(IReplaySession replaySession, Player player) {
        CraftWorld world = ((CraftWorld) replaySession.getWorld());
        net.minecraft.server.v1_8_R3.Entity entity = world.createEntity(spawnLocation.toBukkitLocation(world), entityType.getEntityClass());
        if (customName != null) entity.setCustomName(customName);
        if (isCustomNameVisible && customName != null) entity.setCustomNameVisible(true);
        entity.motX = this.motX;
        entity.motY = this.motY;
        entity.motZ = this.motZ;
        entity.velocityChanged = true;

        if (isLiving) {
            EntityLiving livingEntity = (EntityLiving) entity;
            PacketPlayOutSpawnEntityLiving entitySpawn = new PacketPlayOutSpawnEntityLiving(livingEntity);
            v1_8_R3.sendPacket(player, entitySpawn);
        } else {
            PacketPlayOutSpawnEntity entitySpawn = new PacketPlayOutSpawnEntity(entity, EntityTypes.getId(entityType));
            v1_8_R3.sendPacket(player, entitySpawn);
        }

        replaySession.getSpawnedEntities().put(entityId, entity.getBukkitEntity());
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entity, (byte) ((spawnLocation.getYaw() * 256.0F) / 360.0F));
        PacketPlayOutEntity.PacketPlayOutEntityLook entityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((spawnLocation.getYaw() * 256.0F) / 360.0F), (byte) ((spawnLocation.getPitch() * 256.0F) / 360.0F), true);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(entity);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove movement = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entity.getId(), (byte) motX, (byte) motY, (byte) motZ, false);

        v1_8_R3.sendPackets(player, headRotation, entityLook, metadata, velocity, movement);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = ((CraftEntity)  replaySession.getSpawnedEntities().get(entityId)).getHandle();

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getId());

        v1_8_R3.sendPacket(player, destroy);
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.ENTITY_SPAWN;
    }
}
