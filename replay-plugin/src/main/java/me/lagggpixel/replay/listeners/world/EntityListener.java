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

public class EntityListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        if (entity instanceof Item) return;
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
        Entity entity = e.getEntity();
        World world = entity.getWorld();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(world);
        if (recording == null) return;

        Recordable spawn = Replay.getInstance().getVersionSupport().createEntitySpawnRecordable(recording, entity);
        recording.getLastFrame().addRecordable(spawn);
    }
}
