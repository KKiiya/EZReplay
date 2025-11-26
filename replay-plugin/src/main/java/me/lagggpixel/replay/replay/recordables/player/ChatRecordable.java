package me.lagggpixel.replay.replay.recordables.player;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatRecordable extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final String format;
    @Writeable private final String content;

    public ChatRecordable(IRecording replay, UUID sender, String format, String content) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(sender);
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
    public short getTypeId() {
        return RecordableRegistry.CHAT;
    }
}
