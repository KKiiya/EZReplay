package me.lagggpixel.replay.support.nms.recordable.player;

import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.arena.IChat;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatRecordable extends Recordable implements IChat {

    private final UUID sender;
    private final String format;
    private final String content;

    public ChatRecordable(IRecording replay, UUID sender, String format, String content) {
        super(replay);
        this.sender = sender;
        this.format = format;
        this.content = content;
    }

    @Override
    public void play(IReplaySession replaySession, Player player) {
        player.sendMessage(format);
    }

    @Override
    public void unplay(IReplaySession replaySession, Player player) {

    }

    @Override
    public UUID getSender() {
        return sender;
    }

    @Override
    public String getFormat() {
        return format;
    }
}
