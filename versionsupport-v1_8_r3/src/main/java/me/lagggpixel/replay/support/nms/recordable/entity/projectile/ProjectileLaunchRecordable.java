package me.lagggpixel.replay.support.nms.recordable.entity.projectile;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.utils.Vector3d;
import me.lagggpixel.replay.support.nms.v1_8_R3;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftLivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

public class ProjectileLaunchRecordable extends Recordable {

    @Writeable private final EntityType type;
    @Writeable private final short shooterId;
    @Writeable private final short entityId;
    @Writeable private final Vector3d position;
    @Writeable private final Vector3d velocity;
    @Writeable private final float yaw;
    @Writeable private final float pitch;

    public ProjectileLaunchRecordable(IRecording replay, Entity shooter, Projectile projectile) {
        super(replay);
        EntityIndex index = replay.getEntityIndex();
        this.entityId = index.getOrRegister(projectile.getUniqueId());
        this.shooterId = index.getOrRegister(shooter.getUniqueId());
        
        Location loc = projectile.getLocation();
        this.position = new Vector3d(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        this.velocity = new Vector3d(projectile.getVelocity());
        this.yaw = loc.getYaw();
        this.pitch = loc.getPitch();
        this.type = projectile.getType();
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        Entity shooter = replaySession.getSpawnedEntities().get(this.shooterId);
        if (shooter == null) return;

        CraftWorld craftWorld = (CraftWorld) player.getWorld();
        WorldServer world = (WorldServer) craftWorld.getHandle();
        
        // Create the appropriate projectile entity
        net.minecraft.server.v1_8_R3.Entity nmsProjectile = createProjectile(world, shooter, type);
        if (nmsProjectile == null) return;

        // Set position and rotation
        nmsProjectile.setPositionRotation(position.getX(), position.getY(), position.getZ(), yaw, pitch);

        // Set velocity (motion)
        nmsProjectile.motX = velocity.getX();
        nmsProjectile.motY = velocity.getY();
        nmsProjectile.motZ = velocity.getZ();

        // Set shooter if projectile supports it
        if (nmsProjectile instanceof IProjectile) {
            IProjectile proj = (IProjectile) nmsProjectile;
            if (shooter instanceof LivingEntity) {
                proj.shoot(pitch, yaw, 0.0F, 
                    (float) Math.sqrt(velocity.getX() * velocity.getX() + 
                                     velocity.getY() * velocity.getY() + 
                                     velocity.getZ() * velocity.getZ()), 
                    1.0F);
            }
        }

        // Special handling for specific projectile types
        configureProjectile(nmsProjectile, shooter);

        // Store in session
        Entity bukkitProjectile = nmsProjectile.getBukkitEntity();
        replaySession.getSpawnedEntities().put(this.entityId, bukkitProjectile);

        // Send spawn packet to player
        PacketPlayOutSpawnEntity spawnPacket = new PacketPlayOutSpawnEntity(nmsProjectile, getObjectId(nmsProjectile));
        PacketPlayOutEntityMetadata metadataPacket = new PacketPlayOutEntityMetadata(nmsProjectile.getId(), nmsProjectile.getDataWatcher(), true);
        v1_8_R3.sendPackets(player, spawnPacket, metadataPacket);
        
        // Send velocity packet
        PacketPlayOutEntityVelocity velocityPacket = new PacketPlayOutEntityVelocity(
            nmsProjectile.getId(),
            velocity.getX(),
            velocity.getY(),
            velocity.getZ()
        );
        v1_8_R3.sendPacket(player, velocityPacket);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {
        Entity projectile = replaySession.getSpawnedEntities().get(this.entityId);
        if (projectile != null) {
            // Send destroy packet
            PacketPlayOutEntityDestroy destroyPacket = new PacketPlayOutEntityDestroy(
                projectile.getEntityId()
            );
            v1_8_R3.sendPacket(player, destroyPacket);
            
            // Remove from world
            projectile.remove();
            replaySession.getSpawnedEntities().remove(this.entityId);
        }
    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.PROJECTILE_LAUNCH;
    }

    /**
     * Create the appropriate NMS projectile entity based on EntityType
     */
    private net.minecraft.server.v1_8_R3.Entity createProjectile(World world, Entity shooter, EntityType type) {
        switch (type) {
            case ARROW:
                return new EntityArrow(world);
            case SNOWBALL:
                return new EntitySnowball(world);
            case EGG:
                return new EntityEgg(world);
            case FIREBALL:
                return new EntityLargeFireball(world);
            case SMALL_FIREBALL:
                return new EntitySmallFireball(world);
            case ENDER_PEARL:
                return new EntityEnderPearl(world);
            case THROWN_EXP_BOTTLE:
                return new EntityThrownExpBottle(world);
            case SPLASH_POTION:
                return new EntityPotion(world);
            case FISHING_HOOK:
                if (shooter instanceof Player) return new EntityFishingHook(world, (EntityHuman) shooter);
                return null;
            case WITHER_SKULL:
                return new EntityWitherSkull(world);
            case FIREWORK:
                return new EntityFireworks(world);
            default:
                return null;
        }
    }

    /**
     * Configure projectile-specific properties
     */
    private void configureProjectile(net.minecraft.server.v1_8_R3.Entity projectile, Entity shooter) {
        // Set shooter for projectiles that track it
        if (projectile instanceof EntityArrow && shooter instanceof LivingEntity) {
            EntityArrow arrow = (EntityArrow) projectile;
            arrow.shooter = ((CraftLivingEntity) shooter).getHandle();
        } else if (projectile instanceof EntityFireball && shooter instanceof LivingEntity) {
            EntityFireball fireball = (EntityFireball) projectile;
            fireball.shooter = ((CraftLivingEntity) shooter).getHandle();
        } else if (projectile instanceof EntityProjectile && shooter instanceof LivingEntity) {
            EntityProjectile proj = (EntityProjectile) projectile;
            proj.shooter = ((CraftLivingEntity) shooter).getHandle();
        }
    }

    /**
     * Get the object type ID for spawn packet
     */
    private int getObjectId(net.minecraft.server.v1_8_R3.Entity entity) {
        if (entity instanceof EntityArrow) return 60;
        if (entity instanceof EntitySnowball) return 61;
        if (entity instanceof EntityEgg) return 62;
        if (entity instanceof EntityLargeFireball) return 63;
        if (entity instanceof EntitySmallFireball) return 64;
        if (entity instanceof EntityEnderPearl) return 65;
        if (entity instanceof EntityWitherSkull) return 66;
        if (entity instanceof EntityThrownExpBottle) return 75;
        if (entity instanceof EntityPotion) return 73;
        if (entity instanceof EntityFireworks) return 76;
        if (entity instanceof EntityFishingHook) return 90;
        return 60; // Default to arrow
    }
}