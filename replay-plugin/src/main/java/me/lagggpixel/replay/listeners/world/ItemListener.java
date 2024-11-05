package me.lagggpixel.replay.listeners.world;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;

public class ItemListener {

    public static class LegacyDropPick implements Listener {

        @EventHandler
        public void onItemSpawn(EntitySpawnEvent e) {
            Entity entity = e.getEntity();

            if (!(entity instanceof Item)) return;
            if (e.isCancelled()) return;
            Item item = (Item) entity;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(entity.getWorld());
            if (recording == null) return;

            Recordable recordable = Replay.getInstance().getVersionSupport().createItemDropRecordable(recording, item);
            recording.getLastFrame().addRecordable(recordable);
        }

        @EventHandler
        public void onPick(PlayerPickupItemEvent e) {
            Player p = e.getPlayer();
            Item item = e.getItem();

            if (e.isCancelled()) return;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(p.getWorld());
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

            if (!(entity instanceof Item)) return;
            if (e.isCancelled()) return;
            Item item = (Item) entity;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(entity.getWorld());
            if (recording == null) return;

            Recordable recordable = Replay.getInstance().getVersionSupport().createItemDropRecordable(recording, item);
            recording.getLastFrame().addRecordable(recordable);
        }

        @EventHandler
        public void onPickup(EntityPickupItemEvent e) {
            Entity entity = e.getEntity();
            Item item = e.getItem();

            if (e.isCancelled()) return;

            IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(entity.getWorld());
            if (recording == null) return;

            recording.getSpawnedEntities().remove(item);
            Recordable recordable = Replay.getInstance().getVersionSupport().createItemPickRecordable(recording, item, entity);
            recording.getLastFrame().addRecordable(recordable);
        }
    }
}
