package me.lagggpixel.replay.listeners.player;

import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.utils.entity.AnimationType;
import me.lagggpixel.replay.replay.ReplayManager;
import me.lagggpixel.replay.replay.recordables.entity.entity.Animation;
import me.lagggpixel.replay.replay.recordables.entity.entity.EntityRecordable;
import me.lagggpixel.replay.replay.recordables.entity.player.Respawn;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();

        IRecording recording = ReplayManager.getInstance().getActiveRecording(player.getWorld());
        if (recording == null) return;
        if (player.getVehicle() != null) return;

        Recordable movement = new EntityRecordable(recording, player);
        recording.getLastFrame().addRecordable(movement);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        Player player = e.getPlayer();

        IRecording recording = ReplayManager.getInstance().getActiveRecording(player.getWorld());
        if (recording == null) return;

        Recordable respawn = new Respawn(recording, player);
        recording.getLastFrame().addRecordable(respawn);
    }

    @EventHandler
    public void onArmSwing(PlayerInteractEvent e) {
        Player player = e.getPlayer();

        if (e.isCancelled()) return;
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        IRecording recording = ReplayManager.getInstance().getActiveRecording(player.getWorld());
        if (recording == null) return;

        Recordable animation = new Animation(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(animation);
    }


}
