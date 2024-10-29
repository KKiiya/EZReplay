package me.lagggpixel.replay.listeners.arena;

import com.tomkeuper.bedwars.api.arena.IArena;
import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();
        IArena a = Replay.getInstance().getBedWarsAPI().getArenaUtil().getArenaByPlayer(player);

        if (a == null) return;
        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(a);
        if (recording == null) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createChatRecordable(recording, player.getUniqueId(), e.getFormat(), e.getMessage());
        recording.getLastFrame().addRecordable(recordable);
    }
}
