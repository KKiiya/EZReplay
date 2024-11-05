package me.lagggpixel.replay.support.nms.recordable.entity.recordables;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.recordables.IEntitySpawn;
import me.lagggpixel.replay.api.utils.entity.EntityTypes;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import java.util.UUID;

public class EntitySpawn extends Recordable implements IEntitySpawn {

    private final EntityType entityType;
    private final String customName;
    private final boolean isCustomNameVisible;
    private final Location spawnLocation;
    private final double motX;
    private final double motY;
    private final double motZ;
    private final UUID uniqueId;
    private final boolean isLiving;

    public EntitySpawn(IRecording replay, org.bukkit.entity.Entity entity) {
        super(replay);
        this.spawnLocation = entity.getLocation();
        this.customName = entity.getCustomName();
        this.isCustomNameVisible = entity.isCustomNameVisible();
        this.entityType = entity.getType();
        this.uniqueId = entity.getUniqueId();
        this.motX = entity.getVelocity().getX();
        this.motY = entity.getVelocity().getY();
        this.motZ = entity.getVelocity().getZ();
        this.isLiving = entity instanceof LivingEntity;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        CraftWorld world = ((CraftWorld) replaySession.getWorld());
        net.minecraft.server.v1_8_R3.Entity entity = world.createEntity(spawnLocation, entityType.getEntityClass());
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

        replaySession.getSpawnedEntities().put(uniqueId.toString(), entity.getBukkitEntity());
        PacketPlayOutEntityHeadRotation headRotation = new PacketPlayOutEntityHeadRotation(entity, (byte) ((spawnLocation.getYaw() * 256.0F) / 360.0F));
        PacketPlayOutEntity.PacketPlayOutEntityLook entityLook = new PacketPlayOutEntity.PacketPlayOutEntityLook(entity.getId(), (byte) ((spawnLocation.getYaw() * 256.0F) / 360.0F), (byte) ((spawnLocation.getPitch() * 256.0F) / 360.0F), true);
        PacketPlayOutEntityMetadata metadata = new PacketPlayOutEntityMetadata(entity.getId(), entity.getDataWatcher(), true);
        PacketPlayOutEntityVelocity velocity = new PacketPlayOutEntityVelocity(entity);
        PacketPlayOutEntity.PacketPlayOutRelEntityMove movement = new PacketPlayOutEntity.PacketPlayOutRelEntityMove(entity.getId(), (byte) motX, (byte) motY, (byte) motZ, false);

        v1_8_R3.sendPackets(player, headRotation, entityLook, metadata, velocity, movement);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity entity = ((CraftEntity)  replaySession.getSpawnedEntities().get(uniqueId.toString())).getHandle();

        PacketPlayOutEntityDestroy destroy = new PacketPlayOutEntityDestroy(entity.getId());

        v1_8_R3.sendPacket(player, destroy);
    }
}
