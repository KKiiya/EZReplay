package me.lagggpixel.replay.listeners.world;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.projectiles.ProjectileSource;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        if (entity instanceof Item) return;
        if (entity instanceof Projectile) return;
        if (entity instanceof LivingEntity) recording.getSpawnedEntities().add(entity);

        Recordable spawn = Replay.getInstance().getVersionSupport().createEntitySpawnRecordable(recording, entity);
        recording.getLastFrame().addRecordable(spawn);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        World world = entity.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        if (!(entity instanceof LivingEntity)) return;

        Recordable spawn = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, entity, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(spawn);
    }

    @EventHandler
    public void onEntityDie(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        recording.getSpawnedEntities().remove(entity);

        Recordable recordable = Replay.getInstance().getVersionSupport().createEntityDeathRecordable(recording, entity);
        recording.getLastFrame().addRecordable(recordable);
    }

    @EventHandler
    public void onExplosion(ExplosionPrimeEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        Recordable explosion = Replay.getInstance().getVersionSupport().createExplosionRecordable(recording, entity.getLocation(), e.getEntity(), e.getRadius());
        recording.getLastFrame().addRecordable(explosion);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        if (e.isCancelled()) return;
        if (!(entity instanceof LivingEntity)) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        Recordable damage = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, entity, AnimationType.HURT);
        recording.getLastFrame().addRecordable(damage);
    }

    @EventHandler
    public void onProjectile(ProjectileLaunchEvent e) {
        ProjectileSource shooter = e.getEntity().getShooter();
        if (!(shooter instanceof Entity)) return;

        Projectile projectile = e.getEntity();
        World world = projectile.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        Recordable spawn = Replay.getInstance().getVersionSupport().createProjectileLaunchRecordable(recording, (Entity) shooter, projectile);
        recording.getLastFrame().addRecordable(spawn);
    }
    
    @EventHandler
    public void onVehicleSpawn(VehicleCreateEvent e) {
        Entity entity = e.getVehicle();
        World world = entity.getWorld();

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;
        if (entity instanceof LivingEntity) return;
        recording.getSpawnedEntities().add(entity);

        Recordable spawn = Replay.getInstance().getVersionSupport().createEntitySpawnRecordable(recording, entity);
        recording.getLastFrame().addRecordable(spawn);
    }

    @EventHandler
    public void onVehicleRide(VehicleEnterEvent e) {
        Entity vehicle = e.getVehicle();
        Entity entity = e.getEntered();
        World world = vehicle.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        Recordable ride = Replay.getInstance().getVersionSupport().createEntityRideRecordable(recording, vehicle, entity);
        recording.getLastFrame().addRecordable(ride);
    }
}
