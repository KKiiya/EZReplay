package me.lagggpixel.replay.listeners.player;

import me.lagggpixel.replay.Replay;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        Player player = e.getPlayer();

        if (e.isCancelled()) return;

        IRecording recording = Replay.getInstance().getReplayManager().getActiveRecording(player.getWorld());
        if (recording == null) return;
        if (!recording.isRecordingChat()) return;

        Recordable recordable = Replay.getInstance().getVersionSupport().createChatRecordable(recording, player.getUniqueId(), e.getFormat(), e.getMessage());
        recording.getLastFrame().addRecordable(recordable);
    }
}
