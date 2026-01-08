package me.lagggpixel.replay.api.replay.data.recordable;

import me.lagggpixel.replay.api.data.Writeable;
import me.lagggpixel.replay.api.replay.content.IReplaySession;
import me.lagggpixel.replay.api.replay.data.EntityIndex;
import me.lagggpixel.replay.api.replay.data.IRecording;
import me.lagggpixel.replay.api.replay.serialize.BinarySerializable;
import me.lagggpixel.replay.api.serializer.ReplayByteBuffer;
import org.bukkit.entity.Player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Objects;
import java.util.UUID;

/**
 * @author Lagggpixel
 * @since May 01, 2024
 */
public abstract class Recordable implements ReplayByteBuffer.Writer {
    private final IRecording replay;

    public Recordable(IRecording replay) {
        this.replay = replay;
    }

    public IRecording getRecording() {
        return replay;
    }

    public abstract void play(IReplaySession replaySession);

    public abstract void unplay(IReplaySession replaySession);

    public abstract short getTypeId();


    public int getIdentityHash() {
        return System.identityHashCode(this);
    }

}
