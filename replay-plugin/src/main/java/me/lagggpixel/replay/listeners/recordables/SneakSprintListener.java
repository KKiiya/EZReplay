package me.lagggpixel.replay.listeners.recordables;

import com.tomkeuper.bedwars.api.arena.IArena;
import com.tomkeuper.bedwars.arena.Arena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.replay.ReplayManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;

public class SneakSprintListener implements Listener {

    @EventHandler
    public void onBlock(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        Action action = e.getAction();
        IArena a = Arena.getArenaByPlayer(player);

        if (a == null) return;
        if (e.isCancelled()) return;
        if (action != Action.RIGHT_CLICK_AIR) return;
        if (!player.isBlocking()) return;

        IRecording recording = ReplayManager.getInstance().getActiveReplay(a);
        if (recording == null) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createSwordBlockRecordable(recording, player);
        recording.getLastFrame().addRecordable(recordable);
    }
}
