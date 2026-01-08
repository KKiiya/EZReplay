package me.lagggpixel.replay.replay.recordables.player;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.data.recordable.Recordable;
import me.lagggpixel.replay.api.replay.data.recordable.RecordableRegistry;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import static me.lagggpixel.replay.api.serializer.ReplayByteBuffer.*;

public class ChatRecordable extends Recordable {

    @Writeable private final short entityId;
    @Writeable private final String format;

    public ChatRecordable(ReplayByteBuffer reader) {
        super(null);
        this.entityId = reader.read(SHORT);
        this.format = reader.read(STRING);
    }

    public ChatRecordable(IRecording replay, UUID sender, String format) {
        super(replay);
        this.entityId = replay.getEntityIndex().getOrRegister(sender);
        this.format = format;
    }

    @Override
    public void play(IReplaySession replaySession) {
        for (Player viewer : replaySession.getViewers()) viewer.sendMessage(format);
    }

    @Override
    public void unplay(IReplaySession replaySession) {

    }

    @Override
    public short getTypeId() {
        return RecordableRegistry.CHAT;
    }

    @Override
    public void write(@NotNull ReplayByteBuffer writer) {
        writer.write(SHORT, entityId);
        writer.write(STRING, format);
    }
}
