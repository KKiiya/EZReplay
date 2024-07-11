package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.entity.player.AnimationType;
import me.lagggpixel.replay.replay.ReplayManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

public class PlayerListener implements Listener {

    @EventHandler
    public void onArmSwing(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) return;
        if (e.getAction() != Action.LEFT_CLICK_AIR && e.getAction() != Action.LEFT_CLICK_BLOCK) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        Recordable animation = Replay.getInstance().getVersionSupport().createAnimationRecordable(recording, player, AnimationType.SWING_MAIN_HAND);
        recording.getLastFrame().addRecordable(animation);
    }
}
