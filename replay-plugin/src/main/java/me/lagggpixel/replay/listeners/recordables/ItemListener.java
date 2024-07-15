package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemListener {

    public static class LegacyDropPick implements Listener {

        @EventHandler
        public void onItemSpawn(EntitySpawnEvent e) {
            Entity entity = e.getEntity();
            String identifier = entity.getWorld().getName();
            IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(identifier);

            if (a == null) return;
            if (!(entity instanceof Item)) return;
            if (e.isCancelled()) return;
            Item item = (Item) entity;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
            if (recording == null) return;

            Recordable recordable = Replay.getInstance().getVersionSupport().createItemDropRecordable(recording, item);
            recording.getLastFrame().addRecordable(recordable);
        }

        @EventHandler
        public void onPick(PlayerPickupItemEvent e) {
            Player p = e.getPlayer();
            IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByPlayer(p);
            Item item = e.getItem();

            if (a == null) return;
            if (e.isCancelled()) return;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
            if (recording == null) return;

            recording.getSpawnedEntities().remove(item);
            Recordable recordable = Replay.getInstance().getVersionSupport().createItemPickRecordable(recording, item, p);
            recording.getLastFrame().addRecordable(recordable);
        }
    }

    public static class NewDropPick implements Listener {

        @EventHandler
        public void onItemSpawn(EntitySpawnEvent e) {
            Entity entity = e.getEntity();
            String identifier = entity.getWorld().getName();
            IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(identifier);

            if (a == null) return;
            if (!(entity instanceof Item)) return;
            if (e.isCancelled()) return;
            Item item = (Item) entity;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
            if (recording == null) return;

            Recordable recordable = Replay.getInstance().getVersionSupport().createItemDropRecordable(recording, item);
            recording.getLastFrame().addRecordable(recordable);
        }

        @EventHandler
        public void onPickup(EntityPickupItemEvent e) {
            Entity entity = e.getEntity();
            String identifier = entity.getWorld().getName();
            IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByIdentifier(identifier);
            Item item = e.getItem();

            if (a == null) return;
            if (e.isCancelled()) return;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveReplay(a);
            if (recording == null) return;

            recording.getSpawnedEntities().remove(item);
            Recordable recordable = Replay.getInstance().getVersionSupport().createItemPickRecordable(recording, item, entity);
            recording.getLastFrame().addRecordable(recordable);
        }
    }
}
