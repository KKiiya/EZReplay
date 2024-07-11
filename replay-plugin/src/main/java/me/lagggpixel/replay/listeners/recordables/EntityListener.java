package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.AnimationType;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        recording.getSpawnedEntities().add(entity);
        recording.getLastFrame().addRecordable(Replay.getInstance().getVersionSupport().createEntitySpawnRecordable(recording, entity.getLocation(), entity.getType(), entity.getEntityId(), entity.getUniqueId()));
    }

    @EventHandler
    public void onEntityDie(EntityDeathEvent e) {

    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;

        Recordable damage = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, entity, AnimationType.HURT);
        recording.getLastFrame().addRecordable(damage);
    }

    @EventHandler
    public void onProjectileThrow(ProjectileLaunchEvent e) {
        Entity entity = e.getEntity();
        String worldName = entity.getWorld().getName();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(worldName);

        if (a == null) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
        if (recording == null) return;
    }
}
