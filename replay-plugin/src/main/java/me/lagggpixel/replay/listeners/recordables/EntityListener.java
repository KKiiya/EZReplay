package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;
        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        recording.getSpawnedEntities().add(entity);
        if (entity instanceof Item) return;

        Recordable spawn = Replay.getInstance().getVersionSupport().createEntitySpawnRecordable(recording, entity);
        recording.getLastFrame().addRecordable(spawn);
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent e) {
        Entity entity = e.getDamager();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;
        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        if (!(entity instanceof LivingEntity)) return;

        Recordable spawn = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, entity, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(spawn);
    }

    @EventHandler
    public void onEntityDie(EntityDeathEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        recording.getSpawnedEntities().remove(entity);

        if (entity instanceof Item) return;
        if (entity instanceof Projectile) return;
        Recordable recordable = Replay.getInstance().getVersionSupport().createEntityDeathRecordable(recording, entity);
        recording.getLastFrame().addRecordable(recordable);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;
        if (e.isCancelled()) return;
        if (!(entity instanceof LivingEntity)) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        Recordable damage = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, entity, AnimationType.HURT);
        recording.getLastFrame().addRecordable(damage);
    }
}
